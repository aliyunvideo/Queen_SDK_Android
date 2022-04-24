## 前言
本开源工程介绍了，如何在腾讯云直播SDK中，接入Queen美颜SDK。

## 前提条件
1. 已注册好腾讯账号
2. 已申请好Queen-SDK的License授权，申请方式参见[License申请](https://help.aliyun.com/document_detail/312036.html)

## 环境配置
### 配置LicenseKey
1. 使用 Android Studio（3.5及以上的版本）打开源码工程`MLVB-API-Example`
2. 找到子工程Advanced/ThirdBeauty，将前提条件2中申请到的License-key文件，xxx.crt文件放到assets目录下，若不存在，则在src/main下新建assets目录。
3. 找到子工程Advanced/ThirdBeauty，打开AndroidManifest.xml文件，在application标签下，配置好Licensekey信息，如下：
```
    //元数据项指定值，请填入您邮件获取到的LicenseKey
    <meta-data
         android:name="com.aliyun.alivc_license.licensekey"   //元数据项名字，固定取值
         android:value="Your LicenseKey"/>
         
    //元数据项指定值，请填入证书文件在工程中相对assets的路径，例如alivc_license/AliVideoCert.crt
    <meta-data
       android:name="com.aliyun.alivc_license.licensefile"
       android:value="Your LicenseFile Path"/>
 ```
### 配置gradle
1. 请确保在App主工程中的build.gradle文件中，已配置signingConfigs，且签名文件为申请License时的签名文件。
2. 在Advanced/ThirdBeauty子工程下的build.gradle文件中，新增如下SDK声明：
```
    // 此处修改：根据自身项目需要，指定依赖的菜单组件sdk
    implementation 'com.aliyunsdk.components:queen_menu:2.0.1.full-beta1'
    // 此处修改：根据自身项目需要，指定依赖的sdk版本
    implementation 'com.aliyun.maliang.android:queen:2.0.0-official-full'
```
## 代码修改
1. 在Advanced/ThirdBeauty子工程下，新建一个第三方跳转Activity（ThirdQueenBeautyActivity），并在AndroidManifest.xml及响应跳转中声明和实现该Activity的跳转；
2. 拷贝com.aliyun.queen目录到Advanced/ThirdBeauty子工程中；
3. new QueenRender对象mQueenRenderer，并在onGLContextCreated、onProcessVideoFrame、onGLContextDestroyed三个接口回调中，分别调用QueenRender的三个相应方法即可，例如：
```
            @Override
            public void onGLContextCreated() {
                mQueenRenderer.onTextureCreate(getApplicationContext());
            }

            @Override
            public int onProcessVideoFrame(V2TXLiveDef.V2TXLiveVideoFrame srcFrame, V2TXLiveDef.V2TXLiveVideoFrame dstFrame) {
                dstFrame.texture.textureId = mQueenRenderer.onTextureProcess(srcFrame.texture.textureId, flipMatrix, srcFrame.width, srcFrame.height);
                return 0;
            }

            @Override
            public void onGLContextDestroyed() {
                mQueenRenderer.onTextureDestroy();
            }
```
