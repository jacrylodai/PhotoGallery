package com.bignerdranch.android.photogallery.domain;

import android.graphics.Bitmap;

public class GalleryItem {
	
	private static final int MAX_RETRY_TIMES = 3;
	
	private String caption;
	
	private String id;
	
	private String url;
	
	private Bitmap bitmap;
	
	private int retryTimes;
	
	public GalleryItem(){
		
		bitmap = null;
		retryTimes = 0;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	/**
	 * 图片下载失败，重试一次
	 */
	public void retryOnce(){
		retryTimes++;
	}
	
	/**
	 * 是否还可以重试
	 * @return
	 */
	public boolean canRetryAgain(){
		if(retryTimes < MAX_RETRY_TIMES){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public String toString() {
		return caption;
	}

}
