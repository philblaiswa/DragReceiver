package com.philblaiswa.samples.dragreceiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.philblaiswa.samples.dragreceiver.download.DownloadImageTask;
import com.philblaiswa.samples.dragreceiver.download.DownloadOrchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements IEventChangeNotifier {
    private static final String TAG = "MainActivity";

    @NonNull
    private final ExecutorService threadpool = Executors.newFixedThreadPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
    @NonNull
    private final ArrayList dragEvents = new ArrayList();

    private ArrayAdapter dragEventsAdapter;
    private ListView dragEventsListView;
    private final ImageView[] images = new ImageView[4];
    private DownloadOrchestrator downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        for (PackageInfo pkg : getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            if (pkg.packageName.startsWith("com.microsoft.appmanager")) {
                if (pkg.providers != null) {
                    for (ProviderInfo pi : pkg.providers) {
                        Log.e(TAG, "ding ding!");
                    }
                }
            }
        }

        this.downloader = new DownloadOrchestrator(this.getApplicationContext(), threadpool, this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    101);
        }

        findViewById(R.id.drag_target1).setOnDragListener(new View.OnDragListener() {
            private Drawable dragOverShape = getResources().getDrawable(R.drawable.shape_drag_target);
            private Drawable normalShape = getResources().getDrawable(R.drawable.shape_drag_source);

            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        dragEvents.add("===================\nACTION_DRAG_STARTED -- drag target 1\n===================");
                        dragEvents.add(ClipDataLogger.getClipDescription(dragEvent.getClipDescription()));
                        break;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        dragEvents.add("===================\nACTION_DRAG_ENTERED -=- drag target 1\n===================");
                        view.setBackground(dragOverShape);
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        dragEvents.add("===================\nACTION_DRAG_EXITED -- drag target 1\n===================");
                        view.setBackground(normalShape);
                        break;

                    case DragEvent.ACTION_DROP:
                        dragEvents.add("===================\nACTION_DROP -- drag target 1\n===================");
                        DragAndDropPermissions permissions = requestDragAndDropPermissions(dragEvent);
                        if (permissions == null) {
                            dragEvents.add("Could not obtain permissions for drop because no content uris associated or permission could not be granted");
                            break;
                        }
                        
                        try {
                            dragEvents.add(ClipDataLogger.getClipData(getContentResolver(), dragEvent.getClipData()));

                            for (ImageView imageView : images) {
                                imageView.setImageResource(R.mipmap.ic_launcher);
                            }

                            ClipData clipData = dragEvent.getClipData();

                            dragEvents.addAll(streamContent(clipData));

                            //if (dragEvent.getClipDescription().hasMimeType("image/*")) {
                            //    dragEvents.addAll(loadImages(dragEvent.getClipData(), images[0], images[1]));
                            //}
                        } finally {
                            permissions.release();
                        }
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        dragEvents.add("===================\nACTION_DRAG_ENDED-- drag target 2\n===================");
                        dragEvents.add("Result: " + dragEvent.getResult());
                        view.setBackground(normalShape);
                        break;
                }

                notifyDataSetChanged();

                return true;
            }
        });

        findViewById(R.id.drag_target2).setOnDragListener(new View.OnDragListener() {
            private Drawable dragOverShape = getResources().getDrawable(R.drawable.shape_drag_target);
            private Drawable normalShape = getResources().getDrawable(R.drawable.shape_drag_source);

            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        dragEvents.add("===================\nACTION_DRAG_STARTED -- drag target 2\n===================");
                        dragEvents.add(ClipDataLogger.getClipDescription(dragEvent.getClipDescription()));
                        break;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        dragEvents.add("===================\nACTION_DRAG_ENTERED -- drag target 2\n===================");
                        view.setBackground(dragOverShape);
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        dragEvents.add("===================\nACTION_DRAG_EXITED -- drag target 2\n===================");
                        view.setBackground(normalShape);
                        break;

                    case DragEvent.ACTION_DROP:
                        dragEvents.add("===================\nACTION_DROP -- drag target 2\n===================");
                        DragAndDropPermissions permissions = requestDragAndDropPermissions(dragEvent);
                        if (permissions == null) {
                            dragEvents.add("Could not obtain permissions for drop because no content uris associated or permission could not be granted");
                            break;
                        }

                        try {
                            dragEvents.add(ClipDataLogger.getClipData(getContentResolver(), dragEvent.getClipData()));

                            for (ImageView imageView : images) {
                                imageView.setImageResource(R.mipmap.ic_launcher);
                            }

                            ClipData clipData = dragEvent.getClipData();

                            dragEvents.addAll(streamContent(clipData));

                            //if (dragEvent.getClipDescription().hasMimeType("image/*")) {
                            //    dragEvents.addAll(loadImages(dragEvent.getClipData(), images[2], images[3]));
                            //}
                        } finally {
                            permissions.release();
                        }
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        dragEvents.add("===================\nACTION_DRAG_ENDED -- drag target 2\n===================");
                        dragEvents.add("Result: " + dragEvent.getResult());
                        view.setBackground(normalShape);
                        break;
                }

                notifyDataSetChanged();

                return true;
            }
        });

        images[0] = findViewById(R.id.image1);
        images[1] = findViewById(R.id.image2);
        images[2] = findViewById(R.id.image3);
        images[3] = findViewById(R.id.image4);

        for (ImageView image : images) {
            image.setImageResource(R.mipmap.ic_launcher);
        }

        dragEvents.add("Drag/Drop listener ready");
        dragEventsAdapter = new ArrayAdapter<>(this, R.layout.listview_single_item, dragEvents);
        dragEventsListView = findViewById(R.id.drag_event_list);
        dragEventsListView.setAdapter(dragEventsAdapter);
        notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        this.downloader.onDestroy();
        super.onDestroy();
    }

    @Override
    public void notifyDataSetChanged() {
        // Keep only last 10 events if there are more than 30
        if (dragEvents.size() > 200) {
            dragEvents.subList(0, 50).clear();
        }
        dragEventsAdapter.notifyDataSetChanged();
        dragEventsListView.post(new Runnable() {
            @Override
            public void run() {
                dragEventsListView.setSelection(dragEventsAdapter.getCount() - 1);
            }
        });
    }

    private List<String> streamContent(ClipData clipData) {
        List<String> events = new ArrayList<>();

        events.add("File count: " + clipData.getItemCount());

        downloader.download(clipData);
        return events;
    }
}
