package com.philblaiswa.samples.dragreceiver.download;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgressTracker implements IProgressTracker {
    @Nullable
    private IProgressReport callback;
    private int expectedFileCount;

    private long totalBytes;
    Map<Uri, Long> filesInProgress = new ConcurrentHashMap<>();
    Map<Uri, Long> filesSuccessfullyCompleted = new ConcurrentHashMap<>();
    private int filesCompleted;
    private int filesFailed;

    @Override
    public void initialize(IProgressReport callback) {
        this.callback = callback;
        this.filesInProgress.clear();
        this.filesSuccessfullyCompleted.clear();
        this.filesCompleted = 0;
        this.filesFailed = 0;
        this.totalBytes = 0;
    }

    @Override
    public void start(int expectedFileCount) {
        this.expectedFileCount = expectedFileCount;
        callback.started(expectedFileCount);
    }

    @Override
    public void startFile(Uri uri) {
        Log.e("XXXXXXX", "ADDED: " + uri.toString());
        filesInProgress.put(uri, new Long(0));
        reportProgress(filesInProgress.size(), filesCompleted, filesFailed, totalBytes);
    }

    @Override
    public void reportFileProgress(Uri uri, long progressSize) {
        if (!filesInProgress.containsKey(uri)) {
            return;
        }
        totalBytes += progressSize;
        reportProgress(filesInProgress.size(), filesCompleted, filesFailed, totalBytes);
    }

    @Override
    public void completeFile(Uri uri, boolean succeeded) {
        Log.e("XXXXXXX", "REMOVED: " + uri.toString());
        filesInProgress.remove(uri);
        if (succeeded) {
            filesCompleted++;
            filesSuccessfullyCompleted.put(uri, new Long(1));
        } else {
            filesFailed++;
        }
        reportProgress(filesInProgress.size(), filesCompleted, filesFailed, totalBytes);
    }

    private void reportProgress(int filesStarted, int filesComplete, int filesFailed, long currentProgress) {
        callback.progress(filesStarted, filesComplete, filesFailed, currentProgress);

        if (filesInProgress.size() == 0) {
            callback.completed(filesSuccessfullyCompleted.keySet());
        }
    }
}
