package com.kleberandrade.coronagel.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class FileHelper {
    public String[] getFolderFiles(String folder) {
        File[] files = new File(folder).listFiles(File::isFile);
        if (files == null) return new String[0];
        return Arrays.stream(files).filter(this::isImage).map(File::getName).sorted().toArray(String[]::new);
    }
    private boolean isImage(File file) {
        try { return ImageIO.read(file) != null; }
        catch (IOException failure) { return false; }
    }
}
