package celeryroot.game.inputs;

//no buffer input that can be -1, 0, or 1
public class Axis {

    public int pressValue = 0;
    public int value = 0;

    public Axis(){
    }

    public void copyFrom(Axis axis){
        pressValue = axis.pressValue;
        value = axis.value;
    }

    public void tick(){
        value = pressValue;
    }

    @Override
    public String toString() {
        return "raw: " + pressValue + ", registered: " + value;
    }
}
