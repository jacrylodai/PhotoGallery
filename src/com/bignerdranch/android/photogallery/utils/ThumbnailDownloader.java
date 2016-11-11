package com.bignerdranch.android.photogallery.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.bignerdranch.android.photogallery.domain.GalleryItem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class ThumbnailDownloader<Token> extends HandlerThread {
	
	private static final String TAG = "ThumbnailDownloader";
	
	private static final int MESSAGE_DOWNLOAD = 1;
	
	private Handler handler;
	
	private Map<Token, GalleryItem> requestMap = 
			Collections.synchronizedMap(new HashMap<Token,GalleryItem>());
	
	private Handler responseHandler;
	
	private OnThumbnailDownloadedListener<Token> onThumbnailDownloadedListener;

	public ThumbnailDownloader(Handler responseHandler){
		super(TAG);
		this.responseHandler = responseHandler;
	}
	
	public void setOnThumbnailDownloadedListener(
			OnThumbnailDownloadedListener<Token> onThumbnailDownloadedListener) {
		this.onThumbnailDownloadedListener = onThumbnailDownloadedListener;
	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		
		handler = new Handler(){
			
			public void handleMessage(Message msg) {
				
				switch (msg.what) {
				case MESSAGE_DOWNLOAD:
					
					Token token = (Token) msg.obj;
					handleRequest(token);
					break;

				default:
					break;
				}
			};
		};
	}
	
	public void queueThumbnail(Token token,GalleryItem galleryItem){
		Log.i(TAG, "queueThumbnail:Got an image Id:"+galleryItem.getId());
		
		requestMap.put(token, galleryItem);
		handler
			.obtainMessage(MESSAGE_DOWNLOAD, token)
			.sendToTarget();
	}
	
	/**
	 * 清除对当前视图对应的下载命令，一旦当前视图有已经存在的图片
	 * ，就直接加载，之前的下载命令全部作废
	 * @param token
	 */
	public void removeThumbnail(Token token){
		
		requestMap.remove(token);
		handler.removeMessages(MESSAGE_DOWNLOAD, token);
	}
	
	private void handleRequest(final Token token){
		
		final GalleryItem galleryItem = requestMap.get(token);
		
		if(galleryItem == null){
			Log.i(TAG, "handleRequest:gallery is null for token:"+token.toString());
			return;
		}
		Log.i(TAG, "handleRequest:Got a request for image:"+galleryItem.getId());
		
		byte[] bitmapBytes = null;
		try {
			bitmapBytes = new FlickrFetcher().getUrlBytes(galleryItem.getUrl());
		} catch (IOException e) {
			Log.e(TAG, "handleRequest:Error download image url:"+galleryItem.getUrl());
			Log.e(TAG, "handleRequest:Error download image", e);
			return;
		}
		
		final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
		Log.i(TAG, "handleRequest:bitmap created");
		galleryItem.setBitmap(bitmap);
		
		responseHandler.post(new Runnable() {
			
			@Override
			public void run() {

				if(requestMap.get(token) != galleryItem){
					Log.i(TAG, "responseHandler.post.run:gallery item is changed");
					return;
				}
				
				requestMap.remove(token);
				Log.i(TAG, "responseHandler.post.run:remove token");
				onThumbnailDownloadedListener.onThumbnailDownloaded(token, galleryItem);
			}
		});
	}
	
	public void clearQueue(){
		
		handler.removeMessages(MESSAGE_DOWNLOAD);
		requestMap.clear();
	}
	
	public interface OnThumbnailDownloadedListener<Token>{
		
		public void onThumbnailDownloaded(Token token,GalleryItem galleryItem);
		
	}
	
}
