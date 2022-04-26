package im.zego.advancedaudioprocessing.soundlevelandspectrum.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import im.zego.advancedaudioprocessing.R;


public class SoundLevelAndSpectrumItem extends LinearLayout {

    private SpectrumView spectrumView;

    public SpectrumView getSpectrumView() {
        return spectrumView;
    }



    private TextView userIdTextView;

    public TextView getUserId() {
        return userIdTextView;
    }

    public void setUserId(TextView userid) {
        this.userIdTextView = userid;
    }


    private TextView streamIdTextView;

    public TextView getTvStreamId() {
        return streamIdTextView;
    }

    public void setTvStreamId(TextView streamId) {
        this.streamIdTextView = streamId;
    }


    private String streamid;

    public String getStreamid() {
        return streamid;
    }

    public void setStreamid(String streamid) {
        this.streamid = streamid;
    }


    private ProgressBar pbSoundLevel;

    public ProgressBar getPbSoundLevel() {
        return pbSoundLevel;
    }

    public void setPbSoundLevel(ProgressBar pb_play_sound_level) {
        this.pbSoundLevel = pb_play_sound_level;
    }



    public SoundLevelAndSpectrumItem(Context ctx, AttributeSet attributeSet){

        super(ctx, attributeSet);

        LayoutInflater.from(ctx).inflate(R.layout.activity_soundlevelandspectrum_layout_item, this);

        spectrumView = findViewById(R.id.soundlevelandspectrum_spectrum_view);

        userIdTextView = findViewById(R.id.tv_soundlevelandspectrum_userid);
        streamIdTextView = findViewById(R.id.tv_soundlevelandspectrum_streamid);

        pbSoundLevel = findViewById(R.id.pb_sound_level);

    }


}
