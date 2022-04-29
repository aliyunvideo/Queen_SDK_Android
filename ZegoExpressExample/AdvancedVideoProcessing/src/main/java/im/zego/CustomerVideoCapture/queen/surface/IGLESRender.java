package im.zego.CustomerVideoCapture.queen.surface;

public interface IGLESRender {

    public void onSurfaceAvailableSize(int width, int height);

    public void onSurfaceCreated();

    public void onSurfaceChanged(int width, int height);

    public void onDrawFrame();

    public void onResume();

    public void onPause();

    public void onDestroy();

    }
