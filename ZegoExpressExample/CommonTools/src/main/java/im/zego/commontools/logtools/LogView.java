package im.zego.commontools.logtools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import im.zego.commontools.R;

public class LogView extends DialogFragment {

    private static final String LF = "\n";
    private RecyclerView recyclerView;
    public LogAdapter logAdapter;
    public Button shareButton;
    private TextView txCpuInfo, cpuClockFrequency;
    private String eachCpuInfo = "[ CPU\t%d\t\tCurrent frequency\t%s\t\tMax frequency\t%s\t\tMin frequency\t%s ]" + LF;
    private StringBuffer mCPUHeaderText;
    private int mCPUCoreNum;
    private List<String> cpuMaxList = new ArrayList<String>();
    private List<String> cpuMinList = new ArrayList<String>();
    RelativeLayout cpuInfoRelativeView;
    private View view;
    private Context context;
    private ImageView closeButton;
    Handler handler;
    Runnable runnable;

    public LogView(Context context){
        this.context = context;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //去掉dialog的标题，需要在setContentView()之前
        // remove title of dialog before setContentView();
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = this.getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        view = inflater.inflate(R.layout.log_view_dialog, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
        setShareLogButtonEvent();
    }

    public void init() {
        recyclerView = view.findViewById(R.id.logListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        logAdapter = LogAdapter.get();
        recyclerView.setAdapter(logAdapter);
        shareButton = view.findViewById(R.id.share_log);
        txCpuInfo = view.findViewById(R.id.tx_cpu_info);
        cpuInfoRelativeView = view.findViewById(R.id.cpuInfoRelativeView);
        cpuClockFrequency = view.findViewById(R.id.cpu_clock_frequency);
        closeButton = view.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Button button= view.findViewById(R.id.clear_log);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLogger.getInstance().clearLog();
            }
        });
        // initial CPU header text
        mCPUHeaderText = new StringBuffer();
        mCPUCoreNum = CpuUtil.getNumCores();
        mCPUHeaderText.append("Number of CPU Core:" + mCPUCoreNum + LF);
        for (int i = 0; i < mCPUCoreNum; i++) {
            cpuMaxList.add(getKHz(CpuUtil.getMaxCpuFreq(i)));
            cpuMinList.add(getKHz(CpuUtil.getMinCpuFreq(i)));
        }

        scrollToBottom();

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                final float rate = DeviceInfoManager.getCurProcessCpuRate();
                String cpuInfo;

                cpuInfo = String.format("[ CPU Usage: %s%s ]", (int) rate, "%");

                setTxCpuInfo(cpuInfo);
                handler.postDelayed(this, 2000);
            }
        };
        runnable.run();
    }

    private String getKHz(String hzStr) {
        try {
            int hz = Integer.parseInt(hzStr);
            DecimalFormat df = new DecimalFormat("###mhz");
            hzStr = df.format(hz / 1000);
            int hzStrLength = hzStr.length();
            if (hzStrLength <= 6) {
                int length = 7 - hzStrLength;
                for (int i = 0; i < length; i++) {
                    hzStr = "\t" + hzStr;
                }
            }
            return hzStr;
        } catch (Exception e) {
            return "\tstopped ";
        }
    }
    public void setShareLogButtonEvent(){
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String logPath = context.getFilesDir().getAbsolutePath();
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String filename = "log_"+ sdf.format(date) + ".txt";
                File file = logAdapter.writeListIntoSDcard(context,filename);
                if (file == null){
                    Log.e("error","Empty File");
                    return;
                }
                ArrayList<Uri> requestFileUri = new ArrayList<>();
                    try {
                        requestFileUri.add(FileProvider.getUriForFile(context,
                                "im.zego.expresssample.fileprovider", file));
                    } catch (IllegalArgumentException e) {
                        Log.e("File Selector",
                                "The selected file can't be shared: " + file);
                        Log.e("File Selector",e.toString());
                    }
                if (requestFileUri.size() > 0 && requestFileUri.get(0)!= null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    // Grant temporary read permission to the content URI
                    sendIntent.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, requestFileUri);
                    sendIntent.setType("text/plain");   //分享文件类型
                    startActivity(Intent.createChooser(sendIntent,"Share Log"));
                }
            }
        });
    }
    /**
     * 更新视窗 cpu信息
     * Update view with CPU information
     */
    public void setTxCpuInfo(String cpuInfo) {
        if (txCpuInfo != null) {
            txCpuInfo.setText(cpuInfo);
        }
        // 更新cpu时钟频率
        updateCpuFrequency();
    }

    private void updateCpuFrequency() {
        StringBuffer sb = new StringBuffer();

        sb.append(mCPUHeaderText.toString());
        for (int i = 0; i < mCPUCoreNum; i++) {
            sb.append(String.format(eachCpuInfo, i + 1,
                    getKHz(CpuUtil.getCurCpuFreq(i)),
                    cpuMaxList.get(i),
                    cpuMinList.get(i)
            ));
        }
        // 同时更新cpu频率信息
        if (cpuClockFrequency != null) {
            cpuClockFrequency.setText(sb.toString());
        }
    }

    /**
     * 日志列表滚动到最底部。
     * Log View scroll to bottom
     */
    public void scrollToBottom() {
        if (recyclerView != null) {
            int itemCount = logAdapter.getItemCount();
            if (itemCount > 10)
                recyclerView.scrollToPosition(itemCount - 1);
        }
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(runnable);
        super.onDestroyView();
    }
}
