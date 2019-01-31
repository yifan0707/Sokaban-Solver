package solution.movingBox.singleBoxSearch;

/**
 * Class that used to store boxState together with its cost
 * Created by Yifan Lu - team CAPS 02/09/2018
 */
public class BoxPair implements Comparable<BoxPair>{
    private BoxState box;
    private double cost;

    public BoxPair(BoxState box, double cost) {
        this.box = box;
        this.cost = cost;
    }

    public BoxState getBoxState() {
        return box;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public int compareTo(BoxPair o) {
        return Double.compare(this.cost, o.cost);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof BoxPair) {
            BoxPair boxPair = (BoxPair)o;
            return this.getBoxState().equals(boxPair.getBoxState());
        } else {
            return false;
        }
    }
}
