package jaegers.denguenator.seiranalysis;

import jaegers.denguenator.csvwriter.WriteSEIRAnalysisResults;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static jaegers.denguenator.seiranalysis.GeneralUtils.correlation;

/**
 * Created by suchira on 1/27/17.
 */
public class BestParameters extends SEIRAnalysis{
    private double bestReportingRate=0, gammah, sigmah=0, bestGammah=0, bestSigmah=0,bestCorrelation = 0;

    public BestParameters(String threadName, String mohName, int startWeek, int maxWeeks, Date date, String year) {
        super(threadName, mohName, startWeek, maxWeeks, 0, 0, 0, date, year);
    }

    @Override public synchronized void start() {
        if(getThread() == null) {
            setThread(new Thread(this, getThreadName()));
            getThread().start();
        }
    }

    @Override public void run() {
        boolean daily = false;
        initializeBestValues(daily);

        int maxSize = getMAX_WEEKS() - getSTART_WEEK() + 2;
        int[] sh = new int[maxSize];
        int[] eh = new int[maxSize];
        int[] ih = new int[maxSize];
        int[] rh = new int[maxSize];
        double[] a = new double[maxSize];


        for(double reportingRate = 0.05; reportingRate <= 1; reportingRate+=0.05) {
            setReportingRate(reportingRate);
            for (int infectionDays = 2; infectionDays <= 9; infectionDays++) {
                gammah = 1.0/infectionDays*7;
                setGammah(gammah);
                for (int recoveryDays = 2; recoveryDays <= 9; recoveryDays++) {
                    sigmah = 1.0/recoveryDays*7;
                    setSigmah(sigmah);
                    weeklyMethod();

                    if(getBestSh()[1] == -1) {
                        continue;
                    }

                    double eh_ih_correlation = correlation(getBestEh(), getBestIh());

                    if (eh_ih_correlation >= 1 || eh_ih_correlation <= 0)
                        continue;

                    if(bestCorrelation < eh_ih_correlation) {
                        bestCorrelation = eh_ih_correlation;
                        bestReportingRate = reportingRate;
                        bestGammah = gammah;
                        bestSigmah = sigmah;
/*

                        if(bestCorrelation > 0.5)
                            System.out.println("------------------------------  gammah = " + gammah + ", sigmah = " + sigmah + ", reporting rate = " + reportingRate + "\n");
*/

                        for(int i = 0; i < getBestSh().length; i++) {
                            sh[i] = getBestSh()[i];
                            ih[i] = getBestIh()[i];
                            rh[i] = getBestRh()[i];
                            a[i] = getBestAs()[i];
                        }

                        initializeBestValues(false);
                    }
                }
            }
        }

        synchronized (getMutex()) {
            System.out.println("Best reporting rate for " + getMohName() + "_" + getYear() + " is  .......  " + bestReportingRate);
            System.out.println("Best gammah for " + getMohName() + "_" + getYear() + " is  .......  " + bestGammah);
            System.out.println("Best sigmah for " + getMohName() + "_" + getYear() + " is  .......  " + bestSigmah);
            System.out.println("Best correlation for " + getMohName() + "_" + getYear() + " is  .......  " + bestCorrelation);

            try {
                writeBestProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeBestProperties() throws IOException {
        if(getWriter() == null) {
            setWriter(new WriteSEIRAnalysisResults(getMainFolderName(), getFolderName(), getThreadName(), false, getYear(), getMohName(), getN()));
            getWriter().close();
        }
        List<String> lines = Arrays.asList(
                "reportingRate" + getMohName() + "_" + getYear() + "=" + bestReportingRate,
                "gammah=" + getMohName() + "_" + getYear() + "=" + bestGammah,
                "sigmah=" + getMohName() + "_" + getYear() + "=" + bestSigmah,
                "bestCorrelation=" + getMohName() + "_" + getYear() + "=" + bestCorrelation);
        Path file = Paths.get(getWriter().getMainFolderPath() + "/bestProperties.txt");
        Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

}
