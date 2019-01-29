package solution.helper;

import problem.RobotConfig;
import solution.movingRobot.RobotState;

import java.awt.geom.Point2D;
import java.util.*;

import static solution.helper.GameManager.checkOrientation;

public class TreeNode {
    private RobotState robotState;
    private TreeNode parent;
    private Map<TreeNode, List<RobotState>> edges;

    public TreeNode(RobotState robotState) {
        this.robotState = robotState;
        this.parent = null;
        edges = new HashMap<>();
    }

    public void addEdge(TreeNode newTreeNode){
        if(newTreeNode != null) {
            edges.put(newTreeNode, generatePath(newTreeNode));
            newTreeNode.parent = this;
        }
    }

    public void addEdge(TreeNode newTreeNode, int rotationStatus) {
        if(newTreeNode != null) {
            edges.put(newTreeNode, generateRotationPath(newTreeNode, rotationStatus));
            newTreeNode.parent = this;
        }
    }

    public List<RobotState> generatePath(TreeNode newTreeNode) {
        double xG = newTreeNode.getRobotState().getRobot().getPos().getX();
        double yG = newTreeNode.getRobotState().getRobot().getPos().getY();
        double xI = this.getRobotState().getRobot().getPos().getX();
        double yI = this.getRobotState().getRobot().getPos().getY();
        List<RobotState> path = new ArrayList<>();
        if(GameManager.compareDouble(xI, xG) == 0) {
            //only y changed
            int totalSteps = (int)Math.round(Math.abs(yG - yI) / GameManager.MIN_MOVE);
            for(int i = 0; i < totalSteps + 1; i++) {
                double tempY;
                if(yG >= yI) {
                    //goal Y > ini Y
                    tempY = GameManager.accurateDouble(yI + i * GameManager.MIN_MOVE);
                } else {
                    //goal Y < ini Y
                    tempY = GameManager.accurateDouble(yI - i * GameManager.MIN_MOVE);
                }
                RobotConfig robotConfig = new RobotConfig(new Point2D.Double(xI, tempY),
                        newTreeNode.getRobotState().getRobot().getOrientation());
                robotConfig.setWidth(newTreeNode.getRobotState().getRobot().getWidth());
                path.add(new RobotState(robotConfig));
            }

        } else if(GameManager.compareDouble(yI, yG) == 0){
            //only x changed
            int totalSteps = (int)Math.round(Math.abs(xG - xI) / GameManager.MIN_MOVE);
            for(int i = 0; i < totalSteps + 1; i++) {
                double tempX;
                if(xG >= xI) {
                    //goal X > ini X
                    tempX = GameManager.accurateDouble(xI + i * GameManager.MIN_MOVE);
                } else {
                    //goal X < ini X
                    tempX = GameManager.accurateDouble(xI - i * GameManager.MIN_MOVE);
                }
                RobotConfig robotConfig = new RobotConfig(new Point2D.Double(tempX, yI),
                        newTreeNode.getRobotState().getRobot().getOrientation());
                robotConfig.setWidth(newTreeNode.getRobotState().getRobot().getWidth());
                path.add(new RobotState(robotConfig));
            }
        }
        return path;
    }

    public List<RobotState> generateRotationPath(TreeNode newTreeNode, int rotationStatus) {
        double xI = this.getRobotState().getRobot().getPos().getX();
        double yI = this.getRobotState().getRobot().getPos().getY();
        double iniOri = this.getRobotState().getRobot().getOrientation();
        double nextOri = newTreeNode.getRobotState().getRobot().getOrientation();
        List<RobotState> result = new ArrayList<>();
        if(!checkOrientation(iniOri, nextOri)){
            //orientation is different
            RobotConfig robotConfig = new RobotConfig(new Point2D.Double(xI, yI), iniOri);
            robotConfig.setWidth(newTreeNode.getRobotState().getRobot().getWidth());
            result.addAll(this.robotState.rotateRobot(this.robotState,
                    newTreeNode.getRobotState().getRobot().getOrientation()));
            return result;
        }
        return result;
    }

    public RobotState getRobotState() {
        return robotState;
    }

    public Map<TreeNode, List<RobotState>> getEdges() {
        return edges;
    }

    public TreeNode getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof  TreeNode) {
            TreeNode node = (TreeNode)o;
            return this.getRobotState().equals(node.getRobotState());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getRobotState().hashCode();
    }
}
