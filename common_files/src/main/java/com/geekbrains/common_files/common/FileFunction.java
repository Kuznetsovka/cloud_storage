package com.geekbrains.common_files.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileFunction {
    public static void createDirectory (String dirName){
        File file = new File (dirName);
        if (!file.exists ()) {
            file.mkdir ();
        }
    }
    public static void createDirectories (String dirName){
        File file = new File (dirName);
        if (!file.exists ()) {
            file.mkdirs ();
        }
    }

    public static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
}
