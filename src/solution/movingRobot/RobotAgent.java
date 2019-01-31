package solution.movingRobot;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


import problem.*;
import solution.State;
import solution.enums.Direction;
import solution.helper.*;

import static solution.helper.GameManager.*;

/**
 * An agent to find the paths using RRT for the robot
 * Created by Jamieson Lee - team CAPS on 03/09/2018
 */


public class RobotAgent {
    private List<Box> movableBoxes;
    private List<Box> movableObstacles;
    private List<StaticObstacle> staticObstacles;
    private RobotConfig robot;
    private double maxRotation;
    public RobotAgent(State state, List<StaticObstacle> staticObstacles) {
        this.movableBoxes = state.getMovingBoxes();
        this.movableObstacles = state.getMovingObstacles();
        this.staticObstacles = staticObstacles;
        this.robot = state.getRobot();
        this.robot.setWidth(state.getRobot().getWidth());
        this.maxRotation = 2 * ((int)((Math.asin((GameManager.MIN_MOVE / 2) / (robot.getWidth() / 2))) * 1000) / 1000.0);
    }

    public RobotAgent(ProblemSpec ps) {
        this. movableBoxes = ps.getMovingBoxes();
        this.movableObstacles = ps.getMovingObstacles();
        this.staticObstacles = ps.getStaticObstacles();
        this.robot = ps.getInitialRobotConfig();
        this.robot.setWidth(ps.getRobotWidth());
        this.maxRotation = 2 * ((int)((Math.asin((GameManager.MIN_MOVE / 2) / (robot.getWidth() / 2))) * 1000) / 1000.0);

    }

    public List<RobotState> searchRobotPath(RobotState iniState, RobotState goalState, Box target) {
        System.out.println("target: " + target.getPos());
        System.out.println("ini: " + iniState.getRobot().getPos());
        System.out.println("goalState: " + goalState.getRobot().getPos());

        TreeNode iniNode = new TreeNode(iniState);
        TreeNode goalNode = new TreeNode(goalState);
        List<RobotState> result = new ArrayList<>();
        TreeGraph treeGraph = new TreeGraph(iniNode);
        Point2D bound1, bound2;
        SearchBounds searchBounds = new SearchBounds();
        final int rotatePressure = (int)Math.round((Math.PI / 2) / maxRotation + 1);

        int curRotatePressure = 0;
        int sampleCount = 0;
        double tolerance = iniState.getRobot().getWidth();
        boolean shrinked = false;
        while(treeGraph.getTreeSize() < GameManager.MAX_SAMPLE) {
            if(sampleCount > 2000 && tolerance<= 1) {
                tolerance += 0.1;
                sampleCount = 0;
                System.out.println("tolerance added: " + tolerance);
            }
            sampleCount++;
            //sample
            bound1 = new Point2D.Double(iniState.getRobot().getPos().getX(), iniState.getRobot().getPos().getY());
            bound2 = new Point2D.Double(goalState.getRobot().getPos().getX(), goalState.getRobot().getPos().getY());
            searchBounds.setBounds(bound1, bound2, tolerance);
            RobotState sampleRobotState = newRobotSample(iniState, searchBounds);

            //find Qnearest and generate new robotSample based on Qnearest and sample
            TreeNode nearestNode= treeGraph.findNearestNode(new TreeNode(sampleRobotState));
            RobotState newRobotState = generateNewSimple(sampleRobotState, nearestNode.getRobotState());
            TreeNode newNode = new TreeNode(newRobotState);

            int rotationStatus = checkRotation(nearestNode.getRobotState());
            if(!checkPointRobot(newNode.getRobotState())) {
                //whether we can rotate
                if(rotationStatus > 0) {
                    if (curRotatePressure > rotatePressure) {
                        //if robot can rotate and exceed rotatePressure
                        double nearestOri = nearestNode.getRobotState().getRobot().getOrientation();
                        if(nearestOri < 0) {
                            nearestOri = nearestOri + Math.PI * 2;
                        } else if (nearestOri > 2 * Math.PI) {
                            nearestOri = nearestOri - Math.PI * 2;
                        }
                        RobotState rotatedNewRobot;
                        //orientation is the same, rotate 90 clockwise or anticlockwise
                        if(GameManager.checkOrientation(nearestOri, goalNode.getRobotState().getRobot().getOrientation())) {
                            if(rotationStatus == 1 || rotationStatus ==3) {
                                rotatedNewRobot = new RobotState(new RobotConfig(nearestNode.getRobotState().getRobot().getPos(),
                                        goalState.getRobot().getOrientation() - Math.PI / 2));
                            } else {
                                rotatedNewRobot = new RobotState(new RobotConfig(nearestNode.getRobotState().getRobot().getPos(),
                                        goalState.getRobot().getOrientation() + Math.PI / 2));
                            }
                            rotatedNewRobot.getRobot().setWidth(nearestNode.getRobotState().getRobot().getWidth());
                            TreeNode rotatedNewNode = new TreeNode(rotatedNewRobot);
                            treeGraph.addVertex(rotatedNewNode);
                            treeGraph.addEdge(nearestNode, rotatedNewNode, rotationStatus);
                            curRotatePressure = 0;
                        } else {
                            if(rotationStatus == 3) {
                                //rotate towards goalState's orientation
                                rotatedNewRobot = new RobotState(new RobotConfig(nearestNode.getRobotState().getRobot().getPos(),
                                        goalState.getRobot().getOrientation()));
                                rotatedNewRobot.getRobot().setWidth(nearestNode.getRobotState().getRobot().getWidth());
                                TreeNode rotatedNewNode = new TreeNode(rotatedNewRobot);
                                treeGraph.addVertex(rotatedNewNode);
                                treeGraph.addEdge(nearestNode, rotatedNewNode, rotationStatus);
                                curRotatePressure = 0;
                            }
                        }
                        //need to start next round of loop when we finished rotating
                        continue;
                    }
                    curRotatePressure += PRESSURE_CHANGE;
                }

                if(checkIfCloseGoalSimple(newRobotState, goalState) && !shrinked) {
                    Point2D pos1 = new Point2D.Double(goalState.getRobot().getPos().getX() - robot.getWidth() / 2, goalState.getRobot().getPos().getY() - robot.getWidth() / 2);
                    Point2D pos2 = new Point2D.Double(goalState.getRobot().getPos().getX() + robot.getWidth() / 2, goalState.getRobot().getPos().getY() + robot.getWidth() / 2);
                    tolerance = robot.getWidth();
                    searchBounds.setBounds(pos1, pos2, tolerance);
                    shrinked = true;
                }
                
                //whether we can make to the new node
                if(!checkLineSimple(nearestNode.getRobotState(), newRobotState)) {
                    treeGraph.addVertex(newNode);
                    treeGraph.addEdge(nearestNode, newNode);

                    if(checkIfNearGoalSimple(newRobotState, goalState) &&
                            GameManager.checkOrientation(nearestNode.getRobotState().getRobot().getOrientation(),
                                    goalNode.getRobotState().getRobot().getOrientation())) {
                        System.out.println("found goal");
                        List<RobotState> path = new ArrayList<>();
                        //rotate first
                        RobotState bridgingRobotState = generateBridgingState(newRobotState, goalState);

                        TreeNode bridgingNode = new TreeNode(bridgingRobotState);
                        treeGraph.addVertex(bridgingNode);
                        treeGraph.addEdge(newNode, bridgingNode);
                        treeGraph.addVertex(goalNode);
                        treeGraph.addEdge(bridgingNode, goalNode);
                        TreeNode tempNode = goalNode;
                        while (tempNode.getParent() != null) {
                            List<RobotState> tempPath = tempNode.getParent().getEdges().get(tempNode);
                            Collections.reverse(tempPath);
                            path.addAll(tempPath);
                            tempNode = tempNode.getParent();
                        }
                        Collections.reverse(path);
                        path.add(goalState);
                        result.addAll(path);
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public RobotState generateNewSimple(RobotState sampleRobotState, RobotState nearestRobotState) {
        double x1 = sampleRobotState.getRobot().getPos().getX();
        double y1 = sampleRobotState.getRobot().getPos().getY();
        double x2 = nearestRobotState.getRobot().getPos().getX();
        double y2 = nearestRobotState.getRobot().getPos().getY();
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;

        double xNew = 0.0d;
        double yNew = 0.0d;

        if(Math.abs(deltaX) >= Math.abs(deltaY)) {
            xNew = (x2 + x1) / 2;
            yNew = y2;
        } else {
            xNew = x2;
            yNew = (y1 + y2) / 2;
        }
        if(deltaX == 0 && deltaY == 0){
            xNew = x2;
            yNew = y2;
        }
        xNew = GameManager.accurateDouble(xNew);
        yNew = GameManager.accurateDouble(yNew);
        RobotConfig newRobotConfig = new RobotConfig(new Point2D.Double(xNew, yNew), nearestRobotState.getRobot().getOrientation());
        newRobotConfig.setWidth(nearestRobotState.getRobot().getWidth());
        RobotState newRobotState = new RobotState(newRobotConfig);
        return newRobotState;

    }

    /**
     * all angles will be with [-Pi, Pi]
     * @param iniState
     * @param goalState
     * @return
     */
    public int checkRotation(RobotState iniState, RobotState goalState) {
        int status = 0;
        boolean pass1 = false, pass2 = false, pass3 = false, pass4 = false;
        double iniOri = iniState.getRobot().getOrientation();
        double goalOri = iniState.getRobot().getOrientation();

        if(!GameManager.checkPosEqual(iniState.getRobot().getPos(), goalState.getRobot().getPos())) {
            System.out.println("robot's ini goal state's positions are different");
            return -1;
        }

        if(GameManager.compareDouble(iniOri, goalOri) == 0) {
            return -1;
        } else {
            //rotate clockwise
            RobotConfig tempRobot = new RobotConfig(iniState.getRobot().getPos(), iniOri);
            tempRobot.setWidth(iniState.getRobot().getWidth());
            double x = iniState.getRobot().getPos().getX();
            double y = iniState.getRobot().getPos().getY();
            double width = iniState.getRobot().getWidth();
            //check clockwise rotation
            if(GameManager.compareDouble(iniOri, Math.PI / 2) == 0) {
                //check topRight collision rec
                pass1 = GameManager.checkCollision(new Rectangle2D.Double(x, y, width / 2,width / 2),
                        movableBoxes, movableObstacles, staticObstacles);
                //check leftBottom collision rec
                pass2 =  GameManager.checkCollision(new Rectangle2D.Double(x - width / 2 , y - width / 2,
                                width / 2,width / 2),
                        movableBoxes, movableObstacles, staticObstacles);
            } else if(GameManager.compareDouble(iniOri, 0) == 0) {
                //check topRight collision rec
                pass1 = GameManager.checkCollision(new Rectangle2D.Double(x - width / 2, y, width / 2,width / 2),
                        movableBoxes, movableObstacles, staticObstacles);
                //check leftBottom collision rec
                pass2 =  GameManager.checkCollision(new Rectangle2D.Double(x, y - width / 2,
                                width / 2,width / 2),
                        movableBoxes, movableObstacles, staticObstacles) ;
            }

            //check antiClockwise rotation
            if(GameManager.compareDouble(iniOri, Math.PI / 2) == 0) {
                //check topleft collision rec
                pass3 = GameManager.checkCollision(new Rectangle2D.Double(x - width / 2, y, width / 2,width / 2),
                        movableBoxes, movableObstacles, staticObstacles);
                //check right bottom collision rec
                pass4 =  GameManager.checkCollision(new Rectangle2D.Double(x , y - width / 2,
                                width / 2,width / 2),
                        movableBoxes, movableObstacles, staticObstacles);
            } else if(GameManager.compareDouble(iniOri, 0) == 0) {
                //check topRight collision rec
                pass3 = GameManager.checkCollision(new Rectangle2D.Double(x, y, width,width / 2),
                        movableBoxes, movableObstacles, staticObstacles);
                //check leftBottom collision rec
                pass4 =  GameManager.checkCollision(new Rectangle2D.Double(x - width / 2 , y - width / 2,
                                width / 2,width / 2),
                        movableBoxes, movableObstacles, staticObstacles) ;
            }
            if(pass3 && pass4) {
                status += 2;
            }
            if(pass1 && pass2) {
                status += 1;
            }
        }
        //return false at default
        return status;
    }

    /**
     * all angles will be with [-Pi, Pi]
     * @param iniState
     * @return -    0 -> can't rotate
     *              1 -> can rotate clockwise
     *              2 -> can rotate anticlockWise
     *              3 -> can rotate clockwise and anticlockwise
     */
    public int checkRotation(RobotState iniState) {
        int returnStatus = 0;
        boolean pass1 = false, pass2 = false, pass3 = false, pass4 = false;
        double iniOri = iniState.getRobot().getOrientation();
        if(iniOri < 0) {
            iniOri = iniOri + Math.PI * 2;
        } else if (iniOri > Math.PI * 2) {
            iniOri -= Math.PI * 2;
        }
        //rotate clockwise
        RobotConfig tempRobot = new RobotConfig(iniState.getRobot().getPos(), iniOri);
        tempRobot.setWidth(iniState.getRobot().getWidth());
        double x = iniState.getRobot().getPos().getX();
        double y = iniState.getRobot().getPos().getY();
        double width = iniState.getRobot().getWidth();
        //check clockwise rotation
        if(GameManager.compareDouble(iniOri, Math.PI / 2) == 0 || GameManager.compareDouble(iniOri, Math.PI / 2 * 3) == 0) {
            //check topRight collision rec
            pass1 = !GameManager.checkCollision(new Rectangle2D.Double(x, y, width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles);
            //check leftBottom collision rec
            pass2 =  !GameManager.checkCollision(new Rectangle2D.Double(x - width / 2 , y - width / 2,
                            width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles);
        } else if(GameManager.compareDouble(iniOri, 0) == 0 || GameManager.compareDouble(iniOri, Math.PI * 2) == 0) {
            //check leftTop collision rec
            pass1 = !GameManager.checkCollision(new Rectangle2D.Double(x - width / 2, y, width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles);
            //check rightBottom collision rec
            pass2 =  !GameManager.checkCollision(new Rectangle2D.Double(x, y - width / 2,
                            width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles) ;
        }

        //check antiClockwise rotation
        if(GameManager.compareDouble(iniOri, Math.PI / 2) == 0 || GameManager.compareDouble(iniOri, Math.PI / 2 * 3) == 0) {
            //check topleft collision rec
            pass3 = !GameManager.checkCollision(new Rectangle2D.Double(x - width / 2, y, width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles);
            //check right bottom collision rec
            pass4 =  !GameManager.checkCollision(new Rectangle2D.Double(x , y - width / 2,
                            width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles);
        } else if(GameManager.compareDouble(iniOri, 0) == 0 || GameManager.compareDouble(iniOri, Math.PI * 2) == 0) {
            //check topRight collision rec
            pass3 = !GameManager.checkCollision(new Rectangle2D.Double(x, y, width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles);
            //check leftBottom collision rec
            pass4 =  !GameManager.checkCollision(new Rectangle2D.Double(x - width / 2 , y - width / 2,
                            width / 2,width / 2),
                    movableBoxes, movableObstacles, staticObstacles) ;
        }
        if(pass3 && pass4) {
            returnStatus += 1;
        }

        if(pass1 && pass2) {
            returnStatus += 2;
        }
        return returnStatus;
    }

    private boolean checkIfNearGoalSimple(RobotState curRobotState, RobotState goalRobotState) {
        Point2D curP = curRobotState.getRobot().getPos();
        Point2D goalP = goalRobotState.getRobot().getPos();
        double width = curRobotState.getRobot().getWidth();
        Rectangle2D curRec = new Rectangle2D.Double(curP.getX() - width / 2, curP.getY() - width / 2, width, width);
        Rectangle2D goalRec = new Rectangle2D.Double(goalP.getX() - width / 2, goalP.getY() - width / 2, width, width);
        return curRec.intersects(goalRec);
    }

    private boolean checkIfCloseGoalSimple(RobotState curRobotState, RobotState goalRobotState) {
        Point2D curP = curRobotState.getRobot().getPos();
        Point2D goalP = goalRobotState.getRobot().getPos();
        double width = curRobotState.getRobot().getWidth();
        Rectangle2D curRec = new Rectangle2D.Double(curP.getX() - width, curP.getY(), width, width);
        Rectangle2D goalRec = new Rectangle2D.Double(goalP.getX() - width, goalP.getY(), width, width);
        return curRec.intersects(goalRec);
    }

    private RobotState generateBridgingState(RobotState curRobotState, RobotState goalRobotState) {
        double finalX = goalRobotState.getRobot().getPos().getX();
        double finalY = goalRobotState.getRobot().getPos().getY();

        double curX = curRobotState.getRobot().getPos().getX();
        double curY = curRobotState.getRobot().getPos().getY();
        double curAngle = curRobotState.getRobot().getOrientation();


        RobotConfig bridgingRobot = new RobotConfig(new Point2D.Double(curX, curY), curAngle);
        bridgingRobot.setWidth(curRobotState.getRobot().getWidth());
        RobotState bridgingRobotState = new RobotState(bridgingRobot);

        double[] coor1 = new double[2];
        coor1[0] = curX;
        coor1[1] = finalY;
        bridgingRobotState.getRobot().setPos(coor1);
        return bridgingRobotState;


    }

    public RobotState newRobotSample(RobotState robotState, SearchBounds searchBounds) {
        double xNew, yNew;
        Random random = new Random();
        int x1Bound = GameManager.convertDoubleToInt(searchBounds.getX1());
        int x2Bound = GameManager.convertDoubleToInt(searchBounds.getX2());
        int y1Bound = GameManager.convertDoubleToInt(searchBounds.getY1());
        int y2Bound = GameManager.convertDoubleToInt(searchBounds.getY2());
        if(y2Bound == y1Bound) {
            xNew = accurateDouble((x1Bound + (random.nextInt(Math.abs(x2Bound - x1Bound)))) / 1000.0);
            yNew = accurateDouble((random.nextInt(Math.abs(y1Bound))) / 1000.0);
        } else if (x1Bound == x2Bound) {
            xNew = accurateDouble(((random.nextInt(Math.abs(x1Bound)))) / 1000.0);
            yNew = accurateDouble((y1Bound + (random.nextInt(Math.abs(y2Bound - y1Bound)))) / 1000.0);
        } else {
            yNew = accurateDouble((y1Bound + (random.nextInt(Math.abs(y2Bound - y1Bound)))) / 1000.0);
            xNew = accurateDouble((x1Bound + (random.nextInt(Math.abs(x2Bound - x1Bound)))) / 1000.0);
        }

        RobotConfig newRobot = new RobotConfig(new Point2D.Double(xNew, yNew), robotState.getRobot().getOrientation());
        newRobot.setWidth(robotState.getRobot().getWidth());
        return new RobotState(newRobot);
    }


    public RobotState generateOneRobotSample(double x1, double x2, double y1, double y2) {

        double robotWidth = robot.getWidth();
        //double angles[] = {0, Math.PI / 2, Math.PI, 3 * Math.PI / 2};    //all possible directions
        double angles[] = {-Math.PI/2, 0, Math.PI/2}; //not important
        double unit = 0.001;
        //split up board X
        double xpos[] = new double[Math.abs((int)((x2 - x1)/unit))+1];
        int countX = 0;

        for (double i = x1*1000; i < x2*1000; i++) {
            xpos[countX++] = unit * i;
        }
        //split up board Y
        double ypos[] = new double[Math.abs((int)((y2 - y1)/unit))+1];
        int countY = 0;
        for (double i = y1*1000; i < y2*1000; i++) {
            ypos[countY++] = unit * i;
        }
        //generate random angle
        Random rand = new Random();
        int randInt1 = rand.nextInt(angles.length);
        double randomAngle = angles[randInt1];
        //generate random position
        int randInt2 = rand.nextInt(xpos.length);
        int randInt3 = rand.nextInt(ypos.length);
        double randX = xpos[randInt2];
        double randY = ypos[randInt3];
        //return result
        double coords[] = {randX, randY};
        RobotConfig newRobot = new RobotConfig(coords, randomAngle, robotWidth);
        return new RobotState(newRobot);
    }

    public boolean checkPointRobot(RobotState state) {
        //int collideCheck = 0;
        //check robot in bounds
        if (state.getRobot().getX1(state.getRobot().getWidth()) < -0.000001 || state.getRobot().getX1(state.getRobot().getWidth()) > 1.000001 ||
                state.getRobot().getX2(state.getRobot().getWidth()) < -0.000001 || state.getRobot().getX2(state.getRobot().getWidth()) > 1.000001 ||
                state.getRobot().getY1(state.getRobot().getWidth()) < -0.000001 || state.getRobot().getY1(state.getRobot().getWidth()) > 1.000001 ||
                state.getRobot().getY2(state.getRobot().getWidth()) < -0.000001 || state.getRobot().getY2(state.getRobot().getWidth()) > 1.000001) {
            return true;
        }
        DecimalFormat three = new DecimalFormat("#0.000");
        RobotConfig robot = state.getRobot();
        double robotWidth = robot.getWidth();
        double rx1 = Double.parseDouble(three.format(robot.getX1(robotWidth)));
        double rx2 = Double.parseDouble(three.format(robot.getX2(robotWidth)));
        double ry1 = Double.parseDouble(three.format(robot.getY1(robotWidth)));
        double ry2 = Double.parseDouble(three.format(robot.getY2(robotWidth)));
        //check moving obstacles
        for (int i = 0; i < movableObstacles.size(); i++) {
            if (movableObstacles.get(i).getRect().intersectsLine(rx1,ry1,rx2,ry2)) {
                //collideCheck++;
                return true;
            }
        }
        //check static obstacles
        for (int i = 0; i < this.staticObstacles.size(); i++) {
            if (this.staticObstacles.get(i).getRect().intersectsLine(rx1,ry1,rx2,ry2)) {
                //collideCheck++;
                return true;
            }
        }

        //check moving boxes
        for (int i = 0; i < movableBoxes.size(); i++) {
            if (movableBoxes.get(i).getRect().intersectsLine(rx1,ry1,rx2,ry2)) {
                if (ry1 == ry2) {
                    //horizontal
                    if (!(rx2 == movableBoxes.get(i).pos.getX() || rx1 == movableBoxes.get(i).pos.getX()+(rx2-rx1))) {
                        //collideCheck++;
                        return true;
                    }
                }
                if (rx1 == rx2) {
                    //horizontal
                    if (!(ry1 == movableBoxes.get(i).pos.getY()+(rx2-rx1) || ry2 == movableBoxes.get(i).pos.getY())) {
                        //collideCheck++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean checkLineRobot(double x1, double x2, double y1, double y2, int div, RobotState nearest) {

        double theta = Math.atan2(y2 - y1, x2 - x1);
        double step = Math.sqrt(Math.pow(y2 - y1,2) + Math.pow(x2 - x1, 2)) / div;
        double robotWidth = robot.getWidth();

        for (int i = 0; i < div; i++) {

            //interpolate point coords
            double coords[] = {x1 + i * step * Math.cos(theta), y1 + i * step * Math.sin(theta)};
            RobotConfig newRobot = new RobotConfig(coords, nearest.getRobot().getOrientation(), robotWidth);

            //check individual points
            if (checkPointRobot(new RobotState(newRobot))) {  //collision
                return true;
            }
        }
        return false;
    }

    public boolean checkLineRobot(RobotState nearest, RobotState newState) {
        return checkLineRobot(nearest.getRobot().getPos().getX(), newState.getRobot().getPos().getX(),
                nearest.getRobot().getPos().getY(), newState.getRobot().getPos().getY(), 10, nearest);
    }


    public boolean checkLineSimple(RobotState nearest, RobotState newState) {
        double newX, newY, nearX, nearY;
        double width = nearest.getRobot().getWidth();
        newX = newState.getRobot().getPos().getX();
        newY = newState.getRobot().getPos().getY();
        nearX = nearest.getRobot().getPos().getX();
        nearY = nearest.getRobot().getPos().getY();
        double orientation = nearest.getRobot().getOrientation();
        if(orientation < 0) {
            orientation += Math.PI * 2;
        } else if(orientation > 2 * Math.PI) {
            orientation -= Math.PI * 2;
        }

        Rectangle2D collisionRec;
        Line2D line0, line1, line2;
        if(GameManager.checkOrientation(nearest.getRobot().getOrientation(), newState.getRobot().getOrientation())) {
            if(GameManager.compareDouble(nearest.getRobot().getPos().getX(), newState.getRobot().getPos().getX()) == 0) {
                //y changed
                if(GameManager.checkOrientation(Math.PI / 2, orientation) || GameManager.checkOrientation(Math.PI / 2 * 3, orientation)) {
                    //vertical orientation
                    line0 = new Line2D.Double(newX, Math.min(newY, nearY) - width / 2 - MAX_ERROR, newX, Math.max(newY, nearY) + width / 2 - MAX_ERROR);
                    line1 = new Line2D.Double(newX - MAX_ERROR, Math.min(newY, nearY) - width / 2 - MAX_ERROR, newX - MAX_ERROR, Math.max(newY, nearY) + width / 2 - MAX_ERROR);
                    line2 = new Line2D.Double(newX + MAX_ERROR, Math.min(newY, nearY) - width / 2 - MAX_ERROR, newX + MAX_ERROR, Math.max(newY, nearY) + width / 2 - MAX_ERROR);
                    if(GameManager.checkCollisionLine(line1, movableBoxes, movableObstacles, staticObstacles) &&
                            GameManager.checkCollisionLine(line2, movableBoxes, movableObstacles, staticObstacles) &&
                            GameManager.checkCollisionLine(line0, movableBoxes, movableObstacles, staticObstacles)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    //horizontal orientation
                    collisionRec = new Rectangle2D.Double(newX - width / 2  + MAX_ERROR, Math.min(nearY, newY) + MAX_ERROR, width - MAX_ERROR, Math.abs(newY  - nearY) - MAX_ERROR);
                    return GameManager.checkCollision(collisionRec, movableBoxes, movableObstacles, staticObstacles);
                }
            } else if(GameManager.compareDouble(nearest.getRobot().getPos().getY(), newState.getRobot().getPos().getY()) == 0) {
                //x changed
                if(GameManager.checkOrientation(Math.PI / 2, orientation) || GameManager.checkOrientation(Math.PI / 2 * 3, orientation)) {
                    //vertical orientation
                    collisionRec = new Rectangle2D.Double(Math.min(newX, nearX) + MAX_ERROR, newY - width / 2  + MAX_ERROR, Math.abs(newX - nearX) - MAX_ERROR, width - MAX_ERROR);
                    return GameManager.checkCollision(collisionRec, movableBoxes, movableObstacles, staticObstacles);
                } else {
                    //horizontal orientation
                    line0 = new Line2D.Double(Math.min(newX, nearX) - width / 2 - MAX_ERROR, newY, Math.max(newX, nearX) + width / 2 - MAX_ERROR, newY);
                    line1 = new Line2D.Double(Math.min(newX, nearX) - width / 2 - MAX_ERROR, newY + MAX_ERROR, Math.max(newX, nearX) + width / 2 - MAX_ERROR, newY + MAX_ERROR);
                    line2 = new Line2D.Double(Math.min(newX, nearX) - width / 2 - MAX_ERROR, newY - MAX_ERROR, Math.max(newX, nearX) + width / 2 - MAX_ERROR, newY - MAX_ERROR);
                    if(GameManager.checkCollisionLine(line1, movableBoxes, movableObstacles, staticObstacles) &&
                            GameManager.checkCollisionLine(line2, movableBoxes, movableObstacles, staticObstacles) &&
                            GameManager.checkCollisionLine(line0, movableBoxes, movableObstacles, staticObstacles)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                System.out.println("current x, y invalid");
                return false;
            }
        }
        return true;
    }
}