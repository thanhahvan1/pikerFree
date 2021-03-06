package ha.thanh.pikerfree.activities.editProfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.customviews.CustomEditText;
import ha.thanh.pikerfree.customviews.WaitingDialog;

public class EditProfileActivity extends AppCompatActivity implements EditProfileInterface.RequiredViewOps {

    private static final int PICK_IMAGE_REQUEST = 234;
    @BindView(R.id.profile_image)
    public CircleImageView imageView;
    @BindView(R.id.et_user_name)
    public CustomEditText etUserName;
    @BindView(R.id.et_user_address)
    public CustomEditText etUserAddress;
    @BindView(R.id.et_user_phone)
    public CustomEditText etUserPhone;
    private WaitingDialog waitingDialog;
    private Uri filePath;
    private Bitmap bitmap;
    private EditProfilePresenter profilePresenter;
    private boolean isPickedImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        profilePresenter = new EditProfilePresenter(this, this);
        profilePresenter.addTextChangeListener(etUserName, etUserAddress, etUserPhone);
        profilePresenter.getLocalData();
        waitingDialog = new WaitingDialog(this);
        waitingDialog.showDialog();
    }

    @OnClick(R.id.btn_change_image)
    public void showFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @OnClick(R.id.btn_save)
    public void saveEditing() {
        showDialog();
        profilePresenter.saveLocal(bitmap);
        profilePresenter.saveDatabaseSetting();
        profilePresenter.uploadFile(filePath);
    }

    @OnClick(R.id.ic_back)

    public void getBack() {
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            isPickedImage = true;
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

        Toast.makeText(this, getResources().getString(R.string.completed), Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                waitingDialog.hideDialog();
            }
        }, 200);

    }

    @Override
    public void onUserDataReady(String name, String address, String userPhone) {

        waitingDialog.hideDialog();
        etUserName.setText(name);
        etUserAddress.setText(address);
        etUserPhone.setText(userPhone);
        etUserName.setSelection(etUserName.getText().length());
    }

    @Override
    public void getOwnerImageDone(Uri uri) {
        if (isPickedImage) return;
        try {
            Glide.with(this)
                    .load(uri)
                    .apply(new RequestOptions()
                            .error(R.drawable.ic_user)
                            .centerCrop()
                            .dontAnimate()
                            .override(150, 150)
                            .dontTransform())
                    .into(imageView);
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
