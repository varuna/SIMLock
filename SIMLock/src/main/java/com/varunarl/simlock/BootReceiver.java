package com.varunarl.simlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Varuna on 6/2/13.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            if (!SimLockApplication.getInstance().getSecurityManager().validate())
                SecurityManager.lostMode(context);
        }
    }
}
