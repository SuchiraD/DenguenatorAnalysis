package jaegers.denguenator.seiranalysis;

import java.util.Date;

/**
 * Created by suchira on 1/27/17.
 */
public class FindBestParameters {
    public static void main( String[] args ) throws InterruptedException {
        int startWeek = 1,
                maxWeeks = 52;

        //        String[] mohs = {"Dehiwala", "MC - Colombo", "Kaduwela", "Moratuwa", "Maharagama", "Kollonnawa", "Nugegoda", "Wattala", "Kelaniya", "Boralesgamuwa", "Homagama", "Piliyandala", "Panadura", "MC - Galle"};
//                String[] mohs = {"Maharagama", "Panadura", "Moratuwa", "Kaduwela", "Kollonnawa", "Nugegoda", "Kelaniya", "Wattala"};
        String[] mohs = {"Kandy"};
//                String[] mohs = {"Dehiwala", "MC - Colombo"};

        /* Colombo District*/
        //        String[] mohs = {"Dehiwala", "Piliyandala", "Homagama", "Kaduwela","Kollonnawa","Maharagama","MC - Colombo", "Moratuwa", "Nugegoda", "Padukka", "Boralesgamuwa", "Hanwella"};
        /* Kandy District*/
        //        String[] mohs = {"Akurana","Galagedara","Gampola","Gangawatakorale","Hasalaka","Doluwa","Kundasale","Kurunduwatta","Medadumbara","Poojapitiya","Nawalapitiya","Talatuoya","Ududumbara","Udunuwara","Wattegama","Yatinuwara","Hataraliyadda","Menikhinna","Bambaradeniya"};

        String year = "2014";
        int iterations = 1;

        Date date = new Date();

        for (int mohIndex = 0; mohIndex < mohs.length; mohIndex++) {
            String mohName = mohs[mohIndex];
            System.out.println("Starting threads for ---------- " + mohName);

            SEIRAnalysis analysis1= new BestParameters("thread1", mohName, startWeek, maxWeeks, date, "2012");
            SEIRAnalysis analysis2;
            SEIRAnalysis analysis3;
            for(int i = 1; i <= iterations && ("NEW").equals(analysis1.getState().toString()); i++) {
                analysis1 = new BestParameters("thread1", mohName, startWeek, maxWeeks, date, "2012");
                analysis2 = new BestParameters("thread2", mohName, startWeek, maxWeeks, date, "2013");
                analysis1.start();
                analysis2.start();
                analysis1.join();
                analysis2.join();
            }

            SEIRAnalysis test = new BestParameters("TEST", mohName, 1, 51, date, "2014");
            test.start();
            test.join();
            System.out.println("----------- Ending threads for ---------- " + mohName);
        }
    }
}
