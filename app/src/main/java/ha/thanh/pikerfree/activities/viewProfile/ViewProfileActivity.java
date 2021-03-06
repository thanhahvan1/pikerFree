package ha.thanh.pikerfree.activities.viewProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.activities.conversation.ConActivity;
import ha.thanh.pikerfree.activities.main.MainActivity;
import ha.thanh.pikerfree.activities.viewPost.PostActivity;
import ha.thanh.pikerfree.adapters.PostAdapter;
import ha.thanh.pikerfree.adapters.UserAdapter;
import ha.thanh.pikerfree.constants.Constants;
import ha.thanh.pikerfree.customviews.CustomAlertDialog;
import ha.thanh.pikerfree.customviews.CustomTextView;
import ha.thanh.pikerfree.customviews.RatingDialog;
import ha.thanh.pikerfree.models.User;

public class ViewProfileActivity extends AppCompatActivity
        implements ViewProfileInterface.RequiredViewOps,
        PostAdapter.ItemClickListener,
        UserAdapter.ItemClickListener {


    @BindView(R.id.rv_my_post)
    public RecyclerView rvPost;
    @BindView(R.id.rv_my_followers)
    public RecyclerView rvFollowers;
    @BindView(R.id.profile_image)
    public CircleImageView userImage;
    @BindView(R.id.op_status)
    ImageView opStatus;
    @BindView(R.id.user_address)
    CustomTextView tvUserAddress;
    @BindView(R.id.user_name)
    CustomTextView tvUserName;
    @BindView(R.id.tv_loading_post)
    CustomTextView tvLoadingPost;
    @BindView(R.id.tv_no_data)
    CustomTextView tvNoData;
    @BindView(R.id.user_phone)
    CustomTextView tvPhone;
    @BindView(R.id.btn_follow)
    CustomTextView followBtn;
    @BindView(R.id.btn_send_mess)
    CustomTextView btnSendMess;
    @BindView(R.id.tv_rating_num)
    CustomTextView tvRateNum;
    @BindView(R.id.user_email)
    CustomTextView tvEmail;
    PostAdapter adapter;
    UserAdapter userAdapter;
    int fromSplash;
    private ViewProfilePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_conslapp);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initView() {

        adapter = new PostAdapter(this,
                presenter.getPostList(),
                this, presenter.getUserLat(),
                presenter.getUserLng());
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvPost.setLayoutManager(layoutManager);
        rvPost.setAdapter(adapter);

        userAdapter = new UserAdapter(this,
                presenter.getFolowingUserList(), this);
        LinearLayoutManager layoutManager2 =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvFollowers.setLayoutManager(layoutManager2);
        rvFollowers.setAdapter(userAdapter);

    }


    private void initData() {
        Intent intent = getIntent();
        String userId = intent.getStringExtra(Constants.USER_ID);
        fromSplash = intent.getIntExtra(Constants.IS_FIRST_RUN, 0);
        presenter = new ViewProfilePresenter(this, this, userId);
    }

    @OnClick(R.id.btn_follow)
    public void followUser() {
        if (followBtn.getText().toString().equalsIgnoreCase(getResources().getString(R.string.follow)))
            presenter.followUser();
        else presenter.unfollowUser();
    }

    @OnClick(R.id.btn_send_mess)
    public void sendMess() {
        Intent intent = new Intent(this, ConActivity.class);
        intent.putExtra(Constants.U_ID_1, presenter.getOPId());
        intent.putExtra(Constants.U_ID_2, presenter.getUserId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onUnFollowSuccess(String inform) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.showAlertDialog(getResources().getString(R.string.success), inform);
        followBtn.setText(getResources().getString(R.string.follow));
    }

    @Override
    public void onFollowSuccess(String inform) {

        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.showAlertDialog(getResources().getString(R.string.success), inform);
        followBtn.setText(getResources().getString(R.string.un_follow));
    }

    @Override
    public void onIsMyProfile() {
        tvRateNum.setClickable(false);
        followBtn.setVisibility(View.GONE);
        btnSendMess.setVisibility(View.GONE);
        rvPost.setVisibility(View.GONE);
    }

    @Override
    public void getFollowingUserDone() {
        rvFollowers.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        tvLoadingPost.setText(getResources().getString(R.string.your_follower));
        userAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChooseUser(int position) {

        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(Constants.USER_ID, presenter.getFllowingLisst().get(position));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadAllMyPost();
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
    public void onGetUserDataDone(User user) {
        tvUserAddress.setText(user.getAddress());
        tvUserName.setText(user.getName());
        tvPhone.setText(user.getPhoneNumber());
        tvRateNum.setText(String.valueOf(user.getRating()));
        tvEmail.setText(user.getEmail());
        if (user.isOnline()) opStatus.setImageResource(R.drawable.shape_circle_green);
        else opStatus.setImageResource(R.drawable.shape_circle_gray);
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
                            .override(150, 150)
                            .dontTransform())
                    .into(userImage);
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }
    }

    @Override
    public void onAlreadyFollow() {
        followBtn.setText(getResources().getString(R.string.un_follow));
    }

    @Override
    public void onGetUserPostsDone() {
        rvPost.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        tvLoadingPost.setText(getResources().getString(R.string.your_post));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        Intent in = new Intent(this, PostActivity.class);
        in.putExtra(Constants.POST_VIEW, presenter.getPostList().get(position).getPostId());
        startActivity(in);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @OnClick(R.id.rating)
    public void showRating() {
        RatingDialog ratingDialog = new RatingDialog(this, new RatingDialog.optionInterface() {
            @Override
            public void onReview(double rating) {
                presenter.updateRating(rating);
            }
        });
        ratingDialog.showRatingDialog();
    }


    @Override
    public void onRatingFail(String err) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.showAlertDialog(getResources().getString(R.string.error), err);
    }

    @Override
    public void onRatingDone(double newrate) {
        tvRateNum.setText(String.valueOf(newrate));
        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.showAlertDialog(getResources().getString(R.string.completed), "");
    }
}
