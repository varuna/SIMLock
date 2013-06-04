package com.varunarl.simlock;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by Varuna on 6/3/13.
 */
public class AuthorizedSimListActivity extends ListActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setListAdapter(SimLockApplication.getInstance().getSecurityManager().getRegisteredSimListAdapter(this));
        getListView().setBackgroundColor(getResources().getColor(R.color.list_dark_gray));
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
    }
}