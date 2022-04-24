# Queen_SDK_Android
***[目录说明]***</br>
本工程接入实现了市面上绝大多数主流直播sdk厂商，以分支的方式进行管理，便于客户对照接入，请根据各自需要，选择并切换相应厂商分支。</br>
1、checkout代码到本地：git checkout 本git仓库地址</br>
2、切换到指定sdk分支：git checkout 分支名称 （见下表）</br>

支持厂商列表对应分支名称如下：</br>
===sdk厂商: 分支名称</br>
【官方接入指导】：Queen_SampleApp</br>
【阿里云直播推流SDK】：Queen_Aliyun_LivePusher</br>
[【腾讯云直播SDK】](https://github.com/LiteAVSDK/Live_Android/tree/main/MLVB-API-Example)：Queen_Tencent_MLVB</br>
[【腾讯云RTCSDK】](https://github.com/tencentyun/TRTCSDK)：Queen_Tencent_TRTC</br>
[【七牛云直播推流SDK】](https://developer.qiniu.com/pili/3718/PLDroidMediaStreaming-quick-start)：Queen_Qiniu_PLDroidMediaStreaming</br>
[【即构实时音视频SDK】](https://doc-zh.zego.im/article/3125)：Queen_Zego_ExpressExample</br>




</br>
====================================================================================
</br>

</br>
***[简介]***</br>
智能美化特效已集成到阿里云Queen SDK中，接入简单、快速。基于智能视觉算法、海量规模的人脸、人体检测和识别技术，智能美化特效为视频创作者提供移动端的人脸美颜、美型、美妆美化、滤镜贴纸等编辑加工能力，满足直播和视频制作时的美颜特效需求。

***[技术优势]***</br>
自研的人脸关键点定位技术，涵盖106个基础点位、280个高精度点位，效果真实。
持续优化的智能视觉算法和实时渲染技术，保证自然流畅的使用体验。
持续升级的美颜美型、滤镜贴纸玩法，不断扩充的素材库，创造更多乐趣。
完善的开发者支持，快速响应客户需求，提供优质可靠的服务。

更多详情与功能体验介绍,参见:
https://help.aliyun.com/document_detail/211047.html

***[FAQ]***</br>
常见问题，详情参见：
[FAQ](https://github.com/aliyunvideo/Queen_SDK_Android/blob/main/FAQ.md "Queen使用FAQ")


***[项目说明]***</br>
本项目,以市面常见的第三方sdk官方版本Demo为例,按照Queen-SDK的官方接入方法,示范实际各场景下的具体接入形式,以达到帮助客户快速理解与掌握Queen-SDK的接入方法的目的.

通过阅读本文，您可以根据自身产品项目需要,快速了解如何在第三方sdk中快速接入Queen-sdk渲染处理与计算库。

所有高级功能所需资源素材,请从本工程中assets文件夹进行拷贝

当前项目分类结构与说明如下:</br>
[QueenSample_Android]:Queen-SDK团队官方推荐的最简化接入示范代码,所有高级功能所需资源素材,请从本工程中assets文件夹进行拷贝</br>
[AliLiveSDK4.0.2_Demo_Android]:阿里云视频直播sdk</br>
[PLDroidMediaStreamingDemo]:七牛直播云sdk</br>
[TRTCSDK]:腾讯实时音视频sdk</br>
[librestreaming]:实时滤镜RTMP推流sdk</br>
[声网sdk]://TODO</br>



***[补充说明]***</br>
注:第三方sdk中Demo代码均以第三方官方下载版本Demo为准,相关所有版权均归第三方版本所有,本项目不进行Demo内部的结构变更与功能修改,本项目也不负责维护与更新第三方sdk的Demo.此处各Demo仅用于代码接入展示,不用于其他任何商业途径,一切以Demo本身所属公司或组织为准.
