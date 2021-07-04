package com.example.outdoors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class ServiceRestartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        String action = intent.getAction();

        if(action != null){
            if(action.equals("restartservice")){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, BackgroundService.class));
                } else {
                    context.startService(new Intent(context, BackgroundService.class));
                }
            }else if(action.equals("stopservice")){
                Intent stopIntent = new Intent(context, BackgroundService.class);
                stopIntent.setAction("stopservice");
                context.startService(stopIntent);
            }
        }
    }

}
