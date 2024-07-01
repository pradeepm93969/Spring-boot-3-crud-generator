package com.pradeep.crudgenerator.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    public static void writeFile(String content, String filePath) {
        try {
            File file = new File(filePath);
            File directory = new File(file.getParent());
            if (!directory.exists()) {
                directory.mkdirs();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
