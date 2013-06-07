package com.varunarl.simlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by vlekamwasam on 6/6/13.
 */
public class LockDialogActivity extends Activity implements View.OnClickListener {

    public final static String ACTION_TWO_BUTTON_DIALOG = "com.varunarl.simlock.ACTION_TWO_BUTTON_DIALOG";
    public final static String ACTION_ONE_BUTTON_DIALOG = "com.varunarl.simlock.ACTION_ONE_BUTTON_DIALOG";
    private ILockModeActions mActionsContext;
    private EditText mLockCodeEditText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getIntent().getAction().equals(ACTION_ONE_BUTTON_DIALOG))
            setContentView(R.layout.set_owner_passcode);
        else
            setContentView(R.layout.screen_lock);

        setTheme(android.R.style.Theme_Dialog);

        Button positive = (Button) findViewById(R.id.positive_button);
        Button negative = (Button) findViewById(R.id.negative_button);
        Button neutral = (Button) findViewById(R.id.neutral_button);
        mLockCodeEditText = (EditText) findViewById(R.id.owner_lock_edit_text);
        mActionsContext = SimLockApplication.getInstance().getActionsContext();

        if (getIntent().getAction().equals(ACTION_TWO_BUTTON_DIALOG)) {
            positive.setOnClickListener(this);
            negative.setOnClickListener(this);
        } else if (getIntent().getAction().equals(ACTION_ONE_BUTTON_DIALOG))
            neutral.setOnClickListener(this);

    }

    public String getTypedLockCode() {
        return mLockCodeEditText.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positive_button:
                mActionsContext.onPositiveButton(LockDialogActivity.this);
                break;
            case R.id.negative_button:
                mActionsContext.onNegativeButton(LockDialogActivity.this);
                break;
            case R.id.neutral_button:
                mActionsContext.onNeutralButton(LockDialogActivity.this);
                break;
        }
    }

    @Override
    public void onAttachedToWindow() {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        super.onAttachedToWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public interface ILockModeActions {
        void onPositiveButton(LockDialogActivity context);

        void onNegativeButton(LockDialogActivity context);

        void onNeutralButton(LockDialogActivity context);
    }

}