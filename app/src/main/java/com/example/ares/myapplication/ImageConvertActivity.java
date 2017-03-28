package com.example.ares.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class ImageConvertActivity extends AppCompatActivity {

    Button chooseImageButton = (Button)findViewById(R.id.button15);
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    int THRESHOLD_VALUE = 382, TOTAL_FAN_SIZE_PIXELS = 178, NUMBER_OF_SCAN_LINES =960 , ACTUAL_FAN_SIZE_PIXELS= 128, threskip = 20;
    ImageView imagePreview =(ImageView)findViewById(R.id.imageView4);
    Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_convert);

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }
    protected void chooseImage()
    {
        verifyStoragePermissions(this);
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX" , 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", TOTAL_FAN_SIZE_PIXELS*2);
        intent.putExtra("outputY", TOTAL_FAN_SIZE_PIXELS*2);
        startActivityForResult(intent, 1);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode==RESULT_OK && requestCode == 1 && intent != null)
        {
            Uri selectedImage = intent.getData();
            imagePreview.setImageURI(selectedImage);
            imageBitmap = Bitmap.createScaledBitmap(((BitmapDrawable)imagePreview.getDrawable()).getBitmap(), TOTAL_FAN_SIZE_PIXELS*2,TOTAL_FAN_SIZE_PIXELS*2, true);
            imagePreview.setImageBitmap(imageBitmap);
        }
    }

}
