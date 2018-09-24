package com.github.soiyeah.lightx;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    ToggleButton led1,led2,led3,led4,led5,led6;
    TextView txt_connected;    // connection status
    private final String DEVICE_NAME="LightX";
    private final String DEVICE_ADDRESS="98:D3:32:30:E4:31";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");   //Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;      // Currently not in use
    private InputStream inputStream;
    boolean deviceConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        led1 = findViewById(R.id.btn1);
        led2 = findViewById(R.id.btn2);
        led3 = findViewById(R.id.btn3);
        led4 = findViewById(R.id.btn4);
        led5 = findViewById(R.id.btn5);
        led6 = findViewById(R.id.btn6);
        txt_connected = findViewById(R.id.txt_connected);

        ((RadioGroup) findViewById(R.id.toggleGroup)).setOnCheckedChangeListener(ToggleListener);


        beginStart();

        if(!deviceConnected)        // disable buttons if not connected
        {
            disableButtons();
            txt_connected.setText("Couldn't connect to LightX");
            //txt_connected.setTextColor();
        }
        else
        {
            txt_connected.setText("Connected to LightX");
        }

    }


    public void disableButtons()
    {
        led1.setEnabled(false);
        led2.setEnabled(false);
        led3.setEnabled(false);
        led4.setEnabled(false);
        led5.setEnabled(false);
        led6.setEnabled(false);
    }

    public void enableButtons()
    {
        led1.setEnabled(true);
        led2.setEnabled(true);
        led3.setEnabled(true);
        led4.setEnabled(true);
        led5.setEnabled(true);
        led6.setEnabled(true);
    }

    static final RadioGroup.OnCheckedChangeListener ToggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                view.setChecked(view.getId() == i);
            }
        }
    };


    public void modeManual(View view) {                               // Control leds manually
        ((RadioGroup)view.getParent()).check(view.getId());

        if(deviceConnected)
        {
            enableButtons();
        }
        Toast.makeText(getApplicationContext(),"manual mode",Toast.LENGTH_SHORT).show();

    }

    public void modeMsync(View view) {                              // Sync leds with music
        ((RadioGroup)view.getParent()).check(view.getId());

        disableButtons();
        Toast.makeText(getApplicationContext(),"m sync mode",Toast.LENGTH_SHORT).show();

    }

    public void btnLed1(View view) // control led 1
    {
        led1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) // The toggle is enabled
                {
                    sendData("1H");
                } else {
                    sendData("1L");
                }
            }
        });
    }

    public void btnLed2(View view) // control led 2
    {
        led2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) // The toggle is enabled
                {
                    sendData("2H");
                } else {
                    sendData("2L");
                }
            }
        });
    }

    public void btnLed3(View view) // control led 3
    {
        led3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) // The toggle is enabled
                {
                    sendData("3H");
                } else {
                    sendData("3L");
                }
            }
        });
    }

    public void btnLed4(View view) // control led 4
    {
        led4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) // The toggle is enabled
                {
                    sendData("4H");
                } else {
                    sendData("4L");
                }
            }
        });
    }

    public void btnLed5(View view) // control led 5
    {
        led5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) // The toggle is enabled
                {
                    sendData("5H");
                } else {
                    sendData("5L");
                }
            }
        });
    }

    public void btnLed6(View view) // control led 6
    {
        led6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) // The toggle is enabled
                {
                    sendData("6H");
                } else {
                    sendData("6L");
                }
            }
        });
    }


    public boolean BTinit()         // initialise Bluetooth.
    {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesn't Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return connected;
    }


    public void beginStart()
    {
        if(BTinit())
        {
            if(BTconnect())
            {
                deviceConnected=true;
                Toast.makeText(getApplicationContext(),"Connected successfully",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendData(String str)
    {
        try {
            Thread.sleep(100);
            outputStream.write(str.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       // textView.append("\nSent Data:"+str+"\n");
    }

/*
    public void onClickSend(View view)
    {
        String string = editText.getText().toString();
        //string.concat("\n");
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
      //  textView.append("\nSent Data:"+string+"\n");
    }
*/

    public void onClickStop(View view) throws IOException
    {
        outputStream.close();
        inputStream.close();
        socket.close();
        deviceConnected=false;
      //  textView.append("\nConnection Closed!\n");

    }

}
