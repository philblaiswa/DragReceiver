package com.philblaiswa.samples.dragreceiver.download;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<Uri, Void, Bitmap> {
    private static final String TAG = "DownloadImageTask";

    private final Context context;
    private ImageView imageView;

    public DownloadImageTask(final Context context, final ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
        Log.i(TAG, "Loading image for " + uris[0].toString());
        try (InputStream inputStream = context.getContentResolver().openInputStream(uris[0])) {
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
