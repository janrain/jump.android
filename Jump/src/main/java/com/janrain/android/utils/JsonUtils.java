/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2013, Janrain, Inc.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static com.janrain.android.utils.CollectionUtils.sortedSetFromIterator;
import static com.janrain.android.utils.CollectionUtils.sortedUnion;
import static com.janrain.android.utils.LogUtils.throwDebugException;

public class JsonUtils {
    /**
     * Copies source into dest.
     * @param source JSONObject to deep copy, assumed to be acyclic
     * @param dest JSONObject to deep copy source into, assumed to be empty
     */
    public static void deepCopy(JSONObject source, JSONObject dest) {
        SortedSet<String> keys = sortedSetFromIterator((Iterator<String>) source.keys());
        for (String key : keys) {
            // copy each key by hand, copying mutable types, not copying immutable types
            Object val = source.opt(key);
            Object val_;
            if (val instanceof JSONArray) {
                val_ = copyJsonVal(val);
            } else if (val instanceof JSONObject) {
                val_ = copyJsonVal(val);
            } else {
                val_ = val;
            }
            jsonObjectUnsafePut(dest, key, val_);
        }
    }

    /**
     * An unsafe variant of JSONObject.put
     * @param this_ a JSONObject into which to put name and value
     * @param name the name of the
     * @param value must be a JSON value
     * @throws RuntimeException if this_.put(name, value) throws a JSONException
     */
    public static void jsonObjectUnsafePut(JSONObject this_, String name, Object value)
            throws RuntimeException {
        try {
            this_.put(name, value);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * An unsafe variant of JSONArray#put(int, Object) which throws an exception if the former would
     * @param original a JSONArray to put the element into
     * @param i the index at which to put the element
     * @param o the element
     */
    public static void jsonArrayUnsafePut(JSONArray original, int i, Object o)
            throws IllegalArgumentException {
        try {
            original.put(i, o);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * "Zippers" up the left and right params, merging fields by key names.
     * Array fields are concatenated. (Left, then right)
     * Object fields are recursively merged.
     * If primitive fields are contained in both left and right then the value from left
     * is taken and the value from right is discarded.
     *
     * e.g.: {'a':1, 'c':[2]} collapsed with {'b':null, 'c':[1]} yields {'a':1, 'b':null, 'c':[2,1]}
     * @param left an object
     * @param right another object
     * @return the merged contents of left and right
     */
    public static JSONObject collapseJsonObjects(JSONObject left, JSONObject right) {
        if (left == null) return (JSONObject) copyJsonVal(right);

        JSONObject retVal = (JSONObject) copyJsonVal(left);
        if (right == null) return retVal;

        // merge every field from right into the copy
        for (String key : sortedSetFromIterator((Iterator<String>) right.keys())) {
            Object rightVal = right.opt(key);
            Object leftVal = retVal.opt(key);
            if (leftVal == null) jsonObjectUnsafePut(retVal, key, copyJsonVal(rightVal));
            if (leftVal instanceof JSONObject) {
                JSONObject value = collapseJsonObjects(((JSONObject) leftVal), (JSONObject) rightVal);
                jsonObjectUnsafePut(retVal, key, value);
            }

            if (leftVal instanceof JSONArray) jsonArrayAddAll(((JSONArray) leftVal),
                    (JSONArray) rightVal);
        }

        return retVal;
    }

    /**
     * An addAll variant for JSONArray
     * @param destArray an array to add elements to
     * @param sourceArray an array with elements to be added
     */
    public static void jsonArrayAddAll(JSONArray destArray, JSONArray sourceArray) {
        for (int i = 0; i < sourceArray.length(); i++) destArray.put(sourceArray.opt(i));
    }

    /**
     * Copies a JSON value if the value is mutable, or returns the same value if immutable
     * @param val a JSON value
     * @return a copy of val
     * @throws IllegalArgumentException if val was not a JSON value, and could not be copied
     */
    public static Object copyJsonVal(Object val) throws IllegalArgumentException {
        try {
            if (val instanceof JSONObject) {
                return new JSONObject(val.toString());
            } else if (val instanceof JSONArray) {
                return new JSONArray(val.toString());
            } else {
                //everything else is^H^H had better be immutable
                return val;
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     *
     * @param array
     * @return
     */
    public static List<Object> jsonArrayToList(JSONArray array) {
        List<Object> retval = new ArrayList<Object>();

        for (int i = 0; i < array.length(); i++) retval.add(array.opt(i));

        return retval;
    }

    /**
     *
     * @param objects
     * @return
     */
    public static JSONArray jsonArrayFromObjects(Object... objects) {
        JSONArray retval = new JSONArray();
        for (Object o : objects) retval.put(o);
        return retval;
    }


    private static int jsonArrayCompareTo(JSONArray this_, Object other) {
        if (other instanceof JSONArray) {
            return jsonArrayCompareTo(this_, (JSONArray) other);
        } else {
            return this_.getClass().getName().compareTo(other.getClass().getName());
        }
    }

    private static int jsonArrayCompareTo(JSONArray this_, JSONArray otherArray) {
        for (int index = 0; index < this_.length(); index++) {
            Object thisValue = this_.opt(index);

            Object otherVal = otherArray.opt(index);
            if (otherVal == null) return 1;

            int comparison = compareJsonVals(thisValue, otherVal);

            if (comparison != 0) return comparison;
        }

        if (otherArray.length() > this_.length()) return -1;

        return 0;
    }

    /**
     * Compares two values of types found in the set of possible types for JSON values
     * Uses a standard ordering of such values where:
     *  If the types are mismatched the values are compared by type class name string
     *  If the types are primitives of matching type their values are compared with compareTo
     *  If the types are JSONObject their fields are iterated over by the union of the key set and compared
     *    recursively. The first non zero comparison is the return value, or zero is returned if all fields
     *    are equal
     *  If the types are JSONArray their elements are compared in order and the first non zero comparison
     *    is returned, or zero if all fields are equal
     *  If the values are both JSONObject.NULL this method returns 0
     *
     *  Null arguments are not supported.
     *
     *  Note that number types aren't compared if their types are unequal, e.g. 1.0 the Double does not
     *  equal 1 the Integer.
     *
     * @param thisVal one JSON value
     * @param otherVal another JSON value
     * @return 0 if they are equal
     * @throws IllegalArgumentException if either argument is not a JSON value
     */
    public static int compareJsonVals(Object thisVal, Object otherVal) throws IllegalArgumentException {
        Integer comparison = null;
        if (thisVal instanceof JSONArray) {
            comparison = jsonArrayCompareTo((JSONArray) thisVal, otherVal);
        } else if (thisVal instanceof JSONObject) {
            comparison = jsonObjectCompareTo((JSONObject) thisVal, otherVal);
        } else if (thisVal instanceof Double) {
            if (otherVal instanceof Double) comparison = ((Double) thisVal).compareTo((Double) otherVal);
        } else if (thisVal instanceof Long) {
            if (otherVal instanceof Long) comparison = ((Long) thisVal).compareTo((Long) otherVal);
        } else if (thisVal instanceof Integer) {
            if (otherVal instanceof Integer) comparison = ((Integer) thisVal).compareTo((Integer) otherVal);
        } else if (thisVal instanceof Byte) {
            if (otherVal instanceof Byte) comparison = ((Byte) thisVal).compareTo((Byte) otherVal);
        } else if (thisVal instanceof Boolean) {
            if (otherVal instanceof Boolean) comparison = ((Boolean) thisVal).compareTo((Boolean) otherVal);
        } else if (thisVal instanceof String) {
            if (otherVal instanceof String) comparison = ((String) thisVal).compareTo((String) otherVal);
        } else if (thisVal.equals(JSONObject.NULL)) {
            if (otherVal.equals(JSONObject.NULL)) comparison = 0;
        } else {
            throw new IllegalArgumentException("Unexpected type in compareTo for: " + thisVal);
        }

        if (comparison == null) {
            comparison = thisVal.getClass().getName().compareTo(otherVal.getClass().getName());
        }
        return comparison;
    }

    private static int jsonObjectCompareTo(JSONObject this_, Object other) {
        if (other instanceof JSONObject) {
            return jsonObjectCompareTo(this_, (JSONObject) other);
        } else {
            return this_.getClass().getName().compareTo(other.getClass().getName());
        }
    }

    private static int jsonObjectCompareTo(JSONObject this_, JSONObject other) {
        SortedSet<String> this_Keys = sortedSetFromIterator((Iterator<String>) this_.keys());
        SortedSet<String> otherKeys = sortedSetFromIterator((Iterator<String>) other.keys());

        SortedSet<String> unionKeys = sortedUnion(this_Keys, otherKeys);

        for (String k : unionKeys) {
            if (this_.opt(k) == null) return -1;
            if (other.opt(k) == null) return 1;

            int comparison = compareJsonVals(this_.opt(k), other.opt(k));
            if (comparison != 0) return comparison;
        }

        return 0;
    }

    /**
     * I don't even.
     * @param content
     * @param i
     * @return
     */
    public static String unsafeJsonObjectToString(JSONObject content, int i) {
        try {
            return content.toString(i);
        } catch (JSONException e) {
            throwDebugException(new IllegalArgumentException(e));
            return null;
        }
    }

    public static Map<String, Object> jsonToCollection(JSONObject jsonObject) {
        Map<String, Object> retval = new HashMap<String, Object>();
        List keys = CollectionUtils.listFromIterator(jsonObject.keys());
        for (Object key : keys) retval.put((String) key, jsonToCollection(jsonObject.opt((String) key)));
        return retval;
    }

    public static Object jsonToCollection(Object jsonValue) {
        if (jsonValue instanceof JSONObject) {
            return jsonToCollection((JSONObject) jsonValue);
        } else if (jsonValue instanceof JSONArray) {
            return jsonToCollection((JSONArray) jsonValue);
        } else if (jsonValue == JSONObject.NULL) {
            return null;
        } else {
            return jsonValue;
        }
    }

    public static List<Object> jsonToCollection(JSONArray jsonArray) {
        List<Object> retval = new ArrayList<Object>();
        List objects = jsonArrayToList(jsonArray);
        for (Object object : objects) retval.add(jsonToCollection(object));
        return retval;
    }

    public static JSONObject collectionToJson(Map<String, Object> m) {
        JSONObject retval = new JSONObject();
        for (Map.Entry<String, Object> e : m.entrySet()) {
            try {
                retval.put(e.getKey(), collectionToJson(e.getValue()));
            } catch (JSONException jsonException) {
                throwDebugException(new RuntimeException("Unexpected", jsonException));
            }
        }
        return retval;
    }

    public static JSONArray collectionToJson(List l) {
        JSONArray retval = new JSONArray();
        for (Object i : l) retval.put(collectionToJson(i));
        return retval;
    }

    public static Object collectionToJson(Object o) {
        if (o instanceof Map) {
            return collectionToJson(((Map) o));
        } else if (o instanceof List) {
            return collectionToJson(((List) o));
        } else if (o == null) {
            return JSONObject.NULL;
        } else {
            return o;
        }
    }
}
