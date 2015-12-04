package com.bugbinc.fastimagegallery;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Download demo Images
        new LoremPixelImageDownloader(MainActivity.this, 100, new LoremPixelImageDownloaderInterface() {
            @Override
            public void onSuccess() {
                ArrayList<String> items = new ArrayList<>();

                for (int i = 0; i < 100; i++) {
                    items.add(i + ".jpg");
                }
                GridView gvImageGrid = (GridView) findViewById(R.id.gv_ImageGrid);

                GalleryAdapter adapter = new GalleryAdapter(MainActivity.this, items);

                gvImageGrid.setAdapter(adapter);
            }

            @Override
            public void onError() {

            }
        });



    }
}
