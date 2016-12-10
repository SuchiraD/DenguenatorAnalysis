package jaegers.denguenator.csvwriter;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by suchira on 11/9/16.
 */
public class WriteSEIRAnalysisResults {
    private String[] header = {"day","best.sh","best.eh","best.ih","best.rh","best.a"};

    private String filePath = "results/";
    private String fileNamePrefix = "SEIRAnalysis";
    private String fileName;
    private CSVWriter csvWriter;
    private static String folderPath;

    public WriteSEIRAnalysisResults(String fileNameSuffix) throws IOException {
        Date date = new Date();
        this.folderPath = filePath+date.toString() + "/";
        new File(folderPath).mkdir();
        this.fileName = folderPath +fileNamePrefix+fileNameSuffix + /*date.toString() +*/ ".csv";
        csvWriter = new CSVWriter(new FileWriter(fileName));
    }

    private List<String[]> prepareEntries(int[] days, int[] sh, int[] eh, int[] ih, int[] rh, double[] a) {
        List<String[]> entries = new ArrayList<>(50);
        for(int index = 0; index < days.length; index++) {
            String[] row = {Integer.toString(days[index]),
                    Integer.toString(sh[index]),
                    Integer.toString(eh[index]),
                    Integer.toString(ih[index]),
                    Integer.toString(rh[index]),
                    Double.toString(a[index])
            };
            entries.add(row);
        }

        return entries;
    }

    public void writeCSV(int[] days, int[] sh, int[] eh, int[] ih, int[] rh, double[] a) {
        List<String[]> entries = prepareEntries(days, sh, eh, ih, rh, a);
        entries.add(0, header);
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

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
