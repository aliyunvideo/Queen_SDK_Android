package io.agora.vlive.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

import io.agora.framework.modules.consumers.SurfaceViewConsumer;

public class CameraSurfaceView extends SurfaceView {
    public CameraSurfaceView(Context context) {
        super(context);
        setSurfaceViewConsumer();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceViewConsumer();
    }

    private void setSurfaceViewConsumer() {
        getHolder().addCallback(new SurfaceViewConsumer(this));
    }
}
