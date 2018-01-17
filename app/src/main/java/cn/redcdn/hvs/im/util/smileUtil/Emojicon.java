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

import android.os.Parcel;
import android.os.Parcelable;

import cn.redcdn.hvs.R;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
public class Emojicon implements Parcelable {

    public static final Creator<Emojicon> CREATOR = new Creator<Emojicon>() {
        @Override
        public Emojicon createFromParcel(Parcel in) {
            return new Emojicon(in);
        }


        @Override
        public Emojicon[] newArray(int size) {
            return new Emojicon[size];
        }
    };


    public String chineseName;

    public String uniCodeName;

    public boolean emojiSupport;

    public int index;

    private int icon;

    private char value;

    private String emoji;


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    private String name;


    public Emojicon(int icon, char value, String emoji, String name) {
        this.icon = icon;
        this.value = value;
        this.emoji = emoji;
        this.name = name;
    }


    public Emojicon(String chineseName, String uniCodeName, boolean emojiSupport) {
        this.chineseName = chineseName;
        this.emojiSupport = emojiSupport;
        this.uniCodeName = uniCodeName;
    }


    public Emojicon(Parcel in) {
        this.icon = in.readInt();
        this.value = (char) in.readInt();
        this.emoji = in.readString();
    }


    private Emojicon() {
    }


    public Emojicon(String emoji) {
        this.emoji = emoji;
    }


    public static Emojicon fromResource(int icon, int value) {
        Emojicon emoji = new Emojicon();
        emoji.icon = icon;
        emoji.value = (char) value;
        return emoji;
    }


    public static Emojicon fromCodePoint(int codePoint) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = newString(codePoint);

        return emoji;
    }


    public static Emojicon fromChar(char ch) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = Character.toString(ch);
        return emoji;
    }


    public static Emojicon fromChars(String chars) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = chars;
        return emoji;
    }


    public static final String newString(int codePoint) {
        //        if (Character.charCount(codePoint) == 1) {
        //            return String.valueOf(codePoint);
        //        } else {
        return new String(Character.toChars(codePoint));
        //        }
    }


    /**
     * 根据表情字符串获取表情资源文件名（emoji_unicode16）
     */
    public static final String getHexResName(String emojiStr) {
        return getHexResName(Character.codePointAt(emojiStr, 0));
    }


    public static final String getHexResName(int unicode) {
        String hexUnicode = Integer.toHexString(unicode);
        return "emoji_" + hexUnicode;
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(icon);
        dest.writeInt(value);
        dest.writeString(emoji);
    }


    public char getValue() {
        return value;
    }


    public int getIcon() {
        return icon;
    }


    public String getEmoji() {
        return emoji;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Emojicon && emoji.equals(((Emojicon) o).emoji);
    }


    @Override
    public int hashCode() {
        return emoji.hashCode();
    }


    //自定义辅助表情
    public static Emojicon getCancelEmoji() {
        Emojicon emoji = new Emojicon();
        emoji.icon = R.drawable.icon_delete_normal;
        emoji.value = (char) 0x0000;
        emoji.emoji = newString(0x0000);
        return emoji;
    }

}
