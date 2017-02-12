package jaegers.denguenator.csvwriter;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by suchira on 11/9/16.
 */
public class WriteSEIRAnalysisResults {
    private String[] header = {"day","best.sh","best.eh","best.ih","best.rh","best.a", "year", "moh_name", "population"};

    private String filePath = "results/";
    private String fileNamePrefix = "SEIRAnalysis";
    private String fileName;
    private CSVWriter csvWriter;
    private static String mainFolderPath;
    private static String subFolderPath;
    private static boolean lock = false;
    private static boolean isSubDirectoryExists = false;
    private static boolean isMainDirectoryExists = false;
    private String year;
    private String mohName;
    private int population;

    public WriteSEIRAnalysisResults(String mainFolderName, String folderName, String fileNameSuffix, boolean append, String year, String mohName, int population) throws IOException {
        this.year = year;
        this.mohName = mohName;
        this.population = population;
        this.mainFolderPath = filePath + mainFolderName + "/";
        File directory = new File(mainFolderPath);
        isMainDirectoryExists = directory.exists();
        if(!isMainDirectoryExists) {
            directory.mkdir();

        }

        this.subFolderPath = filePath + mainFolderName + "/" + folderName + "/";
        directory = new File(subFolderPath);
        isSubDirectoryExists = directory.exists();
        if(!isSubDirectoryExists) {
            directory.mkdir();

        }

        this.fileName = subFolderPath +fileNamePrefix+fileNameSuffix + ".csv";
        File file = new File(fileName);
        if(!file.exists()) {
            csvWriter = new CSVWriter(new FileWriter(fileName, append));
            csvWriter.writeNext(header);
            csvWriter.flush();
        }
        csvWriter = new CSVWriter(new FileWriter(fileName, append));
    }

    private synchronized List<String[]> prepareEntries(int[] days, int[] sh, int[] eh, int[] ih, int[] rh, double[] a) {
        List<String[]> entries = new ArrayList<>(50);
        for(int index = 0; index < days.length; index++) {
            String[] row = {days[index] > 52 ? (days[index] > 104 ? Integer.toString(days[index]-104):Integer.toString(days[index]-52)) : Integer.toString(days[index]),
                    Integer.toString(sh[index]),
                    Integer.toString(eh[index]),
                    Integer.toString(ih[index]),
                    Integer.toString(rh[index]),
                    Double.toString(a[index]),
                    days[index] > 52 ? (days[index] > 104 ? Integer.toString(Integer.parseInt(year)+2) : Integer.toString(Integer.parseInt(year)+1)) : year,
                    mohName,
                    ""+population+""
            };
            entries.add(row);
            if(days[index] == 52) {
                String[] nextYearFirstRow = {Integer.toString(days[index]-52),
                        Integer.toString(sh[index]),
                        Integer.toString(eh[index]),
                        Integer.toString(ih[index]),
                        Integer.toString(rh[index]),
                        Double.toString(a[index]),
                        Integer.toString(Integer.parseInt(year)+1),
                        mohName,
                        ""+population+""
                };
                entries.add(nextYearFirstRow);
            }
/*
            if(days[index] == 104) {
                String[] nextYearFirstRow = {Integer.toString(days[index]-104),
                        Integer.toString(sh[index]),
                        Integer.toString(eh[index]),
                        Integer.toString(ih[index]),
                        Integer.toString(rh[index]),
                        Double.toString(a[index]),
                        Integer.toString(Integer.parseInt(year)+2),
                        mohName,
                        ""+population+""
                };
                entries.add(nextYearFirstRow);
            }*/
        }

        return entries;
    }

    public synchronized void writeCSV(int[] days, int[] sh, int[] eh, int[] ih, int[] rh, double[] a) {
        List<String[]> entries = prepareEntries(days, sh, eh, ih, rh, a);
        csvWriter.writeAll(entries);
    }

    public void close() throws IOException {
        csvWriter.close();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileNameSuffix() {
        return fileNamePrefix;
    }

    public void setFileNameSuffix(String fileNameSuffix) {
        this.fileNamePrefix = fileNameSuffix;
    }

    public String getFileName() {
        return fileName;
    }

    public CSVWriter getCsvWriter() {
        return csvWriter;
    }

    public static String getMainFolderPath() {
        return mainFolderPath;
    }

    public static void setMainFolderPath(String mainFolderPath) {
        WriteSEIRAnalysisResults.mainFolderPath = mainFolderPath;
    }

    public static String getSubFolderPath() {
        return subFolderPath;
    }

    public static void setSubFolderPath(String subFolderPath) {
        WriteSEIRAnalysisResults.subFolderPath = subFolderPath;
    }
}
