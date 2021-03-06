package ha.thanh.pikerfree.activities.notification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.activities.viewPost.PostActivity;
import ha.thanh.pikerfree.activities.viewProfile.ViewProfileActivity;
import ha.thanh.pikerfree.adapters.NotificationAdapter;
import ha.thanh.pikerfree.constants.Constants;
import ha.thanh.pikerfree.dataHelper.NotificationDataHelper;
import ha.thanh.pikerfree.dataHelper.SQLiteNotification;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.CommentClickListener {


    @BindView(R.id.rv_notification)
    RecyclerView rvNotification;
    private List<SQLiteNotification> dataList;
    private NotificationAdapter adapter;
    private NotificationDataHelper dataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        dataHelper = new NotificationDataHelper(this);
        dataList = new ArrayList<>();
        dataList = dataHelper.getAllMess();
    }

    private void initView() {

        adapter = new NotificationAdapter(this, dataList, this);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvNotification.setLayoutManager(layoutManager);
        rvNotification.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleted(int position) {
        dataHelper.deleteNotification(dataList.get(position));
        dataList.remove(position);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClicked(int position) {

        dataList.get(position).setRead(1);
        dataHelper.updateNotification(dataList.get(position));
        SQLiteNotification notification = dataList.get(position);

        Intent intent;
        switch (notification.getType()) {
            case 2: // got new follower
                intent = new Intent(this, ViewProfileActivity.class);
                intent.putExtra(Constants.USER_ID, notification.getDataID());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case 3:
            case 4:
            case 5:// got new post request
                intent = new Intent(this, PostActivity.class);
                intent.putExtra(Constants.POST_VIEW, Integer.valueOf(notification.getDataID()));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.ic_back)
    public void getBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

}
