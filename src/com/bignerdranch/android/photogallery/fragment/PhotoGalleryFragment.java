package com.bignerdranch.android.photogallery.fragment;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.domain.GalleryItem;
import com.bignerdranch.android.photogallery.utils.FlickrFetcher;
import com.bignerdranch.android.photogallery.utils.ThumbnailDownloader;

public class PhotoGalleryFragment extends Fragment{
	
	private static final String TAG = "PhotoGalleryFragment";
	
	private GridView gvPhotoContainer;
	
	private List<GalleryItem> galleryItemList;
	
	private ThumbnailDownloader<ImageView> thumbnailDownloader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		new FetchItemTask().execute();
		
		thumbnailDownloader = new ThumbnailDownloader<ImageView>(new Handler());
		thumbnailDownloader.setOnThumbnailDownloadedListener(
				new ThumbnailDownloader.OnThumbnailDownloadedListener<ImageView>() {

					@Override
					public void onThumbnailDownloaded(ImageView imageView,
							Bitmap thumbnail) {

						if(isVisible()){
							imageView.setImageBitmap(thumbnail);
						}
					}
		});
		thumbnailDownloader.start();
		thumbnailDownloader.getLooper();
		Log.d(TAG, "Background thread started");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		
		gvPhotoContainer = (GridView) view.findViewById(R.id.gv_photo_container);
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		thumbnailDownloader.clearQueue();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		thumbnailDownloader.quit();
		Log.d(TAG, "Background thread destroyed");
	}
	
	private void setupAdapter(){
		
		if(getActivity() == null || gvPhotoContainer == null){
			return;
		}
		
		if(galleryItemList != null){
					
			GalleryItemAdapter adapter = 
					new GalleryItemAdapter(getActivity(),
							R.layout.gallery_item_photo_view, galleryItemList);
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
	
	private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{
		
		private int resourceId;

		public GalleryItemAdapter(Context context, int resource,
				List<GalleryItem> objects) {
			super(context, resource, objects);
			resourceId = resource;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			GalleryItem galleryItem = getItem(position);
			
			View view = convertView;
			if(view == null){
				view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
			}
			
			ImageView ivPhoto = (ImageView) view.findViewById(R.id.iv_photo);
			ivPhoto.setImageResource(R.drawable.loading);
			
			thumbnailDownloader.queueThumbnail(ivPhoto, galleryItem.getUrl());
			
			return view;
		}
		
	}
	
}
