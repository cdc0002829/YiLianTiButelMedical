package cn.redcdn.hvs.im.activity.ViewImages;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;

import com.butel.connectevent.utils.LogUtil;

public class Photo implements Parcelable{
	public String src = ""; // 本地源路径
	public String thumb = ""; // 缩略图路径
	
	public String url = ""; // 原图URL
	public String big_url = ""; // 大图URL
	public String little_url = ""; // 小图URL

	public Photo() {
		
	}
	
	public Photo(String photoPath) {
		this.src = photoPath;
	}
	
	public Photo(JSONObject object) throws JSONException {
		if (!object.isNull("src")) { src = object.getString("src"); }
		if (!object.isNull("thumb")) { thumb = object.getString("thumb"); }
		if (!object.isNull("url")) { url = object.getString("url"); }
		if (!object.isNull("big_url")) { big_url = object.getString("big_url"); }
		if (!object.isNull("little_url")) { little_url = object.getString("little_url"); }
	}
	
	public JSONObject asJSONObject() throws JSONException {
		JSONObject object = new JSONObject();
		object.put("src", src);
		object.put("thumb", thumb);
		object.put("url", url);
		object.put("big_url", big_url);
		object.put("little_url", little_url);
		return object;
	}
	
	public static List<Photo> asArrayList(JSONArray object) {
		if (null == object)
			return null;
		
		try {
			List<Photo> photos = new ArrayList<Photo>();
			int size = object.length();
			for (int i = 0; i < size; i++) {
				JSONObject one = object.getJSONObject(i);
				photos.add(new Photo(one));
			}
			return photos;
		} catch (JSONException e) {
		    LogUtil.e("JSONException", e);
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static JSONArray asJSONArray(List<Photo> photos) {
		JSONArray array = new JSONArray();
		for (Photo one : photos) {
			try {
				array.put(one.asJSONObject());
			} catch (JSONException e) {
			    LogUtil.e("JSONException", e);
				e.printStackTrace();
			}
		}
		return array;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(src);
		arg0.writeString(thumb);
		arg0.writeString(url);
		arg0.writeString(big_url);
		arg0.writeString(little_url);
	}
	
	public static final Creator<Photo> CREATOR = new Creator<Photo>() {
		@Override
		public Photo createFromParcel(Parcel source) {
			Photo photo = new Photo();
			photo.src = source.readString();
			photo.thumb = source.readString();
			photo.url = source.readString();
			photo.big_url = source.readString();
			photo.little_url = source.readString();
			
			return photo;
		}

		@Override
		public Photo[] newArray(int size) {
			return new Photo[size];
		}
	};	
}