package com.geekbrains.common_files.common;

import java.util.Locale;

public final class Systems {
    public static final String user = System.getProperty ("user.name");
    public static final class OsCheck {
        private static OSType detectedOS;
        public static OSType
        getOperatingSystemType(){
            if (detectedOS ==null){
                String OS = System.getProperty ("os.name", "generic").toLowerCase (Locale.ENGLISH);
                if  (OS.indexOf("mac")>=0) {
                    detectedOS = OSType.MacOs;
                } else if (OS.indexOf("win")>=0) {
                    detectedOS = OSType.Windows;
                } else if (OS.indexOf("nux")>=0) {
                    detectedOS = OSType.Linux;
                } else {
                    detectedOS = OSType.Other;
                }
            }
            return detectedOS;
        }
    }
}

