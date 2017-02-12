package jaegers.denguenator.seiranalysis;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class App {

    public static void main( String[] args ) throws InterruptedException {
        int startWeek = 1,
                maxWeeks = 51,
                maxError = 100000,
                maxInitIter = 10000,
                maxAIter = 100000;
        String mohName = "MC - Colombo"; // for 2013 and 2014
        String mohName2012 = "MCColombo"; // for 2012
        mohName2012 = mohName;

        String[] mohs = {"Dehiwala", "MC - Colombo", "Kaduwela", "Moratuwa", "Maharagama", "Kollonnawa", "Nugegoda", "Wattala", "Kelaniya", "Boralesgamuwa", "Homagama", "Piliyandala", "Panadura", "MC - Galle"};
//        String[] mohs = {"MC - Colombo", "Dehiwala", "Maharagama", "Panadura", "Moratuwa", "Kaduwela", "Kollonnawa", "Nugegoda", "Kelaniya", "Wattala"};
//        String[] mohs = {"Colombo"};
//        String[] mohs = {"Colombo", "Kandy", "Galle"};
//        String[] mohs = {"Dehiwala", "MC - Colombo"};

        /* Colombo District*/
//        String[] mohs = {"Dehiwala", "Piliyandala", "Homagama", "Kaduwela","Kollonnawa","Maharagama","MC - Colombo", "Moratuwa", "Nugegoda", "Padukka", "Boralesgamuwa", "Hanwella"};
        /* Kandy District*/
//        String[] mohs = {"Akurana","Galagedara","Gampola","Gangawatakorale","Hasalaka","Doluwa","Kundasale","Kurunduwatta","Medadumbara","Poojapitiya","Nawalapitiya","Talatuoya","Ududumbara","Udunuwara","Wattegama","Yatinuwara","Hataraliyadda","Menikhinna","Bambaradeniya"};
        String year = "2014";
        maxInitIter = 200;

        Date date = new Date();

        for (int mohIndex = 0; mohIndex < mohs.length; mohIndex++) {
            mohName = mohs[mohIndex];
            mohName2012 = mohName;
            System.out.println("Starting threads for ---------- " + mohName);

            SEIRAnalysis analysis1= new SEIRAnalysis("thread1", mohName2012, startWeek, maxWeeks, maxError, maxInitIter, maxAIter, date, "2012");
            SEIRAnalysis analysis2;
            SEIRAnalysis analysis3;
            for(int i = 1; i <= maxInitIter && ("NEW").equals(analysis1.getState().toString()); i++) {
                analysis1 = new SEIRAnalysis("thread1", mohName2012, startWeek, maxWeeks, maxError, maxInitIter, maxAIter, date, "2012");
                analysis2 = new SEIRAnalysis("thread2", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter, date, "2013");
                analysis1.start();
                analysis2.start();
                analysis1.join();
                analysis2.join();

                /*SEIRAnalysis test = new SEIRAnalysis("TEST", mohName, 1, 51, maxError, maxInitIter, maxAIter, date, "2014");
                //            SEIRAnalysis test = new SEIRAnalysis("TEST", mohName, 1, 103, maxError, maxInitIter, maxAIter, date, "2013");
                test.start();
                test.join();*/
            }

            SEIRAnalysis test = new SEIRAnalysis("TEST", mohName, 1, 51, maxError, maxInitIter, maxAIter, date, "2014");
//            SEIRAnalysis test = new SEIRAnalysis("TEST", mohName, 1, 103, maxError, maxInitIter, maxAIter, date, "2013");
            test.start();
            test.join();

            System.out.println("----------- Ending threads for ---------- " + mohName);
        }

    }

    public static double findSmallestValue(double[] arr) {
        double smallest = Double.MAX_VALUE;

        for (int index = 0; index < arr.length; index++) {
            if(arr[index] < smallest) {
                smallest = arr[index];
            }
        }

        return smallest;
    }

}
