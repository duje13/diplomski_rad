package com.example.diplomski;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        TextView tv = findViewById(R.id.textView);

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

        try {
            array = new JSONArray(fishes);

            for(int i = 0; i<array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if(obj.getInt("id") == id) {
                    name = obj.getString("name");
                    break;
                }
            }

        }
        catch (JSONException e) {
            Log.e("DLT", e.getMessage());
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

        tv.setText(name);
    }
}
