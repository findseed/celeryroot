package celeryroot.celery.config;

import celeryroot.util.Vec2f;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//stores info about a starting condition(egg@ basically just player position/speed/other states)
//keeping it as an object for sanity and so maybe i could extend this to take multiple starts?
//this could just hold a player object and copy it between stuff but this is easier esp w/ no obj copy yet
public class StartInfo {

    public int x;
    public int y;
    public Vec2f subPos = new Vec2f();
    public Vec2f speed = new Vec2f();
    public float stamina;
    public float wallSpeedRetained;
    public float wallSpeedRetainedTimer;
    public float varJumpSpeed;
    public float varJumpTimer;
    public float jumpGraceTimer;
    public boolean autoJump;
    public float maxFall;
    public int dashes;
    public int maxDashes = 1;
    public float dashCooldownTimer;
    public float dashAttackTimer;
    public Vec2f dashDir = new Vec2f();
    public int facing = 1;
    public boolean ducking;
    public int state;

    //defines everything that extracts a startInfo from a copied game info
    //!!!!!!!!!prolly update to use a real template- just uses from my normal game info atm
    private static final Regexer[] regexers = new Regexer[]{
            new Regexer("pos", "Pos:\\s*(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)", (start, m) -> {
                double rawX = Double.parseDouble(m.group(1));
                double rawY = Double.parseDouble(m.group(2));
                start.x = (int)Math.rint(rawX);
                start.y = (int)Math.rint(rawY);
                start.subPos.x = (float)(rawX - start.x);
                start.subPos.y = (float)(rawY - start.y);
            }),
            new Regexer("speed", "Speed:\\s*(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)", (start, m) ->
                start.speed.set(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)))),
            new Regexer("stamina", "Stamina:\\s*(-?\\d+\\.\\d+)", (start, m) ->
                start.stamina = Float.parseFloat(m.group(1))),
            new Regexer("wallSpeedRetained", "retained\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                start.wallSpeedRetained = Float.parseFloat(m.group(1))),
            new Regexer("wallSpeedRetainedTimer", "retainedTimer\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                start.wallSpeedRetainedTimer = Float.parseFloat(m.group(1))),
            new Regexer("varJumpSpeed", "jumpSpeed\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                    start.varJumpSpeed = Float.parseFloat(m.group(1))),
            new Regexer("varJumpTimer", "jumpTimer\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                    start.varJumpTimer = Float.parseFloat(m.group(1))),
            new Regexer("jumpGraceTimer", "jumpGrace\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                start.jumpGraceTimer = Float.parseFloat(m.group(1))),
            new Regexer("autoJump", "autoJump\\s*=\\s*(True|False)", (start, m) ->
                start.autoJump = m.group(1).equals("True")),
            new Regexer("maxFall", "maxFall\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                    start.maxFall = Float.parseFloat(m.group(1))),
            new Regexer("dashes", "dashes\\s*=\\s*(\\d+)", (start, m) ->
                    start.dashes = Integer.parseInt(m.group(1))),
            new Regexer("dashCooldownTimer", "dashCool\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                    start.dashCooldownTimer = Float.parseFloat(m.group(1))),
            new Regexer("dashAttackTimer", "dashAttack\\s*=\\s*(-?\\d+\\.\\d+)", (start, m) ->
                    start.dashAttackTimer = Float.parseFloat(m.group(1))),
            new Regexer("dashDir", "dashDir\\s*=\\s*(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)", (start, m) ->
                    start.dashDir.set(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)))),
            new Regexer("facing", "facing\\s*=\\s*(Right|Left)", (start, m) ->
                    start.facing = m.group(1).equals("Right") ? 1 : -1),
            new Regexer("ducking", "ducking\\s*=\\s*(True|False)", (start, m) ->
                    start.ducking = m.group(1).equals("True")),
            new Regexer("state", "state\\s*=\\s*(\\d+)", (start, m) ->
                    start.state = Integer.parseInt(m.group(1))),
    };

    private void parseStringInfo(String string){
        for (Regexer r : regexers){
            r.apply(this, string);
        }
    }

    public StartInfo(String gameInfo){
        parseStringInfo(gameInfo);
    }

}

//dum util thing for getting everything together easily
class Regexer {
    public String name;
    public Pattern regex;
    public BiConsumer<StartInfo, Matcher> consumer;

    public Regexer(String name, String regex, BiConsumer<StartInfo, Matcher> consumer){
        this.name = name;
        this.regex = Pattern.compile(regex);
        this.consumer = consumer;
    }

    public void apply(StartInfo start, String string){
        Matcher matcher = regex.matcher(string);
        if(matcher.find()){
            try{
                consumer.accept(start, matcher);
            }catch(Exception e){
                System.out.printf("invalid %s?\n", name);
            }
        }else{
            System.out.printf("no %s\n", name);
        }
    }


}
