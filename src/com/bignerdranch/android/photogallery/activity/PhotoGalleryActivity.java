package com.bignerdranch.android.photogallery.activity;

import com.bignerdranch.android.photogallery.fragment.PhotoGalleryFragment;

import android.support.v4.app.Fragment;


public class PhotoGalleryActivity extends SingleFragmentActivity {

	@Override
	public Fragment createFragment() {
		return new PhotoGalleryFragment();
	}

}
