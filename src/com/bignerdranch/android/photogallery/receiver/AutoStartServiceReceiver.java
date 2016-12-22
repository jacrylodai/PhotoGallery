package com.bignerdranch.android.photogallery.receiver;

import com.bignerdranch.android.photogallery.service.PollService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoStartServiceReceiver extends BroadcastReceiver {
	
	private static final String TAG = "AutoStartServiceReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(TAG,"check if start service");
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isAlarmOn = pref.getBoolean(PollService.PREF_IS_ALARM_ON, false);
		PollService.setServiceAlarm(context, isAlarmOn);
	}

}
