package com.philblaiswa.samples.dragreceiver.download;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.philblaiswa.samples.dragreceiver.R;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class DownloadProgressPopup implements IProgressReport, ICancelTransfer {
    private static final String TAG = "DownloadProgressPopup";
    private static final Double ONE_MB = 1024.0;
    private static final Double ONE_GB = ONE_MB * ONE_MB;

    @Nullable
    private PopupWindow window;
    @Nullable
    private ProgressUi progressUi;
    @Nullable
    private ProgressText progressText;
    @Nullable
    private Timer timerUiUpdate;

    private int expectedFileCount;

    @NonNull
    private Context context;
    @NonNull
    private final ExecutorService threadpool;
    @NonNull
    private final Activity mainActivity;
    @NonNull
    private final ImageView[] images = new ImageView[4];
    @NonNull
    private final ICancelTransfer cancelCallback;

    public DownloadProgressPopup(
            @NonNull Context context,
            @NonNull  final ExecutorService threadpool,
            @NonNull final Activity mainActivity,
            @NonNull final ICancelTransfer cancelCallback) {
        this.context = context;
        this.threadpool = threadpool;
        this.mainActivity = mainActivity;
        this.cancelCallback = cancelCallback;

        images[0] = mainActivity.findViewById(R.id.image1);
        images[1] = mainActivity.findViewById(R.id.image2);
        images[2] = mainActivity.findViewById(R.id.image3);
        images[3] = mainActivity.findViewById(R.id.image4);
    }

    public void dismiss() {
        if (timerUiUpdate != null) {
            timerUiUpdate.cancel();
            timerUiUpdate = null;
        }
        if (window != null) {
            window.dismiss();
            window = null;
        }
        progressUi = null;
        progressText = null;
    }

    @Override
    public void cancelTransfer() {
        if (timerUiUpdate == null) {
            if (window != null) {
                window.dismiss();
                window = null;
            }
            return;
        }

        if (progressUi != null) {
            progressUi.cancelButton.setText("Cancelled!");
        }

        cancelCallback.cancelTransfer();
    }

    @Override
    public void started(int fileCount) {
        Log.v(TAG, "Transaction started: filecount=" + fileCount);
        expectedFileCount = fileCount;
        progressText = new ProgressText(fileCount);

        mainActivity.runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           // Clear preview images
                                           for (ImageView imageView : images) {
                                               imageView.setImageResource(R.mipmap.ic_launcher);
                                           }
                                       }
                                   });
        showPopupWindow();
        updateProgressUI(progressText);

        // Schedule UI updates as we'll get more progress events
        // than what the UI can render
        timerUiUpdate = new Timer();
        timerUiUpdate.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        updateProgressUI(progressText);
                    }
                },
                500, 500);
    }

    @Override
    public void progress(int filesStarted, int filesComplete, int filesFailed, long currentProgress) {
        Log.v(TAG, "Transaction progress: filesStarted=" + filesStarted
                + ", filesComplete=" + filesComplete
                + ", filesFailed=" + filesFailed
                + ", currentProgress=" + currentProgress);

        progressText.primaryProgress = (int)Math.round((100.0 * filesFailed) / expectedFileCount);
        progressText.secondaryProgress = progressText.primaryProgress + (int)Math.round((100.0 * filesComplete) / expectedFileCount);
        progressText.textFileCount = "" + (filesComplete + filesFailed) + "/" + expectedFileCount;
        if (currentProgress < ONE_GB) {
            progressText.progressTextUnit = "Kb";
            progressText.progressText = String.format("%.2f", (1.0 *currentProgress) / ONE_MB);
        } else {
            progressText.progressTextUnit = "Mb";
            progressText.progressText = String.format("%.2f", (1.0 *currentProgress) / ONE_GB);
        }
    }

    @Override
    public void completed(@NonNull final Set<Uri> successfullUris) {
        Log.v(TAG, "Transaction completed");
        mainActivity.runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           progressUi.cancelButton.setText("Completed!");
                                       }
                                   });
        timerUiUpdate.cancel();;
        timerUiUpdate = null;

        progressText.secondaryProgress = 100;
        updateProgressUI(progressText);

        updateImagePreviews(successfullUris);
    }

    private void showPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupLayout = inflater.inflate(R.layout.progress_popup, null);

        View rootView = mainActivity.findViewById(R.id.rootview);
        window = new PopupWindow(popupLayout, rootView.getWidth(), 370, true);
        window.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        window.setOutsideTouchable(true);

        progressUi = new ProgressUi(popupLayout, this);

        window.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);
    }

    private void updateProgressUI(final ProgressText progressText) {
        final ProgressUi ui = progressUi;
        if (ui == null) {
            return;
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ui.progressBarFiles.isIndeterminate()) {
                    ui.progressBarFiles.setIndeterminate(false);
                }
                ui.progressBarFiles.setProgress(progressText.primaryProgress);
                ui.progressBarFiles.setSecondaryProgress(progressText.secondaryProgress);
                ui.textFileCountView.setText(progressText.textFileCount);
                ui.textTransferredUnitView.setText(progressText.progressTextUnit);
                ui.textTransferredView.setText(progressText.progressText);
                ui.popupLayout.requestLayout();
            }
        });
    }

    private void updateImagePreviews(@NonNull final Set<Uri> uris) {
        int imageIndex = 0;
        for (Uri uri : uris) {
            if (imageIndex >= images.length) {
                break;
            }

            String mimeType = context.getContentResolver().getType(uri);
            if (!mimeType.startsWith("image/")) {
                continue;
            }

            new DownloadImageTask(context, images[imageIndex++]).executeOnExecutor(threadpool, uri);
        }
    }

    private static class ProgressText {
        String textFileCount;
        String progressText;
        String progressTextUnit;
        int primaryProgress;
        int secondaryProgress;

        ProgressText(int fileCount) {
            textFileCount = "0/" + fileCount;
            progressText = "0.00";
            progressTextUnit = "Kb";
            primaryProgress = 0;
            secondaryProgress = 0;
        }
    }

    private static class ProgressUi {
        @NonNull
        private View popupLayout;
        @NonNull ICancelTransfer cancelCallback;
        @NonNull
        Button cancelButton;
        @NonNull
        TextView textFileCountView;
        @NonNull
        TextView textTransferredView;
        @NonNull
        TextView textTransferredUnitView;
        @NonNull
        ProgressBar progressBarFiles;

        ProgressUi(@NonNull final View popupLayout, @NonNull final ICancelTransfer cancelCallback) {
            this.popupLayout = popupLayout;
            this.cancelCallback = cancelCallback;

            this.textFileCountView = popupLayout.findViewById(R.id.textFileCount);
            this.textTransferredView = popupLayout.findViewById(R.id.textTransferred);
            this.textTransferredUnitView = popupLayout.findViewById(R.id.textTransferredUnit);
            this.progressBarFiles = popupLayout.findViewById(R.id.progressBarFiles);
            this.progressBarFiles.setProgress(0);
            this.progressBarFiles.setSecondaryProgress(0);
            this.progressBarFiles.setIndeterminate(true);

            this.cancelButton = popupLayout.findViewById(R.id.cancelButton);
            this.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelCallback.cancelTransfer();
                }
            });
        }
    }
}
