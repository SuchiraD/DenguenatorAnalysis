package jaegers.denguenator.csvreader;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.copyOfRange;

/**
 * Created by suchira on 12/26/16.
 */
public class ReadPopulations {
    private String file = "";
    private CSVReader csvReader = null;

    public ReadPopulations(String file) {
        this.file = file;
        try {
            csvReader = new CSVReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("Check the csv file path");
            e.printStackTrace();
        }
    }

    public int getActualPopulation(String moh) {
        String[] line;
        try {
            while ((line = csvReader.readNext()) != null) {
                if(line[2].equals(moh)) {

                    return Integer.parseInt(line[5]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void close() throws IOException {
        this.csvReader.close();
    }
}
