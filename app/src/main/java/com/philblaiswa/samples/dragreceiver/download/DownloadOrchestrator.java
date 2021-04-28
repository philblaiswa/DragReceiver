package com.philblaiswa.samples.dragreceiver.download;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DownloadOrchestrator implements  ICancelTransfer {
    private static final String TAG = "DownloadOrchestrator";

    @NonNull
    private final Context context;
    @NonNull
    private final ExecutorService threadpool;
    @NonNull
    private final List<DownloadTask> tasks = new ArrayList<>();
    @NonNull
    private final List<Future<?>> futures = new ArrayList<>();
    @NonNull
    private final IProgressTracker progressTracker = new ProgressTracker();
    @NonNull
    private final DownloadProgressPopup progressPopup;

    public DownloadOrchestrator(
            @NonNull final Context context,
            @NonNull final ExecutorService threadpool,
            @NonNull final Activity mainActivity) {
        this.context = context;
        this.threadpool = threadpool;

        this.progressPopup = new DownloadProgressPopup(context, threadpool, mainActivity, this);
    }

    public void onDestroy() {
        cancelTransfer();
        progressPopup.dismiss();
    }

    @Override
    public void cancelTransfer() {
        for (DownloadTask task : tasks) {
            task.cancel();
        }

        for (Future<?> future : futures) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(false);
            }
        }

        tasks.clear();
        futures.clear();
    }

    public void download(ClipData clipData) {
        Log.v(TAG, "File count: " + clipData.getItemCount());

        // Cleanup any transfer
        cancelTransfer();

        // Only download URI items, ignore others
        List<Uri> uris = new ArrayList<>();
        for (int i = 0; i < clipData.getItemCount();i++) {
            ClipData.Item item = clipData.getItemAt(i);
            Uri uri = item.getUri();
            if (uri == null) {
                // skip non-uri
                Log.w(TAG, "Clip data is not a URI, skipping...");
                continue;
            }

            uris.add(uri);
        }
        Log.v(TAG, "Total files to download: " + uris.size());

        progressTracker.initialize(progressPopup);
        progressTracker.start(uris.size());

        for (Uri uri : uris) {
            DownloadTask task = new DownloadTask(context, progressTracker, uri);
            tasks.add(task);
            futures.add(threadpool.submit(task));
        }
    }
}
