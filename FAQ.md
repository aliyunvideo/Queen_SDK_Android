# Queen接入问题分析
## 问题
### 美颜不生效或者黑屏
##### 是否LIcense有问题呢？
启动快速接入包的预览画面，看一下效果是否正常
- 如果正常，那说明证书没有问题；
- 如果不正常，那先确认接入的sdk版本是否对齐License申请的版本
    - 如果版本一致，License不生效，在钉钉技术群咨询
##### 是否每次调用渲染的线程不一致
如果是这个原因，那么通过logcat过滤“QueenDetector”，可以看到提示“<font color=red>调用render的线程与QueenEngine初始化不一致，没有OpenGL上下文，所以渲染会出错或者崩溃</font>”。
##### 是否调用创建QueenEngine的时候，没有OpenGL上下文
如果是这个原因，那么通过logcat过滤“QueenDetector”，可以看到提示“当前线程没有发现OpenGL上下文。解决方案1：在有OpenGL上下文的线程下创建QueenEngine；解决方案2：withContext参数设置为true，QueenEngine会负责创建OpenGL上下文”。
##### 是否设置的纹理有问题
1、如果设置的inputTexture是非法的，那么通过logcat过滤“QueenDetector”，可以看到提示“输入的纹理在当前的OpenGL上下文中非法，请确保输入纹理在当前的OpenGL上下文创建”。
2、设置的就是一张纯色的纹理？
- 在调试窗口显示“渲染输入纹理inputTexture”看看纹理的显示是否符合预期
### 妆容、贴纸对不上人脸
##### 图像格式和长宽是否有问题
通过updateInputDataAndRunAlg或者updateInputTextureBufferAndRunAlg方法设置的图像格式和长宽，需要检查一下。
在调试窗口显示“算法输入图像algInputData”，调整正确的图像格式和长宽，直至符合图像显示预期为止。
注：如果无法判断怎样是符合预期，可以打开快速预览界面看一下正确的情况。

![image.png](https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/IMG/alg_inputdata.png)
##### inputAngle、flip是否有问题
通过updateInputDataAndRunAlg或者updateInputTextureBufferAndRunAlg方法设置的inputAngle和flipAxis，需要检查一下。
在调试窗口显示“算法输入图像经过inputAngle、flip处理”，参考官方Sample设置参数，直至图像的方向向上为止。
注：如果无法判断怎样是图像方向向上，可以打开快速预览界面看一下正确的情况。

![image.png](https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/IMG/alginput_angle_flip.png)
##### outAngle是否有问题
通过updateInputDataAndRunAlg或者updateInputTextureBufferAndRunAlg方法设置的outAngle，需要检查一下。
在调试窗口显示“算法输入图像经过inputAngle、flip、outAngle处理”，参考官方Sample设置参数，直至图像跟预览图像方向一致为止。
注：如果无法判断怎样是图像方向一致，可以打开快速预览界面看一下正确的情况。

![image.png](https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/IMG/alginput_angle_flip_outangle.png)
### 图像变形
##### 图像拉伸
纹理大小与显示控件分辨率不一致导致，通过QueenEngine#setScreenViewport设置为窗口的分辨率即可，具体参考官方Sample。
##### 预览画面方向不正确？
首先要知道一个概念，在调用QueenEngine#autoGenOutTexture的时候参数keepInputDirection，是用于控制outTexture是否要跟inputTexure保持方向一致的。
1、如果keepInputDirection为true，那么在调试窗口显示“渲染输入纹理inputTexture”和“渲染输出纹理outTexture”，它们的方向是一致的，参考官方Sample将纹理渲染到视窗中即可。

![image.png](https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/IMG/in_out_texture.png)

2、如果keepInputDirection为false，那么在调试窗口显示“渲染输出纹理outTexture”；画面方向不对，那么参考官方Sample如何获取正确的transformMatrix传入即可。

![image.png](https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/IMG/outtexture.png)

### 如何做图片美颜
参考以下代码，如果处理的Bitmap大小变化，需要销毁QueenEngine，创建新的QueenEngine处理图片。
```JAVA
public void ensureEngine() {
  QueenConfig config = new QueenConfig();
  config.withContext = withContext;
  config.toScreen = toScreen;
  mQueenEngine = new QueenEngine(context, config);
}
​
public void releaseEngine() {
  if (null != mQueenEngine) {
    mQueenEngine.release();
    mQueenEngine = null;
  }
  if (mOutTexture2D != null) {
    mOutTexture2D.release();
    mOutTexture2D = null;
  }
}
​
/**
 * 建议创建一个HandlerThread来处理美颜
 **/
public void handleBitmap(Bitmap inBmp) {
    if (/*上次处理的bitmap大小变化*/) {
    releaseEngine();
  }
  ensureEngine();
  mQueenEngine.setInputBitMap(inBmp);
  if (null == mOutTexture2D) {
    mOutTexture2D = mQueenEngine.autoGenOutTexture();
  }
  mQueenEngine.setSegmentInfoFlipY(false);
    
  // TODO 设置美颜参数，至少设置一个功能，否则渲染会报错
  mQueenEngine.enableBeautyType(BeautyFilterType.kSkinBuffing, true);//磨皮开关
  mQueenEngine.setBeautyParam(BeautyParams.kBPSkinBuffing, 0.6);  //磨皮 [0,1]
  mQueenEngine.setBeautyParam(BeautyParams.kBPSkinSharpen, 0.2);  //锐化 [0,1]
​
  mQueenEngine.updateInputDataAndRunAlg(inBmp);
​
  @QueenResult int retCode = mQueenEngine.render();
  if (retCode == QueenResult.QUEEN_OK) {
    // TODO 这是美颜后的图片
    renderBmp = mOutTexture2D.readToBitmap();
  } else {
    // TODO 渲染失败，具体原因看本文开头，可能是License无效
    renderBmp = inBmp;
  }
}
```

## 参考
官方Sample地址：https://github.com/aliyunvideo/Queen_SDK_Android/tree/main/QueenSample_Android
快速接入包接入地址：https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/QuickImportPackage.md
