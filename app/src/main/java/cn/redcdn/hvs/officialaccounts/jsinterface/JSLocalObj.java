package cn.redcdn.hvs.officialaccounts.jsinterface;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import cn.redcdn.datacenter.collectcenter.AddCollectionItems;
import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.im.activity.ShareLocalActivity;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.util.EnterGroupUtil;
import cn.redcdn.hvs.im.view.CustomDialog1;
import cn.redcdn.hvs.im.view.Share2FriendsDialog;
import cn.redcdn.hvs.officialaccounts.DingYueActivity;
import cn.redcdn.hvs.officialaccounts.activity.OfficialMainActivity;
import cn.redcdn.hvs.officialaccounts.view.ShareFriendDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.JSSignUpHelper;
import cn.redcdn.log.CustomLog;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

import static cn.redcdn.hvs.util.CommonUtil.GetNetype;

/**
 * Created by KevinZhang on 2017/3/11.
 */

public abstract class JSLocalObj extends Activity {

    boolean done = false;
    private static final String TAG = JSLocalObj.class.getName();
    private static final String TAG_JS = "javascript";
    private Context mContext;
    private Handler mHandler;
    public static final String JS_INTERFACE_NAME = "jsInterFace"; //与前端页面约定的交互对象名称
    private static final int MSG_COLLECT_ARTICLE = 1;
    private static final int MSG_SHOW_OFFICE_PAGE = 2;
    private static final int MSG_CHOOSE_CONTENT = 3;
    private static final int MSG_SHOW_TOAST = 4;
    private static final int MSG_SHOW_PERSONAL_CARD = 5;
    private static final int MSG_SHOW_OFFICIAL_MAIN = 6;
    private static final int MSG_FORWARD_ARTICLE = 7;
    private static final int MSG_VIDEO_ANIMATION = 8;
    private static final int MSG_REGISTERED = 9;
    //微信邀请声明
    public static String APP_ID = "wx075e76791e3ec1a8"; //微信appid
    public static IWXAPI api;
    //qq分享使用
    private String AppId = "1105341562";
    private Tencent mTencent;
    private static final int MSG_LOAD_SUCCESS = 0;
    private Bitmap bitmap;
    private String currRequestId;
    private JSSignUpHelper jsSignUpHelper = null;
    EnterGroupUtil enterGroupUtil = null;


    public JSLocalObj(Context context) {
        this(context, null);
    }


    public JSLocalObj(Context context, WebView webView) {

        mContext = context;
        //注册到微信
        api = WXAPIFactory.createWXAPI(mContext, APP_ID, true);
        api.registerApp(APP_ID);
        mTencent = Tencent.createInstance(AppId, mContext);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_CHOOSE_CONTENT:
                        onChooseContent(msg.getData().getString("contentId"),
                            msg.getData().getString("contentName"), msg.arg1, msg.arg2);
                        break;
                    case MSG_SHOW_OFFICE_PAGE:
                        switchToDingYueActivity((String) msg.obj);
                        break;
                    case MSG_SHOW_TOAST:
                        CustomToast.show(mContext, msg.obj.toString(), Toast.LENGTH_LONG);
                        break;
                    case MSG_SHOW_PERSONAL_CARD:
                        switchToContactCardActivity((String) msg.obj);
                        break;
                    case MSG_SHOW_OFFICIAL_MAIN:
                        switchToOfficialMainActivity((String) msg.obj);
                        break;
                    case MSG_FORWARD_ARTICLE:
                        break;
                    case MSG_LOAD_SUCCESS:
                        bitmap = (Bitmap) msg.obj;
                        break;
                    case MSG_VIDEO_ANIMATION:
                        onExpandVideoWindow(msg.arg1);
                        break;
                    case MSG_REGISTERED:
                        int type = (int) msg.obj;
                        showUserDialog(type);
                        break;
                }
            }
        };

        if (webView != null) {
            jsSignUpHelper = new JSSignUpHelper();
            jsSignUpHelper.setWebview(webView);
        }
    }


    private void showUserDialog(int type) {
        AccountManager.TouristState touristState = AccountManager.getInstance(mContext)
            .getTouristState();
        if (type == 0 && touristState == AccountManager.TouristState.TOURIST_STATE) {
            CustomDialog1.Builder builder = new CustomDialog1.Builder(mContext);
            builder.setMessage(R.string.only_register_can_user_login_again);
            builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.login_or_register,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(mContext, LoginActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            builder.create().show();
        }
    }


    //调用dataCenter 收藏接口进行收藏，如果收藏失败进行错误日志打印，不做提示
    @JavascriptInterface
    public void collectArticle(String officeId, String officeName, String officeLogoUrl, String articleId, String articleTile, String previewUrl, String createTime, int articleType, String introduction) {
        CustomLog.d(TAG,
            "officeId:" + officeId + " | officeName: " + officeName + " |officeLogoUrl: " +
                officeLogoUrl + " |articleId: " + articleId + " |articleTile:" + articleTile +
                " |previewUrl" + previewUrl + " |createTime:" + createTime + " |articleType:" +
                articleType + "| introduction" + introduction);
        AccountManager.TouristState touristState = AccountManager.getInstance(mContext)
            .getTouristState();
        if (touristState == AccountManager.TouristState.TOURIST_STATE) {
            CustomDialog1.Builder builder = new CustomDialog1.Builder(mContext);
            builder.setMessage(R.string.only_register_can_user_login_again);
            builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.login_or_register,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(mContext, LoginActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            builder.create().show();
        } else {
            DataBodyInfo info = new DataBodyInfo();
            info.officialAccountId = officeId;
            info.name = officeName;
            info.ArticleId = articleId;
            info.title = articleTile;
            info.previewUrl = previewUrl;
            info.offAccLogoUrl = officeLogoUrl;
            info.createTime = createTime;
            info.isforwarded = 0;
            info.ForwarderNube = "";
            info.ForwarderName = "";
            info.ForwarderHeaderUrl = "";
            info.articleType = articleType;
            info.introduction = introduction;
            CustomLog.d(TAG, "传过来的类型" + info.articleType);

            AddCollectionItems items = new AddCollectionItems() {
                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                }


                @Override
                protected void onSuccess(JSONObject responseContent) {
                    super.onSuccess(responseContent);

                }
            };
            items.addFavoriteItems(AccountManager.getInstance(mContext).getNube(), getUUID(),
                AccountManager.getInstance(mContext).getToken(), 30, info);
        }
    }


    @JavascriptInterface
    public void collectArticle(String officeId, String officeName, String officeLogoUrl, String articleId, String articleTile, String previewUrl, String createTime, int articleType) {
        CustomLog.d(TAG,
            "officeId:" + officeId + " | officeName: " + officeName + " |officeLogoUrl: " +
                officeLogoUrl + " |articleId: " + articleId + " |articleTile:" + articleTile +
                " |previewUrl" + previewUrl + " |createTime:" + createTime + " |articleType:" +
                articleType);
        AccountManager.TouristState touristState = AccountManager.getInstance(mContext)
            .getTouristState();
        if (touristState == AccountManager.TouristState.TOURIST_STATE) {
            CustomDialog1.Builder builder = new CustomDialog1.Builder(mContext);
            builder.setMessage(R.string.only_register_can_user_login_again);
            builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.login_or_register,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(mContext, LoginActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            builder.create().show();
        } else {
            DataBodyInfo info = new DataBodyInfo();
            info.officialAccountId = officeId;
            info.name = officeName;
            info.ArticleId = articleId;
            info.title = articleTile;
            info.previewUrl = previewUrl;
            info.offAccLogoUrl = officeLogoUrl;
            info.createTime = createTime;
            info.isforwarded = 0;
            info.ForwarderNube = "";
            info.ForwarderName = "";
            info.ForwarderHeaderUrl = "";
            info.articleType = articleType;
            CustomLog.d(TAG, "传过来的类型" + info.articleType);

            AddCollectionItems items = new AddCollectionItems() {
                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                }


                @Override
                protected void onSuccess(JSONObject responseContent) {
                    super.onSuccess(responseContent);

                }
            };
            items.addFavoriteItems(AccountManager.getInstance(mContext).getNube(), getUUID(),
                AccountManager.getInstance(mContext).getToken(), 30, info);
        }
    }


    //切换到公众号名片页面
    @JavascriptInterface
    public void showOfficePage(String officeId) {
        CustomLog.d(TAG, "showOfficePage:" + officeId);
        Message msg = new Message();
        msg.what = MSG_SHOW_OFFICE_PAGE;
        msg.obj = officeId;
        mHandler.sendMessage(msg);

    }


    @JavascriptInterface
    public void chooseContent(String contentId, String contentName, int type) {
        CustomLog.d(TAG,
            "chooseContent:" + contentId + " | contentName: " + contentName + " |type: " + type);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("contentId", contentId);
        data.putString("contentName", contentName);
        msg.what = MSG_CHOOSE_CONTENT;
        msg.arg1 = type;
        msg.setData(data);
        mHandler.sendMessage(msg);
    }


    @JavascriptInterface
    public void chooseContent(String contentId, String contentName, int type, int permissions) {
        CustomLog.d(TAG,
            "chooseContent:" + contentId + " | contentName: " + contentName + " |type: " + type +
                "|permissions" + permissions);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("contentId", contentId);
        data.putString("contentName", contentName);
        msg.what = MSG_CHOOSE_CONTENT;
        msg.arg1 = type;
        msg.arg2 = permissions;
        msg.setData(data);
        mHandler.sendMessage(msg);
    }


    @JavascriptInterface
    public void showToast(String toastMsg) {
        CustomLog.d(TAG, "showToast:" + toastMsg);
        if (!TextUtils.isEmpty(toastMsg)) {
            Message msg = new Message();
            msg.what = MSG_SHOW_TOAST;
            msg.obj = toastMsg;
            mHandler.sendMessage(msg);
        }
    }


    //跳转到个人名片
    @JavascriptInterface
    public void toPersonalCard(String nubeId) {
        CustomLog.d(TAG, "toPersonalCard:" + nubeId);
        Message msg = new Message();
        msg.what = MSG_SHOW_PERSONAL_CARD;
        msg.obj = nubeId;
        mHandler.sendMessage(msg);
    }


    //跳转到公众号主页
    @JavascriptInterface
    public void toOfficialMain(String officialId, String officialName) {
        CustomLog.d(TAG, "toOfficialMain" + officialId + "" + officialName);
        Message msg = new Message();
        msg.what = MSG_SHOW_OFFICIAL_MAIN;
        msg.obj = officialId;
        mHandler.sendMessage(msg);

    }


    //转发到群或者个人
    @JavascriptInterface
    public void forwardArticle(final String articleId, final String articleTile, final String previewUrl, final String introduction, final int articleType, final String officeName) {
        CustomLog.d(TAG,
            "articleId:" + articleId + " | articleTile: " + articleTile + " |previewUrl: " +
                previewUrl + " |introduction: " + introduction + " |articleType:" + articleType +
                " |officeName:" + officeName);
        AccountManager.TouristState touristState = AccountManager.getInstance(mContext)
            .getTouristState();
        if (touristState == AccountManager.TouristState.TOURIST_STATE) {
            CustomDialog1.Builder builder = new CustomDialog1.Builder(mContext);
            builder.setMessage(R.string.only_register_can_user_login_again);
            builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.login_or_register,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(mContext, LoginActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            builder.create().show();
        } else {
            DataBodyInfo bean = new DataBodyInfo();
            bean.setArticleId(articleId);
            bean.setTitle(articleTile);
            bean.setPreviewUrl(previewUrl);
            bean.setIntroduction(introduction);
            bean.setArticleType(articleType);
            bean.setType(FileTaskManager.NOTICE_TYPE_ARTICAL_SEND);
            bean.name = officeName;
            Intent mIntent = new Intent(mContext, ShareLocalActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("articleInfo", bean);
            mIntent.putExtras(mBundle);
            mContext.startActivity(mIntent);
        }

    }


    //转发到微信 QQ或者红云医疗群或者个人 (增加外链地址externalLinkUrl)
    @JavascriptInterface
    public void forwardArticle(final String articleId, final String articleTile, final String previewUrl, final String introduction, final int articleType, final String officeName, final String externalLinkUrl) {
        CustomLog.d(TAG,
            "articleId:" + articleId + " | articleTile: " + articleTile + " |previewUrl: " +
                previewUrl + " |introduction: " + introduction + " |articleType:" + articleType +
                " |officeName:" + officeName + " |externalLinkUrl:" + externalLinkUrl);
        AccountManager.TouristState touristState = AccountManager.getInstance(mContext)
            .getTouristState();
        if (touristState == AccountManager.TouristState.TOURIST_STATE) {
            CustomDialog1.Builder builder = new CustomDialog1.Builder(mContext);
            builder.setMessage(R.string.only_register_can_user_login_again);
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.login_or_register,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(mContext, LoginActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            builder.create().show();
        } else {
            new Thread() {
                @Override
                public void run() {
                    Bitmap bitmap = decodeUriAsBitmapFromNet(previewUrl);
                    Message msg = mHandler.obtainMessage(MSG_LOAD_SUCCESS, bitmap);
                    mHandler.sendMessage(msg);
                }
            }.start();
            ShareFriendDialog share2FriendsDialog = new ShareFriendDialog(mContext);
            share2FriendsDialog.setShare2weChatListener(
                new Share2FriendsDialog.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (GetNetype(mContext) == -1) {
                            CustomToast.show(mContext, R.string.check_internet,
                                CustomToast.LENGTH_SHORT);
                            return;
                        }

                        if (isWeixinAvilible()) {
                            shareByWx(articleTile, introduction, bitmap, externalLinkUrl);
                        }
                    }
                });
            share2FriendsDialog.setShare2QQListener(new Share2FriendsDialog.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    if (GetNetype(mContext) == -1) {
                        CustomToast.show(mContext, R.string.check_internet,
                            CustomToast.LENGTH_SHORT);
                        return;
                    }
                    shareByQQ(articleTile, introduction, previewUrl, externalLinkUrl);
                }
            });
            share2FriendsDialog.setShare2MsgListener(new Share2FriendsDialog.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    CustomLog.d(TAG, "articleId:" + articleId + " | articleTile: " + articleTile +
                        " |previewUrl: " + previewUrl + " |introduction: " + introduction +
                        " |articleType:" + articleType + " |officeName:" + officeName);
                    // CustomToast.show(mContext,"转发",Toast.LENGTH_SHORT);
                    DataBodyInfo bean = new DataBodyInfo();
                    bean.setArticleId(articleId);
                    bean.setTitle(articleTile);
                    bean.setPreviewUrl(previewUrl);
                    bean.setIntroduction(introduction);
                    bean.setArticleType(articleType);
                    bean.setType(FileTaskManager.NOTICE_TYPE_ARTICAL_SEND);
                    bean.name = officeName;
                    Intent mIntent = new Intent(mContext, ShareLocalActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable("articleInfo", bean);
                    mIntent.putExtras(mBundle);
                    mContext.startActivity(mIntent);
                }
            });
        }

    }


    //播放器收放接口
    @JavascriptInterface
    public void expandVideoWindow(int operation) {
        CustomLog.d(TAG, "expandVideoWindow:" + operation);
        Message msg = new Message();
        msg.what = MSG_VIDEO_ANIMATION;
        msg.arg1 = operation;
        mHandler.sendMessage(msg);
    }


    //. 显示“仅注册用户”有权限弹框
    @JavascriptInterface
    public void showWarningDialog(int type) {
        CustomLog.d(TAG, "showWarningDialog:" + type);
        Message msg = new Message();
        msg.what = MSG_REGISTERED;
        msg.obj = type;
        mHandler.sendMessage(msg);
    }


    @JavascriptInterface
    public void writeLog(String param) {
        try {
            JSONObject logJSON = new JSONObject(param);
            int type = logJSON.getInt("type");
            String msg = logJSON.getString("msg");

            if (type == 0) {
                CustomLog.i(TAG_JS, msg);
            } else if (type == 1) {
                CustomLog.d(TAG_JS, msg);
            } else if (type == -1) {
                CustomLog.e(TAG_JS, msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            CustomLog.e(TAG, e.toString());
        }
    }


    private void shareByQQ(String articleTile, String introduction, String previewUrl, String externalLinkUrl) {
        final Bundle params = new Bundle();
        if (externalLinkUrl.equals("undefined")) {
            externalLinkUrl = "http://www.butel.com/";
        }
        params.putString(QQShare.SHARE_TO_QQ_TITLE, articleTile);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, introduction);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, externalLinkUrl);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, previewUrl);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mContext.getString(R.string.app_name));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTencent.shareToQQ((Activity) mContext, params, new IUiListener() {
                    @Override
                    public void onComplete(Object o) {

                    }


                    @Override
                    public void onError(UiError uiError) {

                    }


                    @Override
                    public void onCancel() {

                    }
                });
            }
        });

    }


    private void shareByWx(String articleTile, String introduction, Bitmap bitmap, String externalLinkUrl) {
        if (!done) {
            CustomToast.show(mContext, mContext.getString(R.string.down_thumbnail_waiting),
                CustomToast.LENGTH_LONG);
            return;
        }
        if (externalLinkUrl.equals("undefined")) {
            externalLinkUrl = "http://www.butel.com/";
        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, baos);
        byte[] bytes = baos.toByteArray();
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = externalLinkUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = articleTile;
        msg.description = introduction;
        msg.thumbData = bytes;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }


    private String buildTransaction(String type) {
        return (type == null)
               ? String.valueOf(System.currentTimeMillis())
               : type + System.currentTimeMillis();
    }


    private void switchToDingYueActivity(String officeId) {
        Intent intent = new Intent(mContext, DingYueActivity.class);
        intent.putExtra("officialAccountId", officeId);
        mContext.startActivity(intent);
    }


    private void switchToContactCardActivity(String nubeId) {
        Intent intent = new Intent(mContext, ContactCardActivity.class);
        intent.putExtra("nubeNumber", nubeId);
        mContext.startActivity(intent);
    }


    private void switchToOfficialMainActivity(String officialId) {
        Intent intent = new Intent(mContext, OfficialMainActivity.class);
        intent.putExtra("officialAccountId", officialId);
        //        intent.putExtra("officialName",officialName);
        mContext.startActivity(intent);
    }


    public abstract void onChooseContent(String contentId, String contentName, int type, int permissions);

    public abstract void onExpandVideoWindow(int operation);


    public static String getUUID() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }


    //微信分享
    private boolean isWeixinAvilible() {
        final PackageManager packageManager = mContext.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        CustomToast.show(mContext, mContext.getString(R.string.weixin_version_low_havenot),
            CustomToast.LENGTH_LONG);
        return false;
    }


    /**
     * 根据图片的url路径获得Bitmap对象
     */
    private Bitmap decodeUriAsBitmapFromNet(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl
                .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            done = true;
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * 进入筹备群组
     */
    @JavascriptInterface
    public void enterPreparingGroup(String groupJson) {
        String groupId = "";
        try {
            JSONObject object = new JSONObject(groupJson);
            groupId = object.optString("groupId");
            currRequestId = object.optString("requestId", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(groupId)) {
            CustomLog.e(TAG, "groupId is null");
            return;
        }
        CustomLog.d(TAG, "enterPreparingGroup gid:" + groupId
            + " requestId:" + currRequestId);
        if (enterGroupUtil == null) {
            enterGroupUtil = new EnterGroupUtil(mContext, groupId);
            enterGroupUtil.setEnterGroupListener(new EnterGroupUtil.EnterGroupListener() {
                @Override
                public void OnSuccess() {
                    onReportSucc();
                }


                @Override
                public void OnFailed(int statusCode, String statusInfo) {
                    onReportFailed(statusCode, statusInfo);
                }
            });
        }
        enterGroupUtil.isEnterGroupSuccess = false;
        enterGroupUtil.enterGroup();
    }


    private void onReportSucc() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestId", currRequestId);
            jsonObject.put("rc", 0);
            jsonObject.put("desc", "加入群组成功");
            jsSignUpHelper.onEnterGroupFinshed(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void onReportFailed(int errorCode, String desc) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestId", currRequestId);
            jsonObject.put("rc", errorCode);
            jsonObject.put("desc", desc);
            jsSignUpHelper.onEnterGroupFinshed(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        currRequestId = "";
        jsSignUpHelper = null;
    }
}
