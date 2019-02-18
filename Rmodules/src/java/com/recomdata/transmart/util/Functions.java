package com.recomdata.transmart.util;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gustavo
 */
public final class Functions {

    private Functions() {}

    public static <T> List<T> inner(List<?> list1, List<?> list2, Closure<T> closure) {
        int size = list1.size();
        if (size != list2.size()) {
            throw new IllegalArgumentException("Expected both lists to have the same size");
        }

        List<T> ret = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            ret.add(closure.call(list1.get(i), list2.get(i)));
        }

        return ret;
    }
}
