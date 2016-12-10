package jaegers.denguenator.seiranalysis;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by suchira on 11/24/16.
 */
public class GeneralUtils {

    public static void initDoubleArrays(double[] dArray) {
        for (int i = 0; i < dArray.length; i++) {
            dArray[i] = -1;
        }
    }

    public static void initIntArrays(int[] intArray) {
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = -1;
        }
    }

    public static double getARandomDouble(double lower, double upper, int decimalPoints) {
        double pow = Math.pow(10, decimalPoints);
        long tempLower = Math.round(lower * pow);
        long tempUpper = Math.round(upper * pow);

        long nextLong = ThreadLocalRandom.current().nextLong(tempLower, tempUpper);

        return nextLong/pow;
    }
}
