/*
 * CSC 345 PROJECT
 * Class:           HybridSorts.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     The collection of 5 standalone algorithms and their Merge-based
 *                  hybrid counterparts, as well as regular Merge Sort itself. It
 *                  features Insertion, Selection, Bubble, Heap, Quick, and Merge Sort
 *                  as well as the Merge-Insertion, Merge-Selection, Merge-Bubble,
 *                  Heap-Merge, and Quick-Merge hybrid algorithms. An algorithm is
 *                  ran by passing an AnalyzedArrayGroup in an entry point method
 *                  (has the name of the algorithm itself without "helper" in the name).
 */

public class HybridSorts {
    /*
     * Here is a list of the sorting algorithms organized by order of appearance:
     * (1) Insertion Sort
     * (2) Merge-Insertion
     * (3) Selection Sort
     * (4) Merge-Selection Sort
     * (5) Bubble Sort
     * (6) Merge-Bubble Sort
     * (7) Heap Sort
     * (8) Merge-Heap Sort
     * (9) Quick Sort
     * (10) Merge-Quick Sort
     * (11) Merge Sort
     */

    /**
     * Performs Insertion Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray that is to be sorted
     * @param low   the lower, inclusive bound of indices to sort
     * @param high  the upper, inclusive bound of indices to sort
     */
    private static void helperInsertionSort(AnalyzedArrayGroup group, AnalyzedArray array, int low, int high) {
        if (low >= high) {
            return;
        }

        int elementJ;
        for (int i = low + 1; i <= high; i++) {
            int key = array.get(i);
            int j = i - 1;

            elementJ = array.get(j);

            while (j >= low && key < elementJ) {
                array.set(j + 1, elementJ);
                j--;
                if (j >= low) {
                    elementJ = array.get(j);
                }
            }

            array.set(j + 1, key);

        }
    }

    /**
     * The entry point for a Test Case to perform Insertion Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void insertionSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        helperInsertionSort(group, input, 0, input.size() - 1);
        group.algorithmFinished();
    }

    /**
     * Performs Merge-Insertion on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * This algorithm is a hybrid between Merge Sort and Insertion Sort.
     * It works by recursively halving the given AnalyzedArray into sub-arrays of a
     * size less than the threshold. Once the threshold is reached, the hybrid sorts
     * the sub-array with the regular standalone algorithm Insertion Sort.
     * 
     * @param group     the AnalyzedArrayGroup that the provided AnalyzedArray is
     *                  contained in
     * @param array     the AnalyzedArray that is to be sorted
     * @param temp      the temporary AnalyzedArray for the merge operation
     * @param low       the lower, inclusive bound of indices to sort
     * @param high      the upper, inclusive bound of indices to sort
     * @param threshold the largest size of a sub-array that is sorted with
     *                  Insertion Sort
     */
    private static void helperMergeInsertionSort(AnalyzedArrayGroup group, AnalyzedArray array, AnalyzedArray temp,
            int low, int high, int threshold) {
        if (high - low + 1 <= threshold) {
            // Use insertion sort for small subarrays
            helperInsertionSort(group, array, low, high);
        } else {
            int mid = low + (high - low) / 2;
            helperMergeInsertionSort(group, array, temp, low, mid, threshold);
            helperMergeInsertionSort(group, array, temp, mid + 1, high, threshold);
            helperMerge(group, array, temp, low, mid, high);
        }
    }

    /**
     * The entry point for a Test Case to perform Merge-Insertion Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void mergeInsertionSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        AnalyzedArray temp = group.addArray(input.size(), "temp", false);
        helperMergeInsertionSort(group, input, temp, 0, input.size() - 1, 10);
        group.algorithmFinished();
    }

    /**
     * Performs Selection Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray that is to be sorted
     * @param low   the lower, inclusive bound of indices to sort
     * @param high  the upper, inclusive bound of indices to sort
     */
    public static void helperSelectionSort(AnalyzedArrayGroup group, AnalyzedArray array, int low, int high) {
        for (int i = low; i < high; i++) {
            int minIndex = i;
            for (int j = i + 1; j <= high; j++) {
                if (array.get(j) < array.get(minIndex)) {
                    minIndex = j;
                }
            }

            swap(array, i, minIndex);

        }
    }

    /**
     * The entry point for a Test Case to perform Selection Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void selectionSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        helperSelectionSort(group, input, 0, input.size() - 1);
        group.algorithmFinished();
    }

    /**
     * Performs Merge-Selection on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * This algorithm is a hybrid between Merge Sort and Selection Sort.
     * It works by recursively halving the given AnalyzedArray into sub-arrays of a
     * size less than the threshold. Once the threshold is reached, the hybrid sorts
     * the sub-array with the regular standalone algorithm Selection Sort.
     * 
     * @param group     the AnalyzedArrayGroup that the provided AnalyzedArray is
     *                  contained in
     * @param array     the AnalyzedArray that is to be sorted
     * @param temp      the temporary AnalyzedArray for the merge operation
     * @param low       the lower, inclusive bound of indices to sort
     * @param high      the upper, inclusive bound of indices to sort
     * @param threshold the largest size of a sub-array that is sorted with
     *                  Selection Sort
     */
    private static void helperMergeSelectionSort(AnalyzedArrayGroup group, AnalyzedArray array, AnalyzedArray temp,
            int low, int high, int threshold) {
        if (high - low + 1 <= threshold) {
            // Use insertion sort for small subarrays
            helperSelectionSort(group, array, low, high);
        } else {
            int mid = low + (high - low) / 2;
            helperMergeSelectionSort(group, array, temp, low, mid, threshold);
            helperMergeSelectionSort(group, array, temp, mid + 1, high, threshold);
            helperMerge(group, array, temp, low, mid, high);
        }
    }

    /**
     * The entry point for a Test Case to perform Merge-Selection Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void mergeSelectionSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        AnalyzedArray temp = group.addArray(input.size(), "temp", false);
        helperMergeSelectionSort(group, input, temp, 0, input.size() - 1, 10);
        group.algorithmFinished();
    }

    /**
     * Performs Bubble Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray that is to be sorted
     * @param low   the lower, inclusive bound of indices to sort
     * @param high  the upper, inclusive bound of indices to sort
     */
    public static void helperBubbleSort(AnalyzedArrayGroup group, AnalyzedArray array, int low, int high) {
        boolean continueSwapping = true;

        while (continueSwapping) {
            continueSwapping = false;

            for (int j = low; j < high; j++) {
                int elementJ = array.get(j);
                int elementJNext = array.get(j + 1);

                if (elementJ > elementJNext) {
                    array.set(j + 1, elementJ);
                    array.set(j, elementJNext);

                    continueSwapping = true;
                }
            }
        }
    }

    /**
     * The entry point for a Test Case to perform Bubble Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void bubbleSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        helperBubbleSort(group, input, 0, input.size() - 1);
        group.algorithmFinished();
    }

    /**
     * Performs Bubble-Merge Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * This algorithm is a hybrid between Merge Sort and Bubble Sort.
     * It works by recursively halving the given AnalyzedArray into sub-arrays of a
     * size less than the threshold. Once the threshold is reached, the hybrid sorts
     * the sub-array with the regular standalone algorithm Bubble Sort.
     * 
     * @param group     the AnalyzedArrayGroup that the provided AnalyzedArray is
     *                  contained in
     * @param array     the AnalyzedArray that is to be sorted
     * @param temp      the temporary AnalyzedArray for the merge operation
     * @param low       the lower, inclusive bound of indices to sort
     * @param high      the upper, inclusive bound of indices to sort
     * @param threshold the largest size of a sub-array that is sorted with
     *                  Bubble Sort
     */
    private static void helperBubbleMerge(AnalyzedArrayGroup group, AnalyzedArray array, AnalyzedArray temp, int low,
            int high, int threshold) {
        if (low < high) {
            if (high - low + 1 <= threshold) {
                helperBubbleSort(group, array, low, high);
            } else {
                int mid = (low + high) / 2;
                helperBubbleMerge(group, array, temp, low, mid, threshold);
                helperBubbleMerge(group, array, temp, mid + 1, high, threshold);
                helperMerge(group, array, temp, low, mid, high);
            }
        }
    }

    /**
     * The entry point for a Test Case to perform Bubble-Merge Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void bubbleMergeSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        AnalyzedArray temp = group.addArray(input.size(), "temp", false);
        helperBubbleMerge(group, input, temp, 0, input.size() - 1, 10);
        group.algorithmFinished();
    }

    /**
     * Performs Heap Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray that is to be sorted
     * @param low   the lower, inclusive bound of indices to sort
     * @param high  the upper, inclusive bound of indices to sort
     */
    public static void helperHeapSort(AnalyzedArrayGroup group, AnalyzedArray array, int low, int high) {
        // no low offset
        int rightmostParent = (high - low + 1) / 2 - 1;

        // the "i" is 0 to high - low

        // Build heap (rearrange array)
        for (int i = rightmostParent; i >= 0; i--)
            helperHeapify(group, array, i, low, high);

        // One by one extract an element from heap
        for (int i = high - low; i > 0; i--) {
            // Move current root to end
            swap(array, low, low + i);

            // call max helperHeapify on the reduced heap
            helperHeapify(group, array, 0, low, low + i - 1);

        }
    }

    /**
     * Also known as "sink," recursively swaps a heap element located at index i
     * (relative to the low and high index bounds) with the child of the greatest
     * value.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray where the sink will be performed
     * @param i     the index of the element to sink, relative to low
     * @param low   the lower, inclusive bound of indices to sort
     * @param high  the upper, inclusive bound of indices to sort
     */
    private static void helperHeapify(AnalyzedArrayGroup group, AnalyzedArray array, int i, int low, int high) {
        int largest = low + i; // Initialize largest as root
        int left = low + (2 * i + 1); // left = 2*i + 1
        int right = low + (2 * i + 2); // right = 2*i + 2

        // If left child is larger than root
        if (left <= high && array.get(left) > array.get(largest))
            largest = left;

        // If right child is larger than largest so far
        if (right <= high && array.get(right) > array.get(largest))
            largest = right;

        // If largest is not root
        if (largest != low + i) {
            swap(array, low + i, largest);

            // Recursively helperHeapify the affected sub-tree
            helperHeapify(group, array, largest - low, low, high);
        }
    }

    /**
     * The entry point for a Test Case to perform Heap Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void heapSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        helperHeapSort(group, input, 0, input.size() - 1);
        group.algorithmFinished();
    }

    /**
     * Performs Heap-Merge Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * This algorithm is a hybrid between Merge Sort and Heap Sort.
     * It works by recursively halving the given AnalyzedArray into sub-arrays of a
     * size less than the threshold. Once the threshold is reached, the hybrid sorts
     * the sub-array with the regular standalone algorithm Heap Sort.
     * 
     * @param group     the AnalyzedArrayGroup that the provided AnalyzedArray is
     *                  contained in
     * @param array     the AnalyzedArray that is to be sorted
     * @param temp      the temporary AnalyzedArray for the merge operation
     * @param low       the lower, inclusive bound of indices to sort
     * @param high      the upper, inclusive bound of indices to sort
     * @param threshold the largest size of a sub-array that is sorted with
     *                  Heap Sort
     */
    private static void helperHeapMergeSort(AnalyzedArrayGroup group, AnalyzedArray array, AnalyzedArray temp, int low,
            int high, int threshold) {
        if (high - low + 1 <= threshold) {
            // once array is small enough, use heap sort to sort it
            helperHeapSort(group, array, low, high);
        } else {
            int mid = low + (high - low) / 2;
            helperHeapMergeSort(group, array, temp, low, mid, threshold);
            helperHeapMergeSort(group, array, temp, mid + 1, high, threshold);
            helperMerge(group, array, temp, low, mid, high);
        }
    }

    /**
     * The entry point for a Test Case to perform Heap-Merge Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void heapMergeSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        AnalyzedArray temp = group.addArray(input.size(), "temp", false);
        helperHeapMergeSort(group, input, temp, 0, input.size() - 1, 10);
        group.algorithmFinished();
    }

    /**
     * Performs Quick Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray that is to be sorted
     * @param low   the lower, inclusive bound of indices to sort
     * @param high  the upper, inclusive bound of indices to sort
     */
    public static void helperQuickSort(AnalyzedArrayGroup group, AnalyzedArray array, int low, int high) {
        if (low < high) {
            int pivotIndex = (low + high) / 2;
            int pivot = array.get(pivotIndex);
            int i = low, j = high;

            while (i <= j) {
                while (array.get(i) < pivot)
                    i++;
                while (array.get(j) > pivot)
                    j--;
                if (i <= j) {
                    swap(array, i, j);
                    i++;
                    j--;
                }

            }
            if (low < j)
                helperQuickSort(group, array, low, j);
            if (i < high)
                helperQuickSort(group, array, i, high);
        }
    }

    /**
     * The entry point for a Test Case to perform Quick Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void quickSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        helperQuickSort(group, input, 0, input.size() - 1);
        group.algorithmFinished();
    }

    /**
     * Performs Quick-Merge Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * This algorithm is a hybrid between Merge Sort and Quick Sort.
     * It works by recursively halving the given AnalyzedArray into sub-arrays of a
     * size less than the threshold. Once the threshold is reached, the hybrid sorts
     * the sub-array with the regular standalone algorithm Quick Sort.
     * 
     * @param group     the AnalyzedArrayGroup that the provided AnalyzedArray is
     *                  contained in
     * @param array     the AnalyzedArray that is to be sorted
     * @param temp      the temporary AnalyzedArray for the merge operation
     * @param low       the lower, inclusive bound of indices to sort
     * @param high      the upper, inclusive bound of indices to sort
     * @param threshold the largest size of a sub-array that is sorted with
     *                  Quick Sort
     */
    public static void helperQuickMerge(AnalyzedArrayGroup group, AnalyzedArray array, AnalyzedArray temp, int low,
            int high, int threshold) {
        if (low < high) {
            if (high - low + 1 <= threshold) {
                helperQuickSort(group, array, low, high);
            } else {
                int mid = (low + high) / 2;
                helperQuickMerge(group, array, temp, low, mid, threshold);
                helperQuickMerge(group, array, temp, mid + 1, high, threshold);
                helperMerge(group, array, temp, low, mid, high);
            }
        }
    }

    /**
     * The entry point for a Test Case to perform Quick-Merge Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void quickMergeSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        AnalyzedArray temp = group.addArray(input.size(), "temp", false);
        helperQuickMerge(group, input, temp, 0, input.size() - 1, 10);
        group.algorithmFinished();
    }

    /**
     * Performs Merge Sort on the provided AnalyzedArray between a lower and
     * upper bound of indices, inclusive.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray that is to be sorted
     * @param temp  the temporary AnalyzedArray for the merge operation
     * @param low   the lower, inclusive bound of indices to sort
     * @param high  the upper, inclusive bound of indices to sort
     */
    public static void helperMergeSort(AnalyzedArrayGroup group, AnalyzedArray array, AnalyzedArray temp, int low,
            int high) {
        if (low < high) {
            int mid = low + (high - low) / 2;
            helperMergeSort(group, array, temp, low, mid);
            helperMergeSort(group, array, temp, mid + 1, high);
            helperMerge(group, array, temp, low, mid, high);
        }
    }

    /**
     * Merges two sub-arrays of the given AnalyzedArray into one, final array.
     * The first sub-array is between indices low and mid inclusive and the second
     * sub-array is between indices mid+1 and high inclusive.
     * 
     * @param group the AnalyzedArrayGroup that the provided AnalyzedArray is
     *              contained in
     * @param array the AnalyzedArray with the sub-arrays that are to be merged
     * @param temp  the temporary AnalyzedArray for the merge operation
     * @param low   the first, inclusive index of the first sub-array
     * @param mid   the last, inclusive index of the first sub-array
     * @param high  the last, inclusive index of the second sub-array
     */
    public static void helperMerge(AnalyzedArrayGroup group, AnalyzedArray array, AnalyzedArray temp, int low, int mid,
            int high) {
        int i = low, j = mid + 1;
        for (int k = low; k <= high; k++)
            temp.set(k, array.get(k));

        for (int k = low; k <= high; k++) {
            if (i > mid) {
                array.set(k, temp.get(j));
                j++;
            } else if (j > high) {
                array.set(k, temp.get(i));
                i++;
            } else if (temp.get(i) <= temp.get(j)) {
                array.set(k, temp.get(i));
                i++;
            } else {
                array.set(k, temp.get(j));
                j++;
            }

        }
    }

    /**
     * The entry point for a Test Case to perform Merge Sort.
     * It assumes the input array as an AnalyzedArray with the name "input" in the
     * provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    public static void mergeSort(AnalyzedArrayGroup group) {
        AnalyzedArray input = group.getArray("input");
        AnalyzedArray temp = group.addArray(input.size(), "temp", false);
        helperMergeSort(group, input, temp, 0, input.size() - 1);
        group.algorithmFinished();
    }

    /**
     * Swaps elements at indices i and j in the given AnalyzedArray.
     * 
     * @param array the AnalyzedArray with the elements to swap
     * @param i     the index of the element that is swapped with j
     * @param j     the index of the element that is swapped with i
     */
    public static void swap(AnalyzedArray array, int i, int j) {
        int temp = array.get(i);
        array.set(i, array.get(j));
        array.set(j, temp);
    }
}