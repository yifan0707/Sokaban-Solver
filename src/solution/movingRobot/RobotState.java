package solution.movingRobot;


import problem.*;
import solution.helper.GameManager;

import java.util.ArrayList;
import java.util.List;

import static solution.helper.GameManager.MIN_MOVE;

/**
 * This state will be used to handle all problem related to robot
 */
public class RobotState {
    private RobotConfig robot;
    private double maxRotation;

    public RobotState(RobotConfig robot) {
        this.robot = robot;
        this.maxRotation = GameManager.accurateDouble(Math.asin((MIN_MOVE / 2) / (robot.getWidth() / 2)) * 2);
    }

    public void setRobot(RobotConfig robot) {
        this.robot = robot;
    }

    public RobotConfig getRobot() {
        return robot;
    }

    public double robotDistanceTo(RobotState robotState) {
        double x1 = robotState.getRobot().getPos().getX();
        double y1 = robotState.getRobot().getPos().getY();
        double x2 = this.getRobot().getPos().getX();
        double y2 = this.getRobot().getPos().getY();

        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }


    public List<RobotState> rotateRobot(RobotState curRobotState, double finalAngle) {
        double curAngle = curRobotState.getRobot().getOrientation();
        List<RobotState> result = new ArrayList<>();
        if(GameManager.compareDouble(finalAngle, curAngle) == 0) {
            //we don't have to rotate
        } else {
            //we will need to rotate the robot first
            double deltaAngle;
            if(finalAngle >= curAngle) {
                deltaAngle = finalAngle - curAngle;
                int count = (int)Math.round((deltaAngle) / maxRotation) + 1;
                RobotConfig curStateRobot = curRobotState.getRobot();
                for (int i = 0; i < count; i++) {
                    RobotConfig temp;
                    if((curStateRobot.getOrientation() + i * maxRotation > finalAngle)) {
                        temp = new RobotConfig(curStateRobot.getPos(),
                                GameManager.accurateDouble(finalAngle));
                    } else {
                        temp = new RobotConfig(curStateRobot.getPos(),
                                GameManager.accurateDouble(curStateRobot.getOrientation() + i * maxRotation));
                    }
                    temp.setWidth(curStateRobot.getWidth());
                    result.add(new RobotState(temp));
                }
            } else {
                deltaAngle = curAngle - finalAngle;
                int count = (int)(Math.abs(deltaAngle) / maxRotation) + 1;
                RobotConfig curStateRobot = curRobotState.getRobot();
                for (int i = 0; i < count; i++) {
                    RobotConfig temp;
                    if((curStateRobot.getOrientation() - i * maxRotation < finalAngle)) {
                        temp = new RobotConfig(curStateRobot.getPos(),
                                GameManager.accurateDouble(finalAngle));
                    } else {
                        temp = new RobotConfig(curStateRobot.getPos(),
                                GameManager.accurateDouble(curStateRobot.getOrientation() - i * maxRotation));
                    }
                    temp.setWidth(curStateRobot.getWidth());
                    result.add(new RobotState(temp));
                }

            }
            RobotConfig finalRobot = new RobotConfig(curRobotState.getRobot().getPos(), finalAngle);
            finalRobot.setWidth(curRobotState.getRobot().getWidth());
            result.add(new RobotState(finalRobot));
        }
        return result;
    }

    public List<RobotState> rotateRobotClockwise(RobotState robotState) {
        List<RobotState> rotatePath = new ArrayList<>();
        double ori = robotState.getRobot().getOrientation();
        RobotState goalRobotState = new RobotState(new RobotConfig(robotState.getRobot().getPos(), ori));
        goalRobotState.getRobot().setWidth(robotState.getRobot().getWidth());
        rotatePath.addAll(rotateRobot(goalRobotState, ori + Math.PI / 2));
        return rotatePath;
    }

    public List<RobotState> rotateRobotAntiClockwise(RobotState robotState) {
        List<RobotState> rotatePath = new ArrayList<>();
        double ori = robotState.getRobot().getOrientation();
        RobotState goalRobotState = new RobotState(new RobotConfig(robotState.getRobot().getPos(), ori));
        goalRobotState.getRobot().setWidth(robotState.getRobot().getWidth());
        rotatePath.addAll(rotateRobot(goalRobotState, ori - Math.PI / 2));
        return rotatePath;
    }


    @Override
    public boolean equals(Object o){
        if(o instanceof RobotState) {
            RobotState robotState = (RobotState)o;
            return (GameManager.compareDouble(robotState.getRobot().getPos().getX(), this.getRobot().getPos().getX()) == 0
                    && GameManager.compareDouble(robotState.getRobot().getPos().getY(), this.getRobot().getPos().getY()) == 0);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int x = GameManager.convertDoubleToInt(robot.getPos().getX());
        int y = GameManager.convertDoubleToInt(robot.getPos().getY());
        int angle = GameManager.convertDoubleToInt(robot.getOrientation());
        return x * 263 + y * 271 + angle * 197;
    }


}