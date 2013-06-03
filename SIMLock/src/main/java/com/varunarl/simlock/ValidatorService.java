package com.varunarl.simlock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Varuna on 6/1/13.
 */
public class ValidatorService extends Service {

    private final static  String TAG = "SIM_VALIDATOR_SERVICE";

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Starting to check SIM validation");
        if (!SimLockApplication.getInstance().getSecurityManager().validate())
            SecurityManager.lostMode(this);
        return START_STICKY;
    }
}
