package com.varunarl.simlock;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Varuna on 6/3/13.
 */
public class AuthorizedSimListActivity extends ListActivity implements ListView.OnItemLongClickListener, DialogInterface.OnClickListener {

    private SecurityManager.SIMInfo info;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setListAdapter(SimLockApplication.getInstance().getSecurityManager().getRegisteredSimListAdapter(this));
        getListView().setBackgroundColor(getResources().getColor(R.color.list_dark_gray));
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        info = (SecurityManager.SIMInfo) getListAdapter().getItem(position);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.record_delete_title))
                .setMessage(String.format(getString(R.string.record_delete_prompt_message), info.serial))
                .setPositiveButton("De-Authorize", this)
                .setNegativeButton("Keep", this)
                .create()
                .show();
        return false;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE){
            SimLockApplication.getInstance().getSecurityManager().removeRegisteredSim(info);
            info = null;
        }
    }
}