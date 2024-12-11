package com.example.finalproject.helpers;

import android.content.Context;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.database.Entry;

import java.util.List;

public class DatabaseHelper {

    private final AppDatabase database;

    public DatabaseHelper(Context context) {
        this.database = AppDatabase.getInstance(context);
    }

    public void insertEntry(String location, String photoPath, String price) {
        Entry entry = new Entry();
        entry.setLocationName(location);
        entry.setImageUrl(photoPath);
        entry.setPrice(Integer.parseInt(price));
        new Thread(() -> database.entryDao().insert(entry)).start();
    }

    public List<Entry> getAllEntries() {
        return database.entryDao().getAllEntries();
    }

    public void updateEntry(Entry entry) {
        new Thread(() -> database.entryDao().update(entry)).start();
    }

    public void deleteEntry(Entry entry) {
        new Thread(() -> database.entryDao().delete(entry)).start();
    }
}