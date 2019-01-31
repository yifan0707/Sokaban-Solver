package problem;

		import problem.Box;

		import java.awt.geom.Point2D;


/**
 * This class represents one of the moving boxes in Assignment 1.
 *
 * @author Sergiy Dudnikov
 */
public class MovingBox extends Box {

	/**
	 * Constructs a Moving movingBox at a position width a side width
	 * 
	 * @param pos
	 *            the position of the movingBox
	 * @param width
	 *            the width (and height) of the movingBox
	 */
	public MovingBox(Point2D pos, double width) {
		super(pos, width);
	}
}
