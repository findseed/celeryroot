package celeryroot.game;

import celeryroot.celery.config.Config;
import celeryroot.game.entities.Player;
import celeryroot.game.inputs.Inputs;

public class Game {

    //uh oh! you made time c ont inu ous! :fearful:
    public static final float deltaTime = 0.0166667f;
    public static final float baseDeltaTime =  0.0166667f;

    //igt frame counter
    public int time = 0;

    //keeps track of freeze frames. ideally this shouldn't exit
    private float freezeTimer;

    public Inputs inputs = new Inputs();

    //game specifics
    public Player player;


    public Game(){
    }

    //returns a game with everything setup(entities etc)
    public static Game getInitGame(){
        Game game = new Game();

        //init instance specific entities
        game.player = new Player(game, Config.start);


        return game;
    }

    public void copyFrom(Game game){
        time = game.time;
        freezeTimer = game.freezeTimer;
        inputs.copyFrom(game.inputs);
        player.copyFrom(game.player);
    }


    //runs one game frame
    public void tick(){
        inputs.tick();
        if(freezeTimer > 0) {
            freezeTimer = Math.max(freezeTimer - baseDeltaTime, 0);
            return;
        }
        //actually tick everything in the game
        ++time;
        //dollar store update order
        player.tick();
    }

    public void setFreeze(float time){
        if(freezeTimer < time)
            freezeTimer = time;
    }

}
