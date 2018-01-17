package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.view.View;
import android.view.View.OnClickListener;
/**
 * <dl>
 * <dt>SwitchButton.java</dt>
 * <dd>Description:打开/关闭开关</dd>
 * <dd>Copyright: Copyright (C) 2015</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2015-8-24 13:18:17</dd>
 * </dl>
 * @author niuben
 */
public class SwitchButton extends ImageButton implements OnClickListener{
    private OnCheckedChangeListener mListener=null;
    private int onbg=-1;
    private int offbg=-1;
    private boolean isCheck;


    public SwitchButton(Context context) {
        super(context);
        initSwitch();
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSwitch();
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSwitch();
    }

    private void initSwitch(){
        this.setChecked(false);
    }

    public void setChecked(boolean _isChecked) {
        this.isCheck=_isChecked;
        if (isCheck){
            if (-1!=onbg){
                this.setImageResource(onbg);//未选中，false
            }
        }else {
            if (-1!=offbg){
                this.setImageResource(offbg);//未选中，false
            }
        }
    }

    public void setOnBackgroundResource(int on){
        onbg=on;
    }

    public void setOffBackgroundResource(int off){
        offbg=off;
    }

    private void setChecked(boolean _isChecked,boolean doChecked) {
        setChecked(_isChecked);
        if (doChecked==true){
            if (mListener!=null){
                mListener.onCheckedChanged(this, this.isCheck);
            }
        }
    }

    public void Switch(){
        setChecked(!this.isCheck,true);
    }

    public boolean isChecked() {
        return this.isCheck;
    }

    public void setOnCheckedChangeListener(
        OnCheckedChangeListener onCheckedChangeListener) {
        mListener=onCheckedChangeListener;
        setOnClickListener(this);
    }

    public interface OnCheckedChangeListener{
        public void onCheckedChanged(SwitchButton button,boolean isChecked);
    }

    @Override
    public void onClick(View arg0) {
        setChecked(!this.isCheck,true);
    }
}