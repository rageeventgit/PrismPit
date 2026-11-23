package net.rageevent.hypixel.util;

import dev.lotus.Extension;
import dev.lotus.This;

import java.text.NumberFormat;

@Extension(Integer.class)
public class NumberExtensions {

    public static String format(@This int number){
        return NumberFormat.getNumberInstance().format(number);
    }

}
