package solution.helper;


import java.awt.geom.Point2D;

/**
 * Class created to describe search boundaries for more efficient sampling
 * Created by Jamieson Lee - team CAPS on 09/09/2018
 */

public class SearchBounds {

    private double x1, x2, y1, y2;

    public SearchBounds(Point2D leftBottom, Point2D topRight, double tolerance) {
        double inx1 = leftBottom.getX();
        double iny1 = leftBottom.getY();
        double inx2 = topRight.getX();
        double iny2 = topRight.getY();
        //x ordering
        if (inx1 >= inx2) {
            this.x1 = inx2 - tolerance;
            this.x2 = inx1 + tolerance;
        } else {
            this.x1 = inx1 - tolerance;
            this.x2 = inx2 + tolerance;
        }

        //y ordering
        if (iny1 >= iny2) {
            this.y1 = iny2 - tolerance;
            this.y2 = iny1 + tolerance;
        } else {
            this.y1 = iny1 - tolerance;
            this.y2 = iny2 + tolerance;
        }

        //Don't sample off the board
        if (this.x1 < 0) {
            this.x1 = 0;
        }
        if (this.x2 > 1) {
            this.x2 = 1;
        }

        if (this.y1 < 0) {
            this.y1 = 0;
        }
        if (this.y2 > 1) {
            this.y2 = 1;
        }
    }

    public SearchBounds(Point2D leftBottom, Point2D topRight) {
        double inx1 = leftBottom.getX();
        double iny1 = leftBottom.getY();
        double inx2 = topRight.getX();
        double iny2 = topRight.getY();

        //x ordering
        if (inx1 >= inx2) {
            this.x1 = inx2;
            this.x2 = inx1;
        } else {
            this.x1 = inx1;
            this.x2 = inx2;
        }

        //y ordering
        if (iny1 >= iny2) {
            this.y1 = iny2;
            this.y2 = iny1;
        } else {
            this.y1 = iny1;
            this.y2 = iny2;
        }

        //Don't sample off the board
        if (this.x1 < 0) {
            this.x1 = 0;
        }
        if (this.x2 > 1) {
            this.x2 = 1;
        }

        if (this.y1 < 0) {
            this.y1 = 0;
        }
        if (this.y2 > 1) {
            this.y2 = 1;
        }
    }

    public SearchBounds() {
        searchEntireBoard();
    }

    //get the current search bounds x1
    public double getX1() {
        return this.x1;
    }

    //get the current search bounds x2
    public double getX2() {
        return this.x2;
    }

    //get the current search bounds y1
    public double getY1() {
        return this.y1;
    }

    //get the current search bounds y2
    public double getY2() {
        return this.y2;
    }

    //set the search bounds to the entire board
    public void searchEntireBoard() {
        this.x1 = 0;
        this.x2 = 1;
        this.y1 = 0;
        this.y2 = 1;
    }

    //set the bounds to a new value
    public void setBounds(Point2D leftBottom, Point2D topRight, double tolerance) {
        double inx1 = leftBottom.getX();
        double iny1 = leftBottom.getY();
        double inx2 = topRight.getX();
        double iny2 = topRight.getY();

        //x ordering
        if (inx1 >= inx2) {
            this.x1 = inx2 - tolerance;
            this.x2 = inx1 + tolerance;
        } else {
            this.x1 = inx1 - tolerance;
            this.x2 = inx2 + tolerance;
        }

        //y ordering
        if (iny1 >= iny2) {
            this.y1 = iny2 - tolerance;
            this.y2 = iny1 + tolerance;
        } else {
            this.y1 = iny1 - tolerance;
            this.y2 = iny2 + tolerance;
        }

        //Don't sample off the board
        if (this.x1 < 0) {
            this.x1 = 0;
        }
        if (this.x2 > 1) {
            this.x2 = 1;
        }

        if (this.y1 < 0) {
            this.y1 = 0;
        }
        if (this.y2 > 1) {
            this.y2 = 1;
        }
    }
}

