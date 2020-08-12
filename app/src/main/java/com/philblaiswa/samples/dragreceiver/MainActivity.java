package com.philblaiswa.samples.dragreceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IEventChangeNotifier {
    private static final String TAG = "MainActivity";

    private ArrayList dragEvents = new ArrayList();
    private ArrayAdapter dragEventsAdapter;
    private ListView dragEventsListView;
    private ImageView[] images = new ImageView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

                            if (dragEvent.getClipDescription().hasMimeType("image/*")) {
                                dragEvents.addAll(loadImages(dragEvent.getClipData(), images[0], images[1]));
                            }
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
                        if (!dragEvent.getClipDescription().hasMimeType("image/*")) {
                            dragEvents.add("Clip description DOES NOT contain any images -- returning false");
                            return false;
                        } else {
                            dragEvents.add("Clip description contains at least one image -- returning true");
                        }
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

                            if (dragEvent.getClipDescription().hasMimeType("image/*")) {
                                dragEvents.addAll(loadImages(dragEvent.getClipData(), images[2], images[3]));
                            }
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
        boolean image1Set = false;
        boolean image2Set = false;
        List<String> events = new ArrayList<>();

        for (int i = 0; i < clipData.getItemCount() && i < 2; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            Uri uri = item.getUri();
            if (uri != null) {
                Log.v(TAG, "Uri: " + uri.toString());

                // If the stream hasn't been realized yet by the provider this may return 0 bytes
                long expectedLength = getContentSize(uri);
                events.add("Expected file size: " + expectedLength + " bytes");
                String mimeType = getContentResolver().getType(uri);
                events.add("MIME type: " + mimeType);

                new StreamContentTask(this.getApplicationContext(), this, uri).execute(this.dragEvents);
            }
        }

        return events;
    }

    private List<String> loadImages(ClipData clipData, ImageView image1, ImageView image2) {
        List<String> events = new ArrayList<>();

        boolean image1Set = false;
        boolean image2Set = false;

        try {
            for (int i = 0; i < clipData.getItemCount() && i < 2 && (!image1Set || !image2Set); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                String mimeType = getContentResolver().getType(uri);
                if (mimeType.startsWith("image/") && (!image1Set || !image2Set)) {
                    events.add("Extracting image for " + mimeType);
                    new DownloadImageTask(this.getApplicationContext(), uri).execute(!image1Set ? image1 : image2);
                    if (!image1Set) {
                        image1Set = true;
                    } else {
                        image2Set = true;
                    }
                }
            }
        } catch (Exception e) {
            events.add("Exception: " + e.getMessage());
        }
        return events;
    }
    private long getContentSize(Uri uri) {
        Cursor cursor = null;

        try {
            String[] projection = { OpenableColumns.SIZE };
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) { // Should only have one hit
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        } catch (Exception e) {
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return -1;
    }
}
