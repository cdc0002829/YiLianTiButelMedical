package cn.redcdn.hvs.meeting.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.meeting.constant.CommonConstant;
import cn.redcdn.hvs.util.StringUtil;

/**
 * <dl>
 * <dt>CommonUtil.java</dt>
 * <dd>Description:通用操作类</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2013-7-31 下午5:41:40</dd>
 * </dl>
 * 
 * @author chuwx
 */
public class CommonUtil {

	private static final String TAG = "CommonUtil";

	public static String getSDPath() {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		Log.d(TAG, "getSDPath sdCardExist:" + sdCardExist);
		if (sdCardExist) {
			// 获取跟目录
			return Environment.getExternalStorageDirectory().toString();
		} else {
			return "";
		}
	}



	// /**
	// * @author: chuwx
	// * @Title: getDdPathPostFix
	// * @Description:获取数据库路径名（区别内存中还是sd卡中）
	// * @param context
	// * @param loginUserId
	// * @return
	// * @date: 2013-7-31 下午6:07:18
	// */
	// public static String getDdPathPostFix(Context context) {
	// LogUtil.begin("");
	// String mark = NetPhoneApplication.getPreference().getKeyValue(
	// PrefType.LOGIN_NUMBER_CHANGE, "0");
	// if ("1".equals(mark)) {
	// // 切换账号的场景下，清空数据存储方式
	// NetPhoneApplication.getPreference().setKeyValue(
	// PrefType.USER_KEY_DBSRC_STORE_TYPE, "");
	// }
	// String sqliteFilePath = "";
	// if (CommonUtil.isHasSDCard()) {
	// long sizes = getAvailaleSize();
	// if (sizes >= 20) {
	// if (CommonUtil.isUseSDCard(context)) {
	// LogUtil.d("数据库保存在sd卡场景1");
	// sqliteFilePath = getSDCardDBFolder();
	// LogUtil.d("sqliteFilePath=" + sqliteFilePath);
	// } else {
	// LogUtil.d("数据库保存在内存场景2");
	// sqliteFilePath = DBConstant.SQLITE_FILE_ROM_FOLDER;
	// }
	// } else {
	// LogUtil.d("sd卡空间不足20M,数据库放在内存中");
	// sqliteFilePath = DBConstant.SQLITE_FILE_ROM_FOLDER;
	// }
	// } else {
	// LogUtil.d("sd卡不存在");
	// sqliteFilePath = DBConstant.SQLITE_FILE_ROM_FOLDER;
	// }
	// return sqliteFilePath;
	// }

	/**
	 * 获得外部存储卡的可使用空间
	 * 
	 * @return 可使用值 单位M
	 */
	public static long getAvailaleSize() {
		if (!isHasSDCard()) {
			LogUtil.d(TAG, "getAvailaleSize", "当前sd卡不存在或不可用状态");
			return 0;
		}
		// 获取sd卡可用空间
		File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		long size = ((availableBlocks * blockSize) / 1024 / 1024);
		LogUtil.d(TAG, "getAvailaleSize", "当前sd卡可用空间为：" + size + "M");
		return size;
	}




	/**
	 * 判断SD卡是否存在
	 * 
	 * @return
	 */
	public static boolean isHasSDCard() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}




	private static void checkouForder(String forderDir) {
		File forderSqlite = new File(forderDir);
		if (!forderSqlite.exists()) {
			boolean mark = forderSqlite.mkdirs();
			LogUtil.d(TAG, "checkouForder", "新建目录:" + forderDir + mark);
		}
	}



	public static String trackValue(Object object) {
		if (object == null || object.toString().trim().length() == 0
				|| object.toString().trim().equals("null")
				|| object.toString().trim().equals("NULL")) {
			return "";
		} else {
			return object.toString();
		}
	}

	public static void copySqlite(String newPath, String oldPath)
			throws Exception {
		LogUtil.begin("oldPath=" + oldPath + "|newPath=" + newPath);
		File oldFile = new File(oldPath);

		InputStream is = null;
		FileOutputStream os = null;
		try {
			if (oldFile.exists()) {
				is = new FileInputStream(oldFile);
				os = new FileOutputStream(newPath);

				int byteRead = 0;
				byte[] buffer = new byte[is.available()];

				while ((byteRead = is.read(buffer)) > 0) {
					os.write(buffer, 0, byteRead);
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "copySqlite", "Exception", e);
			throw e;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LogUtil.e(TAG, "copySqlite", "is.close()", e);
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					LogUtil.e(TAG, "copySqlite", "os.close()", e);
				}
			}
		}
		LogUtil.end("");
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		int bytesum = 0;
		int byteread = 0;
		InputStream inStream = null;
		FileOutputStream fs = null;
		byte[] buffer = new byte[1444];
		int length = 0;
		try {

			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				inStream = new FileInputStream(oldPath); // 读入原文件
				fs = new FileOutputStream(newPath);

				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				inStream = null;
				fs.close();
				fs = null;
			}
		} catch (Exception e) {
			LogUtil.e("Exception", e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
					inStream = null;
				} catch (IOException e) {
					LogUtil.e(TAG, "copyFile", "inStream.close()", e);
				}
			}
			if (fs != null) {
				try {
					fs.close();
					fs = null;
				} catch (IOException e) {
					LogUtil.e(TAG, "copyFile", "fs.close()", e);
				}
			}
		}

	}

	/**
	 * @Title: isNetworkAvailable
	 * @Description: 网络状态判断
	 * @param context
	 * @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				int length = info.length;
				for (int count = 0; count < length; count++) {
					if (info[count].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 服务是否正在运行
	 * 
	 * @param serviceName
	 * @return
	 */
	public static boolean isZCBServiceRunning(String serviceName) {
		LogUtil.begin("serviceName=" + serviceName);
		boolean ok = false;
		Process process = null;
		InputStream is = null;
		try {
			process = Runtime.getRuntime().exec("ps");
			is = process.getInputStream();
			int code = -1;
			StringBuffer sb = new StringBuffer();
			while ((code = is.read()) != -1) {
				sb.append((char) code);
			}
			is.close();
			if (sb.toString().indexOf(serviceName) > -1) {
				ok = true;
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "isZCBServiceRunning", "Exception", e);
			if (is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					LogUtil.e(TAG, "isZCBServiceRunning", "IOException", e1);
					is = null;
				}
			}
		}
		LogUtil.end("isRunning:" + ok);
		return ok;
	}





	public static String getUUID() {
		String s = UUID.randomUUID().toString();
		// 去掉“-”符号
		return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
				+ s.substring(19, 23) + s.substring(24);
	}

	/**
	 * @author: lihs
	 * @Title: getPackageInfo
	 * @Description: 应用程序的版本号，版本名称，当前版本的包名
	 * @return
	 * @date: 2013-8-2 下午3:42:56
	 */
	public static PackageInfo getPackageInfo() {

		PackageInfo info = new PackageInfo();
		try {
			info = MedicalApplication
					.getContext()
					.getPackageManager()
					.getPackageInfo(
						MedicalApplication.getContext().getPackageName(),
							0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			LogUtil.e("getPackageInfo", e);
		}
		return info;
	}

	/**
	 * @author: lihs
	 * @Title: getMac
	 * @Description: 获取手机的mac地址
	 * @return
	 * @date: 2013-8-2 下午5:11:31
	 */
	public static String getMac() {

		WifiManager wifi = (WifiManager) MedicalApplication.getContext()
				.getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = wifi.getConnectionInfo();

		String maxText = info.getMacAddress();

		LogUtil.d(TAG, "getMac", "手机的mac地址为：" + maxText);

		return maxText;
	}


	/**
	 * 隐藏界面输入软键盘
	 * 
	 * @param activity
	 */
	public static void hideSoftInputFromWindow(final Activity activity) {
		// activity.runOnUiThread(new Runnable() {
		// @Override
		// public void run() {
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {

			View view = activity.getCurrentFocus();
			if (view != null) {
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
		// }
		// });
	}

	/**
	 * 显示界面输入键盘
	 * 
	 * @param activity
	 */
	public static void showSoftInputFromWindow(final Activity activity) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					View view = activity.getCurrentFocus();
					if (view != null) {
						LogUtil.d("showSoftInput");
						imm.showSoftInput(view,
								InputMethodManager.SHOW_IMPLICIT);
					}
				}
			}
		});
	}

	/**
	 * 切换输入键盘显示与关闭，软键盘显示的场合，关闭之；关闭的场合，显示之。
	 * 
	 * @param activity
	 */
	public static void toggleSoftInput(final Activity activity) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
				}
			}
		});
	}

	public static void scanFileAsync(Context ctx, String filePath) {
		LogUtil.begin("filePath:" + filePath);
		Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		scanIntent.setData(Uri.fromFile(new File(filePath)));
		ctx.sendBroadcast(scanIntent);
	}




	/**
	 * @author: zhaguitao
	 * @Title: getImageRotationByPath
	 * @Description: 根据图片路径获得其旋转角度
	 * @param ctx
	 * @param path
	 * @return
	 * @date: 2013-10-16 下午12:53:34
	 */
	public static int getImageRotationByPath(Context ctx, String path) {
		int rotation = 0;
		if (TextUtils.isEmpty(path)) {
			return rotation;
		}

		Cursor cursor = null;
		try {
			cursor = ctx.getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Images.Media.ORIENTATION },
					MediaStore.Images.Media.DATA + " = ?",
					new String[] { "" + path }, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				rotation = cursor.getInt(0);
			} else {
				rotation = getImageRotationFromUrl(path);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "getImageRotationByPath", "Exception", e);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return rotation;
	}

	/**
	 * @author: zhaguitao
	 * @Title: getImageRotationByPath
	 * @Description: 根据图片id获得其旋转角度
	 * @param ctx
	 * @param path
	 * @return
	 * @date: 2013-10-16 下午12:53:34
	 */
	public static int getImageRotationById(Context ctx, int id) {
		int rotation = 0;
		if (id < 0) {
			return rotation;
		}

		Cursor cursor = null;
		try {
			cursor = ctx.getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Images.Media.ORIENTATION },
					MediaStore.Images.Media._ID + " = ?",
					new String[] { "" + id }, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				rotation = cursor.getInt(0);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "getImageRotationById", "Exception", e);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return rotation;
	}

	/**
	 * @author: zhaguitao
	 * @Title: getIdByPath
	 * @Description: 根据文件路径，查询其id
	 * @param ctx
	 * @param path
	 * @return
	 * @date: 2013-9-23 下午5:26:07
	 */
	public static final String THUMBNAIL_TYPE_IMAGE = "image";
	public static final String THUMBNAIL_TYPE_VIDEO = "video";

	public static int[] getIdRotationByPath(Context ctx, String path,
			String thumbnailType) {

		int id = -1;
		int rotation = 0;

		Cursor cursor = null;

		try {
			if (THUMBNAIL_TYPE_VIDEO.equals(thumbnailType)) {
				cursor = ctx.getContentResolver().query(
						MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						new String[] { MediaStore.Video.Media._ID },
						MediaStore.Video.Media.DATA + " = ? ",
						new String[] { path }, null);
			} else {
				cursor = ctx.getContentResolver().query(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						new String[] { MediaStore.Images.Media._ID,
								MediaStore.Images.Media.ORIENTATION },
						MediaStore.Images.Media.DATA + " = ? ",
						new String[] { path }, null);
			}

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				id = cursor.getInt(0);
				if (!THUMBNAIL_TYPE_VIDEO.equals(thumbnailType)) {
					rotation = cursor.getInt(1);
				}
			} else {
				if (!THUMBNAIL_TYPE_VIDEO.equals(thumbnailType)) {
					rotation = CommonUtil.getImageRotationFromUrl(path);
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "getIdRotationByPath", "Exception", e);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return new int[] { id, rotation };
	}

	/**
	 * @Description: 获取下载图片的旋转角度，从网络传输图片，只能用带方法
	 * @param path
	 *            :图片路径
	 * @return 返回图片角度
	 */
	public static int getImageRotationFromUrl(String path) {
		int orientation = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				orientation = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				orientation = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				orientation = 270;
				break;
			default:
				orientation = 0;
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "getImageRotationFromUrl", "Exception", e);
			orientation = 0;
		}
		return orientation;
	}

	/**
	 * @Title: secToTime
	 * @Description: a integer to xx:xx:xx
	 * @param time
	 * @return: String
	 */
	public static String secToTime(int time) {
		String timeStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (time <= 0)
			return "00:00";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour > 99) {
					return "99:59:59";
				}
				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":"
						+ unitFormat(second);
			}
		}
		return timeStr;
	}

	public static String unitFormat(int i) {
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = "" + i;
		return retStr;
	}

	public static final long SEVEN_DAYS_TIME = 7 * 24 * 60 * 60;

	/**
	 * 
	 * @author: qn-lihs
	 * @Title: activityIsOverdue
	 * @Description: 是否活动过期
	 * @param overdueTime
	 *            活动的截止时间
	 * @return 0 表示活动在到期前7天； 1 表示到期后 ，2至少7天后才过期,为了提示语方便
	 * @date: 2013-11-19 下午5:48:10
	 */
	public static int activityIsOverdue(String overdueTime,
			String serviceCurrentTime) {
		if (!TextUtils.isEmpty(overdueTime)
				&& !TextUtils.isEmpty(serviceCurrentTime)) {
			long isOverDue = Long.parseLong(serviceCurrentTime)
					- Long.parseLong(overdueTime);
			if (isOverDue <= 0) {
				if (Math.abs(isOverDue) <= SEVEN_DAYS_TIME) {
					return 0;
				} else {
					return 2;
				}
			}
		}
		return 1;
	}

	private static int screen_w = 0; // 手机屏幕的宽度，单位像素
	private static int screen_h = 0; // 手机屏幕的高度，单位像素

	private static void initScreenInfo(Context mContext) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = mContext.getResources().getDisplayMetrics();

		float density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
		int densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
		float xdpi = dm.xdpi;
		float ydpi = dm.ydpi;

		// 获取屏幕密度（方法3）
		// dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);

		density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
		densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
		xdpi = dm.xdpi;
		ydpi = dm.ydpi;


		screen_w = dm.widthPixels;
		screen_h = dm.heightPixels;

		// Log.e(TAG + "  DisplayMetrics(222)", "screenWidthDip=" +
		// screenWidthDip + "; screenHeightDip=" + screenHeightDip);
		//
		// screen_w = (int)(dm.widthPixels * density + 0.5f); // 屏幕宽（px，如：480px）
		// screen_h = (int)(dm.heightPixels * density + 0.5f); //
		// 屏幕高（px，如：800px）

	}

	public static int getScreenWidth(Context mContext) {
		// if(screen_w == 0)
		// {
		initScreenInfo(mContext);
		// }
		return screen_w;
	}

	public static int getScreenHeight(Context mContext) {
		// if(screen_h == 0)
		// {
		initScreenInfo(mContext);
		// }
		return screen_h;
	}

	/**
	 * 判断是否需要弹出Web页面进行网络登录。
	 * 
	 * @param responseBody
	 * @return
	 */
	public static boolean isViewWebPage(String responseBody) {
		LogUtil.begin(responseBody);
		boolean result = false;
		String htmlTag = "<html>";
		if (responseBody.toLowerCase().contains(htmlTag)) {
			result = true;
		}
		LogUtil.end("" + result);
		return result;
	}

	// 检查非法字符
	public static boolean checkIllegalChar(String str) {
		boolean mark = true;
		String illStr = "\"" + "\'" + "\\" + "\n" + "\r" + "&<>/%“‘”";
		if (!TextUtils.isEmpty(str.trim())) {
			char[] strChar = str.toCharArray();
			for (char temp : strChar) {
				if (illStr.indexOf(temp) != -1) {
					mark = false;
					break;
				}
			}
		}
		return mark;
	}

	/**
	 * 
	 * Description:过滤非法字符
	 * 
	 * @param str
	 * @return
	 */
	public static String fliteIllegalChar(String str) {
		String illStrs = "\"" + "\'" + "\\" + "\n" + "\r" + "&<>/%“‘”";
		if (!TextUtils.isEmpty(str)) {
			for (char illStr : illStrs.toCharArray()) {
				str = str.replace(illStr, ' ').replace(" ", "");
			}
		}
		return str.trim();
	}

	public static void showToast(String alertMsg) {
		Toast.makeText(MedicalApplication.getContext(), alertMsg,
				Toast.LENGTH_SHORT).show();
	}

	public static void showToast(int alertId, Context context) {
		Toast.makeText(context, context.getString(alertId), Toast.LENGTH_SHORT)
				.show();
	}


	private static long lastClickTime;

	/**
	 * @author: zhaguitao
	 * @Title: isFastDoubleClick
	 * @Description: 防止按钮快速点击导致多次处理
	 * @return
	 * @date: 2014-3-13 下午5:31:47
	 */
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 1000) {
			LogUtil.d("快速点击");
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/**
	 * @author: chuwx
	 * @Title: simpleFormatMoPhone
	 * @Description: 简单格式化手机号码
	 * @param phone
	 * @return
	 * @date: 2014-3-18 下午3:19:07
	 */
	public static String simpleFormatMoPhone(String phone) {
		if (TextUtils.isEmpty(phone)) {
			return "";
		}
		String oldPhone = phone;
		phone = phone.replace("-", "").replace(" ", "");
		if (phone.startsWith("+86") && phone.length() == 14) {
			phone = phone.substring(3);
		}
//		LogUtil.d("简单格式化手机号码:" + oldPhone + "---->" + phone);
		return phone;
	}

	/**
	 * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
	 * 
	 * @param mobile
	 *            移动、联通、电信运营商的号码段
	 *            <p>
	 *            移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
	 *            、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）
	 *            </p>
	 *            <p>
	 *            联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）
	 *            </p>
	 *            <p>
	 *            电信的号段：133、153、180（未启用）、189
	 *            </p>
	 * @return 验证成功返回true，验证失败返回false
	 * @date: 2015/8/14
	 */
	public static boolean checkMobile(String mobile) {
		String regex = "(\\+\\d+)?1[3458]\\d{9}$";
		return Pattern.matches(regex, mobile);
	}

	/**
	 * 验证固定电话号码
	 * 
	 * @param phone
	 *            电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
	 *            <p>
	 *            <b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9
	 *            的一位或多位数字， 数字之后是空格分隔的国家（地区）代码。
	 *            </p>
	 *            <p>
	 *            <b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
	 *            对不使用地区或城市代码的国家（地区），则省略该组件。
	 *            </p>
	 *            <p>
	 *            <b>电话号码：</b>这包含从 0 到 9 的一个或多个数字
	 *            </p>
	 * @return 验证成功返回true，验证失败返回false
	 * @date: 2015/8/14
	 */
	public static boolean checkPhone(String phone) {
		String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
		return Pattern.matches(regex, phone);
	}



	public static boolean matchRegEx(String pattern, String value) {
		Pattern pat = Pattern.compile(pattern);
		Matcher mat = pat.matcher(value);
		return mat.find();
	}

	/**
	 * @author: zhaguitao
	 * @Title: makeCusPhotoFileName
	 * @Description: 自定义照片文件名
	 * @return
	 * @date: 2014-5-27 下午4:19:20
	 */
	public static String makeCusPhotoFileName() {
		return "IMG_" + System.currentTimeMillis() + ".jpg";
	}

	public static Intent getTakePickIntent(File f) {
		// 部分三星手机，在启动照相机后，onActivityResult返回的intent为空，
		// 不能将照相后的图片传递到本页面,故此处用指定路径的形式做透传
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	/**
	 * @author: zhaguitao
	 * @Title: getDispName
	 * @Description: 显示好友姓名，优先级：name->nickName->nubeNumber
	 * @param name
	 * @param nickName
	 * @param nubeNumber
	 * @return
	 * @date: 2014-6-11 上午11:18:18
	 */
	public static String getDispName(String name, String nickName,
			String nubeNumber) {
		if (!TextUtils.isEmpty(name)) {
			return name;
		} else if (!TextUtils.isEmpty(nickName)) {
			return nickName;
		} else if (!TextUtils.isEmpty(nubeNumber)) {
			return nubeNumber;
		} else {
			return "";
		}
	}






	// /**
	// * 根据群成员名字，返回默认群名称-add at 15/6/18
	// * @param namesList
	// * @return
	// */
	// public static String getDefaultGroupName(Map<String, String>
	// nubeNameList) {
	// if (nubeNameList == null) {
	// return "";
	// }
	// String defaultName = "";
	// Set set = nubeNameList.keySet();
	// Iterator it = set.iterator();
	//
	// if (set.size() > 3) {
	// for (int i = 0; i < 3; i++) {
	// if (i > 0) {
	// defaultName += "、";
	// }
	// defaultName += nubeNameList.get(it.next());
	// }
	// } else {
	// for (int i = 0; i < nubeNameList.size(); i++) {
	// if (i > 0) {
	// defaultName += "、";
	// }
	// defaultName += nubeNameList.get(it.next());
	// }
	// }
	// return defaultName;
	// }

	/**
	 * 
	 * @author: lihs
	 * @Title: checkSimCard
	 * @Description: 检测是否有sim 卡
	 * @return
	 * @date: 2013-8-16 上午11:27:31
	 */
	public static boolean checkSimCard(Context mcontext) {
		TelephonyManager manager = (TelephonyManager) mcontext
				.getSystemService(Context.TELEPHONY_SERVICE);
		int states = manager.getSimState();
		if (TelephonyManager.SIM_STATE_ABSENT == states) {
			LogUtil.d(TAG, "checkSimCard", "无sim卡");
			return false;
		}
		return true;
	}

	/***
	 * 获得当前手机的顶层Activity class name
	 * 
	 * @param context
	 * @return format ComponentInfo{package name/full class name}
	 *         ComponentInfo{com
	 *         .channelsoft.netphoneip/com.channelsoft.netphone.
	 *         ui.activity.ipcall.IpCallActivity}
	 */
	public static String getTopActivity(Activity context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return (runningTaskInfos.get(0).topActivity).toString();
		else
			return "";
	}

	/**
	 * @description:名字截取
	 * @param name
	 *            名字
	 * @param isNeedDot
	 *            true:截取后结尾增加省略号 false:截取后结尾不加省略号
	 * @return 截取后的名字
	 */
	public static String getDisplayName(String name, boolean isNeedDot) {
		String tempName = "";
		int basicLength = 5;
		try {
			if (name.length() <= 6 || name.getBytes("utf-8").length <= 12) {
				tempName = name;
			} else {
				tempName = name.substring(0, 6);

				while (tempName.getBytes("utf-8").length < 12) {
					basicLength++;
					tempName = name.substring(0, basicLength);
				}
				if (isNeedDot) {
					tempName = tempName + "...";
				}
			}
		} catch (UnsupportedEncodingException e) {
			LogUtil.e(TAG, "getDisplayName", "UnsupportedEncodingException", e);
		}
		return tempName;
	}

	public static void restartCallManangerServerByAlert(Context context) {
//		if (context != null) {
//			PendingIntent mAlarmSender = PendingIntent.getService(context, 0,
//					new Intent(context, CallManageService.class), 0);
//			long firstTime = SystemClock.elapsedRealtime();
//			AlarmManager am = (AlarmManager) context
//					.getSystemService(Activity.ALARM_SERVICE);
//			am.cancel(mAlarmSender);
//			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
//					30 * 1000, mAlarmSender);
//		}
	}

	public static void cancalAlert(Context context) {
//        if (context != null) {
//            PendingIntent mAlarmSender = PendingIntent.getService(context, 0,
//                    new Intent(context, CallManageService.class), 0);
//            AlarmManager am = (AlarmManager) context
//                    .getSystemService(Activity.ALARM_SERVICE);
//            am.cancel(mAlarmSender);
//        }
	}

	/**
	 * @description:图片、视频加阴影
	 */
	public static void changeLight(ImageView imageview, int brightness) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.set(new float[] { 1, 0, 0, 0, brightness, 0, 1, 0, 0,
				brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0 });
		imageview.setColorFilter(new ColorMatrixColorFilter(matrix));
	}

	/**
	 * @author: zhaguitao
	 * @Title: isValidPath
	 * @Description: 是否合法的本地路径
	 * @param imagePath
	 * @return
	 * @date: 2013-8-19 上午11:47:46
	 */
	public static boolean isValidLocalPath(String imagePath) {
		if (TextUtils.isEmpty(imagePath) || imagePath.endsWith(".temp")) {
			return false;
		}
		File file = new File(imagePath);
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	/**
	 * 根据Exception，得到方法
	 */
	public static String getMethodName(Exception e, String defaultName) {
		String methodName = "";
		StackTraceElement el = null;
		try {
			el = e.getStackTrace()[1];
			methodName = el.getMethodName();
		} catch (Exception ex) {
			LogUtil.e(TAG, "getMethodName", "Exception", ex);
			if (TextUtils.isEmpty(methodName)) {
				methodName = defaultName;
			}
		}
		el = null;
		return methodName;
	}

	public static String[] getClassMethod(Exception e, String defaultClass,
			String defaultMethod) {
		String methodName = "";
		String className = "";
		StackTraceElement el = null;
		try {
			el = e.getStackTrace()[1];
			className = el.getClassName();
			className = className.substring(className.lastIndexOf(".") + 1);
			methodName = el.getMethodName();
		} catch (Exception ex) {
			LogUtil.e(TAG, "getClassMethod", "Exception", ex);
			if (TextUtils.isEmpty(className)) {
				className = defaultClass;
			}
			if (TextUtils.isEmpty(methodName)) {
				methodName = defaultMethod;
			}
		}
		el = null;
		return new String[] { className, methodName };
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void copy2Clipboard(Context context, String value) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager cmb = (android.text.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setText(value);
		} else {
			android.content.ClipboardManager cmb = (android.content.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setText(value);
		}
	}





	/**
	 * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为1,英文字符长度为0.5
	 * 
	 * @param String
	 *            s 需要得到长度的字符串
	 * @return int 得到的字符串长度
	 */
	public static double getLength(String s) {
		double valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < s.length(); i++) {
			// 获取一个字符
			String temp = s.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为1
				valueLength += 1;
			} else {
				// 其他字符长度为0.5
				valueLength += 0.5;
			}
		}
		// 进位取整
		return Math.ceil(valueLength);
	}

	/**
	 * 获取字节长度
	 * 
	 */
	public static int getWordCount(String s) {
		s = s.replaceAll("[^\\x00-\\xff]", "**");
		int length = s.length();
		return length;
	}

	/**
	 * 
	 * Description:长度超过3个汉字（6个字符）字符串，自动截取前三个字，多于的已省略号...标识
	 * 
	 * @param str
	 *            要截取的字符串
	 * @param n
	 *            截取的字节数
	 * @return
	 */
	public static String getFormatString(String str, int n) {
		// 格式化字符串长度，超出部分显示省略号,区分汉字跟字母。汉字2个字节，字母数字一个字节
		String temp = "";
		if (getWordCount(str) <= n)// 如果长度比需要的长度n小,返回原字符串
		{
			return str;
		} else {
			int t = 0;
			int i = 0;
			char[] q = str.toCharArray();
			for (i = 0; i < q.length && t < n; i++) {
				if ((int) q[i] >= 0x4e00 && (int) q[i] <= 0x9fa5)// 是否汉字
				{
					t += 2;
				} else {
					t++;
				}
				// if (t <= n) {//判断如果此时字符数大于要求显示的字符数n，是否取该汉字
				temp += q[i];
				// }
			}
			if (t == getWordCount(str)) {
				return temp;
			} else {
				return (temp + "...");
			}
		}
	}


	public static AnimationSet getMyAnimSet(float fromY, float toY,
			float fromAlpha, float toAlpha) {
		Animation translateAnim = new TranslateAnimation(0, 0, fromY, toY);
		translateAnim.setDuration(1000);
		translateAnim.setInterpolator(new LinearInterpolator());

		Animation alphaAnim = new AlphaAnimation(fromAlpha, toAlpha);
		alphaAnim.setDuration(1000);
		alphaAnim.setInterpolator(new LinearInterpolator());

		AnimationSet animSet = new AnimationSet(false);
		animSet.setFillAfter(true);
		animSet.addAnimation(alphaAnim);
		animSet.addAnimation(translateAnim);
		return animSet;
	}



	/**
	 * 
	 * Description:验证手机号码合法性判断，客户端校验，建议仅校验前三位 ，即 13*，15*，18*, 17*,145*,147*
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public static boolean cheakPhoneNumber(String phoneNumber) {
		String number = phoneNumber.replace(" ", "").replace("-", "");
		if (number.matches("^1[3,5,7,8][0-9]{9}$")
				|| number.matches("^14[5,7][0-9]{8}$")
				|| number.matches("^[+][8][6][1][4][5,7][0-9]{8}$")
				|| number.matches("^[+][8][6][1][3,5,7,8][0-9]{9}$")) {
			return true;
		} else {
			return false;
		}
	}

	public static ArrayList<String> getList(String str) {
		LogUtil.d("解析@功能字符串里面的特定的name：" + str);
		ArrayList<String> result = new ArrayList<String>();
		String nameStr = "";
		int pos = str.indexOf(CommonConstant.SPECIAL_CHAR);
		if (pos != -1 && str.indexOf("@") != -1) {
			String startStr = str.substring(0, pos + 1);
			if (startStr.indexOf("@") != -1) {
				nameStr = startStr
						.substring(startStr.lastIndexOf("@"), pos + 1);
				result.add(nameStr);
			}
			String endStr = str.substring(pos + 1, str.length());
			while (endStr.indexOf(CommonConstant.SPECIAL_CHAR) != -1
					&& endStr.indexOf("@") != -1) {
				int position = endStr.indexOf(CommonConstant.SPECIAL_CHAR);
				startStr = endStr.substring(0, position + 1);
				if (startStr.indexOf("@") != -1) {
					nameStr = startStr.substring(startStr.lastIndexOf("@"),
							position + 1);
					result.add(nameStr);
				}
				endStr = endStr.substring(position + 1, endStr.length());
			}
		}
		// Pattern p = Pattern.compile("^@.*\\" + ch + "$");
		// Matcher m = p.matcher(str);
		// ArrayList<String> result = new ArrayList<String>();
		// while (m.find()) {
		// result.add(m.group());
		// }
		return result;
	}

	public static ArrayList<String> getDispList(String str) {
		LogUtil.d("解析@功能字符串里面的特定的nube：" + str);
		ArrayList<String> result = new ArrayList<String>();
		String nameStr = "";
		int pos = str.indexOf(CommonConstant.SPECIAL_CHAR);
		if (pos != -1 && str.indexOf("@") != -1) {
			String startStr = str.substring(0, pos + 1);
			nameStr = startStr.substring(startStr.lastIndexOf("@") + 1, pos);
			if (nameStr.length() == 8 && StringUtil.isNumeric(nameStr)) {
				result.add(nameStr);
			}
			String endStr = str.substring(pos + 1, str.length());
			while (endStr.indexOf(CommonConstant.SPECIAL_CHAR) != -1
					&& endStr.indexOf("@") != -1) {
				int position = endStr.indexOf(CommonConstant.SPECIAL_CHAR);
				startStr = endStr.substring(0, position + 1);
				nameStr = startStr.substring(startStr.lastIndexOf("@") + 1,
						position);
				if (nameStr.length() == 8 && StringUtil.isNumeric(nameStr)) {
					result.add(nameStr);
				}
				endStr = endStr.substring(position + 1, endStr.length());
			}
		}
		return result;
		// Pattern p = Pattern.compile("^@[0-9]{8}" + ch );
		// Matcher m = p.matcher(str);
		// ArrayList<String> result = new ArrayList<String>();
		// while (m.find()) {
		// result.add(m.group());
		// }
		// return result;
	}


/**
 * 
 * @param text 输入字符串
 * @param length（汉字2个）
 * @return
 */
	public static String getSubStringByMaxLength(String text,int length){
		String sub="";
		for (int i=text.length();i>0;i--){
			if (getStringLength(text.substring(0, i))<=length){
				sub=text.substring(0, i);
				break;
			}
		}
		return sub;
	}
	
	public static boolean isChineseChar(String str) {
		boolean temp = false;
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			temp = true;
		}
		return temp;
	}

	/**
	 * @param str
	 * @return 汉字2个，其他1个
	 */
	public static int getStringLength(String str) {
		int length = 0;
		for (int i = 0; i < str.length(); i++) {
			String c = String.valueOf(str.charAt(i));
			length = length + (isChineseChar(c) ? 2 : 1);
		}
		return length;
	}
	
	public static String getLimitSubstring(String inputStr, int length) {
	    if (inputStr == null) {
	      return "";
	    }
	    char[] ch = inputStr.toCharArray();
	    int varlength = 0;
	    for (int i = 0; i < ch.length; i++) {
	      if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F)
	          || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {
	        varlength = varlength + 2;
	      } else {
	        varlength++;
	      }
	      if (varlength > length) {
	        return  inputStr = inputStr.substring(0, i) + "...";
	      }
	    }
	    return inputStr;
	  }

	
	private boolean isBigFile(long size){
		boolean flag = true;
    	long bigSize = 1024 * 1024 * 3;
    	if(size > bigSize){
    		flag = true;
    	}else{
    		flag = false;
    	}
    	return flag;
	}

}
