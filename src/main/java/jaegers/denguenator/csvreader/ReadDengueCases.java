package jaegers.denguenator.csvreader;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.copyOfRange;

public class ReadDengueCases {
    private String file = "";
    private CSVReader csvReader = null;

    public ReadDengueCases(String file) {
        this.file = file;
        try {
            csvReader = new CSVReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("Check the csv file path");
            e.printStackTrace();
        }
    }

    public List<Integer> getDengueCases(String moh) {
        String[] line;
        try {
            while ((line = csvReader.readNext()) != null) {
                if(line[1].equals(moh)) {

                    return convertToIntegers(copyOfRange(line, 2, line.length));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<Integer> convertToIntegers(String[] numbers) {
        List<Integer> numberList = new ArrayList(1);
        for (String number : numbers) {
            try {
                numberList.add(Integer.parseInt(number));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return numberList;
    }
}
