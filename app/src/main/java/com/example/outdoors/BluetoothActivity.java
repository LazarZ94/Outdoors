/*

Klase za upravljanje BT handshake-om

 */


package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothActivity extends BaseDrawerActivity {

    private String TAG = "BT ACTIVITY";

    static private int REQUEST_ENABLE_BT = 0;
    static private int REQUEST_COARSE_LOCATION = 1;
    static private int REQUEST_LOCATION_SERVICE = 2;

    private UUID uuid = UUID.fromString("130b86dc-5933-48ba-a4d4-2f5a38032c1d");
    private String NAME = "Outdoors";

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    ArrayList<String> deviceNames = new ArrayList<>();
    ArrayAdapter<String> adapter;

    FirebaseFirestore db = DBAuth.getInstance().getDB();

    Comms comms;

    User currUser = UserList.getInstance().getCurrentUser();
    String currId = UserList.getInstance().getCurrentUserID();

    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if(message.what == MESSAGE_READ){
                byte[] readBuff = (byte[]) message.obj;
                final String userID = new String(readBuff, 0, message.arg1);
                String username = UserList.getInstance().getUser(userID).getUsername();
                String promptMsg = "Accept friend request?";
                if(currUser.friends.contains(userID)){
                    new AlertDialog.Builder(BluetoothActivity.this)
                            .setTitle(username)
                            .setMessage("Friend already added")
                            .setPositiveButton(R.string.OK, null)
                            .show();
                }else {
                    new AlertDialog.Builder(BluetoothActivity.this)
                            .setTitle(username)
                            .setMessage(promptMsg)
                            .setPositiveButton(R.string.acceptBTReq, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    acceptFriendRequest(userID);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
            }
            return true;
        }
    });

    private void acceptFriendRequest(String userID){
        User other = UserList.getInstance().getUser(userID);
        ArrayList<String> currFReq = new ArrayList<>(currUser.friendRequests);
        currFReq.remove(userID);
        db.collection("users").document(currId).update("friendRequests", currFReq);
        ArrayList<String> otherSentFR = new ArrayList<>(other.sentFriendRequests);
        otherSentFR.remove(currId);
        db.collection("users").document(userID).update("sentFriendRequests", otherSentFR);
        ArrayList<String> currFriends = new ArrayList<>(currUser.friends);
        currFriends.add(userID);
        db.collection("users").document(currId).update("friends", currFriends);
        ArrayList<String> otherFriends = new ArrayList<>(other.friends);
        otherFriends.add(currId);
        db.collection("users").document(userID).update("friends", otherFriends);
        UserList.getInstance().updateUsers(BluetoothActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_bluetooth);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_bluetooth, contentLayout);

        btAdapter.disable();

        Switch toggleBT = (Switch) findViewById(R.id.toggleBT);
        toggleBT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(!btAdapter.isEnabled()){
                        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBT, REQUEST_ENABLE_BT);
                    }
                }else{
                    btAdapter.disable();
                    deviceNames.removeAll(deviceNames);
                }
            }
        });



        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        ListView devices = (ListView) findViewById(R.id.btdevices);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceNames);

        devices.setAdapter(adapter);

        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ConnectThread client = new ConnectThread(deviceList.get(i));
                client.start();
            }
        });

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.add(device);
                deviceNames.add(device.getName());
                adapter.notifyDataSetChanged();
            }
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            Toast.makeText(this, "BT turned on", Toast.LENGTH_SHORT).show();
            Intent discInt = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discInt.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            AcceptThread server = new AcceptThread();
            server.start();
            startActivity(discInt);
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BluetoothActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION);
            }
            boolean starting = btAdapter.startDiscovery();
            Toast.makeText(BluetoothActivity.this, "Starting : " + starting, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btAdapter.disable();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread(){
            BluetoothServerSocket tmpSock = null;

            try{
                tmpSock = btAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, uuid);
            }catch (IOException e){
                Log.d(TAG, "Socket failed", e);
            }
            serverSocket = tmpSock;
        }

        public void run(){
            BluetoothSocket socket = null;
            while (true){
                try{
                    socket = serverSocket.accept();
                }catch (IOException e){
                    Log.d(TAG, "Socket failed", e);
                    break;
                }

                if(socket != null){
                    comms = new Comms(socket);
                    comms.start();
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel(){
            try{
                serverSocket.close();
            }catch (IOException e){
                Log.e(TAG, "Socket failed", e);
            }
        }

    }


    private class ConnectThread extends Thread {
        private final  BluetoothSocket btSocket;
        private final BluetoothDevice btDevice;

        public ConnectThread(BluetoothDevice dev){
            BluetoothSocket tmpSock = null;
            btDevice = dev;

            try{
                tmpSock = dev.createRfcommSocketToServiceRecord(uuid);
            }catch (IOException e){
                Log.d(TAG, "Socket create failed", e);
            }

            btSocket = tmpSock;
        }

        public void run(){
            btAdapter.cancelDiscovery();

            try{
                btSocket.connect();
            }catch (IOException connExcept){
                try{
                    btSocket.connect();
                }catch (IOException closeExcept){
                    Log.d(TAG, "Couldnt close socket", closeExcept);
                }
                return;
            }
            comms = new Comms(btSocket);
            comms.start();
            comms.write(UserList.getInstance().getCurrentUserID().getBytes());
        }

        public void cancel(){
            try{
                btSocket.close();
            }catch (IOException e){
                Log.d(TAG, "Couldnt close socket", e);
            }
        }
    }


    private class Comms extends Thread{

        private final BluetoothSocket btSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        byte[] buffer;


        public Comms(BluetoothSocket socket){
            btSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    Message readMsg = handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try{
                outputStream.write(bytes);
                Message writtenMsg = handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer);
                writtenMsg.sendToTarget();
            }catch (IOException e){
                Log.d(TAG, "Error sending data", e);
                Message writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldnt send data");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        public void cancel(){
            try{
                btSocket.close();
            }catch (IOException e){
                Log.d(TAG, "Couldnt close socket", e);
            }
        }
    }
}