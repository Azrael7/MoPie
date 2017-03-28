package com.example.ares.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static android.R.attr.delay;

public class ReadyVideoActivity extends AppCompatActivity {


    int THRESHOLD_VALUE = 382, TOTAL_FAN_SIZE_PIXELS = 178, NUMBER_OF_SCAN_LINES =960 , ACTUAL_FAN_SIZE_PIXELS= 128, threskip = 20, h, w, numOfFrames,temp_counter;
    Button chooseVideoButton, convertVideoButton, saveVideoButton, sendButton;
    boolean[][] imageCartesianMatrix;
    TextView videoLocation, status;
    ProgressBar convProgress;
    MediaMetadataRetriever chopper;
    Bitmap frame, convertedFrame;
    ImageView videoFrame;
    byte[] new_bytes;
    File f;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_video);
        chooseVideoButton = (Button)findViewById(R.id.button13);
        videoLocation=(TextView)findViewById(R.id.textView9);
        videoFrame=(ImageView)findViewById(R.id.imageView);
        convertVideoButton = (Button)findViewById(R.id.button14);
        saveVideoButton=(Button)findViewById(R.id.button16);
        sendButton= (Button)findViewById(R.id.button18);
        status=(TextView)findViewById(R.id.textView10);
        convProgress = (ProgressBar)findViewById(R.id.progressBar4);
        convProgress.setMax(100);
        convProgress.setProgress(0);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        convertVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertVideo();
            }
        });

        saveVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVideo();
            }
        });




        chooseVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent,1);
            }
        });
    }
    protected static void verifyStoragePermissions(Activity activity) {
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
    protected void saveVideo(){

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

    private void convertImage(final Bitmap preview, final int frameNo)
    {
        Log.i("ReadyVideoACtivity", "entered image conversion with frame no " + frameNo);

        final Runnable pol1 = new Runnable() {
            @Override
            public void run() {
                cart2polar(preview, 0, NUMBER_OF_SCAN_LINES/4, frameNo);
            }
        };
        Thread pfirstThread = new Thread(pol1);
        pfirstThread.start();
        //Log.i("ReadyVideoACtivity", "pol thread 1 started");
        final Runnable pol2 = new Runnable() {
            @Override
            public void run() {
                cart2polar(preview, NUMBER_OF_SCAN_LINES/4, NUMBER_OF_SCAN_LINES/2, frameNo);
            }
        };
        Thread psecondThread = new Thread(pol2);
        psecondThread.start();
        //Log.i("ReadyVideoACtivity", "pol thread 2 started");
        final Runnable pol3 = new Runnable() {
            @Override
            public void run() {
                cart2polar(preview, NUMBER_OF_SCAN_LINES/2, 3*NUMBER_OF_SCAN_LINES/4, frameNo);
            }
        };
        Thread pthirdThread = new Thread(pol3);
        pthirdThread.start();
        //Log.i("ReadyVideoACtivity", "pol thread 3 started");
        final Runnable pol4 = new Runnable() {
            @Override
            public void run() {
                cart2polar(preview, 3*NUMBER_OF_SCAN_LINES/4, NUMBER_OF_SCAN_LINES, frameNo);
            }
        };
        Thread pfourthThread = new Thread(pol4);
        pfourthThread.start();
        Log.i("ReadyVideoACtivity", "pol threads  started");
        while(pfourthThread.isAlive()||pthirdThread.isAlive()||psecondThread.isAlive()||pfirstThread.isAlive());
        Log.i("ReadyVideoACtivity", "pol threads finsihed");
        Log.i("ReadyVideoACtivity", "FRAME FINSIHED. PROGRESS : " + (float)(frameNo*100/numOfFrames) + " %");
        convProgress.setProgress(frameNo*100/numOfFrames);


        //new_bytes = (String[])ArrayUtils.addAll(first, second);

    }
    protected void sendFile(final String servipaddr, final int servport, final String fileLocation){

        final Runnable rem2 = new Runnable() {
            @Override
            public void run() {
                Socket clisocket = null;

                try {
                    clisocket = new Socket(servipaddr, servport);

                    OutputStream os = clisocket.getOutputStream();
                    os.write(new_bytes, 0, new_bytes.length);
                    os.flush();
                    os.close();
                    clisocket.close();



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread cliThread2 = new Thread(rem2);
        //cliThread2.interrupt();
        cliThread2.start();


    }
    protected void cart2polar(Bitmap frame, int strt, int stp, int frameNo)
    {

        for (int i = strt;i<stp;i++)
        {
            for (int j=0;j<ACTUAL_FAN_SIZE_PIXELS;j++)
            {
                //imagePolarArray[i][j]=getPixel(TOTAL_FAN_SIZE_PIXELS-j,i);
                //imagePolarArray.set((i*ACTUAL_FAN_SIZE_PIXELS+ACTUAL_FAN_SIZE_PIXELS-1-j),getPixel(j+temp-1,i));
                //new_bytes[(i*ACTUAL_FAN_SIZE_PIXELS+j)/8]=(byte)
                setBit((i*ACTUAL_FAN_SIZE_PIXELS+j),getPixelNew(frame, TOTAL_FAN_SIZE_PIXELS-ACTUAL_FAN_SIZE_PIXELS+j,i), frameNo);

            }
        }
    }
    protected boolean getPixelNew(Bitmap preview, int r, int t)
    {
        double theta=(t*2*Math.PI)/NUMBER_OF_SCAN_LINES;
        double rad = (r*(h/2-1))/TOTAL_FAN_SIZE_PIXELS;
        int i = (w/2)+(int)Math.round(rad*Math.cos(theta));
        int j = (h/2)-(int)Math.round(rad*Math.sin(theta));
        return ((Color.red(preview.getPixel(i,j))+Color.green(preview.getPixel(i,j))+Color.blue(preview.getPixel(i,j)))>THRESHOLD_VALUE);

    }
    protected boolean getPixel(boolean[][] imageCartesianMatrix, int r, int t){
        double theta=(t*2*Math.PI)/NUMBER_OF_SCAN_LINES;
        double rad = (r*(h/2-1))/TOTAL_FAN_SIZE_PIXELS;
        //Log.i("ReadyImageActivity","Theta : " + theta + " Rad : " + rad);
        return imageCartesianMatrix[(w/2)+(int)Math.round(rad*Math.cos(theta))][(h/2)-(int)Math.round(rad*Math.sin(theta))];


    }

    protected void setBit(int position, boolean value, int frameNo) {
        int byteLocation = position / 8 + frameNo*15360; // adding frame number x 15360 is the sioze of one frame. probaly shoudl have put that as a constant above i guerss. oh well.;
        int bitLocation = 7 - (position % 8);
        byte tempByte = new_bytes[byteLocation];

        if (value)
            tempByte = (byte) (tempByte & ~(1 << bitLocation));
        else
            tempByte = (byte) (tempByte | (1 << bitLocation));

        new_bytes[byteLocation] = tempByte;
    }
    protected void convertVideo() {
        Log.i("ReadyVideoACtivity", "Entered conversion function");

        status.setText("Conversion started...");




        Bitmap fram = chopper.getFrameAtTime((long) (0));

        String time = chopper.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time );
        Log.i("ReadyVideoACtivity", "Time of video is " + timeInMillisec/1000 + "seconds");

       // fram = chopper.getFrameAtTime((long) (0));
        videoFrame.setImageBitmap(fram);

        h= fram.getHeight();
        w=fram.getWidth();
        Log.i("ReadyVideoACtivity", "h =" + h + " w = "  + w);
        h=w= Math.min(h,w);
        Log.i("ReadyVideoACtivity", "smallest value chosen" + h);
        numOfFrames = (int)(Math.floor((timeInMillisec*3)/200)+1);
        Log.i("ReadyVideoACtivity", "NUmber of frames is " + numOfFrames);


        new_bytes = new byte[(ACTUAL_FAN_SIZE_PIXELS*NUMBER_OF_SCAN_LINES*numOfFrames)/8]; //15360*no of frames
        Log.i("ReadyVideoACtivity", "newbytes created with no of bytes = " + new_bytes.length);
        temp_counter=0;
        int a=0;


        for (double i = 0; i<timeInMillisec-200; i+=(400/3)) {


            frame = chopper.getFrameAtTime((long) (i * 1000), MediaMetadataRetriever.OPTION_PREVIOUS_SYNC );
            Log.i("ReadyVideoACtivity", "Getting frame at time " + (long) (i * 1000) );
            if(frame.getHeight()>0) {
                Log.i("ReadyVideoACtivity", "FRAME NO  " + temp_counter + " received");
            }
            //videoFrame.setImageBitmap(frame);

            Log.i("ReadyVideoACtivity", "imageview changed to current frame");
             final Runnable img1 = new Runnable() {
                @Override
                public void run() {
                    convertImage(frame,temp_counter);
                }
            };

            Thread pfirstThread = new Thread(img1);
            pfirstThread.start();
            Log.i("ReadyVideoACtivity", "Thread for image  " + temp_counter + " started");
            temp_counter+=1;

            frame = chopper.getFrameAtTime((long) ((i+200/3) * 1000), MediaMetadataRetriever.OPTION_PREVIOUS_SYNC );
            Log.i("ReadyVideoACtivity", "Getting frame at time " + (long) ((i+200/3) * 1000) );
            if(frame.getHeight()>0) {
                Log.i("ReadyVideoACtivity", "FRAME NO  " + temp_counter + " received");
            }

            final Runnable img2 = new Runnable() {
                @Override
                public void run() {
                    convertImage(frame,temp_counter);
                }
            };

            Thread psecondThread = new Thread(img2);
            psecondThread.start();
            Log.i("ReadyVideoACtivity", "Thread for image  " + temp_counter + " started");
            temp_counter+=1;
            while(psecondThread.isAlive());

        }


        Log.i("ReadyVideoACtivity", "Starting endian conversion");

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
        Log.i("ReadyVideoACtivity", "endian conversion finished");

        //Toast.makeText(this, "Conversion Completed!", Toast.LENGTH_SHORT).show();
        status.setText("Conversion finished!");
        chopper.release();
        Log.i("ReadyVideoACtivity", "Conversion finished!");

    }
    protected void im2bin(Bitmap preview,boolean[][] imageCartesianMatrix, int strt, int stp)
    {

        for(int i=strt; i<stp; i++)
        {
            for(int j=0;j<h;j++)
            {

                if((Color.red(preview.getPixel(i,j))+Color.green(preview.getPixel(i,j))+Color.blue(preview.getPixel(i,j)))>THRESHOLD_VALUE) //getPixel takes x and y arguments. so width first then height. not like matrix where row first then colum
                {
                    //convertedFrame.setPixel(i,j,Color.rgb(255,255,255)); //white
                    imageCartesianMatrix[i][j]=false; //go figure :/
                }
                else
                {
                    //convertedFrame.setPixel(i,j,Color.rgb(0,0,0));
                    imageCartesianMatrix[i][j]=true;
                }
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case 1:
                if(resultCode==RESULT_OK && requestCode==1){
                    Log.i("ReadyVideoACtivity", "intent received");
                    String FilePath = data.getData().getPath();
                    videoLocation.setText(FilePath);
                    String uri = videoLocation.getText().toString();
                    Log.i("ReadyVideoACtivity", "video location set");
                    chopper = new MediaMetadataRetriever();
                    Log.i("ReadyVideoACtivity", "metadata obbject created");
                    chopper.setDataSource(uri);
                    Log.i("ReadyVideoACtivity", "video assigned to object");
                    FilePath = null;
                    //Toast.makeText(getApplicationContext(), "Object created", Toast.LENGTH_SHORT);
                    Bitmap frame = chopper.getFrameAtTime((long) (4000000), MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
                    Log.i("ReadyVideoACtivity", "first frame received");
                    videoFrame.setImageBitmap(frame);
                    Log.i("ReadyVideoACtivity", "frame set as preview");
                    //Toast.makeText(getApplicationContext(), "Object created", Toast.LENGTH_SHORT);
                }
                break;

        }
    }
}

