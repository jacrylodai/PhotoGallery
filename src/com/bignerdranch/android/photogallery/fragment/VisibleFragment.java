package com.bignerdranch.android.photogallery.fragment;

import com.bignerdranch.android.photogallery.service.PollService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public class VisibleFragment extends Fragment {
	
	private static final String TAG = "VisibleFragment";

	private BroadcastReceiver mOnShowNotification = 
			new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {

					Toast.makeText(context, "receive a broadcast:"+intent.getAction()
							, Toast.LENGTH_SHORT).show();
					Log.i(TAG, "receive a broadcast:"+intent.getAction());
				}
			};
	
	@Override
	public void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
		getActivity().registerReceiver(mOnShowNotification, intentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mOnShowNotification);
	}
	
}
