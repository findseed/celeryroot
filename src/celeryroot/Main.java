package celeryroot;

import celeryroot.celery.Celery;
import celeryroot.celery.config.Config;
import celeryroot.game.inputs.InputUtil;
import celeryroot.game.map.Map;
import celeryroot.util.Calc;



public class Main {


    public static void main(String[] args) {

        long runTime = System.currentTimeMillis();
        System.out.println("start      " + runTime);
        Map.loadMap(Config.mapFile);
        Calc.printTiles(Map.tiles);
        //Calc.exportMapAsPNG("hitboxes.png");


        Celery.start();
    }
}