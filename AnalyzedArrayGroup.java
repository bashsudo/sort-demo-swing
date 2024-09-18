/*
 * CSC 345 PROJECT
 * Class:           AnalyzedArrayGroup.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A high-level container of multiple AnalyzedArrays with operations for managing
 *                  AnalyzedArrays (creating, deleting, retrieving, changing visibility) and
 *                  retrieving information about the entire group (size, min, max, individual
 *                  elements). It also serves as an interface between sorting algorithms and the
 *                  Swing GUI.
 *                  NOTE: THIS CLASS IS DESIGNED TO WORK WITH MULTIPLE THREADS.
 */

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class AnalyzedArrayGroup {
    // Swing GUI
    private SortDemoSwing visualizer;

    // Analyzed Arrays and Visibility
    private Map<String, AnalyzedArray> arrayMap;
    private Map<String, Boolean> arrayVisibility;

    // Minimum and Maximum
    private int globalMinValue, globalMaxValue;

    // Global Index Accessing
    private int globalIndexLastGet, globalIndexLastSet;
    private HashMap<String, Integer> capacityOffset;
    private List<String> arrayNameInOrder;
    private AnalyzedArray[] globalIndexToArray;
    private int[] globalIndexOffsets;
    private int globalCapacity;

    // Other Behavior
    private int delay;
    private boolean applyDelay;
    private boolean reportArrayUpdates;

    /**
     * Initialize the array group by setting reasonable default values.
     * 
     * For simplicity, the global set and get indices are assumed to be -1 and the
     * minimum and maximum are assumed to be 0. The delay of 5 milliseconds is
     * arbitrary but it ensures that the sleeping is not instantaneous for the sake
     * of visualization in the GUI.
     * 
     * @param visualizer the reference to the Swing GUI object
     */
    public AnalyzedArrayGroup(SortDemoSwing visualizer) {
        this.visualizer = visualizer;

        // Analyzed Array Info
        arrayMap = new HashMap<>();
        arrayVisibility = new HashMap<>();

        // Index
        globalIndexLastGet = -1;
        globalIndexLastSet = -1;
        capacityOffset = new HashMap<>();
        arrayNameInOrder = new ArrayList<>();

        // Initialized by the Global Index Methods
        globalIndexToArray = null;
        globalIndexOffsets = null;
        globalCapacity = 0;

        // Min and Max
        globalMinValue = 0;
        globalMaxValue = 0;

        // Other Information
        delay = 5;
        applyDelay = true;
        reportArrayUpdates = true;
    }

    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // METHODS POTENTIALLY CALLED BY BOTH ANALYZED ARRAYS AND THE SWING GUI
    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

    // === === === === === === === === ===
    // MODIFYING ANALYZED ARRAYS (CREATION, DELETION, VISIBILITY)
    // === === === === === === === === ===

    /**
     * Create a new AnalyzedArray in the group with a given capacity and no initial
     * values to copy from.
     * Since there is no initial array of ints provided, a capacity is needed so
     * a new underlying array can be initialized for the AnalyzedArray. This is
     * expected to be used by sorting algorithms for creating temporary space. If
     * the capacity is less than 1 or the name is null or the length of name
     * is zero, then nothing happens and
     * null is returned.
     * 
     * SYNCHRONIZED: this is expected to be called by an AnalyzedArray in a separate
     * sorting thread.
     * 
     * @param capacity the int capacity of the new AnalyzedArray
     * @param name     the String name for the AnalyzedArray to be referenced by
     * @param visible  true if the AnalyzedArray is considered for the global min,
     *                 max, and indexing; false if otherwise
     * @return the new AnalyzedArray configured with the provided information
     */
    public synchronized AnalyzedArray addArray(int capacity, String name, boolean visible) {
        if (capacity < 1 || name == null || name.length() == 0) {
            return null;
        }
        return initNewArray(new AnalyzedArray(capacity, name, this), name, visible);
    }

    /**
     * Create a new AnalyzedArray in the group with initial values provided by a
     * primitive array of ints.
     * The capacity of the new AnalyzedArray is inferred from the length of the
     * provided array. This is expected to be used by the Swing GUI for setting an
     * input array to be sorted. If the array or name is null or the length of name
     * is zero, then nothing happens and null is returned.
     * 
     * SYNCHRONIZED: although this is expected to be called by the main thread in
     * the Swing GUI, it is so infrequently used that the risk of dead-locks is low,
     * so it is best to keep it synchronized to be safe.
     * 
     * @param array   the int array to initialize the AnalyzedArray with
     * @param name    the String name for the AnalyzedArray to be referenced by
     * @param visible true if the AnalyzedArray is considered for the global min,
     *                max, and indexing; false if otherwise
     * @return the new AnalyzedArray configured with the provided information
     */
    public synchronized AnalyzedArray addArray(int[] array, String name, boolean visible) {
        if (array == null || name == null || name.length() == 0) {
            return null;
        }
        return initNewArray(new AnalyzedArray(array, name, this), name, visible);
    }

    /**
     * Create a new AnalyzedArray in the group with initial values provided by a
     * primitive array of ints between the low and high indices, inclusive.
     * The capacity of the new AnalyzedArray is the difference of high and low plus
     * one. This is expected to be used by the Swing GUI for setting an input array
     * for the scatter plot. If low is greater than high, the array or name is null
     * or the length of name is zero, then nothing happens and null is returned.
     * 
     * SYNCHRONIZED: although this is expected to be called by the main thread in
     * the Swing GUI, it is so infrequently used that the risk of dead-locks is low,
     * so it is best to keep it synchronized to be safe.
     * 
     * @param array   the int array to initialize the AnalyzedArray with
     * @param low     the inclusive lower bound of indices to copy from the array
     * @param high    the inclusive upper bound of indices to copy from the array
     * @param name    the String name for the AnalyzedArray to be referenced by
     * @param visible true if the AnalyzedArray is considered for the global min,
     *                max, and indexing; false if otherwise
     * @return the new AnalyzedArray configured with the provided information
     */
    public synchronized AnalyzedArray addArray(int[] array, int low, int high, String name, boolean visible) {
        // NOTE: it is low > high and not low >= high because low == high implies an
        // array of size 1 (valid).
        if (low > high || array == null || name == null || name.length() == 0) {
            return null;
        }
        return initNewArray(new AnalyzedArray(array, low, high, name, this), name, visible);
    }

    /**
     * Register an AnalyzedArray with the group by assigning it a name, visibility
     * status, and performing other needed housekeeping work.
     * The provided AnalyzedArray is NOT directly modified.
     * If array or name is null, then nothing happens and null is returned.
     * 
     * NOT SYNCHRONIZED: expected to be called within the AnalyzedArrayGroup class
     * by another synchronized method.
     * 
     * @param array   the AnalyzedArray to register with the group
     * @param name    the String name for the AnalyzedArray to be referenced by
     * @param visible true if the AnalyzedArray is considered for the global min,
     *                max, and indexing; false if otherwise
     * @return the same, unmodified AnalyzedArray as the one provided
     */
    private AnalyzedArray initNewArray(AnalyzedArray array, String name, boolean visible) {
        if (array == null || name == null) {
            return null;
        }

        arrayMap.put(name, array);
        arrayVisibility.put(name, visible);
        arrayNameInOrder.add(name);

        // Update the minimum and maximum in case they changed.
        scanMin();
        scanMax();

        // Update the global indices.
        globalIndexAppend(name);

        return array;
    }

    /**
     * Removes an AnalyzedArray with the provided name from the group.
     * If the name is null or there is no AnalyzedArray associated with the name,
     * then nothing happens and null is returned.
     * 
     * SYNCHRONIZED: although this is expected to be called by the main thread in
     * the Swing GUI, it is so infrequently used that the risk of dead-locks is low,
     * so it is best to keep it synchronized to be safe.
     * 
     * @param name the String name of the AnalyzedArray to remove
     * @return the AnalyzedArray that was removed
     */
    public synchronized AnalyzedArray removeArray(String name) {
        if (name == null || !arrayMap.containsKey(name)) {
            return null;
        }

        // Removed the array from any containers that hold a reference to it.
        AnalyzedArray array = arrayMap.remove(name);
        arrayVisibility.remove(name);
        arrayNameInOrder.remove(name);

        // Update the global indices.
        globalIndexCalculate();

        // Update the minimum and minimum and maximum in case they changed.
        scanMin();
        scanMax();

        // Since the size of the entire group changed, the last get or set indices may
        // no longer be valid, so they are always set to -1 to be safe.
        globalIndexLastGet = -1;
        globalIndexLastSet = -1;

        // Notify the Swing GUI so the visualization can react.
        if (reportArrayUpdates) {
            visualizer.testCaseArrayUpdated();
        }

        return array;
    }

    /**
     * Sets the visibility of AnalyzedArray.
     * If the AnalyzedArray is visible, then it is included in the global index (is
     * accessible from outside the group and occupies a portion of the indices) and
     * the minimum and maximum. If the name is null or there is no AnalyzedArray
     * associated with the name, then nothing happens.
     * 
     * SYNCHRONIZED: this is expected to be called by an AnalyzedArray in a separate
     * sorting thread to hide temporary arrays if needed.
     * 
     * @param name    the String name of the AnalyzedArray to update
     * @param visible true if the AnalyzedArray is to now be visible and false if
     *                otherwise
     */
    public synchronized void setArrayVisibility(String name, boolean visible) {
        if (name == null || !arrayMap.containsKey(name)) {
            return;
        }

        // If the new visibility is not the same as the current visibility
        // Important check because changing visibility is EXPENSIVE and COSTLY
        if (visible != arrayVisibility.get(name)) {
            arrayVisibility.put(name, visible);

            // Only reset GIM and min/max for VISIBLE arrays, because the general
            // GIM and min/max already includes hidden and non-hidden arrays.
            globalIndexCalculate();
            globalIndexLastGet = -1;
            globalIndexLastSet = -1;

            scanMin();
            scanMax();

            if (reportArrayUpdates) {
                visualizer.testCaseArrayUpdated();
            }
        }
    }

    // === === === === === === === === ===
    // RETRIEVING INFO ABOUT ANALYZED ARRAYS
    // === === === === === === === === ===

    /**
     * Returns true if the AnalyzedArray by the provided name exists.
     * If the name is null, then false is always returned.
     * 
     * SYNCHRONIZED: although this is expected to be called by the main thread in
     * the Swing GUI, it is so infrequently used that the risk of dead-locks is low,
     * so it is best to keep it synchronized to be safe.
     * 
     * @param name the String name of the AnalyzedArray to check for
     * @return true if the AnalyzedArray exists and false if otherwise
     */
    public synchronized boolean hasArray(String name) {
        if (name == null) {
            return false;
        }
        return arrayMap.containsKey(name);
    }

    /**
     * Returns the AnalyzedArray by the name provided.
     * If the name is null or there is no AnalyzedArray associated with the name,
     * then nothing happens and null is returned.
     * 
     * SYNCHRONIZED: expected to be called infrequently by BOTH the sorting thread
     * and the main thread in the Swing GUI, so the risk of dead-locks is low but
     * the risk of strange, de-synchronized behavior is high.
     * 
     * @param name the String name of the AnalyzedArray to retrieve
     * @return the AnalyzedArray of the provided name
     */
    public synchronized AnalyzedArray getArray(String name) {
        if (name == null || !arrayMap.containsKey(name)) {
            return null;
        }
        return arrayMap.get(name);
    }

    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // METHODS CALLED BY ANALYZED ARRAYS
    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

    // === === === === === === === === ===
    // GET/SET
    // === === === === === === === === ===

    /**
     * Called by an AnalyzedArray of the provided name to notify the group that an
     * element in that array was just set.
     * The global set index is updated, the Swing GUI is notified (if
     * reportArrayUpdates is true), and the current thread is slept (if applyDelay
     * is true).
     * 
     * NOT SYNCHRONIZED: expected to be called by a method in an AnalyzedArray that
     * is in a synchronized context, so this method is not synchronized to avoid
     * performance drawbacks or dead-locks.
     * 
     * @param name the String name of the AnalyzedArray that experienced a set
     */
    public void arraySetUpdate(String name) {
        if (name == null || !arrayMap.containsKey(name)) {
            return;
        }

        if (arrayVisibility.get(name)) {
            // The global set index is updated with the local set index of the array, which
            // is made global by adding it by that array's index offset.
            globalIndexLastSet = arrayMap.get(name).getIndexLastSet() + capacityOffset.get(name);
        }

        if (reportArrayUpdates) {
            visualizer.testCaseArrayUpdated();
        }

        if (applyDelay) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Called by an AnalyzedArray of the provided name to notify the group that an
     * element in that array was just retrieved.
     * The global get index is updated, the Swing GUI is notified (if
     * reportArrayUpdates is true), and the current thread is slept (if applyDelay
     * is true).
     * 
     * NOT SYNCHRONIZED: expected to be called by a method in an AnalyzedArray that
     * is in a synchronized context, so this method is not synchronized to avoid
     * performance drawbacks or dead-locks.
     * 
     * @param name the String name of the AnalyzedArray that experienced a get
     */
    public void arrayGetUpdate(String name) {
        if (arrayVisibility.get(name)) {
            // The global get index is updated with the local set index of the array, which
            // is made global by adding it by that array's index offset.
            globalIndexLastGet = arrayMap.get(name).getIndexLastGet() + capacityOffset.get(name);
        }

        if (reportArrayUpdates) {
            visualizer.testCaseArrayUpdated();
        }

        if (applyDelay) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // === === === === === === === === ===
    // MINIMUM/MAXIMUM ELEMENT VALUES
    // === === === === === === === === ===

    /**
     * Called by an AnalyzedArray of the provided name to notify the group that its
     * max value just changed.
     * 
     * NOT SYNCHRONIZED: expected to be called by a method in an AnalyzedArray that
     * is in a synchronized context, so this method is not synchronized to avoid
     * performance drawbacks or dead-locks.
     * 
     * @param name the String name of the AnalyzedArray that just changed its max
     */
    public void arrayMaxUpdate(String name) {
        if (arrayVisibility.get(name)) {
            // If the AnalyzedArray's max is greater than the current global max, then all
            // of the AnalyzedArrays are scanned for a new global maximum.
            if (arrayMap.get(name).getMax() > globalMaxValue) {
                scanMax();
            }
        }
    }

    /**
     * Called by an AnalyzedArray of the provided name to notify the group that its
     * min value just changed.
     * 
     * NOT SYNCHRONIZED: expected to be called by a method in an AnalyzedArray that
     * is in a synchronized context, so this method is not synchronized to avoid
     * performance drawbacks or dead-locks.
     * 
     * @param name the String name of the AnalyzedArray that just changed its min
     */
    public void arrayMinUpdate(String name) {
        if (arrayVisibility.get(name)) {
            // If the AnalyzedArray's min is less than the current global min, then all
            // of the AnalyzedArrays are scanned for a new global minimum.
            if (arrayMap.get(name).getMin() < globalMinValue) {
                scanMin();
            }
        }
    }

    /**
     * Scans all of the AnalyzedArrays in the group to find the smallest minimum
     * value among them and update the global minimum value.
     * 
     * NOT SYNCHRONIZED: expected to be called within the AnalyzedArrayGroup class
     * by another synchronized method.
     */
    private void scanMin() {
        boolean isFirstArray = true;

        for (AnalyzedArray array : arrayMap.values()) {
            // The minimum is updated if an AnalyzedArray is visible and its minimum is less
            // than the current global minimum.
            if (isFirstArray || arrayVisibility.get(array.getName()) && (array.getMin() < globalMinValue)) {
                globalMinValue = array.getMin();
                isFirstArray = false;
            }
        }
    }

    /**
     * Scans all of the AnalyzedArrays in the group to find the largest maximum
     * value among them and update the global maximum value.
     * 
     * NOT SYNCHRONIZED: expected to be called within the AnalyzedArrayGroup class
     * by another synchronized method.
     */
    private void scanMax() {
        boolean isFirstArray = true;

        for (AnalyzedArray array : arrayMap.values()) {
            // The maximum is updated if an AnalyzedArray is visible and its maximum is
            // greater
            // than the current global maximum.
            if (isFirstArray || arrayVisibility.get(array.getName()) && (array.getMax() > globalMaxValue)) {
                globalMaxValue = array.getMax();
                isFirstArray = false;
            }
        }
    }

    // === === === === === === === === ===
    // STATUS OF SORTING ALGORITHM
    // === === === === === === === === ===

    /**
     * Called by a sorting algorithm to notify the group and the Swing GUI that it
     * has finished sorting.
     * 
     * SYNCHRONIZED: this is expected to be called in a separate sorting thread.
     */
    public synchronized void algorithmFinished() {
        globalIndexLastGet = -1;
        globalIndexLastSet = -1;
        visualizer.testCaseFinished();
    }

    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // METHODS CALLED BY SWING GUI
    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

    // === === === === === === === === ===
    // MODIFYING BEHAVIOR OF GROUP
    // === === === === === === === === ===

    /**
     * Updates the length of each delay after a set() and get() in an AnalyzedArray
     * to the provided int in milliseconds.
     * If the delay is less than 0, then nothing happens.
     * 
     * NOT SYNCHRONIZED: expected to only be called by the main thread in the Swing
     * GUI because only it should have control over the sleep delay.
     * 
     * @param delay the new delay in milliseconds
     */
    public void setSleepDelay(int delay) {
        if (delay < 1) {
            return;
        }
        this.delay = delay;
    }

    /**
     * If the provided boolean is true, then the group will incur delays after each
     * set() and get() in an AnalyzedArray; if otherwise, it will not have any
     * delays.
     * 
     * NOT SYNCHRONIZED: expected to only be called by the main thread in the Swing
     * GUI and not the sorting thread.
     * 
     * @param enabled true if delays are enabled and false if otherwise
     */
    public void toggleSleep(boolean enabled) {
        applyDelay = enabled;
    }

    /**
     * If the provided boolean is true, then the group will notify the Swing GUI
     * after each set(), get() and changes to the minimum or maximum of an
     * AnalyzedArray; if otherwise, it will not notify.
     * 
     * NOT SYNCHRONIZED: expected to only be called by the main thread in the Swing
     * GUI and not the sorting thread.
     * 
     * @param enabled true if the Swing GUI should be notified and false if
     *                otherwise
     */
    public void toggleReportDelayUpdates(boolean enabled) {
        reportArrayUpdates = enabled;
    }

    // === === === === === === === === ===
    // ACCESS COUNT
    // === === === === === === === === ===

    /**
     * Return the total access count across all of the AnalyzedArrays as a long
     * value.
     * A long was chosen over an int due to the high likelihood that the access
     * count exceeds that maximum value of the signed 32-bit int (especially for
     * large inputs and slow sorting algorithms).
     * 
     * NOT SYNCHRONIZED: expected to only be called by the main thread in the Swing
     * GUI and not the sorting thread.
     * 
     * @return the long of the total access count
     */
    public long getAccessCount() {
        long totalCount = 0;

        for (AnalyzedArray array : arrayMap.values()) {
            totalCount += array.getAccessCount();
        }

        return totalCount;
    }

    /**
     * Resets the access count of each AnalyzedArray in the group.
     * 
     * NOT SYNCHRONIZED: expected to only be called by the main thread in the Swing
     * GUI and not the sorting thread.
     */
    public void resetAccessCount() {
        for (AnalyzedArray array : arrayMap.values()) {
            array.resetAccessCount();
        }
    }

    // === === === === === === === === ===
    // MINIMUM/MAXIMUM ELEMENT VALUES
    // === === === === === === === === ===

    /**
     * Returns the smallest value of the whole group (across all of the
     * AnalyzedArrays).
     * 
     * NOT SYNCHRONIZED: expected to be FREQUENTLY called by the main thread in the
     * Swing GUI by its visualization code; making this synchronized poses a very
     * high risk for dead-locks and performance issues.
     * 
     * @return the int smallest value of the whole group
     */
    public int getMin() {
        return globalMinValue;
    }

    /**
     * Returns the largest value of the whole group (across all of the
     * AnalyzedArrays).
     * 
     * NOT SYNCHRONIZED: expected to be FREQUENTLY called by the main thread in the
     * Swing GUI by its visualization code; making this synchronized poses a very
     * high risk for dead-locks and performance issues.
     * 
     * @return the int largest value of the whole group
     */
    public int getMax() {
        return globalMaxValue;
    }

    // === === === === === === === === ===
    // GET/SET INDICES
    // === === === === === === === === ===

    /**
     * Returns the global index of the last element that was retrieved in the group
     * (across all of the AnalyzedArrays).
     * 
     * NOT SYNCHRONIZED: expected to be FREQUENTLY called by the main thread in the
     * Swing GUI by its visualization code; making this synchronized poses a very
     * high risk for dead-locks and performance issues.
     * 
     * @return the int index of the last element that was retrieved
     */
    public int getIndexLastGet() {
        return globalIndexLastGet;
    }

    /**
     * Returns the global index of the last element that was set in the group
     * (across all of the AnalyzedArrays).
     * 
     * NOT SYNCHRONIZED: expected to be FREQUENTLY called by the main thread in the
     * Swing GUI by its visualization code; making this synchronized poses a very
     * high risk for dead-locks and performance issues.
     * 
     * @return the int index of the last element that was set
     */
    public int getIndexLastSet() {
        return globalIndexLastSet;
    }

    // === === === === === === === === ===
    // RETRIEVING OTHER INFORMATION
    // === === === === === === === === ===

    /**
     * Returns the value of an element at the global index provided.
     * It does this by:
     * (1) retrieving the corresponding AnalyzedArray for the global index
     * (2) converting the global index to a local index for the AnalyzedArray with
     * the global index offset for that AnalyzedArray
     * 
     * NOT SYNCHRONIZED: expected to be FREQUENTLY called by the main thread in the
     * Swing GUI by its visualization code; making this synchronized poses a very
     * high risk for dead-locks and performance issues.
     * 
     * @param index the int global index for the element to retrieve
     * @return the int value of the element
     */
    public int getExternal(int index) {
        return globalIndexToArray[index].getExternal(index - globalIndexOffsets[index]);
    }

    /**
     * The total number of elements across all of the AnalyzedArrays in the group.
     * 
     * NOT SYNCHRONIZED: expected to be FREQUENTLY called by the main thread in the
     * Swing GUI by its visualization code; making this synchronized poses a very
     * high risk for dead-locks and performance issues.
     * 
     * @return the int total number of elements in the group
     */
    public int size() {
        return globalCapacity;
    }

    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // GLOBAL INDEX METHODS
    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

    /**
     * Updates the globalCapacity, capacityOffset, globalIndexToArray, and
     * globalIndexOffsets for ALL of the AnalyzedArrays:
     * (1) globalCapacity: the total number of elements in the group
     * (2) capacityOffset: given the name of an AnalyzedArray, you can convert a
     * global index to a local index for that AnalyzedArray and vice versa
     * (3) globalIndexToArray: given a global index, you can retrieve its
     * corresponding AnalyzedArray in O(1) time
     * (4) globalIndexOffsets: given a global index, you can retrieve its
     * corresponding index offset in O(1) time
     * 
     * In short, in exchange for high spacial complexity, this method generates
     * arrays that ensure that a global index can be used to retrieve the correct
     * element from the correct AnalyzedArray in O(1) time.
     * 
     * NOT SYNCHRONIZED: expected to be called by synchronized methods in the
     * AnalyzedArrayGroup class and the constructor.
     */
    private void globalIndexCalculate() {
        AnalyzedArray array;
        int currentCap = 0;
        capacityOffset = new HashMap<>();
        globalCapacity = 0;

        /*
         * Calculate the total number of elements of the entire group (global capacity)
         * and generate the index offsets for the AnalyzedArrays in the order they were
         * inserted (leftmost arrays were inserted earliest).
         */
        for (String name : arrayNameInOrder) {
            if (arrayVisibility.get(name)) {
                array = arrayMap.get(name);

                /*
                 * Record the global capacity of the AnalyzedArray, which is the sum of the
                 * capacities of the preceding AnalyzedArrays. This is actually what is added
                 * to a local index to make it global and vice versa.
                 */
                capacityOffset.put(name, globalCapacity);
                globalCapacity += array.size();
            }
        }

        globalIndexOffsets = new int[globalCapacity];
        globalIndexToArray = new AnalyzedArray[globalCapacity];

        for (String name : arrayNameInOrder) {
            if (arrayVisibility.get(name)) {
                array = arrayMap.get(name);

                /*
                 * The idea is that for any possible global index, it will be associated to the
                 * AnalyzedArray that is occupying the region of indices the global index
                 * belongs to, as well as the offset required to convert back and forth between
                 * local and global indices and vice versa.
                 */
                for (int i = currentCap; i < currentCap + array.size(); i++) {
                    globalIndexToArray[i] = array;
                    globalIndexOffsets[i] = currentCap;
                }

                currentCap += array.size();
            }
        }
    }

    /**
     * The same as globalIndexCalculate(), but directed for a single AnalyzedArray
     * of the provided name that was just created/added to the group.
     * Updates the globalIndexOffsets and globalIndexToArray by creating new,
     * larger copies of them (borrowing their existing values) and adding the new
     * values for the new AnalyzedArray.
     * 
     * NOT SYNCHRONIZED: expected to be called by synchronized methods in the
     * AnalyzedArrayGroup class.
     * 
     * @param name the String name of the AnalyzedArray that was just added
     */
    private void globalIndexAppend(String name) {
        if (arrayVisibility.get(name)) {
            AnalyzedArray array = arrayMap.get(name);
            int newCapacity = globalCapacity + array.size();

            // Resize the existing index arrays by making temporary versions of the larger
            // capacity.
            int[] globalIndexOffsetsTemp = new int[newCapacity];
            AnalyzedArray[] globalIndexToArrayTemp = new AnalyzedArray[newCapacity];

            capacityOffset.put(name, globalCapacity);

            // Copy the existing values to the temporary arrays.
            for (int i = 0; i < globalCapacity; i++) {
                globalIndexOffsetsTemp[i] = globalIndexOffsets[i];
                globalIndexToArrayTemp[i] = globalIndexToArray[i];
            }

            // Set the new values for the given AnalyzedArray.
            for (int i = globalCapacity; i < newCapacity; i++) {
                globalIndexOffsetsTemp[i] = globalCapacity;
                globalIndexToArrayTemp[i] = array;
            }

            // Update the references of the index arrays.
            globalIndexOffsets = globalIndexOffsetsTemp;
            globalIndexToArray = globalIndexToArrayTemp;

            globalCapacity += array.size();
        }
    }
}
