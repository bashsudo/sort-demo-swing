/*
 * CSC 345 PROJECT
 * Class:           TestCaseReader.java
 * Authors:         Angelina R, Eiza S, Ethan W, Hayden R
 * Description:     Reads a test case file of the provided path and parses
 *                  each test case as a TestCase object in a TreeMap for
 *                  use with the Swing GUI. The file can have multiple
 *                  test cases, where each case is defined with "@" followed
 *                  by the name and a list of parameters that indicates
 *                  the sorting algorithm, input array, and so on.
 * 
 *                  More details on the parameters used is in the README.
 */

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import java.util.TreeMap;

import java.util.Random;
import java.util.Scanner;

/*
 * NOTE: although TestCaseParseException is thrown without an error message and
 * the error message printing is done separately (even though typically this is
 * done together), this was an intentional choice to make it easier to print
 * multiple lines of an error message at once (without it looking messy).
 */

public class TestCaseReader {
    private TreeMap<String, TestCase> testCaseMap;
    private Map<String, Algorithm> algorithmMap;
    private List<String> algorithmNameList;
    private List<String> generateArrayTypeList;
    private String testCaseFilePath;
    private Random random;
    private boolean readSuccessful;

    /**
     * Initializes the TestCaseReader with a list of known sorting algorithms,
     * supported approaches of generating input arrays, and a map pairing the name
     * of the sorting algorithms with their methods in HybridSorts.
     * Also attempts to parse the test case file at the provided path.
     * 
     * @param testCaseFilePath the String path to the test case file
     */
    public TestCaseReader(String testCaseFilePath) {
        generateArrayTypeList = Arrays.asList(
                "random",
                "shuffled",
                "ascending",
                "descending");
        algorithmNameList = Arrays.asList(
                "insertion",
                "merge",
                "heap",
                "quick",
                "bubble",
                "selection",
                "merge-selection",
                "heap-merge",
                "quick-merge",
                "merge-insertion",
                "bubble-merge");
        algorithmMap = Map.ofEntries(
                Map.entry("insertion", HybridSorts::insertionSort),
                Map.entry("merge", HybridSorts::mergeSort),
                Map.entry("heap", HybridSorts::heapSort),
                Map.entry("quick", HybridSorts::quickSort),
                Map.entry("bubble", HybridSorts::bubbleSort),
                Map.entry("selection", HybridSorts::selectionSort),
                Map.entry("merge-selection", HybridSorts::mergeSelectionSort),
                Map.entry("heap-merge", HybridSorts::heapMergeSort),
                Map.entry("quick-merge", HybridSorts::quickMergeSort),
                Map.entry("merge-insertion", HybridSorts::mergeInsertionSort),
                Map.entry("bubble-merge", HybridSorts::bubbleMergeSort));

        this.testCaseFilePath = testCaseFilePath;

        testCaseMap = new TreeMap<String, TestCase>();

        random = new Random();

        readSuccessful = parse();
    }

    /**
     * Returns true if the provided test case file was parsed successfully and false
     * if otherwise.
     * 
     * @return true if the file was successfully parsed
     */
    public boolean isReadSuccessful() {
        return readSuccessful;
    }

    /**
     * Returns the TreeMap of the String names of the test cases and their
     * corresponding TestCase objects that were parsed from the test case file.
     * 
     * @return the TreeMap with the test case names and data
     */
    public TreeMap<String, TestCase> getTestCases() {
        return testCaseMap;
    }

    /**
     * Parses the remaining lines of the contents of the test case after the first
     * line with the name of the test case.
     * Also returns the String name of the next test case so that the main parsing
     * method can pass it to another call of parseTestCase().
     * 
     * @param fileReader   the Scanner of the test case file
     * @param testCaseName the name of the current test case being parsed
     * @return the name of the next test case to be parsed (or null if there is
     *         nothing left)
     * @throws TestCaseParseException if the parameters of the test case were
     *                                improperly set
     */
    private String parseTestCase(Scanner fileReader, String testCaseName) throws TestCaseParseException {
        int[] inputArray = null;
        String generateArrayType = null;
        Integer generateSize = null;

        // The use of two booleans instead of one is so that if they are both false,
        // then nothing was set (which is not indicated by a boolean on its own).
        boolean expectGivenArray = false;
        boolean expectGenerateArray = false;

        boolean visualEnabled = false;
        boolean plotEnabled = false;

        String sortAlgorithmName = null;

        Integer randomGenLow = null;
        Integer randomGenHigh = null;

        boolean expectPlotBounds = false;
        Long plotLowerBoundX = null;
        Long plotUpperBoundX = null;
        Long plotLowerBoundY = null;
        Long plotUpperBoundY = null;

        String line = null;

        while (fileReader.hasNext()) {
            line = scannerNextUsableLine(fileReader);
            // Exit if the current line is blank (nothing left to read) or it has the name
            // of the next test case.
            if (line == null || line.substring(0, 1).equals("@")) {
                break;
            }

            String[] split = line.split(":");

            if (split.length <= 1) {
                System.out.printf("ERROR: either field or value is empty.\n");
                throw new TestCaseParseException();
            }

            String fieldString = split[0];
            String valueString = split[1];

            Boolean parsedBoolean;

            switch (fieldString) {
                case "sortMethod":
                    sortAlgorithmName = valueString;
                    if (!algorithmNameList.contains(sortAlgorithmName)) {
                        System.out.printf(
                                "ERROR: the sorting algorithm \"%s\" does not exist.\n",
                                sortAlgorithmName);
                        throw new TestCaseParseException();
                    }
                    break;
                case "isArrayGiven":
                    parsedBoolean = parseStringToBoolean(valueString);
                    if (parsedBoolean == null) {
                        throw new TestCaseParseException();
                    }

                    if (parsedBoolean) {
                        expectGivenArray = true;
                        expectGenerateArray = false;
                    } else {
                        expectGivenArray = false;
                        expectGenerateArray = true;
                    }
                    break;
                case "givenArray":
                    if (!expectGenerateArray & expectGivenArray) {
                        inputArray = parseStringToIntArray(valueString);
                        if (inputArray == null) {
                            throw new TestCaseParseException();
                        }
                    } else {
                        System.out.printf(
                                "ERROR: givenArray was set when isArrayGiven was set to false.\n");
                        throw new TestCaseParseException();
                    }
                    break;
                case "generateArraySize":
                    if (expectGenerateArray & !expectGivenArray) {
                        generateSize = parseStringToInteger(valueString);
                        if (generateSize == null) {
                            throw new TestCaseParseException();
                        }
                        if (generateSize < 1) {
                            System.out.printf("ERROR: generateArraySize is less than 1 (generateArraySize = %d).",
                                    generateSize);
                            throw new TestCaseParseException();
                        }

                    } else {
                        System.out.printf(
                                "ERROR: generateArraySize was set when isArrayGiven was set to true.\n");
                        System.out.printf(
                                "(Cannot have parameters for generating an array if it is expected to be given a pre-defined array.)\n");
                        throw new TestCaseParseException();
                    }
                    break;
                case "generateArrayType":
                    if (expectGenerateArray & !expectGivenArray) {
                        generateArrayType = valueString;
                        if (!generateArrayTypeList.contains(generateArrayType)) {
                            System.out.printf(
                                    "ERROR: the array generation type \"%s\" does not exist.\n",
                                    generateArrayType);
                            throw new TestCaseParseException();
                        }
                    } else {
                        System.out.printf(
                                "ERROR: generateArrayType was set when isArrayGiven was set to true.\n");
                        System.out.printf(
                                "(Cannot have parameters for generating an array if it is expected to be given a pre-defined array.)\n");
                        throw new TestCaseParseException();
                    }
                    break;
                case "generateArrayRandomLow":
                    if (expectGenerateArray & !expectGivenArray) {
                        if (generateArrayType != null && generateArrayType.equals("random")) {
                            randomGenLow = parseStringToInteger(valueString);
                            if (randomGenLow == null) {
                                throw new TestCaseParseException();
                            }
                        } else {
                            System.out.printf(
                                    "ERROR: generateArrayRandomLow was set when the generateArrayType was not set to \"random\".\n");
                            throw new TestCaseParseException();
                        }
                    } else {
                        System.out.printf(
                                "ERROR: generateArrayRandomLow was set when isArrayGiven was set to true.\n");
                        System.out.printf(
                                "(Cannot have parameters for generating an array if it is expected to be given a pre-defined array.)\n");
                        throw new TestCaseParseException();
                    }
                    break;
                case "generateArrayRandomHigh":
                    if (expectGenerateArray & !expectGivenArray) {
                        if (generateArrayType != null && generateArrayType.equals("random")) {
                            randomGenHigh = parseStringToInteger(valueString);
                            if (randomGenHigh == null) {
                                throw new TestCaseParseException();
                            }
                        } else {
                            System.out.printf(
                                    "ERROR: generateArrayRandomHigh was set when the generateArrayType was not set to \"random\".\n");
                            throw new TestCaseParseException();
                        }
                    } else {
                        System.out.printf(
                                "ERROR: generateArrayRandomHigh was set when isArrayGiven was set to true.\n");
                        System.out.printf(
                                "(Cannot have parameters for generating an array if it is expected to be given a pre-defined array.)\n");
                        throw new TestCaseParseException();
                    }
                    break;
                case "visualEnabled":
                    parsedBoolean = parseStringToBoolean(valueString);
                    if (parsedBoolean == null) {
                        throw new TestCaseParseException();
                    }
                    visualEnabled = parsedBoolean;
                    break;
                case "plotEnabled":
                    parsedBoolean = parseStringToBoolean(valueString);
                    if (parsedBoolean == null) {
                        throw new TestCaseParseException();
                    }
                    plotEnabled = parsedBoolean;
                    break;
                case "isPlotBoundsGiven":
                    parsedBoolean = parseStringToBoolean(valueString);
                    if (parsedBoolean == null) {
                        throw new TestCaseParseException();
                    }
                    expectPlotBounds = parsedBoolean;
                    break;
                case "plotLowerBoundX":
                    plotLowerBoundX = parseStringToLong(valueString);
                    if (plotLowerBoundX == null) {
                        throw new TestCaseParseException();
                    }
                    break;
                case "plotUpperBoundX":
                    plotUpperBoundX = parseStringToLong(valueString);
                    if (plotUpperBoundX == null) {
                        throw new TestCaseParseException();
                    }
                    break;
                case "plotLowerBoundY":
                    plotLowerBoundY = parseStringToLong(valueString);
                    if (plotLowerBoundY == null) {
                        throw new TestCaseParseException();
                    }
                    break;
                case "plotUpperBoundY":
                    plotUpperBoundY = parseStringToLong(valueString);
                    if (plotUpperBoundY == null) {
                        throw new TestCaseParseException();
                    }
                    break;
                default:
                    System.out.printf("ERROR: unrecognized field \"%s\".\n", fieldString);
                    throw new TestCaseParseException();
            }
        }

        /*
         * CHECK FOR ADDITIONAL ERRORS AFTER READING THE PARAMETERS:
         */

        if (!visualEnabled && !plotEnabled) {
            System.out.printf(
                    "ERROR: neither visualEnabled nor plotEnabled was set to true (at least one of them, or both, needs to be enabled).\n");
            throw new TestCaseParseException();
        }

        if (expectPlotBounds && !plotEnabled) {
            System.out.printf(
                    "ERROR: expectPlotBounds cannot be true if plotEnabled was not set to true (bounds cannot be given when plotting is disabled).\n");
            throw new TestCaseParseException();
        }

        if (expectPlotBounds && (plotLowerBoundX == null || plotUpperBoundX == null || plotLowerBoundY == null
                || plotUpperBoundY == null)) {
            System.out.printf(
                    "ERROR: expectPlotBounds was set to true but one or more of the bounds is missing (one/more of plotLowerBoundX, plotUpperBoundX, plotLowerBoundY, plotUpperBoundY).\n");
            throw new TestCaseParseException();
        }

        if (!expectPlotBounds && (plotLowerBoundX != null || plotUpperBoundX != null || plotLowerBoundY != null
                || plotUpperBoundY != null)) {
            System.out.printf(
                    "ERROR: expectPlotBounds was not set to true but one or more of the bounds was given anyway (one/more of plotLowerBoundX, plotUpperBoundX, plotLowerBoundY, plotUpperBoundY).\n");
            throw new TestCaseParseException();
        }

        if (expectPlotBounds && plotLowerBoundX >= plotUpperBoundX) {
            System.out.printf(
                    "ERROR: plotLowerBoundX cannot be greater than or equal to plotUpperBoundX (plotLowerBoundX = %d, plotUpperBoundX = %d).\n",
                    plotLowerBoundX, plotUpperBoundX);
            throw new TestCaseParseException();
        }

        if (expectPlotBounds && plotLowerBoundY >= plotUpperBoundY) {
            System.out.printf(
                    "ERROR: plotLowerBoundY cannot be greater than or equal to plotUpperBoundY (plotLowerBoundY = %d, plotUpperBoundY = %d).\n",
                    plotLowerBoundY, plotUpperBoundY);
            throw new TestCaseParseException();
        }

        if (sortAlgorithmName == null) {
            System.out.printf(
                    "ERROR: sortMethod was not set.\n");
            throw new TestCaseParseException();
        }

        if (!expectGenerateArray && !expectGivenArray) {
            System.out.printf("ERROR: isArrayGiven was not set.");
            throw new TestCaseParseException();
        }

        if (expectGenerateArray && !expectGivenArray) {
            if (generateSize == null) {
                System.out.printf(
                        "ERROR: isArrayGiven was set to false so that an array can be generated, but generateSize was not set.\n");
                throw new TestCaseParseException();
            }

            if (generateArrayType == null) {
                System.out.printf(
                        "ERROR: isArrayGiven was set to false so that an array can be generated, but generateArrayType was not set.\n");
                throw new TestCaseParseException();
            }

            if (generateArrayType != null && generateArrayType.equals("random")) {
                if (randomGenLow == null || randomGenHigh == null) {
                    System.out.printf(
                            "ERROR: generateArrayType was set to \"random\" but either generateArrayRandomLow or generateArrayRandomHigh is missing.\n");
                    throw new TestCaseParseException();
                }
                if (randomGenLow >= randomGenHigh) {
                    System.out.printf(
                            "ERROR: generateArrayRandomLow cannot be greater than or equal to generateArrayRandomHigh (generateArrayRandomLow = %d, generateArrayRandomHigh = %d).\n",
                            randomGenLow, randomGenHigh);
                    throw new TestCaseParseException();
                }
            }

            if (generateArrayType != null) {
                switch (generateArrayType) {
                    case "random":
                        inputArray = generateRandomArray(generateSize, randomGenLow, randomGenHigh);
                        break;
                    case "shuffled":
                        inputArray = generateShuffledArray(generateSize);
                        break;
                    case "ascending":
                        inputArray = generateAscendingArray(generateSize);
                        break;
                    case "descending":
                        inputArray = generateDescendingArray(generateSize);
                        break;
                }
            }
        } else if (!expectGenerateArray && expectGivenArray) {
            if (inputArray == null) {
                System.out.printf(
                        "ERROR: isArrayGiven was set to true yet no array was provided with givenArray.\n");
                throw new TestCaseParseException();
            }
        }

        // Create the new Test Case object with everything parsed...
        TestCase testCase = new TestCase(testCaseName, sortAlgorithmName, algorithmMap.get(sortAlgorithmName),
                inputArray, expectGivenArray, generateArrayType, randomGenLow, randomGenHigh, visualEnabled,
                plotEnabled, expectPlotBounds, plotLowerBoundX, plotUpperBoundX, plotLowerBoundY, plotUpperBoundY);
        testCaseMap.put(testCaseName, testCase);

        // If the current line has another test case, then return its name (without the
        // "@")
        if (line != null && line.substring(0, 1).equals("@")) {
            return line.substring(1);
        }
        return null;
    }

    /**
     * Parses the test case file and returns true if it was successful.
     * All empty lines (or lines with whitespace) and comments (text after a "#"
     * either on a whole, separate line or on a line with content).
     * A test case is defined with an "@" as the first character followed with its
     * name and parameters on separate lines below it.
     * 
     * @return true if the parse was successful
     */
    private boolean parse() {
        try {
            Scanner fileReader = new Scanner(new File(testCaseFilePath));
            testCaseMap = new TreeMap<>();

            while (fileReader.hasNext()) {
                String line = scannerNextUsableLine(fileReader);

                // If the entire file was not just whitespace and a test case definition was
                // able to be found:
                if (line != null && line.substring(0, 1).equals("@")) {
                    String testCaseName = line.substring(1);

                    // While there is a test case name to be parsed (for all subsequent updates to
                    // the test case name):
                    while (testCaseName != null) {
                        // If the test case name is not empty:
                        if (testCaseName.length() > 1) {
                            try {
                                testCaseName = parseTestCase(fileReader, testCaseName);
                            } catch (TestCaseParseException exception) {
                                // If a parsing error occurred:
                                System.out.printf("\nAn error occurred when processing the test case \"%s\".\n",
                                        testCaseName);
                                fileReader.close();
                                return false;
                            }
                        } else {
                            // If there was a test case definition without a name:
                            System.out
                                    .printf("ERROR: a test case was defined without a name (only found the \"@\").\n");
                            fileReader.close();
                            return false;
                        }
                    }
                }
            }
            fileReader.close();
            return true;

        } catch (FileNotFoundException exception) {
            System.out.printf("ERROR: the test case name file \"%s\" could not be found.\n\n", testCaseFilePath);
            return false;
        }
    }

    /**
     * Returns a copy of the provided String with all of its whitespace removed.
     * 
     * @param value the String that may have whitespace to be removed
     * @return a copy of the String without whitespace
     */
    private static String removeWhiteSpace(String value) {
        return value.replaceAll("\\s+", "");
    }

    /**
     * Iterates through the lines of the scanner and returns the first line that has
     * content (is not empty, does not only have whitespace, or is entirely
     * dedicated to a comment).
     * The line includes the content before any in-line comment that may be in it.
     * 
     * @param scanner the Scanner with the lines to iterate through
     * @return the first String with content (not including commented parts)
     */
    private static String scannerNextUsableLine(Scanner scanner) {
        String line;

        while (scanner.hasNextLine()) {
            line = removeWhiteSpace(scanner.nextLine());
            // If the line, after white space removal, is not empty or is not a block
            // comment.
            if (!line.isEmpty() && !line.substring(0, 1).equals("#")) {
                // Return everything before the in-line comment.
                return line.split("#")[0];
            }
        }

        return null;
    }

    /**
     * Returns an int array that is parsed from the given String value.
     * If the array is empty (less than or equal to 3 characters in length), the
     * brackets are not square brackets "[" and "]", or if any of the individual
     * elements cannot be parsed as an integer, then an error message is printed and
     * null is returned.
     * 
     * @param value the String with contents of an array to be parsed
     * @return the int array that was parsed from the String
     */
    private int[] parseStringToIntArray(String value) {
        // If the array is not empty:
        if (value.length() >= 3) {
            String bracketLeft = value.substring(0, 1);
            String bracketRight = value.substring(value.length() - 1);

            // If the array has the proper brackets:
            if (bracketLeft.equals("[") && bracketRight.equals("]")) {
                String body = value.substring(1, value.length() - 1);
                String[] elementStrings = body.split(",");
                int[] parsedArray = new int[elementStrings.length];

                // Iterate through each comma-separated element:
                for (int i = 0; i < elementStrings.length; i++) {
                    String element = elementStrings[i];

                    // If the element can be parsed as an Integer:
                    try {
                        parsedArray[i] = Integer.parseInt(element);
                    } catch (NumberFormatException exception) {
                        System.out.printf(
                                "ERROR: element \"%s\" in array string \"%s\" cannot be parsed into an integer.\n",
                                element, value);
                        return null;
                    }
                }

                return parsedArray;
            } else {
                System.out.printf("ERROR: either \"[\" or \"]\" missing in array string \"%s\".\n", value);
                return null;
            }
        } else {
            System.out.printf("ERROR: the array string \"%s\" has too few characters (less than 3).\n", value);
            return null;
        }
    }

    /**
     * Returns an Integer that is parsed from the provided String and null if it
     * could not be parsed.
     * Assumes that the provided String already has all of its white space removed
     * (this is for performance reasons because re-filtering a String with its
     * whitespace already removed by the parser is redundant).
     * 
     * @param value the String value that is to be parsed
     * @return the parsed Integer and null if it could not be parsed
     */
    private Integer parseStringToInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            System.out.printf("ERROR: string \"%s\" cannot be parsed into an integer.\n", value);
            return null;
        }
    }

    /**
     * Returns an Long that is parsed from the provided String and null if it
     * could not be parsed.
     * Assumes that the provided String already has all of its white space removed
     * (this is for performance reasons because re-filtering a String with its
     * whitespace already removed by the parser is redundant).
     * 
     * @param value the String value that is to be parsed
     * @return the parsed Long and null if it could not be parsed
     */
    private Long parseStringToLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            System.out.printf("ERROR: string \"%s\" cannot be parsed into a long.\n", value);
            return null;
        }
    }

    /**
     * Returns a Boolean that is parsed from the provided String and null if it
     * could not be parsed.
     * Assumes that the provided String already has all of its white space removed
     * (this is for performance reasons because re-filtering a String with its
     * whitespace already removed by the parser is redundant).
     * 
     * @param value the String value that is to be parsed
     * @return the parsed Boolean and null if it could not be parsed
     */
    private Boolean parseStringToBoolean(String value) {
        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            System.out.printf("ERROR: string \"%s\" cannot be parsed into a boolean.\n", value);
            return null;
        }
    }

    /**
     * Returns a random ints with values ranging from low to high inclusive.
     * 
     * @param low  the lowest random value possible, inclusive
     * @param high the highest random value possible, inclusive
     * @return the random int
     */
    private int randomInt(int low, int high) {
        return (int) (random.nextInt(high - low + 1) + low);
    }

    /**
     * Returns an int array with a provided size and range of the randomized
     * elements.
     * 
     * @param size the size of the int array
     * @param low  the smallest random element possible, inclusive
     * @param high the largest random element possible, inclusive
     * @return the int array of randomized elements
     */
    private int[] generateRandomArray(int size, int low, int high) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = randomInt(low, high);
        }
        return array;
    }

    /**
     * Returns an int array with ascending values ranging from 1 to the provided
     * size of the array.
     * 
     * @param size the size of the int array
     * @return the int array of ascending elements
     */
    private int[] generateAscendingArray(int size) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = i + 1;
        }

        return array;
    }

    /**
     * Returns an int array with descending values ranging from 1 to the provided
     * size of the array.
     * 
     * @param size the size of the int array
     * @return the int array of descending elements
     */
    private int[] generateDescendingArray(int size) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = size - i;
        }

        return array;
    }

    /**
     * Returns an int array with values ranging from 1 to the size of the array that
     * are shuffled and randomized in order.
     * 
     * @param size the size of the int array
     * @return the int array of shuffled elements
     */
    private int[] generateShuffledArray(int size) {
        List<Integer> list = new ArrayList<>(size);

        for (int i = 1; i <= size; i++) {
            list.add(i);
        }

        Collections.shuffle(list);

        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }

        return array;
    }

}
