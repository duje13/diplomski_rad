package com.example.diplomski;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OpenCVFunctions {
    static public native int getColorHist(long img, long hist);
    static public native int getShape(long img, long hu);
    static public native int getBOWFeatures(long img, long dic, long out);
    static public native int getHOG(long img, long hog);
    static public native int predict(String modelFilePath, long trainData);


    static public Mat GetDictionary(Context context, InputStream is) {
        String str = "";
        StringBuffer buf = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        try {
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n");
                }
            }
            is.close();
        } catch (IOException e) {
            Log.e("DLT", e.getMessage());

            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
            return null;
        }

        String dictionaryStr = buf.toString();
        //Log.d("DLT", dictionaryStr);
        JSONArray array = null;
        int rows, cols;

        try {
            array = new JSONArray(dictionaryStr);
            rows = array.length();
            cols = array.getJSONArray(0).length();
        } catch (JSONException e) {
            Log.e("DLT", e.getMessage());
            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
            return null;
        }

        Mat dictionary = new Mat(rows, cols, CvType.CV_32F);

        try {
            for (int i = 0; i < rows; i++) {
                JSONArray row = array.getJSONArray(i);

                for (int j = 0; j < cols; j++) {
                    dictionary.put(i, j, row.getDouble(j));
                }
            }
        } catch (JSONException e) {
            Log.e("DLT", e.getMessage());
            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
            return null;
        }

        return dictionary;
    }
}
