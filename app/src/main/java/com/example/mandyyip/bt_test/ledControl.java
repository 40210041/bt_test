package com.example.mandyyip.bt_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;
import java.lang.String;

public class ledControl extends AppCompatActivity {

    Button btn_on, btn_off, btn_dconnect;
    SeekBar brightness;
    TextView lumn; //created to fix "lumn" var error
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter my_bluetooth = null;
    BluetoothSocket bt_socket = null;
    private boolean is_bt_connected = false;
    static final UUID my_uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private class ConnectBT extends AsyncTask<Void, Void, Void> //UI thread
    {
        private boolean ConnectSuccess = true; //if here then almost connected

        @Override
        protected void onPreExecute()
        {
            //show progress dialog
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait...");
        }

        @Override
        //while progress dialog is shown, connection done in bg
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (bt_socket == null || !is_bt_connected)
                {
                    //get mobile bt device
                    my_bluetooth = BluetoothAdapter.getDefaultAdapter();
                    //connect to devices address and checks if available
                    BluetoothDevice dispositivo = my_bluetooth.getRemoteDevice(address);
                    //create RFCOMM (SPP) connection
                    bt_socket = dispositivo.createInsecureRfcommSocketToServiceRecord(my_uuid);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    //start connection
                    bt_socket.connect();
                }
            }

            catch (IOException e)
            {
                //if try failed, you can check exception here
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        //after doInBackground, check if everything is fine
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                //msg(method) is created later
                msg("Connection Failed. Is it an SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                is_bt_connected = true;
            }
            progress.dismiss();
        }
    }


    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void Disconnect()
    {
        //if the socket is busy
        if (bt_socket != null)
        {
            try
            {
                //close connection
                bt_socket.close();
            }
            catch (IOException e) {
                msg("Error");
            }
        }
        finish();
    }

    private void turnOffLed()
    {
        if (bt_socket != null)
        {
            try
            {
                bt_socket.getOutputStream().write("TF".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (bt_socket != null)
        {
            try
            {
                bt_socket.getOutputStream().write("TO".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        //found in comments – calls ConnectBT method
        ConnectBT bt = new ConnectBT();
        bt.execute();

        //receive address of bt device
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        //view of ledControl layout
        setContentView(R.layout.activity_led_control);

        //call widgets
        btn_on = (Button)findViewById(R.id.btn_on);
        btn_off = (Button)findViewById(R.id.btn_off);
        btn_dconnect = (Button)findViewById(R.id.btn_dconnect);
        brightness = (SeekBar)findViewById(R.id.skbar);
        lumn = (TextView)findViewById(R.id.txt_lumn);


        btn_on.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed(); //method to turn on
                Toast.makeText(getApplicationContext(), "Turning on...", Toast.LENGTH_LONG ).show();
            }
        });

        btn_off.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOffLed(); //method to turn off
                Toast.makeText(getApplicationContext(), "Turning off...", Toast.LENGTH_LONG ).show();
            }
        });

        btn_dconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
                Toast.makeText(getApplicationContext(), "Disconnecting...", Toast.LENGTH_LONG ).show();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               if (fromUser == true)
               {
                   lumn.setText(String.valueOf(progress));
               }
               try
               {
                   bt_socket.getOutputStream().write(String.valueOf(progress).getBytes());
               }
               catch (IOException e)
               {

               }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
