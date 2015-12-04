package com.bugbinc.fastimagegallery;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class LoremPixelImageDownloader extends AsyncTask <Void, Void, Boolean> {

    private static final String IMAGE_FORMAT = ".jpg";
    private static final String LOREM_PIXEL_IMAGE_PATH = "http://lorempixel.com/800/800/people/";

    private LoremPixelImageDownloaderInterface delegate;
    private File downloadFolder;
    private int imageCount;
    
    public LoremPixelImageDownloader(Context context, int imageCount, LoremPixelImageDownloaderInterface delegate) {
        this.delegate = delegate;
        this.imageCount = imageCount;
        downloadFolder = context.getCacheDir();
        this.execute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            for (int i = 0; i < imageCount; i++) {
                File currentFile = new File(downloadFolder, i + IMAGE_FORMAT);
                NetworkCommons.downloadFile(LOREM_PIXEL_IMAGE_PATH, currentFile.getAbsoluteFile());
                Log.d("FastImageGallery", "Downloaded Image: " + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean param) {
        if (param)
            delegate.onSuccess();
        else
            delegate.onError();
    }
}
