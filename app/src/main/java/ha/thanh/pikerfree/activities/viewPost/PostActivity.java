package ha.thanh.pikerfree.activities.viewPost;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.activities.conversation.ConActivity;
import ha.thanh.pikerfree.activities.editPost.EditPostActivity;
import ha.thanh.pikerfree.activities.main.MainActivity;
import ha.thanh.pikerfree.activities.viewProfile.ViewProfileActivity;
import ha.thanh.pikerfree.adapters.CommentAdapter;
import ha.thanh.pikerfree.adapters.ImageSlideAdapter;
import ha.thanh.pikerfree.adapters.UserAdapter;
import ha.thanh.pikerfree.constants.Constants;
import ha.thanh.pikerfree.customviews.CustomAlertDialog;
import ha.thanh.pikerfree.customviews.CustomEditText;
import ha.thanh.pikerfree.customviews.CustomTextView;
import ha.thanh.pikerfree.customviews.CustomYesNoDialog;
import ha.thanh.pikerfree.customviews.EditCommentDialog;
import ha.thanh.pikerfree.customviews.UserInforDialog;
import ha.thanh.pikerfree.customviews.WaitingDialog;
import ha.thanh.pikerfree.dataHelper.PostDataHelper;
import ha.thanh.pikerfree.models.Post;
import ha.thanh.pikerfree.models.User;
import ha.thanh.pikerfree.utils.Utils;

import static com.facebook.share.widget.ShareDialog.canShow;

public class PostActivity extends AppCompatActivity implements
        PostInterface.RequiredViewOps,
        CustomYesNoDialog.YesNoInterFace,
        UserAdapter.ItemClickListener,
        OnMapReadyCallback,
        EditCommentDialog.optionInterface,
        CommentAdapter.CommentClickListener,
        UserInforDialog.optionInterface {

    @BindView(R.id.vp_image_slide)
    ViewPager vpImageSlide;

    @BindView(R.id.op_status)
    ImageView opStatus;
    @BindView(R.id.tv_title)
    CustomTextView title;
    @BindView(R.id.tv_description)
    CustomTextView description;
    @BindView(R.id.tv_day)
    CustomTextView dayTime;
    @BindView(R.id.tv_distance)
    CustomTextView distance;
    @BindView(R.id.tv_owner_name)
    CustomTextView ownerName;
    @BindView(R.id.tv_post_status)
    CustomTextView postStatus;
    @BindView(R.id.owner_pic)
    CircleImageView ownerPic;
    @BindView(R.id.tv_meet_owner)
    CustomTextView meetOwner;
    @BindView(R.id.tv_rating_num)
    CustomTextView ownerRating;
    @BindView(R.id.tv_chat)
    CustomTextView chatToOwner;
    @BindView(R.id.tv_send_request)
    CustomTextView sendRequest;
    @BindView(R.id.tv_post_category)
    CustomTextView tvCategory;
    @BindView(R.id.tv_no_requesting_user)
    CustomTextView noRequestingUser;
    @BindView(R.id.tv_requesting_user)
    CustomTextView tvRequestingUsers;
    @BindView(R.id.view_owner)
    View ownerView;
    @BindView(R.id.scroll_view)
    View scrollView;
    @BindView(R.id.view_bottom_action)
    View bottomView;
    @BindView(R.id.view_requesting_user_list)
    View requestingUserView;
    @BindView(R.id.rv_requesting_user)
    RecyclerView rvRequestingUser;
    @BindView(R.id.rv_comments)
    RecyclerView rvComments;
    @BindView(R.id.tv_no_comment)
    CustomTextView tvNoComment;
    @BindView(R.id.tv_add_comment)
    CustomEditText tvAddComment;
    @BindView(R.id.btn_save_local)
    CustomTextView tvSaveLocal;

    @BindView(R.id.mapView)
    MapView mMapView;

    int postID;
    int fromSplash = 0;
    PostDataHelper db;
    private ImageSlideAdapter imageSlideAdapter;
    private UserAdapter userAdapter;
    private CommentAdapter commentAdapter;
    private PostPresenter mPresenter;
    private WaitingDialog waitingDialog;
    private CustomYesNoDialog confirmDialog;
    private CustomAlertDialog alertDialog;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initData();
        initView();
        try {
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        } catch (Exception e) {

            alertDialog.showAlertDialog(getResources().getString(R.string.error), e.getMessage());
        }

    }

    @OnClick(R.id.btn_share)
    public void doSharePost() {
        waitingDialog.showDialog();
        PostPresenter.DownloadImgTask imgTask = mPresenter.new DownloadImgTask();
        imgTask.execute("");
    }


    @OnClick(R.id.btn_save_local)
    public void doAddToFavList() {

        alertDialog.setListener(null);
        if (tvSaveLocal.getText().toString().equalsIgnoreCase(getResources().getString(R.string.add_to_favorite))) {
            db.addPost(postID);
            alertDialog.showAlertDialog(getResources().getString(R.string.completed), getResources().getString(R.string.add_fav_mess));
            tvSaveLocal.setText(getResources().getString(R.string.remove_from_favorite));
        } else {
            db.deletePost(postID);
            alertDialog.showAlertDialog(getResources().getString(R.string.completed), getResources().getString(R.string.remove_fav_mess));
            tvSaveLocal.setText(getResources().getString(R.string.add_to_favorite));
        }
    }

    @OnClick(R.id.ic_back)
    public void getBack() {
        onBackPressed();
    }

    @OnClick(R.id.tv_chat)
    public void startChat() {
        mPresenter.handleChatOrClose();
    }

    @OnClick(R.id.tv_send_request)
    public void setSendRequest() {

        if (mPresenter.isUserOwner) {
            showConfirmDialog(getResources().getString(R.string.confirm_delete));
        } else {
            showConfirmDialog(getResources().getString(R.string.confirm_request));
        }
    }

    @Override
    public void onYesClicked() {
        waitingDialog.showDialog();
        mPresenter.handleRequestOrDelete();
    }

    @Override
    public void onNoClicked() {

    }

    @OnClick(R.id.btn_add_comment)
    public void addComment() {
        if (!tvAddComment.getText().toString().equalsIgnoreCase(""))
            mPresenter.addComment(tvAddComment.getText().toString());
        tvAddComment.setText("");
    }

    private void initData() {
        db = new PostDataHelper(this);
        Intent in = getIntent();
        postID = in.getIntExtra(Constants.POST_VIEW, 1);
        fromSplash = in.getIntExtra(Constants.IS_FIRST_RUN, 0);
        mPresenter = new PostPresenter(this, this);
        mPresenter.getPostData(postID + "");
        mPresenter.getImageLinksFromId(postID + "");
    }

    private void initView() {
        ButterKnife.bind(this);
        requestingUserView.setVisibility(View.GONE);
        confirmDialog = new CustomYesNoDialog(this, this);
        alertDialog = new CustomAlertDialog(this);
        scrollView.setVisibility(View.INVISIBLE);
        imageSlideAdapter = new ImageSlideAdapter(this, mPresenter.getImagePostList());
        vpImageSlide.setAdapter(imageSlideAdapter);

        waitingDialog = new WaitingDialog(this);
        waitingDialog.showDialog();

        userAdapter = new UserAdapter(this, mPresenter.getRequestingUsers(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        rvRequestingUser.setLayoutManager(layoutManager);
        rvRequestingUser.setAdapter(userAdapter);
        checkLocal();
    }

    private void checkLocal() {
        if (db.hasObject(postID))
            tvSaveLocal.setText(getResources().getString(R.string.remove_fav_mess));
    }

    @Override
    public void onBackPressed() {
        if (fromSplash == 1) {
            startMain();
        }
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        finish();
    }

    @Override
    public void onUserGranted() {
        alertDialog.showAlertDialog(getResources().getString(R.string.granted), getResources().getString(R.string.granted_mess));
    }

    @Override
    public void onCommentClicked(int position) {

        onViewProfile(mPresenter.getComments().get(position).getIdUser());
    }

    @Override
    public void onCommentDelete(final int position) {

        confirmDialog.showAlertDialog(getResources().getString(R.string.confirm), getResources().getString(R.string.confirm_delete));
        confirmDialog.setListener(null);
        confirmDialog.setListener(new CustomYesNoDialog.YesNoInterFace() {
            @Override
            public void onYesClicked() {
                mPresenter.deleteComment(position);
            }

            @Override
            public void onNoClicked() {
            }
        });
    }

    @Override
    public void onGetRequestingUserDone(int type) {
        noRequestingUser.setVisibility(View.GONE);
        requestingUserView.setVisibility(View.VISIBLE);
        userAdapter.notifyDataSetChanged();
        if (type == 1) {
            tvRequestingUsers.setText(getResources().getString(R.string.requesting_user));
        } else {
            tvRequestingUsers.setText(getResources().getString(R.string.granted_user));
        }
    }

    @Override
    public void onChooseUser(int position) {
        UserInforDialog userInforDialog = new UserInforDialog(this, this);
        userInforDialog.showAlertDialog(mPresenter.getRequestingUsers().get(position).getName(),
                mPresenter.getRequestingUsers().get(position).getId());
    }

    @Override
    public void onGetCommentDone() {
        tvNoComment.setVisibility(View.GONE);
        if (commentAdapter != null)
            commentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCommentEdit(int position) {
        EditCommentDialog editCommentDialog = new EditCommentDialog(this, this);
        editCommentDialog.showAlertDialog(mPresenter.getCommentForEdit(position).getComment());
    }

    @Override
    public void onSaveEdit(String string) {
        mPresenter.saveEditComment(string);
    }

    @Override
    public void onSavedComment() {
        commentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteCommentDone() {
        alertDialog.showAlertDialog(getResources().getString(R.string.completed), getResources().getString(R.string.complete_comment));
    }

    @Override
    public void onUserIsOwner() {
        ownerView.setVisibility(View.GONE);
        meetOwner.setText(getResources().getString(R.string.you_own_this));
        sendRequest.setText(getResources().getString(R.string.delete_this));
        chatToOwner.setText(getResources().getString(R.string.edit_this));
    }

    void updateMap(double lat, double lng) {

        if (lat == 0) {
            mMapView.setVisibility(View.GONE);
            return;
        }
        LatLng sydney = new LatLng(lat, lng);
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(sydney)
                    .title(getResources().getString(R.string.post_location)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
            try {
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.getMessage();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public void getPostDone(Post post) {
        scrollView.setVisibility(View.VISIBLE);
        title.setText(post.getTitle());
        description.setText(post.getDescription());
        dayTime.setText(Utils.getTimeString(post.getTimePosted()));
        distance.setText(mPresenter.getDistance());
        waitingDialog.hideDialog();
        postStatus.setText(mPresenter.getStatus());
        tvCategory.setText(Utils.getTextFromIntCategory(post.getCategory()));
        updateMap(post.getLocation().latitude, post.getLocation().longitude);
        setUpComment();
    }

    private void setUpComment() {

        commentAdapter = new CommentAdapter(this, mPresenter.getComments(),
                this, mPresenter.getOwnerId(), mPresenter.getUserId());
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        layoutManager2.setStackFromEnd(true);
        rvComments.setLayoutManager(layoutManager2);
        rvComments.setAdapter(commentAdapter);

    }

    @Override
    public void getPostFail() {

    }

    @OnClick(R.id.view_owner)
    public void goToProfile() {
        onViewProfile(mPresenter.getOwnerId());
    }

    @Override
    public void getOwnerDone(User user) {
        ownerName.setText(user.getName());
        if (user.isOnline()) opStatus.setImageResource(R.drawable.shape_circle_green);
        else opStatus.setImageResource(R.drawable.shape_circle_gray);
        ownerRating.setText(user.getRating() + "");
    }

    @Override
    public void OnGoToEdit(int id) {

        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra(Constants.POST_VIEW, id);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        finish();
    }

    @Override
    public void onShowError(String error) {
        alertDialog.showAlertDialog(getResources().getString(R.string.error), error);
    }

    @Override
    public void onDeleteDone() {
        alertDialog.showAlertDialog(getResources().getString(R.string.completed),
                getResources().getString(R.string.click_to_back));
        alertDialog.setListener(new CustomAlertDialog.AlertListener() {
            @Override
            public void onOkClicked() {
                onBackPressed();
            }
        });
    }

    @Override
    public void getOwnerImageDone(Uri uri) {
        try {
            Glide.with(this)
                    .load(uri)
                    .apply(new RequestOptions()
                            .error(R.drawable.action_button_bg)
                            .centerCrop()
                            .dontAnimate()
                            .override(200, 200)
                            .dontTransform())
                    .into(ownerPic);
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }

    }

    @Override
    public void getLinkDone() {
        imageSlideAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onAlreadyRequested() {
        alertDialog.showAlertDialog(getResources().getString(R.string.error), getResources().getString(R.string.already_sent_rq));
        alertDialog.setListener(null);
        waitingDialog.hideDialog();
    }

    @Override
    public void onRequestSent() {

        alertDialog.showAlertDialog(getResources().getString(R.string.completed), getResources().getString(R.string.complete_send_rq));
        alertDialog.setListener(null);
        waitingDialog.hideDialog();
        userAdapter.notifyDataSetChanged();
    }

    @Override
    public void showConfirmDialog(String mess) {
        confirmDialog.showAlertDialog(getResources().getString(R.string.confirm), mess);
    }

    @Override
    public void OnStartConversation(String id1, String id2) {
        Intent intent = new Intent(this, ConActivity.class);
        intent.putExtra(Constants.U_ID_1, id1);
        intent.putExtra(Constants.U_ID_2, id2);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        finish();
    }

    @Override
    public void onViewProfile(String id) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(Constants.USER_ID, id);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onSendMess(String id) {
        Intent intent = new Intent(this, ConActivity.class);
        intent.putExtra(Constants.U_ID_1, id);
        intent.putExtra(Constants.U_ID_2, FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onChoose(String id) {
        waitingDialog.showDialog();
        mPresenter.chooseUser(id);
    }

    @Override
    public void onGrantedDone(final String userId) {
        waitingDialog.hideDialog();
        alertDialog.showAlertDialog(getResources().getString(R.string.completed), getResources().getString(R.string.complete_choose_receiver));
        alertDialog.setListener(new CustomAlertDialog.AlertListener() {
            @Override
            public void onOkClicked() {
                onViewProfile(userId);
            }
        });
    }

    @Override
    public void onPostFb(List<Bitmap> bms) {

        List<SharePhoto> photos = new ArrayList<>();
        for (Bitmap bitmap : bms
                ) {
            photos.add(new SharePhoto.Builder().setBitmap(bitmap).build());
        }
        SharePhotoContent content = new SharePhotoContent
                .Builder()
                .addPhotos(photos)
                .build();
        ShareDialog dialog = new ShareDialog(this);
        if (canShow(SharePhotoContent.class)) {
            waitingDialog.hideDialog();
            dialog.show(content);
        } else {
        }
    }

}
