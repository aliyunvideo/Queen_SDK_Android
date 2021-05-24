package com.alilive.alilivesdk_demo.bean;

public class MusicBean {

    /**
     * 路径
     */
    private String path;
    /**
     * name
     */
    private String name;

    /**
     * 本地音乐
     */
    private boolean isLocal;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
