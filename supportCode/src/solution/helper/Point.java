package solution.helper;

/**
 * Supportive class that used to store index X and Y
 * Created By Yifan Lu - team CAPS 01/02/2018
 */
public class Point {
    private int x; //x index based on 0.005 unit width map. Range [0, 199]
    private int y; //y index based on 0.005 unit width map. Range [0, 199]

    public Point(int x, int y) {
        this.x = x;
        this.y = y;

    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point add(int deltaX, int deltaY) {
        int tempX = this.x;
        int tempY = this.y;
        tempX += deltaX;
        tempY += deltaY;
        return new Point(tempX, tempY);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Point) {
            Point temp = (Point)o;
            if(temp.x == this.x && temp.y == this.y) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@ x= " + x + " y= " + y;
    }

    @Override
    public int hashCode() {
        return x * 100000 + y * 7;
    }
}
