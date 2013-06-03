package com.varunarl.simlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Varuna on 6/1/13.
 */
public class LostModeActivity extends Activity implements  ISIMLock{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lost_device);

    }

    @Override
    protected void onResume() {
        SimLockApplication.getInstance().getNewOwnerLock(this,this, false);
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!SimLockApplication.getInstance().isOwnerAuthentic()) {
            SecurityManager.lostMode(this);
        }
    }

    @Override
    public void onAuthorizationChallenge(boolean status) {
        if (!status)
            SimLockApplication.getInstance().getSecurityManager().announceLostDevice();
    }
}