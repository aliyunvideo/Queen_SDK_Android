#pragma once

#include <map>
#include <string>
#include "TRTCCloudDef.h"
//��ֵ�Խṹ��
namespace Config {
    #define INI_ROOT_KEY L"TRTCDemo"
    #define INI_KEY_VIDEO_BITRATE L"INI_KEY_VIDEO_BITRATE"
    #define INI_KEY_VIDEO_RESOLUTION L"INI_KEY_VIDEO_RESOLUTION"
    #define INI_KEY_VIDEO_FPS L"INI_KEY_VIDEO_FPS"
    #define INI_KEY_VIDEO_QUALITY L"INI_KEY_VIDEO_QUALITY"
    #define INI_KEY_VIDEO_QUALITY_CONTROL L"INI_KEY_VIDEO_QUALITY_CONTROL"
    #define INI_KEY_SET_PUSH_SMALLVIDEO L"INI_KEY_SET_PUSH_SMALLVIDEO"
    #define INI_KEY_SET_PLAY_SMALLVIDEO L"INI_KEY_SET_PLAY_SMALLVIDEO"
    #define INI_KEY_SET_APP_SENSE L"INI_KEY_SET_APP_SENSE"
};

class SubNode
{
public:
    void InsertElement(std::wstring key, std::wstring value)
    {
        sub_node.insert(std::pair<std::wstring, std::wstring>(key, value));
    }
    std::map<std::wstring, std::wstring> sub_node;
};

//INI�ļ�������
class CConfigMgr
{
public:
    CConfigMgr();
    ~CConfigMgr();
public:
    std::wstring GetValue(std::wstring root, std::wstring key);			    //�ɸ����ͼ���ȡֵ
    bool SetValue(std::wstring root, std::wstring key, std::wstring value);	//���ø����ͼ���ȡֵ
    int GetSize() { return map_ini.size(); }
private:
    int WriteINI();			//д��INI�ļ�
    void Clear() { map_ini.clear(); }	//���
    void Travel();						//������ӡINI�ļ�
    int InitReadINI();
private:
    std::map<std::wstring, SubNode> map_ini;		//INI�ļ����ݵĴ洢����
    std::wstring _IncFilePath;                      //�ļ�·��
};

/*
* Module:   TRTCStorageConfigMgr
*
* Function: �洢�־û������ò���
*
*    1. ��TRTCSettingViewController���õĲ�����Ҫ�־û������ء�
*
*/
class TRTCStorageConfigMgr
{
public:
    static std::shared_ptr<TRTCStorageConfigMgr> GetInstance();
    TRTCStorageConfigMgr();
    ~TRTCStorageConfigMgr();
    void ReadStorageConfig();    //��ʼ��SDK��local������Ϣ
    void WriteStorageConfig();

public: //trtc 
    // ��Ƶ��������
    TRTCVideoEncParam videoEncParams;
    TRTCNetworkQosParam qosParams;
    TRTCAppScene appScene = TRTCAppSceneVideoCall;
    bool bPushSmallVideo = false; //��������˫����־��
    bool bPlaySmallVideo = false; //Ĭ����������Ƶ����־��
private:
    CConfigMgr* m_pConfigMgr;
};

