package solution;

import problem.ProblemSpec;
import solution.movingBox.BoxesAgent;
import solution.movingBox.BoxesNode;
import java.io.*;
import java.util.List;


public class Main {
    public static void main(String args[]) throws IOException{
        ProblemSpec ps = new ProblemSpec();
        try {
            ps.loadProblem("src/examples/input3.txt");
        } catch (IOException e1) {
            System.out.println("FAILED: Invalid problem file");
            System.out.println(e1.getMessage());
            return;
        }
        solveGame(ps);
        return;
    }

    private static void solveGame(ProblemSpec ps) throws IOException{
        long startTime;
        long endTime;

        startTime = System.nanoTime();
        Agent agent = new Agent(ps);
        BoxesAgent boxesAgent = new BoxesAgent(ps);
        List<List<BoxesNode>> boxPathToGoalList = boxesAgent.searchBoxesPath();
        List<State> finalStates = agent.generateFinalPath(boxPathToGoalList);
        initOutputFile(finalStates);
        endTime = System.nanoTime();

        System.out.println("total time: " + (endTime - startTime));
    }

    private static void initOutputFile(List<State> states) {
        try {
            PrintWriter writer = new PrintWriter("output3.txt", "UTF-8");
            writer.println(states.size());
            for(State state : states) {
                writer.println(state.toString());
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

}
