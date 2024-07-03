package celeryroot.util;

//stores a rectangle with 4 integer points
public class Recti {

    public int minX;
    public int minY;
    public int maxX;
    public int maxY;


    public Recti(){
    }

    public Recti(int minX, int minY, int maxX, int maxY){
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    //do these two rectangles intersect?
    //(sharing a side/point is not intersecting they are just really good friends)
    public boolean intersects(Recti b){
        return intersects(b.minX, b.minY, b.maxX, b.maxY);
    }
    public boolean intersects(int minX, int minY, int maxX, int maxY){
        return this.minX < maxX && this.maxX > minX &&
                this.minY < maxY && this.maxY > minY;
    }

    //is this point inside this rectangle
    //(sharing a side/point is considered inside)
    public boolean intersects(Vec2f p){
        return p.x >= minX && p.x <= maxX &&
                p.y >= minY && p.y <= maxY;
    }
    public boolean intersects(int x, int y){
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY;
    }

    public boolean intersects(float x, float y){
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY;
    }

    public void set(Recti recti){
        set(recti.minX, recti.minY, recti.maxX, recti.maxY);
    }
    public void set(int minX, int minY, int maxX, int maxY){
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Recti extend(int e){
        return new Recti(this.minX - e, this.minY - e, this.maxX + e, this.maxY + e);
    }

    @Override
    public String toString() {
        return "(" + minX + ", " + minY + "), (" + maxX + ", " + maxY + ")";
    }
}
