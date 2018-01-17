package cn.redcdn.hvs.im.util.xutils.http.client;

import org.apache.http.NameValuePair;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: wyouflf
 * Date: 13-7-26
 * Time: 下午4:21
 */
public class BodyParamsEntity extends AbstractHttpEntity implements Cloneable {

    protected byte[] content;

    private boolean dirty = true;

    private String charset = HTTP.UTF_8;

    private List<NameValuePair> params;

    public BodyParamsEntity() {
        this((String) null);
    }

    public BodyParamsEntity(String charset) {
        super();
        if (charset != null) {
            this.charset = charset;
        }
//        setContentType(HTTP.PLAIN_TEXT_TYPE + HTTP.CHARSET_PARAM + charset);
        setContentType(URLEncodedUtils.CONTENT_TYPE);
        params = new ArrayList<NameValuePair>();
    }

    public BodyParamsEntity(List<NameValuePair> params) {
        this(params, null);
    }

    public BodyParamsEntity(List<NameValuePair> params, String charset) {
        super();
        if (charset != null) {
            this.charset = charset;
        }
//        setContentType(HTTP.PLAIN_TEXT_TYPE + HTTP.CHARSET_PARAM + charset);
        setContentType(URLEncodedUtils.CONTENT_TYPE);
        this.params = params;
    }

    public BodyParamsEntity addParameter(String name, String value) {
        this.params.add(new BasicNameValuePair(name, value));
        this.dirty = true;
        return this;
    }

    public BodyParamsEntity addParams(List<NameValuePair> params) {
        this.params.addAll(params);
        this.dirty = true;
        return this;
    }

    private void refreshContent() {
        if (dirty) {
            try {
                this.content = URLEncodedUtils.format(params, charset).getBytes(charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            dirty = false;
        }
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        refreshContent();
        return this.content.length;
    }

    public InputStream getContent() throws IOException {
        refreshContent();
        return new ByteArrayInputStream(this.content);
    }

    public void writeTo(final OutputStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        refreshContent();
        outStream.write(this.content);
        outStream.flush();
    }

    /**
     * Tells that this entity is not streaming.
     *
     * @return <code>false</code>
     */
    public boolean isStreaming() {
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
