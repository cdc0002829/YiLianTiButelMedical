package cn.redcdn.hvs.profiles.voicedetection;

/**
 * Created by Administrator on 2017/2/27.
 */

public interface IMediaHelperCb {
    public final static int recordfile_beg = 1;
    public final static int playfile_beg = 2;
    public final static int recordfile_end = 3;
    public final static int playfile_end = 4;

    public final static int analyse_end = 5;

    public final static int playaec_end = 6;

    public final static int recordaec_end = 7;

    public final static int synplayrecord_end = 8;

    public final static int testrecordjitter_end = 9;

    void notifyEvt(int id, String data);

    void callBack();
}
