package com.bignerdranch.android.photogallery.fragment;

import com.bignerdranch.android.photogallery.service.PollService;

import android.app.Activity;
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

					Log.i(TAG, "receive a broadcast:"+intent.getAction());
					//If we receive this,We are visible.
					//so,Just cancel the notification
					setResultCode(Activity.RESULT_CANCELED);
				}
			};
	
	@Override
	public void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
		getActivity().registerReceiver(mOnShowNotification, intentFilter
				,PollService.PERM_PRIVATE,null);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mOnShowNotification);
	}
	
}
