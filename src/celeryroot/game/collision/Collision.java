package celeryroot.game.collision;

import celeryroot.game.entities.Solid;
import celeryroot.game.entities.Player;
import celeryroot.game.map.Map;
import celeryroot.util.Recti;
import celeryroot.util.Vec2f;

//putting all my collision detection code in here ig cuz LAZY
//edit: that was a horrible idea i have no idea how to navigate this
public class Collision {
    

    //check collision with Evil things
    public static boolean playerDeathCheck(Player p){
        p.hurtboxSize.update(p.hurtbox, p.x, p.y);
        if(checkStaticDeathRectis(p))
            return true;
        if(checkStaticSpinners(p))
            return true;
        if(checkStaticSpikes(p))
            return true;
        return false; //nothing hit :3
    }

    private static boolean checkStaticSpinners(Player p){
        for (int i = 0; i < Map.staticSpinners.length; i++)
            if(checkRectiSpinner(p.hurtbox, Map.staticSpinners[i]))
                return true;
        return false;
    }

    private static boolean checkStaticDeathRectis(Player p){
        for (int i = 0; i < Map.staticDeathRectis.length; i++)
            if(p.hurtbox.intersects(Map.staticDeathRectis[i]))
                return true;
        return false;
    }

    private static boolean checkStaticSpikes(Player p){
        if(p.speed.y >= 0f){ //up spikes if down speed n also above spike cuz idfk
            for (int i = 0; i < Map.staticSpikes[0].length; i++) {
                Recti s = Map.staticSpikes[0][i];
                if(s.intersects(p.hurtbox) && p.hurtbox.maxY <= s.maxY)
                    return true;
            }
        }
        if(p.speed.y <= 0f) //down spike if up speed
            for (int i = 0; i < Map.staticSpikes[1].length; i++)
                if(Map.staticSpikes[1][i].intersects(p.hurtbox))
                    return true;
        if(p.speed.x >= 0f) //left spike if right speed
            for (int i = 0; i < Map.staticSpikes[2].length; i++)
                if(Map.staticSpikes[2][i].intersects(p.hurtbox))
                    return true;
        if(p.speed.x <= 0f) //right spike if left speed
            for (int i = 0; i < Map.staticSpikes[3].length; i++)
                if(Map.staticSpikes[3][i].intersects(p.hurtbox))
                    return true;
        return false;
    }

    //are we inside any spikes at all(full hitbox, even if we wouldnt die from them)
    public static boolean rawCheckStaticSpikes(Recti hitbox){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Map.staticSpikes[i].length; j++) {
                if(Map.staticSpikes[i][j].intersects(hitbox))
                    return true;
            }
        }
        return false;
    }



    //return if being at the x,y position with hitbox would intersect any jumpthrus
    public static boolean checkJumpThrus(int x, int y, RelRecti relHitbox){
        int minX = x + relHitbox.xOffset;
        int minY = y + relHitbox.yOffset;
        return checkStaticJumpThrus(minX, minY, minX + relHitbox.width, minY + relHitbox.height);
    }

    //return if being at the x,y position with hitbox would intersect any WATER YUYMMMYMY
    public static boolean checkWater(int x, int y, RelRecti relHitbox){
        int minX = x + relHitbox.xOffset;
        int minY = y + relHitbox.yOffset;
        return checkStaticWaters(minX, minY, minX + relHitbox.width, minY + relHitbox.height);
    }

    //god only knows
    public static boolean checkWaterZip(int x, int y, RelRecti relHitbox){
        int minX = x + relHitbox.xOffset;
        int minY = y + relHitbox.yOffset;
        int maxX = minX + relHitbox.width;
        int maxY = minY + relHitbox.height;
        Recti water = null;
        for (int i = 0; i < Map.staticWaters.length; i++)
            if (Map.staticWaters[i].intersects(minX, minY, maxX, maxY)){
                water = Map.staticWaters[i];
                break;
            }
        return water == null || (((minY + maxY) / 2f) < ((water.minY + water.maxY) / 2f));
    }

    //solid collision area


    //return if being at the x,y position with hitbox would intersect any solid tiles/Rectis
    public static boolean checkSolidCollision(int x, int y, RelRecti relHitbox){
        int minX = x + relHitbox.xOffset;
        int minY = y + relHitbox.yOffset;
        return checkSolidCollision(minX, minY, minX + relHitbox.width, minY + relHitbox.height);
    }

    public static boolean checkSolidCollision(int x, int y){
        return checkTiles(x, y) || checkStaticSolids(x, y);
    }

    //are we on the ground(tile/solid/jumpthru) at this pos(depth=1 for ground 1 unit below)
    public static boolean checkOnGround(int x, int y, RelRecti relHitbox, int depth){
        int minX = x + relHitbox.xOffset;
        int maxX = minX + relHitbox.width;
        int minY = y + relHitbox.yOffset;
        int maxY = minY + relHitbox.height;

        //would we be inside solid if moved depth units below?
        //OR would we be inside a jumpthru(that's below us) if moved depth units below
        return (checkSolidCollision(minX, minY + depth, maxX, maxY + depth) || checkJumpThruGround(minX, maxY, maxX, maxY + depth));
    }

    //checks if being at the x,y position with hitbox would intersect and ledge blocks
    public static boolean checkLedgeBlocks(int x, int y, RelRecti relHitbox){
        int minX = x + relHitbox.xOffset;
        int minY = y + relHitbox.yOffset;
        int maxX = minX + relHitbox.width;
        int maxY = minY + relHitbox.height;
        //all spikes and spinners have a ledge blocker
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Map.staticSpikes[i].length; j++) {
                if(Map.staticSpikes[i][j].intersects(minX, minY, maxX, maxY))
                    return true;
            }
        }
        for (int i = 0; i < Map.staticSpinners.length; i++) {
            if(checkRectiSpinner(minX, minY, maxX, maxY, Map.staticSpinners[i]))
                return true;
        }
        return false;
    }


    //takes a x movement range/dir, returns the farthest you can move without clipping
    //doesn't return the solid actually bonked but i'll worry about that if I need to???
    public static int getMaxXMove(int minX, int minY, int maxX, int maxY, int amount){
        if(amount > 0){
            int i = getMaxXTileMove(minX, maxX + amount, minY, maxY, 1);
            return Math.min(i, getMaxXSolidMove(minX, i, minY, maxY, 1)) - maxX;
        }else{
            int i = getMaxXTileMove(minX + amount, maxX, minY, maxY, -1);
            return Math.max(i, getMaxXSolidMove(i, maxX, minY, maxY, -1)) - minX;
        }
    }


    //takes a y movement range/dir, returns the farthest you can move without clipping
    //doesn't return the solid actually bonked but i'll worry about that if I need to???
    public static int getMaxYMove(int minX, int minY, int maxX, int maxY, int amount){
        if(amount > 0){ //moving down account for jump thru
            int i = getMaxYTileMove(minX, maxX, minY, maxY + amount, 1);
            i = getMaxYJumpThruMove(minX, maxX, maxY, i);
            return Math.min(i, getMaxYSolidMove(minX, maxX, minY, i, 1)) - maxY;
        }else{
            int i = getMaxYTileMove(minX, maxX, minY + amount, maxY, -1);
            return Math.max(i, getMaxYSolidMove(minX, maxX, i, maxY, -1)) - minY;
        }
    }


    private static boolean checkStaticSolids(int minX, int minY, int maxX, int maxY){
        for (int i = 0; i < Map.solids.length; i++)
            if(Map.solids[i].hitbox.intersects(minX, minY, maxX, maxY))
                return true;
        return false;
    }

    private static boolean checkStaticJumpThrus(int minX, int minY, int maxX, int maxY){
        for (int i = 0; i < Map.jumpThrus.length; i++)
            if(Map.jumpThrus[i].intersects(minX, minY, maxX, maxY))
                return true;
        return false;
    }

    private static boolean checkStaticWaters(int minX, int minY, int maxX, int maxY){
        for (int i = 0; i < Map.staticWaters.length; i++)
            if(Map.staticWaters[i].intersects(minX, minY, maxX, maxY))
                return true;
        return false;
    }


    private static boolean checkSolidCollision(int minX, int minY, int maxX, int maxY){
        return checkTiles(minX, minY, maxX, maxY) ||
                checkStaticSolids(minX, minY, maxX, maxY);
    }

    //checks collision with top of jumpthru
    //basically: is the top of a jumpthru in this box
    private static boolean checkJumpThruGround(int minX, int minY, int maxX, int maxY){
        for (Recti s : Map.jumpThrus) {
            if(s.minY >= minY && s.minY < maxY && s.minX < maxX && s.maxX > minX)
                return true;
        }
        return false;
    }




    //gets the max x movement we could make in this direction considering tile collision
    //if this is bugged then so be it not my fault god made visualizing boxes impossible this might as well mean nothing
    private static int getMaxXTileMove(int minX, int maxX, int minY, int maxY, int dir){
        int minTileX = Math.min(Math.max(minX - Map.tilePosX >> 3, 0), Map.tiles.length);
        int minTileY = Math.min(Math.max(minY - Map.tilePosY >> 3, 0), Map.tiles[0].length);
        int maxTileX = Math.min(Math.max((maxX - Map.tilePosX - 1 >> 3) + 1, 0), Map.tiles.length);
        int maxTileY = Math.min(Math.max((maxY - Map.tilePosY - 1 >> 3) + 1, 0), Map.tiles[0].length);
        if(dir > 0) {
            for (int i = minTileX; i < maxTileX; i++)
                for (int j = minTileY; j < maxTileY; j++)
                    if (Map.tiles[i][j])
                        return Math.max(minX, Map.tilePosX + (i << 3));
            return maxX;
        }else{
            for (int i = maxTileX - 1; i >= minTileX; i--)
                for (int j = minTileY; j < maxTileY; j++)
                    if (Map.tiles[i][j])
                        return Math.min(maxX, Map.tilePosX + (i + 1 << 3));
            return minX;
        }
    }

    //gets the max x movement we could make in this direction considering solid collision
    private static int getMaxXSolidMove(int minX, int maxX, int minY, int maxY, int dir){
        int mx;
        if(dir > 0) {
            mx = maxX;
            for (Solid s : Map.solids) {
                if(s.hitbox.minX > minX && s.hitbox.minX < mx
                && s.hitbox.minY < maxY && s.hitbox.maxY > minY)
                    mx = s.hitbox.minX;
            }
        }else{
            mx = minX;
            for (Solid s : Map.solids) {
                if(s.hitbox.maxX > mx && s.hitbox.maxX < maxX
                        && s.hitbox.minY < maxY && s.hitbox.maxY > minY)
                    mx = s.hitbox.minX;
            }
        }
        return mx;
    }

    //gets the max y movement we could make in this direction considering tile collision
    private static int getMaxYTileMove(int minX, int maxX, int minY, int maxY, int dir){
        int minTileX = Math.min(Math.max(minX - Map.tilePosX >> 3, 0), Map.tiles.length);
        int minTileY = Math.min(Math.max(minY - Map.tilePosY >> 3, 0), Map.tiles[0].length);
        int maxTileX = Math.min(Math.max((maxX - Map.tilePosX - 1 >> 3) + 1, 0), Map.tiles.length);
        int maxTileY = Math.min(Math.max((maxY - Map.tilePosY - 1 >> 3) + 1, 0), Map.tiles[0].length);
        if(dir > 0) {
            for (int j = minTileY; j < maxTileY; j++)
                for (int i = minTileX; i < maxTileX; i++)
                    if (Map.tiles[i][j])
                        return Math.max(minY, Map.tilePosY + (j << 3));
            return maxY;
        }else{
            for (int j = maxTileY - 1; j >= minTileY; j--)
                for (int i = minTileX; i < maxTileX; i++)
                    if (Map.tiles[i][j])
                        return Math.min(maxY, Map.tilePosY + (j + 1 << 3));
            return minY;
        }
    }

    //gets the max y movement we could make in this direction considering solid collision
    private static int getMaxYSolidMove(int minX, int maxX, int minY, int maxY, int dir){
        int my;
        if(dir > 0) {
            my = maxY;
            for (Solid s : Map.solids) {
                if(s.hitbox.minY > minY && s.hitbox.minY < my
                && s.hitbox.minX < maxX && s.hitbox.maxX > minX)
                    my = s.hitbox.minY;
            }
        }else{
            my = minY;
            for (Solid s : Map.solids) {
                if(s.hitbox.maxY > my && s.hitbox.maxY < maxY
                && s.hitbox.minX < maxX && s.hitbox.maxX > minX)
                    my = s.hitbox.minY;
            }
        }
        return my;
    }

    //gets the max y down movement we could make considering jumpthru collision
    private static int getMaxYJumpThruMove(int minX, int maxX, int minY, int maxY){
        int my = maxY;
        for (Recti s : Map.jumpThrus) {
            if(s.minY >= minY && s.minY < my && s.minX < maxX && s.maxX > minX)
                my = s.minY;
        }
        return my;
    }


    //default



    //just check if a recti intersects our tile grid
    public static boolean checkRectiTiles(Recti r){
        return checkTiles(r.minX, r.minY, r.maxX, r.maxY);
    }

    private static boolean checkTiles(int minX, int minY, int maxX, int maxY){
        int tileMinX = Math.min(Math.max(minX - Map.tilePosX >> 3, 0), Map.tiles.length);
        int tileMinY = Math.min(Math.max(minY - Map.tilePosY >> 3, 0), Map.tiles[0].length);
        int tileMaxX = Math.min(Math.max((maxX - Map.tilePosX - 1 >> 3) + 1, 0), Map.tiles.length);
        int tileMaxY = Math.min(Math.max((maxY - Map.tilePosY - 1 >> 3) + 1, 0), Map.tiles[0].length);
        for (int i = tileMinX; i < tileMaxX; i++) {
            for (int j = tileMinY; j < tileMaxY; j++) {
                if(Map.tiles[i][j])
                    return true;
            }
        }
        return false;
    }

    //poitn col

    //did not second thought this at all pure vibes no thoughts maybe just works
    private static boolean checkTiles(int x, int y){
        int checkX = (x - Map.tilePosX) >> 3;
        int checkY = (y - Map.tilePosY) >> 3;
        if(checkX < 0 || checkY < 0 || checkX >= Map.tiles.length || checkY >= Map.tiles[0].length)
            return false;
        return Map.tiles[checkX][checkY];
    }

    private static boolean checkStaticSolids(int x, int y){
        for (int i = 0; i < Map.solids.length; i++)
            if(Map.solids[i].hitbox.intersects(x, y))
                return true;
        return false;
    }


    //funny shapes


    public static boolean checkRectiSpinner(Recti p, Vec2f s){
        return checkRectiSpinner(p.minX, p.minY, p.maxX, p.maxY, s);
    }

    //check if a Recti intersects a spinner collider at position s
    //kinda simplified but looks like it should work/round the same?
    public static boolean checkRectiSpinner(int minX, int minY, int maxX, int maxY, Vec2f s){
        //exclude anything not in a basic bb around spinner
        if(maxX <= (s.x - 8f) || minX >= (s.x + 8f) || //rect x
                (maxY - s.y) <= -6f || (minY - s.y) >= 6f) //circle y(maybe float goodly?)
            return false;
        //check rest of the rect for definite collision
        if(minY < (s.y + 1f) && maxY > (s.y - 3f))
            return true;
        //try find closest corner of Recti in the circle
        double cornerX;
        double cornerY;
        if(minX > s.x) //full right
            cornerX = minX;
        else if (maxX < s.x) //full left
            cornerX = maxX;
        else //both? must be colliding(source: the bb)
            return true;
        //no both otherwise rect hitbox would catch it
        cornerY = (minY > s.y) ? minY : maxY;
        //our boy in circle(6f)?
        cornerX-= s.x;
        cornerY-= s.y;
        return (float)(cornerX * cornerX + cornerY * cornerY) < 36f;
    }

    public static boolean checkRectiCircle(Recti p, Vec2f s, float rad){
        return checkRectiCircle(p.minX, p.minY, p.maxX, p.maxY, s, rad);
    }

    //check if the Recti intersects a circle at position s radius rad
    //kinda simplified but looks like it should work/round the same?
    public static boolean checkRectiCircle(int minX, int minY, int maxX, int maxY, Vec2f s, float rad){
        //exclude anything not in a basic bb around spinner(maybe float goodly?)
        if((maxX - s.x) <= -rad || (minX - s.x) >= rad ||
                (maxY - s.y) <= -rad || (minY - s.y) >= rad)
            return false;
        //try find closest corner of Recti in the circle
        double cornerX;
        double cornerY;
        if(minX > s.x) //full right
            cornerX = minX;
        else if (maxX < s.x) //full left
            cornerX = maxX;
        else //both? must be colliding(source: the bb)
            return true;
        if(minY > s.y) //full top
            cornerY = minY;
        else if (maxY < s.y) //full bottom(just like m)
            cornerY = maxY;
        else //both? must be colliding(source: the bb)
            return true;
        //our boy in circle?
        cornerX-= s.x;
        cornerY-= s.y;
        return (float)(cornerX * cornerX + cornerY * cornerY) < (rad * rad);
    }
    
}
