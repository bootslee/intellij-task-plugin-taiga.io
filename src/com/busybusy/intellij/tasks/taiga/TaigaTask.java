package com.busybusy.intellij.tasks.taiga;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tjones on 5/22/15.
 */
public class TaigaTask {

    public static Date parseDateISO8601(String input) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        if (input.endsWith("Z")) {
            input = input.substring(0, input.length() - 1) + "GMT-00:00";
        } else {
            int inset = 6;

            String s0 = input.substring(0, input.length() - inset);
            String s1 = input.substring(input.length() - inset, input.length());

            input = s0 + "GMT" + s1;
        }
        try {
            return df.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
