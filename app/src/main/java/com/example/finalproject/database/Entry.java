package com.example.finalproject.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "entries")
public class Entry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String locationName;
    private String imageUrl;
    private int price;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
}