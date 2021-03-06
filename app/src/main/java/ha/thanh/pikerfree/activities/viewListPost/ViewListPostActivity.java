package ha.thanh.pikerfree.activities.viewListPost;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.activities.viewPost.PostActivity;
import ha.thanh.pikerfree.adapters.PostAdapter;
import ha.thanh.pikerfree.constants.Constants;
import ha.thanh.pikerfree.customviews.CustomAlertDialog;
import ha.thanh.pikerfree.customviews.CustomTextView;
import ha.thanh.pikerfree.customviews.WaitingDialog;
import ha.thanh.pikerfree.utils.Utils;

public class ViewListPostActivity extends AppCompatActivity implements ViewListPostInterface.RequiredViewOps, PostAdapter.ItemClickListener {


    @BindView(R.id.rv_my_post)
    public RecyclerView rvPost;
    @BindView(R.id.tv_title)
    public CustomTextView tvTitle;
    PostAdapter adapter;
    WaitingDialog waitingDialog;
    private ViewListPostPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list_post);
        initData();
        initView();
    }

    private void initData() {

        waitingDialog = new WaitingDialog(this);
        waitingDialog.showDialog();
        presenter = new ViewListPostPresenter(this, this);
        Intent intent = getIntent();
        presenter.setCurrentCategory(intent.getIntExtra(Constants.CATEGORY, 8));
    }

    private void initView() {
        ButterKnife.bind(this);
        tvTitle.setText(Utils.getTextFromIntCategory(presenter.getCurrentCategory()));
        adapter = new PostAdapter(this, presenter.getPostList(), this, presenter.getUserLat(), presenter.getUserLng());
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvPost.setLayoutManager(layoutManager);
        rvPost.setAdapter(adapter);
    }

    @Override
    public void onNoResult() {
        waitingDialog.hideDialog();
        CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
        customAlertDialog.showAlertDialog(getResources().getString(R.string.no_data), getResources().getString(R.string.no_data_mess_2));
    }

    @Override
    public void onItemClick(int position) {
        Intent in = new Intent(this, PostActivity.class);
        try {
            int id = presenter.getPostList().get(position).getPostId();
            in.putExtra(Constants.POST_VIEW, id);
            startActivity(in);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @OnClick(R.id.ic_back)
    public void getBack() {
        onBackPressed();
    }

    @Override
    public void onGetPostDone() {

        waitingDialog.hideDialog();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
