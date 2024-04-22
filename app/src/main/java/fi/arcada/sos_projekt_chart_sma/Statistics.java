package fi.arcada.sos_projekt_chart_sma;

import java.util.ArrayList;

public class Statistics {
    public static ArrayList<Double> movingAverage(ArrayList<Double> values, int window){
        boolean isOn = true;
        ArrayList<Double> results = new ArrayList<Double>();

        if(values.size() < window){
            results.add(0.0);
            return results;
        }

        int i = 0;
        double medeltal = 0;


        //går igänom varje fönster
        while(isOn){
            int j = i;
            int k = 0;

            //räknar medeltalet i det givna fönstrest
            while(k < window){
                medeltal += values.get(j);
                j++;
                k++;
            }
            i++;
            results.add(medeltal/window);
            medeltal = 0;
            if ((values.size() - window) == i )
                isOn = false;

        }
        return results;
    }
}