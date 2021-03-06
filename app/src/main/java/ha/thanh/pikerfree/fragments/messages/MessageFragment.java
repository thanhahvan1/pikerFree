package ha.thanh.pikerfree.fragments.messages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.activities.conversation.ConActivity;
import ha.thanh.pikerfree.activities.search.SearchActivity;
import ha.thanh.pikerfree.adapters.ConversationAdapter;
import ha.thanh.pikerfree.constants.Constants;
import ha.thanh.pikerfree.customviews.CustomEditText;
import ha.thanh.pikerfree.customviews.CustomTextView;
import ha.thanh.pikerfree.models.Conversation;


public class MessageFragment extends Fragment implements MessageInterface.RequiredViewOps, ConversationAdapter.ItemClickListener {

    @BindView(R.id.tv_no_data)
    public CustomTextView tvNotData;
    @BindView(R.id.rv_conversation)
    public RecyclerView rvConversation;

    @BindView(R.id.tv_search_user)
    CustomEditText editTextSearchKey;

    private MessagePresenter presenter;
    private ConversationAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        ButterKnife.bind(this, view);
        initData();
        initView();
        return view;
    }

    @OnClick(R.id.btn_search)
    public void onbtnSearchClicked() {
        if (editTextSearchKey.getText().toString().equalsIgnoreCase(""))
            return;
        Intent intent = new Intent(this.getContext(), SearchActivity.class);
        intent.putExtra(Constants.POST_SEARCH, editTextSearchKey.getText().toString());
        intent.putExtra(Constants.CATEGORY, 2); //1 mean searching for user;
        startActivity(intent);
        this.getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void refreshLayout() {
        try {
            adapter.notifyDataSetChanged();
        } catch (Exception e) {

        }
    }

    private void initData() {
        presenter = new MessagePresenter(this.getContext(), this);
        presenter.loadAllConversation();
    }

    private void initView() {
        adapter = new ConversationAdapter(this.getContext(),
                presenter.getConversationList(),
                this);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvConversation.setLayoutManager(layoutManager);
        rvConversation.setAdapter(adapter);
    }

    @Override
    public void onGetConversationDone() {
        adapter.notifyDataSetChanged();
        rvConversation.setVisibility(View.VISIBLE);
        tvNotData.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(int position) {
        Conversation conversation = presenter.getConversationList().get(position);
        Intent intent = new Intent(this.getContext(), ConActivity.class);
        intent.putExtra(Constants.U_ID_1, conversation.getIdUser1());
        intent.putExtra(Constants.U_ID_2, conversation.getIdUser2());
        startActivity(intent);
        this.getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onGetConversationFail(String error) {

    }

}
