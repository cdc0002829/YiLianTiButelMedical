package cn.redcdn.hvs.appinstall;

import java.io.Serializable;

public class DownLoadInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  private String hashCode;
  private String path;
  private String version;
  private int complet;
  private int size;
  private String url;
  private String chagelist;

  public DownLoadInfo(String hashCode, String path, String version,
      int complet, int size, String url,String changelist) {
    super();
    this.hashCode = hashCode;
    this.path = path;
    this.version = version;
    this.complet = complet;
    this.size = size;
    this.url = url;
    this.chagelist=changelist;
  }

  public String getChagelist() {
	return chagelist;
}

public void setChagelist(String chagelist) {
	this.chagelist = chagelist;
}

public String getHashCode() {
    return hashCode;
  }

  public void setHashCode(String hashCode) {
    this.hashCode = hashCode;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getComplet() {
    return complet;
  }

  public void setComplet(int complet) {
    this.complet = complet;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "DownloadInfo [hashCode=" + hashCode + ", path=" + path
        + ", version=" + version + ", complet=" + complet + ", size=" + size
        + ", url=" + url + "]";
  }
}
