/*
 * CSC 345 PROJECT
 * Class:           TestCaseParseException.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A collection of parameters for a test case for the Swing GUI.
 */

public class TestCase {
    private final String name, algorithmName, arrayGenerateType;
    private final Algorithm algorithm;
    private final int[] input;
    private final boolean isArrayGiven, isVisualEnabled, isPlotEnabled, isPlotBoundsGiven;
    private final Integer randomGenLow, randomGenHigh;
    private final Long plotLowerBoundX, plotUpperBoundX, plotLowerBoundY, plotUpperBoundY;

    /**
     * Initializes the TestCase object with the provided information.
     * The wrapper-class versions for Integer and Long were used so that "null"
     * could be provided if they were not explicitly set by the test case.
     * 
     * @param name              the name of the test case
     * @param algorithmName     the name of the sorting algorithm
     * @param algorithm         the Algorithm containing a reference to the
     *                          algorithm in HybridSorts
     * @param input             the int array input of the test case
     * @param isArrayGiven      true if a hard-coded array was provided
     * @param arrayGenerateType the String label for how the array was generated
     *                          (random, ascending, descending, shuffled)
     * @param randomGenLow      the smallest number that could be generated if the
     *                          array was randomized
     * @param randomGenHigh     the largest number that could be generated if the
     *                          array was randomized
     * @param isVisualEnabled   true if the Swing GUI should allow this to be
     *                          visualized
     * @param isPlotEnabled     true if the Swing GUI should allow this to be
     *                          plotted
     * @param isPlotBoundsGiven true if plot bounds were manually set
     * @param plotLowerBoundX   the manually-set lower bound of the x-axis for the
     *                          scatter plot
     * @param plotUpperBoundX   the manually-set upper bound of the x-axis for the
     *                          scatter plot
     * @param plotLowerBoundY   the manually-set lower bound of the y-axis for the
     *                          scatter plot
     * @param plotUpperBoundY   the manually-set upper bound of the y-axis for the
     *                          scatter plot
     */
    public TestCase(String name, String algorithmName, Algorithm algorithm, int[] input, boolean isArrayGiven,
            String arrayGenerateType, Integer randomGenLow, Integer randomGenHigh, boolean isVisualEnabled,
            boolean isPlotEnabled, boolean isPlotBoundsGiven, Long plotLowerBoundX, Long plotUpperBoundX,
            Long plotLowerBoundY, Long plotUpperBoundY) {
        this.name = name;

        // Info about the algorithm.
        this.algorithm = algorithm;
        this.algorithmName = algorithmName;

        // Info if the input was generated or not and how it was generated.
        this.input = input;
        this.isArrayGiven = isArrayGiven;
        this.arrayGenerateType = arrayGenerateType;
        this.randomGenLow = randomGenLow;
        this.randomGenHigh = randomGenHigh;

        // Visualization or plotting enabled.
        this.isVisualEnabled = isVisualEnabled;
        this.isPlotEnabled = isPlotEnabled;

        // Bounds of the plot.
        this.isPlotBoundsGiven = isPlotBoundsGiven;
        this.plotLowerBoundX = plotLowerBoundX;
        this.plotUpperBoundX = plotUpperBoundX;
        this.plotLowerBoundY = plotLowerBoundY;
        this.plotUpperBoundY = plotUpperBoundY;
    }

    public String getName() {
        return name;
    }

    /*
     * SORTING ALGORITHM
     */

    public String getAlgorithmName() {
        return algorithmName;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /*
     * INPUT INFO
     */

    public int[] getInput() {
        return input;
    }

    public int getInputSize() {
        return input.length;
    }

    /*
     * GENERATED INPUT
     */

    public boolean getIsArrayGiven() {
        return isArrayGiven;
    }

    public String getArrayGenerateType() {
        return arrayGenerateType;
    }

    public Integer getRandomLowerBound() {
        return randomGenLow;
    }

    public Integer getRandomUpperBound() {
        return randomGenHigh;
    }

    /*
     * VISUALIZATION VS. PLOTTING
     */

    public boolean getIsVisualEnabled() {
        return isVisualEnabled;
    }

    public boolean getIsPlotEnabled() {
        return isPlotEnabled;
    }

    /*
     * PLOT BOUNDS
     */

    public boolean getIsPlotBoundsGiven() {
        return isPlotBoundsGiven;
    }

    public Long getPlotLowerBoundX() {
        return plotLowerBoundX;
    }

    public Long getPlotUpperBoundX() {
        return plotUpperBoundX;
    }

    public Long getPlotLowerBoundY() {
        return plotLowerBoundY;
    }

    public Long getPlotUpperBoundY() {
        return plotUpperBoundY;
    }

}
