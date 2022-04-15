# Queen快速接入包使用文档
## 概述
快速接入包用于提升Queen的接入效率。引入最新版本可以保证使用的是最新的素材，同时提供常见接入问题的调试能力和方法。

**注意：Queen原有的集成方式不变，快速接入包只是在Queen的上层封装了快速预览效果和问题分析的能力。**

## 接入
### 集成
1、在项目级build.gradle项目文件中添加阿里云Maven仓库。
```Groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://maven.aliyun.com/repository/releases" }
    }
}
```
2、在相应Android端SDK包的应用级build.gradle项目文件下，加入依赖项。
```Groovy
implementation 'com.aliyun.maliang.android:queen_quick_import_package:2.0.0'
```
3、在应用的AndroidManifest.xml添加声明。
```XML
<!-- 预览界面所需权限 BEGIN -->
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<!-- 预览界面所需权限 END -->

<!-- 调试界面悬浮窗权限 BEGIN -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<!-- 调试界面悬浮窗权限 END -->

<application>
  <!-- 预览界面 -->
  <activity android:name="com.aliyun.maliang.android.simpleapp.MainActivity"
            android:theme="@style/Theme.AppCompat.NoActionBar"
            android:configChanges="keyboardHidden|screenSize">
  </activity>

  <!-- License界面 -->
  <activity android:name="com.aliyun.maliang.android.quick.QuickLicenseActivity"
            android:theme="@style/Theme.AppCompat.NoActionBar">
  </activity>
</application>
```

### 使用示例
##### 启动预览界面
如果License已经申请成功，启动预览界面之后会打开摄像头，并且配置了接入版本相应的美颜效果（如lite版本的磨皮美白等）。
```JAVA
QueenImportHelper.launchPreview(activity);
```
##### 显示License信息
可以看到当前的包名、签名以及当前接入版本的License过期时间。
包名、签名可以直接用于License申请邮件中。
```JAVA
QueenImportHelper.showLicenseInfo(activity);
```
##### 接入问题调试 
```JAVA
// 需要保证在创建QueenEngine之前调用，进行接入使用的分析
QueenImportHelper.initDetector(activity);

// 6.0以上机器会弹出悬浮窗权限申请窗口，需要打开才能显示调试窗口
// 调试窗口可以显示多种调试帧
QueenImportHelper.showDebugLayer(activity);
```
1、执行上述代码之后，可以通过logcat过滤“QueenDetector”，查看具体的接入信息；

2、打开调试窗口之后，可以通过“加图像”按钮，查看不同的调试帧。

使用方法参考文档：https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/FAQ.md

![image.png](https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/IMG/in_out_texture.png)
