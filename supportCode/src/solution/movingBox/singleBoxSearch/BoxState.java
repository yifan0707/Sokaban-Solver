package solution.movingBox.singleBoxSearch;

import com.sun.org.apache.xpath.internal.SourceTree;
import problem.Box;
import problem.MovingBox;
import problem.MovingObstacle;
import solution.enums.Direction;
import solution.enums.ObjectType;
import solution.gridMap.GridMap;
import solution.helper.GameManager;
import solution.helper.Point;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static solution.helper.GameManager.*;
import static solution.enums.Direction.*;

/**
 * A* Search agent for Moving boxes
 * Created by Yifan Lu - team CAPS on 02/09/2018
 */
public class BoxState {
    private Box box;
    private int index;

    public BoxState(Box box, int index) {
        this.index = index;
        this.box = box;
    }

    public int getIndex() {
        return index;
    }

    /**
     * get possible state based on map, box position and goal position.
     * @param goal
     * @return
     */
    public List<BoxPair> getSuccessors(GridMap map, BoxState goal, Direction lastDir) {
        List<BoxPair> boxPairs = new ArrayList<>(4);
        int xIndex = Math.round((float)(box.getRect().getMinX() / map.getUnitWidth()));
        int yIndex = Math.round((float)(box.getRect().getMinY() / map.getUnitWidth()));
        int widthUnit = Math.round((float)(box.getWidth() / map.getUnitWidth()));
        int widthUnitR = Math.round((float)((box.getWidth() + map.getMovableBoxes().get(0).getWidth() / 2) / map.getUnitWidth())); // rotate clearance m
        int clearance;
        //box can move right
        if(xIndex < map.getCount() && xIndex > 0) {
            //check corresponding map clearance value
            clearance = map.getMap().get(new Point(xIndex - 1, yIndex)).getClearance();
            //opposite grid's clearance need to bigger than 1 otherwise the robot can't fit in.
            if(clearance > 0) {
                if (map.getMap().get(new Point(xIndex + 1, yIndex)).getClearance() >= widthUnit
                        & map.getMap().get(new Point(xIndex + 1, yIndex)).getObjectType() == ObjectType.EMPTY) {
                    double newX = box.getPos().getX() + map.getUnitWidth();
                    double newY = box.getPos().getY();
                    if (goal.getBox() instanceof MovingBox) {
                        Box newBox = new MovingBox(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + boxHeuristic(newBox, goal) + iniTurnCost(RIGHT, lastDir)));
                    } else {
                        Box newBox = new MovingObstacle(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + iniTurnCost(RIGHT, lastDir)));
                    }
                }
            }
        }
        //box can move left
        if(xIndex > 0 && xIndex < map.getCount()) {
            clearance = map.getMap().get(new Point(xIndex + 1, yIndex)).getClearance();
            //opposite grid's clearance need to bigger than 1 otherwise the robot can't fit in.
            if(clearance > 0) {
                //check corresponding map clearance value
                if (map.getMap().get(new Point(xIndex - 1, yIndex)).getClearance() >= widthUnit
                        && map.getMap().get(new Point(xIndex - 1, yIndex)).getObjectType() == ObjectType.EMPTY) {
                    double newX = box.getPos().getX() - map.getUnitWidth();
                    double newY = box.getPos().getY();
                    if (goal.getBox() instanceof MovingBox) {
                        Box newBox = new MovingBox(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + boxHeuristic(newBox, goal) + iniTurnCost(LEFT, lastDir)));
                    } else {
                        Box newBox = new MovingObstacle(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + iniTurnCost(LEFT, lastDir)));
                    }
                }
            }
        }
        //box can move up
        if(yIndex < map.getCount() && yIndex > 0) {
            clearance = map.getMap().get(new Point(xIndex, yIndex - 1)).getClearance();
            //opposite grid's clearance need to bigger than 1 otherwise the robot can't fit in.
            if(clearance > 0) {
                //check corresponding map clearance value
                if(map.getMap().get(new Point(xIndex, yIndex + 1)).getClearance() >= widthUnit
                        &&  map.getMap().get(new Point(xIndex, yIndex + 1)).getObjectType() == ObjectType.EMPTY) {
                    double newX = box.getPos().getX();
                    double newY = box.getPos().getY() + map.getUnitWidth();
                    if(goal.getBox() instanceof MovingBox) {
                        Box newBox = new MovingBox(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + boxHeuristic(newBox, goal) + iniTurnCost(UP, lastDir)));
                    } else {
                        Box newBox = new MovingObstacle(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + iniTurnCost(UP, lastDir)));
                    }
                }
            }
        }
        //box can move down
        if(yIndex > 0 && yIndex < map.getCount()) {
            clearance = map.getMap().get(new Point(xIndex , yIndex + 1)).getClearance();
            //opposite grid's clearance need to bigger than 1 otherwise the robot can't fit in.
            if(clearance > 0) {
                //check corresponding map clearance value
                if(map.getMap().get(new Point(xIndex, yIndex - 1)).getClearance() >= widthUnit
                        && map.getMap().get(new Point(xIndex, yIndex - 1)).getObjectType() == ObjectType.EMPTY) {
                    double newX = box.getPos().getX();
                    double newY = box.getPos().getY() - map.getUnitWidth();
                    if(goal.getBox() instanceof MovingBox) {
                        Box newBox = new MovingBox(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + boxHeuristic(newBox, goal) + iniTurnCost(DOWN, lastDir)));
                    } else {
                        Box newBox = new MovingObstacle(new Point2D.Double(newX, newY), box.getWidth());
                        boxPairs.add(new BoxPair(new BoxState(newBox, goal.getIndex()),
                                MULTIPLIER * MIN_MOVE + iniTurnCost(DOWN, lastDir)));
                    }
                }
            }
        }
        return boxPairs;
    }

    /**
     * function that get H value based on current box position and intended goal
     * function return sum of absolute value of delta X and absolute value of delta Y
     * @param box
     * @param goal
     * @return
     */
    public Double boxHeuristic(Box box,BoxState goal) {
        double x0 = GameManager.accurateDouble(box.getRect().getMinX());
        double y0 = GameManager.accurateDouble(box.getRect().getMinY());
        double x1 = GameManager.accurateDouble(goal.getBox().getPos().getX());
        double y1 = GameManager.accurateDouble(goal.getBox().getPos().getY());
        return Math.abs(x1 - x0) + Math.abs(y1 - y0);
    }


    /**
     * helper method for generating current turning cost
     * @param curDir
     * @param lastDir
     * @return
     */
    private double iniTurnCost(Direction curDir, Direction lastDir) {
        return 1 * ((lastDir == curDir) ? NON_MOVE: MIN_MOVE);

    }

    public Box getBox() {
        return box;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof BoxState){
            BoxState boxState = (BoxState)o;
            int objectX = Math.round((float)(boxState.getBox().getPos().getX() * 1000));
            int objectY= Math.round((float)(boxState.getBox().getPos().getY() * 1000));
            int x = Math.round((float)(this.getBox().getPos().getX() * 1000));
            int y = Math.round((float)(this.getBox().getPos().getY() * 1000));
            return (x == objectX && y == objectY);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int x = Math.round((float)(this.getBox().getPos().getX() * 1000));
        int y = Math.round((float)(this.getBox().getPos().getY() * 1000));
        return x * 7 + y * 131;
    }

    @Override
    public String toString() {
        return "[" +getBox().pos.getX() + ", " + getBox().pos.getY() + "]";
    }
}
