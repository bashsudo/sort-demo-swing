/*
 * CSC 345 PROJECT
 * Class:           SortDemoSwing.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     The Swing GUI code of the visualization app. Accepting a TreeMap
 *                  of TestCase objects, it features the following screens: a Selection
 *                  Screen where the user can choose among the test cases and view their
 *                  properties; a Visualization Screen where the input array for a sorting
 *                  algorithm is shown as a bar graph that is rearranged as it is sorted by
 *                  the algorithm; and a Plotting Screen that shows how the total access
 *                  count for a sorting algorithm cna change as the size of the input array
 *                  (N) increases.
 */

import java.util.*;
import java.util.List; // Avoid ambiguous "List" name.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SortDemoSwing {

    // === === === === === === === === ===
    // GUI COMPONENTS
    // === === === === === === === === ===

    // Other misc. GUI settings.
    private int windowSizeX, windowSizeY;
    private Map<String, Integer> visualUpdateDelayPresets;
    private String textProgramIntro;

    // The main JFrame to display.
    private JFrame mainFrame;

    // The custom JPanels to display.
    private VisualGraphPanel visualGraphPanel;
    private ScatterPlotPanel scatterPlotPanel;
    private LoadingBarPanel loadingBarPanel;

    // JButtons for the user to interact with.
    private JButton swingButtonSwitchVisual, swingButtonReturnSelection, swingButtonRestartVisual,
            swingButtonSwitchPlot;

    // JComboBoxes so the user can select from multiple test cases and speeds.
    private JComboBox swingComboTestCase, swingComboVisualUpdateDelay;

    // The icons and JLabels for the "progression bar" indicating the current screen
    // the program has selected.
    private ImageIcon swingIconProgArrowLeft, swingIconProgArrowRight;
    private JLabel swingLabelProgSelection, swingLabelProgVisual, swingLabelProgPlot;
    private JLabel swingLabelProgArrowLeft, swingLabelProgArrowRight;

    // Additional JLabels above the drop down boxes to indicate their purpose.
    private JLabel swingLabelControlGuideTestCase, swingLabelControlGuideDelay;

    // The background, colored JLabels.
    private JLabel swingLabelBackgroundProgArea, swingLabelBackgroundMainArea, swingLabelBackgroundInfoArea,
            swingLabelBackgroundInteractArea;

    // The JLabels with paragraphs of program and test case information.
    private JLabel swingLabelProgramInto, swingLabelTestCaseInfo;

    // === === === === === === === === ===
    // STYLE AND THEME
    // === === === === === === === === ===

    // Swing Insets to change the distance of components from edges of the screen
    // and other components.
    private Insets swingInsetsBackground, swingInsetsInner, swingInsetsZero;

    // Color pallettes to standardize the shades of colors that common components
    // use.
    private Color[] colorPaletteRed, colorPaletteOrange, colorPaletteBlue;

    // Color of the entire window/frame.
    private Color colorWindowBackground;

    // Colors used by the VisualGraphPanel.
    private Color colorVisualBarRegular, colorVisualBarSet, colorVisualBarGet, colorVisualMessageText,
            colorVisualMessageBackground;

    // Colors used by the ScatterPlotPanel.
    private Color colorPlotDots;

    // Colors used by the LoadingBarPanel.
    private Color colorLoadingBackground, colorLoadingBarBackground, colorLoadingBarFill, colorLoadingMessageText;

    // The common font name and various panel-specific font sizes.
    private String fontNameEverything;
    private int fontSizePlotHeaders, fontSizePlotNumbers, fontSizeVisualMessage, fontSizeLoadingBarMessage;

    // === === === === === === === === ===
    // STATUS OF APP AND OTHER TRACKING VARS
    // === === === === === === === === ===

    // Components on each screen (so they can be hidden and unhidden).
    private JComponent[] swingSelectionScreenComponents, swingVisualScreenComponents, swingPlotScreenComponents;
    private JLabel[] swingSelectionScreenProgLabels;

    // Current screen and other specific states of the app.
    private int screenCurrentID; // 1 = Selection, 2 = Visualization, 3 = Scatter Plot
    private boolean statusAppSetup, statusVisualRunning, statusPlotRunning, statusTestCasesReceived;

    // Data for plotting the graph.
    private List<ScatterPoint> plotPointList;
    private int plotSampleAmountCap, plotSampleAmount, plotSampleCurrentIndex, plotValNCurrent;
    private double plotValNJump;

    // === === === === === === === === ===
    // TEST CASE INFO
    // === === === === === === === === ===

    private AnalyzedArrayGroup testCaseArrayGroup;
    private TreeMap<String, TestCase> testCaseMap;
    private String testCaseCurrentName;
    private TestCase testCaseCurrent;
    private int testCaseUpdateDelayMs;

    /**
     * Initializes the SortDemoSwing object with given test cases without actually
     * starting Swing and the GUI.
     * If testCases is null or has no test cases (size is zero), then the object
     * will not be properly initialized and an error message is printed; the app
     * will not be able to start.
     * 
     * @param testCases the TreeMap pairing up test case names and their objects
     *                  with further information.
     */
    public SortDemoSwing(TreeMap<String, TestCase> testCases) {
        if (testCases != null && testCases.size() > 0) {
            testCaseMap = testCases;
            statusTestCasesReceived = true;
        } else {
            statusTestCasesReceived = false;
            System.out.printf("ERROR (SortDemoSwing): the collection of test cases received is empty.\n");
            return;
        }
    }

    /**
     * Launches Swing and the App by initializing the components and switching to
     * the selection screen.
     * If the test cases were not properly received upon construction, then the app
     * will not start and print an error message.
     */
    public void startApp() {
        if (statusTestCasesReceived) {
            setupAppComponents();
            screenSwitchToSelection();
        } else {
            System.out
                    .printf("ERROR (SortDemoSwing): cannot start GUI because the collection is test cases is empty.\n");
        }
    }

    /**
     * Sets the GUI-related defaults of the app and creates the components for all
     * three screens of the app: the Selection Screen, Visualization Screen, and
     * Plotting Screen.
     * If the components have already been set up or the test cases were not
     * properly received, then nothing happens.
     */
    private void setupAppComponents() {
        if (statusAppSetup || !statusTestCasesReceived) {
            return;
        }

        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // PROGRAMMER-ADJUSTABLE COSMETIC SETTINGS
        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

        // === === === === === === === === ===
        // DEFAULT WINDOW SIZE
        // === === === === === === === === ===

        windowSizeX = 900;
        windowSizeY = 900;

        // === === === === === === === === ===
        // FONT NAMES AND FONT SIZES
        // === === === === === === === === ===

        fontNameEverything = "Arial";
        fontSizePlotHeaders = 18;
        fontSizePlotNumbers = 11;
        fontSizeVisualMessage = 12;
        fontSizeLoadingBarMessage = 14;

        // === === === === === === === === ===
        // COLOR PALETTES
        // === === === === === === === === ===

        colorPaletteRed = new Color[] {
                new Color(255, 173, 173),
                new Color(253, 75, 75),
                new Color(220, 17, 17),
                new Color(147, 17, 17),
        };
        colorPaletteBlue = new Color[] {
                new Color(131, 196, 255),
                new Color(86, 174, 255),
                new Color(0, 131, 255),
                new Color(15, 37, 152)
        };
        colorPaletteOrange = new Color[] {
                new Color(255, 209, 166),
                new Color(255, 177, 103),
                new Color(255, 142, 35),
                new Color(163, 88, 18)
        };

        // === === === === === === === === ===
        // SPECIFIC COLORS
        // === === === === === === === === ===

        // >>> COLORS: VISUALIZATION BAR GRAPH
        colorVisualBarRegular = new Color(5, 5, 25);
        colorVisualBarSet = new Color(230, 25, 25);
        colorVisualBarGet = new Color(25, 230, 25);
        colorVisualMessageBackground = new Color(50, 50, 50);
        colorVisualMessageText = new Color(200, 200, 200);

        // >>> COLORS: SCATTER PLOT
        colorPlotDots = new Color(230, 25, 25);

        // >>> COLORS: LOADING BAR FOR SCATTER PLOT
        colorLoadingBarBackground = new Color(100, 100, 100);
        colorLoadingBarFill = new Color(255, 140, 140);
        colorLoadingBackground = new Color(50, 50, 50);
        colorLoadingMessageText = new Color(200, 200, 200);

        // >>> COLORS: OTHER MISC COLORS
        colorWindowBackground = new Color(31, 55, 83);

        // === === === === === === === === ===
        // INSETS (PIXELS BETWEEN EDGES OF COMPONENTS)
        // === === === === === === === === ===
        swingInsetsZero = new Insets(0, 0, 0, 0);
        swingInsetsBackground = new Insets(10, 10, 10, 10);
        swingInsetsInner = new Insets(20, 20, 20, 20);

        // === === === === === === === === ===
        // COMPONENT DATA AND VALUES
        // === === === === === === === === ===

        visualUpdateDelayPresets = Map.of(
                "Very Fast", 1,
                "Fast", 3,
                "Medium", 10,
                "Slow", 50,
                "Very Slow", 100);

        textProgramIntro = "<html><body>" +
                "<h1>SortDemoSwing</h1>" +
                "<h2>Sorting Algorithm Visualizer for the CSC 345 Project</h2>" +
                "<h2>By Eiza S, Ethan W, Angelina A, Hayden R</h2>";

        swingIconProgArrowLeft = setupFetchImageIcon("assets/left_arrow_orange1.png");
        swingIconProgArrowRight = setupFetchImageIcon("assets/right_arrow_orange1.png");

        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // MOST SIGNIFICANT SWING COMPONENTS (FRAME, LAYOUT)
        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

        // === === === === === === === === ===
        // WINDOW FRAME
        // === === === === === === === === ===

        mainFrame = new JFrame("SortDemoSwing");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setMinimumSize(new Dimension(windowSizeX, windowSizeY));

        // === === === === === === === === ===
        // CONTENT PANE, LAYOUT, AND CONSTRAINTS
        // === === === === === === === === ===

        Container contentPane = mainFrame.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(colorWindowBackground);
        GridBagConstraints constraints = new GridBagConstraints();

        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // SPECIFIC SWING COMPONENTS
        // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

        // === === === === === === === === ===
        // SCREEN: SELECTING A TEST CASE ("SELECTION")
        // === === === === === === === === ===

        // >>> SELECT FIRST TEST CASE BY DEFAULT
        testCaseSelect(testCaseMap.firstKey());

        // >>> LABELS FOR THE COMBO BOXES
        swingLabelControlGuideTestCase = setupCreateControlsTextLabel(contentPane, constraints, "Test Case:", 2, 3);
        swingLabelControlGuideDelay = setupCreateControlsTextLabel(contentPane, constraints, "Sort Speed:", 3, 3);

        // >>> BUTTON: switch to the visualization screen.
        swingButtonSwitchVisual = setupCreateButton(contentPane, constraints, "Visualize", 4, 4);
        swingButtonSwitchVisual.setEnabled(testCaseCurrent.getIsVisualEnabled());

        // Set up the Action Listener so the button can switch to a screen.
        swingButtonSwitchVisual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!statusVisualRunning && !statusPlotRunning && screenCurrentID == 1) {
                    processVisualStart();
                }
            }
        });

        // >>> BUTTON: switch to the plotting screen.
        swingButtonSwitchPlot = setupCreateButton(contentPane, constraints, "Plot", 0, 4);
        swingButtonSwitchPlot.setEnabled(testCaseCurrent.getIsPlotEnabled());

        // Set up the Action Listener so the button can switch to a screen.
        swingButtonSwitchPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!statusVisualRunning && !statusPlotRunning && screenCurrentID == 1) {
                    processPlotStart();
                }
            }
        });

        // >>> BUTTON: return to the selection screen.
        // NOTE: the "go back" button is NOT visible on the selection screen, but it is
        // used by both the visualization and plotting screens.
        swingButtonReturnSelection = setupCreateButton(contentPane, constraints, "Go Back", 2, 4);

        // Set up the Action Listener so the button can switch to a screen.
        swingButtonReturnSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!statusVisualRunning && !statusPlotRunning && (screenCurrentID == 2 || screenCurrentID == 3)) {
                    screenSwitchToSelection();
                }
            }
        });

        // >>> COMBO BOX: select a test case to visualize or plot.
        // Copy the names of the test cases into the combo box.
        int testCaseOptionsIndex = 0;
        String[] testCaseOptions = new String[testCaseMap.size()];
        for (String testCaseName : testCaseMap.keySet()) {
            testCaseOptions[testCaseOptionsIndex] = testCaseName;
            testCaseOptionsIndex++;
        }

        // Initialize the combo box and add it to the layout.
        swingComboTestCase = new JComboBox<>(testCaseOptions);
        swingComboTestCase.setPreferredSize(new Dimension(300, 30));
        swingComboTestCase.setSelectedItem(0);
        setupAddComboBox(contentPane, constraints, swingComboTestCase, 2, 4);

        // Set up the action listener to update the test case information when a new one
        // is selected.
        swingComboTestCase.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!statusVisualRunning && !statusPlotRunning && screenCurrentID == 1) {
                    JComboBox comboBox = (JComboBox) event.getSource();
                    String testCase = (String) comboBox.getSelectedItem();
                    testCaseSelect(testCase);
                    swingLabelTestCaseInfo.setText(testCaseInfoTextGenerate(testCaseCurrent));

                    swingButtonSwitchVisual.setEnabled(testCaseCurrent.getIsVisualEnabled());
                    swingComboVisualUpdateDelay.setEnabled(testCaseCurrent.getIsVisualEnabled());
                    swingButtonSwitchPlot.setEnabled(testCaseCurrent.getIsPlotEnabled());
                }
            }
        });

        // >>> COMBO BOX: select a preset for the amount of delay between calls to get()
        // and set() by sorting algorithms in the visualization.

        // Initialize the array delay preset names.
        // We are hard-coding these into an array instead of using the existing keys in
        // the hash map to ensure that the combo box will maintain this exact ordering.
        String[] delayOptionsArray = new String[] {
                "Very Fast", "Fast", "Medium", "Slow", "Very Slow"
        };
        // Initialize the combo box and add it to the layout.
        swingComboVisualUpdateDelay = new JComboBox<>(delayOptionsArray);
        swingComboVisualUpdateDelay.setPreferredSize(new Dimension(100, 30));
        swingComboVisualUpdateDelay.setSelectedItem(0);
        swingComboVisualUpdateDelay.setEnabled(testCaseCurrent.getIsVisualEnabled());
        setupAddComboBox(contentPane, constraints, swingComboVisualUpdateDelay, 3, 4);

        // Update the delay in milliseconds for the visualization.
        testCaseUpdateDelayMs = visualUpdateDelayPresets.get(delayOptionsArray[0]);

        // Set up the action listener to update the delay when a new delay preset is
        // selected.
        swingComboVisualUpdateDelay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!statusVisualRunning && !statusPlotRunning && screenCurrentID == 1) {
                    JComboBox comboBox = (JComboBox) event.getSource();
                    String speedSetting = (String) comboBox.getSelectedItem();
                    if (visualUpdateDelayPresets.containsKey(speedSetting)) {
                        testCaseUpdateDelayMs = visualUpdateDelayPresets.get(speedSetting);
                    }
                }
            }
        });

        // >>> TEXT LABELS: show paragraphs with info of the program and test case.
        swingLabelTestCaseInfo = setupCreateBigTextLabel(contentPane, constraints,
                testCaseInfoTextGenerate(testCaseCurrent), 2);
        swingLabelProgramInto = setupCreateBigTextLabel(contentPane, constraints, textProgramIntro, 1);

        // >>> "PROGRESSION" BAR: a graphical representation of the current
        // screen/status of the GUI.

        // Set up the text labels for describing different screens.
        swingLabelProgVisual = setupCreateProgressionText(contentPane, constraints, "Visualization", 4);
        swingLabelProgSelection = setupCreateProgressionText(contentPane, constraints, "Selection", 2);
        swingLabelProgPlot = setupCreateProgressionText(contentPane, constraints, "Plotting", 0);

        // Set up the left and right arrow pointing away from the center.
        swingLabelProgArrowLeft = setupCreateProgressionArrow(contentPane, constraints, swingIconProgArrowLeft, 1);
        swingLabelProgArrowRight = setupCreateProgressionArrow(contentPane, constraints, swingIconProgArrowRight, 3);

        // === === === === === === === === ===
        // SCREEN: BAR GRAPH VISUALIZATION ("VISUAL")
        // === === === === === === === === ===

        // >>> BUTTON: restart the visualization so that it plays again.
        swingButtonRestartVisual = setupCreateButton(contentPane, constraints, "Restart", 4, 4);
        swingButtonRestartVisual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!statusVisualRunning && !statusPlotRunning && screenCurrentID == 2) {
                    processVisualStart();
                }
            }
        });

        // >>> VISUAL PANEL
        visualGraphPanel = new VisualGraphPanel();

        // Immediately disable painting (until the GUI switches to visualization).
        visualGraphPanel.togglePainting(false);

        // Update the colors and font.
        visualGraphPanel.setColors(colorPaletteBlue[2], colorVisualBarRegular, colorVisualBarSet, colorVisualBarGet,
                colorVisualMessageBackground, colorVisualMessageText);
        visualGraphPanel.setFont(fontNameEverything, fontSizeVisualMessage);
        setupAddMainContentPanel(contentPane, constraints, visualGraphPanel);

        // === === === === === === === === ===
        // SCREEN: PLOTTING ACCESS COUNTS ("PLOT")
        // === === === === === === === === ===

        // >>> LOADING BAR PANEL: show the progress for preparing the plot.
        loadingBarPanel = new LoadingBarPanel();

        // Immediately disable painting (until the GUI switches to plotting).
        loadingBarPanel.togglePainting(false);

        // Update the colors and font.
        loadingBarPanel.setColors(colorLoadingBarBackground, colorLoadingBarFill, colorLoadingBackground,
                colorLoadingMessageText);
        loadingBarPanel.setFont(fontNameEverything, fontSizeLoadingBarMessage);
        setupAddMainContentPanel(contentPane, constraints, loadingBarPanel);

        // >>> PLOTTING PANEL: the plot itself with the access counts.
        scatterPlotPanel = new ScatterPlotPanel();

        // Immediately disable painting (until the GUI switches to plotting and the
        // plotting process is finished).
        scatterPlotPanel.togglePainting(false);

        // Update the colors, fonts, and x-axis and y-axis labels.
        scatterPlotPanel.setColors(colorPaletteBlue[1], colorPaletteBlue[3], colorPaletteBlue[3],
                colorPaletteBlue[3], colorPlotDots);
        scatterPlotPanel.setFont(fontNameEverything, fontSizePlotHeaders, fontSizePlotNumbers);
        scatterPlotPanel.setLabelText("Size of N vs. Total Access Count", "Size of N", "Total Access Count");
        setupAddMainContentPanel(contentPane, constraints, scatterPlotPanel);

        // === === === === === === === === ===
        // BACKGROUND LABELS
        // === === === === === === === === ===

        swingLabelBackgroundProgArea = setupCreateBackgroundFixed(contentPane, constraints, colorPaletteOrange, 0, 1,
                50);
        swingLabelBackgroundMainArea = setupCreateBackgroundFill(contentPane, constraints, colorPaletteBlue, 1, 1);
        swingLabelBackgroundInfoArea = setupCreateBackgroundFill(contentPane, constraints, colorPaletteBlue, 2, 1);
        swingLabelBackgroundInteractArea = setupCreateBackgroundFixed(contentPane, constraints, colorPaletteOrange, 3,
                2, 65);

        // === === === === === === === === ===
        // COMPONENT LISTS
        // === === === === === === === === ===

        swingSelectionScreenComponents = new JComponent[] {
                swingButtonSwitchVisual,
                swingButtonSwitchPlot,

                swingLabelControlGuideTestCase,
                swingLabelControlGuideDelay,

                swingComboTestCase,
                swingComboVisualUpdateDelay,

                swingLabelBackgroundInfoArea,

                swingLabelTestCaseInfo,
                swingLabelProgramInto
        };
        swingVisualScreenComponents = new JComponent[] {
                visualGraphPanel,

                swingButtonReturnSelection,

                swingButtonRestartVisual,
        };
        swingPlotScreenComponents = new JComponent[] {
                scatterPlotPanel,

                loadingBarPanel,

                swingButtonReturnSelection,
        };
        swingSelectionScreenProgLabels = new JLabel[] {
                swingLabelProgSelection,
                swingLabelProgVisual,
                swingLabelProgPlot
        };

        // === === === === === === === === ===
        // LAST STEPS BEFORE FINISHING THE SETUP
        // === === === === === === === === ===

        mainFrame.setVisible(true);
        statusAppSetup = true;
    }

    /**
     * Returns an ImageIcon created from an image file at the specified path that is
     * 50 pixels wide.
     * If the file does not exist (or some other issue occurs) then an error message
     * is printed and null is returned.
     * 
     * @param filePath the String path to the image file to make into an icon
     * @return the ImageIcon representing the image file at the path
     */
    private ImageIcon setupFetchImageIcon(String filePath) {
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            // Return an icon that is 50 pixels wide with smooth pixel scaling.
            return new ImageIcon(image.getScaledInstance(-1, 50, Image.SCALE_SMOOTH));

        } catch (IOException exception) {
            System.out.printf("ERROR: could not find the file \"%s\" for setting up the GUI.\n", filePath);
            return null;
        }
    }

    /**
     * Resets the attributes of the given GridBagConstraints back to their defaults.
     * Although the defaults can be obtained by creating a new GridBagConstraints
     * object, that approach is inefficient because this object type was designed to
     * be "reset" in this fashion. We could also set our unique defaults here if we
     * want to, although these are the actual defaults of the object class.
     * 
     * @param constraints the GridBagConstraints to reset
     */
    private void setupResetConstraints(GridBagConstraints constraints) {
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridy = GridBagConstraints.RELATIVE;

        constraints.gridwidth = 1;
        constraints.gridheight = 1;

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;

        constraints.weightx = 0;
        constraints.weighty = 0;

        constraints.ipadx = 0;
        constraints.ipady = 0;

        constraints.insets = swingInsetsZero;
    }

    /**
     * SETUP METHOD: Adds an already-initialized JComboBox to a specific x and y
     * position on the grid of the Container configured with a GridBagLayout.
     * Existing settings of the given GridBagConstraints are NOT used, it is reset
     * and set with new parameters.
     * 
     * @param container   the Container to add the combo box to (expected to be
     *                    JFrame content pane)
     * @param constraints the GridBagConstraints to reset and change
     * @param comboBox    the JComboBox to add
     * @param gridx       the horizontal position of the component in the
     *                    GridBagLayout
     * @param gridy       the vertical position of the component in the
     *                    GridBagLayout
     */
    private void setupAddComboBox(Container container, GridBagConstraints constraints, JComboBox comboBox, int gridx,
            int gridy) {
        setupResetConstraints(constraints);

        constraints.gridx = gridx;
        constraints.gridy = gridy;

        constraints.weightx = 1;
        constraints.insets = swingInsetsBackground;

        container.add(comboBox, constraints);
    }

    /**
     * SETUP METHOD: Creates and returns a new JButton with the given String as its
     * label at a specific x and y position on the grid of the Container configured
     * with a GridBagLayout.
     * Existing settings of the given GridBagConstraints are NOT used, it is reset
     * and set with new parameters.
     * 
     * @param container   the Container to add the JButton to (expected to be
     *                    JFrame content pane)
     * @param constraints the GridBagConstraints to reset and change
     * @param text        the String label of the JButton
     * @param gridx       the horizontal position of the component in the
     *                    GridBagLayout
     * @param gridy       the vertical position of the component in the
     *                    GridBagLayout
     * @return the initialized and arranged JButton object
     */
    private JButton setupCreateButton(Container container, GridBagConstraints constraints, String text, int gridx,
            int gridy) {
        setupResetConstraints(constraints);
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 30));

        constraints.gridx = gridx;
        constraints.gridy = gridy;

        constraints.weightx = 1;
        constraints.insets = swingInsetsBackground;

        container.add(button, constraints);
        return button;
    }

    /**
     * SETUP METHOD: Adds an already-initialized JPanel to the designated "center"
     * of the app where the main content will always be (the bar graph of the
     * visualization, the scatter plot, loading bar, and so on).
     * Existing settings of the given GridBagConstraints are NOT used, it is reset
     * and set with new parameters.
     * 
     * @param container   the Container to add the JPanel to (expected to be
     *                    JFrame content pane)
     * @param constraints the GridBagConstraints to reset and change
     * @param panel       the JPanel to add
     * @param gridx       the horizontal position of the component in the
     *                    GridBagLayout
     * @param gridy       the vertical position of the component in the
     *                    GridBagLayout
     */
    private void setupAddMainContentPanel(Container container, GridBagConstraints constraints, JPanel panel) {
        setupResetConstraints(constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;

        constraints.gridwidth = GridBagConstraints.REMAINDER;

        constraints.fill = GridBagConstraints.BOTH;

        constraints.weightx = 1;
        constraints.weighty = 1;

        container.add(panel, constraints);
    }

    /**
     * SETUP METHOD: Creates and returns a new JLabel with the given String as its
     * label at a specific y position on the grid of the Container configured with a
     * GridBagLayout.
     * This is intended to hold a paragraph of information so the
     * text is left-aligned. Existing settings of the given GridBagConstraints are
     * NOT used, it is reset and set with new parameters.
     * 
     * @param container   the Container to add the JLabel to (expected to be
     *                    JFrame content pane)
     * @param constraints the GridBagConstraints to reset and change
     * @param text        the String label of the JLabel
     * @param gridy       the vertical position of the component in the
     *                    GridBagLayout
     * @return the initialized and arranged JLabel object
     */
    private JLabel setupCreateBigTextLabel(Container container, GridBagConstraints constraints, String text,
            int gridy) {
        setupResetConstraints(constraints);
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(JLabel.LEFT);

        constraints.gridx = 0;
        constraints.gridy = gridy;

        constraints.gridwidth = GridBagConstraints.REMAINDER;

        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.ipadx = 20;
        constraints.ipady = 20;

        constraints.insets = swingInsetsInner;

        container.add(label, constraints);
        return label;
    }

    /**
     * SETUP METHOD: Creates and returns a new JLabel with the given String as its
     * label at a specific x and y position on the grid of the Container configured
     * with a GridBagLayout.
     * This is intended to be a JLabel for labeling specific interactive components
     * for the user, like the JComboBox drop-down menus.
     * 
     * @param container   the Container to add the JLabel to (expected to be
     *                    JFrame content pane)
     * @param constraints the GridBagConstraints to reset and change
     * @param text        the String label of the JLabel
     * @param gridx       the horizontal position of the component in the
     *                    GridBagLayout
     * @param gridy       the vertical position of the component in the
     *                    GridBagLayout
     * @return the initialized and arranged JLabel object
     */
    private JLabel setupCreateControlsTextLabel(Container container, GridBagConstraints constraints, String text,
            int gridx, int gridy) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setPreferredSize(new Dimension(110, 30));

        label.setBackground(colorPaletteOrange[1]);
        label.setBorder(BorderFactory.createLineBorder(colorPaletteOrange[2], 5));
        label.setOpaque(true);

        constraints.gridx = gridx;
        constraints.gridy = gridy;

        constraints.weightx = 1;

        container.add(label, constraints);
        return label;
    }

    /**
     * SETUP METHOD: Creates and returns a new JLabel with the given String as its
     * label at a specific x position on the grid of the Container configured
     * with a GridBagLayout.
     * This is intended to be a text box labeling a screen of the app on the
     * "progression bar" at the very top of the program (at a grid y-value of 0).
     * 
     * @param container   the Container to add the JLabel to (expected to be
     *                    JFrame content pane)
     * @param constraints the GridBagConstraints to reset and change
     * @param text        the String label of the JLabel
     * @param gridx       the horizontal position of the component in the
     *                    GridBagLayout
     * @return the initialized and arranged JLabel object
     */
    private JLabel setupCreateProgressionText(Container container, GridBagConstraints constraints, String text,
            int gridx) {
        setupResetConstraints(constraints);
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setPreferredSize(new Dimension(110, 30));

        label.setBackground(colorPaletteOrange[1]);
        label.setBorder(BorderFactory.createLineBorder(colorPaletteOrange[2], 5));
        label.setOpaque(true);

        constraints.gridx = gridx;
        constraints.gridy = 0;

        constraints.weightx = 1;
        constraints.insets = swingInsetsBackground;

        container.add(label, constraints);
        return label;
    }

    /**
     * SETUP METHOD: Creates and returns a new JLabel with the given ImageIcon as
     * the icon to be displayed at a specific x position on the grid of the
     * Container configured with a GridBagLayout.
     * This is intended to be an arrow pointing to a specific JLabel representing a
     * screen of the app on the "progression bar" at the very top of the program (at
     * a grid y-value of 0).
     * 
     * @param container      the Container to add the JLabel to (expected to be
     *                       JFrame content pane)
     * @param constraints    the GridBagConstraints to reset and change
     * @param arrowImageIcon geIcon representing an arrow image
     * @param gridx          the horizontal position of the component in the
     *                       GridBagLayout
     * @return the initialized and arranged JLabel object
     */
    private JLabel setupCreateProgressionArrow(Container container, GridBagConstraints constraints,
            ImageIcon arrowImageIcon, int gridx) {
        setupResetConstraints(constraints);
        JLabel label;
        if (arrowImageIcon == null) {
            label = new JLabel();
        } else {
            label = new JLabel(arrowImageIcon);
        }

        constraints.gridx = gridx;
        constraints.gridy = 0;

        constraints.weightx = 1;
        constraints.insets = swingInsetsBackground;

        container.add(label, constraints);
        return label;
    }

    /**
     * SETUP METHOD: Creates and returns a new JLabel with the given color palette
     * at a specific y position of the GridBagLayout grid spanning over a given
     * number of cells and pixels.
     * This is intended to be a color-filled background that is behind all of the
     * other components of the GUI that cannot stretch indefinitely (it will try to
     * maintain a fixed size).
     * 
     * @param container    the Container to add the JLabel to (expected to be
     *                     JFrame content pane)
     * @param constraints  the GridBagConstraints to reset and change
     * @param colorPalette the array of Colors the component will be colored with
     * @param gridx        the horizontal position of the component in the
     *                     GridBagLayout
     * @param gridheight   the number of cells of the grid that the component will
     *                     span over
     * @param heightPixels the minimum height of the component in pixels
     * @return the initialized and arranged JLabel object
     */
    private JLabel setupCreateBackgroundFixed(Container container, GridBagConstraints constraints, Color[] colorPalette,
            int gridy,
            int gridheight, int heightPixels) {
        setupResetConstraints(constraints);
        JLabel label = new JLabel();
        label.setBackground(colorPalette[0]);
        label.setBorder(BorderFactory.createLineBorder(colorPalette[2], 5));
        label.setOpaque(true);

        constraints.gridx = 0;
        constraints.gridy = gridy;

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = gridheight;

        constraints.fill = GridBagConstraints.BOTH;

        constraints.ipady = heightPixels;

        constraints.insets = swingInsetsBackground;
        container.add(label, constraints);
        return label;
    }

    /**
     * SETUP METHOD: Creates and returns a new JLabel with the given color palette
     * at a specific y position of the GridBagLayout grid spanning over a given
     * number of cells.
     * This is intended to be a color-filled background that is behind all of the
     * other components of the GUI that can stretch with the window.
     * 
     * @param container    the Container to add the JLabel to (expected to be
     *                     JFrame content pane)
     * @param constraints  the GridBagConstraints to reset and change
     * @param colorPalette the array of Colors the component will be colored with
     * @param gridy        the vertical position of the component in the
     *                     GridBagLayout
     * @param gridheight   the number of cells of the grid that the component will
     *                     span over
     * @return the initialized and arranged JLabel object
     */
    private JLabel setupCreateBackgroundFill(Container container, GridBagConstraints constraints, Color[] colorPalette,
            int gridy,
            int gridheight) {
        setupResetConstraints(constraints);
        JLabel label = new JLabel();
        label.setBackground(colorPalette[0]);
        label.setBorder(BorderFactory.createLineBorder(colorPalette[2], 5));
        label.setOpaque(true);

        constraints.gridx = 0;
        constraints.gridy = gridy;

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = gridheight;

        constraints.fill = GridBagConstraints.BOTH;

        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = swingInsetsBackground;

        container.add(label, constraints);
        return label;
    }

    /**
     * Returns an HTML-formatted String with the information about the provided
     * TestCase object.
     * If the object is null, then return an HTML string without content.
     * 
     * @param testCase the TestCase object with all of the parameters and details of
     *                 the test case
     * @return the HTML-formatted String with the test case info
     */
    private String testCaseInfoTextGenerate(TestCase testCase) {
        if (testCase == null) {
            return "<html><body></body></html>";
        }
        String infoText = "<html><body><h1>Test Case Information:</h1>";

        infoText += "<h2>Overview</h2>";
        infoText += String.format("<p>Name of the Test Case: %s</strong></p>", testCase.getName());
        infoText += String.format("<p>Sorting Algorithm: %s</strong></p>", testCase.getAlgorithmName());
        infoText += String.format("<p>Input Size: %d</strong></p>", testCase.getInputSize());
        infoText += String.format("<p>The sorting visualization is %s for this particular test case.</p>",
                testCase.getIsVisualEnabled() ? "ENABLED" : "DISABLED");
        infoText += String.format("<p>The access count scatter plot is %s for this particular test case.</p>",
                testCase.getIsPlotEnabled() ? "ENABLED" : "DISABLED");

        infoText += "<h2>Contents of Input</h2>";
        if (testCase.getIsArrayGiven()) {
            infoText += "<p>PRE-DEFINED: the elements of the input array are already provided.</p>";
        } else {
            // Add information regarding how the array was defined if no array was given.
            switch (testCase.getArrayGenerateType()) {
                case "random":
                    infoText += "<p>RANDOMIZED: the elements of the input array were randomized within these bounds:</p>";
                    infoText += String.format("<p>Randomization Lower Bound: %d</strong></p>",
                            testCase.getRandomLowerBound());
                    infoText += String.format("<p>Randomization Upper Bound: %d</strong></p>",
                            testCase.getRandomUpperBound());
                    break;
                case "shuffled":
                    infoText += String.format(
                            "<p>SHUFFLED: the elements of the input array are integers 1 to %d in shuffled order.</p>",
                            testCase.getInputSize());
                    break;
                case "ascending":
                    infoText += String.format(
                            "<p>ASCENDING: the elements of the input array are integers 1 to %d that are already sorted in ascending order.</p>",
                            testCase.getInputSize());
                    break;
                case "descending":
                    infoText += String.format(
                            "<p>DESCENDING: the elements of the input array are integers 1 to %d that are sorted in descending (reverse) order.</p>",
                            testCase.getInputSize());
                    break;
                default:
                    infoText += "<p>OTHER: the elements of the input array were generated with some other approach.</p>";
            }
        }

        // Add information regarding the scatter plot and how its bounds were
        // configured.
        if (testCase.getIsPlotEnabled()) {
            infoText += "<h2>Scatter Plot Settings</h2>";

            // If the plot bounds were manually set:
            if (testCase.getIsPlotBoundsGiven()) {
                infoText += "<p>MANUAL SCALE: the plot scales were manually set within these bounds:</p>";
                infoText += String.format("<p>X-AXIS: [%d, %d]</p>", testCase.getPlotLowerBoundX(),
                        testCase.getPlotUpperBoundX());
                infoText += String.format("<p>Y-AXIS: [%d, %d]</p>", testCase.getPlotLowerBoundY(),
                        testCase.getPlotUpperBoundY());
            } else {
                infoText += "<p>AUTO SCALE: The x-axis and y-axis will be scaled automatically.</p>";
            }
        }

        infoText += "</body></html>";
        return infoText;
    }

    /**
     * Hides the JComponent objects in the array componentsHideA and componentsHideB
     * first and later un-hides the objects in componentsShow.
     * If any of the arrays are null, do nohting.
     * 
     * @param componentsHideA the array of JComponent objects to hide first
     * @param componentsHideB the array of JComponent objects to hide second
     * @param componentsShow  the array of JComponent objects to un-hide last
     */
    private void screenAdjustComponentVisibility(JComponent[] componentsHideA, JComponent[] componentsHideB,
            JComponent[] componentsShow) {
        if (componentsHideA == null || componentsHideB == null || componentsShow == null) {
            return;
        }
        for (JComponent component : componentsHideA) {
            component.setVisible(false);
        }
        for (JComponent component : componentsHideB) {
            component.setVisible(false);
        }
        for (JComponent component : componentsShow) {
            component.setVisible(true);
        }
    }

    /**
     * Update the colors of the "progression bar" JLabel objects based on the int ID
     * of the current screen selected.
     * The JLabel representing the currently selected screen will be colored with
     * the red color palette and all other JLabels with the orange color palette.
     * If the ID is not 1, 2, or 3, then nothing happens.
     * 
     * @param currentScreenID the int ID of the current screen
     */
    private void screenSetProgLabelColor(int currentScreenID) {
        if (currentScreenID < 1 || currentScreenID > 3) {
            return;
        }

        Color[] palette;
        JLabel label;

        for (int i = 0; i < 3; i++) {
            if (currentScreenID == i + 1) {
                palette = colorPaletteRed;
            } else {
                palette = colorPaletteOrange;
            }

            label = swingSelectionScreenProgLabels[i];
            label.setBackground(palette[1]);
            label.setBorder(BorderFactory.createLineBorder(palette[2], 5));
            label.setOpaque(true);
        }
    }

    /**
     * Switch to the Selection Screen by:
     * (1) showing the Selection Screen components
     * (2) disabling painting for the custom JPanels
     * (3) enabling the JButtons based on the test case
     * (4) setting the screen ID to 1
     * 
     * If the test cases were not read, then do nothing.
     */
    private void screenSwitchToSelection() {
        if (!statusTestCasesReceived) {
            return;
        }
        setupAppComponents();

        // Show the Selection Screen components.
        screenAdjustComponentVisibility(swingVisualScreenComponents, swingPlotScreenComponents,
                swingSelectionScreenComponents);
        screenCurrentID = 1;

        // Disable painting for all of the custom visualization or plotting panels.
        visualGraphPanel.togglePainting(false);
        scatterPlotPanel.togglePainting(false);
        loadingBarPanel.togglePainting(false);

        // Enable the buttons based on the test case.
        swingButtonSwitchVisual.setEnabled(testCaseCurrent.getIsVisualEnabled());
        swingComboVisualUpdateDelay.setEnabled(testCaseCurrent.getIsVisualEnabled());
        swingButtonSwitchPlot.setEnabled(testCaseCurrent.getIsPlotEnabled());

        screenSetProgLabelColor(screenCurrentID);
    }

    /**
     * Switch to the Visual Screen by:
     * (1) showing the Visual Screen components
     * (2) enabling painting for the visual JPanel and disabling painting for the
     * other custom JPanels
     * (3) disabling the Visual Screen JButtons
     * (4) setting the screen ID to 2
     * 
     * If the test cases were not read, then do nothing.
     */
    private void screenSwitchToVisual() {
        if (!statusTestCasesReceived) {
            return;
        }
        setupAppComponents();

        // Show the Visual Screen components.
        screenAdjustComponentVisibility(swingSelectionScreenComponents, swingPlotScreenComponents,
                swingVisualScreenComponents);
        screenCurrentID = 2;

        // Disable painting for all of the custom panels except the visual panel.
        visualGraphPanel.togglePainting(true);
        scatterPlotPanel.togglePainting(false);
        loadingBarPanel.togglePainting(false);

        swingButtonReturnSelection.setEnabled(false);
        swingButtonRestartVisual.setEnabled(false);

        screenSetProgLabelColor(screenCurrentID);
    }

    /**
     * Switch to the Plot Screen by:
     * (1) showing the Plot Screen components
     * (2) enabling painting for the plotting JPanel and disabling painting for the
     * other custom JPanels
     * (3) disabling the Plotting Screen JButtons
     * (4) setting the screen ID to 3
     * 
     * If the test cases were not read, then do nothing.
     */
    private void screenSwitchToPlot() {
        if (!statusTestCasesReceived) {
            return;
        }
        setupAppComponents();

        // Show the Plot Screen components.
        screenAdjustComponentVisibility(swingSelectionScreenComponents, swingVisualScreenComponents,
                swingPlotScreenComponents);
        screenCurrentID = 3;

        // Disable ALL CUSTOM PANELS, even the plot panel because it is not finished
        // loading yet.
        visualGraphPanel.togglePainting(false);
        scatterPlotPanel.togglePainting(false);
        loadingBarPanel.togglePainting(false);

        swingButtonReturnSelection.setEnabled(false);

        screenSetProgLabelColor(screenCurrentID);
    }

    /**
     * Begin the visualization phase by switching to the visualization screen,
     * setting up an AnalyzedArrayGroup and the VisualGraphPanel, and starting the
     * sorting thread.
     * If the visualization is already running, the plotting is running, or the test
     * cases were not read, then do nothing.
     */
    private void processVisualStart() {
        if (statusVisualRunning || statusPlotRunning || !statusTestCasesReceived) {
            return;
        }
        screenSwitchToVisual();

        // Configure the AnalyzedArrayGroup for the sorting algorithm.
        testCaseArrayGroup = new AnalyzedArrayGroup(this);
        testCaseArrayGroup.toggleSleep(true);
        testCaseArrayGroup.setSleepDelay(testCaseUpdateDelayMs);
        testCaseArrayGroup.toggleReportDelayUpdates(true);
        testCaseArrayGroup.addArray(testCaseCurrent.getInput(), "input", true);

        // Configure the VisualGraphPanel: do not display the sorted results yet and
        // update the input.
        visualGraphPanel.toggleDisplaySorted(false);
        visualGraphPanel.setInput(testCaseArrayGroup);

        statusVisualRunning = true;

        // Start the sorting thread.
        Thread sortThread = new Thread(() -> {
            testCaseCurrent.getAlgorithm().sort(testCaseArrayGroup);
        });
        sortThread.start();
    }

    /**
     * Finish the visualization phase by displaying the sorted status on the
     * VisualGraphPanel and enabling the buttons.
     * If the visualization is not running, the plotting is running, or the test
     * cases were not read, then do nothing.
     */
    private void processVisualFinish() {
        if (!statusVisualRunning || statusPlotRunning || !statusTestCasesReceived) {
            return;
        }

        statusVisualRunning = false;

        // Display the sorted results.
        visualGraphPanel.toggleDisplaySorted(true);
        visualGraphPanel.repaint();

        // Allow the user to interact.
        swingButtonReturnSelection.setEnabled(true);
        swingButtonRestartVisual.setEnabled(true);
    }

    /**
     * Begin the plotting phase by switching to the plotting screen, setting up an
     * AnalyzedArrayGroup and the ScatterPlotPanel and LoadingBarPanel, and starting
     * the sorting thread.
     * If the plotting is already running, the visualization is running, or the test
     * cases were not read, then do nothing.
     */
    private void processPlotStart() {
        if (statusVisualRunning || statusPlotRunning || !statusTestCasesReceived) {
            return;
        }
        screenSwitchToPlot();

        // Set up the AnalyzedArrayGroup to be sorted.
        testCaseArrayGroup = new AnalyzedArrayGroup(this);
        testCaseArrayGroup.toggleSleep(false);
        testCaseArrayGroup.toggleReportDelayUpdates(false);

        // Empty and initialize the list of ScatterPoints to record the access counts.
        plotPointList = new ArrayList<ScatterPoint>();

        // Hard-code the maximum amount of samples/points to 50, although this can be
        // easily changed in the future.
        plotSampleAmountCap = 50;

        // If there are less elements in the input than the maximum amount of samples,
        // then set the sample amount to the element amount.
        plotSampleAmount = Math.min(plotSampleAmountCap, testCaseCurrent.getInputSize());

        if (testCaseCurrent.getInputSize() <= plotSampleAmountCap) {
            // Ensure that the N will increase by 1 when the input size is less than the
            // sample maximum; otherwise the jump may be less than 1 and truncate to an int
            // of 0.
            plotValNJump = 1.0;
        } else {
            plotValNJump = testCaseCurrent.getInputSize()
                    / (double) (plotSampleAmount + 1);
        }

        // Update the current sample and N values for the first (and future) runs of the
        // sorting algorithm.
        plotSampleCurrentIndex = 0;
        plotValNCurrent = (int) plotValNJump;

        // With the current N value set, set the input for the AnalyzedArrayGroup.
        testCaseArrayGroup.addArray(testCaseCurrent.getInput(), 0, plotValNCurrent - 1, "input", true);

        // Configure and display the loading bar panel so the user can see that the plot
        // is loading.
        loadingBarPanel.setTestCaseInfo(testCaseCurrent);
        loadingBarPanel.updateStatus(1, plotSampleAmount, plotValNCurrent);
        loadingBarPanel.togglePainting(true);
        loadingBarPanel.repaint();

        statusPlotRunning = true;

        // Start the sorting thread.
        Thread sortThread = new Thread(() -> {
            testCaseCurrent.getAlgorithm().sort(testCaseArrayGroup);
        });
        sortThread.start();
    }

    /**
     * Called when the sorting algorithm has finished sorting so the next value of N
     * can be tested or the plotting phase can be finished if the last run or
     * largest N has been reached.
     * If the plotting is NOT running, the visualization is running, or the test
     * cases were not read, then do nothing.
     */
    private void processPlottingSampleDone() {
        if (statusVisualRunning || !statusPlotRunning || !statusTestCasesReceived) {
            return;
        }

        plotPointList.add(new ScatterPoint(plotValNCurrent, testCaseArrayGroup.getAccessCount()));
        plotSampleCurrentIndex++;

        boolean finished = false;

        // If the current sample has not reached the end of all the samples.
        if (plotSampleCurrentIndex < plotSampleAmount) {
            plotValNCurrent = (int) ((plotSampleCurrentIndex + 1) * plotValNJump);

            // Ensure that the current N value did not exceed the total number of elements
            // of the input.
            if (plotValNCurrent <= testCaseCurrent.getInputSize()) {
                // Delete the input array if it already exists.
                if (testCaseArrayGroup.hasArray("input")) {
                    testCaseArrayGroup.removeArray("input");
                }

                // Update the loading bar.
                loadingBarPanel.updateStatus(plotSampleCurrentIndex + 1, plotSampleAmount, plotValNCurrent);
                loadingBarPanel.repaint();

                // Update the AnalyzedArrayGroup.
                testCaseArrayGroup.resetAccessCount();
                testCaseArrayGroup.addArray(testCaseCurrent.getInput(), 0, plotValNCurrent - 1, "input", true);

                // Start the sorting thread again.
                Thread sortThread = new Thread(() -> {
                    testCaseCurrent.getAlgorithm().sort(testCaseArrayGroup);
                });
                sortThread.start();
            } else {
                // If the current N has reached or surpassed the size of the input, stop.
                finished = true;
            }
        } else {
            finished = true;
        }

        // Finish up the plotting phase.
        if (finished) {
            processPlotFinish();
        }
    }

    /**
     * Finish the plotting phase by disabling the LoadingBarPanel, allowing the
     * ScatterPlotPanel to be painted with all of the recorded points, and setting
     * its view bounds as necessary.
     * If the plotting is not running, the visualization is running, or the test
     * cases were not read, then do nothing.
     */
    private void processPlotFinish() {
        if (statusVisualRunning || !statusPlotRunning || !statusTestCasesReceived) {
            return;
        }

        statusPlotRunning = false;

        // Set the loading bar to stop painting (effectively hide it).
        loadingBarPanel.togglePainting(false);

        // Set the scatter plot to begin painting.
        scatterPlotPanel.setInput(plotPointList);
        scatterPlotPanel.togglePainting(true);

        // Allow for the user to return.
        swingButtonReturnSelection.setEnabled(true);

        // If the bounds were manually set:
        if (testCaseCurrent.getIsPlotBoundsGiven()) {
            scatterPlotPanel.toggleViewAdjustAuto(false);

            // Set the lower and upper bounds of the x-axis.
            Long lowerX = testCaseCurrent.getPlotLowerBoundX();
            Long upperX = testCaseCurrent.getPlotUpperBoundX();

            if (lowerX != null && upperX != null) {
                scatterPlotPanel.setViewX(lowerX, upperX, 5);
            }

            // Set the lower and upper bounds of the y-axis.
            Long lowerY = testCaseCurrent.getPlotLowerBoundY();
            Long upperY = testCaseCurrent.getPlotUpperBoundY();

            if (lowerY != null && upperY != null) {
                scatterPlotPanel.setViewY(lowerY, upperY, 5);
            }
        } else {
            scatterPlotPanel.toggleViewAdjustAuto(true);
        }

        // With the bounds set, update the grid scale and repaint the panel.
        scatterPlotPanel.setGridScale();
        scatterPlotPanel.repaint();
    }

    /**
     * Called by an AnalyzedArrayGroup that an algorithm has finished sorting and a
     * test case has finished.
     * If the test cases were not read, then do nothing.
     */
    public void testCaseFinished() {
        if (!statusTestCasesReceived) {
            return;
        }
        if (statusVisualRunning && screenCurrentID == 2) {
            // If the visualization is running, then the visualization has ended.
            processVisualFinish();
        } else if (statusPlotRunning && screenCurrentID == 3) {
            // If the plotting is running, then signal that a sample has finished (which may
            // or may not end the plotting).
            processPlottingSampleDone();
        }
    }

    /**
     * Sets the current, class-wide test case to the test case of the given name, if
     * it exists.
     * If the test cases were not read, then do nothing.
     * 
     * @param testCaseName the String name of the test case to set
     */
    private void testCaseSelect(String testCaseName) {
        if (testCaseMap.containsKey(testCaseName) && statusTestCasesReceived) {
            testCaseCurrentName = testCaseName;
            testCaseCurrent = testCaseMap.get(testCaseName);
        }
    }

    /**
     * Repaints the graph to reflect changes to an array in the test case
     * visualization.
     * If the test cases were not read, then do nothing.
     */
    public void testCaseArrayUpdated() {
        if (statusVisualRunning && screenCurrentID == 2 && statusTestCasesReceived) {
            visualGraphPanel.repaint();
        }
    }
}
