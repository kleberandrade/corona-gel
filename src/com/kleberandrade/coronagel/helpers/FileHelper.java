package com.kleberandrade.coronagel.helpers;

import java.io.File;
import java.util.Arrays;

public class FileHelper {

    public String[] getFolderFiles(String folder) {
        File file = new File(folder);
        String[] fileList = file.list();
        Arrays.sort(fileList);
        return fileList;
    }
}
