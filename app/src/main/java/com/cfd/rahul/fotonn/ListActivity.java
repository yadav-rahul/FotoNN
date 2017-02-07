package com.cfd.rahul.fotonn;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ListActivity extends AppCompatActivity {

    final String[] from = new String[]{DatabaseHelper._ID,
            DatabaseHelper.URI, DatabaseHelper.IS_FACE};
    final int[] to = new int[]{R.id.id, R.id.title, R.id.desc};
    private DBManager dbManager;
    private ListView listView1, listView2;
    private SimpleCursorAdapter adapter1, adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        dbManager = new DBManager(this);
        dbManager.open();
        Cursor cursor = dbManager.fetch("No face");

        listView1 = (ListView) findViewById(R.id.no_face_list);
        listView2 = (ListView) findViewById(R.id.face_list);

        adapter1 = new SimpleCursorAdapter(this, R.layout.list_layout, cursor, from, to, 0);
        adapter1.notifyDataSetChanged();
        Cursor cursor2 = dbManager.fetch("Face");

        adapter2 = new SimpleCursorAdapter(this, R.layout.list_layout, cursor2, from, to, 0);
        adapter2.notifyDataSetChanged();

        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long viewId) {
                TextView idTextView = (TextView) view.findViewById(R.id.id);
                TextView titleTextView = (TextView) view.findViewById(R.id.title);
                TextView descTextView = (TextView) view.findViewById(R.id.desc);

                String id = idTextView.getText().toString();
                String uri = titleTextView.getText().toString();
                String desc = descTextView.getText().toString();

                Intent i = new Intent(getApplicationContext(), DBImageActivity.class);
                i.putExtra("uri", uri);
                startActivity(i);
            }
        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long viewId) {
                TextView idTextView = (TextView) view.findViewById(R.id.id);
                TextView titleTextView = (TextView) view.findViewById(R.id.title);
                TextView descTextView = (TextView) view.findViewById(R.id.desc);

                String id = idTextView.getText().toString();
                String uri = titleTextView.getText().toString();
                String desc = descTextView.getText().toString();

                Intent i = new Intent(getApplicationContext(), DBImageActivity.class);
                i.putExtra("uri", uri);
                startActivity(i);
            }
        });
    }
}
