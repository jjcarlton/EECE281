package ece281.joshua.robotcontrol;

/**
 * Created by Joshua on 2015-03-03.
 */
public class RobotState {

    public final int FORWARD = 1;
    public final int BACKWARD = 2;
    public final int LEFT = 3;
    public final int RIGHT = 4;
    public final int IDLE = 0;

    private int curState;

    public RobotState(){

    }

    public int getState(){
        return curState;
    }

    public void setState(int state){
        curState = state;
    }


}
