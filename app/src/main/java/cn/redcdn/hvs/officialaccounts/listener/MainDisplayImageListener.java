package cn.redcdn.hvs.officialaccounts.listener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import cn.redcdn.log.CustomLog;
import cn.redcdn.hvs.R;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MainDisplayImageListener extends SimpleImageLoadingListener {

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
	//		CustomLog.e("DisplayImageListener","主页没有找到头像，使用主页默认头像");
			ImageView imageView = (ImageView) view;
			imageView.setImageResource(R.drawable.main_imagein);
		}
	}

	
}

