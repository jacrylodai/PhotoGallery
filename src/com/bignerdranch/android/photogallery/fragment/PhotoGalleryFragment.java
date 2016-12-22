package com.bignerdranch.android.photogallery.fragment;

import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.domain.GalleryItem;
import com.bignerdranch.android.photogallery.service.PollService;
import com.bignerdranch.android.photogallery.utils.FlickrFetcher;
import com.bignerdranch.android.photogallery.utils.ThumbnailDownloader;

public class PhotoGalleryFragment extends VisibleFragment{
	
	private static final String TAG = "PhotoGalleryFragment";
	
	private GridView gvPhotoContainer;
	
	private List<GalleryItem> galleryItemList;
	
	private ThumbnailDownloader<ImageView> thumbnailDownloader;
	
	private SharedPreferences pref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		setHasOptionsMenu(true);
		
		pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		updateGalleryItems();
				
		thumbnailDownloader = new ThumbnailDownloader<ImageView>(new Handler());
		thumbnailDownloader.setOnThumbnailDownloadedListener(
				new ThumbnailDownloader.OnThumbnailDownloadedListener<ImageView>() {

					@Override
					public void onThumbnailDownloaded(ImageView imageView,
							GalleryItem galleryItem) {

						if(isVisible()){
							imageView.setImageBitmap(galleryItem.getBitmap());
							
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

		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		
		gvPhotoContainer = (GridView) view.findViewById(R.id.gv_photo_container);
		setupAdapter();
		return view;
	}
	
	@Override
	public void onDestroyView() {

		super.onDestroyView();
		Log.d(TAG, "onDestroyView");
		thumbnailDownloader.clearQueue();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		thumbnailDownloader.quit();
		Log.d(TAG, "Background thread destroyed");
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_photo_gallery, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem togglePollingItem = menu.findItem(R.id.menu_item_toggle_polling);

		boolean isPollServiceOn = PollService.isServiceAlarmOn(getActivity());
		if(isPollServiceOn){
			togglePollingItem.setTitle(R.string.stop_polling);
		}else{
			togglePollingItem.setTitle(R.string.start_polling);
		}
	}
	
	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.menu_item_search:
			
			getActivity().onSearchRequested();
			return true;
			
		case R.id.menu_item_clear_search:
			
			pref.edit()
				.remove(FlickrFetcher.PREF_SEARCH_QUERY)
				.commit();
			
			updateGalleryItems();
			
			return true;
			
		case R.id.menu_item_toggle_polling:
			
			boolean shouldTurnOn = !PollService.isServiceAlarmOn(getActivity());
			PollService.setServiceAlarm(getActivity(), shouldTurnOn);
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
				getActivity().invalidateOptionsMenu();
			}
			
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void updateGalleryItems(){
		
		new FetchItemTask().execute();
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
			
			String query = "";
			
			Activity activity = getActivity();
			if(activity != null){
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
				query = pref.getString(FlickrFetcher.PREF_SEARCH_QUERY, "");
			}
			
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
			
			if(galleryItemList != null && galleryItemList.size() > 0){

				String newResultId = galleryItemList.get(0).getId();
				pref.edit()
					.putString(FlickrFetcher.PREF_LAST_RESULT_ID, newResultId)
					.commit();
			}
			return galleryItemList;
		}
		
		@Override
		protected void onPostExecute(List<GalleryItem> itemList) {
			super.onPostExecute(itemList);
			galleryItemList = itemList;
			if(galleryItemList == null || galleryItemList.size()==0){
				Activity activity = getActivity();
				if(activity != null){
					Toast.makeText(getActivity(), "Õ¯¬Á¡¨Ω” ß∞‹", Toast.LENGTH_SHORT).show();
				}
			}
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

			Log.i(TAG, "GalleryItemAdapter.getView: getView at position:"+position);
			
			View view = convertView;
			
			if(view == null){
				Log.i(TAG, "GalleryItemAdapter.getView: create new view");
				view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
			}else{
				Log.i(TAG, "GalleryItemAdapter.getView: reuse old view,view Id:"+view.toString());
			}
			
			Bitmap bitmap = galleryItem.getBitmap();
			ImageView ivPhoto = (ImageView) view.findViewById(R.id.iv_photo);
			
			if(bitmap == null){

				ivPhoto.setImageResource(R.drawable.loading);
				
				thumbnailDownloader.queueThumbnail(ivPhoto, galleryItem);
			}else{
				
				ivPhoto.setImageBitmap(bitmap);
				
				thumbnailDownloader.removeThumbnail(ivPhoto);
			}
			
			return view;
		}
		
	}
	
}
