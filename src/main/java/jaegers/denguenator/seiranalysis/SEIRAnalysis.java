package jaegers.denguenator.seiranalysis;

import jaegers.denguenator.csvreader.ReadDengueCases;
import jaegers.denguenator.csvreader.ReadPopulations;
import jaegers.denguenator.csvwriter.WriteSEIRAnalysisResults;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jaegers.denguenator.seiranalysis.GeneralUtils.initDoubleArrays;
import static jaegers.denguenator.seiranalysis.GeneralUtils.initIntArrays;
import static jaegers.denguenator.seiranalysis.GeneralUtils.correlation;
import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * Created by suchira on 11/8/16.
 */
public class SEIRAnalysis extends Thread {
    private String year = "2012";
    private int tCount = 0;
    private final int MAX_ITERATIONS = 1000;
    private final int MAX_REPEAT = 5;
    private int MAX_A_ITERATIONS = MAX_ITERATIONS;
    private int MAX_INIT_ITERATIONS = MAX_ITERATIONS;
    private int MAX_ERROR = 50000;
    private double currentError = MAX_ERROR;
    private int MAX_WEEKS = 52;
    private int START_WEEK = 1;

    private String mohName;
    private double gammah = 1.0/2*7;
    private double sigmah = 1.0/8.33333333333333*7;

    private List<Integer> dengueCases = new ArrayList(0);

    private int N = 567272; //population for colombo
    private double reportingRate = 0.04;
    private double ShUpperRate = 0.9;
    private double ShLowerRate = 0.4;
    private int ShUpperRange ;
    private int ShLowerRange ;
    private int EhUpperRange = 5000;
    private int EhLowerRange = 0;
    private int IhUpperRange = 5000;
    private int IhLowerRange = 0;

    private double aUpperValue = 0.8;
    private double aLowerValue = 0.0;

    private double[] tempA = new double[8];
    private int[] tempSh = new int[8];
    private int[] tempEh = new int[8];
    private int[] tempIh = new int[8];
    private int[] tempRh = new int[8];
    private double tempReportingRate;

    private  double bestTotalSqError;
    private double[] bestErrors;

    private double[] bestAs;
    private int[] bestSh;
    private int[] bestEh;
    private int[] bestIh;
    private int[] bestRh;
    private double bestReportingRate;

    private Thread thread;
    private String threadName;
    private Date date;
    private String folderName;
    private String mainFolderName;

    private List<Integer> dengueCasesList;

    private WriteSEIRAnalysisResults writer;

    private DecimalFormat df = new DecimalFormat("#.#############");

    private final static Object mutex = new Object();

//    public SEIRAnalysis() {}

    public SEIRAnalysis(String threadName, String mohName, int startWeek, int maxWeeks, int maxError, int maxInitIter, int maxAIter, Date date, String year) {
        df.setRoundingMode(RoundingMode.CEILING);
        this.mohName = mohName;
        this.threadName = threadName;
        this.mohName = mohName;
        this.START_WEEK = startWeek;
        this.MAX_WEEKS = maxWeeks;
        this.MAX_INIT_ITERATIONS = maxInitIter;
        this.MAX_A_ITERATIONS = maxAIter;
        this.MAX_ERROR = maxError;
        this.date = date;
        this.year = year;
        this.mainFolderName = date.toString();
        this.folderName = mohName;
        getReportedDengueCases(mohName);
        getPopulation(mohName);
    }

    private void getPopulation(String mohName) {
        ReadPopulations readPopulation = new ReadPopulations("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/"
                + "Population/Estimated and Actual Populations in MOH's Srilanka2.csv");
//        ReadPopulations readPopulation = new ReadPopulations("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/"
//                + "Population/populations_districts.csv");
        try {
            this.N = readPopulation.getActualPopulation(mohName);
            readPopulation.close();
            ShUpperRange = (int)(ShUpperRate*N);
            ShLowerRange = (int)(ShLowerRate*N);
            /*EhUpperRange = 900;
            EhLowerRange = 0;
            IhUpperRange = 900;
            IhLowerRange = 0;*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calculateDengueDynamics(double tempA, int day) {
        day -= 1;
        double dSh = -tempA*tempSh[day];
        double dEh = tempA*tempSh[day] - gammah*tempEh[day];
        double dIh = gammah*tempEh[day] - sigmah*tempIh[day];
        double dRh = sigmah*tempIh[day];

        tempSh[day+1] = tempSh[day] + (int)dSh;
        tempEh[day+1] = tempEh[day] + (int)dEh;
        tempIh[day+1] = tempIh[day] + (int)dIh;
        tempRh[day+1] = tempRh[day] + (int)dRh;
    }

    private void getReportedDengueCases(String mohName) {
        ReadDengueCases dengueCasesReader = new ReadDengueCases("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/Dengue/"
                + "dengueCases" + year + ".csv");
//        ReadDengueCases dengueCasesReader = new ReadDengueCases("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/Dengue/"
//                + "dengue" + year + "_districts.csv");
        dengueCasesList = dengueCasesReader.getDengueCases(mohName);
        dengueCasesList.remove(dengueCasesList.size()-1); // Removing the Total of dengue cases
        try {
            dengueCasesReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(MAX_WEEKS > 51) {
            dengueCasesReader = new ReadDengueCases("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/Dengue/"
                    + "dengueCases" + (Integer.parseInt(year) + 1) + ".csv");
//            dengueCasesReader = new ReadDengueCases("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/Dengue/"
//                    + "dengue" + (Integer.parseInt(year) + 1) + "_districts.csv");
            dengueCasesList.addAll(dengueCasesReader.getDengueCases(mohName));
            dengueCasesList.remove(dengueCasesList.size()-1); // Removing the Total of dengue cases
            try {
                dengueCasesReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(MAX_WEEKS > 103) {
            dengueCasesReader = new ReadDengueCases("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/Dengue/"
                    + "dengueCases" + (Integer.parseInt(year) + 2) + ".csv");
//            dengueCasesReader = new ReadDengueCases("/media/suchira/0A9E051F0A9E051F/CSE 2012/Semester 07-08/FYP/Denguenator/Dengunator 2.0/Data/Dengue/"
//                    + "dengue" + (Integer.parseInt(year) + 2) + "_districts.csv");
            dengueCasesList.addAll(dengueCasesReader.getDengueCases(mohName));
            dengueCasesList.remove(dengueCasesList.size()-1); // Removing the Total of dengue cases
            try {
                dengueCasesReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        adjustDengueCases(dengueCasesList);
    }

    private void adjustDengueCases(List<Integer> dengueCasesList) {
        dengueCases.addAll(dengueCasesList.stream().map(dengueCase -> (int) (dengueCase / reportingRate))
                .collect(Collectors.toList()));
    }

    //This should be call after initializing MAX_WEEKS
    public void initializeBestValues(boolean daily) {
        int maxSize ;
        if(daily) {
            maxSize = 7*(MAX_WEEKS-START_WEEK+1) + 1;
        }
        else {
            maxSize = MAX_WEEKS-START_WEEK+2;
        }
        bestErrors = new double[MAX_WEEKS-START_WEEK+1];
        bestTotalSqError = -1;
        bestAs = new double[maxSize];
        bestSh = new int[maxSize];
        bestEh = new int[maxSize];
        bestIh = new int[maxSize];
        bestRh = new int[maxSize];
        initDoubleArrays(bestAs);
        initDoubleArrays(bestErrors);
        initIntArrays(bestSh);
        initIntArrays(bestEh);
        initIntArrays(bestIh);
        initIntArrays(bestRh);
    }


    public boolean hasErrors(double[] errors) {
        int errorCount = 0, count = 0;
        for (Double d:
             errors) {
            if(d >= MAX_ERROR) count++;
            if(count > errorCount) return true;
        }

        return false;
    }

    private double getTotalSquareError(double[] errors) {
        int zeroCount = 0;
        int gtMaxError = 0;
        int sum = 0;
        for (double error : errors) {
            if((int)error == 0) zeroCount++;
            if(error > (double) MAX_ERROR) gtMaxError++;

            sum += error;
        }

        double div = (errors.length+zeroCount);
        div = (div <= 0 ? 0.0001 : div);
        return sum/div;
    }

    @Override public void run() {
        boolean daily = false;
        initializeBestValues(daily);

//            leastSquareMethod();
        //            leastSquareMethod2();
        //            backPropagationMethod();
        //            leastSquareMethodWeekly();
        weeklyMethod();
        if (bestSh[0] != -1 && bestSh[0] != 0) {
            synchronized (mutex) {
                try {
                    writeResults(daily, ("TEST").equals(this.threadName));
                    writeProperties();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    public void leastSquareMethod() {
        for(int initIteration = 1; initIteration <= MAX_INIT_ITERATIONS; initIteration++) {
            if(initIteration % 100 == 0)
                System.out.println(threadName + " in iteration " + initIteration);

            int maxSize = (MAX_WEEKS - START_WEEK + 1)*7+1;
            int[] sh = new int[maxSize];
            int[] eh = new int[maxSize];
            int[] ih = new int[maxSize];
            int[] rh = new int[maxSize];
            double[] a = new  double[maxSize];
            double[] errors = new double[MAX_WEEKS - START_WEEK + 2];
            double totalSqError = MAX_ERROR;

            if(bestSh[0] != -1) {
                sh[0] = bestSh[0];
                eh[0] = bestEh[0];
                ih[0] = bestIh[0];
                rh[0] = bestRh[0];
            }

            else {
                setInitialValues(sh, eh, ih, rh);
            }

            for(int week = START_WEEK; week <= MAX_WEEKS; week++) {
                double error = MAX_ERROR;
                double[] bestTempA = new double[7];
                int[] bestTempSh = new int[7];
                int[] bestTempEh = new int[7];
                int[] bestTempIh = new int[7];
                int[] bestTempRh = new int[7];

                int repeatCount = 0;
                for(int aIteration = 1; aIteration <= MAX_A_ITERATIONS; aIteration++) {
                    int index = (week-START_WEEK)*7;
                    tempSh[0] = sh[index];
                    tempEh[0] = eh[index];
                    tempIh[0] = ih[index];
                    tempRh[0] = rh[index];

                    for(int day=1; day<=7; day++) {
                        tempA[day] = current().nextDouble(aLowerValue, aUpperValue);
                        calculateDengueDynamics(tempA[day], day);
                    }
                    double sqError = Math.pow(
                            ((double)dengueCases.get(week-1) - (gammah * (IntStream.of(tempEh).sum() - tempEh[7]))), 2);

                        /*if(sqError == 0)
                            System.out.println("Sq Error is ZERO in " + threadName + " week is " + week);*/

                    if(sqError < error) {
                        error = sqError;
                        for(int i = 1; i<8; i++) {
                            bestTempA[i-1] = tempA[i];
                            bestTempSh[i-1] = tempSh[i];
                            bestTempEh[i-1] = tempEh[i];
                            bestTempIh[i-1] = tempIh[i];
                            bestTempRh[i-1] = tempRh[i];
                        }
                    }

                    if(aIteration == MAX_A_ITERATIONS && bestTempEh[1]==-1 && repeatCount < MAX_REPEAT) {
                        aIteration = 1;
                        repeatCount++;
                    }

                    if(aIteration == MAX_A_ITERATIONS && week == MAX_WEEKS && initIteration == MAX_INIT_ITERATIONS)
                        System.out.println(threadName + " is COMPLETED");
                }

                for(int i = 1; i<=7; i++) {
                    int index = (week-START_WEEK)*7+i;
                    sh[index] = bestTempSh[i-1];
                    eh[index] = bestTempEh[i-1];
                    ih[index] = bestTempIh[i-1];
                    rh[index] = bestTempRh[i-1];
                    a[index] = bestTempA[i-1];
                }
                errors[week-START_WEEK] = error;
            }

            double tempTotalSqError = getTotalSquareError(errors);
            if(!hasErrors(errors) && (totalSqError > tempTotalSqError)) {
                totalSqError = tempTotalSqError;
                bestTotalSqError = totalSqError;
                for(int i = 0; i < sh.length; i++) {
                    bestSh[i] = sh[i];
                    bestEh[i] = eh[i];
                    bestIh[i] = ih[i];
                    bestRh[i] = rh[i];
                    bestAs[i] = a[i];
                }
                //                    for(int i = 0; i < errors.length; i++) {
                bestErrors = errors;
                //                    }
            }
        }
        System.out.println(threadName + " is finished. Best total square error = " + bestTotalSqError);
        System.out.println(threadName + " is finished. Best total square error = " + getTotalSquareError(bestErrors));
        int i = 1;
        for (Double d:
                getBestErrors()) {
            System.out.println("Errors in - " + threadName + " - Error " + i++ + " = " + d);
        }
    }

    public void leastSquareMethodWeekly() {
        for(int q = 1; q < 100; q++) {
            reportingRate = GeneralUtils.getARandomDouble(0.01, 0.7, 3);
            for(int initIteration = 1; initIteration <= MAX_INIT_ITERATIONS; initIteration++) {
                if(initIteration % 1000 == 0)
                    System.out.println(threadName + " in iteration " + initIteration);

                int maxSize = (MAX_WEEKS - START_WEEK + 1)+1;
                int[] sh = new int[maxSize];
                int[] eh = new int[maxSize];
                int[] ih = new int[maxSize];
                int[] rh = new int[maxSize];
                double[] a = new  double[maxSize];
                double[] errors = new double[MAX_WEEKS - START_WEEK + 1];
                double totalSqError = MAX_ERROR;

                setInitialValues(sh, eh, ih, rh);
                adjustDengueCases(dengueCasesList);

                double lowestSqError = Double.MAX_VALUE;
                for(int week = START_WEEK; week <= MAX_WEEKS; week++) {
                    double error = MAX_ERROR;
                    double bestTempA = 0;
                    int bestTempSh = 0;
                    int bestTempEh = 0;
                    int bestTempIh = 0;
                    int bestTempRh = 0;

                    int repeatCount = 0;
                    for(int aIteration = 1; aIteration <= MAX_A_ITERATIONS; aIteration++) {
                        int index = (week-START_WEEK);
                        tempSh[0] = sh[index];
                        tempEh[0] = eh[index];
                        tempIh[0] = ih[index];
                        tempRh[0] = rh[index];

                        tempA[0] = GeneralUtils.getARandomDouble(aLowerValue, aUpperValue, 5);
                        calculateDengueDynamics(tempA[0], week);

                        double sqError = Math.pow(
                                ((double)dengueCases.get(week-1) - (gammah * tempEh[0])), 2);

                        /*if(sqError == 0)
                            System.out.println("Sq Error is ZERO in " + threadName + " week is " + week);*/

                    /*if(sqError < lowestSqError) {
                        lowestSqError = sqError;
                        System.out.println(lowestSqError);
                    }*/

                        if(sqError < error) {
                            error = sqError;
                            bestTempA = tempA[1];
                            bestTempSh = tempSh[1];
                            bestTempEh = tempEh[1];
                            bestTempIh = tempIh[1];
                            bestTempRh = tempRh[1];
                        }

                    /*if(aIteration == MAX_A_ITERATIONS && bestTempEh[1]==-1 && repeatCount < MAX_REPEAT) {
                        aIteration = 1;
                        repeatCount++;
                    }*/

                        if(aIteration == MAX_A_ITERATIONS && week == MAX_WEEKS && initIteration == MAX_INIT_ITERATIONS)
                            System.out.println(threadName + " is COMPLETED");
                    }

                    int index = (week-START_WEEK)+1;
                    sh[index] = bestTempSh;
                    eh[index] = bestTempEh;
                    ih[index] = bestTempIh;
                    rh[index] = bestTempRh;
                    a[index] = bestTempA;

                    errors[week-START_WEEK] = error;
                }

                double tempTotalSqError = getTotalSquareError(errors);
                if(!hasErrors(errors) && (totalSqError > tempTotalSqError)) {
                    totalSqError = tempTotalSqError;
                    bestTotalSqError = totalSqError;
                    bestReportingRate = reportingRate;
                    for(int i = 0; i < sh.length; i++) {
                        bestSh[i] = sh[i];
                        bestEh[i] = eh[i];
                        bestIh[i] = ih[i];
                        bestRh[i] = rh[i];
                        bestAs[i] = a[i];
                    }

                    for (int i = 0; i < errors.length; i++)
                        bestErrors[i] = errors[i];
                }
            }
        }

        System.out.println(threadName + " is finished. Best total square error = " + bestTotalSqError);
        System.out.println(threadName + " is finished. Best total square error = " + getTotalSquareError(bestErrors));
        int i = 1;
        for (Double d:
                getBestErrors()) {
            System.out.println("Errors in - " + threadName + " - Error " + i++ + " = " + d);
        }
    }

    public void leastSquareMethod2() {
            int maxSize = (MAX_WEEKS - START_WEEK + 1)*7+1;
            int[] sh = new int[maxSize];
            int[] eh = new int[maxSize];
            int[] ih = new int[maxSize];
            int[] rh = new int[maxSize];
            double[] a = new  double[maxSize];
            double[] errors = new double[MAX_WEEKS - START_WEEK + 2];
            double totalSqError = MAX_ERROR;

                sh[0] = 113481;
                eh[0] = 307;
                ih[0] = 8241;
                rh[0] = 445243;

            for(int week = START_WEEK; week <= MAX_WEEKS; week++) {
                double error = MAX_ERROR;
                double[] bestTempA = new double[7];
                int[] bestTempSh = new int[7];
                int[] bestTempEh = new int[7];
                int[] bestTempIh = new int[7];
                int[] bestTempRh = new int[7];

                int repeatCount = 0;
                for(int aIteration = 1; aIteration <= MAX_A_ITERATIONS; aIteration++) {
                    int index = (week-START_WEEK)*7;
                    tempSh[0] = sh[index];
                    tempEh[0] = eh[index];
                    tempIh[0] = ih[index];
                    tempRh[0] = rh[index];

                    for(int day=1; day<=7; day++) {
                        tempA[day] = current().nextDouble(aLowerValue, aUpperValue);
                        calculateDengueDynamics(tempA[day], day);
                    }
                    double sqError = Math.pow(
                            ((double)dengueCases.get(week-1) - (gammah * (IntStream.of(tempEh).sum() - tempEh[7]))), 2);

                        /*if(sqError == 0)
                            System.out.println("Sq Error is ZERO in " + threadName + " week is " + week);*/

                    if(sqError < error) {
                        error = sqError;
                        for(int i = 1; i<8; i++) {
                            bestTempA[i-1] = tempA[i];
                            bestTempSh[i-1] = tempSh[i];
                            bestTempEh[i-1] = tempEh[i];
                            bestTempIh[i-1] = tempIh[i];
                            bestTempRh[i-1] = tempRh[i];
                        }
                    }

                    if(aIteration == MAX_A_ITERATIONS && bestTempEh[1]==-1 && repeatCount < MAX_REPEAT) {
                        aIteration = 1;
                        repeatCount++;
                    }

                }

                for(int i = 1; i<=7; i++) {
                    int index = (week-START_WEEK)*7+i;
                    sh[index] = bestTempSh[i-1];
                    eh[index] = bestTempEh[i-1];
                    ih[index] = bestTempIh[i-1];
                    rh[index] = bestTempRh[i-1];
                    a[index] = bestTempA[i-1];
                }
                errors[week-START_WEEK] = error;
            }

            double tempTotalSqError = getTotalSquareError(errors);
            if(!hasErrors(errors) && (totalSqError > tempTotalSqError)) {
                totalSqError = tempTotalSqError;
                bestTotalSqError = totalSqError;
                for(int i = 0; i < sh.length; i++) {
                    bestSh[i] = sh[i];
                    bestEh[i] = eh[i];
                    bestIh[i] = ih[i];
                    bestRh[i] = rh[i];
                    bestAs[i] = a[i];
                }
                //                    for(int i = 0; i < errors.length; i++) {
                bestErrors = errors;
                //                    }
            }

        System.out.println(threadName + " is finished. Best total square error = " + bestTotalSqError);
        System.out.println(threadName + " is finished. Best total square error = " + getTotalSquareError(bestErrors));
        int i = 1;
        for (Double d:
                getBestErrors()) {
            System.out.println("Errors in - " + threadName + " - Error " + i++ + " = " + d);
        }
    }


    private synchronized void writeResults(boolean daily, boolean isTestFile) throws IOException {
        writer = new WriteSEIRAnalysisResults(mainFolderName, folderName, isTestFile? year+"TEST":year, true, year, mohName, N);
        if (daily)
            writer.writeCSV(IntStream.rangeClosed((START_WEEK-1)*7, MAX_WEEKS*7).toArray(), bestSh, bestEh, bestIh, bestRh, bestAs);
        else {
            writer.writeCSV(IntStream.rangeClosed((START_WEEK-1), MAX_WEEKS).toArray(), bestSh, bestEh, bestIh, bestRh, bestAs);
        }
        writer.close();
    }

    private synchronized void writeProperties() throws IOException {
        if(writer == null) {
            writer = new WriteSEIRAnalysisResults(mainFolderName, folderName, threadName, false, year, mohName, N);
            writer.close();
        }
        List<String> lines = Arrays.asList(
                "aUpperValue="+aUpperValue,
                "aLowerValue="+aLowerValue,
                "ShUpperRange="+ShUpperRange,
                "ShLowerRange="+ShLowerRange,
                "EhUpperRange="+EhUpperRange,
                "EhLowerRange="+EhLowerRange,
                "IhUpperRange="+IhUpperRange,
                "IhLowerRange="+IhLowerRange,
                "reportingRate="+reportingRate,
                "gammah="+gammah,
                "sigmah="+sigmah);
        Path file = Paths.get(writer.getMainFolderPath() + "/properties.txt");
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

    public void backPropagationMethod() {
        int maxSize = (MAX_WEEKS - START_WEEK + 1)*7+1;
        int[] sh = new int[maxSize];
        int[] eh = new int[maxSize];
        int[] ih = new int[maxSize];
        int[] rh = new int[maxSize];
        bestSh = sh;
        bestEh = eh;
        bestIh = ih;
        bestRh = rh;
        double[] a = new  double[maxSize];
        double[] errors = new double[MAX_WEEKS - START_WEEK + 2];
        double totalSqError = MAX_ERROR;
        BackPropagation backPropagation = new BackPropagation(sh, eh, a, gammah);

        setInitialValues(sh, eh, ih, rh);

        do {
            findAs(sh, eh, ih, rh, a, backPropagation);
            findInitialValues(sh, eh, ih, rh, a, backPropagation);
        } while(MAX_ERROR < currentError);
    }

    public void setInitialValues(int[] sh, int[] eh, int[] ih, int[] rh) {
        while ((sh[0] + eh[0] + ih[0] + rh[0]) != N) {
            sh[0] = current().nextInt(ShLowerRange, ShUpperRange + 1);
            eh[0] = current().nextInt(EhLowerRange, EhUpperRange + 1);
            ih[0] = current().nextInt(IhLowerRange, IhUpperRange + 1);
            rh[0] = N - (sh[0] + eh[0] + ih[0]);
        }
    }

    public void findAs(int[] sh, int[] eh, int[] ih, int[] rh, double[] a, BackPropagation backPropagation) {
        double error = MAX_ERROR;

        for(int week = START_WEEK; week <= MAX_WEEKS; week++) {
            System.out.println("Week " + week);
            int index = (week-START_WEEK)*7;

            for(int day = index+1; day <= index + 7; day++) {
                a[day] = current().nextDouble(aLowerValue, aUpperValue);
                calculateDengueDynamics(sh, eh, ih, rh, a, day);
            }

            double sqError = getSqError(eh, week);
            while (sqError > error) {
                backPropagation.backPropagateAs(week*7-1);
                for(int day = index+1; day <= index + 7; day++) {
                    calculateDengueDynamics(sh, eh, ih, rh, a, day);
                }

                sqError = getSqError(eh, week);
            }
        }
    }

    private void findInitialValues(int[] sh, int[] eh, int[] ih, int[] rh, double[] a, BackPropagation backPropagation) {
        double error = MAX_ERROR;

        double sqError = Double.MAX_VALUE;
        int count = 0;
        do {
            setInitialValues(sh, eh, ih, rh);

            int MAX_DAYS = MAX_WEEKS * 7;
            for (int day = 1; day <= MAX_DAYS; day++) {
                calculateDengueDynamics(sh, eh, ih, rh, a, day);
            }
            sqError = getSqError(true);
            currentError = sqError;
            count++;
            if(count % 1000 == 0) {
                System.out.println("In initial values iterator");
            }
        }while (sqError > error);
    }

    private void calculateDengueDynamics(int[] sh, int[] eh, int[] ih, int[] rh, double[] a, int day) {
        double dSh = -a[day]*sh[day-1];
        double dEh = a[day]*sh[day-1] - gammah*eh[day-1];
        double dIh = gammah*eh[day-1] - sigmah*ih[day-1];
        double dRh = sigmah*ih[day-1];

        sh[day] = sh[day-1] + (int)dSh;
        eh[day] = eh[day-1] + (int)dEh;
        ih[day] = ih[day-1] + (int)dIh;
        rh[day] = rh[day-1] + (int)dRh;
    }

    private double getSqError(int[] dArray, int week) {
        int index = (week-START_WEEK)*7;
        double sqError;
        int sum = 0;

        for(int i = index; i < index + 7; i++)
            sum += dArray[index];

        sqError = Math.pow(((double) dengueCases.get(week - 1) - (gammah * sum)), 2);

        return sqError;
    }
    @Override public synchronized void start() {
        tCount++;
//        System.out.println("Starting " + threadName + " for MOH area: " + mohName);
//        System.out.println("Population is: " + this.N);
        if(thread == null) {
            thread = new Thread(this, threadName);
            thread.start();
        }
//        System.out.println(threadName + " started #######################");
    }

    /*public double[] getActualSqErrors() {
        double totalSqError = 0;
        double[] errors = new double[MAX_WEEKS];
        for(int week = 0; week < MAX_WEEKS; week++) {
            double sumEh = 0;
            for(int day = 0; day < 7; day++) {
                sumEh += bestEh[week*7 + day];
            }
            double err = dengueCases.get(week) - gammah*sumEh;
            errors[week] = Math.pow(err, 2);
            totalSqError += errors[week];
        }
        System.out.println(threadName + " Actual total sq error = " + totalSqError/MAX_WEEKS);

        return errors;
    }*/

    public double getSqError(boolean daily) {
        double totalSqError = 0;
        double[] errors = new double[MAX_WEEKS-START_WEEK+1];
        for(int week = START_WEEK; week <= MAX_WEEKS; week++) {
            double sumEh = 0;
            if(daily) {
                for(int day = 0; day < 7; day++) {
                    sumEh += bestEh[(week - START_WEEK)*7 + day];
                }
            }
            else {
                sumEh = bestEh[week-START_WEEK];
            }
            double err = dengueCases.get(week-1) - gammah*sumEh;
            errors[week-START_WEEK] = Math.pow(err, 2);
            totalSqError += errors[week-START_WEEK];
        }

        return totalSqError/(MAX_WEEKS-START_WEEK+1);
    }

    public void weeklyMethod() {
        int maxSize = MAX_WEEKS - START_WEEK + 2;
        int[] sh = new int[maxSize];
        int[] eh = new int[maxSize];
        int[] ih = new int[maxSize];
        int[] rh = new int[maxSize];
        double[] a = new double[maxSize];

        int[] tempSh = new int[maxSize];
        int[] tempEh = new int[maxSize];
        int[] tempIh = new int[maxSize];
        int[] tempRh = new int[maxSize];
        double[] tempA = new double[maxSize];

        setInitialEh(eh, START_WEEK);
        boolean finished = false;
        int count = 0;
        double maxCorrelation = 0;
        /*for (int rounds = 1; rounds <= 3; rounds++) {
            finished = false;
            while (!finished) {
                count++;
                if(count % 1000 == 0) {
//                    System.out.println("Iteration " + count);
                    count = 0;
                    break;
                }
                while ((sh[0] + eh[0] + ih[0] + rh[0]) != N || sh[0]<=0 || eh[0]<0 || ih[0]<=0 || rh[0]<=0) {
                    sh[0] = current().nextInt(ShLowerRange, ShUpperRange + 1);
                    ih[0] = current().nextInt(IhLowerRange, IhUpperRange + 1);
                    rh[0] = N - (sh[0] + eh[0] + ih[0]);
                }

                boolean isErrornous = false;
                for (int week = START_WEEK; week <= MAX_WEEKS; week++) {
                    try {
                        calculateDengueDynamicsWeekly(sh, eh, ih, rh, a, week);
                    } catch (Exception e) {
                        isErrornous = true;
                    }
                }

                if(isErrornous) {
                    sh[0] = 0;
                    ih[0] = 0;
                    rh[0] = 0;
                    continue;
                }

                //            System.out.println("###############################  Number of iterations: " + count);
                finished = true;
            }

            if(!finished) {
                sh[0] = 0;
                ih[0] = 0;
                rh[0] = 0;
                continue;
            }

            double correlation = correlation(eh, ih);

            if(maxCorrelation < correlation){
                maxCorrelation = correlation;
                for(int i = 0; i < sh.length; i++) {
                    tempSh[i] = sh[i];
                    tempIh[i] = ih[i];
                    tempRh[i] = rh[i];
                    tempA[i] = a[i];

                    sh[0] = 0;
                    ih[0] = 0;
                    rh[0] = 0;
                }
            }

        }
*/

        while (!finished) {
            count++;
            if(count % 1000 == 0) {
                //                    System.out.println("Iteration " + count);
                count = 0;
                break;
            }
            while ((sh[0] + eh[0] + ih[0] + rh[0]) != N || sh[0]<=0 || eh[0]<0 || ih[0]<=0 || rh[0]<=0) {
                sh[0] = current().nextInt(ShLowerRange, ShUpperRange + 1);
                ih[0] = current().nextInt(IhLowerRange, IhUpperRange + 1);
                rh[0] = N - (sh[0] + eh[0] + ih[0]);
            }

            boolean isErrornous = false;
            for (int week = START_WEEK; week <= MAX_WEEKS; week++) {
                try {
                    calculateDengueDynamicsWeekly(sh, eh, ih, rh, a, week);
                } catch (Exception e) {
                    isErrornous = true;
                }
            }

            if(isErrornous) {
                sh[0] = 0;
                ih[0] = 0;
                rh[0] = 0;
                continue;
            }

            //            System.out.println("###############################  Number of iterations: " + count);
            finished = true;
        }

        if(!finished) {
            sh[0] = -1;
            ih[0] = -1;
            rh[0] = -1;
        }

        for(int i = 0; i < sh.length; i++) {
            bestSh[i] = sh[i];
            bestEh[i] = eh[i];
            bestIh[i] = ih[i];
            bestRh[i] = rh[i];
            bestAs[i] = a[i];
        }

        /*for(int i = 0; i < sh.length; i++) {
            bestSh[i] = tempSh[i];
            bestEh[i] = eh[i];
            bestIh[i] = tempIh[i];
            bestRh[i] = tempRh[i];
            bestAs[i] = tempA[i];
        }*/

//        System.out.println("Error = " + getSqError(false));
    }

    public void calculateDengueDynamicsWeekly(int[] sh, int[] eh, int[] ih, int[] rh, double[] a, int week)
            throws Exception {
        int refactoredWeek = week - START_WEEK + 1;

        eh[refactoredWeek] = (int) (dengueCases.get(week) / (gammah));

//        a[refactoredWeek-1] = Double.parseDouble(df.format((eh[refactoredWeek] - (1-gammah)*eh[refactoredWeek-1])/sh[refactoredWeek-1]));
        a[refactoredWeek-1] = (eh[refactoredWeek] - (1-gammah)*eh[refactoredWeek-1])/sh[refactoredWeek-1];
        if(a[refactoredWeek-1] < 0 || a[refactoredWeek-1] > 2) {
            throw new Exception("Wrong \"a\"");
        }
        sh[refactoredWeek] = (int) ((1-a[refactoredWeek-1]) * sh[refactoredWeek-1]);
        ih[refactoredWeek] = (int) ((1-sigmah)*ih[refactoredWeek-1] + gammah*eh[refactoredWeek-1]);
        if(ih[refactoredWeek] < 0 || ih[refactoredWeek] > IhUpperRange) {
            throw new Exception("Wrong \"Ih\"");
        }
        rh[refactoredWeek] = (int) (rh[refactoredWeek-1] + sigmah*ih[refactoredWeek-1]);
    }

    public void setInitialEh(int[] eh, int week) {
        eh[0] = (int) (dengueCases.get(week-1) / (gammah));
    }

    public synchronized double[] getBestErrors() {
        return bestErrors;
    }

    public void setBestErrors(double[] bestErrors) {
        this.bestErrors = bestErrors;
    }

    public double[] getBestAs() {
        return bestAs;
    }

    public void setBestAs(double[] bestAs) {
        this.bestAs = bestAs;
    }

    public int[] getBestSh() {
        return bestSh;
    }

    public void setBestSh(int[] bestSh) {
        this.bestSh = bestSh;
    }

    public int[] getBestEh() {
        return bestEh;
    }

    public void setBestEh(int[] bestEh) {
        this.bestEh = bestEh;
    }

    public int[] getBestIh() {
        return bestIh;
    }

    public void setBestIh(int[] bestIh) {
        this.bestIh = bestIh;
    }

    public int[] getBestRh() {
        return bestRh;
    }

    public void setBestRh(int[] bestRh) {
        this.bestRh = bestRh;
    }


    public double getGammah() {
        return gammah;
    }

    public void setGammah(double gammah) {
        this.gammah = gammah;
    }

    public double getSigmah() {
        return sigmah;
    }

    public void setSigmah(double sigmah) {
        this.sigmah = sigmah;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int gettCount() {
        return tCount;
    }

    public void settCount(int tCount) {
        this.tCount = tCount;
    }

    public int getMAX_ITERATIONS() {
        return MAX_ITERATIONS;
    }

    public int getMAX_REPEAT() {
        return MAX_REPEAT;
    }

    public int getMAX_A_ITERATIONS() {
        return MAX_A_ITERATIONS;
    }

    public void setMAX_A_ITERATIONS(int MAX_A_ITERATIONS) {
        this.MAX_A_ITERATIONS = MAX_A_ITERATIONS;
    }

    public int getMAX_INIT_ITERATIONS() {
        return MAX_INIT_ITERATIONS;
    }

    public void setMAX_INIT_ITERATIONS(int MAX_INIT_ITERATIONS) {
        this.MAX_INIT_ITERATIONS = MAX_INIT_ITERATIONS;
    }

    public int getMAX_ERROR() {
        return MAX_ERROR;
    }

    public void setMAX_ERROR(int MAX_ERROR) {
        this.MAX_ERROR = MAX_ERROR;
    }

    public double getCurrentError() {
        return currentError;
    }

    public void setCurrentError(double currentError) {
        this.currentError = currentError;
    }

    public int getMAX_WEEKS() {
        return MAX_WEEKS;
    }

    public void setMAX_WEEKS(int MAX_WEEKS) {
        this.MAX_WEEKS = MAX_WEEKS;
    }

    public int getSTART_WEEK() {
        return START_WEEK;
    }

    public void setSTART_WEEK(int START_WEEK) {
        this.START_WEEK = START_WEEK;
    }

    public String getMohName() {
        return mohName;
    }

    public void setMohName(String mohName) {
        this.mohName = mohName;
    }

    public List<Integer> getDengueCases() {
        return dengueCases;
    }

    public void setDengueCases(List<Integer> dengueCases) {
        this.dengueCases = dengueCases;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public double getReportingRate() {
        return reportingRate;
    }

    public void setReportingRate(double reportingRate) {
        this.reportingRate = reportingRate;
    }

    public double getShUpperRate() {
        return ShUpperRate;
    }

    public void setShUpperRate(double shUpperRate) {
        ShUpperRate = shUpperRate;
    }

    public double getShLowerRate() {
        return ShLowerRate;
    }

    public void setShLowerRate(double shLowerRate) {
        ShLowerRate = shLowerRate;
    }

    public int getShUpperRange() {
        return ShUpperRange;
    }

    public void setShUpperRange(int shUpperRange) {
        ShUpperRange = shUpperRange;
    }

    public int getShLowerRange() {
        return ShLowerRange;
    }

    public void setShLowerRange(int shLowerRange) {
        ShLowerRange = shLowerRange;
    }

    public int getEhUpperRange() {
        return EhUpperRange;
    }

    public void setEhUpperRange(int ehUpperRange) {
        EhUpperRange = ehUpperRange;
    }

    public int getEhLowerRange() {
        return EhLowerRange;
    }

    public void setEhLowerRange(int ehLowerRange) {
        EhLowerRange = ehLowerRange;
    }

    public int getIhUpperRange() {
        return IhUpperRange;
    }

    public void setIhUpperRange(int ihUpperRange) {
        IhUpperRange = ihUpperRange;
    }

    public int getIhLowerRange() {
        return IhLowerRange;
    }

    public void setIhLowerRange(int ihLowerRange) {
        IhLowerRange = ihLowerRange;
    }

    public double getaUpperValue() {
        return aUpperValue;
    }

    public void setaUpperValue(double aUpperValue) {
        this.aUpperValue = aUpperValue;
    }

    public double getaLowerValue() {
        return aLowerValue;
    }

    public void setaLowerValue(double aLowerValue) {
        this.aLowerValue = aLowerValue;
    }

    public double[] getTempA() {
        return tempA;
    }

    public void setTempA(double[] tempA) {
        this.tempA = tempA;
    }

    public int[] getTempSh() {
        return tempSh;
    }

    public void setTempSh(int[] tempSh) {
        this.tempSh = tempSh;
    }

    public int[] getTempEh() {
        return tempEh;
    }

    public void setTempEh(int[] tempEh) {
        this.tempEh = tempEh;
    }

    public int[] getTempIh() {
        return tempIh;
    }

    public void setTempIh(int[] tempIh) {
        this.tempIh = tempIh;
    }

    public int[] getTempRh() {
        return tempRh;
    }

    public void setTempRh(int[] tempRh) {
        this.tempRh = tempRh;
    }

    public double getTempReportingRate() {
        return tempReportingRate;
    }

    public void setTempReportingRate(double tempReportingRate) {
        this.tempReportingRate = tempReportingRate;
    }

    public double getBestReportingRate() {
        return bestReportingRate;
    }

    public void setBestReportingRate(double bestReportingRate) {
        this.bestReportingRate = bestReportingRate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getMainFolderName() {
        return mainFolderName;
    }

    public void setMainFolderName(String mainFolderName) {
        this.mainFolderName = mainFolderName;
    }

    public List<Integer> getDengueCasesList() {
        return dengueCasesList;
    }

    public void setDengueCasesList(List<Integer> dengueCasesList) {
        this.dengueCasesList = dengueCasesList;
    }

    public WriteSEIRAnalysisResults getWriter() {
        return writer;
    }

    public void setWriter(WriteSEIRAnalysisResults writer) {
        this.writer = writer;
    }

    public DecimalFormat getDf() {
        return df;
    }

    public void setDf(DecimalFormat df) {
        this.df = df;
    }

    public static Object getMutex() {
        return mutex;
    }


    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public double getBestTotalSqError() {
        return bestTotalSqError;
    }

    public void setBestTotalSqError(double bestTotalSqError) {
        this.bestTotalSqError = bestTotalSqError;
    }

}
