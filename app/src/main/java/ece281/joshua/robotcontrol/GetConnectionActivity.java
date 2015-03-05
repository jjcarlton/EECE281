package ece281.joshua.robotcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class GetConnectionActivity extends ActionBarActivity {

    public final static int REQUEST_ENABLE_BT = 1;
    //UUID for Serial Port Profile (SPP)

    DisplayScreen displayScreen;

    private static final String TAG = "bluetooth2";

    TextView txtArduino;
    SeekBar speedSlider;
    int speedSliderValue;
    Handler h;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    RobotState state;
    Timer updateTimer;
    boolean left = false;
    boolean right = false;
    boolean down = false;
    boolean up = false;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "30:14:11:14:09:19";
    public boolean bluetoothConnected = false;

    Button upButton;
    Button downButton;
    Button leftButton;
    Button rightButton;

    int speedLeft = 0;
    int speedRight = 0;

    public void clickUP(View view){
        if(state.getState() != state.FORWARD){
            state.setState(state.FORWARD);
        }
        else{
            state.setState(state.IDLE);
        }

        displayScreen.invalidate();

      //  sendData("100 100");
    }

    public void clickDOWN(View view){
        if(state.getState() != state.BACKWARD){
            state.setState(state.BACKWARD);
        }
        else {
            state.setState(state.IDLE);
        }
    }

    public void clickLEFT(View view){
      /// mConnectedThread.write("-100 100");
        state.setState(state.LEFT);
       // sendData("0 -100");

    }

    public void clickRIGHT(View view){
        state.setState(state.RIGHT);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_connection);
       //...............................................................................................................................................0 configureBluetooth();
        txtArduino = (TextView) findViewById(R.id.text_from_arduino);
        displayScreen = (DisplayScreen)findViewById(R.id.displayScreen);

        speedSlider = (SeekBar) findViewById(R.id.seekBar);
        speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
                speedSliderValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                }
        });

        state = new RobotState();
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());                                      // and clear
                          //  Log.d(TAG, sbprint);
                            txtArduino.setText(sbprint);            // update TextView

                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        updateTimer = new Timer();
        CommunciationTimer mtt = new CommunciationTimer();
        updateTimer.schedule(mtt, 200, 200);

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
                Log.d(TAG, "....Connection not ok....");
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        bluetoothConnected = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try{
            updateTimer.wait();
        }catch(InterruptedException e){

        }


        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    public void sendData(){


        if(state.getState() == state.FORWARD){
            speedLeft = 150 + speedSliderValue;
            speedRight = 150+  speedSliderValue;
        }
        if(state.getState() == state.BACKWARD){
            speedLeft = -150 -  speedSliderValue;
            speedRight = -150 -   speedSliderValue;
        }

        if(state.getState() == state.LEFT){
            speedLeft = -150 -  speedSliderValue;
            speedRight = 150 +   speedSliderValue;
        }

        if(state.getState() == state.RIGHT){
            speedLeft = 150 +  speedSliderValue;
            speedRight = -150 -   speedSliderValue;
        }if(state.getState() == state.IDLE){
            speedLeft = 0;
            speedRight = 0;
        }

        String toSend = Integer.toString(speedLeft);
        toSend += "n"+ Integer.toString(speedRight);
        Log.d(TAG, toSend);
        mConnectedThread.write(toSend);
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
          //  Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }


    public class CommunciationTimer extends TimerTask{
        public void run(){
         //   Log.d(TAG, "communcatioon timer triggered");
            if(bluetoothConnected){
                sendData();
                state.setState(state.IDLE);
            }


        }
    }




}
