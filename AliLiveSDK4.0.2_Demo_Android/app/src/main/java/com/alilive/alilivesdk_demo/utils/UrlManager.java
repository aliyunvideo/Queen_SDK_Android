package com.alilive.alilivesdk_demo.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * data:2020-08-21
 */
public class UrlManager {
    private Map<String,UrlEntity> urlEntityMap;
    public UrlManager(){
        urlEntityMap = new HashMap<>();
        UrlEntity entity = new UrlEntity("artc://push.rtcdemo.grtn.aliyunlive.com/live/oushuandroid1?auth_key=99999999999-0-0-ddf42c49cfb7be56402a2655aec2291b",
                "artc://pull.rtcdemo.grtn.aliyunlive.com/live/oushuandroid1?auth_key=99999999999-0-0-ddf42c49cfb7be56402a2655aec2291b");
        urlEntityMap.put("artc:oushuandroid1",entity);

        UrlEntity entity1 = new UrlEntity("artc://push.rtcdemo.grtn.aliyunlive.com/live/oushuandroid2?auth_key=99999999999-0-0-e7c575f50606ec47f7590510b5a97a97",
                "artc://pull.rtcdemo.grtn.aliyunlive.com/live/oushuandroid2?auth_key=99999999999-0-0-e7c575f50606ec47f7590510b5a97a97");
        urlEntityMap.put("artc:oushuandroid2",entity1);

        UrlEntity entity2 = new UrlEntity("rtmp://push-demo-rtmp.aliyunlive.com/test/stream888?&auth_key=1603232455-0-0-1613feb3591a0a1dff1f1176a134e324",
                "rtmp://push-demo.aliyunlive.com/test/stream888?&auth_key=1603232455-0-0-bc512aa1ea24ea9d90744f77ce2a8cd4");
        urlEntityMap.put("rtmp:stream888",entity2);

        UrlEntity entity3 = new UrlEntity("artc://push.rtcdemo.grtn.aliyunlive.com/live/xiongjinshui?auth_key=99999999999-0-0-f94f6daa61012e0dc023c05a42986c15",
                "artc://pull.rtcdemo.grtn.aliyunlive.com/live/xiongjinshui?auth_key=99999999999-0-0-f94f6daa61012e0dc023c05a42986c15");
        urlEntityMap.put("artc:xiongjinshui",entity3);

        UrlEntity entity4 = new UrlEntity("artc://push.rtcdemo.grtn.aliyunlive.com/live/yujian1?auth_key=99999999999-0-0-798198712dbd1ec90af7dfbc7e5eb969",
                "artc://pull.rtcdemo.grtn.aliyunlive.com/live/yujian1?auth_key=99999999999-0-0-798198712dbd1ec90af7dfbc7e5eb969");
        urlEntityMap.put("artc:yujian1",entity4);

        UrlEntity entity5 = new UrlEntity("artc://push.rtcdemo.grtn.aliyunlive.com/live/yujian2?auth_key=99999999999-0-0-3b393ab4bb4bb3a9462cd4cd743d91f5",
                "artc://pull.rtcdemo.grtn.aliyunlive.com/live/yujian2?auth_key=99999999999-0-0-3b393ab4bb4bb3a9462cd4cd743d91f5");
        urlEntityMap.put("artc:yujian2",entity5);

        UrlEntity entity6 = new UrlEntity("artc://push.rtcdemo.grtn.aliyunlive.com/live/yujian3?auth_key=99999999999-0-0-d7d7e50631e5bb4a73e5184034bb0325",
                "artc://pull.rtcdemo.grtn.aliyunlive.com/live/yujian3?auth_key=99999999999-0-0-d7d7e50631e5bb4a73e5184034bb0325");
        urlEntityMap.put("artc:yujian3",entity6);

    }
    public String getPullUrl(String streamName){

        UrlEntity entity = urlEntityMap.get(streamName);
        if (entity!=null){
            return entity.getPullUrl();
        }else {
            return "";
        }

    }
    public String getPushUrl(String streamName){
        UrlEntity entity = urlEntityMap.get(streamName);
        if (entity!=null){
            return entity.getPushUrl();
        }else {
            return "";
        }
    }
    public List<String> getKeyList(){
        List<String> keyList = new ArrayList<>();
        Set<String> keys = urlEntityMap.keySet();
        Iterator<String> iterator1=keys.iterator();
        while (iterator1.hasNext()){
            keyList.add(iterator1.next());
        }
        return keyList;
    }

    /**
     * 添加推拉流地址对
     * @param key
     * @param entity
     */
    public void addUrlEntity(String key,UrlEntity entity){
        urlEntityMap.put(key, entity);
    }

    /**
     * 推拉流地址对
     */
    public static class UrlEntity{
        public UrlEntity(){

        }
        public UrlEntity(String pushUrl,String pullUrl){
            this.pushUrl = pushUrl;
            this.pullUrl = pullUrl;

        }
        private String pullUrl;
        private String pushUrl;

        public String getPullUrl() {
            return pullUrl;
        }

        public void setPullUrl(String pullUrl) {
            this.pullUrl = pullUrl;
        }

        public String getPushUrl() {
            return pushUrl;
        }

        public void setPushUrl(String pushUrl) {
            this.pushUrl = pushUrl;
        }
    }
}
