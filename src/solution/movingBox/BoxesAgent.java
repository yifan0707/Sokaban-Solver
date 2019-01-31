package solution.movingBox;

import problem.*;
import solution.helper.GameManager;
import solution.enums.ObjectType;
import solution.gridMap.FakeGridMap;
import solution.gridMap.TrueGridMap;
import solution.helper.Point;
import solution.movingBox.singleBoxSearch.BoxAgent;
import solution.movingBox.singleBoxSearch.BoxPair;
import solution.movingBox.singleBoxSearch.BoxState;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import static solution.helper.GameManager.splitPath;

/**
 * A* Search agent for Moving boxes
 * Created by Yifan Lu - team CAPS on 02/09/2018
 */
public class BoxesAgent {
    private List<Box> movableBoxes;
    private List<Box> movableObstacles;
    private List<StaticObstacle> staticObstacles;
    private List<List<BoxesNode>> nodes;
    private List<Point2D> goals;
    private BoxAgent boxAgent;

    private List<Box> goaled;
    private List<Box> nonGoaled;

    public BoxesAgent(ProblemSpec ps){
        boxAgent = new BoxAgent();
        movableBoxes = ps.getMovingBoxes();
        movableObstacles = ps.getMovingObstacles();
        staticObstacles = ps.getStaticObstacles();

        nodes = new ArrayList<>();
        nonGoaled = GameManager.cloneBoxList(movableBoxes);
        goaled = new ArrayList<>();
        goals = ps.getMovingBoxEndPositions();
    }

    public List<List<BoxesNode>> searchBoxesPath() {
        int targetIndex = 0;
        while(nonGoaled.size() > 0 && goaled.size() < goals.size()) {
            Box target = nonGoaled.get(0); //current target of the box based on current targetIndex
            BoxState targetState = new BoxState(target, targetIndex);
            BoxState targetGoalState = new BoxState(new MovingBox(goals.get(targetIndex), target.getWidth()), targetIndex);
            TrueGridMap gridMap = new TrueGridMap(movableBoxes, movableObstacles, staticObstacles, target);

            //search for a valid path on trueGridMap
            List<BoxPair> currentPath = boxAgent.searchTrueBoxPath(gridMap, targetState, targetGoalState);
            if(currentPath != null) {
                nodes.add(generateBoxesPath(currentPath, targetState));
                updateData();
                targetIndex++;
            } else {
                //we are having obstacles that blocking possible way
                FakeGridMap fakeGridMap = new FakeGridMap(movableBoxes, movableObstacles, staticObstacles, target);
                List<BoxPair> currentFakePath = boxAgent.searchFakeBoxPath(fakeGridMap, targetState, targetGoalState);
                //it will always returns a valid fake path otherwise the problem is unsolvable!
                if(currentFakePath == null) {
                    continue;
                }
                List<Box> pathCollisionBoxes = findPathCollisionBoxes(gridMap, fakeGridMap, currentFakePath);
                List<Box> blockingObjects = findingBlockingObjects(target, movableBoxes, movableObstacles, pathCollisionBoxes);

                //after we get all the blocking objects, we can starting to remove all obstacle out of the way one at a time!
                int indexBlockingObjects = 0;
                HashSet<Point> checkedPositionSet = new HashSet<>();
                while(indexBlockingObjects < blockingObjects.size()) {
                    Box blockTemp = blockingObjects.get(indexBlockingObjects);
                    int blockTempIndex = GameManager.getObjectIndex(movableBoxes, movableObstacles, blockTemp);
                    BoxState blockTempState = new BoxState(blockTemp, blockTempIndex);
                    TrueGridMap curTrueBlockMap = new TrueGridMap(movableBoxes, movableObstacles, staticObstacles, blockTemp);

                    Box blockTempGoal = sampleRandomBox(blockTemp, curTrueBlockMap, currentFakePath, checkedPositionSet);
                    BoxState blockTempGoalState = new BoxState(blockTempGoal, blockTempIndex);

                    //check whether current block object can be moved to the sample
                    List<BoxPair> curBlockTruePath = boxAgent.searchTrueBoxPath(curTrueBlockMap, blockTempState, blockTempGoalState);
                    if(curBlockTruePath == null) {
                        //we add the invalid point into the set of check hashSet which stores all invalid points
                        int tempGoalX = GameManager.convertIndex(blockTempGoal.getPos().getX());
                        int tempGoalY = GameManager.convertIndex(blockTempGoal.getPos().getY());
                        checkedPositionSet.add(new Point(tempGoalX, tempGoalY));
                        continue;
                    } else {
                        nodes.add(generateBoxesPath(curBlockTruePath, blockTempState));
                        updateData();
                        indexBlockingObjects++;

                        //recheck whether there is a valid path after removing a obstacle within the path
                        TrueGridMap newTrueGridMap = new TrueGridMap(movableBoxes, movableObstacles, staticObstacles, target);
                        List<BoxPair> currentTruePath = boxAgent.searchTrueBoxPath(newTrueGridMap, targetState, targetGoalState);
                        if(currentTruePath != null) {
                            nodes.add(generateBoxesPath(currentTruePath, targetState));
                            updateData();
                            targetIndex++;
                            break;
                        }
                    }
                }
            }
        }

        if(goaled.size() == goals.size()) {
            nodes = splitPath(nodes);
            return nodes;
        } else {
            System.out.println("what's wrong?");
        }
        return null;
    }

    private List<BoxesState> generatePath(BoxesNode finalNode) {
        List<BoxesState> boxesStates = new ArrayList<>();
        while(finalNode.getParent() != null) {
            boxesStates.add(finalNode.getBoxesState());
            finalNode = finalNode.getParent();
        }
        return boxesStates;
    }

    private void updateData() {
        goaled.clear();
        nonGoaled.clear();
        List<BoxesNode> lastList = nodes.get(nodes.size() - 1);
        BoxesNode lastNode = lastList.get(lastList.size() - 1);
        this.movableBoxes = lastNode.getBoxesState().getMovableBoxes();
        this.movableObstacles = lastNode.getBoxesState().getMovableObstacles();
        if(lastNode.getTarget() instanceof MovingBox) {
            for(int i = 0; i < goals.size(); i++) {
                if(isGoal(movableBoxes.get(i), goals.get(i))) {
                    goaled.add(movableBoxes.get(i));
                    nonGoaled.remove(movableBoxes.get(i));
                } else {
                    goaled.remove(movableBoxes.get(i));
                    nonGoaled.add(movableBoxes.get(i));
                }
            }
        }
    }

    private boolean isGoal(Box target, Point2D goal) {
        return GameManager.checkPosEqual(target.getPos(), goal);
    }

    public Box sampleRandomBox (Box target, TrueGridMap gridMap, List<BoxPair> curFakePath, HashSet<Point> checkedPointSet) {
        Random random = new Random();
        while(checkedPointSet.size() < gridMap.getCount() * gridMap.getCount()) {
            boolean valid = true;
            int x = random.nextInt(199);
            int y = random.nextInt(199);
            int boxClearance = Math.round((float)(target.getWidth() / (GameManager.MIN_MOVE * GameManager.MULTIPLIER)));
            Point temp = new Point(x, y);
            Point2D temp2D = GameManager.generatePoint2D(temp);

            if(checkedPointSet.contains(temp)) {
                valid = false;
                continue;
            }
            if(gridMap.getMap().get(temp).getClearance() < boxClearance) {
                checkedPointSet.add(temp);
                valid = false;
                continue;
            }

            for(BoxPair boxPair: curFakePath) {
                if(boxPair.getBoxState().getBox().getRect().intersects
                        (new Rectangle2D.Double(temp2D.getX(), temp2D.getY(), target.getWidth(), target.getWidth()))) {
                    checkedPointSet.add(temp);
                    valid = false;
                    break;
                }
            }
            if(valid) {
                if(target instanceof MovingObstacle) {
                    return new MovingObstacle(temp2D, target.getWidth());
                } else {
                    return new MovingBox(temp2D, target.getWidth());
                }
            }
        }
        return null;
    }

    /**
     * finding all coordinates that has higher clearance on fakeGridMap comparing to trueGridMap and return corresponding
     * boxes position as an Arraylist
     * @param gridMap
     * @param fakeGridMap
     * @param currentFakePath
     * @return
     */
    public List<Box> findPathCollisionBoxes(TrueGridMap gridMap, FakeGridMap fakeGridMap, List<BoxPair> currentFakePath) {
        List<Point> fakePointList = new ArrayList<>();
        List<Box> pathCollisionBoxes = new ArrayList<>();
        for(BoxPair boxPair: currentFakePath) {
            int x = GameManager.convertIndex(boxPair.getBoxState().getBox().getPos().getX());
            int y = GameManager.convertIndex(boxPair.getBoxState().getBox().getPos().getY());
            if(x >= 0 && y >= 0) {
                Point temp = new Point(x, y);
                if(gridMap.getMap().get(temp).getClearance() != fakeGridMap.getMap().get(temp).getClearance()) {
                    fakePointList.add(temp);
                }
            }
        }

        if(fakePointList.size() > 0) {
            for(int i = 0; i < fakePointList.size(); i++) {
                pathCollisionBoxes.add(new MovingBox(GameManager.generatePoint2D(fakePointList.get(i)),
                        fakeGridMap.getTargetObject().getWidth()));
            }
        }
        return pathCollisionBoxes;
    }

    /**
     * finding all objects within MovableBoxes and MovableObstacles that collide with the fake path's boxes(except targetBox)
     * @param targetObject
     * @param movableBoxes
     * @param movableObstacles
     * @param pathCollisionBoxes
     * @return
     */
    private List<Box> findingBlockingObjects(Box targetObject, List<Box> movableBoxes,
                                             List<Box> movableObstacles, List<Box> pathCollisionBoxes) {
        List<Box> result = new ArrayList<>();
        ObjectType objectType;
        if(targetObject instanceof MovingBox) {
            objectType = ObjectType.MOVABLE_BOX;
        } else {
            objectType = ObjectType.MOVABLE_OBSTACLE;
        }
        //set all grid that related to moving obstacles within the map to MOVABLE_OBSTACLE type and clearance 0
        for(int i = 0; i < movableObstacles.size(); i++) {
            if(!GameManager.checkPosEqual(targetObject.getPos(), movableObstacles.get(i).getPos()) || objectType != ObjectType.MOVABLE_OBSTACLE) {
                for(Box box: pathCollisionBoxes) {
                    if(movableObstacles.get(i).getRect().intersects(box.getRect())) {
                        result.add(movableObstacles.get(i));
                        break;
                    }
                }
            }
        }
        //set all grid that related to movableBoxes within the map to MOVABLE_BOX type and clearance 0
        for(int i = 0; i < movableBoxes.size(); i++) {
            //We will not take the target box into clearance calculation, thus grid within target box should have grid type: EMPTY
            if(!GameManager.checkPosEqual(targetObject.getPos(), movableBoxes.get(i).getPos()) || objectType != ObjectType.MOVABLE_BOX) {
                for(Box box: pathCollisionBoxes) {
                    if(movableBoxes.get(i).getRect().intersects(box.getRect())) {
                        result.add(movableBoxes.get(i));
                    }
                }
            }
        }
        return result;
    }

    public List<BoxesNode> generateBoxesPath(List<BoxPair> path, BoxState boxState) {
        List<BoxesNode> result = new ArrayList<>();
        double width = boxState.getBox().getWidth();

        if(boxState.getBox() instanceof MovingBox) {
            //add the first node in
            List<Box> oriMovableBoxes = GameManager.cloneBoxList(this.movableBoxes);
            List<Box> oriMovableObstacles = GameManager.cloneBoxList(this.movableObstacles);
            BoxesNode currentNode = new BoxesNode(new BoxesState(oriMovableBoxes, oriMovableObstacles), ObjectType.MOVABLE_BOX, boxState.getIndex());
            result.add(currentNode);

            for(int j = 1; j < path.size(); j++) {
                List<Box> movableBoxes = GameManager.cloneBoxList(currentNode.getBoxesState().getMovableBoxes());
                List<Box> movableObstacles = GameManager.cloneBoxList(currentNode.getBoxesState().getMovableObstacles());

                double tempX = path.get(j).getBoxState().getBox().getPos().getX();
                double tempY = path.get(j).getBoxState().getBox().getPos().getY();
                int index = boxState.getIndex();
                movableBoxes.set(index, new MovingBox(new Point2D.Double(tempX, tempY), width));
                currentNode = new BoxesNode(currentNode, new BoxesState(movableBoxes, movableObstacles), ObjectType.MOVABLE_BOX, index);
                result.add(currentNode);
            }
        } else {
            //add the first node in
            List<Box> oriMovableBoxes = GameManager.cloneBoxList(this.movableBoxes);
            List<Box> oriMovableObstacles = GameManager.cloneBoxList(this.movableObstacles);
            BoxesNode currentNode = new BoxesNode(new BoxesState(oriMovableBoxes, oriMovableObstacles), ObjectType.MOVABLE_OBSTACLE, boxState.getIndex());
            result.add(currentNode);

            for(int j = 1; j < path.size(); j++) {
                List<Box> movableBoxes = GameManager.cloneBoxList(currentNode.getBoxesState().getMovableBoxes());
                List<Box> movableObstacles = GameManager.cloneBoxList(currentNode.getBoxesState().getMovableObstacles());

                double tempX = path.get(j).getBoxState().getBox().getPos().getX();
                double tempY = path.get(j).getBoxState().getBox().getPos().getY();
                int index = boxState.getIndex();
                movableObstacles.set(index, new MovingObstacle(new Point2D.Double(tempX, tempY), width));
                currentNode = new BoxesNode(currentNode, new BoxesState(movableBoxes, movableObstacles), ObjectType.MOVABLE_OBSTACLE, index);
                result.add(currentNode);
            }
        }
        return result;
    }


}
