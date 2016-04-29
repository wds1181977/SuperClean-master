package com.mobilefox.superclean.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.provider.Settings;
import android.animation.LayoutTransition;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.app.INotificationManager;
import android.service.notification.NotificationListenerService;
import android.os.ServiceManager;
import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.mobilefox.superclean.adapter.NotificationManagerAdapter;
import com.mobilefox.superclean.base.BaseSwipeBackActivity;
import com.mobilefox.superclean.fragment.AutoStartFragment;
import com.mobilefox.superclean.R;

/**
 * @author wangximing by 2016/4/25
 *
 */
public class NotificationWithProgressActivity extends BaseSwipeBackActivity{
	private final static String TAG="NotificationWithProgressActivity";
	final List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();
	List<LauncherActivityInfo> activityInfos;
	private LauncherApps launcherApps;
	private PackageManager mPM;
	private static final Intent APP_NOTIFICATION_PREFS_CATEGORY_INTENT = new Intent(Intent.ACTION_MAIN) .addCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES);

    private final ArrayMap<String, AppRow> mRows = new ArrayMap<String, AppRow>();
    private final ArrayList<AppRow> mSortedRows = new ArrayList<AppRow>();
    private final ArrayList<String> mSections = new ArrayList<String>();
    private Backend mBackend = new Backend();
    private static final String EMPTY_SUBTITLE = "";
    private static final String SECTION_BEFORE_A = "*";
    private static final String SECTION_AFTER_Z = "**";
    private  final int REFRESH_NOTIFICATION = 101;
    private NotificationAppAdapter mAdapter;
    private NotificationManagerAdapter mNMAdapter;
    private LayoutInflater mInflater;
    private Context mContext;
    private Parcelable mListViewState;
    private ListView listView;
    private LinearLayout progressLayout;
    private LinearLayout bottom_lin;
	private Button disableButton;
    static final String EXTRA_HAS_SETTINGS_INTENT = "has_settings_intent";
    static final String EXTRA_SETTINGS_INTENT = "settings_intent";
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_NOTIFICATION:
				mSortedRows.clear();
				mNMAdapter.notifyDataSetChanged();
			 
			//	refreshUi(false,mNMAdapter.mSortedRowsList);
			
				break;
			}
		}
	};
	
	
    @Override
    public void onResume() {
        super.onResume();
    	new TaskScanNotificationApp().execute();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_manage);
		mContext = getApplicationContext();
		mPM = getPackageManager();
		mAdapter = new NotificationAppAdapter(mContext);
		mNMAdapter=new NotificationManagerAdapter(mContext,mBackend,mHandler);
		listView = (ListView) findViewById(R.id.listview);
		bottom_lin = (LinearLayout) findViewById(R.id.bottom_lin);
		disableButton = (Button) findViewById(R.id.disable_button);
		disableButton.setOnClickListener(onClickDisable);
		//listView.setAdapter(mAdapter);
		listView.setAdapter(mNMAdapter);
		int footerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
		listView.setOnScrollListener(new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, bottom_lin, footerHeight));
		progressLayout = (LinearLayout) findViewById(R.id.progressBar);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
	
        
	}

	private final Runnable mRefreshAppsListRunnable = new Runnable() {
        @Override
        public void run() {
            refreshDisplayedItems();
        }
    };
	
    @Override
    public void onPause() {
        super.onPause();
        mListViewState = listView.onSaveInstanceState();
    }
    
   private void  refreshUi(boolean b,ArrayList<AppRow> list){
	   mNMAdapter.setDataAndNotify(list);

	   mNMAdapter.notifyDataSetChanged();


        // if the data list is null , set the empty notify text
        if (mNMAdapter.mSortedRowsList == null || mNMAdapter.mSortedRowsList.size() == 0) {
        	bottom_lin.setVisibility(View.GONE);
        } else {
        	bottom_lin.setVisibility(View.VISIBLE);
        }
        if(b){
        	
			progressLayout.setVisibility(View.GONE);
        }
	   
   }
    
    private void refreshDisplayedItems() {
    
//	       
//    	mAdapter.setDataAndNotify(mSortedRows);
//
//    	mAdapter.notifyDataSetChanged();
//        if (DEBUG) Log.d(TAG, "Refreshing apps...");
//        mAdapter.clear();
//        synchronized (mSortedRows) {
//            String section = null;
//            if (mSortedRows == null) {
//                return;
//            }
//            final int N = mSortedRows.size();
//            boolean first = true;
//            for (int i = 0; i < N; i++) {
//                if (mSortedRows.size() < i) {
//                    return;
//                }
//
//                final AppRow row = mSortedRows.get(i);
////                if (!row.section.equals(section)) {
////                    section = row.section;
////                    Row r = new Row();
////                    r.section = section;
////                    mAdapter.add(r);
////                    first = true;
////                }
//                row.first = first;
//                mAdapter.add(row);
//                first = false;
//            }
//        }
//        if (mListViewState != null) {
////            if (DEBUG) Log.d(TAG, "Restoring listView state");
//        	listView.onRestoreInstanceState(mListViewState);;
//            mListViewState = null;
//        }
//        if (DEBUG) Log.d(TAG, "Refreshed " + mSortedRows.size() + " displayed items");
    }
    
	private String getSection(CharSequence label) {
        if (label == null || label.length() == 0) return SECTION_BEFORE_A;
        final char c = Character.toUpperCase(label.charAt(0));
        if (c < 'A') return SECTION_BEFORE_A;
        if (c > 'Z') return SECTION_AFTER_Z;
        return Character.toString(c);
    }
	
	private static final Comparator<AppRow> mRowComparator = new Comparator<AppRow>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppRow lhs, AppRow rhs) {
            return sCollator.compare(lhs.label, rhs.label);
        }
    };
	
	public static void applyConfigActivities(PackageManager pm, ArrayMap<String, AppRow> rows,
            List<ResolveInfo> resolveInfos) {
        for (ResolveInfo ri : resolveInfos) {
            final ActivityInfo activityInfo = ri.activityInfo;
            final ApplicationInfo appInfo = activityInfo.applicationInfo;
            final AppRow row = rows.get(appInfo.packageName);
            if (row == null) {
                continue;
            }
            if (row.settingsIntent != null) {
                continue;
            }
            row.settingsIntent = new Intent(APP_NOTIFICATION_PREFS_CATEGORY_INTENT)
                    .setClassName(activityInfo.packageName, activityInfo.name);
        }
    }
	
	public static AppRow loadAppRow(PackageManager pm, ApplicationInfo app,
            Backend backend) {
        final AppRow row = new AppRow();
        row.pkg = app.packageName;
        row.uid = app.uid;
        try {
            row.label = app.loadLabel(pm);
        } catch (Throwable t) {
            row.label = row.pkg;
        }
        row.icon = app.loadIcon(pm);
        row.banned = backend.getNotificationsBanned(row.pkg, row.uid);
        row.priority = backend.getHighPriority(row.pkg, row.uid);
        row.sensitive = backend.getSensitive(row.pkg, row.uid);
        return row;
    }
	
	public static List<ResolveInfo> queryNotificationConfigActivities(PackageManager pm) {
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(
                APP_NOTIFICATION_PREFS_CATEGORY_INTENT,
                0 //PackageManager.MATCH_DEFAULT_ONLY
        );
        return resolveInfos;
    }
	
	
	private static class Row {
        public String section;
    }

    public static class AppRow extends Row {
        public String pkg;
        public int uid;
        public Drawable icon;
        public CharSequence label;
        public Intent settingsIntent;
        public boolean banned;
        public boolean priority;
        public boolean sensitive;
        public boolean first;  // first app in section
    }
    
    private static class ViewHolder {
        ViewGroup row;
        ImageView icon;
        TextView title;
        TextView subtitle;
        View rowDivider;

    	TextView app_name;
    	TextView disable_switch;
    }
    
    private class NotificationAppAdapter extends ArrayAdapter<Row> implements SectionIndexer {
        public NotificationAppAdapter(Context context) {
            super(context, 0, 0);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Row r = getItem(position);
            return r instanceof AppRow ? 1 : 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Row r = getItem(position);
            View v;
            if (convertView == null) {
                v = newView(parent, r);
            } else {
                v = convertView;
            }
            bindView(v, r, false/* animate*/);
            return v;
        }

        public View newView(ViewGroup parent, Row r) {
            if (!(r instanceof AppRow)) {
//                return mInflater.inflate(R.layout.notification_app_section, parent, false);
            }
            final View v = mInflater.inflate(R.layout.listview_notification_manage, parent, false);
            final ViewHolder vh = new ViewHolder();
            vh.row = (ViewGroup) v;
            vh.row.setLayoutTransition(new LayoutTransition());
            vh.row.setLayoutTransition(new LayoutTransition());
        //    vh.icon = (ImageView) v.findViewById(android.R.id.icon);
            vh.title = (TextView) v.findViewById(android.R.id.title);
            vh.subtitle = (TextView) v.findViewById(android.R.id.text1);
        //    vh.rowDivider = v.findViewById(R.id.row_divider);
            vh.icon = (ImageView) v.findViewById(R.id.app_icon);
            vh.app_name = (TextView) v.findViewById(R.id.app_name);
            vh.disable_switch = (TextView) v.findViewById(R.id.disable_switch);
            v.setTag(vh);
            return v;
        }

        private void enableLayoutTransitions(ViewGroup vg, boolean enabled) {
            if (enabled) {
                vg.getLayoutTransition().enableTransitionType(LayoutTransition.APPEARING);
                vg.getLayoutTransition().enableTransitionType(LayoutTransition.DISAPPEARING);
            } else {
                vg.getLayoutTransition().disableTransitionType(LayoutTransition.APPEARING);
                vg.getLayoutTransition().disableTransitionType(LayoutTransition.DISAPPEARING);
            }
        }

        public void bindView(final View view, Row r, boolean animate) {
            if (!(r instanceof AppRow)) {
            	view.setVisibility(View.GONE);
                // it's a section row
//                final TextView tv = (TextView)view.findViewById(android.R.id.title);
//                tv.setText(r.section);
                return;
            }
            view.setVisibility(View.VISIBLE);
            final AppRow row = (AppRow)r;
            final ViewHolder vh = (ViewHolder) view.getTag();
//            enableLayoutTransitions(vh.row, animate);
//            vh.rowDivider.setVisibility(row.first ? View.GONE : View.VISIBLE);
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
//            enableLayoutTransitions(vh.row, animate);
            vh.icon.setImageDrawable(row.icon);
          //  vh.title.setText(row.label);
            vh.app_name.setText(row.label);
            Log.d("wdss","331"+row.banned+"/"+row.priority+"/"+row.sensitive);
            if(row.banned){
            	
            	
                vh.disable_switch.setBackgroundResource(R.drawable.switch_on);
                vh.disable_switch.setText(getResources().getString(R.string.app_notification_row_banned));
            }
            else{
            	vh.disable_switch.setBackgroundResource(R.drawable.switch_off);
                vh.disable_switch.setText(getResources().getString(R.string.app_notification_row_no_banned));

            
            }
            
            vh.disable_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(row.banned){
                 Log.d("wdss","348"+row.banned);
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
        
        
//        			// compute sections
//        	        mSections.clear();
                   mHandler.post(mRefreshAppsListRunnable);
                    mAdapter.notifyDataSetChanged();
                }
            });
//            final String sub = getSubtitle(row);
//            vh.subtitle.setText(sub);
//            vh.subtitle.setVisibility(!sub.isEmpty() ? View.VISIBLE : View.GONE);
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
        public Object[] getSections() {
            return mSections.toArray(new Object[mSections.size()]);
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            final String section = mSections.get(sectionIndex);
            final int n = getCount();
            for (int i = 0; i < n; i++) {
                final Row r = getItem(i);
                if (r.section.equals(section)) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            Row row = getItem(position);
            return mSections.indexOf(row.section);
        }
    }
    
    public static class Backend {
        static INotificationManager sINM = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));

        public boolean setNotificationsBanned(String pkg, int uid, boolean banned) {
            try {
                sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
                return true;
            } catch (Exception e) {
               Log.w(TAG, "Error calling NoMan", e);
               return false;
            }
        }

        public boolean getNotificationsBanned(String pkg, int uid) {
            try {
                final boolean enabled = sINM.areNotificationsEnabledForPackage(pkg, uid);
                return !enabled;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getHighPriority(String pkg, int uid) {
            try {
                return sINM.getPackagePriority(pkg, uid) == Notification.PRIORITY_MAX;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
            try {
                sINM.setPackagePriority(pkg, uid,
                        highPriority ? Notification.PRIORITY_MAX : Notification.PRIORITY_DEFAULT);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getSensitive(String pkg, int uid) {
            try {
                return sINM.getPackageVisibilityOverride(pkg, uid) == Notification.VISIBILITY_PRIVATE;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean setSensitive(String pkg, int uid, boolean sensitive) {
            try {
                sINM.setPackageVisibilityOverride(pkg, uid,
                        sensitive ? Notification.VISIBILITY_PRIVATE
                                : NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }
    }

	
	private class TaskScanNotificationApp extends AsyncTask<Void, Integer, ArrayList<AppRow>>{

		@Override
		protected void onPreExecute() {
			progressLayout.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected ArrayList<AppRow> doInBackground(Void... params) {
			activityInfos = launcherApps.getActivityList(null,  UserHandle.getCallingUserHandle());


			for (LauncherActivityInfo info : activityInfos) {
				appInfos.add(info.getApplicationInfo());
			}
			final List<ResolveInfo> resolvedConfigActivities = queryNotificationConfigActivities(mPM);
			for (ResolveInfo ri : resolvedConfigActivities) {
				appInfos.add(ri.activityInfo.applicationInfo);
			}
			for (ApplicationInfo info : appInfos) {
	            final String key = info.packageName;
	            if (mRows.containsKey(key)) {
	                // we already have this app, thanks
	                continue;
	            }

	            final AppRow row = loadAppRow(mPM, info, mBackend);
	            mRows.put(key, row);
	        }
			 // add config activities to the list
	        applyConfigActivities(mPM, mRows, resolvedConfigActivities);
	     // sort rows
	        mSortedRows.addAll(mRows.values());
	        Collections.sort(mSortedRows, mRowComparator);
			return mSortedRows;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(ArrayList<AppRow> result) {


			
			refreshUi(true,result);

			
	      //  mHandler.post(mRefreshAppsListRunnable);
		}
	}
	
	View.OnClickListener onClickDisable = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			cancelAllAPPNotification();
		}
	};
	
	private void cancelAllAPPNotification(){
	      if (mSortedRows == null) {
              return;
          }
		
        for (AppRow row : mSortedRows) {
           String pkg=row.pkg;
           int uid=row.uid;
           
           Log.d("wdss","572"+row.banned+"/"+row.priority+"/"+row.sensitive);
           boolean isbanned = mBackend.getNotificationsBanned(pkg, uid);
//           row.priority = mBackend.getHighPriority(pkg, uid);
//           row.sensitive = mBackend.getSensitive(pkg, uid);
           if(isbanned ){
    	   mBackend.setNotificationsBanned( pkg,  uid, false);
           }
//           if(row.priority ){
//        	   mBackend.setHighPriority(pkg, uid, false); 
//           }
//           if(row.sensitive ){
//    	   mBackend.setSensitive(pkg, uid, false);
//           }
       }
		
        mHandler.post(mRefreshAppsListRunnable);
        mAdapter.notifyDataSetChanged();
	}

}
