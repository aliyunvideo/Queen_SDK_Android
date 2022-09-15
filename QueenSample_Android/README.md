# QueenSample_Android

[简介]</br>
智能美化特效已集成到阿里云Queen SDK中，接入简单、快速。基于智能视觉算法、海量规模的人脸、人体检测和识别技术，智能美化特效为视频创作者提供移动端的人脸美颜、美型、美妆美化、滤镜贴纸等编辑加工能力，满足直播和视频制作时的美颜特效需求。

技术优势
自研的人脸关键点定位技术，涵盖106个基础点位、280个高精度点位，效果真实。
持续优化的智能视觉算法和实时渲染技术，保证自然流畅的使用体验。
持续升级的美颜美型、滤镜贴纸玩法，不断扩充的素材库，创造更多乐趣。
完善的开发者支持，快速响应客户需求，提供优质可靠的服务。

更多详情与功能体验介绍,参见:
https://help.aliyun.com/document_detail/211049.html


[项目说明]</br>
本项目Demo来自阿里云智能美化特效官方Demo,用于演示最简化接入Queen-sdk的Demo使用过程,官方下载地址为:
https://help.aliyun.com/document_detail/211050.html

本项目旨在，介绍接入Queen-sdk的最简接入方式。
项目大概分为两大部分:</br>
第一部分，MainActivity+Camera+utils+view，组成本项目界面展示的基本结构，和接入过程几乎无关，可简单理解项目运行即可。</br>
第二部分，为CameraV1/CameraV2/CameraV3/这三种代表不同接入方式的渲染处理类。</br>
以纹理方式接入的，可参考CameraV1TextureRenderer的使用；</br>
以buffer方式接入的，可参考CameraV2BufferRenderer的使用;</br>
以纹理方式渲染，同时能方便获取当前纹理所对应buffer的，可参考CameraV3TextureAndBufferRenderer的使用。该种方式效率最高，纹理只做渲染，buffer直接给算法进行运算。对比v1，减少了从纹理导出buffer的过程，对比v2，减少了要把buffer渲染到纹理的过程，虽然这两个过程本身也耗时不长，但总体v3效率最高。</br>


[补充说明]</br>
注:第三方sdk中Demo代码均以第三方官方下载版本Demo为准,相关所有版权均归第三方版本所有,本项目不进行Demo内部的结构变更与功能修改,本项目也不负责维护与更新第三方sdk的Demo.此处各Demo仅用于代码接入展示,不用于其他任何商业途径,一切以Demo本身所属公司或组织为准.
