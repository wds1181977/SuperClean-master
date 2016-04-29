package com.mobilefox.superclean.adapter;

import android.animation.LayoutTransition;
import android.content.Context;


import android.os.Handler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobilefox.superclean.adapter.AutoStartAdapter.ViewHolder;
import com.mobilefox.superclean.ui.NotificationWithProgressActivity.AppRow;
import com.mobilefox.superclean.ui.NotificationWithProgressActivity.Backend;
import com.mobilefox.superclean.R;





import java.util.ArrayList;
import java.util.List;

public class NotificationManagerAdapter extends BaseAdapter {
	private Backend mBackend;
    public  ArrayList<AppRow> mSortedRowsList = new ArrayList<AppRow>();
    LayoutInflater infater = null;
    private Context mContext;
    public static List<Integer> clearIds;
    private Handler mHandler;
    private  final int REFRESH_NOTIFICATION = 101;
    private boolean mUserCheckedFlag;
    private static final String EMPTY_SUBTITLE = "";
    public NotificationManagerAdapter(Context context,Backend bk,Handler h){
        infater = LayoutInflater.from(context);

        mBackend=bk;
        mContext = context;
        mHandler=h;
    }
    public void setDataAndNotify(ArrayList<AppRow> list) {
    	mSortedRowsList = list;
    
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mSortedRowsList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mSortedRowsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    
    private String getSubtitle(AppRow row) {
        if (row.banned) {
            return mContext.getString(R.string.app_notification_row_banned);
        }
        if (!row.priority && !row.sensitive) {
            return EMPTY_SUBTITLE;
        }
        final String priString = mContext.getString(R.string.app_notification_row_priority);
        final String senString = mContext.getString(R.string.app_notification_row_sensitive);
        if (row.priority != row.sensitive) {
            return row.priority ? priString : senString;
        }
        return priString + mContext.getString(R.string.summary_divider_text) + senString;
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
    	  ViewHolder vh = null;
        if (v == null) {
            v = infater.inflate(R.layout.listview_notification_manage,
                    null);
            vh = new ViewHolder();
            vh.row = (ViewGroup) v;
            vh.row.setLayoutTransition(new LayoutTransition());
            vh.row.setLayoutTransition(new LayoutTransition());

            vh.icon = (ImageView) v.findViewById(R.id.app_icon);
            vh.app_name = (TextView) v.findViewById(R.id.app_name);
            vh.disable_switch = (TextView) v.findViewById(R.id.disable_switch);

            vh.size = (TextView) v
                    .findViewById(R.id.app_size);

            v.setTag(vh);
        } else {
        	vh = (ViewHolder) v.getTag();
        }
        final  AppRow row = mSortedRowsList.get(position);
  
        if (row != null) {


            vh.row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mContext.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
//                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                            .putExtra(Settings.EXTRA_APP_PACKAGE, row.pkg)
//                            .putExtra(Settings.EXTRA_APP_UID, row.uid)
//                            .putExtra(EXTRA_HAS_SETTINGS_INTENT, row.settingsIntent != null)
//                            .putExtra(EXTRA_SETTINGS_INTENT, row.settingsIntent));
                }
            });


            vh.icon.setImageDrawable(row.icon);

            vh.app_name.setText(row.label);
            Log.d("wdss","151"+row.label+"/"+row.banned+"/"+row.priority+"/"+row.sensitive);
            if(row.banned){
            	
            	
                vh.disable_switch.setBackgroundResource(R.drawable.switch_on);
                vh.disable_switch.setText(mContext.getResources().getString(R.string.app_notification_row_banned));
            }
            else{
            	vh.disable_switch.setBackgroundResource(R.drawable.switch_off);
                vh.disable_switch.setText(mContext.getResources().getString(R.string.app_notification_row_no_banned));

            
            }
            
            vh.disable_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(row.banned){
                 Log.d("wdss","169"+row.banned);
                    	mBackend.setNotificationsBanned( row.pkg,  row.uid, false);
                    	
                        }
                        else{
                            Log.d("wdss","363"+row.banned);
                           	mBackend.setNotificationsBanned( row.pkg,  row.uid, true);
                        }
//                    if(row.priority ){
//                 	   mBackend.setHighPriority(row.pkg, row.uid, true); 
//                    }else{
//                    	
//                    	   mBackend.setHighPriority(row.pkg, row.uid, false); 	
//                    }
//                    if(row.sensitive ){
//             	   mBackend.setSensitive(row.pkg, row.uid, false);
//                    }else{
//                    	
//                  	   mBackend.setSensitive(row.pkg, row.uid, true);
//                    }
        

                   mHandler.sendEmptyMessage(REFRESH_NOTIFICATION);

                }
            });
          //  vh.packageName = item.packageName;
      //      vh.app_name=(TextView) row.label;
        }


        return v;
    }


    private  class ViewHolder {
        ViewGroup row;
        ImageView icon;

        TextView size;
    	TextView app_name;
    	TextView disable_switch;
    }
    


}
