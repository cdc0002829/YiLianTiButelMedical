package cn.redcdn.hvs.contacts.contact.diaplayImageListener;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.redcdn.hvs.R;

public class DisplayImageListener  extends SimpleImageLoadingListener {

	public static  List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		if (loadedImage != null) {
			ImageView imageView = (ImageView) view;
			boolean firstDisplay = !displayedImages.contains(imageUri);
			if (firstDisplay) {
				FadeInBitmapDisplayer.animate(imageView, 500);
				displayedImages.add(imageUri);
			}
		}
		else{
//			CustomLog.e("DisplayImageListener","没有找到图片，使用默认头像");
			ImageView imageView = (ImageView) view;
			imageView.setImageResource(R.drawable.default_head_image);
		}
	}

	
}
