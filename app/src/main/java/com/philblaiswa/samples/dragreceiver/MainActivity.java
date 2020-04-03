package com.philblaiswa.samples.dragreceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
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

                            images[0].setImageResource(R.mipmap.ic_launcher);
                            images[1].setImageResource(R.mipmap.ic_launcher);

                            ClipData clipData = dragEvent.getClipData();

                            dragEvents.addAll(streamContentWithInputStream(clipData));

                            if (dragEvent.getClipDescription().hasMimeType("image/*")) {
                                dragEvents.addAll(processImagesWithFileDescriptor(dragEvent.getClipData(), images[0], images[1]));
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

                            images[2].setImageResource(R.mipmap.ic_launcher);
                            images[3].setImageResource(R.mipmap.ic_launcher);

                            ClipData clipData = dragEvent.getClipData();

                            dragEvents.addAll(streamContentWithInputStream(clipData));

                            if (dragEvent.getClipDescription().hasMimeType("image/*")) {
                                dragEvents.addAll(processImagesWithFileDescriptor(dragEvent.getClipData(), images[2], images[3]));
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

    private void notifyDataSetChanged() {
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

    private List<String> streamContentWithInputStream(ClipData clipData) {
        List<String> events = new ArrayList<>();

        for (int i = 0; i < clipData.getItemCount() && i < 2; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            Uri uri = item.getUri();

            if (uri != null) {

                // If the stream hasn't been realized yet by the provider this may return 0 bytes
                long expectedLength = getContentSize(uri);
                events.add("Expected file size: " + expectedLength + " bytes");

                try (InputStream stream = getContentResolver().openInputStream(uri)){
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long totalBytesRead = 0;
                    do {
                        bytesRead = stream.read(buffer);
                        if (bytesRead >= 0) {
                            totalBytesRead += bytesRead;
                        }
                    } while (bytesRead != -1);
                    events.add("Expected file size: " + totalBytesRead + " bytes");
                } catch (NullPointerException | IOException e) {
                    events.add("Exception: " + e.getMessage());
                }
            }
        }

        return events;
    }

    private List<String> processImagesWithFileDescriptor(ClipData clipData, ImageView image1, ImageView image2) {
        List<String> events = new ArrayList<>();

        boolean image1Set = false;
        boolean image2Set = false;

        try {
            for (int i = 0; i < clipData.getItemCount() && i < 2; i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();

                String displayName = getDisplayName(uri);
                AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(uri, "r");
                String mimeType = getContentResolver().getType(uri);

                events.add("File: " + displayName + "[" + fd.getLength() + " bytes, " + mimeType);

                if (mimeType.startsWith("image/") && (!image1Set || !image2Set)) {
                    Bitmap bitmap = BitmapFactory.decodeStream(fd.createInputStream());
                    if (!image1Set) {
                        image1.setImageBitmap(bitmap);
                        image1Set = true;
                    } else {
                        image2.setImageBitmap(bitmap);
                        image2Set = true;
                    }
                }
            }
        } catch (Exception e) {
            events.add("Exception: " + e.getMessage());
        }
        return events;
    }

    private String getDisplayName(Uri uri) {
        Cursor cursor = null;

        try {
            String[] projection = { OpenableColumns.DISPLAY_NAME };
            cursor = getContentResolver().query(uri, projection, null, null, null);
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
