package solution.helper;

import java.util.HashSet;
import java.util.Set;

public class TreeGraph {
    private TreeNode root;
    private Set<TreeNode> allNodes;

    public TreeGraph(TreeNode rootNode) {
        this.root = rootNode;
        allNodes = new HashSet<>();
        allNodes.add(root);
    }

    public void addVertex(TreeNode newTreeNode) {
        allNodes.add(newTreeNode);
    }

    public void addEdge(TreeNode nearestNode, TreeNode newNode) {
        if(findNode(nearestNode) && newNode !=null) {
           nearestNode.addEdge(newNode);
        }
    }

    public void addEdge(TreeNode nearestNode, TreeNode newNode, int rotationStatus) {
        if(findNode(nearestNode) && newNode !=null) {
            nearestNode.addEdge(newNode, rotationStatus);
        }
    }


    public boolean findNode(TreeNode givenTreeNode) {
        for(TreeNode node: allNodes) {
            if(node.equals(givenTreeNode)) {
                return true;
            }
        }
        return false;
    }

    public TreeNode findNearestNode(TreeNode givenTreeNode) {
        TreeNode nearestNode = root;
        double minDis = root.getRobotState().robotDistanceTo(givenTreeNode.getRobotState());
        for(TreeNode node: allNodes) {
            double dis = node.getRobotState().robotDistanceTo(givenTreeNode.getRobotState());
            minDis = (dis <= minDis) ? dis : minDis;
            nearestNode = (dis <= minDis) ? node : nearestNode;
        }
        return nearestNode;
    }

    public int getTreeSize() {
        return this.allNodes.size();
    }

    public Set<TreeNode> getAllNodes() {
        return allNodes;
    }

    public TreeNode getRoot() {
        return root;
    }
}
