package cn.redcdn.hvs.appinstall;

public abstract class InstallCallBackListerner {
  
  public abstract void needForcedInstall();

  public abstract void needOptimizationInstall();
  
  public abstract void noNeedInstall();
  
  public abstract void errorCondition(int error);

}
