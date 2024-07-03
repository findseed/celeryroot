package celeryroot.util;

import celeryroot.celery.config.Config;
import celeryroot.game.collision.Collision;
import celeryroot.game.map.Map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Calc {


    public static int sign(float f) {
        if (f == 0.0)
            return 0;
        return f > 0 ? 1 : -1;
    }

    public static int ceil(float v) {
        int res = (int)v;
        return v > (float)res ? res + 1 : res;
    }

    public static float approach(float val, float target, float amt){
        if(val <= target)
            return Math.min(val + amt, target);
        else
            return Math.max(val - amt, target);
    }

    public static float lerp(float value1, float value2, float amount) {
        return (float)((double)value1 + ((double)value2 - value1) * amount);
    }

    //lazy debug lol
    public static void printTiles(boolean[][] tileMap){
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < tileMap[0].length; j++) {
            for (int i = 0; i < tileMap.length; i++) {
                sb.append(tileMap[i][j] ? 1 : 0);
            }
            sb.append('\n');
        }
        System.out.println(sb);
    }

    //exports a png of the map
    public static void exportMapAsPNG(String path){
        try {
            int w = Config.bounds.maxX - Config.bounds.minX + 64;
            int h = Config.bounds.maxY - Config.bounds.minY + 64;
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            //VERY bad and insane but at least maybe accurate hitbox drawerererer
            Recti r = new Recti(0,0,0,0);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    bi.setRGB(i, j, 0xFF000000);
                    r.minX = Config.bounds.minX + i - 32;
                    r.minY = Config.bounds.minY + j - 32;
                    r.maxX = r.minX + 1;
                    r.maxY = r.minY + 1;
                    if(Collision.checkRectiTiles(r))
                        bi.setRGB(i, j, 0xFFFFFFFF);
                    for (int k = 0; k < Map.jumpThrus.length; k++) {
                        if(r.intersects(Map.jumpThrus[k]))
                            bi.setRGB(i, j, 0xFFFF8000);
                    }
                    for (int k = 0; k < Map.staticSpinners.length; k++) {
                        if(Collision.checkRectiSpinner(r, Map.staticSpinners[k]))
                            bi.setRGB(i, j, 0xFF0000FF);
                    }
                    for (int k = 0; k < 4; k++) {
                        for (int l = 0; l < Map.staticSpikes[k].length; l++) {
                            if(r.intersects(Map.staticSpikes[k][l])){
                                bi.setRGB(i, j, 0xFFFF0000);
                            }
                        }
                    }
                }
            }
            ImageIO.write(bi, "png", new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
