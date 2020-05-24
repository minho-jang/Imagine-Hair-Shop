package com.example.hairchange;

import java.io.File;

public class MyUtil {
    public static String combinePaths(String ... paths) {
        if ( paths.length == 0) {
            return "";
        }

        File combined = new File(paths[0]);

        int i = 1;
        while ( i < paths.length ) {
            combined = new File(combined, paths[i]);
            ++i;
        }

        return combined.getPath();
    }
}
