package com.example.ares.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;



public class MainActivity extends AppCompatActivity {

    TextView textFile;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textFile = (TextView)findViewById(R.id.textView6);
        final Button sendbutton = (Button)findViewById(R.id.button);
        final Button choosebutton = (Button)findViewById(R.id.button2);
        final Button sendButton2 = (Button)findViewById(R.id.button3);
        final EditText ipaddr = (EditText)findViewById(R.id.ipaddr);
        final EditText port = (EditText)findViewById(R.id.port);
        final EditText message = (EditText)findViewById(R.id.message);
        final Button chooseImageButton = (Button)findViewById(R.id.button5);
        final Button chooseVideoButton = (Button)findViewById(R.id.button9);




        choosebutton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
               intent.setType("file/*");
               startActivityForResult(intent,1);

           }
       });

        chooseVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.example.ares.myapplication.ReadyVideoActivity");
                startActivity(intent);
            }
        });

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(ipaddr.getText().toString(), Integer.parseInt(port.getText().toString()), message.getText().toString());
            }
        });
        sendButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendFile(ipaddr.getText().toString(), Integer.parseInt(port.getText().toString()), textFile.getText().toString());


            }
        });

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.example.ares.myapplication.ReadyImageActivity");
                startActivity(intent);

            }
        });






    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if ((checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)&&(checkSelfPermission(android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
                    == PackageManager.PERMISSION_GRANTED)&&checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                return true;
            } else {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }



    public void sendFile(final String servipaddr, final int servport, final String fileLocation){

        final Runnable rem2 = new Runnable() {
            @Override
            public void run() {
                Socket clisocket = null;

                    try {
                        clisocket = new Socket(servipaddr, servport);


                        File file = new File(fileLocation);

                        byte[] bytes = new byte[(int) file.length()];
                        BufferedInputStream bis;
                        bis = new BufferedInputStream(new FileInputStream(file));
                        bis.read(bytes, 0, bytes.length);
                        OutputStream os = clisocket.getOutputStream();
                        os.write(bytes, 0, bytes.length);
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
    public void sendData(final String servipaddr, final int servport, final String theData) {



            final Runnable rem = new Runnable() {
                @Override
                public void run() {


                    Socket clisocket = null;
                    try {
                        clisocket = new Socket(servipaddr, servport);
                        OutputStream os = clisocket.getOutputStream();
                        PrintWriter output = new PrintWriter(os);
                        output.print(theData.substring(0, 2));
                        output.flush();
                        output.close();
                        clisocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            };

            Thread cliThread = new Thread(rem);
       // cliThread.interrupt();
            cliThread.start();

        }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch(requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    String FilePath = data.getData().getPath();
                    textFile.setText(FilePath);
                }
                break;

        }
    }
    }




