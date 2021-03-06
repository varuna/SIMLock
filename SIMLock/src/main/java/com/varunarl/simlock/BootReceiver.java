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
        else if (intent.getAction().equals("com.varunarl.simlock.LOST_DEVICE"))
        {
            Intent i = new Intent(context, LostModeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            context.startActivity(i);
        }
    }
}
