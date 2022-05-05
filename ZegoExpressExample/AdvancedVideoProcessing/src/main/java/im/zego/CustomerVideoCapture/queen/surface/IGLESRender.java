package im.zego.CustomerVideoCapture.queen.surface;

public interface IGLESRender {

    void onSurfaceAvailableSize(int width, int height);

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();

    void onResume();

    void onPause();

    void onDestroy();

    }
