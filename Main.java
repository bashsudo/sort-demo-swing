/*
 * CSC 345 PROJECT
 * Class:           Main.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     The entry point to the entire visualization app of the project.
 *                  It first parses test cases from the file "test_cases.txt" that
 *                  outline specific sorting algorithms, input arrays to sort, and
 *                  other visualization and plotting parameters. It then passes them
 *                  to the Swing GUI that can then visualize the sorting process
 *                  as a bar graph and plot the access count of the sorting algorithm
 *                  as the size of the input N increases.
 */

import javax.swing.SwingUtilities;

public class Main {
    /**
     * Runs the entire CSC 345 Sorting Algorithm Demonstration App.
     * First it reads test cases from "test_cases.txt" and passes the read contents
     * into the SortDemoSwing GUI. It then launches the GUI in the Swing thread.
     * 
     * @param args the String arguments from the command-line; not used here
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Read test cases.
            TestCaseReader reader = new TestCaseReader("test_cases.txt");

            // If the read was successful, launch the app.
            if (reader.isReadSuccessful()) {
                SortDemoSwing app = new SortDemoSwing(reader.getTestCases());
                app.startApp();
            }
        });
    }
}