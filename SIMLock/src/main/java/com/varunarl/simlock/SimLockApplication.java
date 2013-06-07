package com.varunarl.simlock;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Varuna on 6/2/13.
 */
public class SimLockApplication extends Application {

    private static SimLockApplication instance;
    private final String PREF_INIT = "com.varunarl.simlock.INITIALIZED";
    private SecurityManager mSecurityManager;
    private LockDialogActivity.ILockModeActions mActionsContext;
    private boolean isOwnerAuthenticated;

    public static SimLockApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mSecurityManager = new SecurityManager(this);

        boolean init = getSharedPreferences(PREF_INIT, MODE_PRIVATE).getBoolean(PREF_INIT, false);
        if (!init) {
            getSharedPreferences(PREF_INIT, MODE_PRIVATE).edit().putBoolean(PREF_INIT, true).commit();
            mSecurityManager.addOwnerSIM(
                    SecurityManager.SIMType.GENERAL,
                    SecurityManager.SIMType.GENERAL.toString(),
                    mSecurityManager.getSIMInformation().phone,
                    getString(R.string.emergency_number));
        }
        setOwnerAuthenticated(false);
    }

    public SecurityManager getSecurityManager() {
        return mSecurityManager;
    }

    public void getNewOwnerLock(LockDialogActivity.ILockModeActions actions) {

        mActionsContext = actions;

        Intent i = new Intent((Context) actions, LockDialogActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!getSecurityManager().canAuthenticateOwner())
            i.setAction(LockDialogActivity.ACTION_ONE_BUTTON_DIALOG);
        else
            i.setAction(LockDialogActivity.ACTION_TWO_BUTTON_DIALOG);
        startActivity(i);
    }

    public LockDialogActivity.ILockModeActions getActionsContext() {
        return mActionsContext;
    }

    public void setOwnerAuthenticated(boolean ownerAuthenticated) {
        isOwnerAuthenticated = ownerAuthenticated;
    }

    public boolean isOwnerAuthentic() {
        return isOwnerAuthenticated;
    }

    public void release() {
        setOwnerAuthenticated(false);
        System.exit(0);
    }


}
