package jaegers.denguenator.seiranalysis;

/**
 * Created by suchira on 11/20/16.
 */
public class BackPropagation {

    private int[] sh;
    private int[] eh;
    private double[] a;
    private double gammaH;

    public BackPropagation(int[] sh, int[] eh, double[] a, double gammaH) {
        this.sh = sh;
        this.eh = eh;
        this.a = a;
        this.gammaH = gammaH;
    }

    public double partialDifferSbyA(int i, int day) {
        if(i < day || i == 0) {
            return 0;
        }

        if(i == day) {
            return -sh[i-1];
        }

        if (i > day+1) {
            double mult = 1;
            for(int j = day+1; j <= i; j++) {
                mult *= (1-a[j]);
            }

            return (mult*sh[day-1]);
        }

        return 0;
    }

    public double partialDifferEbyA(int i, int day) {
        if(i < day || i == 0) {
            return 0;
        }

        if(i == day) {
            return sh[i-1];
        }

        else {

            return (1 - gammaH) * partialDifferEbyA(i - 1, day) + a[i] * partialDifferSbyA(i - 1, day);
        }
    }

    public double partialDifferShbySh0(int i) {
        if(i == 0) {
            return 1;
        }
        else {
            double mult = 1;
            for(int j = 1; j <= i; j++) {
                mult *= (1-a[j]);
            }

            return mult;
        }
    }

    public double partialDifferEhbyEh0(int i) {
        double ans = Math.pow(1-gammaH, i);

        return ans;
    }

    public double partialDifferEhbySh0(int i) {
        if(i == 0) {
            return 0;
        }
        else if(i == 1) {
            return a[1];
        }
        else {
            double ans = (1-gammaH) * partialDifferEhbySh0(i-1) + a[i] * partialDifferShbySh0(i-1);

            return ans;
        }
    }

    public int[] getSh() {
        return sh;
    }

    public void setSh(int[] sh) {
        this.sh = sh;
    }

    public int[] getEh() {
        return eh;
    }

    public void setEh(int[] eh) {
        this.eh = eh;
    }

    public double[] getA() {
        return a;
    }

    public void setA(double[] a) {
        this.a = a;
    }

    public double getGammaH() {
        return gammaH;
    }

    public void setGammaH(double gammaH) {
        this.gammaH = gammaH;
    }

    public void backPropagateAs(int lastDay) {
        for(int day = lastDay; day > lastDay-7 && day > 0; day--) {
            double partDiffAnswer = 0;
            for(int i = day; i <= lastDay; i++) {
                partDiffAnswer += partialDifferEbyA(i, day);
            }

            if(partDiffAnswer <= 0 || gammaH * partDiffAnswer <= a[day]) {
                a[day] = a[day] + (-gammaH * partDiffAnswer);
            }
        }
    }
}
