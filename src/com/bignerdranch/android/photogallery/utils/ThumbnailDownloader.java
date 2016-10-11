package com.bignerdranch.android.photogallery.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	
	private Map<Token, String> requestMap = 
			Collections.synchronizedMap(new HashMap<Token,String>());
	
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
					String url = requestMap.get(token);
					Log.i(TAG, "handleMessage:Got a request for url:"+url);
					if(url == null){
						Log.i(TAG, "handleMessage:url is null"+token.toString());
						return;
					}
					
					handleRequest(token);
					break;

				default:
					break;
				}
			};
		};
	}
	
	public void queueThumbnail(Token token,String url){
		Log.i(TAG, "queueThumbnail:Got an url:"+url);
		Log.i(TAG, "queueThumbnail:Got an url for token:"+token.toString());
		
		requestMap.put(token, url);
		handler
			.obtainMessage(MESSAGE_DOWNLOAD, token)
			.sendToTarget();
	}
	
	private void handleRequest(final Token token){
		
		final String url = requestMap.get(token);
		
		byte[] bitmapBytes = null;
		try {
			bitmapBytes = new FlickrFetcher().getUrlBytes(url);
		} catch (IOException e) {
			Log.e(TAG, "handleRequest:Error download image", e);
			return;
		}
		
		final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
		Log.i(TAG, "handleRequest:bitmap created");
		
		responseHandler.post(new Runnable() {
			
			@Override
			public void run() {

				if(requestMap.get(token) != url){
					Log.i(TAG, "responseHandler.post.run:token is null"+token.toString());
					return;
				}
				
				requestMap.remove(token);
				Log.i(TAG, "responseHandler.post.run:remove token"+token.toString());
				onThumbnailDownloadedListener.onThumbnailDownloaded(token, bitmap);
			}
		});
	}
	
	public void clearQueue(){
		
		handler.removeMessages(MESSAGE_DOWNLOAD);
		requestMap.clear();
	}
	
	public interface OnThumbnailDownloadedListener<Token>{
		
		public void onThumbnailDownloaded(Token token,Bitmap thumbnail);
		
	}
	
}
