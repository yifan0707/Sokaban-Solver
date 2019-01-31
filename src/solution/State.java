package solution;

import problem.Box;
import problem.ProblemSpec;
import problem.RobotConfig;
import problem.StaticObstacle;
import solution.helper.GameManager;

import java.util.List;

public class State {
    private RobotConfig robot;
    private List<Box> movingBoxes;
    private List<Box> movingObstacles;
    private double maxRotation;

    public State(RobotConfig robot, List<Box> movingBoxes, List<Box> movingObstacles) {
        this.robot = robot;
        this.robot.setWidth(robot.getWidth());
        this.movingBoxes = movingBoxes;
        this.movingObstacles = movingObstacles;
        maxRotation = ((int)((2 * Math.asin((GameManager.MIN_MOVE / 2) / (robot.getWidth() / 2))) * 1000) / 1000.0);
    }

    public State(ProblemSpec ps){
        this.robot = ps.getInitialRobotConfig();
        this.robot.setWidth(ps.getRobotWidth());
        this.movingBoxes = ps.getMovingBoxes();
        this.movingObstacles = ps.getMovingObstacles();
        maxRotation = ((int)((2 * Math.asin((GameManager.MIN_MOVE / 2) / (ps.getRobotWidth() / 2))) * 1000) / 1000.0);
    }

    public double getMaxRotation() {
        return maxRotation;
    }

    public void setRobot(RobotConfig robot) {
        this.robot = robot;
    }

    public RobotConfig getRobot() {
        return robot;
    }

    public List<Box> getMovingBoxes() {
        return movingBoxes;
    }

    public List<Box> getMovingObstacles() {
        return movingObstacles;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(GameManager.accurateDouble(robot.getPos().getX()) + " " + GameManager.accurateDouble(robot.getPos().getY()) + " " + robot.getOrientation() + " ");
        for(int i = 0; i < movingBoxes.size(); i++) {
            stringBuilder.append(GameManager.accurateDouble(movingBoxes.get(i).getPos().getX() + movingBoxes.get(i).getWidth() / 2) + " " + GameManager.accurateDouble(movingBoxes.get(i).getPos().getY()  + movingBoxes.get(i).getWidth() / 2) + " ");
        }
        for(int i = 0; i < movingObstacles.size(); i++) {
            stringBuilder.append(GameManager.accurateDouble(movingObstacles.get(i).getPos().getX() + movingObstacles.get(i).getWidth() / 2) + " " + GameManager.accurateDouble(movingObstacles.get(i).getPos().getY()  + movingObstacles.get(i).getWidth() / 2) + " ");
        }
        return stringBuilder.toString();
    }


    @Override
    public boolean equals(Object o){
        if(o instanceof State) {
            State state = (State)o;
            boolean isEqual = (GameManager.compareDouble(state.getRobot().getPos().getX(), this.getRobot().getPos().getX()) == 0
                    && GameManager.compareDouble(state.getRobot().getPos().getY(), this.getRobot().getPos().getY()) == 0);

            if(state.getMovingBoxes().size() == this.getMovingBoxes().size() &&
                    state.getMovingObstacles().size() == this.getMovingObstacles().size()) {
                //check all movable boxes
                for(int i = 0; i < state.getMovingBoxes().size(); i++) {
                    if(GameManager.compareDouble(state.getMovingBoxes().get(i).pos.getX(), this.getMovingObstacles().get(i).pos.getX()) == 0
                            && GameManager.compareDouble(state.getMovingBoxes().get(i).pos.getY(), this.getMovingObstacles().get(i).pos.getY()) == 0) {
                        continue;
                    } else {
                        isEqual = false;
                        break;
                    }
                }
                //check all movable obstacles
                for(int i = 0; i < state.getMovingObstacles().size(); i++) {
                    if(GameManager.compareDouble(state.getMovingObstacles().get(i).pos.getX(), this.getMovingObstacles().get(i).pos.getX()) == 0
                            && GameManager.compareDouble(state.getMovingObstacles().get(i).pos.getY(), this.getMovingObstacles().get(i).pos.getY()) == 0) {
                        continue;
                    } else {
                        isEqual = false;
                        break;
                    }
                }
                return isEqual;
            }
        }
        return false;
    }
}