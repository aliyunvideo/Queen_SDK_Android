package im.zego.Scenes.VideoForMultipleUsers;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;


import java.util.ArrayList;

import im.zego.R;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class ListDialog extends Dialog {
    public ListDialog(@NonNull Context context) {
        super(context);
    }

    public static class Builder {
        Context context;
        String title;
        //store the content of the dialog.
        ArrayList<String> listContentString = new ArrayList<>();
        Button refresh;
        ArrayAdapter<String> adapter;
        ZegoUser user;
        String streamID;

        public Builder(Context context) {
            this.context = context;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setMyStream(ZegoUser user, String StreamID){
            this.user = user;
            this.streamID = StreamID;
        }

        public void setUserListString(ArrayList<ZegoUser> content){
            listContentString.clear();
            for (ZegoUser user: content){
                listContentString.add("UserName:"+user.userName+"  UserID:"+user.userID);
            }
        }
        public void setStreamListString(ArrayList<ZegoStream> content, boolean isPublish){
            listContentString.clear();
            //if the user is publishing, then add the publishing stream into stream list.
            if (isPublish){
                listContentString.add("StreamID:"+ streamID +"  UserName:"+user.userName+"  UserID:"+user.userID);
            }
            for (ZegoStream stream: content){
                listContentString.add("StreamID:"+stream.streamID+"  UserName:"+stream.user.userName+"  UserID:"+stream.user.userID);
            }
        }
        public ListDialog create() {
            final ListDialog dialog = new ListDialog(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.list_dialog, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            TextView dialogTitle = layout.findViewById(R.id.dialogTitle);
            ListView contentView = layout.findViewById(R.id.listContent);
            ImageView closeButton = layout.findViewById(R.id.closeButton);
            refresh = layout.findViewById(R.id.refreshButton);

            adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, listContentString);
            contentView.setAdapter(adapter);
            dialogTitle.setText(title);

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            return dialog;
        }
        //refresh the List
        public void refresh(){
            adapter.notifyDataSetChanged();
        }
    }
}
