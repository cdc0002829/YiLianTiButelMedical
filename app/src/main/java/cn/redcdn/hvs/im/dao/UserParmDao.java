package cn.redcdn.hvs.im.dao;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Desc
 * Created by wangkai on 2017/3/3.
 */

public interface UserParmDao {

    public void updateUserParm(String commomKey, String commomValue,String userId);

    public String getUserParm(String commomKey,String userId);

    public boolean initUserParams(ConcurrentHashMap<String,String> mCurrentHashMap);

}
