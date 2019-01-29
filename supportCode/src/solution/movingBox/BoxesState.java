package solution.movingBox;

import problem.Box;
import solution.helper.GameManager;

import java.util.List;

/**
 * states for all box objects including movable boxes and movable obstacles
 */
public class BoxesState {
    private List<Box> movableBoxes;
    private List<Box> movableObstacles;

    public BoxesState(List<Box> movableBoxes, List<Box> movableObstacles) {
        this.movableBoxes = movableBoxes;
        this.movableObstacles = movableObstacles;
    }

    public List<Box> getMovableBoxes() {
        return movableBoxes;
    }

    public List<Box> getMovableObstacles() {
        return movableObstacles;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof BoxesState) {
            BoxesState temp = (BoxesState)o;
            if(temp.movableBoxes.size() == this.movableBoxes.size() &&
                    temp.movableObstacles.size() == this.movableBoxes.size()) {
                //check all movable boxes
                for(int i = 0; i < temp.movableBoxes.size(); i++) {
                    if(GameManager.compareDouble(temp.movableBoxes.get(i).pos.getX(), this.movableBoxes.get(i).pos.getX()) == 0
                            && GameManager.compareDouble(temp.movableBoxes.get(i).pos.getY(), this.movableBoxes.get(i).pos.getY()) == 0) {
                        continue;
                    } else {
                        return false;
                    }
                }
                //check all movable obstacles
                for(int i = 0; i < temp.movableObstacles.size(); i++) {
                    if(GameManager.compareDouble(temp.movableObstacles.get(i).pos.getX(), this.movableObstacles.get(i).pos.getX()) == 0
                            && GameManager.compareDouble(temp.movableObstacles.get(i).pos.getY(), this.movableObstacles.get(i).pos.getY()) == 0) {
                        continue;
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
