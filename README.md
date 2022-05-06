# Queen_Agora_LiveSDK

## [简介]</br>
智能美化特效已集成到即构实时音视频SDK中，接入简单、快速。基于智能视觉算法、海量规模的人脸、人体检测和识别技术，智能美化特效为视频创作者提供移动端的人脸美颜、美型、美妆美化、滤镜贴纸等编辑加工能力，满足直播和视频制作时的美颜特效需求。

技术优势 自研的人脸关键点定位技术，涵盖106个基础点位、280个高精度点位，效果真实。 持续优化的智能视觉算法和实时渲染技术，保证自然流畅的使用体验。 持续升级的美颜美型、滤镜贴纸玩法，不断扩充的素材库，创造更多乐趣。 完善的开发者支持，快速响应客户需求，提供优质可靠的服务。

更多详情与功能体验介绍,参见:[Queen美颜特效SDK官网](https://help.aliyun.com/document_detail/211049.html)

</br>

[补充说明]</br>
注:第三方sdk中Demo代码均以第三方官方下载版本Demo为准,相关所有版权均归第三方版本所有,本项目不进行Demo内部的结构变更与功能修改,本项目也不负责维护与更新第三方sdk的Demo.此处各Demo仅用于代码接入展示,不用于其他任何商业途径,一切以Demo本身所属公司或组织为准.


## 前言
1. 本开源工程介绍了，如何在即构实时音视频SDK中，接入Queen美颜SDK。本项目Demo来自即构官方Demo版本,Demo资源名为:[跑通示例源码
](https://doc-zh.zego.im/article/3125),官方下载地址为:[声网Agora Live 的github地址](https://doc-zh.zego.im/article/2969)
2. 本工程可直接本地运行，查看美颜效果，并配置好菜单，不用额外配置账号或授权信息

## 前提条件
1. 已申请好Queen-SDK的License授权，申请方式参见[License申请](https://help.aliyun.com/document_detail/312036.html)

## 环境配置
### 配置LicenseKey
1. 使用 Android Studio（3.5及以上的版本）打开源码工程`ZegoExpressExample`
2. 找到子工程AdvancedVideoProcessing，将前提条件2中申请到的License-key文件，xxx.crt文件放到assets目录下，若不存在，则在src/main下新建assets目录。
3. 找到子工程AdvancedVideoProcessing，打开AndroidManifest.xml文件，在application标签下，配置好Licensekey信息，如下：
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
1. 请确保在App主工程中的build.gradle文件中，已配置signingConfigs，且签名文件为申请License时的签名文件。（本工程已提供测试用的github_debug.jks的app开发证书）
2. 在主工程下的build.gradle文件中，新增如下SDK声明：
```
    // 此处修改：根据自身项目需要，指定依赖的sdk版本
    implementation 'com.aliyun.maliang.android:queen:2.0.0-official-full'
```
## 代码修改
1. 在子工程AdvancedVideoProcessing中，找到目录CustomerVideoCapture，相关代码修改均在该子模块中；
2. 在该目录下，新建一个queen子目录，所有实现均在该目录下，重点关注其中QueenRendererImpl类在VideoCaptureFromCamera的实现与使用全过程；
3. 因为即构工程使用了TextureView，而TextureView本身是不带gl上下文环境的，不能直接使用（任何美颜sdk都是需依赖gl上下文环境的）。因此，这里实现了一个GLESTextureView类，即带gl环境的TextureView，使用方式与传统TextureView完全一致，需注意的是在对应的几个回调接口方法中。
4. 关键方法修改，在类VideoCaptureFromCamera中，对应如下四个接口回调中，分别调用QueenRender的相应方法即可，例如：
```
        @Override
        public void onSurfaceCreatedGL() {
            startCapture();

            mQueenRender = new QueenRender.Builder().setDraw2Screen(true).build();
            if (mQueenRender != null) {
                mQueenRender.onTextureCreate(mTextureView.getContext());
            }
        }

        @Override
        public void onSurfaceChangedGL(int width, int height) {
            if (mQueenRender != null)
                mQueenRender.onTextureSizeChanged(0, 0, width, height);
        }

        @Override
        public void onSurfaceDestroyGL() {
            if (mQueenRender != null)
                mQueenRender.onTextureDestroy();
        }

        @Override
        public int onDrawFramGL(byte[] data, float[] matrix) {
        // 具体绘制流程
            int oesTextureId = mTextureView.getPreviewOESTextureId();
            int ret = mQueenRender.onTextureProcess(oesTextureId, true, matrix, mWidth, mHeight);

            final ZegoExpressEngine zegoExpressEngine = ZegoExpressEngine.getEngine();
            。。。
        }
```


