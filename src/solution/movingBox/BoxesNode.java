package solution.movingBox;

import problem.Box;
import solution.enums.Direction;
import solution.enums.ObjectType;

public class BoxesNode {
    private Direction lastDir;
    private Box target;
    private ObjectType objectType;
    private int targetIndex;
    private BoxesState boxesState;
    private BoxesNode parent;

    public BoxesNode(BoxesState boxesState, ObjectType objectType, int targetIndex) {
        this.boxesState = boxesState;
        this.parent = null;
        this.objectType = objectType;
        this.targetIndex = targetIndex;
        if(objectType == ObjectType.MOVABLE_BOX) {
            this.target = boxesState.getMovableBoxes().get(targetIndex);
        } else {
            this.target = boxesState.getMovableObstacles().get(targetIndex);
        }
        this.lastDir = null;
    }

    public BoxesNode(BoxesNode parent, BoxesState boxesState, ObjectType objectType, int targetIndex) {
        this.boxesState = boxesState;
        this.parent = parent;
        this.objectType = objectType;
        this.targetIndex = targetIndex;
        if(objectType == ObjectType.MOVABLE_BOX) {
            this.target = boxesState.getMovableBoxes().get(targetIndex);
        } else {
            this.target = boxesState.getMovableObstacles().get(targetIndex);
        }
        this.lastDir = generateLastDir();
    }

    private Direction generateLastDir() {
        Box parentTarget = parent.target;
        double parentX = parentTarget.getPos().getX();
        double parentY = parentTarget.getPos().getY();
        double x = target.getPos().getX();
        double y = target.getPos().getY();

        if(parent.getTargetIndex() == this.targetIndex && parent.getObjectType() == this.getObjectType()) {
            if(parentX > x) {
                return Direction.LEFT;
            }
            if(parentX < x) {
                return Direction.RIGHT;
            }
            if(parentY > y) {
                return Direction.DOWN;
            }
            if(parentY < y) {
                return Direction.UP;
            }
        }
        return null;
    }

    public Box getTarget() {
        return target;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public Direction getLastDir() {
        return lastDir;
    }

    public BoxesState getBoxesState() {
        return boxesState;
    }

    public BoxesNode getParent() {
        return parent;
    }

    public void setLastDir(Direction lastDir) {
        this.lastDir = lastDir;
    }

    public void setParent(BoxesNode parent) {
        this.parent = parent;
    }
}
