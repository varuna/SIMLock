package com.varunarl.simlock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Varuna on 6/1/13.
 */
public class SecurityManager {

    private final static String OWNER_INFO = "OWNER_PREFERENCES";
    private final static String OWNER_PRIVATE_SERIAL_TEMPLATE = "OWNER_SIM_SERIAL_0";
    private final static String OWNER_PRIVATE_PHONE_TEMPLATE = "OWNER_PRIVATE_PHONE_0";
    private final static String OWNER_PRIVATE_INFORM_PHONE_TEMPLATE = "OWNER_PRIVATE_INFORM_PHONE_0";
    private final static String OWNER_PRIVATE_PASSWORD = "OWNER_SCREEN_PASSWORD_HASH";
    private Context mContext;
    private RegisteredSimListAdapter mRegisteredSimListAdapter;
    private String mLastInformPhoneNumber;

    public SecurityManager(Context context) {
        mContext = context;
        mLastInformPhoneNumber = "";
    }

    public static void lostMode(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LostModeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, pi);
    }

    public RegisteredSimListAdapter getRegisteredSimListAdapter(Activity context) {
        if (mRegisteredSimListAdapter == null)
            mRegisteredSimListAdapter = new RegisteredSimListAdapter(context);
        return mRegisteredSimListAdapter;
    }

    public void addOwnerSIM(String phone, String inform) {
        int i = 0;
        SIMInfo si = getCurrentSimInfo();
        si.inform = inform;
        si.phone = phone;
        mLastInformPhoneNumber = inform;
        if (availableInSimList(si.serial))
            return;
        List<SIMInfo> simList = getOwnerSIMList();
        for (; i < 10; i++) {
            SIMInfo s = simList.get(i);
            if (s.serial == null || s.serial.equals(""))
                break;
        }

        SharedPreferences pref = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(OWNER_PRIVATE_SERIAL_TEMPLATE + i, si.serial);
        prefEditor.putString(OWNER_PRIVATE_PHONE_TEMPLATE + i, si.phone);
        prefEditor.putString(OWNER_PRIVATE_INFORM_PHONE_TEMPLATE + i, si.inform);
        prefEditor.commit();
        if (mRegisteredSimListAdapter != null)
            mRegisteredSimListAdapter.notifyDataSetChanged();
    }

    public boolean validate() {
        if (isOwnerInfoAvailable()) {
            for (SIMInfo si : getOwnerSIMList())
                if (si.serial.equals(getSIMInformation().serial))
                    return true;
        }
        return false;
    }

    public List<SIMInfo> getOwnerSIMList() {
        List<SIMInfo> simList = new ArrayList<SIMInfo>();
        for (int i = 0; i < 10; i++) {
            SIMInfo si = new SIMInfo();
            si.serial = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).getString(OWNER_PRIVATE_SERIAL_TEMPLATE + i, "");
            si.phone = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).getString(OWNER_PRIVATE_PHONE_TEMPLATE + i, "");
            si.inform = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).getString(OWNER_PRIVATE_INFORM_PHONE_TEMPLATE + i, "");
            simList.add(si);
        }

        if (simList != null)
            return simList;
        return null;
    }

    public boolean isOwnerInfoAvailable() {
        List<SIMInfo> simList = getOwnerSIMList();
        if (simList != null) {
            for (SIMInfo s : simList)
                if (s.serial != null && !s.serial.equals(""))
                    return true;
        }
        return false;
    }

    public boolean isOwnerLockEnabled() {
        return mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).getInt(OWNER_PRIVATE_PASSWORD, 0) != 0;
    }

    private SIMInfo getCurrentSimInfo() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        SIMInfo si = new SIMInfo();
        si.serial = tm.getSimSerialNumber();
        si.phone = tm.getLine1Number();
        return si;
    }

    public boolean authenticateOwnerMode(String password) {
        int ownerhash = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).getInt(OWNER_PRIVATE_PASSWORD, 0);
        return ownerhash == getHashedValue(password);
    }

    public void setOwnerScreenLock(String password) {
        SharedPreferences.Editor edit = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).edit();
        edit.putInt(OWNER_PRIVATE_PASSWORD, getHashedValue(password));
        edit.commit();
    }

    private int getHashedValue(String str) {
        int total = 0;
        int i = 0;
        for (char c : str.toCharArray()) {
            total += c + i;
            i++;
        }
        return total * 998123;
    }

    public int getNumberOfRegisteredSim() {
        int count = 0;
        for (SIMInfo s : getOwnerSIMList())
            if (!s.serial.isEmpty())
                count++;
        return count;
    }

    private boolean availableInSimList(String serial) {
        List<SIMInfo> simList = getOwnerSIMList();
        if (simList != null) {
            for (SIMInfo s : simList) {
                if (s.serial.equals(serial))
                    return true;
            }
        }
        return false;
    }

    public boolean removeRegisteredSim(SIMInfo info) {
        int i = 0;

        List<SIMInfo> list = getOwnerSIMList();
        boolean found = false;
        for (; i < 10; i++)
            if (info.equals(list.get(i))) {
                found = true;
                break;
            }
        Toast.makeText(mContext, info.toString() + String.valueOf(found), Toast.LENGTH_LONG).show();
        if (found) {
            SharedPreferences.Editor editor = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).edit();
            editor.remove(OWNER_PRIVATE_PHONE_TEMPLATE + i);
            editor.remove(OWNER_PRIVATE_SERIAL_TEMPLATE + i);
            editor.remove(OWNER_PRIVATE_INFORM_PHONE_TEMPLATE + i);
            editor.commit();
            if (mRegisteredSimListAdapter != null)
                mRegisteredSimListAdapter.notifyDataSetChanged();
        }
        return found;
    }

    public String getLastInformPhoneNumber() {
        return mLastInformPhoneNumber;
    }

    public SIMInfo getSIMInformation() {
        SIMInfo c = getCurrentSimInfo();
        List<SIMInfo> l = getOwnerSIMList();
        for (SIMInfo s : l) {
            if (c.serial.equals(s.serial)) {
                c.phone = s.phone;
                c.inform = s.inform;
                break;
            }
        }
        return c;
    }

    private List<String> getInformNumberList() {
        List<String> informList = new ArrayList<String>();
        for (SIMInfo s : getOwnerSIMList()) {
            if (!s.serial.isEmpty() && !s.inform.isEmpty())
                informList.add(s.inform);
        }
        return informList;
    }

    public void announceLostDevice() {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> list = getInformNumberList();
        SIMInfo newSim = getSIMInformation();
        for (String tele : list)
            smsManager.sendTextMessage(tele, null, String.format(mContext.getString(R.string.lost_sms),newSim.phone,newSim.serial,10,20), null, null);
    }

    public class SIMInfo {
        String serial;
        String phone;
        String inform;

        @Override
        public boolean equals(Object o) {
            if (o instanceof SIMInfo) {
                SIMInfo s = (SIMInfo) o;
                return serial.equals(s.serial) && phone.equals(s.phone) && inform.equals(s.inform);
            }
            return false;
        }

        @Override
        public String toString() {
            return "SIM info : " + serial + " : " + phone + " : " + inform;
        }
    }


}
