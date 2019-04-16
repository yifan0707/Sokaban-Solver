package problem;
import solution.movingRobot.RobotState;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        ProblemSpec ps = new ProblemSpec();
        try {
            ps.loadProblem("./src/examples/input2.txt");
            ps.loadSolution("./src/examples/output2.txt");
        } catch (IOException e) {
            System.out.println("IO Exception occured");
        }
        System.out.println("Finished loading!");

    }
}