package solution.gridMap;

import problem.Box;
import problem.MovingBox;
import problem.StaticObstacle;
import solution.helper.GameManager;
import solution.helper.Point;
import solution.enums.ObjectType;

import java.util.List;
import java.util.Map;

/**
 * This class is a grid map that will split the game panel into 200 * 200 size and only serve one box state.
 */
public class FakeGridMap extends GridMap{
    /**
     * One grid map will work for moving one certain box to its goal. Thus we will need to pass in the target box index value
     * @param movableBoxes
     * @param movingObstacles
     * @param staticObstacles
     */
    public FakeGridMap(List<Box> movableBoxes, List<Box> movingObstacles, List<StaticObstacle> staticObstacles, Box targetObject) {
        super(movableBoxes, movingObstacles, staticObstacles, targetObject);
        initializeFakeGridMap();
        computeFakeMap(movableBoxes, movingObstacles, staticObstacles);
        computeFakeClearance(movableBoxes, movingObstacles);
    }

    private void initializeFakeGridMap() {
        //insert all grid into the map
        for(int x = 0; x < count; x++) {
            for(int y = 0; y < count; y++){
                map.put(new Point(x, y), new Grid(ObjectType.EMPTY, 0));
            }
        }
    }

    private void computeFakeMap(List<Box> movableBoxes, List<Box> movingObstacles, List<StaticObstacle> staticObstacles) {
        int x0, y0, x1, y1;


        ObjectType objectType;
        if(targetObject instanceof MovingBox) {
            objectType = ObjectType.MOVABLE_BOX;
        } else {
            objectType = ObjectType.MOVABLE_OBSTACLE;
        }

        //set all grid that related to static obstacles within the map to STATIC_OBSTACLE type and clearance 0
        for(int i = 0; i < staticObstacles.size(); i++) {
            x0 = Math.round((float)((staticObstacles.get(i).getRect().getMinX()) / unitWidth));
            y0 = Math.round((float)((staticObstacles.get(i).getRect().getMinY()) / unitWidth));
            x1 = Math.round((float)((staticObstacles.get(i).getRect().getMaxX()) / unitWidth));
            y1 = Math.round((float)((staticObstacles.get(i).getRect().getMaxY()) / unitWidth));


            for (int x = x0; x < x1; x++) {
                for (int y = y0; y < y1; y++) {
                    Grid grid = map.get(new Point(x, y));
                    //static_obstacle's clearance is always 0
                    grid.setGrid(ObjectType.STATIC_OBSTACLE, 0, i);
                }
            }
        }

        //set all grid that related to moving obstacles within the map to MOVABLE_OBSTACLE type and clearance 0
        for(int i = 0; i < movingObstacles.size(); i++) {
            //We will not take the target box into clearance calculation, thus grid within target box should have grid type: EMPTY
            if(!GameManager.checkPosEqual(targetObject.getPos(), movingObstacles.get(i).getPos())
                    || objectType != ObjectType.MOVABLE_OBSTACLE) {
                x0 = Math.round((float)((movingObstacles.get(i).getRect().getMinX()) / unitWidth));
                y0 = Math.round((float)((movingObstacles.get(i).getRect().getMinY()) / unitWidth));
                x1 = Math.round((float)((movingObstacles.get(i).getRect().getMaxX()) / unitWidth));
                y1 = Math.round((float)((movingObstacles.get(i).getRect().getMaxY()) / unitWidth));

                for (int x = x0; x < x1; x++) {
                    for (int y = y0; y < y1; y++) {
                        Grid grid = map.get(new Point(x, y));
                        //static_obstacle's clearance is always 0
                        grid.setGrid(ObjectType.MOVABLE_OBSTACLE, 0, i);
                    }
                }
            }
        }

        //set all grid that related to movableBoxes within the map to MOVABLE_BOX type and clearance 0
        for(int i = 0; i < movableBoxes.size(); i++) {
            //We will not take the target box into clearance calculation, thus grid within target box should have grid type: EMPTY
            if(!GameManager.checkPosEqual(targetObject.getPos(), movableBoxes.get(i).getPos())
                    || objectType != ObjectType.MOVABLE_BOX) {
                x0 = Math.round((float)((movableBoxes.get(i).getRect().getMinX()) / unitWidth));
                y0 = Math.round((float)((movableBoxes.get(i).getRect().getMinY()) / unitWidth));
                x1 = Math.round((float)((movableBoxes.get(i).getRect().getMaxX()) / unitWidth));
                y1 = Math.round((float)((movableBoxes.get(i).getRect().getMaxY()) / unitWidth));

                for (int x = x0; x < x1; x++) {
                    for (int y = y0; y < y1; y++) {
                        Grid grid = map.get(new Point(x, y));
                        //static_obstacle's clearance is always 0
                        grid.setGrid(ObjectType.MOVABLE_BOX, 0, i);
                    }
                }
            }
        }
    }

    private void computeFakeClearance(List<Box> movableBoxes, List<Box> movingObstacles) {
        //calculate all clearance value for all empty grid
        double width1 = movableBoxes.get(0).getWidth();
        double width2 = movingObstacles.get(0).getWidth();
        int maxClearance = Math.round((float)((Math.max(width1, width2) + width1 / 2) / unitWidth));

        for(int x = 0; x < count; x++) {
            for(int y = 0; y < count; y++) {
                Point temp = new Point(x, y);
                boolean expandable = true;
                if(map.get(temp).getObjectType() != ObjectType.STATIC_OBSTACLE) {
                    int clearance = 1;
                    while( (y + clearance) < count && (x + clearance) < count) {
                        for(int i = y; i < y + clearance; i++) {
                            if(map.get(new Point(x + clearance, i)).getObjectType() == ObjectType.STATIC_OBSTACLE) {
                                expandable = false;
                                break;
                            }
                        }
                        for(int i = x; i < x + clearance; i++) {
                            if(map.get(new Point(i, y + clearance)).getObjectType() == ObjectType.STATIC_OBSTACLE) {
                                expandable = false;
                                break;
                            }
                        }
                        if(map.get(new Point(x + clearance, y + clearance)).getObjectType() == ObjectType.STATIC_OBSTACLE) {
                            expandable = false;
                            break;
                        }
                        if(clearance < maxClearance && expandable) {
                            clearance++;
                        } else {
                            break;
                        }
                    }
                    map.get(temp).setClearance(clearance);
                }
            }
        }
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

    public void reset() {
        map.clear();
    }

}
