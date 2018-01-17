package cn.redcdn.hvs.appinstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.datacenter.versionupdate.CheckAppVersion;
import cn.redcdn.datacenter.versionupdate.CheckAppVersionInfo;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpDownloadFile;
import cn.redcdn.network.httprequest.HttpErrorCode;
import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import cn.redcdn.incoming.IncomingMessageManage;

public class MeetingVersionManager {

	public static final int CHECK_MEETING_VERSION = 0x00000012;
	private final String tag = MeetingVersionManager.class.getName();
	private SharedPreferences meetingVersionServiceSetting = null;
	private static MeetingVersionManager instance = null;
	private List<IVersionListener> meetingListenerList = null;
	private Context context;
	private static final int MEETING_ONCHECK_FAILED = 0x00000015;
	private static final int MEETING_ONUPDATE_FAILED = 0x00000016;
	public static final int MEETING_ONUPDATE_SUCCEED = 0x00000017;
	private static final int MEETING_ONCHECK_NONEED_UPDATE = 0x00000018;
	private static final int MEETING_SDCARD_IS_NOT_MOUNT = 0x00000019;
	private final static int MEETING_CHECK_FILE_LEGAL_FAILED = 0x00000020;
	private static final int MEETING_EXIST_NEW_VERSION = 0x00000031;
	private static final int MEETING_CHANGE_UI = 0x00000039;
	private static final int MEETING_ONCHECK_FAILED_FOR_INSTALL = 0x00000041;
	private static final int MEETING_ONCHECK_SUCCEED_FOR_INSTALL = 0x00000042;
	private static final int MEETING_TOAST_LENGTH = 5;
	private String appInstallPackage;
	private String appInstallVersion;
	private String packetName = "";
	private String meetingUpdateDir;
	private String meetingName = MedicalApplication.getContext().getPackageName();
	private String versionName = "";
	private String deviceType;// 设备名称
	private String appHashCode;
	private String updatePlugin;
	private String changelist;
	private int wholeSize;
	private int state;
	private int percent = -1;
	private boolean isHasSetSize = false;
	private boolean isForcedDialogShowing = false;
	private String path;
	private static final int INIT_MEETING_VERSION_MANAGER = 0x00000011;
	private static final int DOWLOAD_MEETING_FILE = 0x00000013;
	private int isForcedInstall = VERSION_COMPATIBLE;
	private boolean isHasNew = false;// 是否有另外一个新的版本
	public static final String MEETING_PATH = "meetingAppPath";
	public static final String MEETING_VERSION = "meetingVersion";
	public static final String MEETING_HASH_CODE = "meetingHashCode";
	public static final String MEETING_DOWNLOAD_URL = "meetingUrl";
	public static final String MEETING_DOWNLOAD_SIZE = "meetingSize";
	public static final String MEETING_DOWNLOAD_CHANGELIST = "meetingChangelist";
	private InstallCallBackListerner installCallBack = null;
	private boolean isInited = false;
	private HandlerThread appCheckThread;
	private Handler appCheckHandler;
	private static final int VERSION_COMPATIBLE = 1;
	private static final int VERSION_NOT_COMPATIBLE = 0;
	private static final int VERSION_NOT_NEED_INSTALL = 2;
	private static final int VERSION_ERROR_CONDITION = 3;
	private boolean isFromBoot = false;
	private boolean isDownloadAbort = false;
	private HttpDownloadFile downloader;
	private CheckAppVersion checkAppVersion;
	// private HttpDownloadFile forceDownloadFile;

	private Handler mainThreadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MEETING_CHANGE_UI:
				Intent intent = new Intent();
				intent.setAction("cn.redcdn.meeting.changeui");
				intent.putExtra("ui", "ReTry");//重试
				intent.putExtra("msg", msg.getData().getString("msg"));
				context.sendBroadcast(intent);
				state = INIT_MEETING_VERSION_MANAGER;
				break;
			case MEETING_ONCHECK_FAILED:
				CustomLog.d(tag, "received MEETING_ONCHECK_FAILED");
				break;
			case MEETING_ONUPDATE_FAILED:
				CustomLog.d(tag, "received MEETING_ONUPDATE_FAILED");
				break;
			case MEETING_ONUPDATE_SUCCEED:
				CustomLog.d(tag, "received MEETING_ONUPDATE_SUCCEED");
				notifyListener(MEETING_ONUPDATE_SUCCEED);
				break;
			case MEETING_ONCHECK_NONEED_UPDATE:
				CustomLog.d(tag, "received MEETING_ONCHECK_NONEED_UPDATE");
				break;
			case MEETING_SDCARD_IS_NOT_MOUNT:
				CustomLog.d(tag, "received MEETING_SDCARD_IS_NOT_MOUNT");
				break;
			case MEETING_EXIST_NEW_VERSION:
				CustomLog.d(tag, "received MEETING_EXIST_NEW_VERSION");
				break;
			case MEETING_ONCHECK_FAILED_FOR_INSTALL:
				CustomLog.i(tag,"升级失败");
				if (!isFromBoot) {
					CustomToast.show(context,context.getString(R.string.installationPackageIsnotlegal),Toast.LENGTH_LONG);
				}
				deleteAllFile();
				clearVersionInfo(context);
				checkVersion(context);// 已经升级失败  清除相关信息就可以了   为什么还要检测版本
				break;
			case MEETING_ONCHECK_SUCCEED_FOR_INSTALL:
				CustomLog.i(tag,"MEETING_ONCHECK_SUCCEED_FOR_INSTALL");
				if (isFromBoot) {
					CustomToast.show(context,context.getString(R.string.noInstallWarning),Toast.LENGTH_LONG);
				}
				switchToAppInstall();
				break;
			default:
				CustomLog.d(tag, "received invalidate msg MEETING auto update");
				break;
			}
		}
	};

	 public int registerVersionListener(IVersionListener listener) {
		    CustomLog.d(tag, "registerVersionListener");
		    if (meetingListenerList == null) {
		      meetingListenerList = new ArrayList<IVersionListener>();
		    }

		    if (listener != null && !meetingListenerList.contains(listener)) {
		      meetingListenerList.add(listener);
		    }
		    if (isHasInstall(context)) {
		      notifyListener(MEETING_ONUPDATE_SUCCEED);
		    }
		    return 0;
		  }

	 public void unRegisterVersionListener(IVersionListener listener) {

		    if (meetingListenerList != null && meetingListenerList.size() > 0
		        && listener != null && meetingListenerList.contains(listener)) {
		      meetingListenerList.remove(listener);
		    }
		  }

	private void notifyListener(int code) {
		CustomLog.i(tag,"notifyListener::code=="+code);
		 if (meetingListenerList != null && meetingListenerList.size() > 0) {
		      for (int i = 0; i < meetingListenerList.size(); i++) {
		        IVersionListener listener = meetingListenerList.get(i);
		        listener.onVersionChange(code);
		      }
		    }
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
//			if (intent.getAction().equals(
//					IncomingMessageManage.START_MEETING_BROADCAST)) {
//				CustomLog.d(tag, "收到进入会议广播 "
//						+ IncomingMessageManage.START_MEETING_BROADCAST);
//				if (downloader != null) {
//					downloader.cancel();
//					downloader = null;
//					if (wholeSize > 0) {
//						setSharedPreferences(new DownLoadInfo(appHashCode,
//								path, appInstallVersion, 0, wholeSize,
//								appInstallPackage, changelist));
//					}
//					isDownloadAbort = true;
//					state = INIT_MEETING_VERSION_MANAGER;
//				}
//			} else if (intent.getAction().equals(
//					IncomingMessageManage.END_MEETING_BROADCAST)) {
//				CustomLog.d(tag, "收到退出会议广播 "
//						+ IncomingMessageManage.END_MEETING_BROADCAST);
//				if (isDownloadAbort) {
//					checkVersion(context);
//					isDownloadAbort = false;
//				}
//			}
		}
	};

	public synchronized static MeetingVersionManager getInstance() {
		if (instance == null) {
			instance = new MeetingVersionManager();
		}
		return instance;
	}

	public int init(Context context) {
		CustomLog.i(tag,"init");
		if (isInited) {
			CustomLog.d(tag,
					"MeetingVersionManager daemon service has initialized");
			return 0;
		}
		isInited = true;
		this.context = context;
		CustomLog.d(tag, "MeetingVersionManager init");
		deviceType = SettingData.getInstance().AppUpdateConfig.DeviceType;
		meetingUpdateDir = SettingData.getInstance().AppRestorePath;
		if (packetName.equals("") || versionName.equals("")) {
			initAppVersionName();
		}
		appCheckThread = new HandlerThread("AppCheckThread");
		appCheckThread.start();
		appCheckHandler = new Handler(appCheckThread.getLooper());
		registerBroadcastReceiver();
		state = INIT_MEETING_VERSION_MANAGER;
		return 0;
	}

	public void release() {
		CustomLog.d(tag, "release");
		isInited = false;
		 if (meetingListenerList != null) {
		      meetingListenerList = null;
		    }
		unRegisterBroadcastReceiver();
	}

	public void checkVersion(Context context) {
		CustomLog.i(tag,"checkVersion");
		if (!isInited) {
			CustomLog.e(tag, "checkVersion isInited " + isInited);
			return;
		}
		if (packetName.equals("") || versionName.equals("")) {
			initAppVersionName();
		}
		if (state == INIT_MEETING_VERSION_MANAGER) {
			checkAppVersion(); //获取服务器版本信息
		}else{
			CustomLog.i(tag,"state=="+state);
		}
	}
	
	/**
	 * 取消版本检测
	 */
	public void cancelCheckVersion(){
		CustomLog.i(tag,"cancelCheckVersion");
		if(checkAppVersion != null){
			checkAppVersion.cancel();
		}
	}
	
	public void setCallBackNull() {
		CustomLog.i(tag,"setCallBackNull");
		if (installCallBack != null) {
			installCallBack = null;
		}
	}

	private void doCallBack(int isForced, int code) {
		CustomLog.i(tag,"doCallBack"+"isForced=="+isForced+"code=="+code);
		if (installCallBack != null) {
			switch (isForced) {
			case VERSION_NOT_COMPATIBLE:
				CustomLog.i(tag, "needForcedInstall");
				installCallBack.needForcedInstall();
				break;
			case VERSION_COMPATIBLE:
				CustomLog.i(tag, "needOptimizationInstall");
				installCallBack.needOptimizationInstall();
				break;
			case VERSION_NOT_NEED_INSTALL:
				CustomLog.i(tag, "noNeedInstall");
				installCallBack.noNeedInstall();
				break;
			case VERSION_ERROR_CONDITION:
				CustomLog.i(tag, "errorCondition");
				installCallBack.errorCondition(code);
				break;
			default:
				CustomLog.i(tag, "received invalidate msg doCallBack");
				break;
			}
			// installCallBack = null;
		}
	}


	/**
	 * 获取服务器版本信息
	 * @return 校验结果同步回调
	 */
	private int checkAppVersion() {
            CustomLog.i(tag,"checkAppVersion::");
	        checkAppVersion = new CheckAppVersion() {
			@Override
			protected void onSuccess(CheckAppVersionInfo responseContent) {

				if (null == responseContent) {
					CustomLog
							.e(tag,
									"invalidate meeting  version check result null == responseContent ");
					sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONCHECK_FAILED);
					doCallBack(VERSION_ERROR_CONDITION, -1);
					KeyEventWrite.write(KeyEventConfig.CHECK_APP_INSTALL
							+ "_fail" + "_" + "Mobile" + "_"
							+ "null == responseContent");
					return;
				}
				try {
					if (responseContent.checkValue == 1) {
						long interval = responseContent.interval;
						updatePlugin = responseContent.updatePlugin;
						appInstallPackage = responseContent.appInstallPackage;
						appHashCode = responseContent.appHashCode;
						appInstallVersion = responseContent.appInstallVersion;
						isForcedInstall = responseContent.isForcedInstall;
						changelist = responseContent.descriptions;
						splitVersion();
						CustomLog
								.i(tag,
										"low version package installed,need update,inerval:"
												+ interval + ",updatePlugin:"
												+ updatePlugin
												+ ",appInstallPackage:"
												+ appInstallPackage
												+ ",appHashCode" + appHashCode
												+ ",version"
												+ appInstallVersion
												+ " isForcedInstall "
												+ isForcedInstall+" changelist "+changelist);
						KeyEventWrite.write(KeyEventConfig.CHECK_APP_INSTALL
								+ "_ok" + "_" + "Mobile");
						if (isForcedInstall == VERSION_NOT_COMPATIBLE) {//强制升级
							CustomLog.i(tag, "isForcedInstall "
									+ isForcedInstall);
							doCallBack(VERSION_NOT_COMPATIBLE, 0);
							String path = meetingUpdateDir + File.separator
									+ meetingName + "_" + appInstallVersion
									+ ".apk";
							if (isHasInstall(context)
									&& isTheSameApp(appInstallPackage, path)) {
								state = INIT_MEETING_VERSION_MANAGER;
								checkCodeBeforeInstall(true);
							} else {
								showForceInstallDialog(changelist);
							}
						} else {
							doCallBack(VERSION_COMPATIBLE, 0);
							if (isHasInstall(context)) {
								if (meetingVersionServiceSetting == null) {
									meetingVersionServiceSetting = context
											.getSharedPreferences(
													"meetingVersionServiceSetting",
													Context.MODE_PRIVATE);
								}
								String hashcoe = meetingVersionServiceSetting
										.getString(MEETING_HASH_CODE, "");
								if (!TextUtils.isEmpty(hashcoe)
										&& appHashCode.equals(hashcoe)) {
									sendUpdateTaskResponse(MeetingVersionManager.MEETING_EXIST_NEW_VERSION);
									CustomLog.d(tag,
											"SharedPreferences中 ： 有下载的新版本等待更新");
								} else {
									isHasNew = true;
									doUpdateThings();
								}
							} else {
								doUpdateThings();
							}
						}
					} else {
						doCallBack(VERSION_NOT_NEED_INSTALL, 0);
						CustomLog.i(tag,
								"meeting check version not need update");
						sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONCHECK_NONEED_UPDATE);
						KeyEventWrite.write(KeyEventConfig.CHECK_APP_INSTALL
								+ "_ok" + "_" + "Mobile");
					}
				} catch (Exception e) {
					CustomLog.e(tag,
							" meeting check version or update Exception" + e);
					sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONCHECK_FAILED);
					doCallBack(VERSION_ERROR_CONDITION, -1);
					KeyEventWrite.write(KeyEventConfig.CHECK_APP_INSTALL
							+ "_fail" + "_" + "Mobile" + "_" + e);
				}

			}

			@Override
			protected void onFail(int statusCode, String statusInfo) {
				CustomLog.e(tag, "meeting version check failed:" + statusCode);
				sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONCHECK_FAILED);
				if (statusCode == 0) {
					doCallBack(VERSION_NOT_NEED_INSTALL, statusCode);
					KeyEventWrite.write(KeyEventConfig.CHECK_APP_INSTALL
							+ "_ok" + "_" + "Mobile" + "_" + statusInfo);
				} else {
					doCallBack(VERSION_ERROR_CONDITION, statusCode);
					KeyEventWrite.write(KeyEventConfig.CHECK_APP_INSTALL
							+ "_fail" + "_" + "Mobile" + "_" + statusInfo);
				}

			}
		};
		int result = checkAppVersion.checkVersion(versionName, packetName,
				deviceType,
				SettingData.getInstance().AppUpdateConfig.ProjectName);
		if (result >= 0) {
			state = CHECK_MEETING_VERSION;
		}
		return result;
	}

	private void deleteFile() {
		CustomLog.i(tag,"deleteFile");
		if (meetingUpdateDir != null) {
			File path1 = new File(meetingUpdateDir);
			String[] filelist = null;
			if (path1 != null) {
				filelist = path1.list();
			}
			if (meetingVersionServiceSetting == null) {
				meetingVersionServiceSetting = context.getSharedPreferences(
						"meetingVersionServiceSetting", Context.MODE_PRIVATE);
			}
			String path = meetingVersionServiceSetting.getString(MEETING_PATH,
					"");
			CustomLog.e(tag, "path " + path);
			if (filelist != null) {
				for (int i = 0; i < filelist.length; i++) {
					String oldFile = meetingUpdateDir + File.separator
							+ filelist[i];
					if (!oldFile.equals(path)) {
						File delfile = new File(oldFile);
						delfile.delete();
					}
				}
			}
		}
	}

	private void deleteAllFile() {
		CustomLog.i(tag,"deleteAllFile::");
		if (meetingUpdateDir != null) {
			File path1 = new File(meetingUpdateDir);
			String[] filelist = null;
			if (path1 != null) {
				filelist = path1.list();
			}
			if (filelist != null) {
				for (int i = 0; i < filelist.length; i++) {
					String oldFile = meetingUpdateDir + File.separator
							+ filelist[i];
					File delfile = new File(oldFile);
					delfile.delete();
				}
			}
		}
	}

	public long getFileSize(String path) {
		CustomLog.i(tag,"path");
		long size = 0;
		try {
			File f = new File(path);
			if (f.exists() && f.isFile()) {
				size = f.length();
			}
		} catch (Exception e) {
			CustomLog.e(tag, "getFileSize " + e);
		}
		return size;
	}


	/**
	 * 执行下载逻辑
	 * @param pluginUrl
	 * @param apkUrl
	 * @param localPath
	 * @param packageName
	 * @return
	 */
	private int update(String pluginUrl, final String apkUrl,
			final String localPath, String packageName) {
		CustomLog.i(tag,"update");
		if (null == pluginUrl || null == apkUrl || null == localPath
				|| null == packageName) {
			CustomLog.e(tag,
					"meeting  update failed: invalidate input parameter");
			sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONUPDATE_FAILED);
			if (isForcedDialogShowing) {
				Message msg = new Message();
				msg.what = MEETING_CHANGE_UI;
				Bundle bundle = new Bundle();
				bundle.putString("msg", context.getString(R.string.parameterNotLegal));
				msg.setData(bundle);
				mainThreadHandler.sendMessage(msg);
			}
			return -1;
		}
		CustomLog.i(tag, "begin download meeting update plugin");
		if (downloader != null) {
			downloader.cancel();
			downloader = null;
			isHasSetSize = false;
		}
		downloader = new HttpDownloadFile() {

			@Override
			protected void OnDownloadFinish() {
				isHasSetSize = false;
				CustomLog.i(tag, "download  meeting update plugin ok");
				File file = new File(localPath);
				if (!file.exists()) {
					CustomLog
							.e(tag, "download  meeting update plugin 文件下载后不存在");
					sendUpdateTaskResponse(MeetingVersionManager.MEETING_CHECK_FILE_LEGAL_FAILED);
					KeyEventWrite.write(KeyEventConfig.DOWNLOAD_APP_APK
							+ "_fail" + "_" + "Mobile" + "_" + "下载升级包失败");
					if (isForcedDialogShowing) {
						Message msg = new Message();
						msg.what = MEETING_CHANGE_UI;
						Bundle bundle = new Bundle();
						bundle.putString("msg", context.getString(R.string.theUpgradepackageFailed));
						msg.setData(bundle);
						mainThreadHandler.sendMessage(msg);
					}
					return;
				}
				checkAppHashCode(localPath);
				KeyEventWrite.write(KeyEventConfig.DOWNLOAD_APP_APK + "_ok"
						+ "_" + "Mobile");
			}

			@Override
			protected void OnDownloadFail(int statusCode) {
				CustomLog.e(tag, "download meeting update plugin failed:"
						+ statusCode);
				sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONUPDATE_FAILED);
				KeyEventWrite.write(KeyEventConfig.DOWNLOAD_APP_APK + "_fail"
						+ "_" + "Mobile" + "_" + statusCode);
				isHasSetSize = false;
				if (isForcedDialogShowing) {
					Message msg = new Message();
					msg.what = MEETING_CHANGE_UI;
					Bundle bundle = new Bundle();
					if (HttpErrorCode.checkNetworkError(statusCode)) {
						bundle.putString("msg", context.getString(R.string.failedToInternet)); //
					} else {
						bundle.putString("msg", "下载升级包失败,错误码:" + statusCode); //
					}
					msg.setData(bundle);
					mainThreadHandler.sendMessage(msg);
				}

			}

			@Override
			protected void onDownloadProgress(final int fileSize,
					final int complete) {
				super.onDownloadProgress(fileSize, complete);
				appCheckHandler.post(new Runnable() {
					@Override
					public void run() {
						if (!isHasSetSize) {
							if (getWholeSize(apkUrl, localPath) >= fileSize) {
								wholeSize = getWholeSize(apkUrl, localPath);
							} else {
								wholeSize = fileSize;
							}
							isHasSetSize = true;
							setSharedPreferences(new DownLoadInfo(appHashCode,
									path, appInstallVersion, 0, wholeSize,
									appInstallPackage, changelist));
						}
						if (isForcedDialogShowing) {
							int percentLocal = (int) (((float) complete / wholeSize) * 100);
							percent = percentLocal;
							Intent intent = new Intent();
							intent.setAction("cn.redcdn.meeting.changeprogress");
							intent.putExtra("progress", percent);
							context.sendBroadcast(intent);
						}
					}
				});

			}
		};
		int re = 0;
		try {
			compareVersion();
			long mcomplete = getCompeleteSize(apkUrl, localPath);
			CustomLog.d(tag, "优化下载之前信息 mcomplete=  " + mcomplete + " apkUrl "
					+ apkUrl + " localPath " + localPath);
			if (isForcedDialogShowing && mcomplete > 0 && getSize() > 0) {
				int percentLocal = (int) (((float) mcomplete / getSize()) * 100);
				percent = percentLocal;
				Intent intent = new Intent();
				intent.setAction("cn.redcdn.meeting.changeprogress");
				intent.putExtra("progress", percent);
				context.sendBroadcast(intent);
			}
			re = downloader.downloadFile(apkUrl, localPath, (int) mcomplete);
		} catch (FileNotFoundException e) {
			CustomLog.e(tag, "download meeting FileNotFoundException:" + e);
		}
		state = DOWLOAD_MEETING_FILE;
		CustomLog.d(tag, "state is " + state +"downloader result=="+re);
		return re;
	}

	private void splitVersion() {
		if (appInstallVersion != null) {
			String[] s = appInstallVersion.split("v");
			if (s != null && s.length == 2) {
				appInstallVersion = s[1];
			}
		}
	}

	private void showForceInstallDialog(String list) {
			CustomLog.d(tag, "showForceInstallDialog");
			if (!isForcedDialogShowing) {
				isForcedDialogShowing = true;
				MedicalApplication.shareInstance().startInstallActivity(list);
			}
		}

		private void setSharedPreferences(DownLoadInfo info) {
			CustomLog.i(tag,"setSharedPreferences  版本检测版本信息=="+info.toString());
			if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		Editor editor = meetingVersionServiceSetting.edit();
		editor.putString(MEETING_PATH, info.getPath());
		editor.putString(MEETING_VERSION, info.getVersion());
		editor.putString(MEETING_HASH_CODE, info.getHashCode());
		editor.putString(MEETING_DOWNLOAD_URL, info.getUrl());
		editor.putInt(MEETING_DOWNLOAD_SIZE, info.getSize());
		editor.putString(MEETING_DOWNLOAD_CHANGELIST, info.getChagelist());
		editor.commit();
	}

	private long getCompeleteSize(String url, String path) {
		CustomLog.i(tag,"getCompeleteSize::"+"url=="+url+"path=="+path);
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		if (meetingVersionServiceSetting.getString(MEETING_DOWNLOAD_URL, "")
				.equals(url)
				&& meetingVersionServiceSetting.getString(MEETING_PATH, "")
						.equals(path)) {
			CustomLog.d(tag, "getCompeleteSize 下载地址和存储路径一致 ："
					+ getFileSize(path));
			return getFileSize(path);
		} else {
			CustomLog.d(tag, "getCompeleteSize 下载地址和存储路径不一致 ");
			return 0;
		}
	}


	/**
	 * 根据下载路径和存放路径 判断是否是同一个apk
	 * @param url
	 * @param path
	 * @return
	 */
	private boolean isTheSameApp(String url, String path) {
		CustomLog.i(tag,"isTheSameApp");
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		if (meetingVersionServiceSetting.getString(MEETING_DOWNLOAD_URL, "")
				.equals(url)
				&& meetingVersionServiceSetting.getString(MEETING_PATH, "")
						.equals(path)) {
			return true;
		} else {
			return false;
		}
	}

	private int getWholeSize(String url, String path) {
		CustomLog.i(tag,"getWholeSize::"+"url=="+url+"path=="+path);
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		if (meetingVersionServiceSetting.getString(MEETING_DOWNLOAD_URL, "")
				.equals(url)
				&& meetingVersionServiceSetting.getString(MEETING_PATH, "")
						.equals(path)) {
			return meetingVersionServiceSetting
					.getInt(MEETING_DOWNLOAD_SIZE, 0);
		} else {
			return 0;
		}
	}

	public String getChangelist() {
		CustomLog.i(tag,"getChangelist::");
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		String list=meetingVersionServiceSetting.getString(MEETING_DOWNLOAD_CHANGELIST, "");
		CustomLog.e(tag, "MEETING_DOWNLOAD_CHANGELIST "+list);
		return list;
 	}

	private int getSize() {
		CustomLog.i(tag,"getSize");
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		return meetingVersionServiceSetting.getInt(MEETING_DOWNLOAD_SIZE, 0);
	}


	/**
	 * 获取当前应用信息   包名  版本号
	 */
	private void initAppVersionName() {
		CustomLog.i(tag,"initAppVersionName");
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			String version = pi.versionName;
			String mPackName = pi.packageName;
			if (version != null && version.length() > 0) {
				versionName = "v" + version;
			}
			if (mPackName != null && mPackName.length() > 0) {
				packetName = mPackName;
			}
		} catch (Exception e) {
			CustomLog
					.e(tag,
							"update daemon service  PackageManager  get version name Exception");
		}
	}

	private void doUpdateThings() {
		CustomLog.i(tag,"doUpdateThings::");
		if (meetingUpdateDir != null) {
			File path1 = new File(meetingUpdateDir);
			if (!path1.exists()) {
				if (!path1.mkdirs()) {
					CustomLog.e(tag, "file.mkdirs is false so do nothing");
					state = INIT_MEETING_VERSION_MANAGER;
					return;
				}
			}
			path = meetingUpdateDir + File.separator + meetingName + "_"
					+ appInstallVersion + ".apk";

			if (NetConnectHelper.getNetWorkType(context) == NetConnectHelper.NETWORKTYPE_INVALID
					|| MedicalApplication.shareInstance().getDownloadSetting()) {
				if (update(updatePlugin, appInstallPackage, path, packetName) < 0) {
					sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONUPDATE_FAILED);
				}
			} else {
				state = INIT_MEETING_VERSION_MANAGER;
				CustomLog.e(tag, "非wifi或者2g/3g/4g状态下不允许下载，so do noting");
			}
		} else {
			sendUpdateTaskResponse(MeetingVersionManager.MEETING_SDCARD_IS_NOT_MOUNT);
		}
	}

	private void checkAppHashCode(final String localPath) {
		CustomLog.i(tag,"checkAppHashCode:: localPath"+localPath);
		appCheckHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					String hash = CheckHashCode.getFileSha1(localPath);
					CustomLog.d(tag, "CheckHashCode " + hash + " appHashCode "
							+ appHashCode);
					if (!hash.equals(appHashCode)) {
						CustomLog.e(tag,
								"invalidate rom update plugin install package");
						sendUpdateTaskResponse(MeetingVersionManager.MEETING_CHECK_FILE_LEGAL_FAILED);
						if (isForcedDialogShowing) {
							Message msg = new Message();
							msg.what = MEETING_CHANGE_UI;
							Bundle bundle = new Bundle();
							bundle.putString("msg", context.getString(R.string.upgradePackageCheckFailed));
							msg.setData(bundle);
							mainThreadHandler.sendMessage(msg);
						}
						return;
					}
					if (isForcedDialogShowing) {
						Intent intent = new Intent();
						intent.setAction("cn.redcdn.meeting.dimiss");
						context.sendBroadcast(intent);
					}
					sendUpdateTaskResponse(MeetingVersionManager.MEETING_ONUPDATE_SUCCEED);
					return;
				} catch (OutOfMemoryError e) {
					CustomLog.e(
							tag,
							"CheckHashCode getFileSha1->OutOfMemoryError"
									+ e.toString());
				} catch (IOException e) {
					CustomLog.e(tag, "CheckHashCode getFileSha1->IOException"
							+ e.toString());
				}
				if (isForcedDialogShowing) {
					Message msg = new Message();
					msg.what = MEETING_CHANGE_UI;
					Bundle bundle = new Bundle();
					bundle.putString("msg", context.getString(R.string.upgradePackageCheckFailed));
					msg.setData(bundle);
					mainThreadHandler.sendMessage(msg);
				}
				sendUpdateTaskResponse(MeetingVersionManager.MEETING_CHECK_FILE_LEGAL_FAILED);
			}
		});
	}

	private int sendUpdateTaskResponse(int valueCode) {
		Message msg = Message.obtain();
		switch (valueCode) {
		case MEETING_ONCHECK_FAILED:
			msg.what = MEETING_ONCHECK_FAILED;
			mainThreadHandler.sendMessage(msg);
			break;
		case MEETING_ONUPDATE_FAILED:
			if (wholeSize > 0) {
				setSharedPreferences(new DownLoadInfo(appHashCode, path,
						appInstallVersion, 0, wholeSize, appInstallPackage,
						changelist));
			}
			deleteFile();
			msg.what = MEETING_ONUPDATE_FAILED;
			mainThreadHandler.sendMessage(msg);
			break;
		case MEETING_CHECK_FILE_LEGAL_FAILED:
			if (!isHasNew) {
				setSharedPreferences(new DownLoadInfo("", "", "", 0, 0, "", ""));
				msg.what = MEETING_ONUPDATE_FAILED;
				mainThreadHandler.sendMessage(msg);
			}
			deleteFile();
			break;
		case MEETING_ONCHECK_NONEED_UPDATE:
			msg.what = MEETING_ONCHECK_NONEED_UPDATE;
			mainThreadHandler.sendMessage(msg);
			break;
		case MEETING_ONUPDATE_SUCCEED:
			setSharedPreferences(new DownLoadInfo(appHashCode, path,
					appInstallVersion, 0, wholeSize, appInstallPackage,
					changelist));
			msg.what = MEETING_ONUPDATE_SUCCEED;
			mainThreadHandler.sendMessage(msg);
			deleteFile();
			break;
		case MEETING_SDCARD_IS_NOT_MOUNT:
			msg.what = MEETING_SDCARD_IS_NOT_MOUNT;
			mainThreadHandler.sendMessage(msg);
			break;
		case MEETING_EXIST_NEW_VERSION:
			msg.what = MEETING_EXIST_NEW_VERSION;
			mainThreadHandler.sendMessage(msg);
			break;
		default:
			break;
		}
		state = INIT_MEETING_VERSION_MANAGER;
		isHasNew = false;
		CustomLog.d(tag, "state is " + state);
		return 0;
	}


	/**
	 * 清除sharePreference中的缓存信息
	 * @param context
	 */
	public void clearVersionInfo(Context context) {
		CustomLog.i(tag,"clearVersionInfo::");
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		Editor editor = meetingVersionServiceSetting.edit();
		editor.putString(MEETING_PATH, "");
		editor.putString(MEETING_VERSION, "");
		editor.putString(MEETING_HASH_CODE, "");
		editor.putString(MEETING_DOWNLOAD_URL, "");
		editor.putInt(MEETING_DOWNLOAD_SIZE, 0);
		editor.putString(MEETING_DOWNLOAD_CHANGELIST, "");
		editor.commit();
	}

	private void registerBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
//		filter.addAction(IncomingMessageManage.START_MEETING_BROADCAST);
//		filter.addAction(IncomingMessageManage.END_MEETING_BROADCAST);
		context.registerReceiver(mReceiver, filter);
	}

	private void unRegisterBroadcastReceiver() {
		if (null != mReceiver && context != null) {
			context.unregisterReceiver(mReceiver);
		}
	}


	/**
	 * 根据  1、文件是否已经存在  2、版本号是否更新  3、文件大小是否正确   返回true|false
	 * @param context
	 * @return   true 有下载好的应用安装包  | false 没有下载好的应用安装包
	 */
	public boolean isHasInstall(Context context) {
		CustomLog.i(tag,"isHasInstall");
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		String path = meetingVersionServiceSetting.getString(MEETING_PATH, "");
		CustomLog.i(tag,"path"+path);
		String version = meetingVersionServiceSetting.getString(
				MEETING_VERSION, "");
		int complete = (int) getFileSize(path);
		int size = meetingVersionServiceSetting
				.getInt(MEETING_DOWNLOAD_SIZE, 0);
		CustomLog.e(path, path + version);
		if (!TextUtils.isEmpty(path)) {
			File file = new File(path);
			file.setReadable(true, false);
			String currentVersion = getAppVersionName();
			if (file.exists()
					&& VersionComparison.compare(version, currentVersion) == 1
					&& complete == size) {
				CustomLog.i(tag, "MEETING_EXIST_NEW_VERSION ");
				return true;
			}
		}
		CustomLog.i(tag, "MEETING_NOT_EXIST_NEW_VERSION ");
		return false;
	}


	/**
	 *
	 * @param context
	 * @param callBack
	 */
	public void checkOrInstall(Context context,
			InstallCallBackListerner callBack) {
		CustomLog.i(tag,"checkOrInstall::");
		if (!isInited) {
			CustomLog.e(tag, "checkVersion isInited " + isInited);
			return;
		}
		installCallBack = callBack;
		if (isHasInstall(context)) {//判断是否有下载好的安装包
			checkCodeBeforeInstall(true);
		} else {
			if (state != INIT_MEETING_VERSION_MANAGER) {
				CustomLog.d(tag, "checkVersion state " + state);
				doCallBack(VERSION_ERROR_CONDITION, -1);
				return;
			}
			CustomLog.d(tag, "checkVersion state " + state);
			compareVersion();//apk文件存在&&服务器版本比本地版本高 --> 否则  删除本地文件&&清楚缓存信息
			checkVersion(context);//获取服务器版本信息根据服务器版本信息跳转到强制升级页面， 和优化升级页面
		}
	}


	/**
	 * apk文件存在&&服务器版本比本地版本高 --> 否则  删除本地文件&&清楚缓存信息
	 */
	private void compareVersion() {
		CustomLog.i(tag,"compareVersion");
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		String path = meetingVersionServiceSetting.getString(MEETING_PATH, "");
		String version = meetingVersionServiceSetting.getString(
				MEETING_VERSION, "");
		String currentVersion = getAppVersionName();
		if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(version)) {//如果缓存信息中apk地址和版本信息都不为空
			File file = new File(path);
			file.setReadable(true, false);
			if (!file.exists()
					|| VersionComparison.compare(version, currentVersion) != 1) {//1 表示当前版本比服务器版本低
				clearVersionInfo(context);
				deleteAllFile();
			}
		} else {
			clearVersionInfo(context);
			deleteAllFile();
		}
	}


	/**
	 * 比较缓存的hashCode和本地文件的hashCode是否相同
	 * @param fromBoot
	 */
	public void checkCodeBeforeInstall(boolean fromBoot) {
		CustomLog.i(tag,"checkCodeBeforeInstall::");
		isFromBoot = fromBoot;
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		final String localPath = meetingVersionServiceSetting.getString(
				MEETING_PATH, "");
		final String hashCode = meetingVersionServiceSetting.getString(
				MEETING_HASH_CODE, "");
		appCheckHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					String hash = CheckHashCode.getFileSha1(localPath);
					CustomLog.d(tag, "CheckHashCode " + hash + " appHashCode "
							+ hashCode);
					if (!hash.equals(hashCode)) {
						CustomLog.e(tag,
								"invalidate rom update plugin install package");
						Message msg = Message.obtain();
						msg.what = MEETING_ONCHECK_FAILED_FOR_INSTALL;
						mainThreadHandler.sendMessage(msg);
						return;
					}
					Message msg = Message.obtain();
					msg.what = MEETING_ONCHECK_SUCCEED_FOR_INSTALL;
					mainThreadHandler.sendMessage(msg);
					return;
				} catch (OutOfMemoryError e) {
					CustomLog.e(
							tag,
							"CheckHashCode getFileSha1->OutOfMemoryError"
									+ e.toString());
				} catch (IOException e) {
					CustomLog.e(tag, "CheckHashCode getFileSha1->IOException"
							+ e.toString());
				}
				Message msg = Message.obtain();
				msg.what = MEETING_ONCHECK_FAILED_FOR_INSTALL;
				mainThreadHandler.sendMessage(msg);
			}
		});
	}

	private String getAppVersionName() {
		CustomLog.i(tag,"getAppVersionName");
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			CustomLog.e(tag, " AboutMeeting get version name Exception" + e);
		}
		return versionName;
	}

	public void switchToAppInstall() {
		CustomLog.e(tag, "switchToAppInstall");
		if (meetingVersionServiceSetting == null) {
			meetingVersionServiceSetting = context.getSharedPreferences(
					"meetingVersionServiceSetting", Context.MODE_PRIVATE);
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(
				Uri.parse("file://"
						+ meetingVersionServiceSetting.getString(MEETING_PATH,
								"")), "application/vnd.android.package-archive");
		context.startActivity(intent);
		KeyEventWrite.write(KeyEventConfig.INSTALL_APP_APK + "_ok" + "_"
				+ "Mobile" + "_" + "跳转到系统安装应用");
	}

	public void resetStateWhenForcedException() {
		CustomLog.d(tag, "resetStateWhenForcedException "
				+ isForcedDialogShowing);
		if (isForcedDialogShowing) {
			state = INIT_MEETING_VERSION_MANAGER;
			isForcedDialogShowing = false;
			isHasSetSize = false;
		}
	}

	public void autoDismiss() {  //强制升级页面点击强制升级按钮的时候调用
		CustomLog.d(tag, "come into autoDismiss");
		//发送广播到强制升级页面
		Intent intent = new Intent();
		intent.setAction("cn.redcdn.meeting.changeui");
		intent.putExtra("ui", "DOWNLOAD");
		context.sendBroadcast(intent);
		if (meetingUpdateDir != null) {
			File path1 = new File(meetingUpdateDir);
			if (!path1.exists()) {
				if (!path1.mkdirs()) {
					CustomLog.e(tag, "file.mkdirs is false so do nothing");
					return;
				}
			}
			path = meetingUpdateDir + File.separator + meetingName + "_"
					+ appInstallVersion + ".apk";
			if (update(updatePlugin, appInstallPackage, path, packetName) < 0) {
				Message msg = new Message();
				msg.what = MEETING_CHANGE_UI;
				Bundle bundle = new Bundle();
				bundle.putString("msg", context.getString(R.string.theUpgradepackageFailed));
				msg.setData(bundle);
				mainThreadHandler.sendMessage(msg);
			}
		} else {
			Message msg = new Message();
			msg.what = MEETING_CHANGE_UI;
			Bundle bundle = new Bundle();
			bundle.putString("msg", context.getString(R.string.sdkNoSuccess));
			msg.setData(bundle);
			mainThreadHandler.sendMessage(msg);
		}
	}
}
