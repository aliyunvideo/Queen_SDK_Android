#pragma once
#include "afxwin.h"

/*
* Module:   TRTCLoginViewController
*
* Function: �ý���������û�����һ��������š���һ�����û�����
*
* Notice:
*
*  ��1�������Ϊ�������ͣ��û���Ϊ�ַ�������
*
*  ��2������ʵ��ʹ�ó����У�����Ŵ�಻���û��ֶ�����ģ������ɺ�̨ҵ�������ֱ�ӷ���ģ�
*       ������Ƶ�����еĻ�����ǻ��ϵͳ��ǰԤ���õģ��ͷ�ϵͳ�еķ����Ҳ�Ǹ��ݿͷ�Ա���Ĺ��ž����ġ�
*/


// TRTCLoginViewController �Ի���
class TRTCMainViewController;
class TRTCLoginViewController : public CDialogEx
{
	DECLARE_DYNAMIC(TRTCLoginViewController)

public:
	TRTCLoginViewController(CWnd* pParent = NULL);   // ��׼���캯��
	virtual ~TRTCLoginViewController();

// �Ի�������
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_DIALOG_TRTC_LOGIN };
#endif
public:
    //���뷿��
    void joinRoom(int roomId);
    
protected:
    virtual BOOL OnInitDialog();
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV ֧��
    virtual void OnCancel();
	DECLARE_MESSAGE_MAP()
protected:
    afx_msg void OnBnClickedEnterRoom();
    afx_msg LRESULT OnMsgMainViewClose(WPARAM wParam, LPARAM lParam);
private:
    CFont newFont;
    TRTCMainViewController * m_pTRTCMainViewController = nullptr;
};
