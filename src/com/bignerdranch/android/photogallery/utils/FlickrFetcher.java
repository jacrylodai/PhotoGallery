package com.bignerdranch.android.photogallery.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.util.Log;

import com.bignerdranch.android.photogallery.domain.GalleryItem;


public class FlickrFetcher {
	
	private static final String TAG = "FlickrFecher";
	
	public static final String PREF_SEARCH_QUERY = "searchQuery";
	
	public static final String PREF_LAST_RESULT_ID = "lastResultId";
	
	//默认连接超时时间，毫秒
	private static final int DEFAULT_CONNECT_TIME_OUT = 5000;
	
	//默认读取超时时间，毫秒
	private static final int DEFAULT_READ_TIME_OUT = 5000;
	
	private static final String ENDPOINT = "https://api.flickr.com/services/rest/";

	private static final String API_KEY = "fc18b2d3cd1ee6186da0124703a7a115";

	private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";

	private static final String METHOD_SEARCH = "flickr.photos.search";

	private static final String PARAM_EXTRAS = "extras";

	private static final String EXTRA_SMALL_URL = "url_s";
	
	private static final String PARAM_TEXT = "text";

	private static final String XML_PHOTO = "photo";


	public byte[] getUrlBytes(String webUrl) throws IOException{
		return getUrlBytes(webUrl, DEFAULT_CONNECT_TIME_OUT, DEFAULT_READ_TIME_OUT);
	}

	public byte[] getUrlBytes(String webUrl,int connectTimeOut,int readTimeOut) throws IOException{
		
		HttpURLConnection connection = null;
		byte[] result = null;
		
		try{
			URL url = new URL(webUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(DEFAULT_CONNECT_TIME_OUT);
			connection.setReadTimeout(DEFAULT_READ_TIME_OUT);

			connection.setDoInput(true);
			
//			Log.d(TAG, "ready to get response code");
			
			int code = connection.getResponseCode();
			if(code != 200){
				throw new IOException("code:"+code
						+".message:"+connection.getResponseMessage());
			}
			
//			Log.d(TAG, "response code:"+code);
			
			InputStream inputStream = connection.getInputStream();
			
			BufferedInputStream bis = new BufferedInputStream(inputStream);
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(byteArrayOutputStream);
			
			byte[] buf = new byte[1024];
			int length = -1;
			while( (length = bis.read(buf)) != -1 ){
				bos.write(buf, 0, length);
			}
			bos.flush();
			bos.close();
			
			bis.close();
			
			result = byteArrayOutputStream.toByteArray();
			
		}catch (IOException ex){
			
			Log.e(TAG, "error", ex);
			throw ex;
		}finally{

			if(connection != null){
				connection.disconnect();
			}
		}
		return result;
	}
	
	public String getUrlContent(String webUrl) throws IOException{
		
		byte[] result = getUrlBytes(webUrl);
		if(result == null){
			return null;
		}else{
			return new String(result);
		}	
	}
	
	public List<GalleryItem> fetchItems() throws IOException, XmlPullParserException{
		
		String url = 
				Uri.parse(ENDPOINT)
					.buildUpon()
					.appendQueryParameter("method", METHOD_GET_RECENT)
					.appendQueryParameter("api_key", API_KEY)
					.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
					.build().toString();
		
		return downloadGalleryItemList(url);
	}
	
	public List<GalleryItem> searchItems(String query) 
			throws IOException, XmlPullParserException{

		String url = 
				Uri.parse(ENDPOINT)
					.buildUpon()
					.appendQueryParameter("method", METHOD_SEARCH)
					.appendQueryParameter("api_key", API_KEY)
					.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
					.appendQueryParameter(PARAM_TEXT, query)
					.build().toString();
		return downloadGalleryItemList(url);
	}

	public List<GalleryItem> downloadGalleryItemList(String webUrl) 
			throws IOException, XmlPullParserException{

		List<GalleryItem> galleryItemList = new ArrayList<GalleryItem>();
		
		String xmlContent = getUrlContent(webUrl);
		Log.d(TAG, "xml content:"+xmlContent);
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();
		
		parser.setInput(new StringReader(xmlContent));
		parseGalleryItemList(galleryItemList, parser);
		
		return galleryItemList;
	}
	
	private void parseGalleryItemList(
			List<GalleryItem> galleryItemList,XmlPullParser parser)
					throws XmlPullParserException, IOException{
		
		int eventType = parser.getEventType();
		
		while(eventType != XmlPullParser.END_DOCUMENT){
			
			String nodeName = parser.getName();
			switch (eventType) {
			case XmlPullParser.START_TAG:
				
				if(nodeName.equals(XML_PHOTO)){
					
					String id = parser.getAttributeValue(null, "id");
					String caption = parser.getAttributeValue(null, "title");
					String url = parser.getAttributeValue(null, "url_s");
					
					GalleryItem galleryItem = new GalleryItem();
					galleryItem.setId(id);
					galleryItem.setCaption(caption);
					galleryItem.setUrl(url);
					
					galleryItemList.add(galleryItem);
				}
				break;

			default:
				break;
			}
			
			eventType = parser.next();
		}
	}
	
}
