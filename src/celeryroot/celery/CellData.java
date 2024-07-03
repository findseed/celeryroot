package celeryroot.celery;

//other half of CellPos that stores actual info about the cell
//aka the inputs to get there, the score, and pot the whole game state if memory(?)
public class CellData {

    public int score;
    public InputNode inputs;

    public CellData(){
        inputs = new InputNode();
    }

    public CellData(int score, InputNode inputs){
        this.score = score;
        this.inputs = inputs;
    }


}
