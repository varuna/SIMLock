package com.varunarl.simlock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Varuna on 6/2/13.
 */
public class RegisteredSimListAdapter extends BaseAdapter {

    private Context mContext;

    public RegisteredSimListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return SimLockApplication.getInstance().getSecurityManager().getNumberOfRegisteredSim();
    }

    @Override
    public Object getItem(int i) {
        return SimLockApplication.getInstance().getSecurityManager().getOwnerSIMList().get(i);
    }

    @Override
    public long getItemId(int i) {
        return SimLockApplication.getInstance().getSecurityManager().getOwnerSIMList().get(i).serial.hashCode();
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater in = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root;
        if (getCount() > 0) {
            root = in.inflate(R.layout.current_sim_list_item, null, false);

            TextView stv = (TextView) root.findViewById(R.id.textView_serial);
            TextView ptv = (TextView) root.findViewById(R.id.textView_phone);
            TextView itv = (TextView) root.findViewById(R.id.textView_inform);

            stv.setText(String.format(stv.getText().toString(), ((SecurityManager.SIMInfo) getItem(i)).serial));
            ptv.setText(String.format(ptv.getText().toString(), ((SecurityManager.SIMInfo) getItem(i)).phone));
            itv.setText(String.format(itv.getText().toString(), ((SecurityManager.SIMInfo) getItem(i)).inform));

            ImageButton rrb = (ImageButton) root.findViewById(R.id.delete_record);
            rrb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SimLockApplication.getInstance().getSecurityManager().removeRegisteredSim((SecurityManager.SIMInfo) getItem(i));
                }
            });
        }else
            root = in.inflate(R.layout.empty_list_item,viewGroup);

        return root;
    }
}
