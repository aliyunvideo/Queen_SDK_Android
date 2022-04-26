package im.zego.keycenter;

public final class KeyCenter {

    // Developers can get appID from admin console.
    // https://console.zego.im/dashboard
    // for example:
    //     private long _appID = 123456789L;
    private long _appID = 123456789L;

    // Developers should customize a user ID.
    // for example:
    //     private String _userID = "zego_benjamin";
    private String _userID = "zego_benjamin";

    // Developers can get token from admin console.
    // https://console.zego.im/dashboard
    // Note: The user ID used to generate the token needs to be the same as the userID filled in above!
    // for example:
    //     private String _token = "04AAAAAxxxxxxxxxxxxxx";
    private String _token = "";

    private static KeyCenter instance = new KeyCenter();
    private KeyCenter() {}

    public static KeyCenter getInstance() {
        return instance;
    }

    public long getAppID() {
        return _appID;
    }

    public void setAppID(long appID) {
        _appID = appID;
    }

    public String getUserID() {
        return _userID;
    }

    public void setUserID(String userID) {
        _userID = userID;
    }

    public String getToken() {
        return _token;
    }

    public void setToken(String token) {
        _token = token;
    }

}
