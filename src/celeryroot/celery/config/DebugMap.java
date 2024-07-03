package celeryroot.celery.config;

import celeryroot.celery.Celery;
import celeryroot.celery.CellPos;

//stuff to print a map showing where the Doohickey has been
public class DebugMap {

    //config

    public static final float scale = 0.125f;


    public static int getMapX(CellPos cell){
        return cell.x;
    }
    public static int getMapY(CellPos cell){
        return cell.y;
    }
    public static char getMapValue(CellPos pos){
        return pos == null ? ' ' : 'X';
    }





    //feels poorly done but idk not smart enugh to care
    public static String createDebugMap(){
        //bounds
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for(CellPos c : Celery.globalCellList){
            int mapX = getMapX(c);
            int mapY = getMapY(c);
            if(mapX < minX) minX = mapX;
            if(mapX > maxX) maxX = mapX;
            if(mapY < minY) minY = mapY;
            if(mapY > maxY) maxY = mapY;
        }
        //populate
        char[][] visitMap = new char[(int)((maxY - minY) * scale) + 1][(int)((maxX - minX) * scale) + 1];
        for (CellPos c : Celery.globalCellList){
            int realMapX = (int)((getMapX(c) - minX) * scale);
            int realMapY = (int)((getMapY(c) - minY) * scale);
            visitMap[realMapY][realMapX] = getMapValue(c);
        }
        //construct

        StringBuilder map = new StringBuilder("(" + minX + "," + minY + "," + maxX + "," + maxY + "):\n");
        for (int i = 0; i < visitMap.length; ++i) {
            for (int j = 0; j < visitMap[0].length; j++) {
                char c = visitMap[i][j];
                map.append(c == '\0' ? ' ' : c);
            }
            map.append('\n');
        }
        return map.toString();
    }


}
