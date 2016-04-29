package com.mobilefox.superclean.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.mediatek.common.mom.IMobileManager;
import com.mediatek.common.mom.ReceiverRecord;
import com.mobilefox.superclean.adapter.AutoStartAdapter;
import com.mobilefox.superclean.base.BaseFragment;
import com.mobilefox.superclean.model.AutoStartInfo;
import com.mobilefox.superclean.service.CoreService;
import com.mobilefox.superclean.utils.BootStartUtils;
import com.mobilefox.superclean.utils.RootUtil;
import com.mobilefox.superclean.utils.ShellUtils;
import com.mobilefox.superclean.utils.T;


import com.mobilefox.superclean.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wangdongsheng 2016/04/25
 *
 */
public class AutoStartFragment extends BaseFragment {

	Context mContext;
	
	private IMobileManager mMoMService;
	private AutoBootAysncLoader mAsyncTask;
	public static final int REFRESH_BT = 111;
	private static final String ARG_POSITION = "position";
	private int position; // 0:普通软件，2 系统软件
	AutoStartAdapter mAutoStartAdapter;
	private LinearLayout mProgressBar;
	private ListView listview;
	private TextView mEmptyView;
	private LinearLayout bottom_lin;

	private Button disableButton;

	private TextView topText;




	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_BT:
		
				refreshUi(false,mAutoStartAdapter.mReceiverList);
			
				break;
			}
		}
	};
	
    // Receiver to handle package update broadcast
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
      
                if (IMobileManager.ACTION_PACKAGE_CHANGE.equals(action)) {
                    load();
                }
            }
        }
    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		position = getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		View view = inflater.inflate(R.layout.fragment_auto_start, container,
				false);
		// ButterKnife.inject(this, view);
		mContext = getActivity();
	     if (mMoMService == null) {
	            mMoMService = (IMobileManager)mContext. getSystemService(Context.MOBILE_SERVICE);
	        }
		listview = (ListView) view.findViewById(R.id.listview);
	    mEmptyView= (TextView)view.findViewById(R.id.empty);	
		bottom_lin = (LinearLayout) view.findViewById(R.id.bottom_lin);
		disableButton = (Button) view.findViewById(R.id.disable_button);
		topText = (TextView) view.findViewById(R.id.topText);
		mProgressBar=  (LinearLayout)view.findViewById(R.id.progressBar5);
		disableButton.setOnClickListener(onClickDisable);
//        int footerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
//        listview.setOnScrollListener(new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, bottom_lin, footerHeight));

		listview.setEmptyView(mEmptyView);
	    mAutoStartAdapter=new  AutoStartAdapter(mContext,mMoMService,mHandler);
	    listview.setAdapter(mAutoStartAdapter);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	     load();
	
	}


	View.OnClickListener onClickDisable = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			disableAllAPP();
		}
	};

	private void disableAllAPP() {
	
		List<ReceiverRecord> mAutoList = mAutoStartAdapter.mReceiverList;
		if(mAutoList==null){
			return;
		}
		for (ReceiverRecord auto : mAutoList) {
            String pkg = auto.packageName;
  	      
            // must get status again according to the package name ,
            // don't use  adapter.mReceiverList.get(i).enableadd directly
            boolean status = mMoMService.getBootReceiverEnabledSetting(pkg);
            if(status){
            	
                mMoMService.setBootReceiverEnabledSetting(pkg, false);
            }
	
		}


		refreshUi(false,mAutoList);
	
	}



	@Override
	public void onDestroyView() {
		super.onDestroyView();
	    if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        
        }
	}
	
   private void load() {

		  mAsyncTask = (AutoBootAysncLoader) new AutoBootAysncLoader().execute();

   }

	private class AutoBootAysncLoader extends
			AsyncTask<Void, Integer, List<ReceiverRecord>> {
        @Override
        protected void onPreExecute() {
    
            	  mProgressBar.setVisibility(View.VISIBLE);

        }

		@Override
		protected List<ReceiverRecord> doInBackground(Void... params) {

			if (isCancelled()) {
		
				return null;
			}

			List<ReceiverRecord> recordList = mMoMService.getBootReceiverList();
			if (recordList == null) {

				return null;
			}

			return recordList;
		}

		@Override
		protected void onPostExecute(List<ReceiverRecord> receiverList) {
		 	
		    refreshUi(true,receiverList);
		  
		}
	}
	
	public void refreshUi(boolean dataChanged,List<ReceiverRecord> receiverList) {
		 
	       
		    mAutoStartAdapter.setDataAndNotify(receiverList);

	        mAutoStartAdapter.notifyDataSetChanged();

	  
	        // if the data list is null , set the empty notify text
	        if (mAutoStartAdapter.mReceiverList == null || mAutoStartAdapter.mReceiverList.size() == 0) {
	        	bottom_lin.setVisibility(View.GONE);
	        } else {
	   
	        }
	
	        int enableCount = 0;
	        int disableCount = 0;
	        int size = 0;
	        if (mAutoStartAdapter.mReceiverList != null) {
	             size = mAutoStartAdapter.mReceiverList.size();
	        }

	        for (int i = 0 ; i < size ; i++) {
	            String pkg = mAutoStartAdapter.mReceiverList.get(i).packageName;
	      
	            // must get status again according to the package name ,
	            // don't use  adapter.mReceiverList.get(i).enableadd directly
	            boolean status = mMoMService.getBootReceiverEnabledSetting(pkg);
	            if (status) {
	                enableCount++;
	            } else {
	                disableCount++;
	            }
	            
	  
	         	
	        }
	         	
	    		if (enableCount > 0) {
					bottom_lin.setVisibility(View.VISIBLE);
					disableButton.setText("可优化" + enableCount + "款");
				} else {
					bottom_lin.setVisibility(View.GONE);
				}
	 
	        topText.setText(getResources().getQuantityString(R.plurals.autoboot_app_desc_allow,
	                enableCount, enableCount)
	                + getResources().getQuantityString(R.plurals.autoboot_app_desc_deny,
	                 disableCount, disableCount)) ;
	        if(dataChanged){
	        	 mProgressBar.setVisibility(View.GONE);
	        }
	      
	    }
	
    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }


}
