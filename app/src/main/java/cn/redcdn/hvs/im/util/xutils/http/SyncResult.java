package cn.redcdn.hvs.im.util.xutils.http;

public class SyncResult {
    private boolean isOK = false;
    // 服务端返回的Json
    private String result = "";

    private int errorCode = 0;
    // 具体需要展现到UI的错误消息
    private String errormsg = "";

    public boolean isOK() {
        return isOK;
    }

    public void setOK(boolean isOK) {
        this.isOK = isOK;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        if (errorCode == -100) {
            errormsg = "请求地址为空";
        } else if (errorCode == -200) {
            errormsg = "服务器连接超时";
        } else if (errorCode == -300) {
            errormsg = "数据连接异常中断";
        }
        return errormsg;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
