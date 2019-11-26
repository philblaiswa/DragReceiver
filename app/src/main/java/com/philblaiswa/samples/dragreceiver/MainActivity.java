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
import android.view.DragEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> dragEvents = new ArrayList();
    private ArrayAdapter dragEventsAdapter;
    private ListView dragEventsListView;
    private ImageView[] images = new ImageView[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    101);
        }

        findViewById(R.id.drag_target).setOnDragListener(new View.OnDragListener() {
            private Drawable dragOverShape = getResources().getDrawable(R.drawable.shape_drag_target);
            private Drawable normalShape = getResources().getDrawable(R.drawable.shape_drag_source);

            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        dragEvents.add("===================\nACTION_DRAG_STARTED\n===================");
                        dragEvents.add(ClipDataLogger.getClipDescription(dragEvent.getClipDescription()));
                        break;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        dragEvents.add("===================\nACTION_DRAG_ENTERED\n===================");
                        view.setBackground(dragOverShape);
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        dragEvents.add("===================\nACTION_DRAG_EXITED\n===================");
                        view.setBackground(normalShape);
                        break;

                    case DragEvent.ACTION_DROP:
                        dragEvents.add("===================\nACTION_DROP\n===================");
                        dragEvents.add(ClipDataLogger.getClipData(getContentResolver(), dragEvent.getClipData()));

                        for (int i = 0; i < images.length; i++) {
                            images[i].setImageResource(R.mipmap.ic_launcher);
                        }

                        if (dragEvent.getClipDescription().hasMimeType("image/jpeg")) {
                            List<String> events = processImages(dragEvent.getClipData());
                            dragEvents.addAll(events);
                        }
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        dragEvents.add("===================\nACTION_DRAG_ENDED\n===================");
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

        for (int i = 0; i < images.length; i++) {
            images[i].setImageResource(R.mipmap.ic_launcher);
        }

        dragEvents.add("Drag/Drop listener ready");
        dragEventsAdapter = new ArrayAdapter<String>(this, R.layout.listview_single_item, dragEvents);
        dragEventsListView = (ListView) findViewById(R.id.drag_event_list);
        dragEventsListView.setAdapter(dragEventsAdapter);
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        // Keep only last 10 events if there are more than 30
        if (dragEvents.size() > 50) {
            dragEvents.subList(0, 9).clear();
        }
        dragEventsAdapter.notifyDataSetChanged();
        dragEventsListView.post(new Runnable() {
            @Override
            public void run() {
                dragEventsListView.setSelection(dragEventsAdapter.getCount() - 1);
            }
        });
    }

    private List<String> processImages(ClipData clipData) {
        List<String> events = new ArrayList<>();

        try {
            for (int i = 0; i < clipData.getItemCount() && i < 2; i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();

                String displayName = getDisplayName(uri);
                events.add("Filename: " + displayName);
                
                AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(uri, "r");
                events.add("File size: " + fd.getLength());

                Bitmap bitmap = BitmapFactory.decodeStream(fd.createInputStream());
                images[i].setImageBitmap(bitmap);
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
}
