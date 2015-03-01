package ece281.joshua.robotcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


public class GetConnectionActivity extends ActionBarActivity {

    public final static int REQUEST_ENABLE_BT = 1;
    //UUID for Serial Port Profile (SPP)
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //TAG for log debugging
    private static final String BLUETOOTH_SOCKET_TAG = "Bluetooth Connection";
    private static final String arduinoBluetoothModuleAddress = "30:14:11:14:09:19"; //Don't know yet
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    public void findBluetoothConnection(View view){
        configureBluetooth();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_connection);
    }

    public void configureBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            //Device Does not support bluetooth. Crash horribly probably
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() != 0){
            //Toast.makeText(getBaseContext(), "device(s) found", Toast.LENGTH_SHORT).show();
            // Loop through paired devices
            // mArrayAdapter = new ArrayAdapter();
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Toast.makeText(getBaseContext(), device.getName() + " " + device.getAddress(), Toast.LENGTH_LONG).show();
                Log.d(BLUETOOTH_SOCKET_TAG, device.getName() + " " + device.getAddress());
               // startMainActivity();
            }
        }else{
            Toast.makeText(getBaseContext(), "no devices found", Toast.LENGTH_SHORT).show();
        }


        BluetoothDevice btModuleDevice = mBluetoothAdapter.getRemoteDevice(arduinoBluetoothModuleAddress);

        try{
            btSocket = createBluetoothSocket(btModuleDevice);
        }catch(IOException ioe){
            Toast.makeText(getBaseContext(), "Something Fucked up at socket creation", Toast.LENGTH_LONG).show();
            finish();
        }

        mBluetoothAdapter.cancelDiscovery();

        Log.d(BLUETOOTH_SOCKET_TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(BLUETOOTH_SOCKET_TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(BLUETOOTH_SOCKET_TAG, "...Create Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(BLUETOOTH_SOCKET_TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    public void toggleLED(View view){

        String data = "1";

        byte[] msgBuffer = data.getBytes();

        Log.d(BLUETOOTH_SOCKET_TAG, "...Send data: " + data + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (arduinoBluetoothModuleAddress.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }


    public void startMainActivity(){
        Intent startMainIntent = new Intent(this, MainActivity.class);
        startActivity(startMainIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

}
