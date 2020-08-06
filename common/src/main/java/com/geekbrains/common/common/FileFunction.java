package com.geekbrains.common.common;

import java.io.File;

public class FileFunction {
    public static void createDirectory (String dirName){
        File file = new File (dirName);
        if (!file.exists ()) {
            file.mkdir ();
        }
    }
}
