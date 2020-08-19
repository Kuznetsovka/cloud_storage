package com.geekbrains.common_files.common;
import org.apache.log4j.Logger;

public class MyLogger {
    private static final Logger log = Logger.getLogger(MyLogger.class);
    public static void logInfo(String txt){
        log.info(txt);
    }
    public static void logError(String txt) {
        log.error(txt);
    }
}
