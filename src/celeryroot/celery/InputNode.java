package celeryroot.celery;

//single Piece of the tree of inputs used to get to different cells
//isn't really any centralized tree or anything, it doesn't even know the cells exist
//each thread just adds new nodes onto it and eventually the get killed when they stop
//i'd like to be able to traverse this for better cell selection(cellection :joy:) for branching\
//but this format super wouldn't work for that and idk how i'd approach that
public class InputNode {

    public short[] inputs;
    public int length;
    public InputNode prev;

    public InputNode(short[] inputs, InputNode prev){
        this.inputs = inputs;
        this.prev = prev;
        this.length = prev.length + 1;
    }

    public InputNode(){
        inputs = null;
        length = -1;
        prev = null;
    }

    //returns an array of every prev input array needed to get to this node
    public short[][] traceInputs(){
        short[][] inputArray = new short[length + 1][];
        InputNode tracer = this;
        for (int i = length; i >= 0; --i) {
            inputArray[i] = tracer.inputs;
            tracer = tracer.prev;
        }
        return inputArray;
    }
}
