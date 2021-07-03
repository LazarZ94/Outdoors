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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.rpc.context.AttributeContext;

import java.util.ArrayList;

public class BackgroundService extends Service {

    UserList userListInst = UserList.getInstance();

    String currID = userListInst.getCurrentUserID();

    String TAG = "OUTDOORS BACKGROUND SERVICE";

    FirebaseFirestore db = DBAuth.getInstance().getDB();

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
                int invCounter = 0;
                while (true){
                    if(!bgActive){
                        return;
                    }
                    try{
                        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            stopForeground(true);
                            isInBg = false;
                        }else{
                            ArrayList<User> onlineUsers = userListInst.getOnlineUsers();
                            if(!isInBg){
                                setForeground();
                                isInBg = true;
                                for(String userID : userListInst.getCurrentUser().getFriends()){
                                    User user = userListInst.getUser(userID);
                                    if(onlineUsers.contains(user)){
                                        onlineFriends.add(user);
                                    }
                                }
                            }else{
                                for(String userID : userListInst.getCurrentUser().getFriends()){
                                    User user = userListInst.getUser(userID);
                                    if(onlineUsers.contains(user)){
                                        if(!onlineFriends.contains(user)){
                                            sendFriendOnlineNotif(user);
                                            onlineFriends.add(user);
                                            break;
                                        }
                                    }
                                }

                            }
                            if(invCounter > 3){
                                checkInvites();
                            }
                        }
                        invCounter++;
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void checkInvites(){
        DocumentReference userRef = db.collection("users").document(currID);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    User user = doc.toObject(User.class);
                    for(Invite inv : user.getPlanInvites()){
                        if(!inv.isSeen()){
                            getInvitePlan(inv);
                            inv.setSeen();
                            break;
                        }
                    }
                    db.collection("users").document(currID).update("planInvites", user.getPlanInvites());
                }
            }
        });
    }

    private void getInvitePlan(Invite inv){
        DocumentReference planRef = db.collection("plans").document(inv.inviteID);
        planRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    Plan plan = doc.toObject(Plan.class);

                    sendPlanInviteNotification(plan);

                }
            }
        });
    }

    private void sendPlanInviteNotification(Plan plan){
        String CHANNEL_ID = "plan.invite.channel";
        String CHANNEL_NAME = "New plan invite channel";

        createNotificationChannel(CHANNEL_ID, CHANNEL_NAME);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_person_pin_circle_24)
                .setContentTitle("Invited to " + plan.planTitle + " by " + userListInst.getUser(plan.createdBy).getUsername())
                .setContentText("Scheduled for " + plan.date)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE);

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(54321, builder.build());
    }

    private void sendFriendOnlineNotif(User user){
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

        if(intent.getAction() != null && intent.getAction().equals("stopservice")){
            stopForeground(true);
            stopSelf();
        }

        printActive();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
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
