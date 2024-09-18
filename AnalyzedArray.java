/*
 * CSC 345 PROJECT
 * Class:           AnalyzedArray.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A substitute for a traditional int array that is to be directly
 *                  used by sorting algorithms. It actively monitors the access count incurred by
 *                  calls to set() and get() and performs housekeeping tasks for the
 *                  AnalyzedArrayGroup and Swing GUI by tracking the min and max and the index of
 *                  the last-get or last-set element.
 *                  NOTE: THIS CLASS IS DESIGNED TO WORK WITH MULTIPLE THREADS.
 */

public class AnalyzedArray {
    private int[] array;
    private long accessCount;

    private AnalyzedArrayGroup group;
    private String name;

    private int minValue;
    private int maxValue;

    private int indexLastGet;
    private int indexLastSet;

    /**
     * Initializes the AnalyzedArray with a given capacity and no initial values to
     * copy from.
     * Since there is no initial array of ints provided, a capacity is needed so
     * a new underlying array can be initialized for the AnalyzedArray. This is
     * expected to be used by sorting algorithms for creating temporary space. If
     * the capacity is less than 1, then an AnalyzedArray of capacity 1 is created.
     * 
     * @param capacity the int capacity of the new AnalyzedArray
     * @param name     the String name for the AnalyzedArray to be referenced by
     * @param group    the AnalyzedArrayGroup this array is associated with
     */
    public AnalyzedArray(int capacity, String name, AnalyzedArrayGroup group) {
        if (capacity < 1) {
            array = new int[1];
        } else {
            array = new int[capacity];
        }

        initArray(name, group);
    }

    /**
     * Initializes the AnalyzedArray with initial values provided by a primitive
     * array of ints.
     * The capacity of the new AnalyzedArray is inferred from the length of the
     * provided array. This is expected to be used by the Swing GUI for setting an
     * input array to be sorted. If the given array is null, then an AnalyzedArray
     * of capacity 1 is created.
     * 
     * @param otherArray the int array to initialize the AnalyzedArray with
     * @param name       the String name for the AnalyzedArray to be referenced by
     * @param group      the AnalyzedArrayGroup this array is associated with
     */
    public AnalyzedArray(int[] otherArray, String name, AnalyzedArrayGroup group) {
        if (otherArray == null) {
            array = new int[1];
        } else {
            array = new int[otherArray.length];
            for (int i = 0; i < otherArray.length; i++) {
                array[i] = otherArray[i];
            }
        }

        initArray(name, group);
    }

    /**
     * Initializes the AnalyzedArray with initial values provided by a primitive
     * array of ints between the low and high indices, inclusive.
     * The capacity of the new AnalyzedArray is the difference of high and low plus
     * one. This is expected to be used by the Swing GUI for setting an input array
     * for the scatter plot. If the given array is null, low is greater than high or
     * if the indices are out of bounds, then an AnalyzedArray of capacity 1 is
     * created.
     * 
     * @param otherArray the int array to initialize the AnalyzedArray with
     * @param low        the inclusive lower bound of indices to copy from the array
     * @param high       the inclusive upper bound of indices to copy from the array
     * @param name       the String name for the AnalyzedArray to be referenced by
     * @param group      the AnalyzedArrayGroup this array is associated with
     */
    public AnalyzedArray(int[] otherArray, int low, int high, String name, AnalyzedArrayGroup group) {
        // NOTE: it is low > high and not low >= high because low == high implies an
        // array of size 1 (valid).
        if (otherArray == null || low > high || low < 0 || high >= otherArray.length) {
            array = new int[1];
        } else {
            int subsetSize = high - low + 1;
            array = new int[subsetSize];

            for (int i = 0; i < subsetSize; i++) {
                array[i] = otherArray[low + i];
            }
        }

        initArray(name, group);
    }

    /**
     * Update the name and AnalyzedArrayGroup reference and set reasonable default
     * values for the AnalyzedArray.
     * 
     * NOT SYNCHRONIZED: expected to be called by the constructor in the
     * AnalyzedArray class.
     * 
     * @param name  the String name for the AnalyzedArray to be referenced by
     * @param group the AnalyzedArrayGroup this array is associated with
     */
    private void initArray(String name, AnalyzedArrayGroup group) {
        this.name = name;
        this.group = group;
        accessCount = 0;

        minValue = 0;
        maxValue = 0;

        indexLastGet = -1;
        indexLastSet = -1;

        // Scan the min and max WITHOUT notifying the group.
        scanMinMax(false);
    }

    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // METHODS CALLED BY SORTING ALGORITHM
    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

    /**
     * Returns the value of the element at the index.
     * Since this is expected to be called by a sorting algorithm, it updates the
     * access count, updates the last-get index, and notifies the group.
     * 
     * SYNCHRONIZED: expected to be called by the sorting algorithm in the sorting
     * thread.
     * 
     * @param index the int index of the element to retrieve
     * @return the int value of the element
     */
    public synchronized int get(int index) {
        accessCount++;
        indexLastGet = index;
        group.arrayGetUpdate(name);
        return array[index];

    }

    /**
     * Updates the value of the element at the index wit the given value.
     * Since this is expected to be called by a sorting algorithm, it updates the
     * access count, updates the last-set index, and notifies the group.
     * 
     * SYNCHRONIZED: expected to be called by the sorting algorithm in the sorting
     * thread.
     * 
     * @param index the int index of the element to retrieve
     * @param value the new int value of the element
     */
    public synchronized void set(int index, int value) {
        array[index] = value;
        accessCount++;
        indexLastSet = index;
        if (value < minValue || value > maxValue) {
            scanMinMax(true);
        }
        group.arraySetUpdate(name);
    }

    // assumed to be called by synchronized methods, should be thread safe
    /**
     * Scan the int array in the AnalyzedArray for the current minimum and maximum
     * and notify the group if set to do so.
     * 
     * NOT SYNCHRONIZED: expected to be called by synchronized methods in the
     * AnalyzedArray class.
     * 
     * @param notifyGroup if true, the method notifies the group
     */
    private void scanMinMax(boolean notifyGroup) {
        int newMinValue = array[0];
        int newMaxValue = array[0];

        for (int i = 0; i < array.length; i++) {
            int element = array[i];
            if (element > newMaxValue) {
                newMaxValue = element;
            }
            if (element < newMinValue) {
                newMinValue = element;
            }
        }

        // If the new minimum value is different, update the minimum and notify the
        // group (if set to do so).
        if (newMinValue != minValue) {
            minValue = newMinValue;

            if (notifyGroup) {
                group.arrayMinUpdate(name);
            }
        }

        // If the new maximum value is different, update the maximum and notify the
        // group (if set to do so).
        if (newMaxValue != maxValue) {
            maxValue = newMaxValue;

            if (notifyGroup) {
                group.arrayMaxUpdate(name);
            }
        }
    }

    /**
     * Returns the length of the primitive int array of the AnalyzedArray.
     * 
     * SYNCHRONIZED: expected to be called in BOTH the main thread with the Swing
     * GUI and the sorting thread; while this may lead to possible performance or
     * liveliness issues, this is to ensure that the size is retrieved safely.
     * 
     * @return the int length of the primitive int array
     */
    public synchronized int size() {
        return array.length;
    }

    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // METHODS CALLED BY SWING GUI
    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

    // === === === === === === === === ===
    // ACCESS COUNT
    // === === === === === === === === ===

    /**
     * Returns the access count of the AnalyzedArray.
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     * 
     * @return the long access count of the AnalyzedArray.
     */
    public long getAccessCount() {
        return accessCount;
    }

    /**
     * Sets the access count to zero.
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     */
    public void resetAccessCount() {
        accessCount = 0;
    }

    // === === === === === === === === ===
    // MINIMUM/MAXIMUM ELEMENT VALUES
    // === === === === === === === === ===

    /**
     * Return the smallest value in the AnalyzedArray.
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     * 
     * @return the int smallest value in the AnalyzedArray
     */
    public int getMin() {
        return minValue;
    }

    /**
     * Return the largest value in the AnalyzedArray.
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     * 
     * @return the int largest value in the AnalyzedArray
     */
    public int getMax() {
        return maxValue;
    }

    // === === === === === === === === ===
    // GET/SET INDICES
    // === === === === === === === === ===

    /**
     * Return the last index that was last accessed with get().
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     * 
     * @return the int index of the last index that was accessed
     */
    public int getIndexLastGet() {
        return indexLastGet;
    }

    /**
     * Return the last index that was last updated with set().
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     * 
     * @return the int index of the last index that was updated
     */
    public int getIndexLastSet() {
        return indexLastSet;
    }

    // === === === === === === === === ===
    // RETRIEVING OTHER INFORMATION
    // === === === === === === === === ===

    /**
     * Returns the value of the element at the index without increasing the access
     * count, updating the last-get index, or notifying the group.
     * 
     * NOT SYNCHRONIZED: expected to be called FREQUENTLY by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread; this should NOT be called in the
     * sorting thread and making this synchronized may risk dead-locking and
     * performance issues.
     * 
     * @param index the int index of the element to retrieve
     * @return the int value of the element
     */
    public int getExternal(int index) {
        return array[index];
    }

    /**
     * Returns the String name of the AnalyzedArray.
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     * 
     * @return the String name of the AnalyzedArray
     */
    public String getName() {
        return name;
    }

    /**
     * Return true if the AnalyzedArray is sorted in ascending order and false if
     * otherwise.
     * 
     * NOT SYNCHRONIZED: expected to be called by the AnalyzedArrayGroup
     * and the Swing GUI in the main thread.
     * 
     * @return true if sorted and false if otherwise
     */
    public boolean isSorted() {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }
}