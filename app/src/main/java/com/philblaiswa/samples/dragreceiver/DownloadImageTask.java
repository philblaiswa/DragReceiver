package com.philblaiswa.samples.dragreceiver;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {
    private static final String TAG = "DownloadImageTask";

    private final Context context;
    private final Uri uri;
    private ImageView imageView;

    public DownloadImageTask(final Context context, final Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        imageView = imageViews[0];

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Bitmap result) {
        if (result != null) {
            imageView.setImageBitmap(result);
        }
    }
}
