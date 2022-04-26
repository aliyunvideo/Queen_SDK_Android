package im.zego.advancedaudioprocessing.rangeaudio.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import im.zego.advancedaudioprocessing.R;
import im.zego.advancedaudioprocessing.rangeaudio.entity.ModuleInfo;

/**
 * Created by zego on 2018/2/6.
 */

public class MainAdapter extends RecyclerView.Adapter {

    private List<ModuleInfo> topicList = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_position_module_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;

        ModuleInfo moduleInfo = topicList.get(position);
        myViewHolder.titleName.setText(moduleInfo.getTitleName());
        myViewHolder.contentName.setText(moduleInfo.getContentName());
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    /**
     * Update module information
     * 新增模块信息
     *
     * @param moduleInfo module info
     */
    public void addModuleInfo(ModuleInfo moduleInfo) {
        topicList.add(moduleInfo);
        notifyDataSetChanged();
    }

    public void clear() {
        topicList.clear();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleName;
        TextView contentName;

        MyViewHolder(View itemView) {
            super(itemView);
            titleName = itemView.findViewById(R.id.title_name);
            contentName = itemView.findViewById(R.id.content_name);
        }
    }
}

