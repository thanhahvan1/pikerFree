package ha.thanh.pikerfree.activities.viewPost;

import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.adapters.ImageSlideAdapter;
import ha.thanh.pikerfree.customviews.CustomTextView;
import ha.thanh.pikerfree.customviews.WaitingDialog;
import ha.thanh.pikerfree.models.Post;
import ha.thanh.pikerfree.models.User;
import ha.thanh.pikerfree.utils.Utils;

public class PostActivity extends AppCompatActivity implements
        PostInterface.RequiredViewOps,
        OnMapReadyCallback {


    @BindView(R.id.layout_count_dot)
    public LinearLayout pager_indicator;

    @BindView(R.id.vp_image_slide)
    ViewPager vpImageSlide;

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
    @BindView(R.id.tv_chat)
    CustomTextView chatToOwner;
    @BindView(R.id.tv_send_request)
    CustomTextView sendRequest;

    @BindView(R.id.view_owner)
    View ownerView;
    @BindView(R.id.scroll_view)
    View scrollView;
    @BindView(R.id.view_bottom_action)
    View bottomView;

    @BindView(R.id.rv_requesting_user)
    RecyclerView rvRequestingUser;


    @BindView(R.id.mapView)
    MapView mMapView;

    private ImageSlideAdapter adapter;
    private PostPresenter mPresenter;
    private ImageView[] dots;
    private int currentPosition = 0;
    private WaitingDialog waitingDialog;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initData();
        initView();
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
    }

    @OnClick(R.id.tv_chat)
    public void startChat() {
        mPresenter.handleChatOrClose();
    }

    @OnClick(R.id.tv_send_request)
    public void setSendRequest() {
        mPresenter.handleRequestOrDelete();
    }

    private void initData() {

        mPresenter = new PostPresenter(this, this);
        mPresenter.getPostData("1");
        mPresenter.getImageLinksFromId("1");
    }

    private void initView() {
        ButterKnife.bind(this);
        scrollView.setVisibility(View.INVISIBLE);
        adapter = new ImageSlideAdapter(this, mPresenter.getImagePostList());
        vpImageSlide = (ViewPager) findViewById(R.id.vp_image_slide);
        vpImageSlide.setAdapter(adapter);
        waitingDialog = new WaitingDialog(this);
        waitingDialog.showDialog();
        vpImageSlide.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onUserIsOwner() {
        ownerView.setVisibility(View.GONE);
        meetOwner.setText(getResources().getString(R.string.you_own_this));
        sendRequest.setText(getResources().getString(R.string.delete_this));
        chatToOwner.setText(getResources().getString(R.string.close_this));
    }

    private void setUiPageViewController() {
        dots = new ImageView[adapter.getCount()];
        for (int i = 0; i < adapter.getCount(); i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.none_seclected_dot);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(6, 0, 6, 0);
            pager_indicator.addView(dots[i], params);
        }
        dots[0].setImageResource(R.drawable.seclected_dot);
    }

    private void pageSelected(int position) {
        currentPosition = position;
        switchDot();
    }

    private void switchDot() {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (i == currentPosition)
                dots[i].setImageResource(R.drawable.seclected_dot);
            else
                dots[i].setImageResource(R.drawable.none_seclected_dot);
        }

    }

    void updateMap(double lat, double lng) {
        LatLng sydney = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Post location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
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
        updateMap(post.getLat(), post.getLng());
        setUiPageViewController();
    }

    @Override
    public void getPostFail() {

    }

    @Override
    public void getOwnerDone(User user) {
        ownerName.setText(user.getName());

    }

    @Override
    public void onDeleteDone() {

    }

    @Override
    public void getOwnerImageDone(Uri uri) {
        Glide.with(this)
                .load(uri)
                .apply(new RequestOptions()
                        .error(R.drawable.action_button_bg)
                        .centerCrop()
                        .dontAnimate()
                        .override(200, 200)
                        .dontTransform())
                .into(ownerPic);
    }

    @Override
    public void getLinkDone() {
        adapter.notifyDataSetChanged();

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
}
