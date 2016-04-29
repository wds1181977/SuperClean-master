package com.mobilefox.superclean.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.TextView;

import com.mobilefox.superclean.base.BaseFragment;
import com.mobilefox.superclean.model.SDCardInfo;
import com.mobilefox.superclean.ui.AutoStartManageActivity;
import com.mobilefox.superclean.ui.MemoryCleanActivity;
import com.mobilefox.superclean.ui.NotificationWithProgressActivity;
import com.mobilefox.superclean.ui.RubbishCleanActivity;
import com.mobilefox.superclean.ui.SoftwareManageActivity;
import com.mobilefox.superclean.utils.AppUtil;
import com.mobilefox.superclean.utils.StorageUtil;
import com.mobilefox.superclean.widget.circleprogress.ArcProgress;
import com.umeng.update.UmengUpdateAgent;
import com.mobilefox.superclean.R;

import java.util.Timer;
import java.util.TimerTask;




public class MainFragment extends BaseFragment {


    ArcProgress arcStore;


    ArcProgress arcProcess;

    TextView capacity;

    Context mContext;

    private Timer timer;
    private Timer timer2;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View view = inflater.inflate(R.layout.fragment_main, container, false);
       // ButterKnife.inject(this, view);
        mContext = getActivity();

        arcStore = (ArcProgress) view.findViewById(R.id.arc_store);

        arcProcess = (ArcProgress) view.findViewById(R.id.arc_process);

        capacity = (TextView) view.findViewById(R.id.capacity);
        view.findViewById(R.id.card1).setOnClickListener(speedUp);
        view.findViewById(R.id.card2).setOnClickListener(rubbishClean);
        view.findViewById(R.id.card3).setOnClickListener(AutoStartManage);
        view.findViewById(R.id.card4).setOnClickListener(SoftwareManage);
        view.findViewById(R.id.card5).setOnClickListener(NotificationManager);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        fillData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        UmengUpdateAgent.update(getActivity());
    }

    private void fillData() {
        // TODO Auto-generated method stub
        timer = null;
        timer2 = null;
        timer = new Timer();
        timer2 = new Timer();


        long l = AppUtil.getAvailMemory(mContext);
        long y = AppUtil.getTotalMemory(mContext);
        final double x = (((y - l) / (double) y) * 100);
        //   arcProcess.setProgress((int) x);
        if(arcProcess!=null){
        arcProcess.setProgress(0);
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if (arcProcess.getProgress() >= (int) x) {
                            timer.cancel();
                        } else {
                            arcProcess.setProgress(arcProcess.getProgress() + 1);
                        }

                    }
                });
            }
        }, 50, 20);

        SDCardInfo mSDCardInfo = StorageUtil.getSDCardInfo();
        SDCardInfo mSystemInfo = StorageUtil.getSystemSpaceInfo(mContext);

        long nAvailaBlock;
        long TotalBlocks;
        if (mSDCardInfo != null) {
            nAvailaBlock = mSDCardInfo.free + mSystemInfo.free;
            TotalBlocks = mSDCardInfo.total + mSystemInfo.total;
        } else {
            nAvailaBlock = mSystemInfo.free;
            TotalBlocks = mSystemInfo.total;
        }

        final double percentStore = (((TotalBlocks - nAvailaBlock) / (double) TotalBlocks) * 100);

        capacity.setText(StorageUtil.convertStorage(TotalBlocks - nAvailaBlock) + "/" + StorageUtil.convertStorage(TotalBlocks));
        arcStore.setProgress(0);

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if (arcStore.getProgress() >= (int) percentStore) {
                            timer2.cancel();
                        } else {
                            arcStore.setProgress(arcStore.getProgress() + 1);
                        }

                    }
                });
            }
        }, 50, 20);


    }



    
    View.OnClickListener speedUp= new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			 startActivity(MemoryCleanActivity.class);
			
		}
	};



    View.OnClickListener rubbishClean= new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			 startActivity(RubbishCleanActivity.class);
			
		}
	};




    View.OnClickListener AutoStartManage= new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			startActivity(AutoStartManageActivity.class);
			
		}
	};


    
    View.OnClickListener SoftwareManage= new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			startActivity(SoftwareManageActivity.class);
			
		}
	};
	
    View.OnClickListener NotificationManager= new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			startActivity(NotificationWithProgressActivity.class);
			
		}
	};

    @Override
    public void onDestroyView() {
        super.onDestroyView();
   
    }


    @Override
    public void onDestroy() {
        timer.cancel();
        timer2.cancel();
        super.onDestroy();
    }
}
