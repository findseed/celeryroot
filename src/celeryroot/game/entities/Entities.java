package celeryroot.game.entities;

import celeryroot.game.map.LevelData;
import celeryroot.util.BinElement;
import celeryroot.util.Recti;
import celeryroot.util.Vec2f;

//lazy thing to make entities i dont care about
public class Entities {



    //take spike element and turn it into its hitbox
    public static Recti parseStaticSpike(BinElement belement, LevelData lD, int dir){
        int spikeX = belement.getIntAttr("x", 0) + lD.bounds.minX;
        int spikeY = belement.getIntAttr("y", 0) + lD.bounds.minY;
        if(dir == 0)
            spikeY-= 3;
        if(dir == 2)
            spikeX-= 3;
        int w = 3;
        int h = 3;
        if(dir < 2)
            w = belement.getIntAttr("width", 0);
        else
            h = belement.getIntAttr("height", 0);
        return new Recti(spikeX, spikeY,spikeX + w,spikeY + h);
    }

    //take spinner element and turn it into its hitbox
    public static Vec2f parseStaticSpinner(BinElement belement, LevelData lD){
        return new Vec2f(
                belement.getIntAttr("x", 0) + lD.bounds.minX,
                belement.getIntAttr("y", 0) + lD.bounds.minY
        );
    }

    //take jumpthru element and turn it into its hitbox
    public static Recti parseStaticJumpThru(BinElement belement, LevelData lD){
        int jumpThruX = belement.getIntAttr("x", 0) + lD.bounds.minX;
        int jumpThruY = belement.getIntAttr("y", 0)  + lD.bounds.minY;
        int w = belement.getIntAttr("width", 0);
        return new Recti(jumpThruX, jumpThruY, jumpThruX + w, jumpThruY + 5);
    }

    //take water element and turn it into its hitbox
    public static Recti parseStaticWater(BinElement belement, LevelData lD){
        int x = belement.getIntAttr("x", 0) + lD.bounds.minX;
        int y = belement.getIntAttr("y", 0) + lD.bounds.minY;
        int w = belement.getIntAttr("width", 0);
        int h = belement.getIntAttr("height", 0);
        return new Recti(x, y,x + w,y + h);
    }

}
