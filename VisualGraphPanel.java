/*
 * CSC 345 PROJECT
 * Class:           VisualGraphPanel.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A custom JPanel that implements a bar graph representing the
 *                  elements of the visible AnalyzedArrays of the provided
 *                  AnalyzedArrayGroup. It colors bars that were last get and set
 *                  to help demonstrate the sorting behavior and efficiently of
 *                  a sorting algorithm. It also has additional methods for
 *                  customizing the font and colors of the graph.
 */

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Font;

public class VisualGraphPanel extends JPanel {
    // THE MAIN INPUT OF THE GRAPH
    private AnalyzedArrayGroup group;

    // The leftmost, rightmost, topmost, bottommost pixel locations of the graph and
    // background.
    private int bgLeftX, bgRightX, bgTopY, bgBottomY;
    private int graphLeftX, graphRightX, graphTopY, graphBottomY;
    private int graphWidth, graphWidthMinimumNeeded, graphHeight;

    // Previous copies of the panel width and height to detect resizing.
    private int prevPanelWidth, prevPanelHeight;

    // Other cosmetic adjustments to the
    private int edgeDistBackground, edgeDistGraph, graphOffsetY;

    // Pixel measurements for the sorted status message.
    private int sortedMessageCornerDist, sortedMessageEdgeDist;

    // The width of each bar and the x-values and y-values of the corners of an
    // individual bar as a polygon.
    private double singleBarWidthWithGap, singleBarWidthWithoutGap;
    private int[] barPolygonX, barPolygonY;

    // Fonts and colors of the graph.
    private Color colorBackground, colorMessageBackground, colorMessageText,
            colorBarRegular, colorBarSet, colorBarGet;
    private Font fontMessage;

    // Other states of the graph and the sorted status of the input AnalyzedArray.
    private boolean paintingEnabled, inputReady;
    private boolean sortedStatusDisplayEnabled, sortedStatusCached, sortedStatusCacheValue;

    public VisualGraphPanel() {
        // Variables for defining the offset of the background and graph area from some
        // chosen X and Y boundaries.
        edgeDistBackground = 20;
        edgeDistGraph = 60;
        graphOffsetY = 20;

        // Initialize int arrays for Graphics to draw the bars of the bar graph as
        // polygons (more efficient and straightforward than using a Polygon object).
        barPolygonX = new int[4];
        barPolygonY = new int[4];

        // Set the default colors for the bar graph.
        setColors(
                new Color(175, 175, 175),
                new Color(25, 25, 25),
                new Color(255, 25, 25),
                new Color(25, 255, 25),
                new Color(50, 50, 50),
                new Color(200, 200, 200));

        // Set the default font.
        setFont("Arial", 12);

        sortedMessageEdgeDist = 5;
        sortedMessageCornerDist = 20;

        group = null;
        inputReady = false;
        paintingEnabled = false;
        sortedStatusDisplayEnabled = false;
    }

    /**
     * If the provided boolean is true, then the graph is allowed to render.
     * Note that in order for the graph to be rendered, painting must be enabled
     * AND the AnalyzedArrayGroup input must be ready.
     * 
     * @param enabled if true, then painting is enabled
     */
    public void togglePainting(boolean enabled) {
        paintingEnabled = enabled;
    }

    /**
     * Set the input of the graph as the provided AnalyzedArrayGroup.
     * Once set, the panel is re-adjusted and the graph is ready to be rendered
     * with the paintComponent() method. If the group is null, then nothing happens.
     * 
     * @param group the AnalyzedArrayGroup that is to be graphed
     */
    public void setInput(AnalyzedArrayGroup group) {
        if (group == null) {
            return;
        }
        this.group = group;
        inputReady = true;
        panelResized();

    }

    /**
     * Update the colors of the graph.
     * 
     * @param graphBackground   the Color of the entire background surrounding the
     *                          contents of the graph
     * @param barRegular        the Color of a bar in the graph
     * @param barSet            the Color of a bar of a recently set element
     * @param barGet            the Color of a bar of a recently get element
     * @param messageBackground the Color of the background surrounding the message
     * @param messageText       the Color of the text of the message
     */
    public void setColors(Color graphBackground, Color barRegular, Color barSet, Color barGet, Color messageBackground,
            Color messageText) {
        colorBackground = graphBackground;
        colorBarRegular = barRegular;
        colorBarSet = barSet;
        colorBarGet = barGet;
        colorMessageBackground = messageBackground;
        colorMessageText = messageText;
    }

    /**
     * Update the name and size of the font for the text in the graph.
     * 
     * @param fontName the String name of the font
     * @param fontSize the int size of the font
     */
    public void setFont(String fontName, int fontSize) {
        fontMessage = new Font(fontName, Font.BOLD, fontSize);
    }

    /**
     * If the provided boolean is true, then the graph is allowed to render the
     * message that indicates whether or not the "input" array of the
     * AnalyzedArrayGroup is sorted in ascending order.
     * This should be set by the Swing GUI once the sorting algorithm is finished.
     * 
     * @param enabled if true, then the graph will render the sort message
     */
    public void toggleDisplaySorted(boolean enabled) {
        sortedStatusDisplayEnabled = enabled;
        sortedStatusCached = false;
    }

    /**
     * Sets the leftmost, rightmost, topmost, and bottommost pixel boundaries of the
     * background and graph contents for use by the methods that actually render the
     * graph.
     * This should be called only if it is found that the panel was resized by the
     * Swing GUI. Naively calling this method each time the graph is rendered is
     * inefficient.
     */
    private void panelResized() {
        if (!inputReady) {
            return;
        }

        // Update copies of width and height (so paintComponent can identify if most
        // recent width and height has changed and resizing is required).
        prevPanelWidth = getWidth();
        prevPanelHeight = getHeight();

        // The leftmost X is the first 1/20th slice of the panel width and the rightmost
        // X is the last 1/20th slice; the background and graph area use these as a
        // reference.
        int farLeftX = prevPanelWidth / 20;
        int farRightX = (prevPanelWidth * 19) / 20;

        // >>> UPDATE THE GRAPH BOUNDS
        // For the X, offset the farthest values inward by the edge distance.
        // For the Y, use the entire panel height that is offset inward by the edge
        // distance and shifted downward by the offsetY.
        graphLeftX = farLeftX + edgeDistGraph;
        graphRightX = farRightX - edgeDistGraph;
        graphTopY = graphOffsetY + edgeDistGraph;
        graphBottomY = prevPanelHeight + graphOffsetY - edgeDistGraph;

        // >>> UPDATE THE GRAPH DIMENSIONS
        graphWidth = graphRightX - graphLeftX;
        graphHeight = graphBottomY - graphTopY;

        // While the bars may change in height, they will always have the same lower Y
        // values, so they can be set in advance.
        barPolygonY[0] = graphBottomY;
        barPolygonY[3] = graphBottomY;

        // >>> UPDATE THE BACKGROUND BOUNDS
        // For the X, offset the farthest values inward by the edge distance.
        // For the Y, use the entire panel height that is offset inward by the edge
        // distance.
        bgLeftX = farLeftX + edgeDistBackground;
        bgRightX = farRightX - edgeDistBackground;
        bgTopY = edgeDistBackground;
        bgBottomY = prevPanelHeight - edgeDistBackground;

        // The decimal width of an entire bar on the bar graph (including the width for
        // the visible bar itself AND the gap between it and the next bar).
        singleBarWidthWithGap = graphWidth / (double) group.size();

        // The decimal width of the visible portion of the bar, which is 9/10ths of the
        // entire width.
        singleBarWidthWithoutGap = (singleBarWidthWithGap * 9) / 10;

        // The minimum width of the graph needed to ensure that each bar (which
        // represents each element in the array group) is at least one pixel wide.
        graphWidthMinimumNeeded = (int) ((group.size() * 10) / 9.0);
    }

    /**
     * If the pixel width of the graph is less than the minimum width required to be
     * able to properly display the bars, then display the message warning the user
     * and indicating the current, inadequate pixel width.
     * If the AnalyzedArrayGroup input has not been set yet or painting is not
     * enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    private void drawMessageResize(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        if (graphWidth < graphWidthMinimumNeeded) {
            // Get the Graphics2D object and use anti-aliasing for smooth edges.
            Graphics2D g2D = (Graphics2D) graphics;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Update the font for the Graphics2D object.
            g2D.setFont(fontMessage);
            FontMetrics metrics = g2D.getFontMetrics(fontMessage);

            // Update the string values of the minimum-width message.
            String messageStringMain = "The window size is too small for the graph, please make it larger.";
            String messageStringPixels = String.format(
                    "Current Graph Width = %d Pixels, Minimum Graph Width Needed = %d Pixels", graphWidth,
                    graphWidthMinimumNeeded);

            // Measure the pixel width of the messages based on their font.
            int messageMainWidth = metrics.stringWidth(messageStringMain);
            int messagePixelsWidth = metrics.stringWidth(messageStringPixels);

            // Ensure that the background of the message is long enough to hold the longest
            // string of the two.
            int messageBackgroundWidth = Math.max(messageMainWidth, messagePixelsWidth) + 20;
            int messageBackgroundHeight = metrics.getHeight() * 2 + 20;

            // Draw the background rectangle in the middle of the panel.
            graphics.setColor(colorMessageBackground);
            graphics.fillRect(prevPanelWidth / 2 - messageBackgroundWidth / 2,
                    prevPanelHeight / 2 - messageBackgroundHeight / 2, messageBackgroundWidth,
                    messageBackgroundHeight);

            // Draw the message text in the middle of the panel.
            g2D.setColor(colorMessageText);
            g2D.drawString(messageStringMain, prevPanelWidth / 2 - messageMainWidth / 2,
                    prevPanelHeight / 2 - metrics.getHeight() / 2);
            g2D.drawString(messageStringPixels, prevPanelWidth / 2 - messagePixelsWidth / 2,
                    prevPanelHeight / 2 + metrics.getHeight() / 2);
        }
    }

    /**
     * Paints the message indicating the sorted status of the "input" array of the
     * AnalyzedArrayGroup if it is enabled for the graph.
     * If the AnalyzedArrayGroup input has not been set yet or painting is not
     * enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    public void drawMessageSorted(Graphics graphics) {
        if (!paintingEnabled || !inputReady || !sortedStatusDisplayEnabled) {
            return;
        }

        // Get the Graphics2D object and use anti-aliasing for smooth edges.
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Update the font for the Graphics2D object.
        g2D.setFont(fontMessage);
        FontMetrics metrics = g2D.getFontMetrics(fontMessage);

        // If the sorted status has not been cached yet, then proceed to cache it and
        // indicate that it was cached.
        if (!sortedStatusCached) {
            sortedStatusCacheValue = group.hasArray("input") && group.getArray("input").isSorted();
            sortedStatusCached = true;
        }

        String message;

        if (sortedStatusCacheValue) {
            message = "The array is sorted";
        } else {
            message = "The array is not sorted";
        }

        // Calculate the width and height of the overall message box to be rendered.
        int messageBackgroundWidth = metrics.stringWidth(message) + sortedMessageEdgeDist;
        int messageBackgroundHeight = metrics.getHeight() + sortedMessageEdgeDist;

        // Draw the background rectangle surrounding the text contents.
        graphics.setColor(colorMessageBackground);
        graphics.fillRect(sortedMessageCornerDist, sortedMessageCornerDist,
                messageBackgroundWidth + sortedMessageEdgeDist / 2,
                messageBackgroundHeight + sortedMessageEdgeDist / 2);

        // Draw the text of the sorted status message itself.
        g2D.setColor(colorMessageText);
        g2D.drawString(message, sortedMessageCornerDist + sortedMessageEdgeDist / 2,
                sortedMessageCornerDist + metrics.getHeight() + sortedMessageEdgeDist / 2);

    }

    /**
     * Paints the bars of the graph if the graph pixel width is greater than the
     * minimum width.
     * If the AnalyzedArrayGroup input has not been set yet or painting is not
     * enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    private void drawGraph(Graphics graphics) {
        if (!paintingEnabled || !inputReady || graphWidth < graphWidthMinimumNeeded) {
            return;
        }

        // Draw the background of the graph.
        graphics.setColor(colorBackground);
        graphics.fillRect(bgLeftX, bgTopY, bgRightX - bgLeftX, bgBottomY - bgTopY);

        // Update the helper variables for defining the boundaries of each bar.
        int barLeftX;
        int barRightX;
        int barTopY;
        double valueRatio;

        // Initialize the min and max for scaling the bars to be within the panel.
        int minVal = group.getMin();
        int maxVal = group.getMax();

        for (int i = 0; i < group.size(); i++) {
            // Determine the leftmost and rightmost pixel of the bar on the panel.
            barLeftX = (int) (graphLeftX + singleBarWidthWithGap * i);
            barRightX = (int) (barLeftX + singleBarWidthWithoutGap);

            // Determine the topmost pixel of the bar, which filters the element value with
            // the range of element values in the group and multiplies it by the graph
            // height.
            // To ensure that an element with the minimum value is not subtracted to 0 and
            // appears as a flat, invisible bar, the "+1" was added to the numerator and
            // denominator.
            valueRatio = (group.getExternal(i) - minVal + 1) / (double) (maxVal - minVal + 1);
            barTopY = (int) (graphBottomY - graphHeight * valueRatio);

            // In case something went wrong with the math in calculating the bar's height,
            // prevent it from being drawn.
            if (barTopY <= graphBottomY && barTopY >= graphTopY) {
                // Update the X and Y integer arrays to represent a polygon (rectangle) so the
                // bar can be drawn by Graphics.
                barPolygonX[0] = barLeftX;
                barPolygonX[1] = barLeftX;
                barPolygonX[2] = barRightX;
                barPolygonX[3] = barRightX;

                barPolygonY[1] = barTopY;
                barPolygonY[2] = barTopY;

                // If the bar represents an element that was the last element to be get or set
                // by an array in the group, then color it accordingly.
                if (i == group.getIndexLastGet()) {
                    graphics.setColor(colorBarGet);
                } else if (i == group.getIndexLastSet()) {
                    graphics.setColor(colorBarSet);
                } else {
                    graphics.setColor(colorBarRegular);
                }

                // Actually draw the bar as a rectangle (polygon with 4 points) with Graphics.
                graphics.fillPolygon(barPolygonX, barPolygonY, 4);
            }
        }
    }

    /**
     * Paints the entire scatter plot to the provided Graphics object (overrides the
     * default paintComponent of JPanel).
     * If the AnalyzedArrayGroup input has not been set yet or painting is not
     * enabled, then nothing happens.
     * 
     * @param graphics the Graphics object to paint to
     */
    public void paintComponent(Graphics graphics) {
        if (!paintingEnabled || !inputReady) {
            return;
        }

        // If the previously recorded dimensions of the panel differ with the current
        // width and height, then the panel changed size.
        if (prevPanelWidth != getWidth() || prevPanelHeight != getHeight()) {
            panelResized();
        }

        drawGraph(graphics);
        drawMessageResize(graphics);
        drawMessageSorted(graphics);
    }
}