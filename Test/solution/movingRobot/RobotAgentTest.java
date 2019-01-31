package solution.movingRobot;

import org.junit.Test;
import problem.Box;
import problem.ProblemSpec;
import problem.RobotConfig;
import problem.StaticObstacle;
import solution.State;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class RobotAgentTest {

    @Test
    public void rotateRobot() {
    }

    @Test
    public void searchRobotPathNew() {
    }

    @Test
    public void generateNewSimple() {
    }

    @Test
    public void computeDistance() {
    }

    @Test
    public void generateOneRobotSample() {
    }

    @Test
    public void checkPointRobot() {
        ProblemSpec ps = new ProblemSpec();
        try {
            ps.loadProblem("C:\\Users\\cq\\Desktop\\comp3702\\supportCode\\input2.txt");
        } catch (IOException e1) {
            System.out.println("FAILED: Invalid problem file");
            System.out.println(e1.getMessage());
            return;
        }
        RobotAgent robotAgent = new RobotAgent(ps);
        RobotConfig intialRobot = ps.getInitialRobotConfig();
        intialRobot.setWidth(0.1);
        double coords[] = {0.1,0.1};
        RobotConfig newRobot = new RobotConfig(coords, 0, intialRobot.getWidth());
        RobotState newState = new RobotState(newRobot);
        boolean result = robotAgent.checkPointRobot(newState);
        assertEquals(false, robotAgent.checkPointRobot(newState));
    }

    @Test
    public void checkLineRobot() {
        ProblemSpec ps = new ProblemSpec();
        try {
            ps.loadProblem("C:\\Users\\cq\\Desktop\\comp3702\\supportCode\\input2.txt");
        } catch (IOException e1) {
            System.out.println("FAILED: Invalid problem file");
            System.out.println(e1.getMessage());
            return;
        }
        RobotAgent robotAgent = new RobotAgent(ps);
        RobotConfig intialRobot = ps.getInitialRobotConfig();
        intialRobot.setWidth(0.1);
        double coords1[] = {0.1,0.1};
        RobotConfig newRobot1 = new RobotConfig(coords1, 0, intialRobot.getWidth());
        RobotState newState1 = new RobotState(newRobot1);

        double coords2[] = {0.1,0.195};
        RobotConfig newRobot2 = new RobotConfig(coords2, 0, intialRobot.getWidth());
        RobotState newState2 = new RobotState(newRobot2);
        boolean result = robotAgent.checkLineRobot(newState1, newState2);
        assertEquals(false,result);
    }

    @Test
    public void checkIfGoal() {
    }
}