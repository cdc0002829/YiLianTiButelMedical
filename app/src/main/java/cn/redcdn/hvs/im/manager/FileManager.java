package cn.redcdn.hvs.im.manager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import com.butel.connectevent.utils.CommonUtil;
import com.butel.connectevent.utils.LogUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import cn.redcdn.hvs.R;

public class FileManager {
    public static final Uri IMAGE_BASEURI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final Uri VIDEO_BASEURI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private static FileManager instance = null;

    public static FileManager getFileManager() {
        if (null == instance) {
            instance = new FileManager();
        }
        return instance;
    }

    public static void cleanManager() {
        if (null != instance) {
            instance = null;
        }
    }

    public static boolean isImageFile(File file) {
        boolean isTrue = false;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            isTrue = isImageFileInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputStream = null;
            }
        }
        return isTrue;
    }

    public static boolean isImageFileInputStream(FileInputStream fis) {
        if (fis == null) {
            return false;
        }
        boolean isTrue = false;
        byte[] buffer = new byte[2];
        String filecode = "";
        try {
            if (-1 != fis.read(buffer)) {
                for (int i = 0; i < buffer.length; i++) {
                    filecode += Integer.toString((buffer[i] & 0xFF));
                }
                switch (Integer.parseInt(filecode)) {
                    case 255216:
                    case 7173:
                    case 6677:
                    case 13780:
                        isTrue = true;
                        break;
                    default:
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isTrue;
    }

    @SuppressWarnings("deprecation")
    public String getFilePath(Context context, Uri DBUri) {
        String path = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = ((Activity) context).managedQuery(DBUri, proj, null,
            null, null);
        if (null == cursor)
            return path;
        int column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        path = cursor.getString(column_index);
        cursor.close();
        cursor = null;
        return path;
    }

    public static Uri getImageUriThrowPath(Context context, String filePath) {
        Uri uri = null;
        Cursor cursor = context.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            new String[] { MediaStore.Images.Media._ID },
            MediaStore.Images.Media.DATA + "=? ",
            new String[] { filePath }, null);
        if (null != cursor) {
            cursor.moveToFirst();
            int id = cursor.getInt(cursor
                .getColumnIndex(MediaStore.MediaColumns._ID));
            uri = Uri.withAppendedPath(IMAGE_BASEURI, "" + id);
            cursor.close();
        }
        return uri;
    }

    /** 小米1 手机上，此段代码查询不出来图片路径 */
    // public String getImagePathFromUri(Context context, Uri contentUri) {
    // String[] proj = { MediaStore.Images.Media.DATA };
    // String path = null;
    // Cursor cursor = context.getContentResolver().query(
    // contentUri, proj, // Which columns to return
    // null, // WHERE clause; which rows to return (all rows)
    // null, // WHERE clause selection arguments (none)
    // null); // Order-by clause (ascending by name)
    // if(null != cursor){
    // int column_index =
    // cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    // cursor.moveToFirst();
    // path = cursor.getString(column_index);
    // cursor.close();
    // }
    // return path;
    // }

    /**
     * 获取uri物理地址
     *
     * @return
     */
    public static String getPhotoPath(Context context, Uri uri) {
        String photoPath = "";
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, null, null, null, null);
            int actual_image_column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            photoPath = cursor.getString(actual_image_column_index);
        } catch (Exception e) {
            photoPath = uri.getPath();
            Log.e("zhagt", uri.toString() + "|" + uri.getPath());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return photoPath;
    }

    /**
     * 获取uri物理地址
     *
     * @return
     */
    public static String getVideoPath(Context context, Uri uri) {
        if (uri == null) {
            return "";
        }
        String photoPath = "";
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, null, null, null, null);
            int actual_image_column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            photoPath = cursor.getString(actual_image_column_index);
        } catch (Exception e) {
            photoPath = uri.getPath();
            Log.e("zhagt", uri.toString() + "|" + uri.getPath());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return photoPath;
    }

    public static String getImgBucketName(Context context, String _id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        String name = context.getString(R.string.photo_gather);
        try {
            String[] selectArgs = new String[] { _id };
            cursor = cr
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.Media.BUCKET_DISPLAY_NAME },
                    MediaStore.Images.Media.BUCKET_ID + " = ? ",
                    selectArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("getContentsName", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return name;
    }

    public static String getVideoBucketName(Context context, String _id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        String name = context.getString(R.string.chat_video_title);
        try {
            String[] selectArgs = new String[] { _id };
            cursor = cr
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Video.Media.BUCKET_DISPLAY_NAME },
                    MediaStore.Video.Media.BUCKET_ID + " = ? ",
                    selectArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("getContentsName", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return name;
    }

    /**
     * 获取图片的宽高
     */
    public static int[] getImageSizeByPath(Context ctx, String path) {
        int width = 0;
        int height = 0;
        int orientation = -1;

        if (TextUtils.isEmpty(path)) {
            return new int[] { width, height, orientation };
        }

        Cursor cursor = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                cursor = ctx.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.Media.WIDTH,
                        MediaStore.Images.Media.HEIGHT,
                        MediaStore.Images.Media.ORIENTATION },
                    MediaStore.Images.Media.DATA + " = ?",
                    new String[] { path }, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    width = cursor.getInt(0);
                    height = cursor.getInt(1);
                    orientation = cursor.getInt(2);
                }
            }
            if (width == 0 && height == 0) {
                // android 4.1之前，只能通过计算获取图片宽高
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                width = options.outWidth;
                height = options.outHeight;
            }
            if (orientation == -1) {
                orientation = CommonUtil.getImageRotationFromUrl(path);
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            orientation = 0;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return new int[] { width, height, orientation };
    }

    /**
     * 获取视频时长
     */
    public static int getVideoDurationByPath(Context ctx, String path) {
        int duration = 0;

        if (TextUtils.isEmpty(path)) {
            return 0;
        }

        Cursor cursor = null;
        try {
            ArrayList<String> cols = new ArrayList<String>();
            cols.add(MediaStore.Video.Media.DURATION);
            cursor = ctx.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.DURATION},
                MediaStore.Video.Media.DATA + " = ?",
                new String[] { path }, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                duration = cursor
                    .getInt(0);
                LogUtil.d("duration:" + duration);
            }

        } catch (Exception e) {
            LogUtil.e("Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return duration;
    }

    /**
     * 获取视频的宽高及时长
     */
    public static int[] getVideoSizeByPath(Context ctx, String path) {
        int width = 0;
        int height = 0;
        int duration = 0;

        if (TextUtils.isEmpty(path)) {
            return new int[] { width, height, duration };
        }

        Cursor cursor = null;
        try {
            ArrayList<String> cols = new ArrayList<String>();
            cols.add(MediaStore.Video.Media.DURATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                cols.add(MediaStore.Video.Media.WIDTH);
                cols.add(MediaStore.Video.Media.HEIGHT);
            }
            cursor = ctx.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                cols.toArray(new String[cols.size()]),
                MediaStore.Video.Media.DATA + " = ?",
                new String[] { path }, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                duration = cursor
                    .getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    width = cursor
                        .getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                    height = cursor
                        .getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                }
            }

            if (width == 0 && height == 0) {
                // android 4.1之前，只能通过计算获取图片宽高
                Bitmap videoThumb = createVideoThumbnail(path);
                if (videoThumb != null) {
                    width = videoThumb.getWidth();
                    height = videoThumb.getHeight();
                    videoThumb.recycle();
                }
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return new int[] { width, height, duration };
    }

    /**
     * 生成视频缩略图
     */
    public static Bitmap createVideoThumbnail(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            LogUtil.e("IllegalArgumentException ex", ex);
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
            LogUtil.e("RuntimeException ex", ex);
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
                LogUtil.e("retriever.release", ex);
            }
        }

        return bitmap;
    }
}
