package celeryroot.game.inputs;

import celeryroot.game.Game;

//hotkey button thing for controlling something
//lets you specify a press/unpress for each frame instead of keeping track of two seperate keys
public class Button {

    //controls whether button will be pressed/unpressed next frame
    public boolean press;
    public boolean unpress;
    //whether its currently held down(between press/unpresses)
    public boolean down;

    //how long it stays "active" for after the initial press
    public final float bufferTime = 0.08f;
    public float bufferTimer = 0;

    public Button(){
    }

    //prolly dont need to copy bufferTime if you're not evil?
    public void copyFrom(Button button){
        press = button.press;
        unpress = button.unpress;
        down = button.down;
        bufferTimer = button.bufferTimer;
    }

    public boolean getActive(){
        return bufferTimer > 0;
    }

    //signals that the input "went thru," disabling it until it's pressed again
    public void consume(){
        bufferTimer = 0;
    }

    public void tick(){
        bufferTimer -= Game.deltaTime;
        if(press){
            down = true;
            bufferTimer = bufferTime;
            press = false;
        } else if (unpress) {
            down = false;
            bufferTimer = 0;
            unpress = false;
        }
    }

    @Override
    public String toString() {
        return "down: " + down + ", buffer: " + bufferTimer;
    }
}
