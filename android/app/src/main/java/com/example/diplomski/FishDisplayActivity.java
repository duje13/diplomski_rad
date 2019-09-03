package com.example.diplomski;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FishDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_display);

        Intent intent = getIntent();
        int id = intent.getIntExtra(MainActivity.FISH_ID, 0);

        int drawableID = getResources().getIdentifier("fish_" + id, "drawable", getPackageName());
        ImageView iv = findViewById(R.id.fish_image);
        iv.setImageResource(drawableID);

        String str = "";
        StringBuffer buf = new StringBuffer();
        InputStream is = this.getResources().openRawResource(R.raw.fishes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        try {
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n");
                }
            }
            is.close();
        }
        catch (IOException e) {
            Log.e("DLT", e.getMessage());
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fishes = buf.toString();
        JSONArray array = null;
        String name = null;
        String latin_name = null;
        String other_names = null;

        try {
            array = new JSONArray(fishes);

            for(int i = 0; i<array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if(obj.getInt("id") == id) {
                    name = obj.getString("name");
                    latin_name = obj.getString("latin_name");
                    other_names = obj.getString("other_names");
                    break;
                }
            }

        }
        catch (JSONException e) {
            Log.e("DLT", e.getMessage());
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView title = findViewById(R.id.fish_title);
        TextView latin = findViewById(R.id.fish_latin);
        TextView others = findViewById(R.id.fish_others);

        title.setText(name);
        latin.setText(latin_name);
        others.setText(other_names);
    }
}
