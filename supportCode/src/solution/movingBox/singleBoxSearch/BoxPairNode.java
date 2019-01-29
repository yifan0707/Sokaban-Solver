package solution.movingBox.singleBoxSearch;

import solution.enums.Direction;

/**
 * Class that store boxPair together with its depth, pathCost and parent Node
 * Created by Yifan Lu - team CAPS 02/09/2018
 */
public class BoxPairNode implements Comparable<BoxPairNode>{
    private int depth;
    private double pathCost;
    private Direction lastDir;
    private BoxPair boxPair;
    private BoxPairNode parent;

    public BoxPairNode(BoxPair boxPair) {
        this.boxPair = boxPair;
        this.parent = null;
        this.depth = 0;
        this.pathCost = 0;
        this.lastDir = iniLastDir(this.parent, this.boxPair);
    }

    public BoxPairNode(BoxPairNode parent, BoxPair boxPair) {
        this.boxPair = boxPair;
        this.parent = parent;
        this.depth = parent.depth + 1;
        this.pathCost = parent.pathCost + boxPair.getCost();
        this.lastDir = iniLastDir(this.parent, this.boxPair);
    }

    private Direction iniLastDir(BoxPairNode parentNode, BoxPair child) {
        if(parentNode == null || child == null) {
            return null;
        } else {
            int parentX = Math.round((float)(parentNode.getBoxPair().getBoxState().getBox().getPos().getX() * 1000));
            int parentY = Math.round((float)(parentNode.getBoxPair().getBoxState().getBox().getPos().getY() * 1000));
            int childX = Math.round((float)(child.getBoxState().getBox().getPos().getX() * 1000));
            int childY = Math.round((float)(child.getBoxState().getBox().getPos().getY() * 1000));


            if(parentX > childX) {
                return Direction.LEFT;
            } else if(parentX < childX) {
                return Direction.RIGHT;
            } else if(parentY > childY) {
                return Direction.DOWN;
            } else if(parentY < childY) {
                return Direction.UP;
            } else {
                return null;
            }
        }
    }

    public Direction getLastDir() {
        return lastDir;
    }

    public int getDepth() {
        return depth;
    }

    public double getPathCost() {
        return pathCost;
    }

    public BoxPair getBoxPair() {
        return boxPair;
    }

    public BoxPairNode getParent() {
        return parent;
    }

    @Override
    public int compareTo(BoxPairNode o) {
        return Double.compare(this.getPathCost(), o.getPathCost());
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof BoxPairNode) {
            BoxPairNode boxPairNode = (BoxPairNode)o;
            return this.getBoxPair().equals(boxPairNode.getBoxPair());
        }
        return false;
    }
}
