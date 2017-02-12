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

    public static double correlation(int[] xs, int[] ys) {
        //TODO: check here that arrays are not null, of the same length etc

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        int n = xs.length;

        for(int i = 0; i < n; ++i) {
            double x = xs[i];
            double y = ys[i];

            sx += x;
            sy += y;
            sxx += x * x;
            syy += y * y;
            sxy += x * y;
        }

        // covariation
        double cov = sxy / n - sx * sy / n / n;
        // standard error of x
        double sigmax = Math.sqrt(sxx / n -  sx * sx / n / n);
        // standard error of y
        double sigmay = Math.sqrt(syy / n -  sy * sy / n / n);

        // correlation is just a normalized covariation
        return cov / sigmax / sigmay;
    }
}
