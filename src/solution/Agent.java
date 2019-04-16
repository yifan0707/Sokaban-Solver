package solution;


import problem.Box;
import problem.ProblemSpec;
import problem.RobotConfig;
import problem.StaticObstacle;
import solution.enums.Direction;
import solution.helper.GameManager;
import solution.movingBox.BoxesNode;
import solution.movingRobot.RobotAgent;
import solution.movingRobot.RobotState;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static solution.enums.Direction.*;
import static solution.helper.GameManager.MIN_MOVE;
import static solution.helper.GameManager.accurateDouble;

/**
 * this Agent will be the overall agent controller output final state list which indicates all movements including robot,
 * boxes, movable obstacles
 */
public class Agent {
    State iniState;
    List<Point2D> goals;
    List<StaticObstacle> staticObstacles;
    double unitWidth;

    public Agent(ProblemSpec ps) {
        iniState = new State(ps);
        System.out.println(iniState.getRobot().getWidth());
        goals = ps.getMovingBoxEndPositions();
        staticObstacles = ps.getStaticObstacles();
        unitWidth = ps.getRobotWidth();
    }

    public List<State> generateFinalPath(List<List<BoxesNode>> nodes) throws IOException{
        List<State> result = new ArrayList<>();
        RobotState robotState1 = new RobotState(iniState.getRobot());
        result.addAll(generateMovePath(iniState.getRobot(), nodes));
        return result;
    }

    private List<State> generateMovePath(RobotConfig iniRobot, List<List<BoxesNode>> nodes) throws IOException{
        RobotConfig curRobot;
        List<State> result = new ArrayList<>();

        for(int i = 0; i < nodes.size(); i++) {
            List<BoxesNode> curBoxPath = nodes.get(i);
            if(i == 0) {
                RobotState curRState = new RobotState(iniRobot);
                RobotState goalRState = initRobotState(curBoxPath.get(0));
                State curState = new State(iniRobot, curBoxPath.get(0).getBoxesState().getMovableBoxes(),
                        curBoxPath.get(0).getBoxesState().getMovableObstacles());
                List<State> robotSwitchTargetPath = generateRobotPath(curRState, goalRState, curState, curBoxPath.get(0).getTarget());
                result.addAll(robotSwitchTargetPath);
            } else {
                State lastState = result.get(result.size() - 1);
                RobotConfig curNewRobot = new RobotConfig(lastState.getRobot().getPos(), lastState.getRobot().getOrientation());
                curNewRobot.setWidth(lastState.getRobot().getWidth());
                RobotState curRState = new RobotState(curNewRobot);
                RobotState goalRState = initRobotState(curBoxPath.get(0));

                State curState = new State(iniRobot, curBoxPath.get(0).getBoxesState().getMovableBoxes(),
                        curBoxPath.get(0).getBoxesState().getMovableObstacles());
                List<State> robotSwitchTargetPath = generateRobotPath(curRState, goalRState, curState, curBoxPath.get(0).getTarget());
                result.addAll(robotSwitchTargetPath);
            }

            //start to push the box based on box path
            for(int j = 0; j < curBoxPath.size() - 1; j++) {
                BoxesNode curNode = curBoxPath.get(j);
                BoxesNode nextNode = curBoxPath.get(j + 1);
                if(result.size() > 0 || i > 0) {
                    curRobot = result.get(result.size() - 1).getRobot();
                    curRobot.setWidth(iniRobot.getWidth());
                } else {
                    curRobot = iniRobot;
                    curRobot.setWidth(iniRobot.getWidth());
                }

                int status = getMovementStatus(curNode, nextNode);
                switch (status){
                    //robot & box move together
                    case 1:
                        result.add(robotMoveWithBox(curRobot, nextNode));
                        break;
                    //robot rotate
                    case 2:
                        //result.addAll(rotateRobotAroundBox(curRobot, curNode, nextNode));
                        BoxesNode tempNextNode = new BoxesNode(curNode.getBoxesState(), curNode.getObjectType(), curNode.getTargetIndex());
                        tempNextNode.setLastDir(nextNode.getLastDir());
                        State lastState = result.get(result.size() - 1);
                        RobotState curRState = initRobotState(curNode);
                        curRState.getRobot().setOrientation(lastState.getRobot().getOrientation());
                        RobotState goalRState = initRobotState(tempNextNode);
                        State curState = new State(iniRobot, curNode.getBoxesState().getMovableBoxes(),
                                curNode.getBoxesState().getMovableObstacles());
                        List<State> robotSwitchTargetPath = generateRobotPath(curRState, goalRState, curState, curNode.getTarget());
                        result.addAll(robotSwitchTargetPath);

                        result.add(robotMoveWithBox(goalRState.getRobot(), nextNode));
                        break;
                }
            }
        }
        for(State state: result) {
            System.out.println(state);
        }
        return result;
    }

    private RobotState initRobotState(BoxesNode node) {
        double xNew, yNew, angle;
        Direction dir = node.getLastDir();
        Box target = node.getTarget();

        if(dir == null) {
            System.out.println("All HEADNODE SHOULD HAVE SAME DIR AS SECOND NODE Head Node, so dir is null");
            System.exit(1);
        }
        if(dir == UP) {
            xNew = GameManager.accurateDouble(target.getRect().getMinX() + target.getWidth() / 2);
            yNew = GameManager.accurateDouble(target.getRect().getMinY());
            angle = 0.0d;
        } else if(dir == DOWN) {
            xNew = GameManager.accurateDouble(target.getRect().getMinX() + target.getWidth() / 2);
            yNew = GameManager.accurateDouble(target.getRect().getMinY() + target.getWidth());
            angle = 0.0d;
        } else if(dir == LEFT) {
            xNew = GameManager.accurateDouble(target.getRect().getMinX() + target.getWidth());
            yNew = GameManager.accurateDouble(target.getRect().getMinY() + target.getWidth() / 2);
            angle = GameManager.accurateDouble(Math.PI / 2);
        } else {
            xNew = GameManager.accurateDouble(target.getRect().getMinX());
            yNew = GameManager.accurateDouble(target.getRect().getMinY() + target.getWidth() / 2);
            angle = GameManager.accurateDouble(Math.PI / 2);
        }
        RobotConfig robotConfig = new RobotConfig(new Point2D.Double(xNew, yNew), angle);
        robotConfig.setWidth(unitWidth);
        return new RobotState(robotConfig);
    }

    private State robotMoveWithBox(RobotConfig robotConfig, BoxesNode node) {
        RobotConfig tempRobot;
        double oriX = robotConfig.getPos().getX();
        double oriY = robotConfig.getPos().getY();
        if(node.getLastDir() == UP) {
            tempRobot = new RobotConfig(new Point2D.Double(accurateDouble(oriX), accurateDouble(oriY + MIN_MOVE)), robotConfig.getOrientation());
            tempRobot.setWidth(robotConfig.getWidth());
        } else if(node.getLastDir() == DOWN) {
            tempRobot = new RobotConfig(new Point2D.Double(accurateDouble(oriX), accurateDouble(oriY - MIN_MOVE)), robotConfig.getOrientation());
            tempRobot.setWidth(robotConfig.getWidth());
        } else if(node.getLastDir() == LEFT) {
            tempRobot = new RobotConfig(new Point2D.Double(accurateDouble(oriX - MIN_MOVE), accurateDouble(oriY)), robotConfig.getOrientation());
            tempRobot.setWidth(robotConfig.getWidth());
        } else {
            tempRobot = new RobotConfig(new Point2D.Double(accurateDouble(oriX + MIN_MOVE), accurateDouble(oriY)), robotConfig.getOrientation());
            tempRobot.setWidth(robotConfig.getWidth());
        }
        return new State(tempRobot, node.getBoxesState().getMovableBoxes(), node.getBoxesState().getMovableObstacles());
    }

    /**
     *
     * @param curNode
     * @param nextNode
     * @return  - 1 -> same direction and same target
     *          - 2 -> diff direction and same target
     *          - 3 -> diff target
     */
    private int getMovementStatus(BoxesNode curNode, BoxesNode nextNode) {
        Direction curDir = curNode.getLastDir();
        Direction nextDir = nextNode.getLastDir();
        int targetIndex = curNode.getTargetIndex();
        int nextTargetIndex = nextNode.getTargetIndex();

        if(targetIndex != nextTargetIndex) {
           return 3;
        } else {
            if(curDir == nextDir) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    private List<State> generateRobotPath(RobotState iniRState, RobotState goalRState, State curState, Box target) throws IOException{
        List<State> result = new ArrayList<>();
        RobotAgent robotAgent = new RobotAgent(curState, staticObstacles);
        List<RobotState> robotStates = robotAgent.searchRobotPath(iniRState, goalRState, target);
        if(robotStates.size() == 0) {
            System.out.println("not valid path found");
        }
        for(RobotState robotState : robotStates) {
            State temp = new State(robotState.getRobot(), curState.getMovingBoxes(), curState.getMovingObstacles());
            result.add(temp);
        }
        return result;
    }

}
