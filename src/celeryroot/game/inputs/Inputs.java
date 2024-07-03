package celeryroot.game.inputs;

import celeryroot.util.Vec2f;

public class Inputs {

    public Button jumpButton;
    public Button grabButton;
    public Button dashButton;
    public Button crouchDashButton;
    public Axis xInput;
    public Axis yInput;

    public Inputs(){
        jumpButton = new Button();
        grabButton = new Button();
        dashButton = new Button();
        crouchDashButton = new Button(); //way to ruin it
        xInput = new Axis();
        yInput = new Axis();
    }

    public void copyFrom(Inputs inputs){
        jumpButton.copyFrom(inputs.jumpButton);
        grabButton.copyFrom(inputs.grabButton);
        dashButton.copyFrom(inputs.dashButton);
        crouchDashButton.copyFrom(inputs.crouchDashButton);
        xInput.copyFrom(inputs.xInput);
        yInput.copyFrom(inputs.yInput);
    }

    public void tick(){
        jumpButton.tick();
        grabButton.tick();
        dashButton.tick();
        crouchDashButton.tick();
        xInput.tick();
        yInput.tick();
    }

    //returns a vector .....? finish your sentence idiot
    public Vec2f getDashAim(int defaultFacing){
        Vec2f aim = new Vec2f(xInput.value, yInput.value);
        if(aim.x == 0f && aim.y == 0f)
            aim.x = defaultFacing;
        aim.normalize();
        return aim;
    }

    public Vec2f getSwimAim(){
        Vec2f aim = new Vec2f(xInput.value, yInput.value);
        if(aim.x != 0 || aim.y != 0)
            aim.normalize();
        return aim;
    }


    @Override
    public String toString() {
        return "jump: " + jumpButton + "\ngrab: " + grabButton + "\ndash: " + dashButton + "\ncrouchDash: " + crouchDashButton + "\nxInput: " + xInput + "\nyInput: " + yInput;
    }

}
