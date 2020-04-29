package com.kleberandrade.coronagel.helpers;

import java.io.File;
import java.util.Arrays;

public class FileHelper {

    //public static String IMAGES_FILE = "D:/Figures/";
    public static String IMAGES_FILE = "/home/pi/Pictures/";
    
    public String[] getFolderFiles(String folder) {
        File file = new File(folder);
        String[] fileList = file.list();
        Arrays.sort(fileList);
        return fileList;
    }

}
