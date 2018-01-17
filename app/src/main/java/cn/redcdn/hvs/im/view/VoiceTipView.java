package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.butel.connectevent.utils.LogUtil;

import org.w3c.dom.Text;

import java.util.Calendar;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/6/29.
 */

public class VoiceTipView extends FrameLayout {

    private Animation animation;
    private Context mContext;
    private SharePressableImageView contactIcon;
    private ImageView tipImageView;
    private TextView timestamp;

    public VoiceTipView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public VoiceTipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView(){
        LayoutInflater.from(mContext).inflate(R.layout.chat_voice_tip, this);
        contactIcon = (SharePressableImageView)findViewById(R.id.head_image);
        tipImageView = (ImageView)findViewById(R.id.tip_image);
        timestamp = (TextView)findViewById(R.id.voice_timestamp);
    }


    public void setTipInfo(String headUrl, Cursor cursor){
        Glide.with(mContext).load(headUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(contactIcon.shareImageview);
        Long currTime = System.currentTimeMillis();
        if(cursor == null){
            timestamp.setVisibility(View.VISIBLE);
            timestamp.setText(getDispTimestamp(currTime));
            return;
        }
        // 时间
        if (cursor.getCount() == 0) {
            // 第一个消息显示
            timestamp.setVisibility(View.VISIBLE);
            timestamp.setText(getDispTimestamp(currTime));
        } else {
             //两条消息时间离得如果稍长，显示时间
            cursor.moveToLast();
            long lastTime = (Long) getCursorDataByCol(cursor,
                    NoticesTable.NOTICE_COLUMN_SENDTIME);

            if (lastTime == 1) {
                lastTime = (Long) getCursorDataByCol(cursor,
                        NoticesTable.NOTICE_COLUMN_RECEIVEDTIME);
            }
            if (isCloseEnough(lastTime, currTime) ){
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setVisibility(View.VISIBLE);
                timestamp.setText(getDispTimestamp(currTime));
            }

        }
    }

    public void startAnimation(){
        animation =  AnimationUtils.loadAnimation(mContext, R.anim.voice_tip_animation);
        tipImageView.startAnimation(animation);
    }

    public void stopAnimation(){
        tipImageView.clearAnimation();
    }

    private Object getCursorDataByCol(Cursor cursor, String column) {
        if (cursor != null && !cursor.isClosed()) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(column));
        }
        return "0";
    }

    private String getDispTimestamp(long dbTime) {

        String dateStr = DateUtil.formatMs2String(dbTime,
                DateUtil.FORMAT_YYYY_MM_DD_HH_MM);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dbTime);

        Calendar nowCal = Calendar.getInstance();

        if (cal.get(Calendar.YEAR) < nowCal.get(Calendar.YEAR)) {
            // 跨年了，显示全部（2014-11-15 14:11）
            return dateStr;
        } else {
            int dayInterval = DateUtil.realDateIntervalDay(cal.getTime(),
                    nowCal.getTime());
            if (dayInterval == 0) {
                // 当天（14:11）
                return dateStr.substring(11, 16);
            } else if (dayInterval == 1) {
                return "昨天 " + dateStr.substring(11, 16);
            } else {
                // 显示月份（11-15 14:11）
                return dateStr.substring(5, 16);
            }
        }
    }

    /**
     * 判断时间间隔是否足够近（5分钟之内）
     */
    private boolean isCloseEnough(long dateFrom, long dateTo) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTimeInMillis(dateFrom);
        fromCal.set(Calendar.SECOND, 0);
        fromCal.set(Calendar.MILLISECOND, 0);

        Calendar toCal = Calendar.getInstance();
        toCal.setTimeInMillis(dateTo);
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);

        fromCal.add(Calendar.MINUTE, 5);

        if (fromCal.compareTo(toCal) >= 0) {
            // 5分钟之内
            return true;
        }
        return false;
    }

}
