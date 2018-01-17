package cn.redcdn.hvs.im.util.smileUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import cn.redcdn.hvs.R;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by UKfire on 16/3/14.
 */
public class StringUtil {

    /**
     * 把String转换成SpannableString
     */
    public static SpannableString stringToSpannableString(String str, Context context) {
        if (str == null)
            return new SpannableString("");
        else {
            SpannableString spannableString = new SpannableString(str);
            String zhengze = "\\[e\\]\\d{4}\\[\\/e\\]";

            try {
                spannableString = getExpressionString(context, spannableString, zhengze);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return spannableString;
        }
    }

    /**
     * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
     *
     * @param context
     * @return
     */
    public static SpannableString getExpressionString(Context context, SpannableString spannableString, String zhengze) {

        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);        //通过传入的正则表达式来生成一个pattern

        try {
            dealExpression(context, spannableString, sinaPatten, 0);
        } catch (Exception e) {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }

    /**
     * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
     *
     * @param context
     * @param spannableString
     * @param patten
     * @param start
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws NumberFormatException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void dealExpression(Context context, SpannableString spannableString, Pattern patten, int start) throws SecurityException, NoSuchFieldException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
        Matcher matcher = patten.matcher(spannableString);
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        int sp = (int) (20 * fontScale + 0.5f);
        while (matcher.find()) {
            String key = matcher.group();
            if (matcher.start() < start) {
                continue;
            }
            String rid = "emotion_" + key.substring(3, 7);
            Field field = R.drawable.class.getDeclaredField(rid);
            int resId = Integer.parseInt(field.get(null).toString());        //通过上面匹配得到的字符串来生成图片资源id
            if (resId != 0) {
                Drawable drawable = context.getResources().getDrawable(resId);
                drawable.setBounds(0, 0, sp, sp);

                ImageSpan imageSpan = new ImageSpan(drawable);                //通过图片资源id来得到bitmap，用一个ImageSpan来包装

                int end = matcher.start() + key.length();                    //计算该图片名字的长度，也就是要替换的字符串的长度
                spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);    //将该图片替换字符串中规定的位置中
                if (end < spannableString.length()) {                        //如果整个字符串还未验证完，则继续。。
                    dealExpression(context, spannableString, patten, end);
                }
                break;
            }
        }
    }

}
