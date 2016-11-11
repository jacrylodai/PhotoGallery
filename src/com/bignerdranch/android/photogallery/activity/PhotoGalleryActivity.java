package com.bignerdranch.android.photogallery.activity;

import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.fragment.PhotoGalleryFragment;
import com.bignerdranch.android.photogallery.utils.FlickrFetcher;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;


public class PhotoGalleryActivity extends SingleFragmentActivity {
	
	private static final String TAG = "PhotoGalleryActivity";

	@Override
	public Fragment createFragment() {
		return new PhotoGalleryFragment();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fl_single_fragment_fragment_container);
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			
			String query = intent.getStringExtra(SearchManager.QUERY);
			Log.i(TAG,"onNewIntent:receive a new search query:"+query);
			if(TextUtils.isEmpty(query)){
				return;
			}
			
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			pref.edit()
				.putString(FlickrFetcher.PREF_SEARCH_QUERY, query)
				.commit();
			
			fragment.updateGalleryItems();
		}
	}

}
