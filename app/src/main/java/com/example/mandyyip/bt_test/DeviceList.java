package com.example.mandyyip.bt_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class DeviceList extends AppCompatActivity {

    private BluetoothAdapter my_bluetooth = null;
    private Set <BluetoothDevice> paired_devices; //found in comments
    public static String EXTRA_ADDRESS = "device_address"; //found in comments

    Button btn_paired;
    ListView device_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        btn_paired = (Button)findViewById(R.id.btn);
        device_list = (ListView)findViewById(R.id.lst_view);

        my_bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (my_bluetooth == null)
        {
            //show message that device has no bt adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        }
        else
        {
            if (my_bluetooth.isEnabled())
            {

            }
            else
            {
                //ask user to turn bt on
                Intent turn_on_bt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turn_on_bt,1);
            }
        }

        btn_paired.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });
        }

    private void pairedDevicesList()
    {
        paired_devices = my_bluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (paired_devices.size() > 0)
        {
            for (BluetoothDevice bt : paired_devices)
            {
                //get name and address of device
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired BT Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        device_list.setAdapter(adapter);
        device_list.setOnItemClickListener(myListClickListener); //method called when device from list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            //get mac address of device, last 17 chars in the view
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            //make intent to start next activity
            Intent i = new Intent(DeviceList.this, ledControl.class);

            //change the activity
            i.putExtra(EXTRA_ADDRESS, address); //will be received at ledControl (class) Activity
            startActivity(i);
        }
    };
}
