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
    private final static String OWNER_PRIVATE_TAG_TEMPLATE = "OWNER_PRIVATE_TAG_0";
    private final static String OWNER_PRIVATE_TYPE_TEMPLATE = "OWNER_PRIVATE_TYPE_0";
    private final static String OWNER_PRIVATE_PASSWORD = "OWNER_SCREEN_PASSWORD_HASH";
    private Context mContext;
    private RegisteredSimListAdapter mRegisteredSimListAdapter;
    private String mLastInformPhoneNumber;

    public SecurityManager(Context context) {
        mContext = context;
        mLastInformPhoneNumber = "";
    }

    public static void lostMode(Context context) {
        Intent i = new Intent("com.varunarl.simlock.LOST_DEVICE");
        context.sendBroadcast(i);
    }

    public RegisteredSimListAdapter getRegisteredSimListAdapter(Activity context) {
        if (mRegisteredSimListAdapter == null)
            mRegisteredSimListAdapter = new RegisteredSimListAdapter(context);
        return mRegisteredSimListAdapter;
    }

    public void addOwnerSIM(SIMType type, String tag, String phone, String inform) {
        int i = 0;
        SIMInfo si = getCurrentSimInfo();
        si.inform = inform;
        si.phone = phone;
        si.tag = tag;
        si.type = type;
        Toast.makeText(mContext, si.toString(), Toast.LENGTH_SHORT).show();
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
        prefEditor.putString(OWNER_PRIVATE_TAG_TEMPLATE + i, si.tag);
        prefEditor.putString(OWNER_PRIVATE_TYPE_TEMPLATE + i, si.type.toString());
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
            si.tag = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).getString(OWNER_PRIVATE_TAG_TEMPLATE + i, "");
            si.type = SIMType.fromString(mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).getString(OWNER_PRIVATE_TYPE_TEMPLATE + i, ""));
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

        if (found) {
            SharedPreferences.Editor editor = mContext.getSharedPreferences(OWNER_INFO, Context.MODE_PRIVATE).edit();
            editor.remove(OWNER_PRIVATE_PHONE_TEMPLATE + i);
            editor.remove(OWNER_PRIVATE_SERIAL_TEMPLATE + i);
            editor.remove(OWNER_PRIVATE_INFORM_PHONE_TEMPLATE + i);
            editor.remove(OWNER_PRIVATE_TAG_TEMPLATE + i);
            editor.remove(OWNER_PRIVATE_TYPE_TEMPLATE + i);
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
        c.tag = "General SIM";
        c.type = SIMType.GENERAL;
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
        for (String tel : list)
            smsManager.sendTextMessage(tel, null, String.format(mContext.getString(R.string.lost_sms), newSim.phone, newSim.serial, 10, 20), null, null);
    }

    public enum SIMType {
        GENERAL, WORK, PERSONAL, DATA_ONLY, GSM_ONLY;

        public static SIMType fromString(String type) {
            if (type.equals("GENERAL")) {
                return GENERAL;
            } else if (type.equals("WORK"))
                return WORK;
            else if (type.equals("PERSONAL"))
                return PERSONAL;
            else if (type.equals("DATA ONLY"))
                return DATA_ONLY;
            else if (type.equals("GSM ONLY"))
                return GSM_ONLY;
            else
                return GENERAL;

        }

        @Override
        public String toString() {
            switch (this) {
                case GENERAL:
                    return "GENERAL";
                case WORK:
                    return "WORK";
                case PERSONAL:
                    return "PERSONAL";
                case DATA_ONLY:
                    return "DATA ONLY";
                case GSM_ONLY:
                    return "GSM ONLY";
            }
            return "";
        }
    }

    public class SIMInfo {
        String serial;
        String phone;
        String inform;
        SIMType type;
        String tag;

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
            return "SIM info : " + type.toString() + " : " + tag + " : " + serial + " : " + phone + " : " + inform;
        }


    }


}
