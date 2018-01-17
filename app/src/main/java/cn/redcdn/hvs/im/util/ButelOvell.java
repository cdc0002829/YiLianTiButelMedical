package cn.redcdn.hvs.im.util;



import cn.redcdn.hvs.R;
import com.butel.connectevent.utils.LogUtil;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <dl>
 * <dt>ButelOvell.java</dt>
 * <dd>Description:Butel设备的能力值</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2015-11-9 17:11:34</dd>
 * </dl>
 * @author niuben
 */

public class ButelOvell {

    //	Ovell: 0010100(发名片、发图片、会议、视频电话、发文字、发语音、发视频)
    private static HashMap<String, Integer> mOvellMap=new HashMap<String, Integer>();//key 号码段,value能力值
    private static HashMap<String, String> mTypeMap=new HashMap<String, String>();//key 号码段,value类型 x1,m1,n8j,n7,n8,moblie

    private static String mButelOvellString="";
    private static String mButelOvellDefault="["+
        "{\"nubeSegment\":\"60\",\"ovell\":\"0010000\",\"type\":\"m1\"}," +
        "{\"nubeSegment\":\"691\",\"ovell\":\"0010000\",\"type\":\"m1\"}," +
        "{\"nubeSegment\":\"62\",\"ovell\":\"0011000\",\"type\":\"n8j\"}," +
        "{\"nubeSegment\":\"692\",\"ovell\":\"0011000\",\"type\":\"n8j\"}," +
        "{\"nubeSegment\":\"5\",\"ovell\":\"0001000\",\"type\":\"x1\"}," +
        "{\"nubeSegment\":\"7\",\"ovell\":\"1101000\",\"type\":\"n7,n8\"}," +
        "{\"nubeSegment\":\"9\",\"ovell\":\"1111111\",\"type\":\"mobile\"}," +
        "{\"nubeSegment\":\"63\",\"ovell\":\"1111111\",\"type\":\"mobile\"}," +
        "{\"nubeSegment\":\"800\",\"ovell\":\"1101111\",\"type\":\"enterprise_access\"}," +
        "{\"nubeSegment\":\"696\",\"ovell\":\"0010000\",\"type\":\"mbox\"}," +
        "{\"nubeSegment\":\"690\",\"ovell\":\"0010000\",\"type\":\"mbox\"}," +
        "{\"nubeSegment\":\"61\",\"ovell\":\"0010000\",\"type\":\"mbox\"}" +
        "]";
    public static final String butel_type_x1="x1",
        butel_type_m1="m1",
        butel_type_n8j="n8j",
        butel_type_n7="n7",
        butel_type_n8="n8",
        butel_type_mobile="mobile",
        butel_type_enterprise_access="enterprise_access",
        butel_type_mbox="mbox";

    private static final byte
        ABILITY_OF_SENDING_VEDIO=1,
        ABILITY_OF_SENDING_RECORD=2,
        ABILITY_OF_SENDING_TEXT=4,
        ABILITY_OF_CALLING=8,
        ABILITY_OF_MEETING=16,
        ABILITY_OF_SENDING_PICTURES=32,
        ABILITY_OF_SENDING_CARD=64;

    /**
     * 有更新时，采用最新的，在saveResult做一次该操作。因为该值不会频繁变动，无需每次都读数据库
     */
    public static void checkDaoPreference(){
        // String butelovell= NetPhoneApplication.getPreference().getKeyValue(PrefType.KEY_BUTEL_OVELL, mButelOvellDefault);
        // if (!(mButelOvellString.equals(butelovell))){
        //     LogUtil.d("与缓存不相等");
        //     saveButelOvell(butelovell);
        //     mButelOvellString=butelovell;
        // }
    }

    private static synchronized void saveButelOvell(String ButelovellArray){
        mOvellMap=new HashMap<String, Integer>();
        mTypeMap=new HashMap<String, String>();
        try {
            JSONArray ButelOvellObject = new JSONArray(ButelovellArray);
            for (int i = 0; i < ButelOvellObject.length(); i++) {
                try {
                    JSONObject obj = ButelOvellObject.getJSONObject(i);
                    String nubeSegment = obj.optString("nubeSegment");
                    String ovell = obj.optString("ovell");
                    String type = obj.optString("type");
                    if (type.contains(",")){
                        LogUtil.d("type="+type+",多个取第一个,客户端显示无法区分");
                        type=type.substring(0,type.indexOf(","));
                    }
                    mOvellMap.put(nubeSegment, getOvellFromOvellString(ovell));
                    mTypeMap.put(nubeSegment, type);
                } catch (JSONException e2) {
                    LogUtil.e("JSONException", e2);
                    continue;
                }
            }
        } catch (JSONException e1) {
            LogUtil.e("JSONArray", e1);
        }
    }

    public static String getNubeType(String nube) {
        if (mOvellMap.size()==0){
            checkDaoPreference();
        }
        String type=butel_type_mobile;//默认为手机号
        if (mTypeMap.size()>0){
            int length=nube.length();
            for (int i=0;i<length;i++){
                String nubeStart=nube.substring(0, length-i);
                if (mTypeMap.containsKey(nubeStart)){
                    type=mTypeMap.get(nubeStart);
                    break;
                }
            }
        }
        return type;
    }

    public static byte getNubeOvell(String nube){
        if (mOvellMap.size()==0){
            checkDaoPreference();
        }
        byte ovell=getDefaultOvell();//默认全部满足
        if (mOvellMap.size()>0){
            int length=nube.length();
            for (int i=0;i<length;i++){
                String nubeStart=nube.substring(0, length-i);
                if (mOvellMap.containsKey(nubeStart)){
                    ovell=mOvellMap.get(nubeStart).byteValue();
                    break;
                }
            }
        }
        return ovell;
    }

    private static int getOvellFromOvellString(String ovellString){
        int ovell=0;
        int length=ovellString.length();
        for (int i=0;i<length;i++){
            if ("1".equals(String.valueOf(ovellString.charAt(i)))){
                int ov=1;
                for (int j=1;j<length-i;j++){
                    ov=2*ov;
                }
                ovell=ov+ovell;
            }
        }
        LogUtil.d("ovellString="+ovellString+"|ovell="+ovell);
        return ovell;
    }

    private static byte getDefaultOvell(){
        return ABILITY_OF_SENDING_VEDIO|ABILITY_OF_SENDING_RECORD|ABILITY_OF_SENDING_TEXT|ABILITY_OF_MEETING|ABILITY_OF_CALLING|ABILITY_OF_SENDING_PICTURES|ABILITY_OF_SENDING_CARD;
    }

    public static int getNubeIconId(String nube){
        String type=getNubeType(nube);
        if (type.equals(butel_type_m1)){
            return R.drawable.butel_m1;
        }else if(type.equals(butel_type_mbox)){
            return R.drawable.butel_m1;
        }else if (type.equals(butel_type_n8j)){
            return  R.drawable.butel_n8;
        }else if (type.equals(butel_type_x1)){
            return R.drawable.butel_m1;
        }else if (type.equals(butel_type_n7)){
            return R.drawable.butel_n8;
        }else if (type.equals(butel_type_n8)){
            return R.drawable.butel_n8;
        }else if (type.equals(butel_type_mobile)){
            return R.drawable.butel_phone;
        }else if (type.equals(butel_type_enterprise_access)){
            return R.drawable.butel_type_enterprise_access;
        }else {
            return R.drawable.butel_phone;
        }
    }

    public static boolean hasSendMessageAbility(byte ovell){
        return !((ABILITY_OF_SENDING_TEXT&ovell)==0);
    }

    public static boolean hasCallAbility(byte ovell){
        return !((ABILITY_OF_CALLING&ovell)==0);
    }

    public static boolean hasMeetingAbility(byte ovell){
        return !((ABILITY_OF_MEETING&ovell)==0);
    }

    public static boolean hasSendPicturesAbility(byte ovell){
        return !((ABILITY_OF_SENDING_PICTURES&ovell)==0);
    }

    public static boolean hasSendCardAbility(byte ovell){
        return !((ABILITY_OF_SENDING_CARD&ovell)==0);
    }

    public static boolean hasSendVedioAbility(byte ovell){
        return !((ABILITY_OF_SENDING_VEDIO&ovell)==0);
    }

    public static boolean hasSendRecordAbility(byte ovell){
        return !((ABILITY_OF_SENDING_RECORD&ovell)==0);
    }
}
