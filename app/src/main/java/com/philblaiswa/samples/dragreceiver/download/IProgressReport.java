package com.philblaiswa.samples.dragreceiver.download;

import android.net.Uri;

import java.util.Set;

public interface IProgressReport {
    void started(int fileCount);
    void progress(int filesStarted, int filesComplete, int filesFailed, long currentProgress);
    void completed(Set<Uri> successfullUris);
}
