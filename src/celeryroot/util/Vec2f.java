package celeryroot.util;

public class Vec2f {

    public float x;
    public float y;

    public Vec2f(float x, float y){
        this.x = x;
        this.y = y;
    }

    public Vec2f(Vec2f v){
        this.x = v.x;
        this.y = v.y;
    }

    public Vec2f(){
        this.x = 0;
        this.y = 0;
    }


    public void normalize(){
        double l = 1.0d / ((float)Math.sqrt((double)x * x + (double)y * y));
        this.x = (float)((double)x * l);
        this.y = (float)((double)y * l);
    }

    public void safeNormalize(){
        if(x != 0 || y != 0)
            normalize();
    }

    public void set(Vec2f v){
        this.x = v.x;
        this.y = v.y;
    }

    public void set(float x, float y){
        this.x = x;
        this.y = y;
    }

    public Vec2f add(Vec2f b){
        return new Vec2f(x + b.x, y + b.y);
    }
    public Vec2f add(float f){
        return new Vec2f(x + f, y + f);
    }

    public Vec2f sub(Vec2f b){
        return new Vec2f(x - b.x, y - b.y);
    }
    public Vec2f sub(float f){
        return new Vec2f(x - f, y - f);
    }

    public Vec2f mult(Vec2f b){
        return new Vec2f(x * b.x, y * b.y);
    }
    public Vec2f mult(float f){
        return new Vec2f(x * f, y * f);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
