package celeryroot.game.map;

import celeryroot.util.BinElement;
import celeryroot.util.Recti;
import celeryroot.util.Vec2f;

import java.util.ArrayList;

//keeps info about individual levels/rooms.
//mainly just to make transitions work(clearly not something im good at)
public class LevelData {

    public String name;
    public String solids;
    public Recti bounds;
    //why am i worried about non-tile aligned rooms this is supposed to be the SIMPLE version
    public int width;
    public int height;

    public ArrayList<BinElement> entityData = new ArrayList<>();

    public LevelData(BinElement levelement){
        //general leveldata info
        int minX = levelement.getIntAttr("x", 0);
        int minY = levelement.getIntAttr("y", 0);
        width = levelement.getIntAttr("width", 0);
        height = levelement.getIntAttr("height", 0);
        if(height == 184)
            height = 180; //?
        bounds = new Recti(minX, minY, minX + width, minY + height);
        name = levelement.getStringAttr("name","");

        //process children(entities/solids)
        Vec2f levelOffset = new Vec2f(minX, minY);
        for(BinElement belement : levelement.children){
            if(belement.name.equals("entities")){
                entityData = belement.children;
            } else if (belement.name.equals("solids")) {
                solids = belement.getStringAttr("innerText", "");
            }
        }
    }

    @Override
    public String toString() {
        return name + " @ " + bounds;
    }

}
