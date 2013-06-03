package com.varunarl.simlock;

import android.app.ListActivity;
import android.os.Bundle;

/**
 * Created by Varuna on 6/3/13.
 */
public class AuthorizedSimListActivity extends ListActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(SimLockApplication.getInstance().getSecurityManager().getRegisteredSimListAdapter(this));
    }
}