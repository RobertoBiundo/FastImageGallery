package com.bugbinc.fastimagegallery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkCommons {

    private static final int FILE_BUFFER_SIZE = 4 * 1024;
    private static final String JACKSON_TEMPORAL_FILE = "JSONTemporal";

    public static void downloadFile(String urlString, File file) throws IOException {
        if (file.exists())
            return;
        file.getParentFile().mkdirs();
        file.createNewFile();

        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        OutputStream output = new FileOutputStream(file);

        try {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            byte[] buffer = new byte[FILE_BUFFER_SIZE];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();

        } finally {
            urlConnection.disconnect();
            output.close();
        }
    }


}
