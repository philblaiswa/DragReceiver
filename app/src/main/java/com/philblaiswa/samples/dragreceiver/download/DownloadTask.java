package com.philblaiswa.samples.dragreceiver.download;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class DownloadTask implements Callable<Boolean> {
    private static final String TAG = "DownloadTask";

    @NonNull
    private final Context context;
    @NonNull
    private final IProgressTracker progressTracker;
    @NonNull
    private final Uri uri;

    private boolean isCancelled = false;

    DownloadTask(@NonNull final Context context, @NonNull final  IProgressTracker progressTracker, @NonNull final Uri uri) {
        this.context = context;
        this.progressTracker = progressTracker;
        this.uri = uri;
    }

    void cancel() {
        isCancelled = true;
    }

    @Override
    public Boolean call() throws Exception {
        progressTracker.startFile(uri);

        String filename = getDisplayName(context, uri);
        if (TextUtils.isEmpty(filename)) {
            progressTracker.completeFile(uri, false);
            return false;
        }

        try (AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r")) {
            long declaredLength = assetFileDescriptor.getDeclaredLength();
            try (InputStream stream = assetFileDescriptor.createInputStream()) {
                byte[] buffer = new byte[10 * 1024];
                long totalBytesRead = 0;
                int bytesRead;
                do {
                    if (stream.available() > 0) {
                        bytesRead = stream.read(buffer);
                        if (bytesRead > 0) {
                            totalBytesRead += bytesRead;
                            progressTracker.reportFileProgress(uri, bytesRead);
                        }
                    } else {
                        bytesRead = -1;
                    }
                } while (!isCancelled && bytesRead != -1);
                /*
                if (((int)(Math.random() * 10) % 3) == 0) {
                    progressTracker.completeFile(uri.toString(), false);
                    return false;
                }
                 */
                if (totalBytesRead == declaredLength) {
                    progressTracker.completeFile(uri, true);
                    return true;
                }
            } catch (NullPointerException | IOException e) {
                Log.e(TAG, "Exception reading stream", e);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception calling openAssetFileDescriptor", e);
        }

        progressTracker.completeFile(uri, false);
        return false;
    }

    private static String getDisplayName(Context context, Uri uri) {
        Cursor cursor = null;

        try {
            String[] projection = { OpenableColumns.DISPLAY_NAME };
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) { // Should only have one hit
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not get display name for uri: " + uri, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }
}
