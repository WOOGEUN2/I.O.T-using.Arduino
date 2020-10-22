package com.cookandroid.loginregisterexample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;


public class NextActivity extends AppCompatActivity {
    dbHelper helper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_activity);
        setTitle("데이터");
        helper = new dbHelper(this);
        db = helper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("SELECT * FROM contacts", null);
        startManagingCursor(cursor);
        String[] from = {"hum","tem","heat"};
        int[] to = {R.id.textView2,R.id.textView5,R.id.textView6};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.customlist, cursor, from, to);

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
    }

}