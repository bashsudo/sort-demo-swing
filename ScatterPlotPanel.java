/*
 * CSC 345 PROJECT
 * Class:           ScatterPlotPanel.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A custom JPanel that implements a basic 2D scatter plot that
 *                  displays each point from a List of ScatterPlot points. It
 *                  can manually set the scale of the x-axis and y-axis or
 *                  automatically set them based on the smallest and largest
 *                  x-value and y-value among the ScatterPlot points. It also has
 *                  additional methods for customizing the font and colors of the
 *                  plot.
 */

import java.util.List;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Font;

public class ScatterPlotPanel extends JPanel {
    // THE MAIN INPUT OF THE SCATTER PLOT
    private List<ScatterPoint> plotInput;

    // Manually set bounds for the x-axis and y-axis.
    private long viewManualLowerX, viewManualUpperX, viewRoundX;
    private long viewManualLowerY, viewManualUpperY, viewRoundY;

    // Final bounds for the x-axis and y-axis.
    private long viewLowerY, viewUpperY, viewHeightY;
    private long viewLowerX, viewUpperX, viewWidthX;
    private boolean viewAdjustAuto;

    // Previous copies of the panel width and height to detect resizing.
    private int prevPanelWidth, prevPanelHeight;

    // Text at the top of the scatter plot.
    private String labelTitle, labelAxisX, labelAxisY;

    // Fonts and colors of the scatter plot.
    private Color colorBackground, colorHeading, colorLines, colorNumbers, colorDots;
    private Font fontTickNumber, fontHeading;

    // Other cosmetic adjustments to the scatter plot.
    private int edgeDistGrid, edgeDistBackground;
    private int gridOffsetY;
    private int dotDiameter;
    private int numTicksX, numTicksY;

    // Pixel bounds and dimensions of the scatter plot.
    private int gridLeftX, gridRightX, gridTopY, gridBottomY;
    private int gridWidthX, gridHeightY;
    private int bgLeftX, bgRightX, bgTopY, bgBottomY;

    // The x-values and y-values of the grid lines polygon.
    private int gridLineWidth;
    private int[] gridLinePolygonX, gridLinePolygonY;

    // Other states of the scatter plot.
    private boolean paintingEnabled, inputReady;

    // Misc variables.
    private String[] powTen;

    /**
     * Initializes the ScatterPlotPanel with reasonable defaults, however the input
     * List of ScatterPoint coordinate points needs to be set separately.
     */
    public ScatterPlotPanel() {
        gridLinePolygonX = new int[4];
        gridLinePolygonY = new int[4];

        setLabelText("Scatter Plot", "X: Values of X", "Y: Values of Y");

        setViewX(0, 100, 10);
        setViewY(0, 100, 10);

        setColors(
                new Color(200, 200, 200),
                new Color(20, 20, 20),
                new Color(60, 30, 30),
                new Color(20, 20, 20),
                new Color(230, 20, 20));

        setFont("Arial", 14, 12);

        gridLineWidth = 2;
        edgeDistGrid = 80;
        gridOffsetY = 40;
        edgeDistBackground = 20;
        dotDiameter = 10;

        numTicksX = 10;
        numTicksY = 10;

        powTen = new String[] { "K", "M", "B" };

        plotInput = null;
        viewAdjustAuto = true;
        inputReady = false;
        paintingEnabled = false;
    }

    /**
     * If the provided boolean is true, then the scatter plot is allowed to render.
     * Note that in order for the plot to be rendered, painting must be enabled
     * AND the input of ScatterPoint objects must be ready.
     * 
     * @param enabled if true, then painting is enabled
     */
    public void togglePainting(boolean enabled) {
        paintingEnabled = enabled;
    }

    /**
     * Set the input of the scatter plot as the provided List of ScatterPoint
     * objects representing 2D coordinate points.
     * Once set, the scales are re-adjusted and the plot is ready to be rendered
     * with the paintComponent() method. If the list is null, then nothing happens.
     * 
     * @param list the List of ScatterPoint objects.
     */
    public void setInput(List<ScatterPoint> list) {
        if (list == null) {
            return;
        }
        inputReady = true;
        plotInput = list;
        setGridScale();
        panelResized();
    }

    /**
     * Update the colors of the scatter plot.
     * 
     * @param background the Color of the entire background surrounding the contents
     *                   of the plot
     * @param heading    the Color of the text at the top of the plot
     * @param numbers    the Color of the numbers for the x-axis and y-axis
     * @param lines      the Color of the grid lines
     * @param dots       the Color of the dots of the plot
     */
    public void setColors(Color background, Color heading, Color numbers, Color lines, Color dots) {
        colorBackground = background;
        colorHeading = heading;
        colorNumbers = numbers;
        colorLines = lines;
        colorDots = dots;
    }

    /**
     * Update the name of the font and the size of the heading text and the numbers
     * of the scatter plot.
     * 
     * @param fontName    the String name of the font
     * @param headingSize the int size of the heading text
     * @param numberSize  the int size of the numbers
     */
    public void setFont(String fontName, int headingSize, int numberSize) {
        fontTickNumber = new Font(fontName, Font.BOLD, numberSize);
        fontHeading = new Font(fontName, Font.BOLD, headingSize);
    }

    /**
     * Set the content of the title, x-axis label, and y-axis label that are
     * displayed at the top of the scatter plot.
     * 
     * @param labelTitle the String content of the title label
     * @param labelAxisX the String content of the x-axis label
     * @param labelAxisY the String content of the y-axis label
     */
    public void setLabelText(String labelTitle, String labelAxisX, String labelAxisY) {
        this.labelTitle = labelTitle;
        this.labelAxisX = labelAxisX;
        this.labelAxisY = labelAxisY;
    }

    /**
     * Update the manual lower and upper boundaries of the domain (x-axis) of the
     * scatter plot as well as the interval to round them by.
     * This means that the domain is made up of values starting at and including
     * "lower" and ending at and including "upper."
     * The way that rounding works is that the lower and upper boundaries are
     * divided by "round" with integer division and multiplied back with "round" so
     * that they are set to the closest multiple of "round".
     * 
     * @param lower the long lower bound of the x-axis
     * @param upper the long upper bound of the x-axis
     * @param round the long value to round the lower and upper bound by
     */
    public void setViewX(long lower, long upper, long round) {
        viewManualLowerX = lower;
        viewManualUpperX = upper;
        viewRoundX = round;
    }

    /**
     * Update the manual lower and upper boundaries of the range (y-axis) of the
     * scatter plot as well as the interval to round them by.
     * This means that the range is made up of values starting at and including
     * "lower" and ending at and including "upper."
     * The way that rounding works is that the lower and upper boundaries are
     * divided by "round" with integer division and multiplied back with "round" so
     * that they are set to the closest multiple of "round".
     * 
     * @param lower the long lower bound of the y-axis
     * @param upper the long upper bound of the y-axis
     * @param round the long value to round the lower and upper bound by
     */
    public void setViewY(long lower, long upper, long round) {
        viewManualLowerY = lower;
        viewManualUpperY = upper;
        viewRoundY = round;
    }

    /**
     * If the provided boolean is true, then the scatter plot will disregard the
     * manually-set lower and upper bounds of the x-axis and y-axis and instead rely
     * on the minimum and maximum x-values and y-values in the input list.
     * If false, then the domain and range of the scatter plot will be based on the
     * bounds set by setViewX() and setViewY().
     * 
     * @param enabled if true then the scatter plot will automatically scale the
     *                x-axis and y-axis
     */
    public void toggleViewAdjustAuto(boolean enabled) {
        viewAdjustAuto = enabled;
    }

    /**
     * Apply either the manually or automatically set lower and upper bound of the
     * x-axis and y-axis to the scatter plot.
     * If viewAdjustAuto is true, then the bounds are determined by the smallest and
     * largest x-values and y-values in the input list of ScatterPoints; if false,
     * then it will use the manually set bounds.
     * If the input has not been set yet, then nothing happens.
     */
    public void setGridScale() {
        if (!inputReady) {
            return;
        }

        long lowerX = viewManualLowerX;
        long upperX = viewManualUpperX;

        long lowerY = viewManualLowerY;
        long upperY = viewManualUpperY;

        if (viewAdjustAuto) {
            boolean firstPoint = true;

            for (ScatterPoint point : plotInput) {
                if (firstPoint) {
                    lowerX = point.getX();
                    upperX = point.getX();

                    lowerY = point.getY();
                    upperY = point.getY();

                    firstPoint = false;
                } else {
                    if (point.getX() < lowerX) {
                        lowerX = point.getX();
                    }
                    if (point.getX() > upperX) {
                        upperX = point.getX();
                    }
                    if (point.getY() < lowerY) {
                        lowerY = point.getY();
                    }
                    if (point.getY() > upperY) {
                        upperY = point.getY();
                    }
                }
            }
        }

        // Round the final lower and upper bounds of the x-axis.
        viewLowerX = (lowerX / viewRoundX) * viewRoundX;
        viewUpperX = ((upperX / viewRoundX) + 1) * viewRoundX;

        // Round the final lower and upper bounds of the y-axis.
        viewLowerY = (lowerY / viewRoundY) * viewRoundY;
        viewUpperY = ((upperY / viewRoundY) + 1) * viewRoundY;

        viewWidthX = viewUpperX - viewLowerX;
        viewHeightY = viewUpperY - viewLowerY;
    }

    /**
     * Sets the leftmost, rightmost, topmost, and bottommost pixel boundaries of the
     * background and grid for use by the methods that actually render the plot.
     * This should be called only if it is found that the panel was resized by the
     * Swing GUI. Naively calling this method each time the plot is rendered is
     * inefficient.
     */
    private void panelResized() {
        prevPanelWidth = getWidth();
        prevPanelHeight = getHeight();

        int squareLeft = (prevPanelWidth / 2) - (prevPanelHeight / 2);
        int squareRight = (prevPanelWidth / 2) + (prevPanelHeight / 2);

        // Update the boundaries of the grid of the scatter plot.
        gridLeftX = squareLeft + edgeDistGrid;
        gridRightX = squareRight - edgeDistGrid;
        gridTopY = edgeDistGrid + gridOffsetY;
        gridBottomY = prevPanelHeight - edgeDistGrid + gridOffsetY;

        gridWidthX = gridRightX - gridLeftX;
        gridHeightY = gridBottomY - gridTopY;

        // Update the boundaries of the background of the scatter plot.
        bgLeftX = squareLeft + edgeDistBackground;
        bgRightX = squareRight - edgeDistBackground;
        bgTopY = edgeDistBackground;
        bgBottomY = prevPanelHeight - edgeDistBackground;
    }

    /**
     * Returns a String of the provided long number that only has at most 2 regular
     * digits and 1 decimal digit and has an abbreviation for a power of 1000.
     * For example, if the number was 10,000, then the output is "10.0 K" because
     * "K" is an abbreviation for (1000)^1 or a thousand. There is also "M" for
     * (1000)^2 or a million and "B" for (1000)^3 or a billion.
     * 
     * @param number the long number to abbreviate
     * @return a String abbreviated form fo the provided number
     */
    private String numAbbreviated(long number) {
        int currentThousandPow = 0;
        long currentVal = 1000;

        if (Math.abs(number) >= currentVal) {
            while (Math.abs(number / currentVal) >= 100 && currentThousandPow < powTen.length - 1) {
                currentVal *= 1000;
                currentThousandPow += 1;
            }

            return String.format("%.1f%s", number / (double) currentVal, powTen[currentThousandPow]);
        }

        return "" + number;
    }

    /**
     * Paints the numbers along the ticks of the x-axis and y-axis onto the provided
     * Graphics object.
     * To save on space, the abbreviated forms of the tick numbers generated with
     * the method numAbbreviated() are used. If the input List of ScatterPoint
     * objects has not been set yet or painting is disabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    private void drawTickNumbers(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        // Setup the Graphics2D object.
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Setup the font and color of the Graphics2D object.
        g2D.setFont(fontTickNumber);
        g2D.setColor(colorNumbers);
        FontMetrics metrics = g2D.getFontMetrics(fontTickNumber);

        String message;
        long originalNumber;

        // Paints the tick numbers for the y-axis.
        for (int i = 0; i <= numTicksY; i++) {
            // Calculate the number for the tick.
            originalNumber = (long) (viewLowerY + i * (viewHeightY / (double) numTicksY));
            // Abbreviate the number.
            message = numAbbreviated(originalNumber);
            // Actually paint the abbreviated number to the Graphics2D object.
            g2D.drawString(message, gridLeftX - metrics.stringWidth(message) - 10,
                    getTickPosY(i) + metrics.getHeight() / 2);
        }

        // Paints the tick numbers for the x-axis.
        for (int i = 0; i <= numTicksX; i++) {
            // Calculate the number for the tick.
            originalNumber = (long) (viewLowerX + i * (viewWidthX / (double) numTicksX));
            // Abbreviate the number.
            message = numAbbreviated(originalNumber);
            // Actually paint the abbreviated number to the Graphics2D object.
            g2D.drawString(message, getTickPosX(i) - metrics.stringWidth(message) / 2, gridBottomY + 15);
        }
    }

    /**
     * Returns the horizontal pixel location of the provided index of a tick on the
     * x-axis.
     * An index of 0 corresponds to the tick on the origin or the leftmost,
     * bottommost corner of the scatter plot.
     * 
     * @param tickIndexX the int index of a tick on the x-axis
     * @return the int horizontal pixel corresponding to the pixel
     */
    private int getTickPosX(int tickIndexX) {
        return (int) (gridLeftX + (tickIndexX / (double) numTicksX) * (gridWidthX));
    }

    /**
     * Returns the vertical pixel location of the provided index of a tick on the
     * y-axis.
     * An index of 0 corresponds to the tick on the origin or the leftmost,
     * bottommost corner of the scatter plot.
     * 
     * @param tickIndexX the int index of a tick on the y-axis
     * @return the int vertical pixel corresponding to the pixel
     */
    private int getTickPosY(int tickIndexY) {
        return (int) (gridTopY + ((numTicksY - tickIndexY) / (double) numTicksY) * (gridHeightY));
    }

    /**
     * Paints the title, x-axis legend/label, and y-axis legend/label at the top of
     * the scatter plot.
     * If the input List of ScatterPoint objects has not been set yet or painting is
     * not enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    private void drawHeading(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        // Setup the Graphics2D object.
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Setup the font and colors of the Graphics2D object.
        g2D.setFont(fontHeading);
        g2D.setColor(colorHeading);
        FontMetrics metrics = g2D.getFontMetrics(fontHeading);

        int headingHeight = metrics.getHeight();

        g2D.drawString(labelTitle, bgLeftX + 5, bgTopY + headingHeight);
        g2D.drawString("X: " + labelAxisX, bgLeftX + 5, bgTopY + headingHeight * 2);
        g2D.drawString("Y: " + labelAxisY, bgLeftX + 5, bgTopY + headingHeight * 3);
    }

    /**
     * Paints the lines of the grid for the scatter plot.
     * If the input List of ScatterPoint objects has not been set yet or painting is
     * not enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    private void drawGrid(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        // Paints the background of the plot.
        graphics.setColor(colorBackground);
        graphics.fillRect(bgLeftX, bgTopY, bgRightX - bgLeftX, bgBottomY - bgTopY);

        graphics.setColor(colorLines);

        // Since the x-values of the horizontal lines along the y-axis remain constant,
        // set them in advance.
        gridLinePolygonX[0] = gridLeftX;
        gridLinePolygonX[1] = gridLeftX;
        gridLinePolygonX[2] = gridRightX;
        gridLinePolygonX[3] = gridRightX;

        // Begin drawing the horizontal lines from the bottom to the top of the plot.
        int currentYTop;
        int currentYBottom;
        for (int i = 0; i <= numTicksY; i++) {
            currentYTop = getTickPosY(i) - gridLineWidth;
            currentYBottom = getTickPosY(i) + gridLineWidth;

            gridLinePolygonY[0] = currentYBottom;
            gridLinePolygonY[1] = currentYTop;
            gridLinePolygonY[2] = currentYTop;
            gridLinePolygonY[3] = currentYBottom;

            graphics.fillPolygon(gridLinePolygonX, gridLinePolygonY, 4);
        }

        // Since the y-values of the vertical lines along the x-axis remain constant,
        // set them in advance.
        gridLinePolygonY[0] = gridBottomY + gridLineWidth;
        gridLinePolygonY[1] = gridTopY - gridLineWidth;
        gridLinePolygonY[2] = gridTopY - gridLineWidth;
        gridLinePolygonY[3] = gridBottomY + gridLineWidth;

        // Begin drawing the vertical lines from the left to the right side of the plot.
        int currentXLeft;
        int currentXRight;
        for (int i = 0; i <= numTicksX; i++) {
            currentXLeft = getTickPosX(i) - gridLineWidth;
            currentXRight = getTickPosX(i) + gridLineWidth;

            gridLinePolygonX[0] = currentXLeft;
            gridLinePolygonX[1] = currentXLeft;
            gridLinePolygonX[2] = currentXRight;
            gridLinePolygonX[3] = currentXRight;

            graphics.fillPolygon(gridLinePolygonX, gridLinePolygonY, 4);
        }
    }

    /**
     * Paints each coordinate point represented by a ScatterPoint object in the
     * input
     * List onto the provided Graphics object.
     * If the input List of ScatterPoint objects has not been set yet or painting is
     * not enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    private void drawDots(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        double pointRatioX, pointRatioY;
        int pixelX, pixelY;

        graphics.setColor(colorDots);

        for (ScatterPoint point : plotInput) {
            // Continue drawing if the x-value and y-value of the ScatterPoint is within the
            // boundaries of the scatter plot.
            if (point.getX() >= viewLowerX && point.getX() <= viewUpperX && point.getY() >= viewLowerY
                    && point.getY() <= viewUpperY) {

                // Calculate the ratio between the x-value of the ScatterPoint and the width of
                // the x-axis.
                pointRatioX = (point.getX() - viewLowerX) / (double) viewWidthX;

                // Calculate the ratio between the y-value of the ScatterPoint and the height of
                // the y-axis.
                pointRatioY = (point.getY() - viewLowerY) / (double) viewHeightY;

                // Multiply the ratios by the actual pixel width and height of the grid.
                pixelX = (int) (gridLeftX + pointRatioX * (gridWidthX));
                pixelY = (int) (gridBottomY - pointRatioY * (gridHeightY));

                graphics.fillOval(pixelX - dotDiameter / 2, pixelY - dotDiameter / 2, dotDiameter, dotDiameter);
            }
        }
    }

    /**
     * Paints the entire scatter plot to the provided Graphics object (overrides the
     * default paintComponent of JPanel).
     * If the input List of ScatterPoint objects has not been set yet or painting is
     * not enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    public void paintComponent(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        // If the current width or height of the panel differs from the previously
        // recorded width and height, then it was resized by the GUI.
        if (prevPanelWidth != getWidth() || prevPanelHeight != getHeight()) {
            panelResized();
        }

        drawGrid(graphics);
        drawTickNumbers(graphics);
        drawDots(graphics);
        drawHeading(graphics);
    }
}