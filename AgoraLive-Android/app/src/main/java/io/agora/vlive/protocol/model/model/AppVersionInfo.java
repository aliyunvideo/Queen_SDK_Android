package io.agora.vlive.protocol.model.model;

public class AppVersionInfo {
    public String appCode;
    int osType;
    int terminalType;
    String appVersion;
    String latestVersion;
    String appPackage;
    String upgradeDescription;
    int forcedUpgrade;
    String upgradeUrl;
    int reviewing;
    int remindTimes;
    public AppId config;


    public static class AppId {
        public String appId;
    }
}


