package com.example.ares.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.BitSet;

import static android.R.attr.delay;
import static android.R.id.list;

public class ReadyImageActivity extends AppCompatActivity {
     ImageView chosenImage;
    TextView imgFile;
    Uri selectedImage;
    SeekBar threseek;

    File f;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //boolean imageCartesianMatrix[][];
    int THRESHOLD_VALUE = 382, TOTAL_FAN_SIZE_PIXELS = 178, NUMBER_OF_SCAN_LINES =960 , ACTUAL_FAN_SIZE_PIXELS= 128, threskip = 20;
    boolean[][] imageCartesianMatrix;
    //boolean[][] imagePolarArray = new boolean[NUMBER_OF_SCAN_LINES][ACTUAL_FAN_SIZE_PIXELS];
    //BitSet imagePolarArray = new BitSet();
    Button chooseImageButton, convertImageButton, saveBinaryImageButton, invertButton;
    Bitmap preview,convertedImage;
    byte[] new_bytes = new byte[(ACTUAL_FAN_SIZE_PIXELS*NUMBER_OF_SCAN_LINES)/8]; //15360
    int h, w;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_image);

        imgFile = (TextView)findViewById(R.id.textView8) ;


        chooseImageButton = (Button)findViewById(R.id.button4);
        chosenImage = (ImageView)findViewById(R.id.imageView2);
        convertImageButton = (Button)findViewById(R.id.button7);
        saveBinaryImageButton =(Button)findViewById(R.id.button6);
        threseek=(SeekBar)findViewById(R.id.seekBar3);
        threseek.setMax(100);
        invertButton=(Button)findViewById(R.id.button8);


        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();

            }
        });
        convertImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                THRESHOLD_VALUE=(int)getTHRESHOLD_VALUE();
                convertImage();
            }
        });
        saveBinaryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
        threseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                THRESHOLD_VALUE = progress * 765 /100; //255*3 = 765 (r+g+b)
                String toaststring = "Threshold Value  :  " + String.valueOf(THRESHOLD_VALUE);
                //Toast.makeText(getApplicationContext(),toaststring, Toast.LENGTH_SHORT).show();
                chosenImage.setImageURI(selectedImage);
                convertImage();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        invertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invertColor();
            }
        });



    }

    protected void saveImage(){

        verifyStoragePermissions(this);
        f = new File(Environment.getExternalStorageDirectory(),
                "/test");
        try {
            f.createNewFile();
        } catch (IOException ex) {
            Log.e("io", ex.getMessage());
        }

        //String strFilePath = "C://FileIO//test";
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(new_bytes);
            Toast.makeText(getApplicationContext(),"File Saved!!", Toast.LENGTH_LONG).show();
        } catch(FileNotFoundException ex)
            {
                System.out.println("FileNotFoundException : " + ex);
            }
            catch(IOException ioe)
            {
                System.out.println("IOException : " + ioe);
            }
    }

    private void convertImage()
    {
      convertedImage = Bitmap.createBitmap(h,w,preview.getConfig());
      imageCartesianMatrix = new boolean[w][h];

        final Runnable rem1 = new Runnable() {
            @Override
            public void run() {
                im2bin(0, h/4);
            }
        };
        Thread firstThread = new Thread(rem1);
        firstThread.start();
        final Runnable rem2 = new Runnable() {
            @Override
            public void run() {
                im2bin(h/4, h/2);
            }
        };
        Thread secondThread = new Thread(rem2);
        secondThread.start();
        final Runnable rem3 = new Runnable() {
            @Override
            public void run() {
                im2bin(h/2, 3*h/4);
            }
        };
        Thread thirdThread = new Thread(rem3);
        thirdThread.start();
        final Runnable rem4 = new Runnable() {
            @Override
            public void run() {
                im2bin(3*h/4, h);
            }
        };
        Thread fourthThread = new Thread(rem4);
        fourthThread.start();

        while(fourthThread.isAlive()||thirdThread.isAlive()||secondThread.isAlive()||firstThread.isAlive());

        final Runnable pol1 = new Runnable() {
            @Override
            public void run() {
                cart2polar(0, NUMBER_OF_SCAN_LINES/4);
            }
        };

        Thread pfirstThread = new Thread(pol1);
        pfirstThread.start();

        final Runnable pol2 = new Runnable() {
            @Override
            public void run() {
                cart2polar(NUMBER_OF_SCAN_LINES/4, NUMBER_OF_SCAN_LINES/2);
            }
        };
        Thread psecondThread = new Thread(pol2);
        psecondThread.start();
        final Runnable pol3 = new Runnable() {
            @Override
            public void run() {
                cart2polar(NUMBER_OF_SCAN_LINES/2, 3*NUMBER_OF_SCAN_LINES/4);
            }
        };
        Thread pthirdThread = new Thread(pol3);
        pthirdThread.start();
        final Runnable pol4 = new Runnable() {
            @Override
            public void run() {
                cart2polar(3*NUMBER_OF_SCAN_LINES/4, NUMBER_OF_SCAN_LINES);
            }
        };
        Thread pfourthThread = new Thread(pol4);
        pfourthThread.start();


        while(pfourthThread.isAlive()||pthirdThread.isAlive()||psecondThread.isAlive()||pfirstThread.isAlive());
        //while(pfirstThread.isAlive()

        chosenImage.setImageBitmap(convertedImage);



        //new_bytes = imagePolarArray.toByteArray();
        //converting to four little endians

        byte tempByte;
        for (int i = 0;i<(new_bytes.length);i+=4)
        {
            tempByte=new_bytes[i];
            new_bytes[i]=new_bytes[i+3];
            new_bytes[i+3]=tempByte;
            tempByte=new_bytes[i+1];
            new_bytes[i+1]=new_bytes[i+2];
            new_bytes[i+2]=tempByte;
        }

        Toast.makeText(getApplicationContext(), "Conversion Finished!", Toast.LENGTH_LONG).show();


        //new_bytes = (String[])ArrayUtils.addAll(first, second);

    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }


    private void chooseImage()
    {
        verifyStoragePermissions(this);
        f = new File(Environment.getExternalStorageDirectory(),
                "/temporary_holder.jpg");
        try {
            f.createNewFile();
        } catch (IOException ex) {
            Log.e("io", ex.getMessage());
        }

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setDataAndType(imageUri, "image/*");
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX" , 1);
        intent.putExtra("aspectY", 1);
        //intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("outputX", TOTAL_FAN_SIZE_PIXELS*2);
        intent.putExtra("outputY", TOTAL_FAN_SIZE_PIXELS*2);
        //intent.putExtra("return-data", true);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode==RESULT_OK && requestCode == 1 && intent != null)
        {
            selectedImage = intent.getData();
            chosenImage.setImageURI(selectedImage);
            preview = ((BitmapDrawable)chosenImage.getDrawable()).getBitmap();
            h=preview.getHeight();
            w=preview.getWidth();
        }
    }

    protected boolean getPixel(int r, int t){
        double theta=(t*2*Math.PI)/NUMBER_OF_SCAN_LINES;
        double rad = (r*(h/2-1))/TOTAL_FAN_SIZE_PIXELS;
        Log.i("ReadyImageActivity","Theta : " + theta + " Rad : " + rad);
        return imageCartesianMatrix[(w/2)+(int)Math.round(rad*Math.cos(theta))][(h/2)-(int)Math.round(rad*Math.sin(theta))];


    }
    protected long getTHRESHOLD_VALUE()
    {
        long sum = 0;
        int len = 0;
        for (int i = 0; i < w;i += threskip)
        {
            for (int j = 0; j < h; j+= threskip)
            {
                sum+=(Color.red(preview.getPixel(i,j))+Color.green(preview.getPixel(i,j))+Color.blue(preview.getPixel(i,j)));
                len+=1;
            }

        }
        Toast.makeText(getApplicationContext(), "Threshold set to " + sum/len, Toast.LENGTH_LONG).show();
        return(sum/len);
    }
    protected void invertColor()
    {
        final Runnable rem1 = new Runnable() {
            @Override
            public void run() {
                invertPixel(0, h/4);
            }
        };
        Thread firstThread = new Thread(rem1);
        firstThread.start();
        final Runnable rem2 = new Runnable() {
            @Override
            public void run() {
                invertPixel(h/4, h/2);
            }
        };
        Thread secondThread = new Thread(rem2);
        secondThread.start();
        final Runnable rem3 = new Runnable() {
            @Override
            public void run() {
                invertPixel(h/2, 3*h/4);
            }
        };
        Thread thirdThread = new Thread(rem3);
        thirdThread.start();
        final Runnable rem4 = new Runnable() {
            @Override
            public void run() {
                invertPixel(3*h/4, h);
            }
        };
        Thread fourthThread = new Thread(rem4);
        fourthThread.start();
        while(fourthThread.isAlive()||thirdThread.isAlive()||secondThread.isAlive()||firstThread.isAlive());
        Toast.makeText(getApplicationContext(),"Inverted Colors!", Toast.LENGTH_LONG).show();

    }
    protected void invertPixel(int strt, int stp)
    {
        for(int i=strt; i<stp; i++) {
            for (int j = 0; j < h; j++) {
                if(imageCartesianMatrix[i][j]){
                    convertedImage.setPixel(i,j,Color.rgb(255,255,255)); //white
                    imageCartesianMatrix[i][j]=false;
                }
                else{
                    convertedImage.setPixel(i,j,Color.rgb(0,0,0));
                    imageCartesianMatrix[i][j]=true;
                }

            }
        }

    }

    protected void im2bin(int strt, int stp)
    {
        for(int i=strt; i<stp; i++)
        {
            for(int j=0;j<h;j++)
            {

                if((Color.red(preview.getPixel(i,j))+Color.green(preview.getPixel(i,j))+Color.blue(preview.getPixel(i,j)))>THRESHOLD_VALUE) //getPixel takes x and y arguments. so width first then height. not like matrix where row first then colum
                {
                    convertedImage.setPixel(i,j,Color.rgb(255,255,255)); //white
                    imageCartesianMatrix[i][j]=false; //go figure :/
                }
                else
                {
                    convertedImage.setPixel(i,j,Color.rgb(0,0,0));
                    imageCartesianMatrix[i][j]=true;
                }
            }
        }
    }

    public void setBit(int position, boolean value) {
        int byteLocation = position / 8;
        int bitLocation = 7 - (position % 8);
        byte tempByte = new_bytes[byteLocation];

        if (value)
            tempByte = (byte) (tempByte & ~(1 << bitLocation));
        else
            tempByte = (byte) (tempByte | (1 << bitLocation));

        new_bytes[byteLocation] = tempByte;
    }
    public void cart2polar(int strt, int stp)
    {
        for (int i = strt;i<stp;i++)
        {
            for (int j=0;j<ACTUAL_FAN_SIZE_PIXELS;j++)
            {
                //imagePolarArray[i][j]=getPixel(TOTAL_FAN_SIZE_PIXELS-j,i);
                //imagePolarArray.set((i*ACTUAL_FAN_SIZE_PIXELS+ACTUAL_FAN_SIZE_PIXELS-1-j),getPixel(j+temp-1,i));
                //new_bytes[(i*ACTUAL_FAN_SIZE_PIXELS+j)/8]=(byte)
                setBit((i*ACTUAL_FAN_SIZE_PIXELS+j),getPixel(TOTAL_FAN_SIZE_PIXELS-ACTUAL_FAN_SIZE_PIXELS+j,i));

            }
        }
    }


}
