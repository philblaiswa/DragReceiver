package com.philblaiswa.samples.dragreceiver.download;

import android.net.Uri;

public interface IProgressTracker {
    void initialize(IProgressReport callback);
    void start(int expectedFileCount);
    void startFile(Uri uri);
    void reportFileProgress(Uri uri, long progressSize);
    void completeFile(Uri uri, boolean succeeded);
}
