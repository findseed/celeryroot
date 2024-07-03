package celeryroot.celery;

import celeryroot.celery.config.Config;
import celeryroot.celery.config.DebugMap;
import celeryroot.game.Game;
import celeryroot.game.inputs.InputUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;

//general manager of the Thing
public class Celery {

    public static HashMap<CellPos, CellData> globalCellMap = new HashMap<>(Config.initCapacities);
    public static ArrayList<CellPos> globalCellList = new ArrayList<>(Config.initCapacities);
    public static Thread[] threads;
    public static StalkThread[] stalks;

    public static CyclicBarrier syncBarrier = new CyclicBarrier(Config.threads, Celery::syncThreads);

    //lowest score completion yet
    public static int globalBestScore = Integer.MAX_VALUE;

    private static long lastSyncTime = 0;

    public volatile static boolean syncFlag = false;


    //useless test thing that is useful
    public static ArrayList<CellPos> initCells = new ArrayList<>(1000);
    //pre-adds all cells that a set of inputs goes trhough
    public static void addInitCellInputs(String path){
        short[] inputs = InputUtil.readTASfile(path);
        if(inputs == null)
            return;
        Game initGame = Game.getInitGame();

        //root cell
        CellPos initPos = new CellPos();
        CellData prevData = new CellData();
        Config.assignCell(initGame, initPos);
        globalCellMap.put(initPos, prevData);
        globalCellList.add(initPos);

        CellPos currentCell = new CellPos(initPos);
        CellPos testCell = new CellPos();

        int lastSegmentIn = -1;
        for (int f = 0; f < inputs.length; ++f) {
            InputUtil.applyInput(initGame.inputs, inputs[f]);
            initGame.tick();
            if(Config.trimBranch(initGame))
                System.out.println("!!!! init inputs went oob !!!!");
            Config.assignCell(initGame, testCell);
            if(!testCell.equals(currentCell)){
                currentCell.set(testCell);
                short[] segmentInputs = new short[f - lastSegmentIn];
                System.arraycopy(inputs, lastSegmentIn + 1, segmentInputs, 0, f - lastSegmentIn);
                InputNode newNode = new InputNode(segmentInputs, prevData.inputs);
                CellPos c = new CellPos(testCell);
                prevData = new CellData(Config.getScore(initGame), newNode);
                lastSegmentIn = f;
                globalCellMap.put(c, prevData);
                globalCellList.add(c);
                initCells.add(c);
            }
        }
    }

    //prints info about the initCells and the best ways to get to them
    //useful to compare over time to see why it thinks it's better than you(it's not(it's stupid(i hate computers)))
    public static void dumpInitCells(){
        for(CellPos pos : initCells){
            CellData cellData = globalCellMap.get(pos);
            System.out.printf("%s - %d\n", pos, cellData.score);
            short[][] cellInputs = cellData.inputs.traceInputs();

            String fileName = String.format("%s - _DUMP_%s__%s.tas", Config.TASname, pos, UUID.randomUUID());
            InputUtil.writeTASfile(Config.outputFolder, fileName, null, cellInputs);
        }
    }

    //adds the root cell for everything to becometh
    public static void addInitCell(){
        Game initGame = Game.getInitGame();
        CellPos initPos = new CellPos();
        CellData initData = new CellData();
        Config.assignCell(initGame, initPos);
        globalCellMap.put(initPos, initData);
        globalCellList.add(initPos);
    }

    //inits/starts threads and lets them take everything from there...
    public static void start(){
        //add starting cell
        addInitCell();

        //off to the races
        lastSyncTime = System.currentTimeMillis();
        threads = new Thread[Config.threads];
        stalks = new StalkThread[Config.threads];
        for (int i = 0; i < Config.threads; i++) {
            stalks[i] = new StalkThread(i);
            threads[i] = new Thread(stalks[i]);
            threads[i].start();
        }
    }


    //runs every so many thread iterations to sync the global lists with everything they've found
    //threads will wait for each other before running so nothing gets Messy cuz Lazy
    private static void syncThreads(){
        syncFlag = false;
        //lazy stat info
        int totalFrames = 0;
        int totalRerecords = 0;
        for (int i = 0; i < threads.length; i++) {
            StalkThread s = stalks[i];
            totalFrames+= s.frameCount;
            totalRerecords+= s.rerecordCount;
            s.rerecordCount = 0;
            s.frameCount = 0;
        }
        System.out.printf("ran %d frames and %d rerecords at %.1f fps\n", totalFrames, totalRerecords, totalFrames / ((System.currentTimeMillis() - lastSyncTime) / 1_000f));
        lastSyncTime = System.currentTimeMillis();

        //sync
        int preSyncCells = globalCellList.size();
        for (int i = 0; i < threads.length; i++) {
            stalks[i].syncCells();
        }
        System.out.printf("synced %d cells(+%d) in %.3f\n", globalCellList.size(), globalCellList.size() - preSyncCells, (System.currentTimeMillis() - lastSyncTime) / 1_000f);


        //map
        if(Config.printDebugMap)
            System.out.println(DebugMap.createDebugMap());
    }

}
