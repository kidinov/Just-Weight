package org.kidinov.justweight.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by akid on 20/04/15.
 */
public class FileHelper {
    private static final String TAG = "FileHelper";

    public static int writeTextToFile(String path, String text) {
        File f = new File(path);
        if (f.exists()) {
            Log.d(TAG, String.format("file exist in %s", path));
            f.delete();
        }

        FileWriter fw = null;
        try {
            if (!f.createNewFile()) {
                Log.d(TAG, String.format("unable create new file in %s", path));
                return -1 ;
            }

            fw = new FileWriter(f);
            fw.write(text);
            fw.flush();
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ignored) {
                }
            }
        }
        return 1;
    }

    public static String readTextFromFile(String path) {
        File f = new File(path);
        if (!f.exists()) {
            Log.d(TAG, String.format("file not exist in %s", path));
            return "";
        }

        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "", e);
            return "";
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
