package solution.gridMap;

import solution.enums.ObjectType;

/**
 * helper class for GridMap that used to represent a instance within the gridMap
 */
public class Grid {
    private ObjectType objectType;
    private int clearance; //the maximum width for a object can be place on the grid
    private int listIndex; //the default listIndex will be 0

    public Grid(ObjectType objectType, int clearance) {
        this.objectType = objectType;
        this.clearance = clearance;
        this.listIndex = 0;
    }

    public void setGrid(ObjectType objectType, int clearance) {
        setObjectType(objectType);
        setClearance(clearance);
    }

    /**
     * @param objectType
     * @param clearance
     * @param listIndex - index of corresponding object within its list
     *                  e.g ObjectType MovableBox, 8, 2 -> the corresponding object is the third object within list MovableBoxes
     */
    public void setGrid(ObjectType objectType, int clearance, int listIndex) {
        setObjectType(objectType);
        setClearance(clearance);
        setListIndex(listIndex);
    }

    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public int getListIndex() {
        return listIndex;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public int getClearance() {
        return clearance;
    }

    public void setClearance(int clearance) {
        this.clearance = clearance;
    }
}
