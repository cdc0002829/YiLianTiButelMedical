package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;

import cn.redcdn.hvs.contacts.contact.ContactCardActivity;

public class TouchableSpan extends ClickableSpan {
    protected boolean mIsPressed;
    protected int mPressedBackgroundColor = 0xffd0d0d0;
    protected int mNormalTextColor = 0xff4191D3;
    protected int mPressedTextColor = 0xff4191D3;

    Context mContext;
    String str; // 文字内容
    String link; // 文字链接

    public TouchableSpan(Context context, String str, String link) {
        this.str = str;
        this.link = link;
        this.mContext = context;
    }

    public void setPressedBgColor(int color){
    	mPressedBackgroundColor=color;
    }
    public void setPressed(boolean isSelected) {
        mIsPressed = isSelected;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(mIsPressed ? mPressedTextColor : mNormalTextColor);
        ds.bgColor = mIsPressed ? mPressedBackgroundColor : Color.TRANSPARENT;
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        if (widget instanceof ComplexTextView) {
            if (((ComplexTextView) widget).ignoreSpannableClick())
                return;
            ((ComplexTextView) widget).preventNextClick();
        }
        if (TextUtils.isEmpty(link)) {
            return;
        }
        Intent intent = new Intent(mContext, ContactCardActivity.class);
        intent.putExtra("contact", link);
        // 查询是否是亲情好友的关系
//        FamilyNumberDao familyNumberDao = new FamilyNumberDao(mContext);
//        if (familyNumberDao.existFamilyNumber(link)) {
//            intent.putExtra(ContactDetail.IS_FAMILY_NUMBER, true);
//        } else {
//            intent.putExtra(ContactDetail.IS_FAMILY_NUMBER, false);
//        }
        mContext.startActivity(intent);
    }
}
