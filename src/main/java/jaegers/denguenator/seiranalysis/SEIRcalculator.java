package jaegers.denguenator.seiranalysis;

import static jaegers.denguenator.seiranalysis.GeneralUtils.initIntArrays;

/**
 * Created by suchira on 11/24/16.
 */
public class SEIRcalculator {
    private int[] sh;
    private int[] eh;
    private int[] ih;
    private int[] rh;
    private double gammah = 0.5;
    private double sigmah = 0.25;

    public SEIRcalculator(int sh0, int eh0,int ih0, int rh0, int startWeek, int endWeek) {
        initArrays(startWeek, endWeek);
        sh[0] = sh0;
        eh[0] = eh0;
        ih[0] = ih0;
        rh[0] = rh0;
    }

    private void initArrays(int startWeek, int endWeek) {
        sh = new int[endWeek - startWeek + 2];
        eh = new int[endWeek - startWeek + 2];
        ih = new int[endWeek - startWeek + 2];
        rh = new int[endWeek - startWeek + 2];

        initIntArrays(sh);
        initIntArrays(eh);
        initIntArrays(ih);
        initIntArrays(rh);
    }

}
