package com.example.anja.lab1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by jennyseong on 11/7/17.
 */

public class CatListAdapter extends BaseAdapter {

    private final Context context;
    private final JSONArray values;

    public CatListAdapter(Context context, JSONArray values){
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length();
    }

    @Override
    public JSONObject getItem(int i) {
        try {
            return values.getJSONObject(i);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.cat_list,parent,false);
        DecimalFormat mLng = new DecimalFormat("Longitude: #.000000;Longitude: -#.000000");
        DecimalFormat mLat = new DecimalFormat("Latitude: #.000000;Latitude: -#.000000");

        TextView catName = rowView.findViewById(R.id.name);
        TextView catLong = rowView.findViewById(R.id.longitude);
        TextView catLat = rowView.findViewById(R.id.latitude);
        ImageView catProfile = rowView.findViewById(R.id.image);
        ImageView catPet = rowView.findViewById(R.id.petted);

        try {
            JSONObject cat = values.getJSONObject(position);

            catName.setText(cat.getString("name"));
            catLong.setText(mLng.format(cat.getDouble("lng")));
            catLat.setText(mLat.format(cat.getDouble("lat")));
            Picasso.with(context).load(cat.getString("picUrl")).into(catProfile);

            if(cat.getBoolean("petted")) {
                catPet.setImageResource(R.drawable.pet);
            } else {
                catPet.setImageResource(R.drawable.not_pet);
            }

        } catch (JSONException e) {
            Log.d("ERROR", "can't parse JSON");
        }

        return rowView;
    }
}

