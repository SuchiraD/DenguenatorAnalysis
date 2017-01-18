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

        String[] mohs = {/*"Dehiwala", */"MC - Colombo"/*, "Kaduwela", "Moratuwa", "Maharagama", "Kollonnawa", "Nugegoda", "Wattala", "Kelaniya", "Boralesgamuwa", "Homagama", "Piliyandala", "Panadura", "MC-Galle"*/};

        String year = "2014";
        maxInitIter = 1;

        Date date = new Date();

        //String threadName, String mohName, int maxWeeks, int maxError, int maxInitIter, int maxAIter
        /*SEIRAnalysis analysis1 = new SEIRAnalysis("thread1", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter, date, year);
        SEIRAnalysis analysis2 = new SEIRAnalysis("thread2", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis3 = new SEIRAnalysis("thread3", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis4 = new SEIRAnalysis("thread4", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis5 = new SEIRAnalysis("thread5", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis6 = new SEIRAnalysis("thread6", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis7 = new SEIRAnalysis("thread7", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis8 = new SEIRAnalysis("thread8", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis9 = new SEIRAnalysis("thread9", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        SEIRAnalysis analysis10 = new SEIRAnalysis("thread10", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter);
        analysis1.start();
        analysis2.start();
        analysis3.start();
        analysis4.start();
        analysis5.start();
        analysis6.start();
        analysis7.start();
        analysis8.start();
        analysis9.start();
        analysis10.start();
        System.out.println("ALL THREADS STARTED-----------------------");
        try {
            analysis1.join();
            analysis2.join();
            analysis3.join();
            analysis4.join();
            analysis5.join();
            analysis6.join();
            analysis7.join();
            analysis8.join();
            analysis9.join();
            analysis10.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("In MAIN thread ----------------" + analysis1.getState());*/

        for (int mohIndex = 0; mohIndex < mohs.length; mohIndex++) {
            mohName = mohs[mohIndex];
            mohName2012 = mohName;

            SEIRAnalysis analysis1= new SEIRAnalysis("thread1", mohName2012, startWeek, maxWeeks, maxError, maxInitIter, maxAIter, date, "2012");
            SEIRAnalysis analysis2;
            SEIRAnalysis analysis3;
            for(int i = 1; i <= maxInitIter && ("NEW").equals(analysis1.getState().toString()); i++) {
                analysis1 = new SEIRAnalysis("thread1", mohName2012, startWeek, maxWeeks, maxError, maxInitIter, maxAIter, date, "2012");
                analysis2 = new SEIRAnalysis("thread2", mohName, startWeek, maxWeeks, maxError, maxInitIter, maxAIter, date, "2013");
//                analysis3 = new SEIRAnalysis("thread3", mohName, 1, 27, maxError, maxInitIter, maxAIter, date, "2014");
                analysis1.start();
                analysis2.start();
//                analysis3.start();
                analysis1.join();
                analysis2.join();
//                analysis3.join();
            }

            //        for (int i = 1; i<5; i++) {
            SEIRAnalysis test = new SEIRAnalysis("TEST", mohName, 1, 51, maxError, maxInitIter, maxAIter, date, "2014");
            test.start();
            test.join();
            //        }
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
