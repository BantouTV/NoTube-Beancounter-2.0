package io.beancounter.commons.helper;

import java.io.*;
import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class PropertiesHelper {

    public static Properties readFromFileSystem(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File [" + filePath + "] does not exist");
        }
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while reading stream from [" + filePath + "]", e);
        }
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading stream from [" + filePath + "]", e);
        }
        try {
            return properties;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing stream from " + "[" + filePath + "]", e);
            }
        }
    }

    public static Properties readFromClasspath(String filename) {
        Properties properties = new Properties();
        InputStream is;
        is = PropertiesHelper.class.getResourceAsStream(filename);
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading stream from [" + filename + "]", e);
        }
        try {
            return properties;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing stream from " + "[" + filename + "]", e);
            }
        }
    }

}
