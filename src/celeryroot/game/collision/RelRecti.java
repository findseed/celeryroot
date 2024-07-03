package celeryroot.game.collision;

import celeryroot.util.Recti;

public class RelRecti {

    public int xOffset;
    public int yOffset;
    public int width;
    public int height;

    public RelRecti(int xOffset, int yOffset, int width, int height) {
        super();
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    //updates our Recti to the x,y pos
    public void update(Recti r, int x, int y){
        r.minX = x + xOffset;
        r.minY = y + yOffset;
        r.maxX = r.minX + width;
        r.maxY = r.minY + height;
    }


}
