package com.mobilefox.superclean.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobilefox.superclean.R;
import com.mediatek.common.mom.IMobileManager;
import com.mediatek.common.mom.ReceiverRecord;
import com.mobilefox.superclean.fragment.AutoStartFragment;
import com.mobilefox.superclean.model.AutoStartInfo;
import com.mobilefox.superclean.utils.ShellUtils;
import com.mobilefox.superclean.utils.T;


import java.util.ArrayList;
import java.util.List;

public class AutoStartAdapter extends BaseAdapter {
	private IMobileManager mMoMService;
    public List<ReceiverRecord> mReceiverList = new ArrayList<ReceiverRecord>();
    LayoutInflater infater = null;
    private Context mContext;
    public static List<Integer> clearIds;
    private Handler mHandler;
    private boolean mUserCheckedFlag;

    public AutoStartAdapter(Context context,IMobileManager ms,Handler h){
        infater = LayoutInflater.from(context);
        mMoMService=ms;
        mContext = context;
        mHandler=h;
    }
    public void setDataAndNotify(List<ReceiverRecord> receiverList) {
    	mReceiverList = receiverList;
    
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
    	
        return mReceiverList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mReceiverList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = infater.inflate(R.layout.listview_auto_start,
                    null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView
                    .findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView
                    .findViewById(R.id.app_name);
            holder.size = (TextView) convertView
                    .findViewById(R.id.app_size);
            holder.disable_switch = (TextView) convertView
                    .findViewById(R.id.disable_switch);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final  ReceiverRecord item = mReceiverList.get(position);

        if (item != null) {
        	
            String pkgName = item.packageName;
            String label =getApplicationName(mContext,
                    pkgName);
            Drawable icon = getApplicationIcon(mContext,
                    pkgName);
            holder.appIcon.setImageDrawable(icon);
            holder.appName.setText(label);
            boolean status = mMoMService.getBootReceiverEnabledSetting(pkgName);
            mUserCheckedFlag = false;
            if (status) {
     
                holder.disable_switch.setBackgroundResource(R.drawable.switch_on);
                holder.disable_switch.setText("已开启");
            } else {
         
                holder.disable_switch.setBackgroundResource(R.drawable.switch_off);
                holder.disable_switch.setText("已禁止");
            }
            mUserCheckedFlag = true;
            // holder.size.setText(Formatter.formatShortFileSize(mContext, item.getCacheSize()));
           final String pkg = item.packageName;
           final TextView dis_switch=holder.disable_switch;
            holder.disable_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
         	
                    if (!mUserCheckedFlag) {
            
                        return;
                    }
  	
                	boolean status = mMoMService.getBootReceiverEnabledSetting(pkg);
                    if (status) {

                        mMoMService.setBootReceiverEnabledSetting(pkg, false);
                    } else {
   
                        mMoMService.setBootReceiverEnabledSetting(pkg, true);
                    }
          
                    if(mHandler!=null){
                    	mHandler.sendEmptyMessage(AutoStartFragment.REFRESH_BT);
       
                    }

                

                }
            });
         //   holder.packageName = item.packageName;
        }


        return convertView;
    }


    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView size;
        TextView disable_switch;

        String packageName;
    }
    
    private String getApplicationName(Context context, String pkgName) {
        if (pkgName == null) {
            return null;
        }
        String appName = null;
        try {
            PackageManager pkgManager = context.getPackageManager();
            ApplicationInfo info = pkgManager.getApplicationInfo(pkgName, 0);
            appName = pkgManager.getApplicationLabel(info).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }
    
    private Drawable getApplicationIcon(Context context, String pkgName) {
        Drawable appIcon = null;
        try {
            PackageManager pkgManager = context.getPackageManager();
            ApplicationInfo info = pkgManager.getApplicationInfo(pkgName, 0);
            appIcon = pkgManager.getApplicationIcon(info);
        } catch (NameNotFoundException ex) {
           
        }
        return appIcon;
    }

}
