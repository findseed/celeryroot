package celeryroot.game.entities;

import celeryroot.celery.config.StartInfo;
import celeryroot.game.Game;
import celeryroot.game.collision.Collision;
import celeryroot.game.collision.CollisionData;
import celeryroot.game.collision.RelRecti;
import celeryroot.game.inputs.Inputs;
import celeryroot.game.map.Map;
import celeryroot.util.Calc;
import celeryroot.util.Recti;
import celeryroot.util.Vec2f;

public class Player {

    static final RelRecti normalHitbox = new RelRecti(-4, -11, 8, 11);
    static final RelRecti duckHitbox = new RelRecti(-4, -6, 8, 6);
    static final RelRecti starFlyHitbox = new RelRecti(-4, -10, 8, 8);
    static final RelRecti normalHurtbox = new RelRecti(-4, -11, 8, 9);
    static final RelRecti duckHurtbox = new RelRecti(-4, -6, 8, 4);
    static final RelRecti starFlyHurtbox = new RelRecti(-3, -9, 6, 6);


    public int x;
    public int y;
    public Vec2f subPos = new Vec2f();
    public Vec2f speed = new Vec2f();
    public float stamina = MAX_STAMINA;

    public Recti hitbox = new Recti();
    public Recti hurtbox = new Recti();
    public RelRecti hitboxSize = normalHitbox;
    public RelRecti hurtboxSize = normalHurtbox;

    public boolean dead;

    private final Game game;
    private final Inputs inputs;

    private boolean onGround;
    private boolean wasOnGround;

    //jumpin
    public float jumpGraceTimer;
    public float varJumpSpeed; //upwards force
    public float varJumpTimer;
    public boolean autoJump;
    public float autoJumpTimer;

    //directionals
    public int facing;
    public int moveX;
    private float forceMoveXTimer;
    private int forceMoveXDir;
    private int hopWaitXDir;
    private float hopWaitXSpeed;

    //wall speed
    public float wallSpeedRetained;
    public float wallSpeedRetainedTimer;

    //falling
    private float wallSlideTimer = MAX_WALL_SLIDE_TIMER;
    private float maxFall;
    private int wallSlideDir;

    //wallboost
    private float wallBoostTimer;
    private int wallBoostDir;

    //clim
    private int climbTriggerDir;
    private Solid climbHopSolid;
    private Vec2f climbHopSolidPos;
    private int lastClimbDir;
    private float climbNoMoveTimer;

    //dashing
    public float dashCoolDownTimer;
    private float dashAttackTimer;
    private float dashRefillCooldownTimer;
    public int totalDashes = 0;
    public int dashes = 1;
    public int maxDashes = 1;
    private boolean demoDash;
    private Vec2f dashAim = new Vec2f();
    private Vec2f dashDir = new Vec2f();
    private Vec2f preDashSpeed = new Vec2f();
    private boolean dashFromGround;

    //holdable
    private float minHoldTimer;
    private boolean holdCannotDuck; //thing to not immediately drop floaty holdable if holding down with it

    public boolean updated;

    public int state;

    private int coroutinePos;
    private float coroutineTimer;

    //consts
    public static final float MAX_STAMINA = 110f;
    public static final float MAX_WALL_SLIDE_TIMER = 1.2f;
    public static final float MAX_CLIMB_NO_MOVE_TIMER = 0.1f;
    public static final float MAX_WALL_SPEED_RETAINED_TIMER = 0.06f;

    public static final int ST_NORMAL = 0;
    public static final int ST_CLIMB = 1;
    public static final int ST_DASH = 2;
    public static final int ST_SWIM = 3;
    public static final int ST_BOOST = 4;
    public static final int ST_RED_DASH = 5;
    public static final int ST_HIT_SQUASH = 6;
    public static final int ST_LAUNCH = 7;
    public static final int ST_PICKUP = 8;
    public static final int ST_DREAM_DASH = 9;
    public static final int ST_STAR_FLY = 19;

    public Player(Game game, int x, int y, float subX, float subY){
        this.game = game;
        inputs = game.inputs;
        this.x = x;
        this.y = y;
        subPos.set(subX, subY);
    }

    //inits a player from a config startinfo thingiieie
    public Player(Game game, StartInfo start){
        this.game = game;
        this.inputs = game.inputs;
        this.x = start.x;
        this.y = start.y;
        this.subPos.set(start.subPos);
        this.speed.set(start.speed);
        this.stamina = start.stamina;
        this.wallSpeedRetained = start.wallSpeedRetained;
        this.wallSpeedRetainedTimer = start.wallSpeedRetainedTimer;
        this.varJumpSpeed = start.varJumpSpeed;
        this.varJumpTimer = start.varJumpTimer;
        this.jumpGraceTimer = start.jumpGraceTimer;
        this.autoJump = start.autoJump;
        this.maxFall = start.maxFall;
        this.dashes = start.dashes;
        this.maxDashes = start.maxDashes;
        this.dashCoolDownTimer = start.dashCooldownTimer;
        this.dashAttackTimer = start.dashAttackTimer;
        this.dashDir.set(start.dashDir);
        this.facing = start.facing;
        this.setDucking(start.ducking);
        this.state = start.state;
    }

    //no guarantee i remembered everything...
    public void copyFrom(Player player){
        x = player.x;
        y = player.y;
        subPos.set(player.subPos);
        speed.set(player.speed);
        stamina = player.stamina;

        hitbox.set(player.hitbox);
        hurtbox.set(player.hurtbox);
        hitboxSize = player.hitboxSize;
        hurtboxSize = player.hurtboxSize;

        dead = player.dead;

        onGround = player.onGround;
        wasOnGround = player.wasOnGround;

        jumpGraceTimer = player.jumpGraceTimer;
        varJumpSpeed = player.varJumpSpeed;
        varJumpTimer = player.varJumpTimer;
        autoJump = player.autoJump;
        autoJumpTimer = player.autoJumpTimer;

        facing = player.facing;
        moveX = player.moveX;
        forceMoveXTimer = player.forceMoveXTimer;
        forceMoveXDir = player.forceMoveXDir;
        hopWaitXDir = player.hopWaitXDir;
        hopWaitXSpeed = player.hopWaitXSpeed;

        wallSpeedRetained = player.wallSpeedRetained;
        wallSpeedRetainedTimer = player.wallSpeedRetainedTimer;

        wallSlideTimer = player.wallSlideTimer;
        maxFall = player.maxFall;
        wallSlideDir = player.wallSlideDir;

        wallBoostTimer = player.wallBoostTimer;
        wallBoostDir = player.wallBoostDir;

        climbTriggerDir = player.climbTriggerDir;
        climbHopSolid = player.climbHopSolid;
        climbHopSolidPos = player.climbHopSolidPos;
        lastClimbDir = player.lastClimbDir;
        climbNoMoveTimer = player.climbNoMoveTimer;

        dashCoolDownTimer = player.dashCoolDownTimer;
        dashAttackTimer = player.dashAttackTimer;
        dashRefillCooldownTimer = player.dashRefillCooldownTimer;
        totalDashes = player.totalDashes;
        dashes = player.dashes;
        maxDashes = player.maxDashes;
        demoDash = player.demoDash;
        dashAim.set(player.dashAim);
        dashDir.set(player.dashDir);
        preDashSpeed.set(player.preDashSpeed);
        dashFromGround = player.dashFromGround;

        minHoldTimer = player.minHoldTimer;
        holdCannotDuck = player.holdCannotDuck;

        updated = player.updated;

        state = player.state;

        coroutinePos = player.coroutinePos;
        coroutineTimer = player.coroutineTimer;
    }


    public void tick(){
        updated = true;

        climbTriggerDir = 0;
        //gliderboost
        //explodelaunch
        hurtboxSize.update(hurtbox, x, y);
        hitboxSize.update(hitbox, x, y);
        updateOnGround();

        //wallboost timer/logic
        if(wallBoostTimer > 0f){
            wallBoostTimer -= Game.deltaTime;
            if(moveX == wallBoostDir)
                wallBoost();
        }
        //wallslide timer
        if (wallSlideDir != 0){ //decrease time left to slide if we held into wall last frame
            wallSlideTimer = Math.max(wallSlideTimer - Game.deltaTime, 0f);
            wallSlideDir = 0;
        }
        //restore stuff if on the ground(not dashes?)
        if(onGround && state != ST_CLIMB){
            autoJump = false;
            stamina = MAX_STAMINA;
            wallSlideTimer = MAX_WALL_SLIDE_TIMER;
        }
        if (onGround)
            jumpGraceTimer = 0.1f;
        else if (jumpGraceTimer > 0f)
            jumpGraceTimer -= Game.deltaTime;
        //dash timers
        if(dashAttackTimer > 0f)
            dashAttackTimer -= Game.deltaTime;
        if(dashCoolDownTimer > 0f)
            dashCoolDownTimer -= Game.deltaTime;
        if(dashRefillCooldownTimer > 0f) //cooldown after dashing before getting dash back on ground
            dashRefillCooldownTimer -= Game.deltaTime;
        else{
            tryRefillDash();
        }
        dashAim = inputs.getDashAim(facing); //this should maybe be moved?
        //jump
        if(varJumpTimer > 0f)
            varJumpTimer -= Game.deltaTime;
        if(autoJumpTimer > 0f){ //turn off autojump after xxxxx
            if(autoJump){
                autoJumpTimer -= Game.deltaTime;
                if(autoJumpTimer <= 0)
                    autoJump = false;
            }else
                autoJumpTimer = 0;
        }

        //update x holding dir from either force move or input
        if(forceMoveXTimer > 0f){
            moveX = forceMoveXDir;
            forceMoveXTimer -= Game.deltaTime;
        }else{
            moveX = inputs.xInput.value;
        }
        //-----climbhop stuff here for when instance solids
        if(moveX != 0 &&
        state != ST_CLIMB &&
        state != ST_PICKUP &&
        state != ST_RED_DASH &&
        state != ST_HIT_SQUASH) //face moveX but make it the longest if statement in your life
            facing = moveX;

        //wall retained speed
        if(wallSpeedRetainedTimer > 0f){
            if(Calc.sign(speed.x) == -Calc.sign(wallSpeedRetained)) //kill retained if moving away
                wallSpeedRetainedTimer = 0;
            else if(!Collision.checkSolidCollision(x + Calc.sign(wallSpeedRetained), y, hitboxSize)){ //return speed if empty space in front
                speed.x = wallSpeedRetained;
                wallSpeedRetainedTimer = 0f;
            }else
                wallSpeedRetainedTimer -= Game.deltaTime;
        }

        //climbhop raw logic
        if(hopWaitXDir != 0){ //We. have stored hop speed towards sm dir
            if(Calc.sign(speed.x) == -hopWaitXDir || speed.y > 0f) //kill stored hop speed if falling/reverse
                hopWaitXDir = 0;
            else if(!Collision.checkSolidCollision(x + hopWaitXDir, y, hitboxSize)){ //activate hop speed if space in front
                speed.x = hopWaitXSpeed;
                hopWaitXDir = 0;
            }
        }
        //state physics(Idaho reference)
        tickStates();

        //get sucked by a jumpthru(interestingly prolly not the frame you leave the ground)
        if(!onGround && speed.y <= 0f &&
                (state != ST_CLIMB || lastClimbDir == -1) && //if we're climbing only do it if climbing up!!
                Collision.checkJumpThrus(x, y, hitboxSize) &&
                !Collision.checkLedgeBlocks(x, y - 2, hitboxSize))
            moveY(-40f * Game.deltaTime);

        //get sucked DOWN by ground while dashattacking
        if(!onGround && isDashAttacking() && dashDir.y == 0f && //in air last dash hor
            Collision.checkOnGround(x, y, hitboxSize, 3) && //ground to snap to
            !Collision.checkLedgeBlocks(x, y + 3, hurtboxSize)) //nothing blocking
            moveYPixel(3);

        //unduck if moving down in full air
        if(!onGround && speed.y > 0f && jumpGraceTimer <= 0f && canUnduck() && hitboxSize != starFlyHitbox)
            setDucking(false);

        //do the actual movement
        if(state != ST_DREAM_DASH){
            if(speed.x != 0){
                CollisionData c = moveX(speed.x * Game.deltaTime);
                if(c != null) onCollideX(c);
            }
            if(speed.y != 0){
                CollisionData c = moveY(speed.y * Game.deltaTime);
                if(c != null) onCollideY(c);
            }
        }

        doWaterChecks();

        //carryables

        //player colliders //for now just death
        if(Collision.playerDeathCheck(this))
            this.dead = true;

        //PROBABLY BAD SINCE NO TRANSITIONS
        enforceBounds();

        wasOnGround = onGround;
    }


    //runs state specific physics for a frame
    private void tickStates(){
        //physics
        switch(state){
            case ST_NORMAL -> changeState(normalTick());
            case ST_CLIMB -> changeState(climbTick());
            case ST_DASH -> changeState(dashTick());
            case ST_SWIM -> changeState(swimTick());
        }
        //coroutines apparently these run a frame earlier than the state tick?
        switch(state){
            case ST_DASH -> dashCoroutine();
        }
    }

    //swaps our state and calls any end/start state logic
    public void changeState(int newState){
        if(state == newState)
            return;
        switch (state){
            case ST_NORMAL -> normalEnd();
            case ST_CLIMB -> climbEnd();
            case ST_DASH -> dashEnd();
        }
        state = newState;
        switch (state){
            case ST_NORMAL -> normalStart();
            case ST_CLIMB -> climbStart();
            case ST_SWIM -> swimStart();
            case ST_DASH -> dashStart();
        }
    }


    //refills dash if possible(onground/swimming,)
    //{!}HITBOX{!}
    private void tryRefillDash(){
        if(state == ST_SWIM || (onGround && !Collision.rawCheckStaticSpikes(hitbox)))
            refillDash();
    }

    //sets onground depending on whether on the ground n stuff
    private void updateOnGround(){
        if(state == ST_DREAM_DASH)
            onGround = false;
        else
            onGround = (speed.y >= 0 && Collision.checkOnGround(x, y, hitboxSize, 1));
    }


    private void doWaterChecks(){
        if(state == ST_SWIM){
            if(speed.y < 0f && speed.y >= -60f){ //wont pretend to know or care
                while(!swimCheck()){
                    speed.y = 0f;
                    if(moveYPixel(1) != null)
                        break;
                }
            }
        }else if(state == ST_NORMAL && swimCheck())
            changeState(ST_SWIM);
        else if(state == ST_CLIMB && swimCheck()){ //still not pretending. idk the funny transition zippy thing
            if(Collision.checkWaterZip(x, y, hitboxSize)){
                while(swimCheck() && moveYPixel(-1) == null){
                }
                if(swimCheck())
                    changeState(ST_SWIM);
            }else{
                changeState(ST_SWIM);
            }
        }
    }

    //bad version of normal enforcebounds that just assumes no transistions ever
    //should prolly track bordering lvls and just add a flag for "transitioned" or smthn so it can at least be a goal
    public void enforceBounds(){
        Recti lvl = Map.level.bounds;
        int temp = lvl.minX - (x + hitboxSize.xOffset);
        if(temp > 0){
            x += temp;
            speed.x = 0; //fake collisions smmsmhmshmhsmsmh
        }
        temp = lvl.maxX - (x + hitboxSize.xOffset + hitboxSize.width); //theocrystal prolly makes this -1 or msthn idr
        if(temp < 0){
            x += temp;
            speed.x = 0;
        }
        temp = lvl.minY - 24 - (y + hitboxSize.yOffset);
        if(temp > 0){
            y += temp;
            speed.y = 0;
        }
        temp = lvl.maxY + 4 - (y + hitboxSize.yOffset); //still top. crouch strats ftw
        if(temp < 0)
            dead = true;
    }

    public void refillDash(){
        if(dashes < maxDashes)
            dashes = maxDashes;
    }

    //now presenting: an unknowable collection of random integer soup
    private boolean swimCheck(){ //for starting swim state ig?
        return Collision.checkWater(x, y - 8, hitboxSize) && Collision.checkWater(x, y, hitboxSize);
    }
    private boolean swimUnderwaterCheck(){ //swim accel physics
        return Collision.checkWater(x, y - 9, hitboxSize);
    }
    private boolean swimJumpCheck(){ //take a guess
        return !Collision.checkWater(x, y - 14, hitboxSize);
    }
    private boolean swimRiseCheck(){ //auto surface sucky
        return !Collision.checkWater(x, y - 18, hitboxSize);
    }



    //Jumps!!!!



    //code that like every jump runs to reset whatever
    public void resetJumpVars(){
        inputs.jumpButton.consume();
        jumpGraceTimer = 0f;
        autoJump = false;
        dashAttackTimer = 0f;
        wallSlideTimer = MAX_WALL_SLIDE_TIMER; //me when my favorite hobby is setting wallSlideTimer to 1.2
        wallBoostTimer = 0f;
    }

    public void jump(){
        resetJumpVars();
        varJumpTimer = 0.2f;
        speed.x+= 40f * moveX;
        speed.y = -105f;
        //liftboost
        varJumpSpeed = speed.y;
    }

    private void wallJump(int dir) {
        setDucking(false);
        resetJumpVars();
        varJumpTimer = 0.2f;
        if (moveX != 0){
            forceMoveXDir = dir;
            forceMoveXTimer = 0.16f;
        }
        //liftboost
        speed.x = 130f * dir;
        speed.y = -105f;
        //liftboost
        varJumpSpeed = speed.y;
    }

    //jump from climb/grab
    private void climbJump(){
        if(!onGround)
            stamina -= 27.5f; //fascinating choice
        jump();
        if(moveX == 0){ //initiate wallboost to throw it back :wea
            wallBoostDir = -facing;
            wallBoostTimer = 0.2f;
        }
    }

    private void wallBoost(){
        speed.x = 130f * moveX;
        stamina+= 27.5f;
        wallBoostTimer = 0;
    }

    //climbed up to edge
    private void climbHop(){
        //add extra check to get climbhopsolid we collided with(:fear:)
        if(Collision.checkSolidCollision(x + facing, y, hitboxSize)){ //blocked, store hop speed
            hopWaitXDir = facing;
            hopWaitXSpeed = facing * 100f;
        }else{ //all clear gogogogog
            hopWaitXDir = 0;
            speed.x = facing * 100f;
        }
        speed.y = Math.min(speed.y, -120f); //faster than jump?
        forceMoveXDir = 0;
        forceMoveXTimer = 0.2f; //?
    }

    //jump out of hor dash
    private void superJump(){
        resetJumpVars();
        varJumpTimer = 0.2f;
        speed.x = 260f * facing;
        speed.y = -105f;
        //liftboost
        if(isDucking()){ //ducking cuts y for higher x. only specific to ducking?
            setDucking(false);
            speed.x*= 1.25f;
            speed.y*= 0.5f;
        }
        varJumpSpeed = speed.y;
    }

    //wall jump out of a vert dash
    private void superWallJump(int dir){
        setDucking(false);
        resetJumpVars();
        varJumpTimer = 0.25f; //rare large var jump timer
        speed.x = 170f * dir;
        speed.y = -160f;
        //liftboost
        varJumpSpeed = speed.y;
    }


    //can we climb onto smthn in this direct, at this yoffset(PLOT twist theyre both offsets...)
    private boolean climbCheck(int dir, int yOff){
        return climbBoundsCheck(dir) &&
                Collision.checkSolidCollision(x + dir * 2, y + yOff, hitboxSize);
    }
    //pls keep arms and legs inside the vehicle
    //aka is our climb target inside level bounds
    private boolean climbBoundsCheck(int dir){
        int minX = x + hitboxSize.xOffset + (dir * 2);
        return minX >= Map.level.bounds.minX &&
                minX + hitboxSize.width < Map.level.bounds.maxX;
    }

    //basically checks if there's a wall we can still grab
    //by: is there something at either top of our hitbox in the direction we're facing, or something 4 units down from that
    //returns true if there's nothing at either. aka we should slip down????
    private boolean slipCheck(int yOff) {
        int xCheck = x + hitboxSize.xOffset + (facing == 1 ? hitboxSize.width : -1);
        int yCheck = y + hitboxSize.yOffset + yOff;
        return !Collision.checkSolidCollision(xCheck, yCheck + 4) && !Collision.checkSolidCollision(xCheck, yCheck);
    }

    //can you wall jump from this direction?
    private boolean wallJumpCheck(int dir){
        return climbBoundsCheck(dir) && Collision.checkSolidCollision(x + (getWallJumpLeniency(dir) * dir), y, hitboxSize);
    }

    //haha im so quirky
    //defaults to let you wall jump 3 units away
    //but goes to 5 if dashing upwards(assuming theres no spikes in the way)
    private int getWallJumpLeniency(int dir){
        int minX = x + hitboxSize.xOffset + dir * 5;
        int minY = y + hitboxSize.yOffset + dir * 5;
        int maxX = minX + hitboxSize.width;
        int maxY = minY + hitboxSize.height;
        if(isDashAttacking() && dashDir.x == 0f && dashDir.y == -1f){
            int spikes = dir > 0 ? 2 : 3;
            for (int i = 0; i < Map.staticSpikes[spikes].length; i++) {
                if(Map.staticSpikes[spikes][i].intersects(minX, minY, maxX, maxY))
                    return 3;
            }
            return 5;
        }
        return 3;
    }

    //pt 2. snaps player up to jumpthrus wow look at me
    private void snapToJumpThrus(){
        int minX = x + hitboxSize.xOffset;
        int maxX = minX + hitboxSize.width;
        int maxY = y + hitboxSize.yOffset + hitboxSize.height;
        for (int i = 0; i < Map.jumpThrus.length; i++) {
            Recti j = Map.jumpThrus[i];
            int snap = j.minY - maxY;
            if(j.minX < maxX && j.maxX > minX &&
                    j.minY < maxY && snap >= -6) //only snap up to 6 units
                moveYPixel(snap);
        }
    }

    //true if dashing straight up(except for the weird dashes then idk do the math idiot)
    //determines whether you can do the funny wall jump
    private boolean superWallJumpCheck() {
        return dashDir.x <= .2f && dashDir.x >= -.2f && dashDir.y <= -.75f;
    }

    private void startDash(){
        demoDash = inputs.crouchDashButton.getActive();
        dashes = Math.max(0, dashes - 1);
        inputs.dashButton.consume();
        inputs.crouchDashButton.consume();
    }


    //rounds a dash direction minecraft velocity style
    //basically takes anythign super near an axis and snaps/normalizes it to there(.0001,0.3 becomes 0,1)
    //?
    private void correctDashPrecision(Vec2f dir) {
        if (dir.x != 0f && dir.x > -0.001f && dir.x < 0.001f) //non horizontal
            dir.set(0, Calc.sign(dir.y));
        else if (dir.y != 0f && dir.y > -0.001f && dir.y < 0.001f) //non vertical
            dir.set(Calc.sign(dir.x), 0f);
    }

    //tries to move the player by dX units on the X axis, considering solid collision
    public CollisionData moveX(float dX){
        //update subpixel
        subPos.x+= (double)dX;
        int fullMove = (int)Math.rint(subPos.x);
        //trying to move into another pixel, check collision yada yada
        if(fullMove != 0){
            subPos.x-= fullMove;
            return moveXPixel(fullMove);
        }
        return null;
    }

    //tries to move the player dX pixels on the X axis, + does the actual solid collision
    public CollisionData moveXPixel(int dX){
        hitboxSize.update(hitbox, x, y);
        int delta = Collision.getMaxXMove(hitbox.minX, hitbox.minY, hitbox.maxX, hitbox.maxY, dX);
        int targetX = x + dX;
        x+= delta;
        if(delta != dX){ //must've hit something
            subPos.x = 0.0f;
            return new CollisionData(targetX, y);
        }
        return null;
    }

    //tries to move the player by dY units on the Y axis, considering solid collision
    public CollisionData moveY(float dY){
        //update subpixel
        subPos.y+= (double)dY;
        int fullMove = (int)Math.rint(subPos.y);
        //trying to move into another pixel, check collision yada yada
        if(fullMove != 0){
            subPos.y-= fullMove;
            return moveYPixel(fullMove);
        }
        return null;
    }

    //tries to move the player dY pixels on the Y axis, + does the actual solid collision
    public CollisionData moveYPixel(int dY){
        hitboxSize.update(hitbox, x, y);
        int delta = Collision.getMaxYMove(hitbox.minX, hitbox.minY, hitbox.maxX, hitbox.maxY, dY);
        int targetY = y + dY;
        y+= delta;
        if(delta != dY){ //must've hit something
            subPos.y = 0.0f;
            return new CollisionData(x, targetY);
        }
        return null;
    }

    //called when we collide on X from our speed movement ig?
    private void onCollideX(CollisionData data){
        if(state == ST_DREAM_DASH)
            return;
        //bouncy dash collisions here if you ever care
        if(state == ST_DASH){
            //can we duck to avoid collision(interesting that this comes after the bouincy dash collisions)
            if(onGround && !Collision.checkSolidCollision(x + Calc.sign(speed.x), y, duckHitbox)){
                setDucking(true);
                return;
            }
            //hor dash only try snap over corners to avoid collision. iterates outwards swapping +- for 1-4
            for(int yOff = 1; yOff <= 4; ++yOff){
                for(int yOffSign = 1; yOffSign >= -1; yOffSign -= 2){
                    int tX = x + Calc.sign(speed.x);
                    int tY = y + yOff * yOffSign;
                    if(!Collision.checkSolidCollision(tX, tY, hitboxSize) && //is it clear
                            Collision.checkSolidCollision(tX, tY - yOffSign, hitboxSize)) { //hugging the corner? idk why
                        moveYPixel(yOff * yOffSign); //gasp
                        moveXPixel(Calc.sign(speed.x));
                        return;
                    }
                }
            }
        }
        //retained logic
        if(wallSpeedRetainedTimer <= 0f){
            wallSpeedRetained = speed.x;
            wallSpeedRetainedTimer = MAX_WALL_SPEED_RETAINED_TIMER;
        }
        speed.x = 0f;
        dashAttackTimer = 0f;
    }

    //called when we collide on Y from our speed movement ig?
    private void onCollideY(CollisionData data){
        if(state == ST_SWIM){
            speed.y = 0;
        }
        if(state == ST_DREAM_DASH)
            return;
        //bouncy dash collisions here if you ever care
        //snap to the sides to avoid collision?
        if(speed.y > 0f) {//downwards collision(dash-only snaps + dashdir boost)
            if(state == ST_DASH && !dashFromGround){ //only from air so you dont SLIP
                //have to be moving in the direction to snap(or ~fullly vertical)
                if(speed.x <= 0.01f){ //neg
                    for(int xOff = -1; xOff >= -4; --xOff){
                        if(!Collision.checkOnGround(x + xOff, y, hitboxSize, 1)){ //onground fo jumptrhyyrurus
                            moveXPixel(xOff);
                            moveYPixel(1);
                            return;
                        }
                    }
                }
                if(speed.x >= -0.01f){ //pos
                    for(int xOff = 1; xOff <= 4; ++xOff){
                        if(!Collision.checkOnGround(x + xOff, y, hitboxSize, 1)){ //onground fo jumptrhyyrurus
                            moveXPixel(xOff);
                            moveYPixel(1);
                            return;
                        }
                    }
                }
            }
            //down diag dashdir boost pog pog pog pogp og!!!
            if(dashDir.x != 0 && dashDir.y > 0f){
                dashDir.x = Calc.sign(dashDir.x);
                dashDir.y = 0f; //flatten dash into a horizontal one
                speed.y = 0f;
                speed.x *= 1.2f;
                setDucking(true);
            }
        }else if(speed.y < 0f){ //uppers snapping(not dash-only) + kill var jump
            //+1 leniency over down snaps for dashing, equivalent for no dashing
            int leniency = (isDashAttacking() && speed.x > -0.01f && speed.x < 0.01f) ? 5 : 4;
            //idk why these just edit raw x/y
            if(speed.x <= 0.01f){ //neg
                for(int xOff = 1; xOff <= leniency; ++xOff){
                    if(!Collision.checkSolidCollision(x - xOff, y - 1, hitboxSize)){
                        x-= xOff;
                        y-= 1;
                        return;
                    }
                }
            }
            if(speed.x >= -0.01f){ //pos
                for(int xOff = 1; xOff <= leniency; ++xOff){
                    if(!Collision.checkSolidCollision(x + xOff, y - 1, hitboxSize)){
                        x+= xOff;
                        y-= 1;
                        return;
                    }
                }
            }
            if(varJumpTimer < 0.15f) //doesn't kill it for the first however long?
                varJumpTimer = 0f;
        }
        dashAttackTimer = 0f;
        speed.y = 0f;
    }


    //updates hitbox/hurtbox to duck size
    public void setDucking(boolean d){
        if(d){
            hitboxSize = duckHitbox;
            hurtboxSize = duckHurtbox;
        }else{
            hitboxSize = normalHitbox;
            hurtboxSize = normalHurtbox;
        }
    }


    //can we unduck without colliding with any solids?
    public boolean canUnduck(){
        if(!isDucking())
            return true;
        return !Collision.checkSolidCollision(x, y, normalHitbox);
    }
    public boolean canUnduckAt(int x, int y){
        if(!isDucking())
            return true;
        return !Collision.checkSolidCollision(x, y, normalHitbox);
    }

    public void Die(){
        dead = true;
    }

    public boolean isDucking(){
        return hitboxSize == duckHitbox && hurtboxSize == duckHurtbox;
    }

    private boolean isTired(){
        return (wallBoostTimer > 0f ? stamina + 27.5f : stamina) < 20f;
    }

    //dash tonite :eyes:
    //dash tonight queen??
    //dash tonite :eyes:
    private boolean canDash(){
        return ((inputs.dashButton.getActive() || inputs.crouchDashButton.getActive())
        && dashCoolDownTimer <= 0f && dashes > 0);
    }

    //test thing idk
    public float getRealXSpeed(){
        return wallSpeedRetainedTimer <= 0 ? (state == ST_DASH && speed.x == 0) ? preDashSpeed.x : speed.x : wallSpeedRetained;
    }
    public float getRealYSpeed(){
        return (state == ST_DASH && speed.y == 0) ? preDashSpeed.y : speed.y;
    }








//                                                                        W E L C O M E   T O  T H E
//
//██████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████
//██░░░░░░░░░░░░░░██░░░░░░░░░░░░░░██░░░░░░░░░░░░░░██░░░░░░░░░░░░░░██░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░░░██░░░░░░░░░░░░░░██░░░░░░██████████░░░░░░██░░░░░░░░░░░░░░██
//██░░▄▀▄▀▄▀▄▀▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██████░░▄▀▄▀▄▀▄▀▄▀▄▀▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██░░▄▀░░░░░░██████░░▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██
//██░░▄▀░░░░░░░░░░██░░░░░░▄▀░░░░░░██░░▄▀░░░░░░▄▀░░██░░░░░░▄▀░░░░░░██░░▄▀░░░░░░░░░░██████░░░░░░░░░░░░▄▀▄▀░░██░░▄▀░░░░░░▄▀░░██░░▄▀▄▀▄▀░░░░████░░▄▀░░██░░▄▀░░░░░░░░░░██
//██░░▄▀░░██████████████░░▄▀░░██████░░▄▀░░██░░▄▀░░██████░░▄▀░░██████░░▄▀░░██████████████████████░░░░▄▀░░░░██░░▄▀░░██░░▄▀░░██░░▄▀░░░░▄▀░░░░██░░▄▀░░██░░▄▀░░██████████
//██░░▄▀░░░░░░░░░░██████░░▄▀░░██████░░▄▀░░░░░░▄▀░░██████░░▄▀░░██████░░▄▀░░░░░░░░██████████████░░░░▄▀░░░░████░░▄▀░░██░░▄▀░░██░░▄▀░░░░░░▄▀░░██░░▄▀░░██░░▄▀░░░░░░░░████
//██░░▄▀▄▀▄▀▄▀▄▀░░██████░░▄▀░░██████░░▄▀▄▀▄▀▄▀▄▀░░██████░░▄▀░░██████░░▄▀▄▀▄▀▄▀░░████████████░░░░▄▀░░░░██████░░▄▀░░██░░▄▀░░██░░▄▀░░██░░▄▀░░██░░▄▀░░██░░▄▀▄▀▄▀▄▀░░████
//██░░░░░░░░░░▄▀░░██████░░▄▀░░██████░░▄▀░░░░░░▄▀░░██████░░▄▀░░██████░░▄▀░░░░░░░░██████████░░░░▄▀░░░░████████░░▄▀░░██░░▄▀░░██░░▄▀░░██░░▄▀░░░░░░▄▀░░██░░▄▀░░░░░░░░████
//██████████░░▄▀░░██████░░▄▀░░██████░░▄▀░░██░░▄▀░░██████░░▄▀░░██████░░▄▀░░██████████████░░░░▄▀░░░░██████████░░▄▀░░██░░▄▀░░██░░▄▀░░██░░░░▄▀░░░░▄▀░░██░░▄▀░░██████████
//██░░░░░░░░░░▄▀░░██████░░▄▀░░██████░░▄▀░░██░░▄▀░░██████░░▄▀░░██████░░▄▀░░░░░░░░░░██████░░▄▀▄▀░░░░░░░░░░░░██░░▄▀░░░░░░▄▀░░██░░▄▀░░████░░░░▄▀▄▀▄▀░░██░░▄▀░░░░░░░░░░██
//██░░▄▀▄▀▄▀▄▀▄▀░░██████░░▄▀░░██████░░▄▀░░██░░▄▀░░██████░░▄▀░░██████░░▄▀▄▀▄▀▄▀▄▀░░██████░░▄▀▄▀▄▀▄▀▄▀▄▀▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██░░▄▀░░██████░░░░░░▄▀░░██░░▄▀▄▀▄▀▄▀▄▀░░██
//██░░░░░░░░░░░░░░██████░░░░░░██████░░░░░░██░░░░░░██████░░░░░░██████░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░░░██░░░░░░░░░░░░░░██░░░░░░██████████░░░░░░██░░░░░░░░░░░░░░██
//██████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████








//starts
    public void normalStart(){
        maxFall = 160f;
    }
    public void climbStart(){
        autoJump = false;
        speed.x = 0f;
        speed.y *= 0.2f;
        wallSlideTimer = MAX_WALL_SLIDE_TIMER;
        climbNoMoveTimer = MAX_CLIMB_NO_MOVE_TIMER;
        wallBoostTimer = 0f; //?
        lastClimbDir = 0;
        //snap to walll
        for (int i = 0; i < 2 && !Collision.checkSolidCollision(x + facing, y, hitboxSize); i++)
            x += facing;
    }
    public void dashStart(){
        ++totalDashes;
        dashFromGround = onGround;
        game.setFreeze(0.05f);
        dashCoolDownTimer = 0.2f;
        dashRefillCooldownTimer = 0.1f;
        wallSlideTimer = MAX_WALL_SLIDE_TIMER;
        dashAttackTimer = 0.3f;
        preDashSpeed.set(speed);
        speed.set(0,0);
        dashDir.set(0,0);
        //weird
        if(!onGround && isDucking() && canUnduck()) //unduck if was ducking
            setDucking(false);
        else if(!isDucking() && (demoDash || inputs.yInput.value == 1)) //but duck if we weren't??
            setDucking(true);
        //coroutine
        coroutinePos = 0;
        coroutineTimer = 0;
    }
    public void swimStart(){
        if(speed.y > 0f)
            speed.y *= 0.5f;
        stamina = MAX_STAMINA;
    }


//ends
    public void normalEnd(){
        wallBoostTimer = 0f;
        wallSpeedRetainedTimer = 0f;
        hopWaitXDir = 0;
    }
    public void climbEnd(){
        wallSpeedRetainedTimer = 0; //?
    }
    public void dashEnd(){
        demoDash = false;
    }


//ticks
private int normalTick(){
    //liftboost
    if(/*holding == null*/ true){
        if(inputs.grabButton.down && !isTired() && !isDucking()){
            //try grab holdable

            //try grab wall (moving down, moving/facing towards wall)
            if(!(speed.y < 0f || Calc.sign(speed.x) == -facing)){
                //its in front of you
                if(climbCheck(facing, 0)){
                    setDucking(false); //gotta be sure smmh
                    return ST_CLIMB;
                }
                //snap up and grab it(if not holding down????)
                if(inputs.yInput.value < 1){
                    for (int yOff = -1; yOff >= -2; --yOff) {
                        if(!Collision.checkSolidCollision(x, y + yOff, hitboxSize) &&
                                climbCheck(facing, yOff)){
                            moveYPixel(yOff);
                            setDucking(false); //gotta be sure smmh
                            return ST_CLIMB;
                        }
                    }
                }
            }
        }
        //dash
        if(canDash()){
            //liftboost
            startDash();
            return ST_DASH;
        }
        //duck logic
        if(isDucking()){
            if(onGround && inputs.yInput.value < 1){
                if(canUnduck()) //unduck if ez
                    setDucking(false);
                else if (speed.x == 0f) { //try and wiggle if still
                    for (int xOff = 4; xOff > 0; xOff--) {
                        if (canUnduckAt(x + xOff, y)) {
                            moveX(50f * Game.deltaTime);
                            break;
                        }
                        if (canUnduckAt(x - xOff, y)) {
                            moveX(-50f * Game.deltaTime);
                            break;
                        }
                    }
                }
            }
        } else if (onGround && inputs.yInput.value == 1 && speed.y >= 0f) { //initiate 🦆
            setDucking(true);
        }
    }else{ //holdable logic
        if(!inputs.grabButton.down && minHoldTimer <= 0f){ //not holding grab, throw
            //throwHoldable();
        }
        if(!isDucking() && onGround && inputs.yInput.value == 1 && speed.y >= 0f && !holdCannotDuck){ //holding down, drop+duck if able
            //dropHoldable();
            setDucking(true);
        }else if(onGround && isDucking() && speed.y >= 0f){ //not sure when this would apply?
            if(canUnduck())
                setDucking(false);
            else {
                //dropHoldable();
            }
        } else if (onGround && inputs.yInput.value < 1 && holdCannotDuck) { //disable disable duck
            holdCannotDuck = false;
        }
    }

    //movement/fricion physics stuffs

    if(isDucking() && onGround){ //duck friction
        speed.x = Calc.approach(speed.x, 0f, (float)(500.0 * Game.deltaTime));
    }else{ //non duck accel
        float frictionMult = onGround ? 1f : 0.65f; //less control in air
        float targetSpeed;
        if(/*holding == null*/ true)
            targetSpeed = 90f;
        //else{
        //    if(holding.slowRun)
        //        targetSpeed = 70f;
        //    else if(holding.slowFall && !onGround)
        //        targetSpeed = 108.00001f;
        //        frictionMult*= 0.5f;
        //}

        //odd number deccel at high speed holding forward
        if(Math.abs(speed.x) > targetSpeed && Calc.sign(speed.x) == moveX) //always towards forward targetSpeed because of criteria
            speed.x = Calc.approach(speed.x, targetSpeed * moveX, (float)(400.0 * frictionMult * Game.deltaTime));
        else //could be towards forward targetSpeed, 0, or backwards targetspeed(this is almost always, only differs in target)
            speed.x = Calc.approach(speed.x, targetSpeed * moveX, (float)(1000.0 * frictionMult * Game.deltaTime));
    }

    //gravity/falling physics stuffs
    //maxfall calc
    float fallTarget;
    if(/*holding != null && holding.slowFall && forceMoveXTimer <= 0f*/ false) {//idk why forcemove is here
        //glider movement basically
        fallTarget = switch (inputs.yInput.value){
            case 1 -> 120f;
            case -1 -> 24f;
            default -> 40f;
        };
    } else
        fallTarget = (inputs.yInput.value == 1 && speed.y >= 160f) ? 240f : 160f;
    maxFall = Calc.approach(maxFall, fallTarget, (float)(300.0 * Game.deltaTime));

    //actual gravity/fall speed calc
    if(!onGround){
        float practMaxFall = maxFall;
        if(/*holding != null && holding.slowFall*/false)
            holdCannotDuck = inputs.yInput.value == 1;
        //wall slide physics
        if((moveX == facing || //moving towards wall
                moveX == 0 && inputs.grabButton.down) && //or not moving but grabbing(prolly have to be tired otherwise you'd cancel early to climbing)
                inputs.yInput.value < 1){ //holding down completely disables slide
            if(/*holding == null && */speed.y >= 0f && wallSlideTimer > 0f && climbBoundsCheck(facing) && //moving down with time to slide
                    Collision.checkSolidCollision(x + facing, y, hitboxSize) && canUnduck()){ //is there a solid, space to unduck,
                setDucking(false); //immediately unducks without waiting for main tick?
                wallSlideDir = facing;
            }
            if(wallSlideDir != 0) { //did we slide at sm point and not get cleared
                if(inputs.grabButton.down) //basically if we got here tired i think
                    climbTriggerDir = wallSlideDir;
                practMaxFall = Calc.lerp(160f, 20f, wallSlideTimer / MAX_WALL_SLIDE_TIMER);
            }
        }
        //floaty if low speed and holding jump
        float gravity = (inputs.jumpButton.down || autoJump) && (speed.y > -40f && speed.y < 40f) ? 0.5f : 1f;
        //slowfall holdable cuts it again
        //if(holding != null && holding.slowFall && forceMoveXTimer <= 0f)
        //    gravity*= 0.5f;
        speed.y = Calc.approach(speed.y, practMaxFall, (float)(900.0 * gravity * Game.deltaTime));
    }

    //jumping

    if(varJumpTimer > 0f){ //continue jump speed if holding jump
        if(inputs.jumpButton.down || autoJump)
            speed.y = Math.min(speed.y, varJumpSpeed);
        else
            varJumpTimer = 0f;
    }
    //initiate jump
    if(inputs.jumpButton.getActive()){
        if(jumpGraceTimer > 0f)
            jump();
            //jumps off walls
        else if(canUnduck()){ //need space to do these ig
            boolean canUnDuck = canUnduck();
            if(canUnDuck && wallJumpCheck(1)){ //right side wall jump always first
                //okay we should jump. type priority is climb jump -> super wall jump -> wall jump
                if(facing == 1 && inputs.grabButton.down & stamina > 0f) //explicitly needs stamina
                    climbJump();
                else if (isDashAttacking() && superWallJumpCheck())
                    superWallJump(-1);
                else
                    wallJump(-1);
            } else if (canUnDuck && wallJumpCheck(-1)) { //try left side only if we couldnt right side
                if(facing == -1 && inputs.grabButton.down & stamina > 0f) //explicitly needs stamina
                    climbJump();
                else if (isDashAttacking() && superWallJumpCheck())
                    superWallJump(1);
                else
                    wallJump(1);
            }
            //water(cant if wall nearby?)
            else if(Collision.checkWater(x, y + 2, hitboxSize))
                jump();
        }
    }
    return ST_NORMAL;
}
    private int climbTick(){
        climbNoMoveTimer -= Game.deltaTime;
        if(onGround)
            stamina = MAX_STAMINA;
        //try jumping out of climb into normal
        //no walljump checks just assumed
        if(inputs.jumpButton.getActive() && (!isDucking() || canUnduck())){ //need space to unduck if we're ducked
            if(moveX == -facing) //holding away, wall jump
                wallJump(-facing);
            else
                climbJump(); //no requirements?
            return ST_NORMAL;
        }
        //dash
        if(canDash()){
            //liftboost
            startDash();
            return ST_DASH;
        }
        //let go of grab, cya babe
        if(!inputs.grabButton.down){
            //liftboost
            return ST_NORMAL;
        }
        //ran out of wall to climb?
        if(!Collision.checkSolidCollision(x + facing, y, hitboxSize)){
            if(speed.y < 0f) //going up, try climbhop
                climbHop();
            return ST_NORMAL; //otherwise just drop
        }
        float targetClimbSpeed = 0f;
        boolean stopped = false;
        if(climbNoMoveTimer <= 0f){ //cant move until this is up(shocking considering the name)
            if(inputs.yInput.value == -1){ //climb up
                targetClimbSpeed = -45;
                if(Collision.checkSolidCollision(x, y - 1, hitboxSize) ||
                        (Collision.checkLedgeBlocks(x, y - 6, hitboxSize) && slipCheck(-1))){
                    //hit ceiling, or reaching end of stuff to grab and theres something blocking you up there
                    if(speed.y < 0f)
                        speed.y = 0f;
                    targetClimbSpeed = 0f;
                    stopped = true;
                } else if (slipCheck(0)) { //hit ege but!! not blocked so hop off
                    climbHop();
                    return ST_NORMAL;
                }
            } else if (inputs.yInput.value == 1) { //climb down
                targetClimbSpeed = 80f;
                if(onGround){ //opposite of hitting ceiling if you thionk about it... just foood for thought
                    if(speed.y > 0f)
                        speed.y = 0f;
                    targetClimbSpeed = 0f;
                }
            }else //nothing
                stopped = true;
        }else
            stopped = true;
        lastClimbDir = Calc.sign(targetClimbSpeed); //dir of last movement
        if(stopped && slipCheck(0)) //slide downwards if youre in the danger zone
            targetClimbSpeed = 30f;
        speed.y = Calc.approach(speed.y, targetClimbSpeed, (float)(900.0 * Game.deltaTime));

        //stop from slipping off bottom unwillingly
        if(inputs.yInput.value < 1 && speed.y > 0f && !Collision.checkSolidCollision(x + facing, y + 1, hitboxSize))
            speed.y = 0;

        //eat stamina
        if(climbNoMoveTimer <= 0f){
            if(lastClimbDir == -1)
                stamina -= 45.454544f * Game.deltaTime; //very sane number
            else if (lastClimbDir == 0) //no stamina loss even if we just TRIED moving down
                stamina -= 10f * Game.deltaTime;
        }
        if(stamina <= 0f){ //rip can't hold on
            //liftboost
            return ST_NORMAL;
        }
        return ST_CLIMB;
    }
    private int dashTick(){
        //if(holding == null && dashDir.x != 0 && dashDir.y != 0 && inputs.grabButton.down && canUnduck()){
        //    //try grab holdable
        //    return ST_PICKUP
        //}
        if(dashDir.y < .1f && dashDir.y > -.1f) //hor dash. snap up to jumpthrus
            snapToJumpThrus();
        //dash jumps
        if(inputs.jumpButton.getActive() && canUnduck()){
            //hor dash super jumps
            if(dashDir.y < .1f && dashDir.y > -.1f && jumpGraceTimer > 0f){
                superJump();
                return ST_NORMAL;
            }
            //super wall jump for up dash(no climb jump?)
            if(superWallJumpCheck()){
                if(this.wallJumpCheck(1)){
                    this.superWallJump(-1);
                    return ST_NORMAL;
                }
                if(this.wallJumpCheck(-1)){
                    this.superWallJump(1);
                    return ST_NORMAL;
                }
            }else{ //wall jumps for any other dash
                if(this.wallJumpCheck(1)){
                    if(/*holding == null &&*/ facing == 1 && inputs.grabButton.down && stamina > 0f)
                        climbJump();
                    else
                        wallJump(-1);
                    return ST_NORMAL;
                }
                if(this.wallJumpCheck(-1)){
                    if(/*holding == null &&*/ facing == -1 && inputs.grabButton.down && stamina > 0f)
                        climbJump();
                    else
                        wallJump(1);
                    return ST_NORMAL;
                }
            }
        }
        return ST_DASH;
    }
    public int swimTick(){
        if(!swimCheck())
            return ST_NORMAL;
        if(canUnduck()) //no duck
            setDucking(false);
        //dash but no lose
        if(canDash()){
            demoDash = inputs.crouchDashButton.getActive();
            inputs.dashButton.consume();
            inputs.crouchDashButton.consume();
            return ST_DASH;
        }
        boolean underwater = swimUnderwaterCheck();
        //grab wall (moved up 1 unty)?
        if(inputs.grabButton.down && !isTired() && canUnduck() &&
                !underwater && speed.y >= 0f && Calc.sign(speed.x) == facing && climbCheck(facing, 0) &&
                moveYPixel(-1) == null){
            setDucking(false);
            return ST_CLIMB;
        }
        //joystick input if i ever care(prolly 100x more worth to just make a feather/dashless swim specific solver)
        Vec2f aim = inputs.getSwimAim();
        //speed ellipse size
        float maxX = underwater ? 60f : 80f;
        float maxY = 80f;
        //slow deccel for high speeds facing (raw 80 for x regardless)
        if((speed.x < -80f && aim.x < 0f) || (speed.x > 80f && aim.x > 0f))
            speed.x = Calc.approach(speed.x, maxX * aim.x, (float)(400.0 * Game.deltaTime));
        else
            speed.x = Calc.approach(speed.x, maxX * aim.x, (float)(600.0 * Game.deltaTime));
        //auto-rise at surface, or y movement the same as x above
        if(aim.y == 0 && swimRiseCheck())
            speed.y = Calc.approach(speed.y, -60f, (float)(600.0 * Game.deltaTime));
        else {
            if((speed.y < -80f && aim.y < 0f) || (speed.y > 80f && aim.y > 0f))
                speed.y = Calc.approach(speed.y, maxY * aim.y, (float)(400.0 * Game.deltaTime));
            else
                speed.y = Calc.approach(speed.y, maxY * aim.y, (float)(600.0 * Game.deltaTime));
        }
        //hop OFF!!!!
        if(!underwater && moveX != 0 &&
                Collision.checkSolidCollision(x + moveX, y, hitboxSize) && //hit smthn
                !Collision.checkSolidCollision(x + moveX, y - 3, hitboxSize)) //space to hop out
            climbHop(); //doesnt leave state
        if(inputs.jumpButton.getActive() && swimJumpCheck()){
            jump();
            return ST_NORMAL;
        }
        return ST_SWIM;
    }

//coroutines
    public void dashCoroutine(){
        if(coroutineTimer > 0f) { //wait till next frame
            coroutineTimer -= Game.deltaTime;
            return;
        }
        switch(coroutinePos){
            case 0 -> { //wait frame
                coroutinePos = 1;
            }
            case 1 -> { //starts dash
                dashDir.set(dashAim);
                correctDashPrecision(dashDir);
                speed.set(dashDir.x * 240f, dashDir.y * 240);
                if(Calc.sign(speed.x) == Calc.sign(preDashSpeed.x) && Math.abs(preDashSpeed.x) > Math.abs(speed.x))
                    speed.x = preDashSpeed.x;
                //slow in water
                if(Collision.checkWater(x, y, hitboxSize)){
                    speed.x *= 0.75f;
                    speed.y *= 0.75f;
                }
                //gliderboost
                if(dashDir.x != 0)
                    facing = Calc.sign(dashDir.x);
                //down diagonal dashes from ground immediately activate
                if(onGround && dashDir.x != 0f && dashDir.y > 0f && speed.y > 0f){
                    dashDir.set(Calc.sign(dashDir.x), 0f);
                    speed.x *= 1.2f;
                    speed.y = 0f;
                    setDucking(true);
                }
                coroutinePos = 2;
                coroutineTimer = .15f; //dash length
            }
            case 2 ->{ //end dash
                autoJump = true;
                autoJumpTimer = 0f;
                if(dashDir.y <= 0f){ //any hor/up dash resets to 160 mag speed at end..?
                    speed.set(dashDir.x * 160f, dashDir.y * 160f);
                }
                //upwards dashes get less vert speed
                if(speed.y < 0f)
                    speed.y*= 0.75f;
                changeState(ST_NORMAL);
            }
        }
    }


    private boolean isDashAttacking(){
        return (dashAttackTimer > 0f || state == ST_RED_DASH);
    }

    @Override
    public String toString() {
        return "(" + (x + subPos.x) + ", " + (y + subPos.y) + ") / (" + speed.x + ", " + speed.y + ")";
    }

}
