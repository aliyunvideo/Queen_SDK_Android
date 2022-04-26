package io.agora.vlive.utils;

import android.graphics.Color;

import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.vlive.R;

public class Global {
    public static class Constants {
        public static final int ROLE_AUDIENCE = 0;
        public static final int ROLE_HOST = 1;
        public static final int ROLE_OWNER = 2;

        // Shared reference keys
        public static final String SF_NAME = "sf-agora-live";
        public static final String KEY_BEAUTY_ENABLED = "key-beauty-enabled";
        public static final String KEY_BLUR = "key-blur";
        public static final String KEY_WHITEN = "key-whiten";
        public static final String KEY_CHEEK = "key-cheek";
        public static final String KEY_EYE = "key-eye";

        public static final String KEY_RESOLUTION = "key-resolution";
        public static final String KEY_FRAME_RATE = "key-framerate";
        public static final String KEY_BITRATE = "key-bitrate";

        public static final String TAB_KEY = "live-tab";
        public static final int TAB_ID_MULTI = 0;
        public static final int TAB_ID_SINGLE = 1;
        public static final int TAB_ID_PK = 2;
        public static final int TAB_ID_VIRTUAL = 3;
        public static final int TAB_ECOMMERCE = 4;

        public static final String KEY_ROOM_ID = "room-id";
        public static final String KEY_ROOM_NAME = "room-name";
        public static final String KEY_IS_ROOM_OWNER = "is-room-owner";
        public static final String KEY_ROOM_OWNER_ID = "room-owner-id";
        public static final String KEY_CREATE_ROOM = "create-room";
        public static final String KEY_VIRTUAL_IMAGE = "virtual-image";
        public static final String KEY_PEER_ID = "peer-id";
        public static final String KEY_NICKNAME = "nick-name";
        public static final String KEY_AUDIENCE_VIRTUAL_IMAGE = "audience-select-image";

        public static final String KEY_PROFILE_UID = "key-profile-uid";
        public static final String KEY_USER_NAME = "key-user-name";
        public static final String KEY_IMAGE_URL = "key-image-url";
        public static final String KEY_TOKEN = "key-token";

        public static final String KEY_SHOW_PRIVACY = "key_show_privacy";

        public static final int EDIT_USER_NAME_RESULT_CANCEL = 1;
        public static final int EDIT_USER_NAME_RESULT_DONE = 2;

        public static final int DIVIDER_COLOR = Color.rgb(239, 239, 239);

        public static final int VIDEO_MAX_BITRATE = 2000;
        public static final int VIDEO_MIN_BITRATE = 0;
        public static final int VIDEO_DEFAULT_BITRATE = VideoEncoderConfiguration.STANDARD_BITRATE;
        public static final int VIDEO_DEFAULT_RESOLUTION_INDEX = 0;
        public static final int VIDEO_DEFAULT_FRAME_RATE_INDEX = 0;

        public static final int CAMERA_CAPTURE_WIDTH = 1920;
        public static final int CAMERA_CAPTURE_HEIGHT = 1080;
        public static final int CAMERA_FRAME_RATE = 30;

        // By default the app log keeps for 5 days before being destroyed
        public static final long LOG_DURATION = 1000 * 60 * 24 * 5;

        // Enable stack trace info with depth
        public static final int LOG_CLASS_DEPTH = 1;

        public static final long APP_LOG_SIZE = 1 << 30;

        public static final int[] TAB_IDS_RES = {
                R.string.home_category_title_multi,
                R.string.home_category_title_single,
                R.string.home_category_title_pk,
                R.string.home_category_title_virtual_host,
                R.string.home_category_title_ecommerce
        };

        public static final String[] RESOLUTIONS_MULTI_HOST_TEXT = {
                "240x240"
        };

        public static final VideoEncoderConfiguration.VideoDimensions[] RESOLUTIONS_MULTI_HOST = {
                VideoEncoderConfiguration.VD_240x240
        };

        public static final String[] RESOLUTIONS_SINGLE_HOST_TEXT = {
                "360x640",
                "480x848",
                "720x1280"
        };

        public static final VideoEncoderConfiguration.VideoDimensions[] RESOLUTIONS_SINGLE_HOST = {
                VideoEncoderConfiguration.VD_640x360,
                new VideoEncoderConfiguration.VideoDimensions(848, 480),
                VideoEncoderConfiguration.VD_1280x720,

        };

        public static final String[] RESOLUTIONS_PK_HOST_TEXT = {
                "360x640"
        };

        public static final VideoEncoderConfiguration.VideoDimensions[] RESOLUTIONS_PK_HOST = {
                new VideoEncoderConfiguration.VideoDimensions(360, 640)
        };

        public static final VideoEncoderConfiguration.VideoDimensions[] RESOLUTIONS_VIRTUAL_IMAGE = {
                new VideoEncoderConfiguration.VideoDimensions(720, 1280)
        };

        public static final String[] RESOLUTIONS_VIRTUAL_IMAGE_TEXT = {
                "720x1280"
        };

        public static final VideoEncoderConfiguration.VideoDimensions[] RESOLUTIONS_ECOMMERCE = {
                new VideoEncoderConfiguration.VideoDimensions(720, 1280)
        };

        public static final String[] RESOLUTIONS_ECOMMERCE_TEXT = {
                "720x1280"
        };

        public static final String[] FRAME_RATES_TEXT = {
                "15"
        };

        public static final VideoEncoderConfiguration.FRAME_RATE[] FRAME_RATES = {
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15
        };

        public static final int[] PROFILE_BG_RES = {
                R.drawable.profile_image_1,
                R.drawable.profile_image_2,
                R.drawable.profile_image_3,
                R.drawable.profile_image_4,
                R.drawable.profile_image_5,
                R.drawable.profile_image_6,
                R.drawable.profile_image_7,
                R.drawable.profile_image_8,
                R.drawable.profile_image_9,
                R.drawable.profile_image_10,
                R.drawable.profile_image_11,
                R.drawable.profile_image_12
        };

        public static final int VOICE_INDICATE_INTERVAL = 1500;

        public static final int VOICE_INDICATE_SMOOTH = 3;
    }
}
