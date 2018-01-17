package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * *
 * <dl>
 *
 * <dt>RoundCornerImageView.java</dt>
 *
 * <dd>Description:圆角图片</dd>
 *
 * <dd>Copyright: Copyright (C) 2012</dd>
 *
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 *
 * <dd>CreateDate: 2012-10-12</dd>
 *
 * </dl>
 * * @author zhaguitao
 */

public class RoundCornerImageView extends ImageView{

    public RoundCornerImageView(Context context) {
        super(context);
        init(context, null);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private Paint paint;
    private int roundWidth = 5;
    private int roundHeight = 5;
    private Paint paint2;

    /**
     * @author majj 设置圆角弧度，初始弧度为5，设置弧度为控件长度一半时，该控件为圆形
     * @param round ---此处的round不是px；而是dp
     */
    public void setRound(int round) {
        roundWidth = round;
        roundHeight = round;
    }

    private void init(Context context, AttributeSet attrs) {

        float density = context.getResources().getDisplayMetrics().density;
        roundWidth = (int) (roundWidth * density);
        roundHeight = (int) (roundHeight * density);

        paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

        paint2 = new Paint();
        paint2.setXfermode(null);
    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap);
        super.draw(canvas2);
        drawLiftUp(canvas2);
        drawRightUp(canvas2);
        drawLiftDown(canvas2);
        drawRightDown(canvas2);
        canvas.drawBitmap(bitmap, 0, 0, paint2);
        bitmap.recycle();
    }

    private void drawLiftUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, roundHeight);
        path.lineTo(0, 0);
        path.lineTo(roundWidth, 0);
        path.arcTo(new RectF(0, 0, roundWidth * 2, roundHeight * 2), -90, -90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLiftDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, getHeight() - roundHeight);
        path.lineTo(0, getHeight());
        path.lineTo(roundWidth, getHeight());
        path.arcTo(new RectF(0, getHeight() - roundHeight * 2,
                0 + roundWidth * 2, getHeight()), 90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth() - roundWidth, getHeight());
        path.lineTo(getWidth(), getHeight());
        path.lineTo(getWidth(), getHeight() - roundHeight);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, getHeight()
                - roundHeight * 2, getWidth(), getHeight()), 0, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth(), roundHeight);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth() - roundWidth, 0);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, 0, getWidth(),
                0 + roundHeight * 2), -90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //按下状态改变时invalidate(触发onDraw),onDraw中根据按下状态绘制0x33000000,实现点击遮罩层效果
        if (isPressed())
            canvas.drawColor(0x33000000);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        //imageView.setClickable(true),或imageView.setOnClickListener时才可触发dispatchSetPressed
        super.dispatchSetPressed(pressed);
        invalidate();
    }
}
