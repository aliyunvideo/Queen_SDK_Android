/*
* Module:   TRTCLoginViewController
*
* Function: �ý���������û�����һ��������š���һ�����û�����
*
* Notice:
*
*  ��1�������Ϊ�������ͣ��û���Ϊ�ַ�������
*
*  ��2������ʵ��ʹ�ó����У�����Ŵ�಻���û��ֶ�����ģ�����ϵͳ����ģ�
*       ������Ƶ�����еĻ�����ǻ��ϵͳ��ǰԤ���õģ��ͷ�ϵͳ�еķ����Ҳ�Ǹ��ݿͷ�Ա���Ĺ��ž����ġ�
*/

#include "stdafx.h"
#include "TRTCDemo.h"
#include "afxdialogex.h"
#include "StorageConfigMgr.h"
#include "TRTCLoginViewController.h"
#include "GenerateTestUserSig.h"
#include "TRTCMainViewController.h"
#include "util/Base.h"
// TRTCLoginViewController �Ի���

IMPLEMENT_DYNAMIC(TRTCLoginViewController, CDialogEx)

TRTCLoginViewController::TRTCLoginViewController(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_DIALOG_TRTC_LOGIN, pParent)
{

}

TRTCLoginViewController::~TRTCLoginViewController()
{

}

BOOL TRTCLoginViewController::OnInitDialog()
{
    CDialogEx::OnInitDialog();
    TRTCStorageConfigMgr::GetInstance()->ReadStorageConfig();
    newFont.CreatePointFont(120, L"΢���ź�");
    // ���ô˶Ի����ͼ�ꡣ  ��Ӧ�ó��������ڲ��ǶԻ���ʱ����ܽ��Զ�
    // ִ�д˲���
    //SetIcon(m_hIcon, TRUE);         // ���ô�ͼ��
    //SetIcon(m_hIcon, FALSE);        // ����Сͼ��

    int sdkAppID = GenerateTestUserSig::SDKAPPID;
    if (sdkAppID == 0)
    {
        CWnd *pEnterRoomBtn = GetDlgItem(IDC_ENTER_ROOM);
        pEnterRoomBtn->EnableWindow(FALSE);
        MessageBoxW(L"����Ҫ������Ѷ���˺���Ϣ�� GenerateTestUserSig.h ��������demo.", L"����", MB_OK);
        return FALSE;
    }

    CWnd *pStatic = GetDlgItem(IDC_STATIC_ROOM_ID);
    pStatic->SetWindowTextW(L"���䣺");
    pStatic->SetFont(&newFont);

    CWnd *pEditRoomId = GetDlgItem(IDC_EDIT_ROOM_ID);
    pEditRoomId->SetWindowTextW(L"901");
    pEditRoomId->SetFont(&newFont);

    CWnd *pBtnEnterRoom = GetDlgItem(IDC_ENTER_ROOM);
    pBtnEnterRoom->SetFont(&newFont);


    CWnd *pStaticUser = GetDlgItem(IDC_STATIC_USER_ID);
    pStaticUser->SetWindowTextW(L"�û���");
    pStaticUser->SetFont(&newFont);

    CWnd *pEditUserId = GetDlgItem(IDC_EDIT_USER_ID);
    pEditUserId->SetWindowTextW(L"TRTC_TEST_USER01");
    pEditUserId->SetFont(&newFont);

    CWnd *pEnterRoomBtn = GetDlgItem(IDC_ENTER_ROOM);
    pEnterRoomBtn->EnableWindow(TRUE);

    ShowWindow(SW_NORMAL);

    CRect rtDesk, rtDlg;
    ::GetWindowRect(::GetDesktopWindow(), &rtDesk);
    GetWindowRect(&rtDlg);
    int iXPos = rtDesk.Width() / 2 - rtDlg.Width() / 2;
    int iYPos = rtDesk.Height() / 2 - rtDlg.Height() / 2;
    SetWindowPos(NULL, iXPos, iYPos, 0, 0, SWP_NOOWNERZORDER | SWP_NOSIZE | SWP_NOZORDER);
    return TRUE;  // ���ǽ��������õ��ؼ������򷵻� TRUE
}

void TRTCLoginViewController::DoDataExchange(CDataExchange* pDX)
{
    CDialogEx::DoDataExchange(pDX);
}

void TRTCLoginViewController::OnCancel()
{
    destroyTRTCCloud();
    CDialogEx::OnCancel();
}

BEGIN_MESSAGE_MAP(TRTCLoginViewController, CDialogEx)
    ON_BN_CLICKED(IDC_ENTER_ROOM, &TRTCLoginViewController::OnBnClickedEnterRoom)
    ON_MESSAGE(WM_CUSTOM_CLOSE_MAINVIEW, OnMsgMainViewClose)
END_MESSAGE_MAP()


/**
*  Function: ��ȡ�û����룬������������룩����Ƶ����
*
*  �˶�ʾ����������Ҫ����������װ TRTC SDK ��������� TRTCParams
*
*  TRTCParams.sdkAppId => ��������Ѷ��ʵʱ����Ƶ����̨��https://console.cloud.tencent.com/rav����ȡ
*  TRTCParams.userId   => �˴����û�������û���������һ���ַ���
*  TRTCParams.roomId   => �˴����û����������Ƶ����ţ����� 125
*  TRTCParams.userSig  => �˴�ʾ������չʾ�����ֻ�ȡ usersig �ķ�ʽ��һ���Ǵӡ�����̨����ȡ��һ���Ǵӡ�����������ȡ
*
* ��1������̨��ȡ�����Ի�ü����Ѿ����ɺõ� userid �� usersig�����ǻᱻ����һ�� json ��ʽ�������ļ��У����ʺϵ���ʹ��
* ��2����������ȡ��ֱ���ڷ��������������ṩ��Դ���룬���� userid ʵʱ���� usersig�����ַ�ʽ��ȫ�ɿ����ʺ�����ʹ��
*
*  �ο��ĵ���https://cloud.tencent.com/document/product/647/17275
*/

void TRTCLoginViewController::joinRoom(int roomId)
{
    // �ӿ���̨��ȡ�� json �ļ��У��򵥻�ȡ�����Ѿ���ǰ����õ� userid �� usersig
    wchar_t buffer[MAX_PATH];
    CWnd *pEditUserID = GetDlgItem(IDC_EDIT_USER_ID);
    pEditUserID->GetWindowTextW(buffer, _countof(buffer));
    std::wstring strUserId = buffer;
    if (strUserId == L"")
    {
        MessageBoxW(L"�û�ID����Ϊ�գ�", L"����", MB_OK);
        return;
    }
    std::string userId = Wide2Ansi(strUserId);

    //�ӱ��ؼ��㷨������ȡ userid ��Ӧ�� usersig
    std::string userSig = GenerateTestUserSig::instance().genTestUserSig(userId);
    if (userSig == "")
    {
        //Ҳ����ͨ�� http Э����һ̨��������ȡ userid ��Ӧ�� usersig
        //ʾ����TRTCGetUserIDAndUserSig::instance().getUserSigFromServer();
        MessageBoxW(L"userSig ��ȡʧ�ܣ������Ƿ���д�˺���Ϣ��", L"����", MB_OK);
        return;
    }

    if (m_pTRTCMainViewController == nullptr)
    {
        m_pTRTCMainViewController = new TRTCMainViewController(this);
        m_pTRTCMainViewController->Create(IDD_TESTTRTCAPP_DIALOG, this);
        m_pTRTCMainViewController->ShowWindow(SW_SHOW);
    }
    
    std::string privateMapKey = "";
    TRTCParams params;
    params.sdkAppId = GenerateTestUserSig::SDKAPPID;
    params.roomId = roomId;//std::to_string(roomId).c_str();
    params.userId = userId.c_str();
    params.userSig = userSig.c_str();
    params.privateMapKey = privateMapKey.c_str();

    m_pTRTCMainViewController->enterRoom(params);

    ShowWindow(SW_HIDE);
}


void TRTCLoginViewController::OnBnClickedEnterRoom()
{
    wchar_t buffer[MAX_PATH] = { 0 };
    CWnd *pEdit = GetDlgItem(IDC_EDIT_ROOM_ID);
    pEdit->GetWindowTextW(buffer, _countof(buffer));
    std::wstring strRoomId = buffer;
    if (strRoomId.compare(L"") == 0)
    {
        MessageBoxW(L"����Ų���Ϊ�գ�", L"����", MB_OK);
        return;
    }
    uint32_t roomId = 0;
    ::swscanf_s(buffer, L"%lu", &roomId);

    joinRoom(roomId);
}

LRESULT TRTCLoginViewController::OnMsgMainViewClose(WPARAM wParam, LPARAM lParam)
{
    if (m_pTRTCMainViewController != nullptr)
    {
        m_pTRTCMainViewController->DestroyWindow();
        delete m_pTRTCMainViewController;
        m_pTRTCMainViewController = nullptr;
    }

    SetForegroundWindow();
    return LRESULT();
}

