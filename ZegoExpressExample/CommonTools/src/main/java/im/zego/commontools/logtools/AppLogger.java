package im.zego.commontools.logtools;

import android.util.Log;

/**
 * Created by cc on 2019/03/08.
 * <p>
 * AppLogger
 * <p>
 * Manage Log
 * 管理app日志
 * Update log to view
 * 输出日志到视图。
 * <p>
 * Usage:
 * 用法如下:
 * <p>
 * AppLogger.getInstance().i(AppLogger.class, "test out info log");
 * <p>
 * AppLogger.getInstance().e(AppLogger.class, "test out error log");
 * <p>
 * AppLogger.getInstance().w(AppLogger.class, "test out warn log");
 * <p>
 * AppLogger.getInstance().d(AppLogger.class, "test out debug log");
 */
public class AppLogger {

    private static final String TAG = "AppLogger";
    LogAdapter logAdapter = LogAdapter.get();
    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    static private AppLogger sInstance;

    static public AppLogger getInstance() {
        if (sInstance == null) {
            synchronized (AppLogger.class) {
                if (sInstance == null) {
                    sInstance = new AppLogger();
                }
            }
        }
        return sInstance;
    }
    public void callApi(String msgFormat, Object... args){
        String message = String.format(msgFormat, args);
        message = getEmojiStringByUnicode(0x1F680)+message;
        log(message, LogLevel.INFO);
    }
    public void receiveCallback(String msgFormat, Object... args){
        String message = String.format(msgFormat, args);
        message = getEmojiStringByUnicode(0x1F4E9)+message;
        log(message, LogLevel.INFO);
    }
    public void success(String msgFormat, Object... args){
        String message = String.format(msgFormat, args);
        message = getEmojiStringByUnicode(0x2705)+message;
        log(message, LogLevel.INFO);
    }
    public void fail(String msgFormat, Object... args){
        String message = String.format(msgFormat, args);
        message = getEmojiStringByUnicode(0x274C)+message;
        log(message, LogLevel.ERROR);
    }
    public void e(String msgFormat, Object... args) {
        String message = String.format(msgFormat, args);
        log(message, LogLevel.ERROR);
    }

    public void i(String msgFormat, Object... args) {
        String message = String.format(msgFormat, args);
        log(message, LogLevel.INFO);
    }

    public void w(String msgFormat, Object... args) {
        String message = String.format(msgFormat, args);
        log(message, LogLevel.WARN);
    }

    public void d(String msgFormat, Object... args) {
        String message = String.format(msgFormat, args);
        log(message, LogLevel.DEBUG);
    }

    private void log(String message, LogLevel logLevel) {
        String message_with_time = String.format("[ %s ][ %s ] %s", TimeUtil.getLogStr(), logLevel.name(), message);
        switch (logLevel) {
            case INFO:
                Log.i(TAG, message_with_time);
                break;
            case WARN:
                Log.w(TAG, message_with_time);
                break;
            case DEBUG:
                Log.d(TAG, message_with_time);
                break;
            case ERROR:
                Log.d(TAG, message_with_time);
                break;
        }
        logAdapter.addLog(message);
    }
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
    public void clearLog() {
        logAdapter.clear();
    }
}
