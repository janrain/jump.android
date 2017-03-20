/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package com.janrain.android.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.reflect.Array.newInstance;

/**
 * @internal
 *
 * @class CollectionUtils
 **/
public final class CollectionUtils {
    /**
     * Loosey goosey variant of emptiness checking
     * @param list a list or null
     * @return true if there are no elements in list
     */
    public static boolean isEmpty(List<?> list) {
        return ((list == null) || (list.size() < 1));
    }

    /**
     * Constructs a SortedSet&lt;T> with all the elements available from i
     * @param i an iterator to pull elements from
     * @param <T> The type of the elements
     * @return a SortedSet of the elements
     */
    public static <T> SortedSet<T> sortedSetFromIterator(Iterator<T> i) {
        SortedSet<T> retval = new TreeSet<T>();
        while (i.hasNext()) retval.add(i.next());
        return retval;
    }

    public static <T> List<T> listFromIterator(Iterator<T> i) {
        List<T> retval = new ArrayList<T>();
        while (i.hasNext()) retval.add(i.next());
        return retval;
    }

    /**
     * Constructs the sorted union of two sets
     * @param a
     * @param b
     * @return the sorted union
     */
    public static <T> SortedSet<T> sortedUnion(Set<T> a, Set<T> b) {
        SortedSet<T> retVal = new TreeSet<T>(a);
        retVal.addAll(b);
        return retVal;
    }

    public static String collectionToHumanReadableString(Map<String, Object> messages) {
        return JsonUtils.collectionToJson(messages).toString();
    }

    /**
     * A first-class function stand-in
     * @param <L> the return type of the function
     * @param <R> the parameter type of the function
     */
    public static interface Function<L, R> {
        /**
         * The function's implementation
         * @param arg
         * @return the function's evaluation on arg
         */
        L operate(R arg);
    }

    /**
     * Maps a function f onto the values of a map map. Returns a new map with with the result.
     * @param map
     *      A map onto which to apply f to the values of
     * @param f
     *      A function mapped onto the values of map
     * @param <K>
     *      The key type for map
     * @param <L>
     *      The left hand side type of f
     * @param <R>
     *      The right hand side type of f
     * @return
     *      A new map&gt;K, L> with with the result.
     */
    public static <K, L, R> HashMap<K, L> map(Map<K, R> map, Function<L, R> f) {
        HashMap<K, L> retMap = new HashMap<K, L>();

        for (Map.Entry<K, R> e : map.entrySet()) retMap.put(e.getKey(), f.operate(e.getValue()));

        return retMap;
    }

    //public static <K, R> ArrayList<R> collectValues(Map<K, R> map, Function<Boolean, Map.Entry> f) {
    //    ArrayList<R> retList = new ArrayList<R>();
    //    for (Map.Entry<K, R> e : map.entrySet()) if (f.operate(e)) retList.add(e.getValue());
    //    return retList;
    //}

    public static <K, R> HashMap<K, R> filter(Map<K, R> map, Function<Boolean, Map.Entry> f) {
        HashMap<K, R> retMap = new HashMap<K, R>();
        for (Map.Entry<K, R> e : map.entrySet()) if (f.operate(e)) retMap.put(e.getKey(), e.getValue());
        return retMap;
    }

    /**
     * Maps a function f onto the values of a list list. Returns a new list with with the result.
     * @param list
     *      A list onto which to apply f to the values of
     * @param f
     *      A function mapped onto the values of list
     * @param <L>
     *      The left hand side type of f
     * @param <R>
     *      The right hand side type of f
     * @return
     *      A new list&gt;L> with the result
     */
    public static <L, R> ArrayList<L> map(List<R> list, Function<L, R> f) {
        ArrayList<L> retList = new ArrayList<L>();

        for (R e : list) retList.add(f.operate(e));

        return retList;
    }

    /**
     * Attempts to construct a new collection of the same class as collection with elements as f mapped onto
     * collection. If that class cannot be instantiated via Class#newInstance() then an ArrayList is
     * constructed to hold the return value
     *
     * @param collection the collection to map f onto
     * @param f a Function to map onto collection
     * @param <L> the generic type of the constructed collection
     * @param <R> the generic type of collection
     * @return the newly constructed collection
     */
    public static <L, R> Collection<L> map(Collection<R> collection, Function<L, R> f) {
        Collection<L> retCollection;
        try {
            retCollection = collection.getClass().newInstance();
        } catch (InstantiationException ignore) {
            retCollection = new ArrayList<L>();
        } catch (IllegalAccessException ignore) {
            retCollection = new ArrayList<L>();
        }

        for (R e : collection) retCollection.add(f.operate(e));

        return retCollection;
    }

    /**
     * Maps a function f onto the values of a array array. Returns a new array of the same type
     * with with the result.
     * @param array
     *      A array onto which to apply f to the values of
     * @param f
     *      A function mapped onto the values of array
     * @param <T>
     *      The right and left hand side type of f
     * @return
     *      A new T[] with the result
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] map(T[] array, Function<T, T> f) {
        T[] retArray = (T[]) newInstance(array.getClass().getComponentType(), array.length);

        int i = 0;
        for (T e : array) retArray[i++] = (f.operate(e));

        return retArray;
    }
}