package com.example.diplomski;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String FISH_ID = "com.example.diplomski.FISH_ID";
    public String modelPath;
    Mat dic;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modelPath = CreateModelFile();
        dic = OpenCVFunctions.GetDictionary(getApplicationContext(), this.getResources().openRawResource(R.raw.dic));
    }

    public void OpenImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 0);
    }

    public void OpenImageCamera(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED)
            return;

        if (data != null) {
            Bitmap image = null;

            if (requestCode == 0) {

                try {
                    Uri imageUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    image = BitmapFactory.decodeStream(imageStream);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Failed to open image!", Toast.LENGTH_SHORT).show();
                    return;
                }

            } else {

                image = (Bitmap) data.getExtras().get("data");
            }

            AsyncTask predict = new Predict();
            predict.execute(image);
        }
    }

    private class Predict extends AsyncTask<Object, String, String>{

        private LinearLayout main = findViewById(R.id.main_layout);
        private LinearLayout progress = findViewById(R.id.progress_layout);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            main.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(Object... images) {
            Bitmap bitmap = (Bitmap)images[0];

            Mat img = new Mat();

            //histogram
            Mat hist = new Mat();
            Utils.bitmapToMat(bitmap, img);
            OpenCVFunctions.getColorHist(img.getNativeObjAddr(), hist.getNativeObjAddr());

            //shape (hu moments)
            Mat hu = new Mat();
            Utils.bitmapToMat(bitmap, img);;
            OpenCVFunctions.getShape(img.getNativeObjAddr(), hu.getNativeObjAddr());

            //bow (SIFT)
            Mat bow = new Mat();
            Utils.bitmapToMat(bitmap, img);
            OpenCVFunctions.getBOWFeatures(img.getNativeObjAddr(), dic.getNativeObjAddr(), bow.getNativeObjAddr());

            //hog
            Mat hog = new Mat();
            Utils.bitmapToMat(bitmap, img);
            OpenCVFunctions.getHOG(img.getNativeObjAddr(), hog.getNativeObjAddr());

            Mat data = new Mat();
            List<Mat> src = Arrays.asList(bow, hist, hu, hog);
            Core.hconcat(src, data);

            int res = OpenCVFunctions.predict(modelPath, data.getNativeObjAddr());

            return Integer.toString(res);
        }
        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);

            ShowFish(Integer.parseInt(res));
            progress.setVisibility(View.GONE);
            main.setVisibility(View.VISIBLE);
        }
    }

    public void ShowFish(int id) {
        Intent intent = new Intent(this, FishDisplayActivity.class);
        intent.putExtra(FISH_ID, id);
        startActivity(intent);
    }

    public String CreateModelFile() {
        InputStream is = getResources().openRawResource(R.raw.model);

        File dir = getDir("diplomski", Context.MODE_PRIVATE);
        File file = new File(dir, "model.xml");

        if(!file.exists()) {
            try {
                FileOutputStream os = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

            } catch (IOException e) {
                Log.e("DTS", e.getMessage());
            }
        }


        return file.getAbsolutePath();
    }
}
