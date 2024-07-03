package celeryroot.celery;

import java.util.Objects;

//stores the map identity of a game state in the defined grid
//split from the CellData because hashmap is a FUCK i hate coding!!!!!
public class CellPos {

    public int x;
    public int y;
    public int z;
    public int w;

    public CellPos(){
    }

    public CellPos(int x, int y, int z, int w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    public CellPos(CellPos cellPos){
        this.x = cellPos.x;
        this.y = cellPos.y;
        this.z = cellPos.z;
        this.w = cellPos.w;
    }

    public void set(CellPos cellPos){
        this.x = cellPos.x;
        this.y = cellPos.y;
        this.z = cellPos.z;
        this.w = cellPos.w;
    }
    public void set(int x, int y, int z, int w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    public String toString() {
        return "pos(" + x + ',' + y + ',' + z + ',' + w + ')';
    }

    //god, if you're reading this, wtf am i supposed to do pls i am not cut out for writing essays for computers
    @Override
    public int hashCode() {
        //return (x & 0xFF) | ((y << 8) & 0xFF00) | ((z << 16) & 0XFF0000) | ((w << 24) & 0xFF000000);
        return Objects.hash(x, y, z, w);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof CellPos c))
            return false;
        return(x == c.x && y == c.y && z == c.z && w == c.w);
    }
}
