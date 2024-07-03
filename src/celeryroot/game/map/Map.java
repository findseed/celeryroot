package celeryroot.game.map;

import celeryroot.celery.config.Config;
import celeryroot.game.entities.Entities;
import celeryroot.game.entities.Solid;
import celeryroot.util.BinElement;
import celeryroot.util.Calc;
import celeryroot.util.Recti;
import celeryroot.util.Vec2f;

import java.util.ArrayList;
import java.util.Objects;

//largely static class to keep track of/interpret map info
public class Map {


    //tiles
    public static boolean[][] tiles;
    public static int tilePosX;
    public static int tilePosY;

    //the one level we'll be in
    public static LevelData level;

    //all our spike that aren't gonna do anything
    //0 - Up
    //1 - Down
    //2 - Left
    //3 - Right
    public static Recti[][] staticSpikes;
    //all our waters that aren't gonna do anything
    public static Recti[] staticWaters;
    //all our extra deadly rectangles that won't do anything
    public static Recti[] staticDeathRectis;
    //all our spinners that aren't gonna do anything
    public static Vec2f[] staticSpinners;
    //all our jumpthrus that aren't gonna do anything
    public static Recti[] jumpThrus;
    //all our solids(static for now.. not sure how exactly to unstatic?)
    public static Solid[] solids;




    //loads info from a map bin file
    //only happens once when starting program to get everything set up
    public static boolean loadMap(String binPath){
        if(Config.bounds == null){
            System.out.println("failed map load: no bounds set");
            return false;
        }
        if(Config.start == null){
            System.out.println("failed map load: no start set");
            return false;
        }

        ArrayList<LevelData> levelsList = new ArrayList<>();
        ArrayList<Recti> filler = new ArrayList<>();
        //load data from each room of the map
        int mapMinX = Integer.MAX_VALUE;
        int mapMinY = Integer.MAX_VALUE;
        int mapMaxX = Integer.MIN_VALUE;
        int mapMaxY = Integer.MIN_VALUE;
        BinElement mapData = BinElement.createBinElement(binPath);
        for(BinElement belement : mapData.children){
            if(Objects.equals(belement.name, "levels")){ //process all the levels
                for(BinElement levelData : belement.children){
                    LevelData lD = new LevelData(levelData);
                    levelsList.add(lD);
                    if(lD.bounds.minX < mapMinX) mapMinX = lD.bounds.minX;
                    if(lD.bounds.minY < mapMinY) mapMinY = lD.bounds.minY;
                    if(lD.bounds.maxX > mapMaxX) mapMaxX = lD.bounds.maxX;
                    if(lD.bounds.maxY > mapMaxY) mapMaxY = lD.bounds.maxY;
                }
            }
            else if(Objects.equals(belement.name, "Filler")){ //captial F jumpscare process all the filler(necessary? prolly not?)
                for(BinElement fillerData : belement.children){
                    int minX = fillerData.getIntAttr("x", 0);
                    int minY = fillerData.getIntAttr("y", 0);
                    int maxX = minX + fillerData.getIntAttr("w", 0);
                    int maxY = minY + fillerData.getIntAttr("h", 0);
                    if(minX < mapMinX) mapMinX = minX;
                    if(minY < mapMinY) mapMinY = minY;
                    if(maxX > mapMaxX) mapMaxX = maxX;
                    if(maxY > mapMaxY) mapMaxY = maxY;
                    filler.add(new Recti(minX, minY, maxX, maxY));
                }
            }
        }

        //process and create solids for all the map
        tilePosX = (mapMinX - 64);
        tilePosY = (mapMinY - 64);
        int tileTilePosX = tilePosX / 8;
        int tileTilePosY = tilePosY / 8;
        int tileW = Calc.ceil((mapMaxX - mapMinX + 128) / 8f);
        int tileH = Calc.ceil((mapMaxY - mapMinY + 128) / 8f);
        boolean[][] tileMap = new boolean[tileW][tileH]; //actual solids
        boolean[][] voidMap = new boolean[tileW][tileH]; //keeps track of where not to extend solids
        //fill in each level's solids
        for(LevelData lD : levelsList){
            int tMinX = lD.bounds.minX / 8 - tileTilePosX;
            int tMinY = lD.bounds.minY / 8 - tileTilePosY;
            int tMaxX = tMinX + Calc.ceil(lD.width / 8f);
            int tMaxY = tMinY + Calc.ceil(lD.height / 8f);
            String[] solidRows = lD.solids.split("\\r\\n|\\n\\r|\\n|\\r");
            //fill in the solids
            for (int i = tMinY; i < tMinY + solidRows.length; i++) { //each solidRow string index
                for(int j = tMinX; j < tMinX + solidRows[i - tMinY].length(); j++){ //each char
                    tileMap[j][i] = solidRows[i - tMinY].charAt(j - tMinX) != '0';
                }
            }
            //mark whole level as filled
            for (int i = tMinX; i < tMaxX; i++) {
                for (int j = tMinY; j < tMaxY; j++) {
                    voidMap[i][j] = true;
                }
            }
        }
        //fill in the filler solids
        for(Recti rect : filler){
            for(int i = rect.minX; i < rect.maxX; i++){
                for (int j = rect.minY; j < rect.maxY; j++) {
                    voidMap[i - tileTilePosX][j - tileTilePosY] = true;
                    tileMap[i - tileTilePosX][j - tileTilePosY] = true;
                }
            }
        }
        //extend void up/down on empty edges in each level(so i dont block anything)
        for(LevelData lD : levelsList){
            int tMinX = lD.bounds.minX / 8 - tileTilePosX;
            int tMinY = lD.bounds.minY / 8 - tileTilePosY;
            int tMaxX = tMinX + Calc.ceil(lD.width / 8f);
            int tMaxY = tMinY + Calc.ceil(lD.height / 8f) - 1;
            int margin;
            for (int i = tMinX; i < tMaxX; i++) {//iterate along the top/bottom
                if(!tileMap[i][tMinY]) //extend top
                    for (margin = 1; margin < 8; margin++)
                        voidMap[i][tMinY - margin] = true;
                if(!tileMap[i][tMaxY]) //extend bottom
                    for (margin = 1; margin < 8; margin++)
                        voidMap[i][tMaxY + margin] = true;
            }
        }
        //extend the solids on the outside edges of all the levels(without encroaching on set voids)
        for(LevelData lD : levelsList){
            int tMinX = lD.bounds.minX / 8 - tileTilePosX;
            int tMinY = lD.bounds.minY / 8 - tileTilePosY;
            int tMaxX = tMinX + Calc.ceil(lD.width / 8f) - 1;
            int tMaxY = tMinY + Calc.ceil(lD.height / 8f) - 1;
            //extrude the top/bottom up
            int offset;
            boolean fill;
            for (int i = tMinX; i <= tMaxX; i++) {
                fill = tileMap[i][tMinY];
                for(offset = 1; offset < 4 && !voidMap[i][tMinY - offset]; offset++)
                    tileMap[i][tMinY - offset] = fill;
                fill = tileMap[i][tMaxY];
                for(offset = 1; offset < 4 && !voidMap[i][tMaxY + offset]; offset++)
                    tileMap[i][tMaxY + offset] = fill;
            }
            //extrude the sides(+/- 4 to account for the above extension and +1 because im lazy above)
            tMinY-= 4;
            tMaxY+= 4;
            for(int i = tMinY; i <= tMaxY; i++){
                fill = tileMap[tMinX][i];
                for(offset = 1; offset < 4 && !voidMap[tMinX - offset][i]; offset++)
                    tileMap[tMinX - offset][i] = fill;
                fill = tileMap[tMaxX][i];
                for(offset = 1; offset < 4 && !voidMap[tMaxX + offset][i]; offset++)
                    tileMap[tMaxX + offset][i] = fill;
            }
        }
        tiles = tileMap;

        //find the level we'll be starting in
        for(LevelData lD : levelsList){
            if(lD.bounds.intersects(new Vec2f(Config.start.x, Config.start.y))){
                level = lD;
                break;
            }
        }
        if (level == null) {
            System.out.println("failed map load: couldn't find level to start in");
            return false;
        }
        parseEntities(level); //load entities from this level
        trimToBounds(); //trim solids/entities from everything not in bounds
        return true;
    }

    //jank thing to process all our entities into arrays
    //i think i want them generally in arrays but not sure how i'm supposed to do that so now this exists
    //gets the job done and this only runs once so prolly doesn't matter
    public static void parseEntities(LevelData lD){
        //coward list init like listen...
        ArrayList<Recti>[] staticSpikesList = new ArrayList[4];
        for (int i = 0; i < 4; i++) {
            staticSpikesList[i] = new ArrayList<>();
        }
        ArrayList<Vec2f> staticSpinnersList = new ArrayList<>();
        ArrayList<Recti> staticWaterList = new ArrayList<>();
        ArrayList<Recti> staticDeathRectisList = new ArrayList<>();
        ArrayList<Solid> solidsList = new ArrayList<>();
        ArrayList<Recti> jumpThrusList = new ArrayList<>();
        for(BinElement belement : lD.entityData){
            switch (belement.name){
                case "spikesUp" -> staticSpikesList[0].add(Entities.parseStaticSpike(belement, lD, 0));
                case "spikesDown" -> staticSpikesList[1].add(Entities.parseStaticSpike(belement, lD, 1));
                case "spikesLeft" -> staticSpikesList[1].add(Entities.parseStaticSpike(belement, lD, 2));
                case "spikesRight" -> staticSpikesList[1].add(Entities.parseStaticSpike(belement, lD, 3));
                case "jumpThru" -> jumpThrusList.add(Entities.parseStaticJumpThru(belement, lD));
                case "spinner" -> staticSpinnersList.add(Entities.parseStaticSpinner(belement, lD));
                case "water" -> staticWaterList.add(Entities.parseStaticWater(belement, lD));
            }
        }
        staticSpikes = new Recti[4][];
        for (int i = 0; i < 4; i++) {
            staticSpikes[i] = staticSpikesList[i].toArray(new Recti[0]);
        }
        staticSpinners = staticSpinnersList.toArray(new Vec2f[0]);
        staticWaters = staticWaterList.toArray(new Recti[0]);
        staticDeathRectis = staticDeathRectisList.toArray(new Recti[0]);
        solids = solidsList.toArray(new Solid[0]);
        jumpThrus = jumpThrusList.toArray(new Recti[0]);
    }

    //all in one :slight_smile: to get rid of anything that doesn't intersect our "fullbounds"
    //ft. what is an array
    private static void trimToBounds(){
        Recti bounds = Config.bounds.extend(32);
        { //the woke left is locking humanities major dropouts in rooms and forcing them to do array math at 2pm on a thursday :(
            int minSX = Math.max(bounds.minX, tilePosX);
            int minSY = Math.max(bounds.minY, tilePosY);
            int boundOffsetX = (minSX - tilePosX) / 8;
            int boundOffsetY = (minSY - tilePosY) / 8;
            int widthS = Math.min((bounds.maxX - minSX) / 8, tiles.length - boundOffsetX);
            int heightS = Math.min((bounds.maxY - minSY) / 8, tiles[0].length - boundOffsetY);
            boolean[][] trimmedTiles = new boolean[widthS][heightS];
            for (int i = 0; i < widthS; i++) {
                System.arraycopy(tiles[boundOffsetX + i], boundOffsetY, trimmedTiles[i], 0, heightS);
            }
            tilePosX+= (boundOffsetX * 8);
            tilePosY+= (boundOffsetY * 8);
            tiles = trimmedTiles;
        }
        ArrayList<Vec2f> trimmedSpinners = new ArrayList<>();
        for (int i = 0; i < staticSpinners.length; i++)
            if (bounds.intersects(staticSpinners[i]))
                trimmedSpinners.add(staticSpinners[i]);
        staticSpinners = trimmedSpinners.toArray(new Vec2f[0]);
        for (int i = 0; i < 4; i++) {
            ArrayList<Recti> trimmedSpikes = new ArrayList<>();
            for (int j = 0; j < staticSpikes[i].length; j++) {
                if(bounds.intersects(staticSpikes[i][j]))
                    trimmedSpikes.add(staticSpikes[i][j]);
            }
            staticSpikes[i] = trimmedSpikes.toArray(new Recti[0]);
        }
        ArrayList<Solid> trimmedSolids = new ArrayList<>();
        for (int i = 0; i < solids.length; i++) {
            if(bounds.intersects(solids[i].hitbox))
                trimmedSolids.add(solids[i]);
        }
        solids = trimmedSolids.toArray(new Solid[0]);
        ArrayList<Recti> trimmedJumpThrus = new ArrayList<>();
        for (int i = 0; i < jumpThrus.length; i++) {
            if(bounds.intersects(jumpThrus[i]))
                trimmedJumpThrus.add(jumpThrus[i]);
        }
        jumpThrus = trimmedJumpThrus.toArray(new Recti[0]);
        ArrayList<Recti> trimmedWaters = new ArrayList<>();
        for (int i = 0; i < staticWaters.length; i++) {
            if(bounds.intersects(staticWaters[i]))
                trimmedWaters.add(staticWaters[i]);
        }
        staticWaters = trimmedWaters.toArray(new Recti[0]);
    }

}
