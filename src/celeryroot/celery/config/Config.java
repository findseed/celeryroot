package celeryroot.celery.config;

import celeryroot.celery.CellPos;
import celeryroot.game.Game;
import celeryroot.game.entities.Player;
import celeryroot.game.inputs.InputUtil;
import celeryroot.game.inputs.Inputs;
import celeryroot.util.Recti;

import java.util.Random;

//keeps all the settings for the betty boop
public class Config {

    //location of .bin file
    public static String mapFile = "demo.bin";

    //output folder for solution .tas files
    public static String outputFolder = "out";

    //tas file name
    public static String TASname = "erm";

    //# of threads to run with
    public static int threads = 12;

    //# of iterations between syncs(in 2^n-1)
    public static final int syncCount = 16383;

    //# of times to try branching off a cell
    public static final int branchInputCount = 50;

    //# of frames of input to try each branch
    public static final int branchInputLength = 20;

    //whether to terminate a branch after reaching the goal
    //(e.g. if optimizing for time there's no reason to keep trying after winning)
    public static final boolean terminateCompletions = true;

    //how likely(1/prevInputFiddleChance chance) to randomly edit previous inputs instead of randomly generating more
    public static final int prevInputChance = 4;

    //how many previous inputs to randomly edit. will choose a random amount between these
    public static final int minPrevInputs = 8;
    public static final int maxPrevInputs = 24;

    //how likely(1/completionFocusChance) to choose a cell from the last completed run to branch off of
    public static final int completionFocusChance = 16;

    //idk
    public static final int initCapacities = 1_000;

    //default raw pos bounds to stay within/clipping extra stuff out of
    public static final Recti bounds = new Recti(6, -6, 288, 151);

    //whether to print lousy ascii map when syncing
    public static final boolean printDebugMap = true;


    //used to initialize start game state
    public static StartInfo start = new StartInfo("""
Pos:   19.000000000000, 144.000000000000
Speed: 0.000000000000, 0.000000000000
Vel:   0.000000000000, 0.000000000000
Stamina: 110.00 StNormal
CanDash Coyote(5) CantPause
[1] Timer: 0:00.000(0)

guh = 0.016666699201
pixel = 19.000000000000, 144.000000000000
subpixel = 0.000000000000, 0.000000000000
onGround = True
dashes = 1
maxDashes= 1
dashCool = 0.000000000000
dashDir = 0.000000000000, 0.000000000000
dashAttack = 0.000000000000
jumpSpeed = 0.000000000000
jumpTimer = 0.000000000000
jumpGrace = 0.100000001490
autoJump = False
autoJumpT = 0.000000000000
retained = 0.000000000000
retainedTimer = 0.000000000000
starFlyT = 0.000000000000
maxFall = 160.000000000000
facing = Right
ducking = False
state = 0
            """);

////////////////////logic settings stuffs


    //how to group game states into cells
    //aka: defines the state space to explore
    //+ideally, the lowest overall score between two cells should have a linear path of cells between them(hard)
    public static void assignCell(Game game, CellPos cellPos){
        cellPos.x = game.player.x >> 1;
        cellPos.y = game.player.y >> 1;
        cellPos.z = (int)game.player.getRealXSpeed() >> 4;
        cellPos.w = game.player.dashes | game.player.state << 2;
    }

    //defines how to measure the Greatness of a cell/state
    //the lower the better, see assignCell for other half of context ig?
    public static int getScore(Game game){
        return game.time;
    }

    //executed after each frame to see if it should stop pursuing the current branch(by return true)
    //so basically descrribes any "oob" or restrictions like min speed/dashes or death
    public static boolean trimBranch(Game game){
        float realXSpeed = game.player.getRealXSpeed();
        float realYSpeed = game.player.getRealYSpeed();

        if(realXSpeed < 0) return true;
        if(game.player.x > 175 && game.player.y > 88) return true;

        //default obviously good things to have
        if(!bounds.intersects(game.player.x, game.player.y)) return true;
        if(game.player.dead) return true;
        return false;
    }

    //executed after each frame to see if we've reached our goal end state(by return true)
    //cant be destructive to the state
    public static boolean goalCondition(Game game){
        return game.player.y < 7;
    }

    //computes the final metric for comparing two completions
    //each cell will be optimized with their individual scores, but only improvements to this will be output(lower the better)
    //technically can be destructive if terminateCompletions is true
    public static int getFinalScore(Game game){
        return game.time;
    }

    //custom extra stuff you can add to output file name
    public static String scoreAlias(Game game){
        return String.format("%d", game.time);
    }

    //generates new random input based on current game/input
    //prolly poorly done im just making stuff up idk im not a Toaser
    public static short inputFiddler(Game game, Random r){
        Inputs inputs = game.inputs;

        //xInput
        int t = r.nextInt(64);
        if(t < 16) inputs.xInput.pressValue = 1;
        else if (t > 56) inputs.xInput.pressValue = -1;
        else if (t > 30 && t < 34) inputs.xInput.pressValue = 0;

        //yInput
        t = r.nextInt(64);
        if(t < 4) inputs.yInput.pressValue = 1;
        else if (t > 59) inputs.yInput.pressValue = -1;
        else if (t > 30 && t < 34) inputs.yInput.pressValue = 0;

        //jump
        if(t < 8) inputs.jumpButton.press = true;
        else if(inputs.jumpButton.down)
            if(t > 56) inputs.jumpButton.unpress = true;

        //grab
        if(t < 16) inputs.grabButton.press = true;
        else if(inputs.grabButton.down)
            if(t > 58) inputs.grabButton.unpress = true;

        //dash
        if(t < 4) inputs.dashButton.press = true;
        else if(t < 8) inputs.crouchDashButton.press = true;
        else if (t > 48) {
            if(inputs.dashButton.down)
                inputs.dashButton.unpress = true;
            if(inputs.crouchDashButton.down)
                inputs.crouchDashButton.unpress = true;
        }

        //lazy lazy lazy
        return InputUtil.extractInputs(inputs);
    }

    //modifies previous inputs for hopefully better
    //currently very poorly done just as a test. in theory should be good but this is 😬😬😬😬😬😬😬😬😬😬
    public static short[] prevInputFiddler(short[] inputs, int startIndex, Random r){
        short[] newInputs = inputs.clone();
        for (int f = startIndex; f < newInputs.length; ++f) {
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.JUMP_PRESS, r, 32, 8, 32);
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.JUMP_UNPRESS, r, 32, 8, 32);
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.GRAB_PRESS, r, 32, 8, 32);
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.GRAB_UNPRESS, r, 32, 8, 32);
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.DASH_PRESS, r, 32, 8, 32);
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.DASH_UNPRESS, r, 32, 8, 32);
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.CDASH_PRESS, r, 32, 8, 32);
            InputUtil.modifyButton(startIndex, inputs, f, InputUtil.CDASH_UNPRESS, r, 32, 8, 32);
            InputUtil.modifyAxis(inputs, f, InputUtil.X_INPUT_SHIFT, r, 32, 32);
            InputUtil.modifyAxis(inputs, f, InputUtil.Y_INPUT_SHIFT, r, 32, 32);
        }
        return newInputs;
    }


}
