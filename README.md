
# Librestreaming

## [简介]</br>
智能美化特效已集成到第三方直播SDK-Librestreaming中，接入简单、快速。基于智能视觉算法、海量规模的人脸、人体检测和识别技术，智能美化特效为视频创作者提供移动端的人脸美颜、美型、美妆美化、滤镜贴纸等编辑加工能力，满足直播和视频制作时的美颜特效需求。

技术优势 自研的人脸关键点定位技术，涵盖106个基础点位、280个高精度点位，效果真实。 持续优化的智能视觉算法和实时渲染技术，保证自然流畅的使用体验。 持续升级的美颜美型、滤镜贴纸玩法，不断扩充的素材库，创造更多乐趣。 完善的开发者支持，快速响应客户需求，提供优质可靠的服务。

更多详情与功能体验介绍,参见:[Queen美颜特效SDK官网](https://help.aliyun.com/document_detail/211049.html)

</br>

[补充说明]</br>
注:第三方sdk中Demo代码均以第三方官方下载版本Demo为准,相关所有版权均归第三方版本所有,本项目不进行Demo内部的结构变更与功能修改,本项目也不负责维护与更新第三方sdk的Demo.此处各Demo仅用于代码接入展示,不用于其他任何商业途径,一切以Demo本身所属公司或组织为准.


## 前言
1. 本项目Demo来自实时滤镜Rtmp推流官方Demo,用于演示最简化接入Queen-sdk的Demo使用过程,[git下载地址](https://github.com/lakeinchina/librestreaming)
对应版本为最后提交日期:3 Aug 2020,[对应代码地址](https://github.com/lakeinchina/librestreaming/tree/d971dbd2b2b7ed6fa6c48d870607284fc0fdd7c3)

2. 本工程可直接本地运行，查看美颜效果，并配置好菜单，不用额外配置账号或授权信息

## 环境配置
### 配置LicenseKey
1. 使用 Android Studio（3.5及以上的版本）打开源码工程`PLDroidMediaStreamingDemo`
2. 找到主工程app，将前提条件2中申请到的License-key文件，xxx.crt文件放到assets目录下，若不存在，则在src/main下新建assets目录。
3. 找到主工程app，打开AndroidManifest.xml文件，在application标签下，配置好Licensekey信息，如下：
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
    // 此处修改：根据自身项目需要，指定依赖的菜单组件sdk
    implementation 'com.aliyunsdk.components:queen_menu:2.0.1.full-beta3'
    // 此处修改：根据自身项目需要，指定依赖的sdk版本
    implementation 'com.aliyun.maliang.android:queen:2.0.0-official-full'
```
## 代码修改
1. 所有修改均在 QueenGPUImageFilter.java文件中，并做修改。
