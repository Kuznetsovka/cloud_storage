package com.geekbrains.common1.common;

import java.io.File;

public class FileFunction {
    public static void createDirectory (String dirName){
        File file = new File (dirName);
        if (!file.exists ()) {
            file.mkdir ();
        }
    }
}
