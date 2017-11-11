package com.dexmohq.dexml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class StreamUtils {


    public static <T> Collector<T, List<T>, T> single() {
        return Collector.of(
                ArrayList::new,
                List::add,
                (left, right) -> { left.addAll(right); return left; },
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }


}
