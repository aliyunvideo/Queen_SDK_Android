#include <iostream>
#include <fstream>
#include <sstream>
#include <cstdlib>
#include <vector>
#include <mutex>
#include "StorageConfigMgr.h"
#include "util/Base.h"

//#define INIDEBUG
//INI�ļ����洢�ṹ
class ININode
{
public:
    ININode(std::wstring root, std::wstring key, std::wstring value)
    {
        this->root = root;
        this->key = key;
        this->value = value;
    }
    std::wstring root;
    std::wstring key;
    std::wstring value;
};
//////////////////////////////////////////////////////////////////////////ININode

CConfigMgr::CConfigMgr()
{
    wchar_t szCurrentDirectory[MAX_PATH] = { 0 };
    DWORD dwCurDirPathLen;
    dwCurDirPathLen = GetModuleFileNameW(NULL, szCurrentDirectory, MAX_PATH);
    std::wstring appPath;
    appPath = szCurrentDirectory;
    int pos = appPath.find_last_of(L'\\');
    int size = appPath.size();
    _IncFilePath = appPath.erase(pos, size);
    _IncFilePath += L"\\TRTStorageConfig.ini";
    InitReadINI();
}

CConfigMgr::~CConfigMgr()
{
    WriteINI();
}

//************************************************************************
// ��������:    	TrimString
// ����Ȩ��:    	public 
// ��������:		2017/01/05
// �� �� ��:		
// ����˵��:		ȥ���ո�
// ��������: 	string & str	������ַ���
// �� �� ֵ:   	std::string &	����ַ���
//************************************************************************
std::string &TrimString(std::string &str)
{
    std::string::size_type pos = 0;
    while (str.npos != (pos = str.find(" ")))
        str = str.replace(pos, pos + 1, "");
    return str;
}

//************************************************************************
// ��������:    	ReadINI
// ����Ȩ��:    	public 
// ��������:		2017/01/05
// �� �� ��:		
// ����˵��:		��ȡINI�ļ��������䱣�浽map�ṹ��
// �� �� ֵ:   	int
//************************************************************************
int CConfigMgr::InitReadINI()
{
    std::ifstream in_conf_file(_IncFilePath.c_str());
    if (!in_conf_file) return 0;
    std::string str_line = "";
    std::string str_root = "";
    std::vector<ININode> vec_ini;
    while (getline(in_conf_file, str_line))
    {
        std::string::size_type left_pos = 0;
        std::string::size_type right_pos = 0;
        std::string::size_type equal_div_pos = 0;
        std::string str_key = "";
        std::string str_value = "";
        if ((str_line.npos != (left_pos = str_line.find("["))) && (str_line.npos != (right_pos = str_line.find("]"))))
        {
            //cout << str_line.substr(left_pos+1, right_pos-1) << endl;
            str_root = str_line.substr(left_pos + 1, right_pos - 1);
        }

        if (str_line.npos != (equal_div_pos = str_line.find("=")))
        {
            str_key = str_line.substr(0, equal_div_pos);
            str_value = str_line.substr(equal_div_pos + 1, str_line.size() - 1);
            //str_key = TrimString(str_key);
            //str_value = TrimString(str_value);
            //cout << str_key << "=" << str_value << endl;
        }

        if ((!str_root.empty()) && (!str_key.empty()) && (!str_value.empty()))
        {
            ININode ini_node(UTF82Wide(str_root), UTF82Wide(str_key), UTF82Wide(str_value));
            vec_ini.push_back(ini_node);
            //cout << vec_ini.size() << endl;
        }
    }
    in_conf_file.close();
    in_conf_file.clear();

    //vector convert to map
    std::map<std::wstring, std::wstring> map_tmp;
    for (std::vector<ININode>::iterator itr = vec_ini.begin(); itr != vec_ini.end(); ++itr)
    {
        map_tmp.insert(std::pair<std::wstring, std::wstring>(itr->root, L""));
    }	//��ȡ�����ڵ�
    for (std::map<std::wstring, std::wstring>::iterator itr = map_tmp.begin(); itr != map_tmp.end(); ++itr)
    {
#ifdef INIDEBUG
        cout << "���ڵ㣺 " << itr->first << endl;
#endif	//INIDEBUG
        SubNode sn;
        for (std::vector<ININode>::iterator sub_itr = vec_ini.begin(); sub_itr != vec_ini.end(); ++sub_itr)
        {
            if (sub_itr->root == itr->first)
            {
#ifdef INIDEBUG
                cout << "��ֵ�ԣ� " << sub_itr->key << "=" << sub_itr->value << endl;
#endif	//INIDEBUG
                sn.InsertElement(sub_itr->key, sub_itr->value);
            }
        }
        map_ini.insert(std::pair<std::wstring, SubNode>(itr->first, sn));
    }
    return 1;
}

//************************************************************************
// ��������:    	GetValue
// ����Ȩ��:    	public 
// ��������:		2017/01/05
// �� �� ��:		
// ����˵��:		���ݸ����ĸ����ͼ�ֵ�����������ֵ
// ��������: 	string root		������ĸ����
// ��������: 	string key		������ļ�
// �� �� ֵ:   	std::string		�������ֵ
//************************************************************************
std::wstring CConfigMgr::GetValue(std::wstring root, std::wstring key)
{
    std::map<std::wstring, SubNode>::iterator itr = map_ini.find(root);
    if (map_ini.end() == itr)
        return L"";

    std::map<std::wstring, std::wstring>::iterator sub_itr = itr->second.sub_node.find(key);
    if (itr->second.sub_node.end() == sub_itr)
        return L"";

    if (sub_itr->second.empty())
        return L"";

    return sub_itr->second;
}

//************************************************************************
// ��������:    	WriteINI
// ����Ȩ��:    	public 
// ��������:		2017/01/05
// �� �� ��:		
// ����˵��:    ����XML����Ϣ���ļ���
// ��������: 	string path	INI�ļ��ı���·��
// �� �� ֵ:   	int
//************************************************************************
int CConfigMgr::WriteINI()
{
    //...�ļ��򿪣�������Ϣ���ر��ļ��Ȳ�����
    std::wofstream out_conf_file(_IncFilePath.c_str());
    if (!out_conf_file)
        return -1;
    //cout << map_ini.size() << endl;
    for (std::map<std::wstring, SubNode>::iterator itr = map_ini.begin(); itr != map_ini.end(); ++itr)
    {
        //cout << itr->first << endl;
        out_conf_file << "[" << Wide2UTF8(itr->first).c_str() << "]" << std::endl;
        for (std::map<std::wstring, std::wstring>::iterator sub_itr = itr->second.sub_node.begin(); sub_itr != itr->second.sub_node.end(); ++sub_itr)
        {
            //cout << sub_itr->first << "=" << sub_itr->second << endl;
            out_conf_file << Wide2UTF8(sub_itr->first).c_str() << "=" << Wide2UTF8(sub_itr->second).c_str() << std::endl;
        }
    }
    out_conf_file.close();
    out_conf_file.clear();
    return 1;
}

//************************************************************************
// ��������:    	SetValue
// ����Ȩ��:    	public 
// ��������:		2017/01/05
// �� �� ��:		
// ����˵��:		�����������ֵ
// ��������: 	string root		������ĸ��ڵ�
// ��������: 	string key		������ļ�
// ��������: 	string value	�������ֵ
// �� �� ֵ:   	std::vector<ININode>::size_type	
//************************************************************************
bool CConfigMgr::SetValue(std::wstring root, std::wstring key, std::wstring value)
{
    std::map<std::wstring, SubNode>::iterator itr = map_ini.find(root);	//����
    if (map_ini.end() != itr)
    {
        itr->second.sub_node[key] = value;
    }	//���ڵ��Ѿ������ˣ�����ֵ
    else
    {
        SubNode sn;
        sn.InsertElement(key, value);
        map_ini.insert(std::pair<std::wstring, SubNode>(root, sn));
    }	//���ڵ㲻���ڣ����ֵ

    return true;
}

//************************************************************************
// ��������:    	Travel
// ����Ȩ��:    	public 
// ��������:		2017/01/05
// �� �� ��:		
// ����˵��:		������ӡINI�ļ�
// �� �� ֵ:   	void
//************************************************************************
void CConfigMgr::Travel()
{
    for (std::map<std::wstring, SubNode>::iterator itr = this->map_ini.begin(); itr != this->map_ini.end(); ++itr)
    {
        //root
        std::cout << L"[" << itr->first.c_str() << L"]" << std::endl;
        for (std::map<std::wstring, std::wstring>::iterator itr1 = itr->second.sub_node.begin(); itr1 != itr->second.sub_node.end();
            ++itr1)
        {
            std::cout << L"    " << itr1->first.c_str() << L" = " << itr1->second.c_str() << std::endl;
        }
    }
}

////////////////////////////////////////////////////////////////////////// TRTCStorageConfig
static std::shared_ptr<TRTCStorageConfigMgr> s_pInstance;
static std::mutex engine_mex;
std::shared_ptr<TRTCStorageConfigMgr> TRTCStorageConfigMgr::GetInstance()
{
    if (s_pInstance == NULL) {
        engine_mex.lock();
        if (s_pInstance == NULL)
        {
            s_pInstance = std::make_shared<TRTCStorageConfigMgr>();
        }
        engine_mex.unlock();
    }
    return s_pInstance;
}

TRTCStorageConfigMgr::TRTCStorageConfigMgr()
{
    m_pConfigMgr = new CConfigMgr;
    videoEncParams.videoResolution = TRTCVideoResolution_640_360;
    videoEncParams.videoBitrate = 500;
    videoEncParams.videoFps = 15;
}

TRTCStorageConfigMgr::~TRTCStorageConfigMgr()
{
    WriteStorageConfig();
    if (m_pConfigMgr)
    {
        delete  m_pConfigMgr;
        m_pConfigMgr = nullptr;
    }
}


void TRTCStorageConfigMgr::ReadStorageConfig()
{
    if (m_pConfigMgr == false || m_pConfigMgr->GetSize() == 0)
        return;
    //����Ƶ��������
    std::wstring strParam;
    strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_VIDEO_BITRATE);
    videoEncParams.videoBitrate = _wtoi(strParam.c_str());

    strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_VIDEO_RESOLUTION);
    videoEncParams.videoResolution = (TRTCVideoResolution)_wtoi(strParam.c_str());

    strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_VIDEO_FPS);
    videoEncParams.videoFps = _wtoi(strParam.c_str());

    strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_VIDEO_QUALITY);
    qosParams.preference = (TRTCVideoQosPreference)_wtoi(strParam.c_str());

    //strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_VIDEO_QUALITY_CONTROL);
    qosParams.controlMode = TRTCQosControlModeServer;

    strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_SET_PUSH_SMALLVIDEO);
    bPushSmallVideo = _wtoi(strParam.c_str());

    strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_SET_PLAY_SMALLVIDEO);
    bPlaySmallVideo = _wtoi(strParam.c_str());

    strParam = m_pConfigMgr->GetValue(INI_ROOT_KEY, INI_KEY_SET_APP_SENSE);
    appScene = (TRTCAppScene)_wtoi(strParam.c_str());
}

void TRTCStorageConfigMgr::WriteStorageConfig()
{
    //����Ƶ��������
    std::wstring strFormat;
    strFormat = format(L"%d", videoEncParams.videoBitrate);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_VIDEO_BITRATE, strFormat.c_str());

    strFormat = format(L"%d", videoEncParams.videoResolution);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_VIDEO_RESOLUTION, strFormat.c_str());

    strFormat = format(L"%d", videoEncParams.videoFps);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_VIDEO_FPS, strFormat.c_str());

    strFormat = format(L"%d", qosParams.preference);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_VIDEO_QUALITY, strFormat.c_str());

    strFormat = format(L"%d", qosParams.controlMode);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_VIDEO_QUALITY_CONTROL, strFormat.c_str());

    strFormat = format(L"%d", bPushSmallVideo);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_SET_PUSH_SMALLVIDEO, strFormat.c_str());

    strFormat = format(L"%d", bPlaySmallVideo);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_SET_PLAY_SMALLVIDEO, strFormat.c_str());

    strFormat = format(L"%d", appScene);
    m_pConfigMgr->SetValue(INI_ROOT_KEY, INI_KEY_SET_APP_SENSE, strFormat.c_str());
}
