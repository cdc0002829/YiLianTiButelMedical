package cn.redcdn.hvs.im.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.GONE;
import static com.butel.connectevent.utils.CommonUtil.getImageRotationFromUrl;

/**
 * Desc
 * Created by wangkai on 2017/2/25.
 */

public class IMCommonUtil {

    private static final String TAG = "IMCommonUtil";
    public static final String KEY_BROADCAST_INTENT_DATA = "KEY_BROADCAST_INTENT_DATA";
    private static Point deviceSize = null;


    /**
     * 根据性别男女返回默认头像id
     *
     */
    public static int getHeadIdBySex(String sex) {
        int headId = R.drawable.head_default;// 默认头像为男
        if (CommonUtil.getString(R.string.woman).equals(sex)
                || String.valueOf(NubeFriendColumn.SEX_FEMALE).equals(sex)) {
            headId = R.drawable.head_default;// 头像改为女
        }
        return headId;
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


        density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
        xdpi = dm.xdpi;
        ydpi = dm.ydpi;

        CustomLog.d("commonutil" + "  DisplayMetrics", "xdpi=" + xdpi + "; ydpi=" + ydpi);
        CustomLog.d("commonutil" + "  DisplayMetrics", "density=" + density
                + "; densityDPI=" + densityDPI);

        screen_w = dm.widthPixels;
        screen_h = dm.heightPixels;

        // Log.e(TAG + "  DisplayMetrics(222)", "screenWidthDip=" +
        // screenWidthDip + "; screenHeightDip=" + screenHeightDip);
        //
        // screen_w = (int)(dm.widthPixels * density + 0.5f); // 屏幕宽（px，如：480px）
        // screen_h = (int)(dm.heightPixels * density + 0.5f); //
        // 屏幕高（px，如：800px）

        CustomLog.d("commonutil" + "  DisplayMetrics(222)", "screenWidth=" + screen_w
                + "; screenHeight=" + screen_h);
    }

    public static int getScreenWidth(Context mContext) {
        // if(screen_w == 0)
        // {
        initScreenInfo(mContext);
        // }
        return screen_w;
    }

    /**
     * @author: chuwx
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
        // LogUtil.d("简单格式化手机号码:" + oldPhone + "---->" + phone);
        return phone;
    }

    /**
     *
     * Description:过滤非法字符
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

    public static ArrayList<String> getList(String str) {
        CustomLog.d(TAG,"解析@功能字符串里面的特定的name：" + str);
        ArrayList<String> result = new ArrayList<String>();
        String nameStr = "";
        int pos = str.indexOf(IMConstant.SPECIAL_CHAR);
        if (pos != -1 && str.indexOf("@") != -1) {
            String startStr = str.substring(0, pos + 1);
            if (startStr.indexOf("@") != -1) {
                nameStr = startStr
                        .substring(startStr.lastIndexOf("@"), pos + 1);
                result.add(nameStr);
            }
            String endStr = str.substring(pos + 1, str.length());
            while (endStr.indexOf(IMConstant.SPECIAL_CHAR) != -1
                    && endStr.indexOf("@") != -1) {
                int position = endStr.indexOf(IMConstant.SPECIAL_CHAR);
                startStr = endStr.substring(0, position + 1);
                if (startStr.indexOf("@") != -1) {
                    nameStr = startStr.substring(startStr.lastIndexOf("@"),
                            position + 1);
                    result.add(nameStr);
                }
                endStr = endStr.substring(position + 1, endStr.length());
            }
        }
        return result;
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
            CustomLog.e(TAG, "getImageRotationByPath"+ "Exception" + e.toString());
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
     * @Title: getDeviceSize
     * @Description: 获取手机屏幕宽高
     * @param context
     * @return
     * @date: 2014-3-13 上午9:45:55
     */
    @SuppressLint("NewApi")
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

    public static void scanFileAsync(Context ctx, String filePath) {
        CustomLog.d(TAG,"filePath:" + filePath);
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        ctx.sendBroadcast(scanIntent);
    }

    public static void setKeyValue(String txt, String gid) {
        if (!TextUtils.isEmpty(gid)) {
            CustomLog.d(TAG,"setKeyValue KEY_CHAT_REMIND_LIST:" + txt + gid);
            ArrayList<String> resultList = new ArrayList<String>();
            resultList = getDispList(txt);
            if (resultList.toString().contains(
                    AccountManager.getInstance(MedicalApplication.getContext()).getNube())) {
                String value = MedicalApplication.getPreference().getKeyValue(
                        PrefType.KEY_CHAT_REMIND_LIST, "");
                value = value + gid + ";";
                MedicalApplication.getPreference().setKeyValue(
                        PrefType.KEY_CHAT_REMIND_LIST, value);
            }
        }
    }

    public static void setRemindKeyValue(String txt, String gid) {
        if (!TextUtils.isEmpty(gid)) {
            CustomLog.d(TAG,"setRemindKeyValue KEY_CHAT_REMIND_LIST:" + txt + gid);
            String value = MedicalApplication.getPreference().getKeyValue(
                    PrefType.KEY_CHAT_REMIND_LIST, "");
            value = value + gid + ";";
            MedicalApplication.getPreference().setKeyValue(
                    PrefType.KEY_CHAT_REMIND_LIST, value);
        }
    }


    public static ArrayList<String> getDispList(String str) {
        CustomLog.d(TAG,"解析@功能字符串里面的特定的nube：" + str);
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
     *
     * Description: dp 转换 px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
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

    /**
     * @Title: isNetworkAvailable
     * @Description: 网络状态判断
     * @param context
     * @return
     * @return boolean
     * @throws
     */
    public static boolean isNetworkAvailable(Context context) {
        if(NetConnectHelper.NETWORKTYPE_INVALID == NetConnectHelper.getNetWorkType(MedicalApplication.getContext())){
            return false;
        }
        return true;

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
     * 拨号盘和消息列表界面添加好友，要插入默认消息
     *
     * @throws Exception
     */
    public static void addFriendTxt(final Activity activity, String str,
                                    ContactFriendBean currentInfo) {
        NoticesDao noticeDao = new NoticesDao(activity);
        ThreadsDao threadDao = new ThreadsDao(activity);
        String convstId;
        ThreadsBean th = threadDao.getThreadByRecipentIds(currentInfo
            .getNubeNumber());
        // 判断要插入默认消息的场景
        if (th == null) {
            convstId = "";
            noticeDao.createAddFriendTxt("",
                currentInfo.getNubeNumber(),"",
                null,
                "",
                FileTaskManager.NOTICE_TYPE_DESCRIPTION,
                MedicalApplication.getContext().getString(
                    R.string.add_friend_text), convstId, null, "");
        } else if (th != null) {
            Cursor cusor = null;
            convstId = th.getId();
            try {
                cusor = noticeDao.queryAllNotices(th.getId());
                if (cusor == null || cusor.getCount() == 0) {
                    noticeDao.createAddFriendTxt("",
                        currentInfo.getNubeNumber(),"",
                        null,
                        "",
                        FileTaskManager.NOTICE_TYPE_DESCRIPTION,
                        MedicalApplication.getContext().getString(
                            R.string.add_friend_text), convstId, null,
                        "");
                }
            } catch (Exception e) {
                CustomLog.e("addFriendTxt :", String.valueOf(e));
            } finally {
                if (cusor != null) {
                    cusor.close();
                    cusor = null;
                }
            }
        }
    }

    public static void alertPermissionDialog(final Context mContext,
                                             final CommonDialog.BtnClickedListener btnOkLister,
                                             final CommonDialog.BtnClickedListener btnCancleLister, int id) {
        CustomLog.d(TAG,"show 非Wifi网络下，流量使用确认对话框");
        CommonDialog dialog = new CommonDialog(mContext,
                ((Activity) mContext).getLocalClassName(), 301);
        dialog.setMessage(id);
        dialog.setCancelable(false);
        dialog.setPositiveButton(btnOkLister, "确定");
        dialog.showDialog();
    }


    /**
     * @Description: 调用系统播放器，播放视频文件
     * @param context
     * @param videoPath
     */
    public static void playVideo(Context context, String videoPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String strend = "";
        if (videoPath.toLowerCase().endsWith(".mp4")) {
            strend = "mp4";
        } else if (videoPath.toLowerCase().endsWith(".mpg4")) {
            strend = "mp4";
        } else if (videoPath.toLowerCase().endsWith(".3gp")) {
            strend = "3gpp";
        } else if (videoPath.toLowerCase().endsWith(".mov")) {
            strend = "quicktime";
        } else if (videoPath.toLowerCase().endsWith(".wmv")) {
            strend = "wmv";
        } else if (videoPath.toLowerCase().endsWith(".avi")) {
            strend = "x-msvideo";
        } else if (videoPath.toLowerCase().endsWith(".mpe")) {
            strend = "mpeg";
        } else if (videoPath.toLowerCase().endsWith(".mpeg")) {
            strend = "mpeg";
        } else if (videoPath.toLowerCase().endsWith(".mpg")) {
            strend = "mpeg";
        }

        intent.setDataAndType(Uri.fromFile(new File(videoPath)), "video/"
            + strend);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            CustomLog.e("playVideo" + "ActivityNotFoundException", String.valueOf(e));
            CustomToast.show(context, R.string.play_media_error, 1);
        }
    }


    /**
     * 切换语音播放模式自定义 View
     * @param mContext
     * @param text 文本
     * @param background 图片
     */
    public static void makeModeChangeToast(Context mContext,CharSequence text,int background) {
        final RelativeLayout playModeViewGroup
            = (RelativeLayout) ((Activity) mContext)
            .findViewById(R.id.container_toast);
        TextView textView = (TextView) playModeViewGroup.findViewById(R.id.slogan);
        textView.setText(text);
        ImageView imageView = (ImageView)playModeViewGroup.findViewById(R.id.avatar);
        imageView.setImageResource(background);
        playModeViewGroup.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                playModeViewGroup.setVisibility(GONE);
            }
        }, 2000);

    }

    public static int getDimensionX(Context context, int px) {
        switch (px){
            case 156:
                return (int) context.getResources().getDimension(R.dimen.x156);
            case 157:
                return (int) context.getResources().getDimension(R.dimen.x157);
            case 158:
                return (int) context.getResources().getDimension(R.dimen.x158);
            case 159:
                return (int) context.getResources().getDimension(R.dimen.x159);
            case 160:
                return (int) context.getResources().getDimension(R.dimen.x160);
            case 161:
                return (int) context.getResources().getDimension(R.dimen.x161);
            case 162:
                return (int) context.getResources().getDimension(R.dimen.x162);
            case 163:
                return (int) context.getResources().getDimension(R.dimen.x163);
            case 164:
                return (int) context.getResources().getDimension(R.dimen.x164);
            case 165:
                return (int) context.getResources().getDimension(R.dimen.x165);
            case 166:
                return (int) context.getResources().getDimension(R.dimen.x166);
            case 167:
                return (int) context.getResources().getDimension(R.dimen.x167);
            case 168:
                return (int) context.getResources().getDimension(R.dimen.x168);
            case 169:
                return (int) context.getResources().getDimension(R.dimen.x169);
            case 170:
                return (int) context.getResources().getDimension(R.dimen.x170);
            case 171:
                return (int) context.getResources().getDimension(R.dimen.x171);
            case 172:
                return (int) context.getResources().getDimension(R.dimen.x172);
            case 173:
                return (int) context.getResources().getDimension(R.dimen.x173);
            case 174:
                return (int) context.getResources().getDimension(R.dimen.x174);
            case 175:
                return (int) context.getResources().getDimension(R.dimen.x175);
            case 176:
                return (int) context.getResources().getDimension(R.dimen.x176);
            case 177:
                return (int) context.getResources().getDimension(R.dimen.x177);
            case 178:
                return (int) context.getResources().getDimension(R.dimen.x178);
            case 179:
                return (int) context.getResources().getDimension(R.dimen.x179);
            case 180:
                return (int) context.getResources().getDimension(R.dimen.x180);
            case 181:
                return (int) context.getResources().getDimension(R.dimen.x181);
            case 182:
                return (int) context.getResources().getDimension(R.dimen.x182);
            case 183:
                return (int) context.getResources().getDimension(R.dimen.x183);
            case 184:
                return (int) context.getResources().getDimension(R.dimen.x184);
            case 185:
                return (int) context.getResources().getDimension(R.dimen.x185);
            case 186:
                return (int) context.getResources().getDimension(R.dimen.x186);
            case 187:
                return (int) context.getResources().getDimension(R.dimen.x187);
            case 188:
                return (int) context.getResources().getDimension(R.dimen.x188);
            case 189:
                return (int) context.getResources().getDimension(R.dimen.x189);
            case 190:
                return (int) context.getResources().getDimension(R.dimen.x190);
            case 191:
                return (int) context.getResources().getDimension(R.dimen.x191);
            case 192:
                return (int) context.getResources().getDimension(R.dimen.x192);
            case 193:
                return (int) context.getResources().getDimension(R.dimen.x193);
            case 194:
                return (int) context.getResources().getDimension(R.dimen.x194);
            case 195:
                return (int) context.getResources().getDimension(R.dimen.x195);
            case 196:
                return (int) context.getResources().getDimension(R.dimen.x196);
            case 197:
                return (int) context.getResources().getDimension(R.dimen.x197);
            case 198:
                return (int) context.getResources().getDimension(R.dimen.x198);
            case 199:
                return (int) context.getResources().getDimension(R.dimen.x199);
            case 200:
                return (int) context.getResources().getDimension(R.dimen.x200);
            case 201:
                return (int) context.getResources().getDimension(R.dimen.x201);
            case 202:
                return (int) context.getResources().getDimension(R.dimen.x202);
            case 203:
                return (int) context.getResources().getDimension(R.dimen.x203);
            case 204:
                return (int) context.getResources().getDimension(R.dimen.x204);
            case 205:
                return (int) context.getResources().getDimension(R.dimen.x205);
            case 206:
                return (int) context.getResources().getDimension(R.dimen.x206);
            case 207:
                return (int) context.getResources().getDimension(R.dimen.x207);
            case 208:
                return (int) context.getResources().getDimension(R.dimen.x208);
            case 209:
                return (int) context.getResources().getDimension(R.dimen.x209);
            case 210:
                return (int) context.getResources().getDimension(R.dimen.x210);
            case 211:
                return (int) context.getResources().getDimension(R.dimen.x211);
            case 212:
                return (int) context.getResources().getDimension(R.dimen.x212);
            case 213:
                return (int) context.getResources().getDimension(R.dimen.x213);
            case 214:
                return (int) context.getResources().getDimension(R.dimen.x214);
            case 215:
                return (int) context.getResources().getDimension(R.dimen.x215);
            case 216:
                return (int) context.getResources().getDimension(R.dimen.x216);
            case 217:
                return (int) context.getResources().getDimension(R.dimen.x217);
            case 218:
                return (int) context.getResources().getDimension(R.dimen.x218);
            case 219:
                return (int) context.getResources().getDimension(R.dimen.x219);
            case 220:
                return (int) context.getResources().getDimension(R.dimen.x220);
            case 221:
                return (int) context.getResources().getDimension(R.dimen.x221);
            case 222:
                return (int) context.getResources().getDimension(R.dimen.x222);
            case 223:
                return (int) context.getResources().getDimension(R.dimen.x223);
            case 224:
                return (int) context.getResources().getDimension(R.dimen.x224);
            case 225:
                return (int) context.getResources().getDimension(R.dimen.x225);
            case 226:
                return (int) context.getResources().getDimension(R.dimen.x226);
            case 227:
                return (int) context.getResources().getDimension(R.dimen.x227);
            case 228:
                return (int) context.getResources().getDimension(R.dimen.x228);
            case 229:
                return (int) context.getResources().getDimension(R.dimen.x229);
            case 230:
                return (int) context.getResources().getDimension(R.dimen.x230);
            case 231:
                return (int) context.getResources().getDimension(R.dimen.x231);
            case 232:
                return (int) context.getResources().getDimension(R.dimen.x232);
            case 233:
                return (int) context.getResources().getDimension(R.dimen.x233);
            case 234:
                return (int) context.getResources().getDimension(R.dimen.x234);
            case 235:
                return (int) context.getResources().getDimension(R.dimen.x235);
            case 236:
                return (int) context.getResources().getDimension(R.dimen.x236);
            case 237:
                return (int) context.getResources().getDimension(R.dimen.x237);
            case 238:
                return (int) context.getResources().getDimension(R.dimen.x238);
            case 239:
                return (int) context.getResources().getDimension(R.dimen.x239);
            case 240:
                return (int) context.getResources().getDimension(R.dimen.x240);
            case 241:
                return (int) context.getResources().getDimension(R.dimen.x241);
            case 242:
                return (int) context.getResources().getDimension(R.dimen.x242);
            case 243:
                return (int) context.getResources().getDimension(R.dimen.x243);
            case 244:
                return (int) context.getResources().getDimension(R.dimen.x244);
            case 245:
                return (int) context.getResources().getDimension(R.dimen.x245);
            case 246:
                return (int) context.getResources().getDimension(R.dimen.x246);
            case 247:
                return (int) context.getResources().getDimension(R.dimen.x247);
            case 248:
                return (int) context.getResources().getDimension(R.dimen.x248);
            case 249:
                return (int) context.getResources().getDimension(R.dimen.x249);
            case 250:
                return (int) context.getResources().getDimension(R.dimen.x250);
            case 251:
                return (int) context.getResources().getDimension(R.dimen.x251);
            case 252:
                return (int) context.getResources().getDimension(R.dimen.x252);
            case 253:
                return (int) context.getResources().getDimension(R.dimen.x253);
            case 254:
                return (int) context.getResources().getDimension(R.dimen.x254);
            case 255:
                return (int) context.getResources().getDimension(R.dimen.x255);
            case 256:
                return (int) context.getResources().getDimension(R.dimen.x256);
            case 257:
                return (int) context.getResources().getDimension(R.dimen.x257);
            case 258:
                return (int) context.getResources().getDimension(R.dimen.x258);
            case 259:
                return (int) context.getResources().getDimension(R.dimen.x259);
            case 260:
                return (int) context.getResources().getDimension(R.dimen.x260);
            case 261:
                return (int) context.getResources().getDimension(R.dimen.x261);
            case 262:
                return (int) context.getResources().getDimension(R.dimen.x262);
            case 263:
                return (int) context.getResources().getDimension(R.dimen.x263);
            case 264:
                return (int) context.getResources().getDimension(R.dimen.x264);
            case 265:
                return (int) context.getResources().getDimension(R.dimen.x265);
            case 266:
                return (int) context.getResources().getDimension(R.dimen.x266);
            case 267:
                return (int) context.getResources().getDimension(R.dimen.x267);
            case 268:
                return (int) context.getResources().getDimension(R.dimen.x268);
            case 269:
                return (int) context.getResources().getDimension(R.dimen.x269);
            case 270:
                return (int) context.getResources().getDimension(R.dimen.x270);
            case 271:
                return (int) context.getResources().getDimension(R.dimen.x271);
            case 272:
                return (int) context.getResources().getDimension(R.dimen.x272);
            case 273:
                return (int) context.getResources().getDimension(R.dimen.x273);
            case 274:
                return (int) context.getResources().getDimension(R.dimen.x274);
            case 275:
                return (int) context.getResources().getDimension(R.dimen.x275);
            case 276:
                return (int) context.getResources().getDimension(R.dimen.x276);
            case 277:
                return (int) context.getResources().getDimension(R.dimen.x277);
            case 278:
                return (int) context.getResources().getDimension(R.dimen.x278);
            case 279:
                return (int) context.getResources().getDimension(R.dimen.x279);
            case 280:
                return (int) context.getResources().getDimension(R.dimen.x280);
        }
        return (int) context.getResources().getDimension(R.dimen.x156);
    }

    public static int getDimensionY(Context context, int px) {
        switch (px){
            case 156:
                return (int) context.getResources().getDimension(R.dimen.y156);
            case 157:
                return (int) context.getResources().getDimension(R.dimen.y157);
            case 158:
                return (int) context.getResources().getDimension(R.dimen.y158);
            case 159:
                return (int) context.getResources().getDimension(R.dimen.y159);
            case 160:
                return (int) context.getResources().getDimension(R.dimen.y160);
            case 161:
                return (int) context.getResources().getDimension(R.dimen.y161);
            case 162:
                return (int) context.getResources().getDimension(R.dimen.y162);
            case 163:
                return (int) context.getResources().getDimension(R.dimen.y163);
            case 164:
                return (int) context.getResources().getDimension(R.dimen.y164);
            case 165:
                return (int) context.getResources().getDimension(R.dimen.y165);
            case 166:
                return (int) context.getResources().getDimension(R.dimen.y166);
            case 167:
                return (int) context.getResources().getDimension(R.dimen.y167);
            case 168:
                return (int) context.getResources().getDimension(R.dimen.y168);
            case 169:
                return (int) context.getResources().getDimension(R.dimen.y169);
            case 170:
                return (int) context.getResources().getDimension(R.dimen.y170);
            case 171:
                return (int) context.getResources().getDimension(R.dimen.y171);
            case 172:
                return (int) context.getResources().getDimension(R.dimen.y172);
            case 173:
                return (int) context.getResources().getDimension(R.dimen.y173);
            case 174:
                return (int) context.getResources().getDimension(R.dimen.y174);
            case 175:
                return (int) context.getResources().getDimension(R.dimen.y175);
            case 176:
                return (int) context.getResources().getDimension(R.dimen.y176);
            case 177:
                return (int) context.getResources().getDimension(R.dimen.y177);
            case 178:
                return (int) context.getResources().getDimension(R.dimen.y178);
            case 179:
                return (int) context.getResources().getDimension(R.dimen.y179);
            case 180:
                return (int) context.getResources().getDimension(R.dimen.y180);
            case 181:
                return (int) context.getResources().getDimension(R.dimen.y181);
            case 182:
                return (int) context.getResources().getDimension(R.dimen.y182);
            case 183:
                return (int) context.getResources().getDimension(R.dimen.y183);
            case 184:
                return (int) context.getResources().getDimension(R.dimen.y184);
            case 185:
                return (int) context.getResources().getDimension(R.dimen.y185);
            case 186:
                return (int) context.getResources().getDimension(R.dimen.y186);
            case 187:
                return (int) context.getResources().getDimension(R.dimen.y187);
            case 188:
                return (int) context.getResources().getDimension(R.dimen.y188);
            case 189:
                return (int) context.getResources().getDimension(R.dimen.y189);
            case 190:
                return (int) context.getResources().getDimension(R.dimen.y190);
            case 191:
                return (int) context.getResources().getDimension(R.dimen.y191);
            case 192:
                return (int) context.getResources().getDimension(R.dimen.y192);
            case 193:
                return (int) context.getResources().getDimension(R.dimen.y193);
            case 194:
                return (int) context.getResources().getDimension(R.dimen.y194);
            case 195:
                return (int) context.getResources().getDimension(R.dimen.y195);
            case 196:
                return (int) context.getResources().getDimension(R.dimen.y196);
            case 197:
                return (int) context.getResources().getDimension(R.dimen.y197);
            case 198:
                return (int) context.getResources().getDimension(R.dimen.y198);
            case 199:
                return (int) context.getResources().getDimension(R.dimen.y199);
            case 200:
                return (int) context.getResources().getDimension(R.dimen.y200);
            case 201:
                return (int) context.getResources().getDimension(R.dimen.y201);
            case 202:
                return (int) context.getResources().getDimension(R.dimen.y202);
            case 203:
                return (int) context.getResources().getDimension(R.dimen.y203);
            case 204:
                return (int) context.getResources().getDimension(R.dimen.y204);
            case 205:
                return (int) context.getResources().getDimension(R.dimen.y205);
            case 206:
                return (int) context.getResources().getDimension(R.dimen.y206);
            case 207:
                return (int) context.getResources().getDimension(R.dimen.y207);
            case 208:
                return (int) context.getResources().getDimension(R.dimen.y208);
            case 209:
                return (int) context.getResources().getDimension(R.dimen.y209);
            case 210:
                return (int) context.getResources().getDimension(R.dimen.y210);
            case 211:
                return (int) context.getResources().getDimension(R.dimen.y211);
            case 212:
                return (int) context.getResources().getDimension(R.dimen.y212);
            case 213:
                return (int) context.getResources().getDimension(R.dimen.y213);
            case 214:
                return (int) context.getResources().getDimension(R.dimen.y214);
            case 215:
                return (int) context.getResources().getDimension(R.dimen.y215);
            case 216:
                return (int) context.getResources().getDimension(R.dimen.y216);
            case 217:
                return (int) context.getResources().getDimension(R.dimen.y217);
            case 218:
                return (int) context.getResources().getDimension(R.dimen.y218);
            case 219:
                return (int) context.getResources().getDimension(R.dimen.y219);
            case 220:
                return (int) context.getResources().getDimension(R.dimen.y220);
            case 221:
                return (int) context.getResources().getDimension(R.dimen.y221);
            case 222:
                return (int) context.getResources().getDimension(R.dimen.y222);
            case 223:
                return (int) context.getResources().getDimension(R.dimen.y223);
            case 224:
                return (int) context.getResources().getDimension(R.dimen.y224);
            case 225:
                return (int) context.getResources().getDimension(R.dimen.y225);
            case 226:
                return (int) context.getResources().getDimension(R.dimen.y226);
            case 227:
                return (int) context.getResources().getDimension(R.dimen.y227);
            case 228:
                return (int) context.getResources().getDimension(R.dimen.y228);
            case 229:
                return (int) context.getResources().getDimension(R.dimen.y229);
            case 230:
                return (int) context.getResources().getDimension(R.dimen.y230);
            case 231:
                return (int) context.getResources().getDimension(R.dimen.y231);
            case 232:
                return (int) context.getResources().getDimension(R.dimen.y232);
            case 233:
                return (int) context.getResources().getDimension(R.dimen.y233);
            case 234:
                return (int) context.getResources().getDimension(R.dimen.y234);
            case 235:
                return (int) context.getResources().getDimension(R.dimen.y235);
            case 236:
                return (int) context.getResources().getDimension(R.dimen.y236);
            case 237:
                return (int) context.getResources().getDimension(R.dimen.y237);
            case 238:
                return (int) context.getResources().getDimension(R.dimen.y238);
            case 239:
                return (int) context.getResources().getDimension(R.dimen.y239);
            case 240:
                return (int) context.getResources().getDimension(R.dimen.y240);
            case 241:
                return (int) context.getResources().getDimension(R.dimen.y241);
            case 242:
                return (int) context.getResources().getDimension(R.dimen.y242);
            case 243:
                return (int) context.getResources().getDimension(R.dimen.y243);
            case 244:
                return (int) context.getResources().getDimension(R.dimen.y244);
            case 245:
                return (int) context.getResources().getDimension(R.dimen.y245);
            case 246:
                return (int) context.getResources().getDimension(R.dimen.y246);
            case 247:
                return (int) context.getResources().getDimension(R.dimen.y247);
            case 248:
                return (int) context.getResources().getDimension(R.dimen.y248);
            case 249:
                return (int) context.getResources().getDimension(R.dimen.y249);
            case 250:
                return (int) context.getResources().getDimension(R.dimen.y250);
            case 251:
                return (int) context.getResources().getDimension(R.dimen.y251);
            case 252:
                return (int) context.getResources().getDimension(R.dimen.y252);
            case 253:
                return (int) context.getResources().getDimension(R.dimen.y253);
            case 254:
                return (int) context.getResources().getDimension(R.dimen.y254);
            case 255:
                return (int) context.getResources().getDimension(R.dimen.y255);
            case 256:
                return (int) context.getResources().getDimension(R.dimen.y256);
            case 257:
                return (int) context.getResources().getDimension(R.dimen.y257);
            case 258:
                return (int) context.getResources().getDimension(R.dimen.y258);
            case 259:
                return (int) context.getResources().getDimension(R.dimen.y259);
            case 260:
                return (int) context.getResources().getDimension(R.dimen.y260);
            case 261:
                return (int) context.getResources().getDimension(R.dimen.y261);
            case 262:
                return (int) context.getResources().getDimension(R.dimen.y262);
            case 263:
                return (int) context.getResources().getDimension(R.dimen.y263);
            case 264:
                return (int) context.getResources().getDimension(R.dimen.y264);
            case 265:
                return (int) context.getResources().getDimension(R.dimen.y265);
            case 266:
                return (int) context.getResources().getDimension(R.dimen.y266);
            case 267:
                return (int) context.getResources().getDimension(R.dimen.y267);
            case 268:
                return (int) context.getResources().getDimension(R.dimen.y268);
            case 269:
                return (int) context.getResources().getDimension(R.dimen.y269);
            case 270:
                return (int) context.getResources().getDimension(R.dimen.y270);
            case 271:
                return (int) context.getResources().getDimension(R.dimen.y271);
            case 272:
                return (int) context.getResources().getDimension(R.dimen.y272);
            case 273:
                return (int) context.getResources().getDimension(R.dimen.y273);
            case 274:
                return (int) context.getResources().getDimension(R.dimen.y274);
            case 275:
                return (int) context.getResources().getDimension(R.dimen.y275);
            case 276:
                return (int) context.getResources().getDimension(R.dimen.y276);
            case 277:
                return (int) context.getResources().getDimension(R.dimen.y277);
            case 278:
                return (int) context.getResources().getDimension(R.dimen.y278);
            case 279:
                return (int) context.getResources().getDimension(R.dimen.y279);
            case 280:
                return (int) context.getResources().getDimension(R.dimen.y280);
        }
        return (int) context.getResources().getDimension(R.dimen.y156);
    }

}
