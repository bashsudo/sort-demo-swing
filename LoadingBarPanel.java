/*
 * CSC 345 PROJECT
 * Class:           LoadingBarPanel.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A custom JPanel that complements the ScatterPlotPanel by displaying
 *                  the information of the plotting process as both a colored, rectangular
 *                  bar and text beneath it. It displays the current size of the input (N)
 *                  being plotted, the current "run" of the sorting algorithm, and the name
 *                  of the sorting algorithm being processed. It uses some of the information
 *                  provided by a TestCase object.
 */

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Font;

public class LoadingBarPanel extends JPanel {
    // THE MAIN INPUT OF THE LOADING BAR
    private TestCase testCase;

    // The font and colors of the loading bar.
    private Color colorBackground, colorBarBackground,
            colorBarFill, colorMessageText;
    private Font fontMessage;

    // Previous copies of the panel width and height to detect resizing.
    private int prevPanelWidth, prevPanelHeight;

    // Pixel bounds and dimensions of the loading bar, including the bar itself.
    private int contentEdgeDist;
    private int bgLeftX, bgRightX, bgTopY, bgBottomY;
    private int barLeftX, barRightX, barTopY, barBottomY;
    private int barWidth, barHeight;
    private double barFractionFull;

    // Pixel width, height, and spacing of the text messages.
    private String[] messageList;
    private int messageWidthNeeded, messageHeightNeeded,
            messageSpacing, barMessageGap;

    // Other states of the loading bar.
    private boolean needAdjustment, paintingEnabled, inputReady;

    /**
     * Initializes the LoadingBarPanel with reasonable defaults, however the input
     * TestCase needs to be set separately.
     */
    public LoadingBarPanel() {
        messageList = new String[] {
                "EMPTY",
                "EMPTY",
                "EMPTY"
        };

        setFont("Arial", 14);

        setColors(
                new Color(50, 50, 50),
                new Color(100, 100, 100),
                new Color(255, 180, 180),
                new Color(255, 255, 255));

        barWidth = 400;
        barHeight = 30;
        contentEdgeDist = 10;
        barMessageGap = 30;
        messageSpacing = 5;

        testCase = null;
        paintingEnabled = false;
        inputReady = false;
        needAdjustment = true;
    }

    /**
     * If the provided boolean is true, then the loading bar is allowed to render.
     * Note that in order for the loading bar to be rendered, painting must be
     * enabled AND a TestCase must be provided.
     * 
     * @param enabled if true, then painting is enabled
     */
    public void togglePainting(boolean enabled) {
        paintingEnabled = enabled;
    }

    /**
     * Set the TestCase so that the loading bar can display its name and total input
     * array size to provide the user with more useful information.
     * 
     * @param testCase the TestCase object with the info to display
     */
    public void setTestCaseInfo(TestCase testCase) {
        if (testCase == null) {
            return;
        }
        this.testCase = testCase;
        inputReady = true;
        panelResized();
    }

    /**
     * Update the colors of the loading bar.
     * 
     * @param barBackground the Color of the entire background surrounding the
     *                      contents of the loading bar
     * @param barFill       the Color of the "complete" portion of the bar
     * @param background    the Color of the "incomplete" portion of the bar
     *                      (progress yet to be made)
     * @param messageText   the Color of the text of the loading bar
     */
    public void setColors(Color barBackground, Color barFill, Color background, Color messageText) {
        colorBarBackground = barBackground;
        colorBarFill = barFill;
        colorBackground = background;
        colorMessageText = messageText;
    }

    /**
     * Update the name and size of the font of the loading bar.
     * 
     * @param fontName the String name of the font
     * @param fontSize the int size of the font
     */
    public void setFont(String fontName, int fontSize) {
        fontMessage = new Font(fontName, Font.BOLD, fontSize);
    }

    /**
     * Sets the leftmost, rightmost, topmost, and bottommost pixel boundaries of the
     * background and loading bar for use by the methods that actually render them.
     * This should be called only if it is found that the panel was resized by the
     * Swing GUI. Naively calling this method each time the plot is rendered is
     * inefficient.
     */
    private void panelResized() {
        prevPanelWidth = getWidth();
        prevPanelHeight = getHeight();

        // Define the width and height of the background based on how how much space is
        // needed and later calculate the boundaries.
        int bgHeight = barHeight + contentEdgeDist + barMessageGap + messageHeightNeeded;
        int bgWidth = Math.max(barWidth, messageWidthNeeded) + contentEdgeDist * 2;

        // Update the boundaries of the contents of the loading bar altogether.
        bgLeftX = getWidth() / 2 - bgWidth / 2;
        bgRightX = getWidth() / 2 + bgWidth / 2;
        bgTopY = getHeight() / 2 - bgHeight / 2;
        bgBottomY = getHeight() / 2 + bgHeight / 2;

        // Update the boundaries of the bar itself.
        barLeftX = getWidth() / 2 - barWidth / 2;
        barRightX = getWidth() / 2 + barWidth / 2;
        barTopY = bgTopY + contentEdgeDist;
        barBottomY = bgTopY + contentEdgeDist + barHeight;
    }

    /**
     * Uses the provided Graphics object to measure the pixel width of the longest
     * String text of the loading bar in advance so that it can be used for
     * rendering.
     * It also calculates the average height of each line of text.
     * 
     * @param graphics the Graphics object to use for the measurement
     */
    private void adjustBarDimensions(Graphics graphics) {
        // Set up the Graphics2D object.
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setFont(fontMessage);
        FontMetrics metrics = g2D.getFontMetrics(fontMessage);

        messageHeightNeeded = (metrics.getHeight() + messageSpacing) * messageList.length;

        boolean firstMessage = true;
        int width;

        for (String message : messageList) {
            width = metrics.stringWidth(message);

            if (firstMessage) {
                messageWidthNeeded = width;
                firstMessage = false;

            } else if (width > messageWidthNeeded) {
                messageWidthNeeded = width;
            }
        }
    }

    /**
     * Updates the width of the bar and the text contents of the messages that are
     * to be displayed.
     * Since the messages may now have different lengths than before, the flag for
     * adjustment is set so that the longest width can be found again.
     * 
     * @param runCurrent   the int current run number of the sorting algorithm in
     *                     the Swing GUI plotting process
     * @param runMax       the int largest run number (the last run when the
     *                     plotting process is finished)
     * @param currentSizeN the int current input size that is being plotted
     */
    public void updateStatus(int runCurrent, int runMax, int currentSizeN) {
        barFractionFull = runCurrent / (double) runMax;
        int percentFinished = (int) (barFractionFull * 100);

        messageList[0] = String.format("Progress of Plotting Algorithm \"%s\": %d%%", testCase.getAlgorithmName(),
                percentFinished);
        messageList[1] = String.format("Current Size of Input (N): %d / %d", currentSizeN, testCase.getInputSize());
        messageList[2] = String.format("Current Run: %d / %d", runCurrent, runMax);

        needAdjustment = true;
    }

    /**
     * Paints the text messages indicating the current status and information of the
     * loading bar onto the provided Graphics object.
     * If the TestCase has not been set yet or painting has not been enabled, then
     * nothing happens.
     * 
     * @param graphics the Graphics object to paint onto
     */
    private void drawMessages(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setFont(fontMessage);
        FontMetrics metrics = g2D.getFontMetrics(fontMessage);

        g2D.setColor(colorMessageText);

        for (int i = 0; i < messageList.length; i++) {
            g2D.drawString(messageList[i], bgLeftX + contentEdgeDist,
                    barBottomY + barMessageGap + i * (metrics.getHeight() + messageSpacing));
        }
    }

    /**
     * Paints the background of the entire loading bar and the bar itself onto the
     * provided Graphics object.
     * If the TestCase has not been set yet or painting has not been enabled, then
     * nothing happens.
     * 
     * @param graphics the Graphics object to paint onto
     */
    private void drawBar(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }
        // Paint the background of the entire loading bar.
        graphics.setColor(colorBackground);
        graphics.fillRect(bgLeftX, bgTopY, bgRightX - bgLeftX, bgBottomY - bgTopY);

        // Paint the background of the bar itself.
        graphics.setColor(colorBarBackground);
        graphics.fillRect(barLeftX, barTopY, barRightX - barLeftX, barBottomY - barTopY);

        // Paint the "fill" or "progress" portion of the bar itself.
        graphics.setColor(colorBarFill);
        graphics.fillRect(barLeftX, barTopY, (int) ((barRightX - barLeftX) * barFractionFull), barBottomY - barTopY);
    }

    /**
     * Paints the entire loading bar onto the provided Graphics object (overrides
     * the default paintComponent of JPanel).
     * If the TestCase has not been set yet or painting has not been enabled, then
     * nothing happens.
     * 
     * @param graphics the Graphics object to paint onto
     */
    public void paintComponent(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        // If the message contents recently changed, adjust and re-calculate the bar
        // dimensions.
        if (needAdjustment) {
            adjustBarDimensions(graphics);
            panelResized();
            needAdjustment = false;
        }

        // If the current width or height of the panel differs from the previously
        // recorded width and height, then it was resized by the GUI.
        if (prevPanelWidth != getWidth() || prevPanelHeight != getHeight()) {
            panelResized();
        }

        drawBar(graphics);
        drawMessages(graphics);
    }
}