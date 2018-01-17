package cn.redcdn.dep;

public class FileUploadClientJNI{

    private FileUploadClientJNI() {}

	/*
	启动agent。
	参数说明：
		@param :userid：		视讯号。
		@param :hardwareid：	"MB" 。
		@param :clientIP，clientPort： 本地所用ip 端口。
		@param :srvIP , srvPort ：	日志服务器的ip 和端口。 nps 中获取 JustMeetingLogUpload 下的配置 。
		@param :zipfilepath：	上传数据压缩包临时放置目录。
		@param :uppathsjson：要上传的 路径， 是json 格式：
				{
					"uploadpaths": [
						{"path": "/mnt/sdcard/meeting/log"},
						{"path": "/mnt/sdcard/meeting/QOSLOG"},
						{"path": "/mnt/sdcard/meeting/crash"}
					]
				}
		@param :configpath：本模块日志输出，日志配置文件路径。
		@param :logoutpath：本模块日志输出路径。

	*/

    public static native int StartLogUploadManager(String userid , String hardwareid,
                                                   String clientIP , int clientPort ,
                                                   String srvIP ,		int srvPort ,
                                                   String zipfilepath ,String uppathsjson ,
                                                   String configpath,String logoutpath);

    //停止agent
    public static native int StopLogUploadManager();

    static
    {

        System.loadLibrary("logwriter");
        System.loadLibrary("litezip");
        System.loadLibrary("fileuploadclient");
    }
}



/*

*/