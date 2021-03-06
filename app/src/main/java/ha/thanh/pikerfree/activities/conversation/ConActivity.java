package ha.thanh.pikerfree.activities.conversation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.activities.viewProfile.ViewProfileActivity;
import ha.thanh.pikerfree.adapters.MessageAdapter;
import ha.thanh.pikerfree.constants.Constants;
import ha.thanh.pikerfree.customviews.CustomEditText;
import ha.thanh.pikerfree.customviews.CustomTextView;
import ha.thanh.pikerfree.customviews.CustomYesNoDialog;
import ha.thanh.pikerfree.models.Messages.Message;
import ha.thanh.pikerfree.models.User;

public class ConActivity extends AppCompatActivity implements ConInterface.RequiredViewOps {


    public ConPresenter presenter;
    @BindView(R.id.img_op)
    public CircleImageView OPImage;
    @BindView(R.id.tv_op_name)
    public CustomTextView OPName;
    @BindView(R.id.btn_send)
    public ImageView sendButton;
    @BindView(R.id.et_mess_to_send)
    public CustomEditText tvMessToSend;
    @BindView(R.id.op_status)
    public ImageView opStatus;
    @BindView(R.id.rv_messes)
    public RecyclerView rvMess;
    @BindView(R.id.swipeRefreshLayout)
    public SwipeRefreshLayout swipeRefreshLayout;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        String id1 = intent.getStringExtra(Constants.U_ID_1);
        String id2 = intent.getStringExtra(Constants.U_ID_2);
        presenter = new ConPresenter(this, this, id1, id2);
    }

    private void initView() {

        ButterKnife.bind(this);
        messageAdapter = new MessageAdapter(this, presenter.getMessageList());
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvMess.setLayoutManager(layoutManager);
        rvMess.setAdapter(messageAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.onPull();
            }
        });
    }

    @OnClick(R.id.tv_op_name)
    public void goToProfile() {
        try {
            Intent intent = new Intent(this, ViewProfileActivity.class);
            intent.putExtra(Constants.USER_ID, presenter.getOpId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.img_op)
    public void goProfile() {
        goToProfile();
    }

    @OnClick(R.id.delete_con)
    public void deleteConversation() {
        CustomYesNoDialog customYesNoDialog = new CustomYesNoDialog(this, new CustomYesNoDialog.YesNoInterFace() {
            @Override
            public void onYesClicked() {
                presenter.deleteConversation();
            }

            @Override
            public void onNoClicked() {
            }
        });
        customYesNoDialog.showAlertDialog(getResources().getString(R.string.confirm), getResources().getString(R.string.confirm_delete));
    }

    @OnClick(R.id.ic_back)
    public void getBack() {
        onBackPressed();
    }

    @OnClick(R.id.btn_send)
    public void sendNewMess() {
        String textToSend = tvMessToSend.getText().toString();
        if (textToSend.equals("") || textToSend.equals("\n"))
            return;
        presenter.onUserAddNewMess(tvMessToSend.getText().toString());
        tvMessToSend.setText("");
    }

    @Override
    public void onPullDone() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onEndOfConversation() {
        try {
            rvMess.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        } catch (IllegalArgumentException e) {

            e.getMessage();
        }
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, getResources().getString(R.string.no_more), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteDone() {
        onBackPressed();
    }

    @Override
    public void onDeleteFailed() {
        Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getOPDone(User user) {
        OPName.setText(user.getName());
        if (user.isOnline())
            opStatus.setImageResource(R.drawable.shape_circle_green);
        else opStatus.setImageResource(R.drawable.shape_circle_gray);

    }

    @Override
    public void onOpNotFound() {
        OPName.setText(getResources().getString(R.string.deleted_user));
        opStatus.setImageResource(R.drawable.shape_circle_gray);
    }

    @Override
    public void getOwnerImageDone(Uri uri) {
        Glide.with(this)
                .load(uri)
                .apply(new RequestOptions()
                        .error(R.drawable.action_button_bg)
                        .centerCrop()
                        .dontAnimate()
                        .override(100, 100)
                        .dontTransform())
                .into(OPImage);
    }

    @Override
    public void onGetMessDone(Message message) {

        messageAdapter.notifyDataSetChanged();
        rvMess.smoothScrollToPosition(0);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
