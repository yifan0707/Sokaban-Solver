package solution.movingBox.singleBoxSearch;

import java.util.PriorityQueue;

import solution.helper.GameManager;
import solution.enums.Direction;
import solution.gridMap.TrueGridMap;
import solution.gridMap.FakeGridMap;
import solution.helper.Point;

import java.util.*;

/**
 * A* search agent for boxState
 * Created by Yifan Lu - team CAPS on 08/09/2018
 */
public class BoxAgent {
    private PriorityQueue<BoxPairNode> container;
    private Map<Point, BoxPair> map;

    public BoxAgent(){
        container = new PriorityQueue<>();
        map = new HashMap<>();
    }

    public List<BoxPair> searchTrueBoxPath(TrueGridMap gridMap, BoxState box, BoxState goal) {
        container.add(new BoxPairNode(new BoxPair(box, 0)));

        while(container.size() > 0) {
            BoxPairNode boxPairNode = container.poll();
            BoxPair boxPair = boxPairNode.getBoxPair();

            //retrieve the integer value of current movingBox's position and goal position
            int currentX = Math.round((float)(boxPair.getBoxState().getBox().getPos().getX() * 1000));
            int currentY = Math.round((float)(boxPair.getBoxState().getBox().getPos().getY() * 1000));
            int goalX = Math.round((float)(goal.getBox().getPos().getX() * 1000));
            int goalY = Math.round((float)(goal.getBox().getPos().getY() * 1000));

            this.map.put(new Point(currentX, currentY), boxPair);

            //goal reached if the statement is true
            if(currentX == goalX && currentY == goalY) {
                List<BoxPair> boxPathToGoal = new ArrayList<>();
                //traverse back to the original node
                while(boxPairNode.getParent() != null) {
                    boxPathToGoal.add(boxPairNode.getBoxPair());
                    boxPairNode = boxPairNode.getParent();
                }
                boxPathToGoal.add(new BoxPair(box, 0));
                Collections.reverse(boxPathToGoal);
                resetAgent();
                return boxPathToGoal;
            }

            //get all possible next state
            List<BoxPair> successors = boxPair.getBoxState().getSuccessors(gridMap, goal, boxPairNode.getLastDir());
            for(int i = 0; i < successors.size(); i++) {
                BoxPair newBoxPair = successors.get(i);
                int tempX = Math.round((float)(newBoxPair.getBoxState().getBox().getPos().getX() * 1000));
                int tempY = Math.round((float)(newBoxPair.getBoxState().getBox().getPos().getY() * 1000));
                if((!this.map.containsKey(new Point(tempX, tempY))) && !container.contains(new BoxPairNode(newBoxPair))) {
                    container.add(new BoxPairNode(boxPairNode, newBoxPair));
                }
            }
        }
        resetAgent();
        return null;
    }

    public List<BoxPair> searchFakeBoxPath(FakeGridMap map, BoxState box, BoxState goal) {
        container.add(new BoxPairNode(new BoxPair(box, 0)));

        while(container.size() > 0) {
            BoxPairNode boxPairNode = container.poll();
            BoxPair boxPair = boxPairNode.getBoxPair();

            //retrieve the integer value of current movingBox's position and goal position
            int currentX = Math.round((float)(boxPair.getBoxState().getBox().getPos().getX() * 1000));
            int currentY = Math.round((float)(boxPair.getBoxState().getBox().getPos().getY() * 1000));
            int goalX = Math.round((float)(goal.getBox().getPos().getX() * 1000));
            int goalY = Math.round((float)(goal.getBox().getPos().getY() * 1000));

            this.map.put(new Point(currentX, currentY), boxPair);

            //goal reached if the statement is true
            if(currentX == goalX && currentY == goalY) {
                List<BoxPair> boxPathToGoal = new ArrayList<>();
                //traverse back to the original node
                while(boxPairNode.getParent() != null) {
                    boxPathToGoal.add(boxPairNode.getBoxPair());
                    boxPairNode = boxPairNode.getParent();
                }
                boxPathToGoal.add(new BoxPair(box, 0));
                Collections.reverse(boxPathToGoal);
                resetAgent();
                return boxPathToGoal;
            }

            //get all possible next state
            List<BoxPair> successors = boxPair.getBoxState().getSuccessors(map, goal, boxPairNode.getLastDir());
            for(int i = 0; i < successors.size(); i++) {
                BoxPair newBoxPair = successors.get(i);
                int tempX = Math.round((float)(newBoxPair.getBoxState().getBox().getPos().getX() * 1000));
                int tempY = Math.round((float)(newBoxPair.getBoxState().getBox().getPos().getY() * 1000));
                if((!this.map.containsKey(new Point(tempX, tempY))) && !container.contains(new BoxPairNode(newBoxPair))) {
                    container.add(new BoxPairNode(boxPairNode, newBoxPair));
                }
            }
        }
        resetAgent();
        return null;
    }

    /**
     * function that will take in two closes box pair and return the current direction of box movement
     * @param curPair
     * @param nextPair
     * @return
     */
    private Direction findDirection(BoxPair curPair, BoxPair nextPair) {
        double x1  = curPair.getBoxState().getBox().getPos().getX();
        double y1  = curPair.getBoxState().getBox().getPos().getX();
        double x2  = nextPair.getBoxState().getBox().getPos().getX();
        double y2  = nextPair.getBoxState().getBox().getPos().getX();

        if(x2 > x1) {
            return Direction.RIGHT;
        }
        if(x2 < x1) {
            return Direction.LEFT;
        }
        if(y2 > y1) {
            return Direction.UP;
        }
        if(y2 < y1) {
            return Direction.DOWN;
        }
        return null;
    }

    public void resetAgent() {
        container.clear();
        map.clear();
    }
}
