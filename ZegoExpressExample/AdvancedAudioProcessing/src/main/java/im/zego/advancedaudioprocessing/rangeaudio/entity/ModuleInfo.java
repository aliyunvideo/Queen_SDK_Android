package im.zego.advancedaudioprocessing.rangeaudio.entity;

/**
 * Created by zego on 2018/10/16.
 */

public class ModuleInfo {

    private String titleName;
    private String contentName;

    public String getTitleName() {
        return titleName;
    }

    public String getContentName() { return contentName; }


    public ModuleInfo titleName(String titleName) {
        this.titleName = titleName;
        return this;
    }

    public ModuleInfo contentName(String content) {
        this.contentName = content;
        return this;
    }

}
