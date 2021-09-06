# Queen_SDK_Android


[Q]:接入Queen后，画面纯色闪烁，移动手机后，画面颜色变化，但仍然没有画面？
出现条件：engine的构造参数toScreen为true，input纹理为oes纹理，render采用无参render()。
[A]:render()改为renderTexture(matrix)
如果输入纹理是oes的，则需要使用renderTexture，其中参数matrix是需要从surfaceTexture中获取的。


[Q]: 启动后闪退？
[A]:检查engine.setInputTexture中的textureId是否正确？


[Q]：贴纸/美妆对，人脸像横屏，呈90度，横屏后，人脸不可识别？
出现条件：engine的构造参数toScreen为true，input纹理为oes纹理，render采用无参render()，且算法运算采用取帧方案，而非bytebuffer方案。
示范效果：
<image>
<image>

用bytebuffer方案，人脸能识别，但因为w-h的原因，被错误拉大或拉窄
 


[Q]：基础美颜有效，高级美颜/美妆/贴纸无效？
[A]：基础美颜有效，说明Queen-engine的初始化/设参/渲染流程是通的，高级美颜无效，通常是由于设置参数错误，导致识别不了人脸，从而导致一切需要人脸关键点的效果均失效。常见的，可能设置错误的参数，主要有三个方面：
1.设置纹理输入时宽高参数错误，如接口：setInputTexture(int texture, int width, int height, boolean isOES)
其中，width/height必须是当前texture的显示宽高大小，需与texture参数匹配。第四个参数isOES直接决定了texture是否是一个oes纹理（Android特有），是否oes纹理，决定了Queen-engine内部是否需要做纹理显示转换（对应需要render时传入当前相机采集时正确的matrix）。此处的宽/高直接决定了显示画面渲染的宽高比，从而可能影响到高级功能的效果在具体渲染时的画面缩放比。常见的影响效果是，高级功能效果可用，但尺寸被拉大且变形。

2.设置输入数据时的宽-高参数错误，如接口updateInputDataAndRunAlg(byte[] imageData, int format, int width, int height, int stride, int inputAngle, int outAngle, int flipAxis, boolean reuseData)
其中的width/height，指示的是当前输入数据imageData的实际宽/高，它和上面第1个问题中的宽/高，可能是相同参数，也可能是相反参数（如Android 端直接从相机里获取到的buffer都是旋转90度的，宽-高刚好相反）。此处的宽/高直接决定了人脸头像是否能正常识别，进而直接影响功能是否可用。

3.设置输入数据时的输入角度/输出角度/xy轴旋转等参数错误，如接口updateInputDataAndRunAlg(byte[] imageData, int format, int width, int height, int stride, int inputAngle, int outAngle, int flipAxis, boolean reuseData)/或者帧同步接口updateInputTextureBufferAndRunAlg(int inputAngle, int outAngle, int flipAxis, boolean usePreviousFrame)
其中的，inputAngle/outAngle/flipAxis，都是高级功能中人脸识别所必需的参数。
参数inputAngle决定了算法如何使用传入的数据或纹理，是否需要旋转，旋转多少角度;
参数outAngle决定了算法如何将识别结果进行渲染显示，是否需要旋转，旋转多少角度;
参数flipAxis，是个枚举值，Queen-engine内有定义，决定了最终渲染画面是否需要对称翻转，沿x轴还是y轴翻转;
上述几个参数，对算法识别特别关键，且又和当前相机角度/前后摄像头密切相关，为便于处理，特地封装与整理了相关处理到工具类QueenCameraHelper.java中，各应用可在此基础上直接或适当调整使用。

