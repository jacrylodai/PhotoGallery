package com.bignerdranch.android.photogallery.fragment;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.domain.GalleryItem;
import com.bignerdranch.android.photogallery.utils.FlickrFetcher;

public class PhotoGalleryFragment extends Fragment{
	
	private static final String TAG = "PhotoGalleryFragment";
	
	private GridView gvPhotoContainer;
	
	private List<GalleryItem> galleryItemList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		new FetchItemTask().execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		
		gvPhotoContainer = (GridView) view.findViewById(R.id.gv_photo_container);
		
		return view;
	}
	
	private void setupAdapter(){
		
		if(getActivity() == null || gvPhotoContainer == null){
			return;
		}
		
		if(galleryItemList != null){
			ArrayAdapter<GalleryItem> adapter = 
					new ArrayAdapter<>(getActivity(), android.R.layout.simple_gallery_item
							, galleryItemList);
			gvPhotoContainer.setAdapter(adapter);
		}else{
			gvPhotoContainer.setAdapter(null);
		}
	}
	
	private class FetchItemTask extends AsyncTask<Void, Void, List<GalleryItem>>{

		@Override
		protected List<GalleryItem> doInBackground(Void... params) {
			
			FlickrFetcher flickrFetcher = new FlickrFetcher();
			List<GalleryItem> galleryItemList = null;
			try {
				galleryItemList = flickrFetcher.fetchItems();
			} catch (IOException e) {
				Log.e(TAG, "error", e);
			} catch (XmlPullParserException e) {
				Log.e(TAG, "error", e);
			}
			return galleryItemList;
		}
		
		@Override
		protected void onPostExecute(List<GalleryItem> itemList) {
			super.onPostExecute(itemList);
			
			galleryItemList = itemList;
			setupAdapter();
		}
		
	}
	
}
