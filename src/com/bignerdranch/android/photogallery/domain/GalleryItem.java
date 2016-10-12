package com.bignerdranch.android.photogallery.domain;

import android.graphics.Bitmap;

public class GalleryItem {
	
	private String caption;
	
	private String id;
	
	private String url;
	
	private Bitmap bitmap;
	
	public GalleryItem(){
		
		bitmap = null;
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

	@Override
	public String toString() {
		return caption;
	}

}
