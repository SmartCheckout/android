package com.smartcheckout.poc.util;

import java.util.Date;

/**
 * Created by swetha_swaminathan on 10/18/2017.
 */

public class CommonUtils {

    public static long getDifferenceinMinutes(Date date1,Date date2)
    {
        System.out.println(date1.toString());

        System.out.println(date2.toString());

        if(date1 == null || date2 == null)
            return 0;

        long diff = date2.getTime() - date1.getTime();
        System.out.println(diff);

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;

        return diffMinutes;
    }

    public static Date getCurrentDate()
    {
        return new Date();
    }
}
