package com.malaclord.clientcommands.client.util;

public class StringUtils {
    public static String listValues(Object[] values, String last) {
        StringBuilder a = new StringBuilder();
        int i = 0;

        for (var v : values) {
            a.append(v.toString());

            if (i == values.length-2) a.append(" ").append(last).append(" ");
            else if (i != values.length-1) a.append(", ");

            i++;
        }

        return a.toString();
    }
}
