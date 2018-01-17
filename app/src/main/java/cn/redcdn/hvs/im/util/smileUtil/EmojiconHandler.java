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
import android.text.Spannable;

import cn.redcdn.log.CustomLog;


/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
public final class EmojiconHandler {
	private EmojiconHandler() {
	}

//	private static final SparseIntArray sEmojisMap = new SparseIntArray(1029);
//	private static final SparseIntArray sSoftbanksMap = new SparseIntArray(471);
//	private static Map<String, Integer> sEmojisModifiedMap = new HashMap<String, Integer>();

//	private static boolean isSoftBankEmoji(char c) {
//		return ((c >> 12) == 0xe);
//	}

	private static int getEmojiResource(Context context, int codePoint) {
		return context.getResources().getIdentifier(
                Emojicon.getHexResName(codePoint), "drawable",
                context.getApplicationContext().getPackageName());
	}

//	private static int getSoftbankEmojiResource(char c) {
//		return sSoftbanksMap.get(c);
//	}

    public static void refreshEmojis(Context context, Spannable text,
            int emojiSize, int emojiAlignment, int textSize,
            boolean useSystemDefault) {
        addEmojis(context, text, emojiSize, emojiAlignment, textSize, 0, -1,
                useSystemDefault);
    }

	/**
	 * Convert emoji characters of the given Spannable to the according
	 * emojicon.
	 * 
	 * @param context
	 * @param text
	 * @param emojiSize
	 * @param emojiAlignment
	 * @param textSize
	 * @param index
	 * @param length
	 * @param useSystemDefault
	 */
	public static void addEmojis(Context context, Spannable text,
			int emojiSize, int emojiAlignment, int textSize, int index,
			int length, boolean useSystemDefault) {
		if (useSystemDefault) {
			return;
		}

		int textLength = text.length();
		int textLengthToProcessMax = textLength - index;
		int textLengthToProcess = length < 0
				|| length >= textLengthToProcessMax ? textLength
				: (length + index);
		CustomLog.d("EmojiconHandler","index=" + index + "|length=" + length + "|textLengthToProcess=" + textLengthToProcess);

		// remove spans
		EmojiconSpan[] oldSpans = text.getSpans(index, textLengthToProcess,
				EmojiconSpan.class);
		for (int i = 0; i < oldSpans.length; i++) {
			text.removeSpan(oldSpans[i]);
		}

		int skip;
		for (int i = index; i < textLengthToProcess; i += skip) {
			skip = 0;
			int icon = 0;
//			char c = text.charAt(i);
//			if (isSoftBankEmoji(c)) {
//				icon = getSoftbankEmojiResource(c);
//				skip = icon == 0 ? 0 : 1;
//			}

			if (icon == 0) {
				int unicode = Character.codePointAt(text, i);
				skip = Character.charCount(unicode);

				if (unicode > 0xff) {
					icon = getEmojiResource(context, unicode);
				}

				if (i + skip < textLengthToProcess) {
					int followUnicode = Character.codePointAt(text, i + skip);
					// Non-spacing mark (Combining mark)
					if (followUnicode == 0xfe0f) {
						int followSkip = Character.charCount(followUnicode);
						if (i + skip + followSkip < textLengthToProcess) {

							int nextFollowUnicode = Character.codePointAt(text,
									i + skip + followSkip);
							if (nextFollowUnicode == 0x20e3) {
								int nextFollowSkip = Character
										.charCount(nextFollowUnicode);
								int tempIcon = getKeyCapEmoji(unicode);

								if (tempIcon == 0) {
									followSkip = 0;
									nextFollowSkip = 0;
								} else {
									icon = tempIcon;
								}
								skip += (followSkip + nextFollowSkip);
							}
						}
					} else if (followUnicode == 0x20e3) {
						// some older versions of iOS don't use a combining
						// character, instead it just goes straight to the
						// second part
						int followSkip = Character.charCount(followUnicode);

						int tempIcon = getKeyCapEmoji(unicode);
						if (tempIcon == 0) {
							followSkip = 0;
						} else {
							icon = tempIcon;
						}
						skip += followSkip;

//					} else {
//						// handle other emoji modifiers
//						int followSkip = Character.charCount(followUnicode);
//
//						// TODO seems like we could do this for every emoji type
//						// rather than having that giant static map, maybe this
//						// is too slow?
//						String hexUnicode = Integer.toHexString(unicode);
//						String hexFollowUnicode = Integer
//								.toHexString(followUnicode);
//
//						String resourceName = "emoji_" + hexUnicode + "_"
//								+ hexFollowUnicode;
//
//						int resourceId = 0;
//						if (sEmojisModifiedMap.containsKey(resourceName)) {
//							resourceId = sEmojisModifiedMap.get(resourceName);
//						} else {
//							resourceId = context.getResources().getIdentifier(
//									resourceName,
//									"drawable",
//									context.getApplicationContext()
//											.getPackageName());
//							if (resourceId != 0) {
//								sEmojisModifiedMap
//										.put(resourceName, resourceId);
//							}
//						}
//
//						if (resourceId == 0) {
//							followSkip = 0;
//						} else {
//							icon = resourceId;
//						}
//						skip += followSkip;
					}
				}
			}

			if (icon > 0) {
				text.setSpan(new EmojiconSpan(context, icon, emojiSize,
						emojiAlignment, textSize), i, i + skip,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	private static int getKeyCapEmoji(int unicode) {
		int icon = 0;
//		switch (unicode) {
//		case 0x0023:
//			icon = R.drawable.emoji_0023;
//			break;
//		case 0x002a:
//			icon = R.drawable.emoji_002a_20e3;
//			break;
//
//		default:
//			break;
//		}
		return icon;
	}

}
