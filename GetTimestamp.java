package com.example.drowsinessdetection;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetTimestamp {

    static SimpleDateFormat hours = new SimpleDateFormat("kk");
    static SimpleDateFormat minutes = new SimpleDateFormat("mm");
    static SimpleDateFormat seconds = new SimpleDateFormat("ss");
    static SimpleDateFormat ms = new SimpleDateFormat("SSS");

    static int getTimestamp() { /*This will be used in the main file (MainActivity.java) to get the timestamp of
                                  each drowsiness instance*/
        int timestamp = (Integer.parseInt(hours.format(new Date())) * 3600000) //Converts hours to milliseconds
                + (Integer.parseInt(minutes.format(new Date())) * 60000) //Converts minutes to milliseconds
                + (Integer.parseInt(seconds.format(new Date())) * 1000) //Converts seconds to milliseconds
                + Integer.parseInt(ms.format(new Date())); //Gets milliseconds
        return timestamp; //Timestamp is sum of hours, minutes, seconds and milliseconds.
    }

}
