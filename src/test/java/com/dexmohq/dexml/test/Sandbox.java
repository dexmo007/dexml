package com.dexmohq.dexml.test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Sandbox {


    public static void main(String[] args) {
        final int[] ints = new int[7];
        ints[3] = 8;
        final String[] strings = new String[6];
        strings[3] = "Hi";
        System.out.println(Array.get(ints, 3));
        System.out.println(Array.get(strings, 3));
    }
}
