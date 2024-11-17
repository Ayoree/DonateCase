package com.jodexindustries.donatecase.api.tools;

/*
 * Copyright (c) 2020 Lewys Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.TreeSet;

/**
 * ProbabilityCollection for retrieving random elements based on probability.
 * <br>
 * <br>
 * <b>Selection Algorithm Implementation</b>:
 *
 * <ul>
 * <li>Elements have a "block" of space, sized based on their probability share
 * <li>"Blocks" start from index 1 and end at the total probability of all
 * elements
 * <li>A random number is selected between 1 and the total probability
 * <li>Which "block" the random number falls in is the element that is selected
 * <li>Therefore "block"s with larger probability have a greater chance of being
 * selected than those with smaller probability.
 * </ul>
 *
 * @author Lewys Davies
 * @version 0.8
 *
 * @param <E> Type of elements
 */
public final class ProbabilityCollection<E> {

    private final NavigableSet<ProbabilitySetElement<E>> collection;
    private final SplittableRandom random = new SplittableRandom();

    private double totalProbability;

    /**
     * Construct a new Probability Collection
     */
    public ProbabilityCollection() {
        this.collection = new TreeSet<>(Comparator.comparingDouble(ProbabilitySetElement::getIndex));
        this.totalProbability = 0;
    }

    /**
     * @return Number of objects inside the collection
     */
    public int size() {
        return this.collection.size();
    }

    /**
     * @return True if collection contains no elements, else False
     */
    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    /**
     * @param object object
     * @return True if collection contains the object, else False
     * @throws IllegalArgumentException if object is null
     */
    public boolean contains(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot check if null object is contained in this collection");
        }

        return this.collection.stream().anyMatch(entry -> entry.getObject().equals(object));
    }

    /**
     * @return Iterator over this collection
     */
    public Iterator<ProbabilitySetElement<E>> iterator() {
        return this.collection.iterator();
    }

    /**
     * Add an object to this collection
     *
     * @param object object. Not null.
     * @param probability share. Must be greater than 0.
     *
     * @throws IllegalArgumentException if object is null
     * @throws IllegalArgumentException if probability smaller or equals 0
     */
    public void add(E object, double probability) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot add null object");
        }

        if (probability <= 0) probability = 1;

        ProbabilitySetElement<E> entry = new ProbabilitySetElement<>(object, probability);
        entry.setIndex(this.totalProbability + 1);

        this.collection.add(entry);
        this.totalProbability += probability;
    }

    /**
     * Remove an object from this collection
     *
     * @param object object
     * @return True if object was removed, else False.
     *
     * @throws IllegalArgumentException if object is null
     */
    public boolean remove(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot remove null object");
        }

        Iterator<ProbabilitySetElement<E>> it = this.iterator();
        boolean removed = false;

        // Remove all instances of the object
        while (it.hasNext()) {
            ProbabilitySetElement<E> entry = it.next();
            if (entry.getObject().equals(object)) {
                this.totalProbability -= entry.getProbability();
                it.remove();
                removed = true;
            }
        }

        // Recalculate remaining elements "block" of space: i.e 1-5, 6-10, 11-14
        if (removed) {
            double previousIndex = 0;
            for (ProbabilitySetElement<E> entry : this.collection) {
                previousIndex = entry.setIndex(previousIndex + 1) + (entry.getProbability() - 1);
            }
        }

        return removed;
    }

    /**
     * Remove all objects from this collection
     */
    public void clear() {
        this.collection.clear();
        this.totalProbability = 0;
    }

    /**
     * Get a random object from this collection, based on probability.
     *
     * @return Random object
     *
     * @throws IllegalStateException if this collection is empty
     */
    public E get() {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot get an object out of a empty collection");
        }

        ProbabilitySetElement<E> toFind = new ProbabilitySetElement<>(null, 0);
        toFind.setIndex(this.random.nextDouble(1, this.totalProbability + 1));

        return Objects.requireNonNull(this.collection.floor(toFind)).getObject();
    }

    /**
     * @return Sum of all element's probability
     */
    public double getTotalProbability() {
        return this.totalProbability;
    }

    /**
     * Used internally to store information about a object's state in a collection.
     * Specifically, the probability and index within the collection.
     * <p>
     * Indexes refer to the start position of this element's "block" of space. The
     * space between element "block"s represents their probability of being selected
     *
     * @author Lewys Davies
     *
     * @param <T> Type of element
     */
    public final static class ProbabilitySetElement<T> {
        private final T object;
        private final double probability;
        private double index;

        /**
         * @param object object
         * @param probability share within the collection
         */
        private ProbabilitySetElement(T object, double probability) {
            this.object = object;
            this.probability = probability;
        }

        /**
         * @return The actual object
         */
        public T getObject() {
            return this.object;
        }

        /**
         * @return Probability share in this collection
         */
        public double getProbability() {
            return this.probability;
        }

        // Used internally, see this class's documentation
        private double getIndex() {
            return this.index;
        }

        // Used Internally, see this class's documentation
        private double setIndex(double index) {
            this.index = index;
            return this.index;
        }
    }

}