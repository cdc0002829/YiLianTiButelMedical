package cn.redcdn.hvs.appinstall;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public class CheckHashCode {
  /**
   * 适用于大的文件
   */
  public static String getFileSha1(String path) throws OutOfMemoryError,
      IOException
  {
    File file = new File(path);
    FileInputStream in = new FileInputStream(file);
    MessageDigest messagedigest;
    try
    {
      messagedigest = MessageDigest.getInstance("SHA-1");

      byte[] buffer = new byte[1024 * 1024 * 10];
      int len = 0;

      while ((len = in.read(buffer)) > 0)
      {
        // 该对象通过使用 update（）方法处理数据
        messagedigest.update(buffer, 0, len);
      }

      // 对于给定数量的更新数据，digest 方法只能被调用一次。在调用 digest 之后，MessageDigest
      // 对象被重新设置成其初始状态。
      return byte2hex(messagedigest.digest());
    }
    catch (NoSuchAlgorithmException e)
    {
      Log.e("CheckHashCode","getFileSha1->NoSuchAlgorithmException###");
    }
    catch (OutOfMemoryError e)
    {

      Log.e("CheckHashCode","getFileSha1->OutOfMemoryError###");
    }
    finally
    {
      in.close();
    }
    return null;
  }

  private static String byte2hex(byte[] b)
  {
    StringBuffer hs = new StringBuffer(b.length);
    String stmp = "";
    int len = b.length;
    for (int n = 0; n < len; n++)
    {
      stmp = Integer.toHexString(b[n] & 0xFF);
      if (stmp.length() == 1)
      {
        hs = hs.append("0").append(stmp);
      }
      else
      {
        hs = hs.append(stmp);
      }
    }
    return String.valueOf(hs);
  }


}
