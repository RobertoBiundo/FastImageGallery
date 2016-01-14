package com.bugbinc.fastimagegallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.GridLayout.LayoutParams;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> items;
    private LruCache<String, Bitmap> memoryCache;
    private LayoutInflater inflater;

    public GalleryAdapter(Context context, ArrayList<String> items) {
        this.context = context;
        this.items = items;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null)
            convertView = inflater.inflate(R.layout.row_imagecell, null);

        imageView = (ImageView) convertView.findViewById(R.id.iv_ImageView);

        loadBitmap(position + ".jpg", imageView);

        return convertView;
    }

    public void loadBitmap(String imageName, ImageView imageView) {
        if (cancelPotentialWork(imageName, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(imageName);
            AsyncImage imageDrawble = new AsyncImage(task);
            imageView.setImageDrawable(imageDrawble);
        }
    }

    static class AsyncImage extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncImage(BitmapWorkerTask bitmapWorkerTask) {
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String imageName, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapImageName = bitmapWorkerTask.imageName;
            if (bitmapImageName!= null && imageName != null && !bitmapImageName.equals(imageName)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable image = imageView.getDrawable();
            if (image instanceof AsyncImage) {
                AsyncImage asyncImage = (AsyncImage) image;
                return asyncImage.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        public String imageName;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imageName = params[0];
            final Bitmap bitmap = decodeSampledBitmapFromResource(context.getCacheDir(), imageName, 400, 400);
            addBitmapToMemoryCache(imageName, bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(File folderPath, String imageName, int reqWidth, int reqHeight) {

        // Get File
        File bitmapFile = new File(folderPath, imageName);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
