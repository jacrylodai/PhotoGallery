package com.bignerdranch.android.photogallery.service;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.bignerdranch.android.photogallery.domain.GalleryItem;
import com.bignerdranch.android.photogallery.utils.FlickrFetcher;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class PollService extends IntentService {

	private static final String TAG = "PollService";
	
	private SharedPreferences pref;
	
	public PollService() {
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i(TAG, "Receive an intent: "+intent);
		
		ConnectivityManager connectivityManager = 
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		boolean isNetworkActive = 
				(networkInfo != null) && (connectivityManager.getBackgroundDataSetting());
		if(isNetworkActive == false){
			Log.i(TAG, "There is no active network");
			return;
		}else{
			Log.i(TAG, "Network is fine");
		}
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		String lastResultId = pref.getString(FlickrFetcher.PREF_LAST_RESULT_ID, "");
		
		FlickrFetcher flickrFetcher = new FlickrFetcher();
		List<GalleryItem> galleryItemList = null;
		
		String query = pref.getString(FlickrFetcher.PREF_SEARCH_QUERY, "");
		try {
			if(TextUtils.isEmpty(query)){
				galleryItemList = flickrFetcher.fetchItems();
			}else{
				galleryItemList = flickrFetcher.searchItems(query);
			}
		} catch (IOException e) {
			Log.e(TAG, "error", e);
		} catch (XmlPullParserException e) {
			Log.e(TAG, "error", e);
		}
		
		if(galleryItemList == null || galleryItemList.size() == 0){
			return;
		}
		String newResultId = galleryItemList.get(0).getId();
		
		if(!newResultId.equals(lastResultId)){
			//如果有新的结果
			Log.i(TAG, "There are new pictures");
			
			//process
			
			//保存新的id
			SharedPreferences.Editor editor = pref.edit();
			editor.putString(FlickrFetcher.PREF_LAST_RESULT_ID, newResultId);
			editor.commit();
		}else{
			Log.i(TAG, "There aren't new pictures");
		}
	}
	
	public static void setServiceAlarm(Context context,boolean isOn){
		
		Intent intent = new Intent(context,PollService.class);
		PendingIntent pendingIntent = 
				PendingIntent.getService(context, 0, intent, 0);
		
		AlarmManager alarmManager = 
				(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if(isOn){
			long triggerAtMillis = System.currentTimeMillis();
			long intervalMillis = 10*1000;
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis
					, intervalMillis, pendingIntent);
		}else{
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

}
