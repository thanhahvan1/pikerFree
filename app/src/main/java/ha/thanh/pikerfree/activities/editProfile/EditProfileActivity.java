package ha.thanh.pikerfree.activities.editProfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.customviews.CustomEditText;
import ha.thanh.pikerfree.customviews.WaitingDialog;

public class EditProfileActivity extends AppCompatActivity implements EditProfileInterface.RequiredViewOps {

    @BindView(R.id.profile_image)
    public CircleImageView imageView;
    @BindView(R.id.et_user_name)
    public CustomEditText etUserName;
    @BindView(R.id.et_user_address)
    public CustomEditText etUserAddress;
    private WaitingDialog waitingDialog;
    private Uri filePath;
    private Bitmap bitmap;
    private static final int PICK_IMAGE_REQUEST = 234;
    private EditProfilePresenter profilePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        profilePresenter = new EditProfilePresenter(this, this);
        profilePresenter.addTextChangeListener(etUserName, etUserAddress);
        profilePresenter.getLocalData();
        waitingDialog = new WaitingDialog(this);
    }


    @OnClick(R.id.btn_change_image)
    public void showFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @OnClick(R.id.btn_save)
    public void saveEditing() {
        showDialog();
        profilePresenter.saveLocal(bitmap);
        profilePresenter.saveAuthSetting(etUserName.getText().toString(), etUserAddress.getText().toString());
        profilePresenter.saveDatabaseSetting();
        profilePresenter.uploadFile(filePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                profilePresenter.setImagesChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void showDialog() {
        waitingDialog.showDialog();
    }

    @Override
    public void hideDialog() {
        waitingDialog.hideDialog();
    }

    @Override
    public void onLocalInforReady(String name, String address, String filepath) {
        etUserName.setText(name);
        etUserAddress.setText(address);
        File imgFile = new File(filepath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
            Log.d("editProfile", "Loaded bitmap from local");
        }
    }
}
