/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.redcdn.hvs.im.util.smileUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import cn.redcdn.log.CustomLog;
import cn.redcdn.hvs.R;


/**
 * @author Hieu Rocker (rockerhieu@gmail.com).
 */
public class EmojiconEditText extends EditText {
	private int mEmojiconSize;
	private int mEmojiconAlignment;
	private int mEmojiconTextSize;
	private boolean mUseSystemDefault = false;

	public EmojiconEditText(Context context) {
		super(context);
		mEmojiconSize = (int) getTextSize();
		mEmojiconTextSize = (int) getTextSize();
	}

	public EmojiconEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public EmojiconEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.Emojicon);
		mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize,
				38);
		mEmojiconAlignment = a.getInt(R.styleable.Emojicon_emojiconAlignment,
				DynamicDrawableSpan.ALIGN_BASELINE);
		mUseSystemDefault = a.getBoolean(
				R.styleable.Emojicon_emojiconUseSystemDefault, false);
		a.recycle();
		mEmojiconTextSize = (int) getTextSize();
		CustomLog.d("EmojiconEditText","textSize=" + mEmojiconTextSize + ",emojiSize="
				+ mEmojiconSize);
		setText(getText());
	}

	@Override
	protected void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		CustomLog.d("EmojiconEditText","start=" + start + "|lengthBefore=" + lengthBefore + "|lengthAfter=" + lengthAfter);
	    if (lengthAfter == 0) {
	        // 删除的场合，不需要更新表情
	    } else {
	        EmojiconHandler.addEmojis(getContext(), getText(), mEmojiconSize,
	                mEmojiconAlignment, mEmojiconTextSize, start, lengthAfter, mUseSystemDefault);
	    }
	}

	/**
	 * Set the size of emojicon in pixels.
	 */
	public void setEmojiconSize(int pixels) {
		mEmojiconSize = pixels;

        EmojiconHandler.refreshEmojis(getContext(), getText(), mEmojiconSize,
                mEmojiconAlignment, mEmojiconTextSize, mUseSystemDefault);
	}

	/**
	 * Set whether to use system default emojicon
	 */
	public void setUseSystemDefault(boolean useSystemDefault) {
		mUseSystemDefault = useSystemDefault;
	}
}
