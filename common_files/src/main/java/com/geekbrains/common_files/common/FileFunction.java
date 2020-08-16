package com.geekbrains.common_files.common;

import java.io.File;

public class FileFunction {
    public static void createDirectory (String dirName){
        File file = new File (dirName);
        if (!file.exists ()) {
            file.mkdir ();
        }
    }
}
