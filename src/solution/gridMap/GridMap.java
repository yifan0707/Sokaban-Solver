package solution.gridMap;

import problem.Box;
import problem.StaticObstacle;
import solution.helper.GameManager;
import solution.helper.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a grid map that will split the game panel into 200 * 200 size and only serve one box state.
 */
public abstract class GridMap {
    protected int count;
    protected double unitWidth;
    protected Map<Point, Grid> map;
    protected Box targetObject;

    protected List<Box> movableBoxes;
    protected List<Box> movableObstacles;
    protected List<StaticObstacle> staticObstacles;

    /**
     * One grid map will work for moving one certain box to its goal. Thus we will need to pass in the target box index value
     */
    public GridMap(List<Box> movableBoxes, List<Box> movingObstacles, List<StaticObstacle> staticObstacles, Box targetObject) {
        this.map = new HashMap<>();
        this.targetObject = targetObject;
        count = (int)(1d / (GameManager.MIN_MOVE * GameManager.MULTIPLIER));
        unitWidth = GameManager.MIN_MOVE * GameManager.MULTIPLIER;
        this.movableBoxes = movableBoxes;
        this.movableObstacles = movingObstacles;
        this.staticObstacles = staticObstacles;
    }

    public double getUnitWidth() {
        return unitWidth;
    }

    public Map<Point, Grid> getMap() {
        return map;
    }

    public int getCount() {
        return count;
    }

    public Box getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Box targetObject) {
        this.targetObject = targetObject;
    }

    public void reset() {
        map.clear();
    }


    public List<Box> getMovableBoxes() {
        return movableBoxes;
    }

    public List<Box> getMovableObstacles() {
        return movableObstacles;
    }

    public List<StaticObstacle> getStaticObstacles() {
        return staticObstacles;
    }
}
