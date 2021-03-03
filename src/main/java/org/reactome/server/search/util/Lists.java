package org.reactome.server.search.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lists {
    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        List<T> result = new ArrayList<>(elements.length);
        result.addAll(Arrays.asList(elements));
        return result;
    }
}
