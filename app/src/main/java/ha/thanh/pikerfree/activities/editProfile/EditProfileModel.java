package ha.thanh.pikerfree.activities.editProfile;

import android.content.Context;
import android.content.SharedPreferences;

import ha.thanh.pikerfree.constants.Constants;

/**
 * Created by HaVan on 9/10/2017.
 */

class EditProfileModel {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    EditProfileModel(Context context) {

        sharedPreferences = context.getSharedPreferences(Constants.SETTING_CONFIG, Context.MODE_PRIVATE);
    }

    String getUserIdFromSharePf() {
        return sharedPreferences.getString(Constants.USER_ID, "");
    }

    void saveLocal(String username, String userAddress, String path, String userPhone) {
        editor = sharedPreferences.edit();
        editor.putString(Constants.USER_NAME, username);
        editor.putString(Constants.USER_ADDRESS, userAddress);
        editor.putString(Constants.USER_PROFILE_PIC_PATH, path);
        editor.putString(Constants.USER_PHONE, userPhone);
        editor.apply();

    }
}
