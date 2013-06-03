package com.varunarl.simlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements DialogInterface.OnClickListener,ISIMLock {
    private SecurityManager mSecurityManager;
    private SecurityManager.SIMInfo mCurrentSIMInfo;
    private View mEditSIMInfoDialog;
    private TextView mCurrentPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mSecurityManager = SimLockApplication.getInstance().getSecurityManager();
        TextView currentSerial = (TextView) findViewById(R.id.textView_serial);
        mCurrentPhone = (TextView) findViewById(R.id.textView_phone);
        mCurrentSIMInfo = mSecurityManager.getSIMInformation();
        currentSerial.setText(String.format(getString(R.string.serial_number_label), mCurrentSIMInfo.serial));
        mCurrentPhone.setText(String.format(getString(R.string.phone_number_label), mCurrentSIMInfo.phone.isEmpty() ? "No MSISDN in SIM" : mSecurityManager.getSIMInformation().phone));
        ImageButton editInfo = (ImageButton) findViewById(R.id.edit_sim_info);
        EditText informNumber = (EditText) findViewById(R.id.editText_inform_phone);
        informNumber.setText(mCurrentSIMInfo.inform);
        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editSimInfo();
            }
        });
        mEditSIMInfoDialog = getLayoutInflater().inflate(R.layout.dialog_edit_sim_information, null, false);
        mSecurityManager.validate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_add_new_sim:
                EditText informNumber = (EditText) findViewById(R.id.editText_inform_phone);
                String s = informNumber.getText().toString();
                if (s != null && !s.equals(""))
                    mSecurityManager.addOwnerSIM(mCurrentSIMInfo.phone,s);
                else {
                    if (mSecurityManager.getLastInformPhoneNumber().equals("")) {
                        Toast.makeText(SettingsActivity.this, "Need to provide a number to inform", Toast.LENGTH_LONG).show();
                    } else {
                        mSecurityManager.addOwnerSIM(mCurrentSIMInfo.phone,mSecurityManager.getLastInformPhoneNumber());
                    }
                }
                break;
            case R.id.action_view_sim_list:
                startActivity(new Intent(SettingsActivity.this,AuthorizedSimListActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if (!SimLockApplication.getInstance().isOwnerAuthentic())
            SimLockApplication.getInstance().getNewOwnerLock(this,this, true);
        super.onPostResume();

        SimLockApplication.getInstance().getSecurityManager().validate();
    }

    @Override
    protected void onDestroy() {
        SimLockApplication.getInstance().release();
        super.onDestroy();
    }

    public void editSimInfo() {

        TextView t = (TextView) mEditSIMInfoDialog.findViewById(R.id.textView_current_serial);
        t.setText(String.format(getString(R.string.serial_number_label), mCurrentSIMInfo.serial));
        EditText e = (EditText) mEditSIMInfoDialog.findViewById(R.id.editText_new_phone);
        e.setText(mCurrentSIMInfo.phone);

        new AlertDialog.Builder(this)
                .setTitle("Edit SIM information")
                .setView(mEditSIMInfoDialog)
                .setPositiveButton("Save", this)
                .setNegativeButton("Cancel", this)
                .create()
                .show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                EditText e = (EditText) mEditSIMInfoDialog.findViewById(R.id.editText_new_phone);
                mCurrentSIMInfo.phone = e.getText().toString();
                mCurrentPhone.setText(String.format(getString(R.string.phone_number_label, mCurrentSIMInfo.phone)));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }

    @Override
    public void onAuthorizationChallenge(boolean status) {
        //DO nothing
    }
}
