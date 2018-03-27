package com.example.mandyyip.bt_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
    EditText editRed, editGreen, editBlue;
    TextView txtOriginal;
    CheckBox chk_white;
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
            // check values are integers only
            if (!TextUtils.isEmpty(editRed.getText().toString()) && !TextUtils.isEmpty(editGreen.getText().toString()) && !TextUtils.isEmpty(editBlue.getText().toString()))
            {
                Integer rr, gg, bb, brightness;
                rr = Integer.parseInt(editRed.getText().toString());
                gg = Integer.parseInt(editGreen.getText().toString());
                bb = Integer.parseInt(editBlue.getText().toString());
//                brightness = brightness.getProgress();
                // checks that the numbers are valid
                if(checkRGBisValid(rr, gg, bb))
                {
                    // set the text to show the values being sent to the lights
                    txtOriginal.setText("(" + rr.toString() + ", " + gg.toString() + ", " + bb.toString() + ")");

//                    // converts the values relative to the chosen brightness
//                    int newBrightnessR = convertToBrightness(rr, brightness);
//                    int newBrightnessG = convertToBrightness(gg, brightness);
//                    int newBrightnessB = convertToBrightness(bb, brightness);

                    // display the new values being sent after being changed to match the brightness
//                    txtAltered.setText("(" + newBrightnessR + ", " + newBrightnessG + ", " + newBrightnessB + ")");
                    // possibly will only need to send the brightness value over rather than converting the rbg values

                    String finalOutput = createOutput();
                    Toast.makeText(getApplicationContext(), finalOutput, Toast.LENGTH_SHORT).show();
                    try
                    {
                        bt_socket.getOutputStream().write(finalOutput.getBytes());
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Values are not integers", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // checks if the numbers are valid
    public boolean checkRGBisValid(int r, int g, int b)
    {
        // check if each is within range of 0 - 255
        if (r>=0 && r<=255)
        {
            if(g>=0 && g<=255)
            {
                if(b>=0 && b<=255)
                {
                    return true;
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "b is out of range", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "g is out of range", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "r is out of range", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public String createOutput()
    {
        // create the string to be sent back
        String sendBack = "";

        // get the alread aproved numbers
        String redText = editRed.getText().toString();
        String greText = editGreen.getText().toString();
        String bluText = editBlue.getText().toString();

        // check how long they are and if they are too short add a 0 to the start
        if (redText.length() < 2)
        {
            sendBack = sendBack + "00" + redText;
        }
        else if (redText.length() < 3)
        {
            sendBack = sendBack + "0" + redText;
        }
        else
        {
            sendBack = sendBack + redText;
        }

        // check how long they are and if they are too short add a 0 to the start
        if (greText.length() < 2)
        {
            sendBack = sendBack + "00" + greText;
        }
        else if (greText.length() < 3)
        {
            sendBack = sendBack + "0" + greText;
        }
        else
        {
            sendBack = sendBack + greText;
        }

        // check how long they are and if they are too short add a 0 to the start
        if (bluText.length() < 2)
        {
            sendBack = sendBack + "00" + bluText;
        }
        else if (bluText.length() < 3)
        {
            sendBack = sendBack + "0" + bluText;
        }
        else
        {
            sendBack = sendBack + bluText;
        }

        return sendBack;
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
        btn_dconnect = (Button)findViewById(R.id.btn_dconnect);
        brightness = (SeekBar)findViewById(R.id.skbar);
        lumn = (TextView)findViewById(R.id.txt_lumn);
        chk_white = findViewById(R.id.chkWhite);
        editRed = findViewById(R.id.editRed);
        editGreen = findViewById(R.id.editGreen);
        editBlue = findViewById(R.id.editBlue);
        txtOriginal = findViewById(R.id.txtOriginal);

        chk_white.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(chk_white.isChecked())
                {
                    editRed.setFocusable(false);
                    editGreen.setFocusable(false);
                    editBlue.setFocusable(false);

                    editRed.setText("255");
                    editGreen.setText("255");
                    editBlue.setText("255");
                }
                else
                {
                    editRed.setFocusable(true);
                    editRed.setFocusableInTouchMode(true);
                    editGreen.setFocusable(true);
                    editGreen.setFocusableInTouchMode(true);
                    editBlue.setFocusable(true);
                    editBlue.setFocusableInTouchMode(true);
                }
            }
        });

        btn_on.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed(); //method to turn on
                Toast.makeText(getApplicationContext(), "Turning on...", Toast.LENGTH_SHORT ).show();
            }
        });

        btn_dconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
                Toast.makeText(getApplicationContext(), "Disconnecting...", Toast.LENGTH_SHORT ).show();
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
