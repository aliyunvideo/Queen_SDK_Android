本工程介绍了，如何在腾讯云直播SDK中，接入Queen美颜SDK。

原工程下载后，修改点如下：
1、在在Advanced/ThirdBeauty子工程下的build.gradle文件中，新增如下SDK声明：
// 此处修改：根据自身项目需要，指定依赖的菜单组件sdk
    implementation 'com.aliyunsdk.components:queen_menu:2.0.1.full-beta1'
    // 此处修改：根据自身项目需要，指定依赖的sdk版本
    implementation 'com.aliyun.maliang.android:queen:2.0.0-official-full'

2、在Advanced/ThirdBeauty子工程下，新建一个第三方跳转Activity（ThirdQueenBeautyActivity），并在AndroidManifest.xml及响应跳转中声明和实现该Activity的跳转；
3、拷贝com.aliyun.queen目录；
4、new QueenRender对象mQueenRenderer，并在onGLContextCreated、onProcessVideoFrame、onGLContextDestroyed三个接口回调中，分别调用QueenRender的三个相应方法即可；

