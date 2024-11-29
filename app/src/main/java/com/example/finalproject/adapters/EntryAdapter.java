package com.example.finalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.finalproject.R;
import com.example.finalproject.database.Entry;
import com.bumptech.glide.Glide;
import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private final Context context;
    private final List<Entry> entries;

    public EntryAdapter(Context context, List<Entry> entries) {
        this.context = context;
        this.entries = entries;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.entry_template, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        Entry entry = entries.get(position);

        holder.locationName.setText(entry.getLocationName());

        Glide.with(context)
                .load(entry.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imageView);

        holder.price.setText("Rp. " + entry.getPrice());
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {

        TextView locationName, price;
        ImageView imageView;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.text_location);
            price = itemView.findViewById(R.id.location_price);
            imageView = itemView.findViewById(R.id.location_image);
        }
    }
}
