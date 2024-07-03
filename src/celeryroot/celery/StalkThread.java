package celeryroot.celery;

import celeryroot.celery.config.Config;
import celeryroot.game.Game;
import celeryroot.game.inputs.InputUtil;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//obj for each thread thats doing the calclaiccing
public class StalkThread implements Runnable{

    public final int id;
    public HashMap<CellPos, CellData> localCellMap = new HashMap<>(Config.initCapacities);
    public long syncWaitTime;
    public int frameCount;
    public int rerecordCount;

    //lowest score completion on this block
    public int localBestScore = Integer.MAX_VALUE;

    //game objects for managing save state esque stuff
    private final Game initGame = Game.getInitGame(); //initial game state to return to
    private final Game saveGame = Game.getInitGame(); //game to copy intermediate states to
    private final Game tickGame = Game.getInitGame(); //game actually used to branch/run stuffs

    //friends for testing when we leave places
    private final CellPos testPos = new CellPos();
    private final CellPos newPos = new CellPos();

    //silly thing to focus good
    private final ArrayList<CellPos> completionFocus = new ArrayList<>();

    Random masterRandom = ThreadLocalRandom.current();

    public StalkThread(int threadID){
        id = threadID;
    }


    @Override
    public void run() {
        //main loop
        for (int itt = 0; ; ++itt) {
            if(id == 0 && (itt & Config.syncCount) == 0){ //trigger sync
                Celery.syncFlag = true;
            }
            if(Celery.syncFlag){ //wait for sync
                syncWaitTime = System.currentTimeMillis();
                try {
                    Celery.syncBarrier.await();
                } catch (Exception ignored) {
                }
            }


            //otherwise find a cell to branch off of
            CellPos indexPos;
            CellData indexData;
            if(masterRandom.nextInt(Config.completionFocusChance) == 0 && !completionFocus.isEmpty()){
                indexPos = completionFocus.get(masterRandom.nextInt(completionFocus.size())); //focus a good route
            }else{
                indexPos = Celery.globalCellList.get(masterRandom.nextInt(Celery.globalCellList.size())); //pick a random
            }
            indexData = localCellMap.get(indexPos);
            if(indexData == null)
                indexData = Celery.globalCellMap.get(indexPos);
            if(indexData == null)
                continue; //THIS SHOULDN't HAPPEN BUT IT DOES BECAUSE completionfocus ADDS A BAD CELL CUZ IM LAZY

            //idk man
            if(itt % Config.prevInputChance == 0)
                mutatePrevious(indexPos, indexData); // :|
            else //branch new
                branchFutures(indexPos, indexData);
        }
    }


    //gives everything we own to the Boss
    public void syncCells(){
        //sync completion times
        if(localBestScore < Celery.globalBestScore)
            Celery.globalBestScore = localBestScore;
        else
            localBestScore = Celery.globalBestScore;
        //sync new cells 🤓
        for(Map.Entry<CellPos, CellData> e : localCellMap.entrySet()){
            CellPos impPos = e.getKey();
            CellData impData = e.getValue();
            CellData curData = Celery.globalCellMap.get(impPos);
            if(curData == null){
                Celery.globalCellList.add(impPos);
                Celery.globalCellMap.put(impPos, impData);
            } else if (impData.score <= curData.score)
                Celery.globalCellMap.put(impPos, impData);
        }
        //dont need these anymore
        localCellMap.clear();
    }

    //reruns inputs from init state to (hopefully) recreate the state of a cell
    private void recreateState(short[][] inputs){
        tickGame.copyFrom(initGame);
        for(short[] inputArray : inputs){
            for(short data : inputArray){
                InputUtil.applyInput(tickGame.inputs, data);
                tickGame.tick();
            }
        }
    }

    //add all the cells this completion went thru to our thingie
    private void saveCompletion(short[][] inputs){
        completionFocus.clear();
        Game finishGame = Game.getInitGame();
        CellPos currentCell = new CellPos();
        Config.assignCell(finishGame, currentCell);
        for(short[] inputArray : inputs){
            for(short data : inputArray){
                InputUtil.applyInput(finishGame.inputs, data);
                finishGame.tick();
                Config.assignCell(finishGame, newPos);
                if(!newPos.equals(currentCell)){
                    currentCell.set(newPos);
                    completionFocus.add(new CellPos(currentCell));
                }
            }
        }
    }

    //outputs a solution to a TAS file/prints it whatever
    private void outputCompletion(Game game, short[][] inputs, int finalScore){
        saveCompletion(inputs); //save the cells to use later
        localBestScore = finalScore;
        String alias = Config.scoreAlias(game);
        String fileName = String.format("%s - %s__%s.tas", Config.TASname, alias, UUID.randomUUID());
        String extrainfo = String.format("finished at %s", game.player.toString());
        InputUtil.writeTASfile(Config.outputFolder, fileName, extrainfo, inputs);
        System.out.printf("Solution in %s(%d)! Finished at %s %s\n", alias, finalScore, game.player.toString(), LocalTime.now());
    }

    //tries new branches starting from where a cell left off for anything better
    private void branchFutures(CellPos cellPos, CellData cellData){
        short[][] cellInputs = cellData.inputs.traceInputs();

        recreateState(cellInputs);

        //tickGame should now have the same state the old cell left off at
        Config.assignCell(tickGame, testPos);
        if(!testPos.equals(cellPos)) { //if not something went terribly wrong
            System.out.printf("desync! expected cell %s but got %s instead! skipping...\n", cellPos, testPos);
            return;
        }

        saveGame.copyFrom(tickGame); //save state

        //start branching
        for (int branch = 0; branch < Config.branchInputCount; ++branch) {
            ++rerecordCount;
            tickGame.copyFrom(saveGame); //loadstate
            testPos.set(cellPos); //if we leave this cell then we've found smthn new
            short[] branchInputs = new short[Config.branchInputLength];
            //old celery would tick frames where the player didnt update for free(mostly so transitions didn't confuse it)
            //but since only 1 room here im going to Not Do That and hope there wasn't another reason i'm forgetting
            for (int f = 0; f < Config.branchInputLength; ++f) {
                branchInputs[f] = Config.inputFiddler(tickGame, masterRandom);
                tickGame.tick();
                ++frameCount;
                if (Config.trimBranch(tickGame))
                    break;
                if (Config.goalCondition(tickGame)) { //met our goal wrap it upppp
                    int finalScore = Config.getFinalScore(tickGame);
                    if (finalScore < localBestScore && finalScore < Celery.globalBestScore) {
                        short[] finalInputs = new short[f + 1];
                        System.arraycopy(branchInputs, 0, finalInputs, 0, f + 1);
                        short[][] fullInputs = new short[cellInputs.length + 1][];
                        System.arraycopy(cellInputs, 0, fullInputs, 0, cellInputs.length);
                        fullInputs[cellInputs.length] = finalInputs;
                        outputCompletion(tickGame, fullInputs, finalScore);
                    }
                    if (Config.terminateCompletions)
                        break;
                }
                //have we found anything interesting...
                Config.assignCell(tickGame, newPos);
                if (!newPos.equals(testPos)) { //left cell!
                    testPos.set(newPos); //update current cell
                    int score = Config.getScore(tickGame);
                    //have we been here before
                    CellData existingCell = localCellMap.get(newPos);
                    if (existingCell == null)
                        Celery.globalCellMap.get(newPos);
                    //if new territory or better, track it
                    if (existingCell == null || score <= existingCell.score) { //save equivalent stuff too to encourage Variety
                        short[] newInputs = new short[f + 1];
                        System.arraycopy(branchInputs, 0, newInputs, 0, f + 1);
                        InputNode newNode = new InputNode(newInputs, cellData.inputs);
                        localCellMap.put(new CellPos(newPos), new CellData(score, newNode));
                    }
                }
            }
        }
    }

    //tries editing inputs leading up to this cell for anything better
    private void mutatePrevious(CellPos cellPos, CellData cellData){
        //# of inputs to mess up
        int prevCount = masterRandom.nextInt(Config.minPrevInputs, Config.maxPrevInputs);
        //go backwards and find said inputs
        InputNode prevStartNode = cellData.inputs;
        if(prevStartNode.prev == null)
            return; //nothing came before us
        int revCount = 0;
        ArrayList<short[]> prevInputList = new ArrayList<>();
        while(revCount < prevCount){ //trace nodes back + store inputs
            revCount+= prevStartNode.inputs.length;
            prevInputList.add(prevStartNode.inputs);
            prevStartNode = prevStartNode.prev;
            if(prevStartNode.inputs == null){ //hit the start settle for what we have
                prevCount = revCount;
                break;
            }
        }
        //prevStartNode is now the closest node to branch off of
        //but prolly means extra frames, so where does the stuff we want start?
        int startOffset = revCount - prevCount;

        //consolidate those inputs for MANGLING
        short[] baseInputs = new short[revCount];
        int in = 0;
        for (short[] inputs : prevInputList) {
            in += inputs.length;
            System.arraycopy(inputs, 0, baseInputs, revCount - in, inputs.length); //imagine forgetting they're backwards
        }

        //get a game state to this start(just have to blindly trust that it's right)
        short[][] prevPrevInputs = prevStartNode.traceInputs();
        recreateState(prevPrevInputs);
        //extra stuff we're not changing
        for(int f = 0; f < startOffset; ++f){
            InputUtil.applyInput(tickGame.inputs, baseInputs[f]);
            tickGame.tick();
        }
        saveGame.copyFrom(tickGame);
        CellPos startCell = new CellPos();
        Config.assignCell(tickGame, startCell); //cell we're starting from

        //now do the modifying stuffz
        for (int branch = 0; branch < Config.branchInputCount; ++branch) {
            ++rerecordCount;
            tickGame.copyFrom(saveGame); //loadstate
            testPos.set(startCell); //if we leave this cell then we've found smthn new
            short[] branchInputs = Config.prevInputFiddler(baseInputs, startOffset, masterRandom);
            //run the inputs
            for (int f = startOffset; f < branchInputs.length; ++f) {
                InputUtil.applyInput(tickGame.inputs, branchInputs[f]);
                tickGame.tick(); //this should maybe be a seperate method idk
                ++frameCount;
                if (Config.trimBranch(tickGame))
                    break;
                if (Config.goalCondition(tickGame)) { //met our goal wrap it upppp
                    int finalScore = Config.getFinalScore(tickGame);
                    if (finalScore < localBestScore && finalScore < Celery.globalBestScore) {
                        short[] finalInputs = new short[f + 1];
                        System.arraycopy(branchInputs, 0, finalInputs, 0, f + 1);
                        short[][] fullInputs = new short[prevPrevInputs.length + 1][];
                        System.arraycopy(prevPrevInputs, 0, fullInputs, 0, prevPrevInputs.length);
                        fullInputs[prevPrevInputs.length] = finalInputs;
                        outputCompletion(tickGame, fullInputs, finalScore);
                    }
                    if (Config.terminateCompletions)
                        break;
                }
                ////have we found anything interesting...
                Config.assignCell(tickGame, newPos);
                if (!newPos.equals(testPos)) { //left cell!
                    testPos.set(newPos); //update current cell
                    int score = Config.getScore(tickGame);
                    //have we been here before
                    CellData existingCell = localCellMap.get(newPos);
                    if (existingCell == null)
                        Celery.globalCellMap.get(newPos);
                    //new territory or better, track it
                    if (existingCell == null || score <= existingCell.score) { //save equivalent stuff to encourage ~Variety~
                        short[] newInputs = new short[f + 1];
                        System.arraycopy(branchInputs, 0, newInputs, 0, f + 1);
                        InputNode newNode = new InputNode(newInputs, prevStartNode);
                        localCellMap.put(new CellPos(newPos), new CellData(score, newNode));
                    }
                }
            }
        }
    }

}
