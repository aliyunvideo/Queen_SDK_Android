package im.zego.commontools.logtools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import im.zego.commontools.R;


/**
 * Created by zego on 2018/2/6.
 */

public class LogAdapter extends RecyclerView.Adapter {

    private static LogAdapter instance;
    private List<String> logList = new ArrayList<>();
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_list, parent, false);
        return new MyViewHolder(view);
    }
    private LogAdapter(){}
    public static LogAdapter get() {
        if (instance == null) {
            synchronized (LogAdapter.class) {
                if (instance == null) {
                    instance = new LogAdapter();
                }
            }
        }
        return instance;
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.name.setText(logList.get(position));
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }


    public void addLog(final String log) {
        Log.e("addLog", " " + logList.size());
        logList.add(log);
        // 防止日志太多。当日志超过1000条就清空500条
        // Prevent too many logs. When the log exceeds 1000, clear 500
        if (logList.size() > 1000) {
            List<String> tempList = new ArrayList<>(logList.subList(500, 1000));
            logList.clear();
            logList = tempList;
        }
        notifyDataSetChanged();
    }


    public void clear() {

        logList.clear();
        notifyDataSetChanged();
    }

    /**
     * 将log写入sd卡
     * Write log into SD Card
     *
     * @param context 上下文 Application Context
     * @param fileName 文件名 File Name
     */
    public File writeListIntoSDcard(Context context, String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = context.getExternalFilesDir(null).getAbsolutePath();
            File sdFile = new File(sdCardDir, fileName);
            try {
                FileOutputStream fOut = new FileOutputStream(sdFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                for (int i = 0; i<logList.size();i++) {
                    myOutWriter.write(logList.get(i)+"\n");
                }
                myOutWriter.close();
                fOut.flush();
                fOut.close();
                return sdFile;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.log);
        }
    }
}

