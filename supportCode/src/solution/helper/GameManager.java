package solution.helper;

import problem.*;
import solution.enums.Direction;
import solution.enums.ObjectType;
import solution.movingBox.BoxesNode;
import solution.movingBox.BoxesState;
import solution.movingBox.singleBoxSearch.BoxPair;
import solution.movingBox.singleBoxSearch.BoxState;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static solution.enums.Direction.*;

public class GameManager {
    public static final double MIN_MOVE = 0.001;
    public static final double NON_MOVE = 0.0d;
    public static final int BOARD_MAX_BOUND = 1;
    public static final int BOARD_MIN_BOUND = 0;
    public static final int MULTIPLIER = 5;
    public static final int MAX_SAMPLE = 10000000;
    public static final double MAX_ERROR = 0.0001;
    public static final double PRESSURE_CHANGE = 5;

    /**
     * Retrieve box index value within its corresponding list
     * @param movableBoxes
     * @param movableObstacles
     * @param target
     * @return
     */
    public static int getObjectIndex(List<Box> movableBoxes, List<Box> movableObstacles, Box target) {
        int index = 0;
        if(target instanceof MovingBox) {
            for(;index < movableBoxes.size(); index++) {
                if(checkPosEqual(movableBoxes.get(index).getPos(), target.getPos())) {
                    return index;
                }
            }
        } else {
            for(; index < movableObstacles.size(); index++) {
                if(checkPosEqual(movableObstacles.get(index).getPos(), target.getPos())) {
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     * return a deep copy of given boxes list
     * @param boxes
     * @return
     */
    public static List<Box> cloneBoxList(List<Box> boxes) {
        List<Box> result = new ArrayList<>();
        for(Box box : boxes) {
            double x = box.getRect().getX();
            double y = box.getRect().getY();
            double width = box.getWidth();
            if(box instanceof MovingBox) {
                result.add(new MovingBox(new Point2D.Double(x, y), width));
            } else {
                result.add(new MovingObstacle(new Point2D.Double(x, y), width));
            }
        }
        return result;
    }

    /**
     * comparing two double value. Accurate up to three digits after decimal point
     * @param d1
     * @param d2
     * @return  positive value - d1 > d2
     *          0 - d1 == d2
     *          negative value - d1 < d2
     */
    public static int compareDouble(double d1, double d2) {
        int i1 = (int)Math.round(d1 * 1000);
        int i2 = (int)Math.round(d2 * 1000);
        return Integer.compare(i1, i2);
    }

    public static boolean checkPosEqual(Point2D pos1, Point2D pos2) {
        if(compareDouble(pos1.getX(), pos2.getX()) == 0 && compareDouble(pos1.getY(), pos2.getY()) == 0) {
            return true;
        }
        return false;
    }

    /**
     * converting certain value of x pos or y pos to xIndex or yIndex based on unit width of 0.005
     * e.g 0.005 -> 1, 0.995-> 199
     * @param value
     * @return  non-negative integer -> valid double value
     *          negative integer -> invalid double value
     */
    public static int convertIndex(double value) {
        int valueInt = (int)Math.round(value * 1000);
        if(valueInt % MULTIPLIER == 0) {
            return valueInt / MULTIPLIER;
        } else {
            return -1;
        }
    }

    /**
     * this is a helper method that takes in a double and read first three digits after decimal points
     * and then return it as an integer
     * @return e.g 0.023 -> 23,  0.225500023343249 -> 226, 0.334000001 -> 334
     */
    public static int convertDoubleToInt(double value) {
        return (int)Math.round(value * 1000);
    }

    public static double accurateDouble(double value) {
        return Math.round(value * 1000) / 1000.0;
    }

    /**
     * generate correct position using given x Index and y Index based on primitive move distance (0.001)
     * @param x
     * @param y
     * @return
     */
    public static Point2D generateCoor(int x, int y) {
        double corX = x / 1000.0;
        double corY = y / 1000.0;
        return new Point2D.Double(corX, corY);
    }

    public static Point2D generatePoint2D(Point temp) {
        double xD = (temp.getX() * MULTIPLIER) / 1000.0;
        double yD = (temp.getY() * MULTIPLIER) / 1000.0;

        return new Point2D.Double(xD, yD);
    }

    /**
     * this is a helper methods that used to split valid path which has 5 unit gap between each step into
     * a valid path has 1 unit gap(0.001).
     * @return
     *
     * BoxesNode parent, BoxesState boxesState, ObjectType objectType, int targetIndex
     */
    public static List<List<BoxesNode>> splitPath(List<List<BoxesNode>> boxesNodes) {
        List<List<BoxesNode>> result = new ArrayList<>();

        //each list is a box's movement
        for(int i = 0; i < boxesNodes.size(); i++) {
            List<BoxesNode> boxPath = boxesNodes.get(i);
            List<BoxesNode> curBoxPath = new ArrayList<>();
            if (boxPath.get(0).getTarget() instanceof MovingBox) {
                double boxWidth = boxPath.get(0).getTarget().getWidth();
                int curTargetIndex = boxPath.get(0).getTargetIndex();
                Point2D newPoint;

                //add first node in(dir == null, parent == null)
                curBoxPath.add(boxPath.get(0));
                for (int m = 0; m < boxPath.size() - 1; m++) {
                    for (int n = 0; n < MULTIPLIER; n++) {
                        BoxesNode curBoxesNode = boxPath.get(m);
                        List<Box> curMovableBoxes = GameManager.cloneBoxList(curBoxesNode.getBoxesState().getMovableBoxes());
                        List<Box> curMovableObstacles = GameManager.cloneBoxList(curBoxesNode.getBoxesState().getMovableObstacles());

                        BoxesNode nextBoxesNode = boxPath.get(m + 1);
                        List<Box> nextMovableBoxes = GameManager.cloneBoxList(nextBoxesNode.getBoxesState().getMovableBoxes());

                        //either x or y. has 5 unit differences
                        double curX = curMovableBoxes.get(curTargetIndex).getPos().getX();
                        double curY = curMovableBoxes.get(curTargetIndex).getPos().getY();

                        double nextX = nextMovableBoxes.get(curTargetIndex).getPos().getX();
                        double nextY = nextMovableBoxes.get(curTargetIndex).getPos().getY();

                        if (compareDouble(curX, nextX) == 0) {
                            //Vertical movement
                            if (nextY > curY) {
                                //move UP
                                newPoint = new Point2D.Double(accurateDouble(curX), GameManager.accurateDouble(curY + n * MIN_MOVE));
                            } else {
                                //move down
                                newPoint = new Point2D.Double(accurateDouble(curX), GameManager.accurateDouble(curY - n * MIN_MOVE));
                            }
                        } else {
                            //Horizontal movement
                            if (nextX > curX) {
                                //move RIGHT
                                newPoint = new Point2D.Double(accurateDouble(curX + n * MIN_MOVE), accurateDouble(curY));
                            } else {
                                //move LEFT
                                newPoint = new Point2D.Double(accurateDouble(curX - n * MIN_MOVE), accurateDouble(curY));
                            }
                        }
                        Box newBox = new MovingBox(newPoint, boxWidth);
                        curMovableBoxes.set(curTargetIndex, newBox);
                        //assign the last item in the path as its parent
                        curBoxPath.add(new BoxesNode(curBoxPath.get(curBoxPath.size() - 1),
                                new BoxesState(curMovableBoxes, curMovableObstacles), ObjectType.MOVABLE_BOX, curTargetIndex));
                    }
                }
                boxPath.get(boxPath.size() - 1).setParent(curBoxPath.get(curBoxPath.size() - 1));
                curBoxPath.add(boxPath.get(boxPath.size() - 1));
            } else {
                double boxWidth = boxPath.get(0).getTarget().getWidth();
                int curTargetIndex = boxPath.get(0).getTargetIndex();
                Point2D newPoint;

                //add first node in(dir == null, parent == null)
                curBoxPath.add(boxPath.get(0));
                for (int m = 0; m < boxPath.size() - 1; m++) {
                    for (int n = 0; n < MULTIPLIER; n++) {
                        BoxesNode curBoxesNode = boxPath.get(m);
                        List<Box> curMovableBoxes = GameManager.cloneBoxList(curBoxesNode.getBoxesState().getMovableBoxes());
                        List<Box> curMovableObstacles = GameManager.cloneBoxList(curBoxesNode.getBoxesState().getMovableObstacles());

                        BoxesNode nextBoxesNode = boxPath.get(m + 1);
                        List<Box> nextMovableObstacles = GameManager.cloneBoxList(nextBoxesNode.getBoxesState().getMovableObstacles());

                        double curX = curMovableObstacles.get(curTargetIndex).getPos().getX();
                        double curY = curMovableObstacles.get(curTargetIndex).getPos().getY();
                        double nextX = nextMovableObstacles.get(curTargetIndex).getPos().getX();
                        double nextY = nextMovableObstacles.get(curTargetIndex).getPos().getY();
                        if (compareDouble(curX, nextX) == 0) {
                            //Vertical movement
                            if (nextY > curY) {
                                //move UP
                                newPoint = new Point2D.Double(accurateDouble(curX), GameManager.accurateDouble(curY + n * MIN_MOVE));
                            } else {
                                //move down
                                newPoint = new Point2D.Double(accurateDouble(curX), GameManager.accurateDouble(curY - n * MIN_MOVE));
                            }
                        } else {
                            //Horizontal movement
                            if (nextX > curX) {
                                //move RIGHT
                                newPoint = new Point2D.Double(accurateDouble(curX + n * MIN_MOVE), accurateDouble(curY));
                            } else {
                                //move LEFT
                                newPoint = new Point2D.Double(accurateDouble(curX - n * MIN_MOVE), accurateDouble(curY));
                            }
                        }
                        Box newBox = new MovingObstacle(newPoint, boxWidth);
                        curMovableObstacles.set(curTargetIndex, newBox);
                        //assign the last item in the path as its parent
                        curBoxPath.add(new BoxesNode(curBoxPath.get(curBoxPath.size() - 1),
                                new BoxesState(curMovableBoxes, curMovableObstacles), ObjectType.MOVABLE_OBSTACLE, curTargetIndex));
                    }
                }
                boxPath.get(boxPath.size() - 1).setParent(curBoxPath.get(curBoxPath.size() - 1));
                curBoxPath.add(boxPath.get(boxPath.size() - 1));
            }
            result.add(curBoxPath);
        }

        for(int i = 0; i < result.size(); i++) {
            result.get(i).remove(0);
            result.get(i).get(0).setLastDir(result.get(i).get(1).getLastDir());
        }
        return result;
    }

    public static List<BoxPair> cloneBoxPairList(List<BoxPair> list) {
        List<BoxPair> result = new ArrayList<>();
        for(BoxPair boxPair : list) {
            double boxX = boxPair.getBoxState().getBox().getPos().getX();
            double boxY = boxPair.getBoxState().getBox().getPos().getY();
            double boxWidth = boxPair.getBoxState().getBox().getWidth();
            if(boxPair.getBoxState().getBox() instanceof MovingBox) {
                BoxPair tempBoxPair = new BoxPair(new BoxState(new MovingBox
                        (new Point2D.Double(boxX, boxY), boxWidth), boxPair.getBoxState().getIndex()), boxPair.getCost());
                result.add(tempBoxPair);
            } else {
                BoxPair tempBoxPair = new BoxPair(new BoxState(new MovingObstacle
                        (new Point2D.Double(boxX, boxY), boxWidth), boxPair.getBoxState().getIndex()), boxPair.getCost());
                result.add(tempBoxPair);
            }
        }
        return result;
    }

    public static boolean checkCollision(Rectangle2D rectangle2D, List<Box> movableBoxes, List<Box> movableObstacles, List<StaticObstacle> staticObstacles) {
        for(Box box : movableBoxes) {
            Rectangle2D boxRec = box.getRect();
            if(rectangle2D.intersects(boxRec)) {
                return true;
            }
        }
        for(Box box : movableObstacles) {
            Rectangle2D boxRec = box.getRect();
            if(rectangle2D.intersects(boxRec)) {
                return true;
            }
        }
        for(StaticObstacle obstacle : staticObstacles) {
            Rectangle2D boxRec = obstacle.getRect();
            if(rectangle2D.intersects(boxRec)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkCollisionLine(Line2D line2D, List<Box> movableBoxes, List<Box> movableObstacles, List<StaticObstacle> staticObstacles) {
        for(Box box : movableBoxes) {
            Rectangle2D boxRec = box.getRect();
            if(line2D.intersects(boxRec)) {
                return true;
            }
        }
        for(Box box : movableObstacles) {
            Rectangle2D boxRec = box.getRect();
            if(line2D.intersects(boxRec)) {
                return true;
            }
        }
        for(StaticObstacle obstacle : staticObstacles) {
            Rectangle2D boxRec = obstacle.getRect();
            if(line2D.intersects(boxRec)) {
                return true;
            }
        }
        return false;
    }



    public static Direction revertDirection(Direction dir) {
        if(dir == UP) {
            return DOWN;
        } else if(dir == DOWN) {
            return UP;
        } else if(dir == LEFT) {
            return RIGHT;
        } else if(dir == RIGHT) {
            return LEFT;
        } else {
            return null;
        }
    }

    public static boolean checkOrientation(double angle1, double angle2) {
        double temp1 = angle1;
        double temp2 = angle2;

        if(angle1 < 0) {
            temp1 = angle1 + Math.PI * 2;
        }
        if(angle2 < 0) {
            temp2 = angle2 + Math.PI * 2;
        }
        if(angle1 > Math.PI * 2) {
            temp1 = angle1 - Math.PI * 2;
        }
        if(temp2 > Math.PI * 2){
            temp2 = angle2 - Math.PI * 2;
        }

        if(compareDouble(temp1, temp2) == 0) {
            return true;
        }
        return false;
    }

}
