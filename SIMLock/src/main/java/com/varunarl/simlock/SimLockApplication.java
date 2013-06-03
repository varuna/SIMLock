package com.varunarl.simlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Varuna on 6/2/13.
 */
public class SimLockApplication extends Application {

    private static SimLockApplication instance;
    private final String PREF_INIT = "com.varunarl.simlock.INITIALIZED";
    private SecurityManager mSecurityManager;
    private OwnerLock mLock;

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
            mSecurityManager.addOwnerSIM(mSecurityManager.getSIMInformation().phone,"911");
        }
    }

    public SecurityManager getSecurityManager() {
        return mSecurityManager;
    }

    public void getNewOwnerLock(Activity context, ISIMLock ilocker, boolean isCancelable) {
        mLock = new OwnerLock(context,ilocker, isCancelable);
        mLock.lock();
    }

    public boolean isOwnerAuthentic() {
        if (mLock != null)
            return mLock.isAuthenticated();
        return false;
    }

    public void release() {
        if (mLock != null) {
            mLock.release();
            mLock = null;
        }
        System.exit(0);
    }

    private class OwnerLock implements DialogInterface.OnClickListener {
        private AlertDialog mAlertDialog;
        private Activity mActivityContext;
        private boolean isAuthenticated;
        private boolean isCancelable;
        private ISIMLock mLocker;

        OwnerLock(Activity context,ISIMLock locker, boolean isCancelable) {
            mActivityContext = context;
            isAuthenticated = false;
            mAlertDialog = createDialog(isCancelable);
            this.isCancelable = isCancelable;
            mLocker = locker;
        }

        private AlertDialog createDialog(boolean isCancelable) {
            if (mAlertDialog != null)
                mAlertDialog.dismiss();
            AlertDialog.Builder b = new AlertDialog.Builder(mActivityContext);
            b.setCancelable(isCancelable);
            LayoutInflater in = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = in.inflate(R.layout.screen_lock, null, false);
            b.setView(v);
            b.setTitle("Owner Lock");
            if (mSecurityManager.isOwnerLockEnabled())
                b.setPositiveButton(R.string.unlock_screen_button_label, this);
            else{
                isCancelable = false;
                b.setPositiveButton(R.string.set_owner_passcode_button_label, this);
            }
            if (isCancelable)
                b.setNegativeButton(R.string.cancel_button_label, this);
            return b.create();
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            switch (i) {
                case DialogInterface.BUTTON_NEGATIVE:
                    mActivityContext.finish();
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    EditText et = (EditText) mAlertDialog.getWindow().getDecorView().findViewById(R.id.owner_lock);
                    if (mSecurityManager.isOwnerLockEnabled()) {
                        if (!mSecurityManager.authenticateOwnerMode(et.getText().toString())) {
                            if (isCancelable)
                                mActivityContext.finish();
                        } else {
                            isAuthenticated = true;
                            if (!isCancelable)
                                mActivityContext.finish();
                        }
                    } else {
                        mSecurityManager.setOwnerScreenLock(et.getText().toString());
                        isAuthenticated = true;
                    }
                    break;
                default:
                    mActivityContext.finish();
                    break;
            }
            mLocker.onAuthorizationChallenge(isAuthenticated);
        }

        public void lock() {
            mAlertDialog.show();
        }

        public void release() {
            isAuthenticated = false;
        }

        public boolean isAuthenticated() {
            return isAuthenticated;
        }
    }
}
