package br.com.dsx.vendamais.common;

/**
 * Created by rmk on 22/2/16.
 */
public class PermissionApi {
    private static PermissionApi mInstance;
    private PermissionCallback permissionCallback;

    private PermissionApi() {

    }

    public static PermissionApi getInstance() {
        if (mInstance == null) {
            mInstance = new PermissionApi();
        }
        return mInstance;
    }

    public void setPermissionCallback(PermissionCallback permissionCallback) {
        this.permissionCallback = permissionCallback;
    }

    public void onPermissionsGranted(int requestCode) {
        permissionCallback.onPermissionGranted(requestCode);
    }

    public void onPermissionsDenied(int requestCode) {
        permissionCallback.onPermissionDenied(requestCode);
    }

    public interface PermissionCallback {
        void onPermissionGranted(int requestCode);

        void onPermissionDenied(int requestCode);
    }
}
