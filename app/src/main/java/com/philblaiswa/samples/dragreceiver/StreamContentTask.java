package com.philblaiswa.samples.dragreceiver;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class StreamContentTask extends AsyncTask<ArrayList, Void, ArrayList> {
    private final static String TAG = "StreamContentTask";

    private final Context context;
    private final IEventChangeNotifier notifier;
    private final Uri uri;
    private ArrayList events;

    public StreamContentTask(Context context, IEventChangeNotifier notifier, Uri uri) {
        this.context = context;
        this.notifier = notifier;
        this.uri = uri;
    }

    @Override
    protected ArrayList doInBackground(ArrayList... arrayLists) {
        events = arrayLists[0];

        long start = System.currentTimeMillis();
        ArrayList<String> readEvents = new ArrayList<>();

        AssetFileDescriptor fd = null;
        try {
            fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            readEvents.add("Exception: " + e.getMessage());
        }

        if (fd == null) {
            return readEvents;
        }

        String displayName = getDisplayName(uri);
        String mimeType = context.getContentResolver().getType(uri);
        readEvents.add("Info: " + displayName + " " + mimeType + " " + fd.getLength() + " bytes");

        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            Log.v(TAG, "Stream opened");
            byte[] buffer = new byte[8096];
            int bytesRead;
            long totalBytesRead = 0;
            do {
                bytesRead = stream.read(buffer);
                Log.v(TAG, "Read " + bytesRead + " bytes");
                if (bytesRead >= 0) {
                    totalBytesRead += bytesRead;
                }
            } while (bytesRead != -1);
            readEvents.add("Actual file size: " + totalBytesRead + " bytes");
        } catch (NullPointerException | IOException e) {
            readEvents.add("Exception: " + e.getMessage());
        }

        long end = System.currentTimeMillis();
        readEvents.add("Time to read: " + (end - start) + "ms");
        return readEvents;
    }

    @Override
    protected void onPostExecute(final ArrayList result) {
        if (result != null) {
            events.addAll(result);
            notifier.notifyDataSetChanged();
        }
    }

    private String getDisplayName(Uri uri) {
        Cursor cursor = null;

        try {
            String[] projection = { OpenableColumns.DISPLAY_NAME };
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) { // Should only have one hit
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return "No display name";
    }
}
