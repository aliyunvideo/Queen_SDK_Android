package io.agora.vlive.ui.actionsheets.toolactionsheet;

import android.content.Context;
import android.util.AttributeSet;

import io.agora.vlive.R;

public class ECommerceToolActionSheet extends AbsToolActionSheet {
    private static final int[] ICON_RES_OWNER = {
            R.drawable.icon_setting,
            R.drawable.icon_data,
            R.drawable.icon_action_sheet_beauty,
            R.drawable.icon_action_sheet_music,
            R.drawable.icon_rotate,
            R.drawable.action_sheet_tool_video,
            R.drawable.action_sheet_tool_speaker,
            R.drawable.action_sheet_tool_ear_monitor
    };

    private static final int[] ICON_RES_AUDIENCE = {
            R.drawable.icon_action_sheet_call,
            R.drawable.icon_data,
    };

    private static final int[] TITLE_RES_OWNER = {
            R.string.live_room_tool_action_sheet_title_setting,
            R.string.live_room_tool_action_sheet_title_data,
            R.string.live_room_tool_action_sheet_title_beauty,
            R.string.live_room_tool_action_sheet_title_music,
            R.string.live_room_tool_action_sheet_title_rotate,
            R.string.live_room_tool_action_sheet_title_camera,
            R.string.live_room_tool_action_sheet_title_speaker,
            R.string.live_room_tool_action_sheet_title_inear
    };

    private static final int[] TITLE_RES_AUDIENCE = {
            R.string.live_room_tool_action_sheet_title_apply,
            R.string.live_room_tool_action_sheet_title_data
    };

    public ECommerceToolActionSheet(Context context) {
        super(context);
    }

    public ECommerceToolActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int onGetLayoutResource() {
        return R.layout.action_tool;
    }

    @Override
    public int getItemCount(int role) {
        switch (role) {
            case ROLE_AUDIENCE: return 2;
            case ROLE_OWNER: return 8;
            default: return 0;
        }
    }

    @Override
    public int[] getItemTitleResource(int role) {
        return role == ROLE_OWNER ? TITLE_RES_OWNER : TITLE_RES_AUDIENCE;
    }

    @Override
    public int[] getItemIconResource(int role) {
        return role == ROLE_OWNER ? ICON_RES_OWNER : ICON_RES_AUDIENCE;
    }
}
