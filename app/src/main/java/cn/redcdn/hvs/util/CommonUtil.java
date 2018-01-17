package cn.redcdn.hvs.util;

import android.Manifest;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.common.SideBar;
import cn.redcdn.hvs.im.util.permissions.AudioPermissionCheckUtils;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.log.CustomLog;
import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cn.redcdn.commonutil.NetConnectHelper.NETWORKTYPE_2G;
import static cn.redcdn.commonutil.NetConnectHelper.NETWORKTYPE_3G;
import static cn.redcdn.commonutil.NetConnectHelper.NETWORKTYPE_INVALID;
import static cn.redcdn.commonutil.NetConnectHelper.NETWORKTYPE_WAP;
import static cn.redcdn.commonutil.NetConnectHelper.NETWORKTYPE_WIFI;
import static cn.redcdn.hvs.meeting.util.CommonUtil.getImageRotationFromUrl;

public class CommonUtil {
    private static final String TAG = CommonUtil.class.getSimpleName();

    private static SideBar sideBar;
    private static Point deviceSize = null;
    private static TextView mTextView;


    /**
     * 移除子View
     */
    public static void removeSelfFromParent(View child) {
        if (child != null) {
            ViewParent parent = child.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(child);//移除子View
            }
        }
    }


    /**
     * 根据性别男女返回默认头像id
     */
    // public static int getHeadIdBySex(String sex) {
    //     int headId = R.drawable.contact_head_default_man;// 默认头像为男
    //     if (AndroidUtil.getString(R.string.woman).equals(sex)
    //         || String.valueOf(NubeFriendColumn.SEX_FEMALE).equals(sex)) {
    //         headId = R.drawable.contact_head_default_woman;// 头像改为女
    //     }
    //     return headId;
    // }
    public static Drawable getDrawable(int id) {
        return MedicalApplication.shareInstance().getResources().getDrawable(id);
    }


    public static String getString(int id) {
        return MedicalApplication.shareInstance().getResources().getString(id);
    }


    public static String[] getStringArray(int id) {
        return MedicalApplication.shareInstance().getResources().getStringArray(id);
    }


    public static int getColor(int id) {
        return MedicalApplication.shareInstance().getResources().getColor(id);
    }


    /**
     * @Description: 防止按钮快速点击导致多次处理
     * @return
     */

    private static long lastClickTime;


    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 200) {
            Log.d("TAG", "快速点击");
            return true;
        }
        lastClickTime = time;
        return false;
    }


    public static Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);

        View v = inflater.inflate(
            MResource.getIdByName(context, MResource.LAYOUT, "jmeetingsdk_loading_dialog"), null);

        ((RelativeLayout) v).setGravity(Gravity.CENTER);

        RelativeLayout layout = (RelativeLayout) v.findViewById(
            MResource.getIdByName(context, MResource.ID, "dialog_view"));

        layout.setGravity(Gravity.CENTER);

        ImageView spaceshipImage = (ImageView) v.findViewById(
            MResource.getIdByName(context, MResource.ID, "img"));

        TextView tipTextView = (TextView) v.findViewById(
            MResource.getIdByName(context, MResource.ID, "tipTextView"));

        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,

            MResource.getIdByName(context, MResource.ANIM, "jmeetingsdk_loading_animation"));

        spaceshipImage.startAnimation(hyperspaceJumpAnimation);

        if (msg == null || msg.equals("")) {

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(

                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.CENTER_IN_PARENT);

            spaceshipImage.setLayoutParams(params);

        }

        tipTextView.setText(msg);

        Dialog loadingDialog = new Dialog(context,
            MResource.getIdByName(context, MResource.STYLE, "jmetingsdk_loading_dialog"));

        loadingDialog.setCanceledOnTouchOutside(false);

        loadingDialog.setCancelable(false);

        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(

            LinearLayout.LayoutParams.MATCH_PARENT,

            LinearLayout.LayoutParams.MATCH_PARENT));

        return loadingDialog;
    }


    public static Dialog createLoadingDialog(Context context, String msg,

                                             DialogInterface.OnCancelListener listener) {

        LayoutInflater inflater = LayoutInflater.from(context);

        View v = inflater.inflate(
            MResource.getIdByName(context, MResource.LAYOUT, "jmeetingsdk_loading_dialog"), null);

        ((RelativeLayout) v).setGravity(Gravity.CENTER);

        RelativeLayout layout = (RelativeLayout) v.findViewById(
            MResource.getIdByName(context, MResource.ID, "dialog_view"));

        layout.setGravity(Gravity.CENTER);

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(

            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params1.addRule(RelativeLayout.CENTER_IN_PARENT);

        layout.setLayoutParams(params1);

        ImageView spaceshipImage = (ImageView) v.findViewById(
            MResource.getIdByName(context, MResource.ID, "img"));

        TextView tipTextView = (TextView) v.findViewById(
            MResource.getIdByName(context, MResource.ID, "tipTextView"));

        mTextView = tipTextView;

        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,
            MResource.getIdByName(context, MResource.ANIM, "jmeetingsdk_loading_animation"));

        if (msg == null || msg.equals("")) {

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(

                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.CENTER_IN_PARENT);

            spaceshipImage.setLayoutParams(params);

        }

        spaceshipImage.startAnimation(hyperspaceJumpAnimation);

        tipTextView.setText(msg);

        Dialog loadingDialog = new Dialog(context,
            MResource.getIdByName(context, MResource.STYLE, "jmetingsdk_loading_dialog"));

        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(

            LinearLayout.LayoutParams.MATCH_PARENT,

            LinearLayout.LayoutParams.MATCH_PARENT));

        loadingDialog.setCanceledOnTouchOutside(false);

        if (listener != null) {

            loadingDialog.setOnCancelListener(listener);

        }
        return loadingDialog;
    }


    /***
     * MD5加码 生成32位md5码
     */

    public static String string2MD5(String inStr) {

        MessageDigest md5 = null;

        try {

            md5 = MessageDigest.getInstance("MD5");

        } catch (Exception e) {

            System.out.println(e.toString());

            e.printStackTrace();

            return "";

        }

        char[] charArray = inStr.toCharArray();

        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)

        {
            byteArray[i] = (byte) charArray[i];
        }

        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {

            int val = ((int) md5Bytes[i]) & 0xff;

            if (val < 16)

            {
                hexValue.append("0");
            }

            hexValue.append(Integer.toHexString(val));

        }

        return hexValue.toString();

    }


    public static String getUUID() {

        String s = UUID.randomUUID().toString();

        // 去掉“-”符号

        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)

            + s.substring(19, 23) + s.substring(24);
    }


    /**
     * 隐藏界面输入软键盘
     */
    public static void hideSoftInputFromWindow(final Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {

            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }


    /**
     * >>>> ORIGINAL //depot/005_Medical/android/src/ButelMedical/app/src/main/java/cn/redcdn/hvs/util/CommonUtil.java#5
     * ==== THEIRS //depot/005_Medical/android/src/ButelMedical/app/src/main/java/cn/redcdn/hvs/util/CommonUtil.java#14
     * 显示界面输入键盘
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
                        CustomLog.d("CommonUtil", "showSoftInput");
                        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                    }
                }
            }
        });
    }


    public static ArrayList<String> getDispList(String str) {
        CustomLog.i(TAG, "getDispList");
        CustomLog.d(TAG, "解析@功能字符串里面的特定的nube：" + str);
        ArrayList<String> result = new ArrayList<String>();
        String nameStr = "";
        int pos = str.indexOf(IMConstant.SPECIAL_CHAR);
        if (pos != -1 && str.indexOf("@") != -1) {
            String startStr = str.substring(0, pos + 1);
            nameStr = startStr.substring(startStr.lastIndexOf("@") + 1, pos);
            if (nameStr.length() == 8 && StringUtil.isNumeric(nameStr)) {
                result.add(nameStr);
            }
            String endStr = str.substring(pos + 1, str.length());
            while (endStr.indexOf(IMConstant.SPECIAL_CHAR) != -1
                && endStr.indexOf("@") != -1) {
                int position = endStr.indexOf(IMConstant.SPECIAL_CHAR);
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
    }


    /**
     * @author: zhaguitao
     * @Title: makeCusPhotoFileName
     * @Description: 自定义照片文件名
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


    private static boolean isFastMobileNetwork(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context

            .getSystemService(Context.TELEPHONY_SERVICE);

        switch (telephonyManager.getNetworkType()) {

            case TelephonyManager.NETWORK_TYPE_1xRTT:

                return false; // ~ 50-100 kbps

            case TelephonyManager.NETWORK_TYPE_CDMA:

                return false; // ~ 14-64 kbps

            case TelephonyManager.NETWORK_TYPE_EDGE:

                return false; // ~ 50-100 kbps

            case TelephonyManager.NETWORK_TYPE_EVDO_0:

                return true; // ~ 400-1000 kbps

            case TelephonyManager.NETWORK_TYPE_EVDO_A:

                return true; // ~ 600-1400 kbps

            case TelephonyManager.NETWORK_TYPE_GPRS:

                return false; // ~ 100 kbps

            case TelephonyManager.NETWORK_TYPE_HSDPA:

                return true; // ~ 2-14 Mbps

            case TelephonyManager.NETWORK_TYPE_HSPA:

                return true; // ~ 700-1700 kbps

            case TelephonyManager.NETWORK_TYPE_HSUPA:

                return true; // ~ 1-23 Mbps

            case TelephonyManager.NETWORK_TYPE_UMTS:

                return true; // ~ 400-7000 kbps

            case TelephonyManager.NETWORK_TYPE_EHRPD:

                return true; // ~ 1-2 Mbps

            case TelephonyManager.NETWORK_TYPE_EVDO_B:

                return true; // ~ 5 Mbps

            case TelephonyManager.NETWORK_TYPE_HSPAP:

                return true; // ~ 10-20 Mbps

            case TelephonyManager.NETWORK_TYPE_IDEN:

                return false; // ~25 kbps

            case TelephonyManager.NETWORK_TYPE_LTE:

                return true; // ~ 10+ Mbps

            case TelephonyManager.NETWORK_TYPE_UNKNOWN:

                return false;

            default:

                return false;

        }

    }


    /**
     * 判断是否需要弹出Web页面进行网络登录。
     */
    public static boolean isViewWebPage(String responseBody) {
        CustomLog.d("CommonUtil", "isViewWebPage responseBody:" + responseBody);
        boolean result = false;
        String htmlTag = "<html>";
        if (responseBody.toLowerCase().contains(htmlTag)) {
            result = true;
        }
        return result;
    }


    public static void showToast(String alertMsg) {
        Toast.makeText(MedicalApplication.getContext(), alertMsg,
            Toast.LENGTH_SHORT).show();
    }


    public static void showToast(int alertId, Context context) {
        Toast.makeText(context, context.getString(alertId), Toast.LENGTH_SHORT)
            .show();
    }


    public static boolean selfPermissionGranted(Context context, String permission) {
        CustomLog.i(TAG,"selfPermissionGranted()");
        // For Android < Android M, self permissions are always granted.
        boolean result = true;
        int targetSdkVersion = Build.VERSION_CODES.BASE;

        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //当前运行环境为 6.0（API 23）及以上
            if (targetSdkVersion >= Build.VERSION_CODES.M) { //目标运行环境为6.0及以上，则使用新api查询权限
                result = context.checkSelfPermission(permission)
                    == PackageManager.PERMISSION_GRANTED;
            } else {                                         //目标运行环境为6.0以下，使用老api查询权限
                result = PermissionChecker.checkSelfPermission(context, permission)
                    == PermissionChecker.PERMISSION_GRANTED;
            }
        } else { //当前运行环境为 6.0 以下，没有办法查询到当前的授权情况
            if (permission.equalsIgnoreCase(
                android.Manifest.permission.CAMERA)) { //对于验证camera权限，尝试打开camera
                Camera camera;
                try {
                    camera = android.hardware.Camera.open(0);

                    camera.getParameters();
                    camera.release();
                    result = true;
                } catch (Exception ex) {
                    result = false;
                }
                CustomLog.i(TAG, "< 23 hasCameraPermission = " + result);
            } else if (permission.equalsIgnoreCase(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    result = true;

                } else {
                    result = false;
                }
            } else if (permission.equalsIgnoreCase(
                android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    result = true;

                } else {
                    result = false;
                }
            }else if (permission.equalsIgnoreCase(
                Manifest.permission.RECORD_AUDIO)) {
                result = AudioPermissionCheckUtils.checkAudioPermission(context);
                CustomLog.i(TAG, "< 23 hasAudioPermission = " + result);

            }

        }

        return result;
    }


    /**
     * 检查当前设备的系统版本是否大于 23
     *
     * @returnr
     */
    public static boolean isRuntimeAboveMarshmallow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //当前运行环境为 6.0（API 23）及以上
            return true;
        } else {
            return false;
        }
    }


    public static boolean hasRecordPermission(Context context, String permissionName) {
        CustomLog.i(TAG, "hasRecordPermission()");

        int permission = PermissionChecker.checkSelfPermission(context, permissionName);

        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * @Title: simpleFormatMoPhone
     * @Description: 简单格式化手机号码
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

        // CustomLog.d("CommonUtil", "简单格式化手机号码:" + oldPhone + "---->" + phone);

        return phone;

    }


    /**
     * 获取网络状态: wifi,wap,2g,3g.
     *
     * @param appcontext 上下文
     * @return int 网络状态
     * <p>
     * <p>
     * <p>
     * -1: 没有网络；
     * <p>
     * <p>
     * <p>
     * 1：wifi网络；
     * <p>
     * <p>
     * <p>
     * 2: 2G网络；
     * <p>
     * <p>
     * <p>
     * 3: 3G及以上网络；
     * <p>
     * <p>
     * <p>
     * 4：wap网络
     */

    public static int getNetWorkType(Context appcontext) {

        ConnectivityManager manager = (ConnectivityManager) appcontext

            .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        int mNetWorkType = NETWORKTYPE_INVALID;

        if (networkInfo != null && networkInfo.isConnected()) {

            String type = networkInfo.getTypeName();

            if (type.equalsIgnoreCase("WIFI")) {

                mNetWorkType = NETWORKTYPE_WIFI;

            } else if (type.equalsIgnoreCase("MOBILE")) {

                String proxyHost = android.net.Proxy.getDefaultHost();

                mNetWorkType = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(appcontext)
                                                               ? NETWORKTYPE_3G

                                                               : NETWORKTYPE_2G)

                                                            : NETWORKTYPE_WAP;

            }

        }

        return mNetWorkType;

    }


    /**
     * 根据当前选中的项获取其第一次出现该项首字母的位置
     *
     * @param position 当前选中的位置
     * @param datas 数据源
     */
    public static int getPositionForSection(int position, List<? extends SideBase> datas) {
        // 当前选中的项
        SideBase sideBase = datas.get(position);
        for (int i = 0; i < datas.size(); i++) {
            String firstStr = datas.get(i).getLetterName().toUpperCase();
            // 返回第一次出现该项首字母的位置
            if (firstStr.equals(sideBase.getLetterName())) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 获取所选中的索引在列表中的位置
     */
    public static int getLetterPosition(List<? extends SideBase> list, String letter) {
        int position = -1;

        if (list != null && !list.isEmpty() && !"".equals(letter)) {
            for (int i = 0; i < list.size(); i++) {
                SideBase bean = list.get(i);
                if (bean.getLetterName().equals(letter)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }


    /**
     * 筛选出数据源中所包含的全部索引值
     */
    public static String[] getLetters(List<? extends SideBase> list) {
        List<String> letters = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (!letters.contains(list.get(i).getLetterName())) {
                    letters.add(list.get(i).getLetterName());
                }
            }
        }
        return (String[]) letters.toArray(new String[letters.size()]);
    }


    public static Point getDeviceSize(Context context) {
        if (deviceSize == null) {
            deviceSize = new Point(0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                ((WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getSize(deviceSize);
            } else {
                Display display = ((WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
                deviceSize.x = display.getWidth();
                deviceSize.y = display.getHeight();
                display = null;
            }
        }
        return deviceSize;
    }


    /**
     * @Title: getImageRotationByPath
     * @Description: 根据图片路径获得其旋转角度
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

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return rotation;
    }


    //返回值 -1：没有网络  1：WIFI网络2：wap网络3：net网络
    public static int GetNetype(Context context) {
        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(
            Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
                netType = 3;
            } else {
                netType = 2;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }


    public static void setLoadingText(String msg) {
        if (mTextView != null) {
            mTextView.setText(msg);
        }

    }


    public static void removeTextView() {
        mTextView = null;
    }


    /***
     * 在移动网络环境，非Wifi网络，每次触发视频、语音通话及视频分享操作，需提醒用户，告知操作将产生的结果
     *
     * @param activity
     * @param btnOkLister
     *            按确定按钮执行的操作
     */
    public static void alertDataConsumeDialog(final Activity activity,
                                              final CommonDialog.BtnClickedListener btnOkLister, final CommonDialog.BtnClickedListener btnCancleLister) {
        CustomLog.i(TAG, "alertDataConsumeDialog()");

        CommonDialog dialog = new CommonDialog(activity, activity.getLocalClassName(), 301);
        dialog.setMessage(R.string.down_load_hint);
        dialog.setCancleButton(new CommonDialog.BtnClickedListener() {
            @Override
            public void onBtnClicked() {
                if (btnCancleLister != null) {
                    btnCancleLister.onBtnClicked();
                }
                CustomLog.d(TAG, "非Wifi网络下，流量使用确认对话框 中，点击‘取消");
            }
        }, R.string.btn_cancle);

        dialog.setPositiveButton(new CommonDialog.BtnClickedListener() {
            @Override
            public void onBtnClicked() {
                if (btnOkLister != null) {
                    btnOkLister.onBtnClicked();
                }
                CustomLog.d(TAG, "非Wifi网络下，流量使用确认对话框 中，点击‘继续");
            }
        }, R.string.btn_ok);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (btnCancleLister != null) {
                    btnCancleLister.onBtnClicked();
                }
                CustomLog.d(TAG, "非Wifi网络下，流量使用确认对话框 中，点击‘back返回’");
            }
        });
        dialog.showDialog();
    }

}
