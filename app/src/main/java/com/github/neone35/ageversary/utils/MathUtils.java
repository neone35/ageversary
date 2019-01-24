package com.github.neone35.ageversary.utils;

public class MathUtils {
    // finds next i number dividable by v number
    public static double roundUp(double i, int v) {
        return Math.ceil(i / v) * v;
    }

    // finds previous i number dividable by v number
    public static double roundDown(double i, int v) {
        return Math.floor(i / v) * v;
    }
}
