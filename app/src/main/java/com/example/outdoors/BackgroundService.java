package com.example.outdoors;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.rpc.context.AttributeContext;

import java.util.ArrayList;

public class BackgroundService extends Service {

    UserList userListInst = UserList.getInstance();

    String TAG = "OUTDOORS BACKGROUND SERVICE";

    Thread bgT;

    boolean bgActive = false;

    boolean isInBg = false;

    ArrayList<User> onlineFriends = new ArrayList();


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        bgT = new Thread(new Runnable(){
            @Override
            public void run() {
                Log.w(TAG , "IN PRINTACTIVE RUN");
                while (true){
                    if(!bgActive){
                        Log.w(TAG , "STOPPING THREAD ");
                        return;
                    }
                    try{
                        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            Log.w(TAG , "PRINT FROM SERVICE FOREGROUND");
                            Log.w(TAG, userListInst.getUserList().toString());
                            stopForeground(true);
                            isInBg = false;
                        }else{
                            Log.w(TAG , "PRINT FROM SERVICE BACKGROUND");
                            Log.w(TAG, userListInst.getUserList().toString());
                            ArrayList<User> onlineUsers = userListInst.getOnlineUsers();
                            Log.w(TAG + " CURRENT USERID", DBAuth.getInstance().getAuth().getCurrentUser().getUid());
                            if(!isInBg){
                                Log.w(TAG, "FIRST LOOP");
                                setForeground();
                                isInBg = true;
                                //if(userListInst.getCurrentUser() != null){
                                    for(String userID : userListInst.getCurrentUser().getFriends()){
                                        User user = userListInst.getUser(userID);
                                        if(onlineUsers.contains(user)){
                                            onlineFriends.add(user);
                                        }
                                    }
                                //}
                            }else{
                                for(String userID : userListInst.getCurrentUser().getFriends()){
                                    Log.w(TAG, "User id " + userID);
                                    User user = userListInst.getUser(userID);
                                    if(onlineUsers.contains(user)){
                                        Log.w(TAG, "User id CONTAINED ONLINE" + userID);
                                        Log.w(TAG + "ONLINE FRIENDS", onlineFriends.toString());
                                        if(!onlineFriends.contains(user)){
                                            Log.w(TAG, "NEW FRIEND ONLINE");
                                            sendFriendOnlineNotif(user);
                                            onlineFriends.add(user);
                                            break;
                                            //TODO update online friends
                                        }
                                    }
                                }
                            }

                        }
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendFriendOnlineNotif(User user){
        //TODO Intent top show user on map

        String CHANNEL_ID = "friend.online.channel";
        String CHANNEL_NAME = "New online friend channel";

        createNotificationChannel(CHANNEL_ID, CHANNEL_NAME);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_person_pin_circle_24)
                .setContentTitle(user.getUsername() + " is online!")
                .setContentText("textContent")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE);

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(15435, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        if(intent.getAction() != null && intent.getAction().equals("stopservice")){
            Log.w(TAG, "STOPPING SERVICE");
            stopForeground(true);
            stopSelf();
        }

        printActive();

        //setForeground();


        Log.w(TAG , "AFTER PRINT ACTIVE");
        Toast.makeText(this, "after print active", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        bgActive = false;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceRestartReceiver.class);
        this.sendBroadcast(broadcastIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void printActive(){
        Log.w(TAG , "IN PRINTACTIVE");
        bgActive = true;
        if(!bgT.isAlive()){
            bgT.start();
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setForeground(){
        Intent intent = new Intent(this, MainScreenActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,   PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT >= 26){
//            String NOTIFICATION_CHANNEL_ID = "channel.id";
//            String channelName = "Foreground service channel";
//            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//
//            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            nm.createNotificationChannel(chan);

            String NOTIFICATION_CHANNEL_ID = "foreground.notification.channel";
            String CHANNEL_NAME = "Foreground service channel";

            createNotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

            Intent stopIntent = new Intent(this, ServiceRestartReceiver.class);
            stopIntent.setAction("stopservice");
            PendingIntent stopPending = PendingIntent.getBroadcast(this, 0, stopIntent, 0);


            builder.setSmallIcon(R.drawable.ic_baseline_person_pin_circle_24);
            builder.setTicker("App info string");
            builder.setContentIntent(pi);
            builder.setContentTitle("Background Service Running");
            builder.setContentText("Scanning for new objects");
            builder.setOngoing(true);
            builder.setOnlyAlertOnce(true);
            builder.addAction(R.drawable.ic_baseline_stop_24, "STOP", stopPending);

            Notification notification = builder.build();


            startForeground(1, notification);

        }else{
            startForeground(2, new Notification());
        }

    }

    private void createNotificationChannel(String channelId, String channelName){
        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            nm.createNotificationChannel(chan);
        }
    }



}
