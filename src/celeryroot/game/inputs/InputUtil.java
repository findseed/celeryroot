package celeryroot.game.inputs;

//helpful stuff for reading/writing/parsing inputs
//either to/from files or Inputs objects

import celeryroot.celery.config.Config;
import celeryroot.game.Game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class InputUtil {

    static final char LEFT_KEY =   'L';
    static final char RIGHT_KEY =  'R';
    static final char UP_KEY =     'U';
    static final char DOWN_KEY =   'D';
    static final char JUMP_KEY0 =  'J';
    static final char JUMP_KEY1 =  'K';
    static final char GRAB_KEY0 =  'G';
    static final char GRAB_KEY1 =  'H';
    static final char DASH_KEY0 =  'X';
    static final char DASH_KEY1 =  'C';
    static final char CDASH_KEY0 = 'Z';
    static final char CDASH_KEY1 = 'V';

    public static final int JUMP_PRESS     = 0b0000_0000_0000_0001;
    public static final int GRAB_PRESS     = 0b0000_0000_0000_0010;
    public static final int DASH_PRESS     = 0b0000_0000_0000_0100;
    public static final int CDASH_PRESS    = 0b0000_0000_0000_1000;
    public static final int JUMP_UNPRESS   = 0b0000_0000_0001_0000;
    public static final int GRAB_UNPRESS   = 0b0000_0000_0010_0000;
    public static final int DASH_UNPRESS   = 0b0000_0000_0100_0000;
    public static final int CDASH_UNPRESS  = 0b0000_0000_1000_0000;
    public static final int X_INPUT        = 0b0000_1111_0000_0000;
    public static final int Y_INPUT        = 0b1111_0000_0000_0000;
    public static final int X_INPUT_SHIFT  = 8;
    public static final int Y_INPUT_SHIFT  = 12;



    //apply data to inputs
    public static void applyInput(Inputs inputs, int data){
        inputs.jumpButton.press =         (data & JUMP_PRESS) != 0;
        inputs.grabButton.press =         (data & GRAB_PRESS) != 0;
        inputs.dashButton.press =         (data & DASH_PRESS) != 0;
        inputs.crouchDashButton.press =   (data & CDASH_PRESS) != 0;
        inputs.jumpButton.unpress =       (data & JUMP_UNPRESS) != 0;
        inputs.grabButton.unpress =       (data & GRAB_UNPRESS) != 0;
        inputs.dashButton.unpress =       (data & DASH_UNPRESS) != 0;
        inputs.crouchDashButton.unpress = (data & CDASH_UNPRESS) != 0;
        inputs.xInput.pressValue =        ((data >> 8) & 0b11) - 1;
        inputs.yInput.pressValue =        ((data >> 12) & 0b11) - 1;
    }

    //readable info about the input for ME
    public static String inputToString(short data){
        char jumpPress =         getPressJump(data) ? 'X' : '.';
        char grabPress =         getPressGrab(data) ? 'X' : '.';
        char dashPress =         getPressDash(data) ? 'X' : '.';
        char cDashPress =        getPressCDash(data) ? 'X' : '.';
        char jumpUnpress =       getUnpressJump(data) ? 'X' : '.';
        char grabUnpress =       getUnpressGrab(data) ? 'X' : '.';
        char dashUnpress =       getUnpressDash(data) ? 'X' : '.';
        char cDashUnpress =      getUnpressCDash(data) ? 'X' : '.';
        int xInput =             getXInput(data);
        int yInput =             getYInput(data);
        return String.format("X: %2d Y: %2d PRESS: J: %s G: %s D: %s C: %s UNPRESS: J: %s G: %s D: %s C: %s", xInput, yInput, jumpPress, grabPress, dashPress, cDashPress, jumpUnpress, grabUnpress, dashUnpress, cDashUnpress);
    }

    //ez methods to get indv values
    public static int getXInput(short data){
        return ((data >> 8) & 0b11) - 1;
    }
    public static int getYInput(short data){
        return ((data >> 12) & 0b11) - 1;
    }
    public static boolean getPressJump(short data){
        return (data & JUMP_PRESS) != 0;
    }
    public static boolean getPressGrab(short data){
        return (data & GRAB_PRESS) != 0;
    }
    public static boolean getPressDash(short data){
        return (data & DASH_PRESS) != 0;
    }
    public static boolean getPressCDash(short data){
        return (data & CDASH_PRESS) != 0;
    }
    public static boolean getUnpressJump(short data){
        return (data & JUMP_UNPRESS) != 0;
    }
    public static boolean getUnpressGrab(short data){
        return (data & GRAB_UNPRESS) != 0;
    }
    public static boolean getUnpressDash(short data){
        return (data & DASH_UNPRESS) != 0;
    }
    public static boolean getUnpressCDash(short data){
        return (data & CDASH_UNPRESS) != 0;
    }

    //turns the current unregistered press/unpress state of inputs into an input short
    //technically bad cuz if you need this you already know whether they're press/unpressed cuz you set them...
    public static short extractInputs(Inputs inputs){
        int res = (inputs.yInput.pressValue + 1) << 12;
        res|= (inputs.xInput.pressValue + 1) << 8;
        if(inputs.jumpButton.press) res|= JUMP_PRESS;
        if(inputs.grabButton.press) res|= GRAB_PRESS;
        if(inputs.dashButton.press) res|= DASH_PRESS;
        if(inputs.crouchDashButton.press) res|= CDASH_PRESS;
        if(inputs.jumpButton.unpress) res|= JUMP_UNPRESS;
        if(inputs.grabButton.unpress) res|= GRAB_UNPRESS;
        if(inputs.dashButton.unpress) res|= DASH_UNPRESS;
        if(inputs.crouchDashButton.unpress) res|= CDASH_UNPRESS;
        return (short)res;
    }

    //hepl thgnie to shift an button around(for prev input fiddler)
    public static void modifyButton(int minIndex, short[] inputs, int index, int button, Random r, int shiftChance, int shiftAmount, int toggleChance) {
        if ((inputs[index] & button) != 0 && r.nextInt(shiftChance) == 0){ //should shift
            int newIndex = index + r.nextInt(shiftAmount << 1) - shiftAmount;
            if (newIndex < minIndex)
                newIndex = minIndex;
            else if (newIndex >= inputs.length)
                newIndex = inputs.length - 1;
            inputs[index] &= (short) ~button;
            inputs[newIndex] |= (short) button;
        } else if (r.nextInt(toggleChance) == 0) { //should toggle
            inputs[index] ^= (short) button;
        }
    }

    //help thinige to mess with axes(for prev input fiddler)
    public static void modifyAxis(short[] inputs, int index, int shift, Random r, int toggleChance, int resetChance){
        if(r.nextInt(toggleChance) == 0){
            int axis = ((inputs[index] >> shift) & 0b11) - 1;
            inputs[index]&= (short) ~(0b1111 << shift);
            inputs[index]|= (short) ((-axis + 1) << shift);
        } else if (r.nextInt(resetChance) == 0) {
            inputs[index]&= (short) ~(0b1111 << shift);
            inputs[index]|= (short) (1 << shift);
        }
    }


    //returns an input array from the specified file
    public static short[] readTASfile(String path) {
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            ArrayList<Short> inputList = new ArrayList<>();
            String line;
            char lastJump = ' ';
            char lastGrab = ' ';
            char lastDash = ' ';
            char lastCDash = ' ';
            while((line = br.readLine()) != null){
                if(line.length() < 4 | line.contains("#"))
                    continue; //presumably invalid
                String[] arg = line.split(",");
                int frameLength;
                try{
                    frameLength = Integer.parseInt(arg[0].trim());
                    if(frameLength <= 0)
                        continue;
                }catch (Exception e){
                    continue;
                }
                //extract each input for this line
                char curJump = ' ';
                char curGrab = ' ';
                char curDash = ' ';
                char curCDash = ' ';
                int xAxis = 1;
                int yAxis = 1;
                for (int i = 1; i < arg.length; ++i) {
                    char c = arg[i].charAt(0);
                    switch (c){
                        case LEFT_KEY -> xAxis = 0;
                        case RIGHT_KEY -> xAxis = 2;
                        case UP_KEY -> yAxis = 0;
                        case DOWN_KEY -> yAxis = 2;
                        case JUMP_KEY0, JUMP_KEY1 -> curJump = c;
                        case GRAB_KEY0, GRAB_KEY1 -> curGrab = c;
                        case DASH_KEY0, DASH_KEY1 -> curDash = c;
                        case CDASH_KEY0, CDASH_KEY1 -> curCDash = c;
                    }
                }
                //write the data
                short data = 0;
                data|= (short) (xAxis << 8);
                data|= (short) (yAxis << 12);
                if(lastJump != curJump) {
                    data |= (short) (curJump != ' ' ? JUMP_PRESS : JUMP_UNPRESS);
                    lastJump = curJump;
                } if(lastGrab != curGrab) {
                    data |= (short) (curGrab != ' ' ? GRAB_PRESS : GRAB_UNPRESS);
                    lastGrab = curGrab;
                } if(lastDash != curDash) {
                    data |= (short) (curDash != ' ' ? DASH_PRESS : DASH_UNPRESS);
                    lastDash = curDash;
                } if(lastCDash != curCDash) {
                    data |= (short) (curCDash != ' ' ? CDASH_PRESS : CDASH_UNPRESS);
                    lastCDash = curCDash;
                }
                inputList.add(data);
                if(frameLength > 1){
                    data &= (short)0b1111_1111_0000_0000;
                    for (int i = 1; i < frameLength; i++)
                        inputList.add(data);
                }
            }
            br.close();
            short[] inputArray = new short[inputList.size()];
            for (int i = 0; i < inputList.size(); ++i)
                inputArray[i] = inputList.get(i);
            return inputArray;
        }catch (Exception e){
            System.out.println("error reading tas file...\n" + e);
            return null;
        }
    }

    //writes a tas file to the specified path using the input arrays
    //surely there's a cleaner way to do this
    public static void writeTASfile(String path, String name, String extraInfo, short[] inputs){
        writeTASfile(path, name, extraInfo, new short[][]{inputs});
    }
    public static void writeTASfile(String path, String name, String extraInfo, short[][] inputList){
        try(PrintWriter pw = new PrintWriter(new File(path, name))) {
            pw.println("#homegrown with love, sadness, and global warming");
            if(extraInfo != null)
                pw.println('#' + extraInfo);
            pw.println();
            //keep current state
            int lastXAxis = 0;
            int lastYAxis = 0;
            boolean lastJump = false;
            boolean lastGrab = false;
            boolean lastDash = false;
            boolean lastCDash = false;
            //whether to print secondary hotkey
            //true at start because idk
            boolean altJump = true;
            boolean altGrab = true;
            boolean altDash = true;
            boolean altCDash = true;
            int frameCount = 0; //how many frames this state has stayed the same

            //go through each frame
            for(int l = 0; l < inputList.length; ++l){
                short[] inputArray = inputList[l];
                int lengthWithEnd = (l == inputList.length - 1) ? inputArray.length + 1 : inputArray.length; //jank to cleanup at end
                for(int i = 0; i < lengthWithEnd; ++i){
                    short data = i < inputArray.length ? inputArray[i] : 0;
                    //this frames state?
                    int curXAxis = getXInput(data);
                    int curYAxis = getYInput(data);
                    //did anything change?(or are we on the last cleanup iteration)
                    if(curXAxis != lastXAxis || curYAxis != lastYAxis || (data & 0b0000_0000_1111_1111) != 0 || i == inputArray.length){
                        //yes. print current state
                        if(frameCount > 0){
                            pw.printf("%4d", frameCount);
                            if(lastXAxis != 0)
                                pw.print("," + (lastXAxis == 1 ? RIGHT_KEY : LEFT_KEY));
                            if(lastYAxis != 0)
                                pw.print("," + (lastYAxis == 1 ? DOWN_KEY : UP_KEY));
                            if(lastJump)
                                pw.print("," + (altJump ? JUMP_KEY1 : JUMP_KEY0));
                            if(lastCDash)
                                pw.print("," + (altCDash ? CDASH_KEY1 : CDASH_KEY0));
                            if(lastDash)
                                pw.print("," + (altDash ? DASH_KEY1 : DASH_KEY0));
                            if(lastGrab)
                                pw.print("," + (altGrab ? GRAB_KEY1 : GRAB_KEY0));
                            pw.print('\n');
                        }
                        lastXAxis = curXAxis;
                        lastYAxis = curYAxis;
                        frameCount = 1;
                        //update state based on this frame
                        if(getPressJump(data)){
                            lastJump = true;
                            altJump = !altJump;
                        } else if (getUnpressJump(data)) {
                            lastJump = false;
                            altJump = true;
                        }
                        if(getPressGrab(data)){
                            lastGrab = true;
                            altGrab = !altGrab;
                        } else if (getUnpressGrab(data)) {
                            lastGrab = false;
                            altGrab = true;
                        }
                        if(getPressDash(data)){
                            lastDash = true;
                            altDash = !altDash;
                        } else if (getUnpressDash(data)) {
                            lastDash = false;
                            altDash = true;
                        }
                        if(getPressCDash(data)){
                            lastCDash = true;
                            altCDash = !altCDash;
                        } else if (getUnpressCDash(data)) {
                            lastCDash = false;
                            altCDash = true;
                        }
                    }else
                        ++frameCount; //state is the same
                }
            }

        }catch (Exception e){
            System.out.println("error writing tas file...\n" + e);
        }
    }

    //reads and re-writes a tas file, showing the middle man format i use for la boot
    //then plays it back and prints the player position/time each frame
    public static void debugTASFile(String path){
        short[] inputs = InputUtil.readTASfile(path);
        if(inputs == null){
            System.out.println("couldn't read input file!");
            return;
        }
        InputUtil.writeTASfile(Config.outputFolder, "DEBUGOUT.tas", "rewritten from " + path, inputs);
        System.out.println("input press/unpresses:");
        for (int f = 0; f < inputs.length; ++f)
            System.out.printf("%-4d - %s\n", f, InputUtil.inputToString(inputs[f]));
        System.out.println("frame positions:");
        Game game = Game.getInitGame();
        for(short s : inputs){
            InputUtil.applyInput(game.inputs, s);
            game.tick();
            if(Config.trimBranch(game))
                System.out.println("TRIMMED!");
            if(Config.goalCondition(game))
                System.out.println("COMPLETED!");
            System.out.println(game.time + " - " + game.player);
        }
    }

}
