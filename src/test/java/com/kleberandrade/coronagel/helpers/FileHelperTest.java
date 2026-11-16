package com.kleberandrade.coronagel.helpers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class FileHelperTest {
    @TempDir Path directory;
    @Test void missingDirectoryIsEmpty() {
        assertArrayEquals(new String[0], new FileHelper().getFolderFiles(directory.resolve("missing").toString()));
    }
    @Test void onlyReadableImagesAreSorted() throws Exception {
        Files.writeString(directory.resolve("notes.txt"), "text");
        Files.createDirectory(directory.resolve("folder.png"));
        Files.writeString(directory.resolve("broken.png"), "invalid");
        var image = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        javax.imageio.ImageIO.write(image, "png", directory.resolve("b.PNG").toFile());
        javax.imageio.ImageIO.write(image, "png", directory.resolve("a.png").toFile());
        assertArrayEquals(new String[]{"a.png", "b.PNG"}, new FileHelper().getFolderFiles(directory.toString()));
    }
}
