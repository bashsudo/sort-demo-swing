/*
 * CSC 345 PROJECT
 * Class:           Algorithm.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A functional interface that maps an Algorithm object to a reference
 *                  to the entry point method for a sorting algorithm in HybridSorts.
 *                  This is so that the HybridSorts method can be passed around like an
 *                  object instead of needing switch-case statements that matches an
 *                  algorithm name to a HybridSorts method, etc.
 */

@FunctionalInterface
public interface Algorithm {

    /**
     * When mapped to the entry-point method for a sorting algorithm, sorts the
     * "input" AnalyzedArray of the provided AnalyzedArrayGroup.
     * 
     * @param group the AnalyzedArrayGroup with the input array to sort
     */
    void sort(AnalyzedArrayGroup group);
}