package ha.thanh.pikerfree.activities.viewPost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.constants.Constants;
import ha.thanh.pikerfree.models.Comment;
import ha.thanh.pikerfree.models.Notification.MessageNotification;
import ha.thanh.pikerfree.models.Post;
import ha.thanh.pikerfree.models.User;
import ha.thanh.pikerfree.utils.Utils;


class PostPresenter {

    boolean isUserOwner = false;
    private PostInterface.RequiredViewOps mView;
    private PostModel mModel;
    private List<String> imagePostList;
    private StorageReference mStorageRef;
    private FirebaseDatabase database;
    private User dataUser;
    private List<String> requestingUserIDs;
    private List<User> requestingUsers;
    private List<Comment> comments;
    private Post post;
    private Handler handler;
    private int postID;
    private int currentEditingComment = -1;
    private Context context;

    PostPresenter(Context context, PostInterface.RequiredViewOps mView) {
        this.context = context;
        this.mView = mView;
        mModel = new PostModel(context);
        initData();
    }

    List<String> getImagePostList() {
        return imagePostList;
    }

    List<User> getRequestingUsers() {
        return requestingUsers;
    }

    private void initData() {
        requestingUsers = new ArrayList<>();
        imagePostList = new ArrayList<>();
        comments = new ArrayList<>();
        mStorageRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child("postImages");
        database = FirebaseDatabase.getInstance();
        dataUser = new User();
        post = new Post();
        handler = new Handler();
    }


    void addComment(String commentContent) {

        Comment comment = new Comment();
        comment.setComment(commentContent);
        comment.setIdUser(mModel.getUserIdFromSharePref());
        if (comment.getIdUser() == null)
            comment.setIdUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
        comments.add(comment);
        UploadComment();
    }

    private void UploadComment() {
        post.setComments(comments);
        handler.post(new Runnable() {
            @Override
            public void run() {
                DatabaseReference postPref;
                postPref = database.getReference("posts").child("" + post.getPostId()).child("comments");
                postPref.setValue(post.getComments());
                mView.onGetCommentDone();
            }
        });
    }

    void deleteComment(int position) {
        comments.remove(position);
        UploadComment();
        mView.onDeleteCommentDone();
    }

    void getPostData(final String postId) {

        postID = Integer.parseInt(postId);
        handler.post(new Runnable() {
            @Override
            public void run() {
                DatabaseReference postRef;
                postRef = database
                        .getReference("posts")
                        .child(postId);
                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        post = dataSnapshot.getValue(Post.class);
                        if (post.getComments() != null)
                            comments.addAll(post.getComments());
                        mView.onGetCommentDone();
                        mView.getPostDone(post);
                        requestingUserIDs = post.getRequestingUser();
                        if (post.getOwnerId().equals(mModel.getUserIdFromSharePref())) {
                            mView.onUserIsOwner();
                            isUserOwner = true;
                            getRequestingUserList();
                        }
                        if (post.getStatus() == Constants.STATUS_CLOSE)
                            getGrantedUserData();
                        getOwnerData(post.getOwnerId());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        mView.getPostFail();
                    }
                });
            }
        });
    }

    List<Comment> getComments() {
        return comments;
    }

    Comment getCommentForEdit(int id) {

        currentEditingComment = id;
        return comments.get(id);
    }

    void saveEditComment(String string) {
        comments.get(currentEditingComment).setComment(string);

        DatabaseReference postPref;
        postPref = database.getReference("posts").child("" + post.getPostId()).child("comments").child(currentEditingComment + "").child("comment");
        postPref.setValue(comments.get(currentEditingComment).getComment());
        mView.onSavedComment();

    }

    private void getGrantedUserData() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (post.getGrantedUser() != null) {
                    DatabaseReference userPref;
                    userPref = database
                            .getReference("users")
                            .child(post.getGrantedUser());
                    userPref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            user.setOnline((Boolean) dataSnapshot.child("isOnline").getValue());
                            requestingUsers.add(user);
                            mView.onGetRequestingUserDone(2);
                            if (user.getId().equalsIgnoreCase(mModel.getUserIdFromSharePref()))
                                mView.onUserGranted();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });
    }

    String getOwnerId() {
        return post.getOwnerId();
    }

    String getUserId() {
        return mModel.getUserIdFromSharePref();
    }

    void handleRequestOrDelete() {
        if (isUserOwner) {
            deletePost();
        } else {
            if (post.getStatus() == Constants.STATUS_OPEN) {
                updateRequestingUserList();
                uploadRequestNotification();
            } else
                mView.onShowError(context.getResources().getString(R.string.post_is_closed));
        }
    }

    private void uploadRequestNotification() {
        uploadNotification(getOwnerId(), getUserId(), "requesting", post.getPostId() + "");
    }

    private void uploadGrantedNotification(String receiver) {
        uploadNotification(receiver, getOwnerId(), "granted", post.getPostId() + "");
    }

    private void uploadNotification(String receiverId, String senderId, String child, String mess) {
        MessageNotification message =
                new MessageNotification(mess, senderId, receiverId, Utils.getCurrentTimestamp());
        database.getReference()
                .child("notifications")
                .child(child)
                .push()
                .setValue(message);
    }

    private void deletePost() {
        // delete data base.

        DatabaseReference postPref;
        postPref = database.getReference("posts").child("" + post.getPostId());
        postPref.setValue(null);

        updateUserData();
    }

    private void updateUserData() {
        List<Integer> posts = dataUser.getPosts();
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i) == postID) {
                posts.remove(i);
            }
        }
        DatabaseReference userPost;
        userPost = database.getReference("users").child("" + dataUser.getId()).child("posts");
        userPost.setValue(posts);
        mView.onDeleteDone();

    }

    private void updateRequestingUserList() {

        boolean canRequest = true;
        if (requestingUserIDs == null) {
            // if it's null then must init then add element into it.
            requestingUserIDs = new ArrayList<>();

        } else {
            // if it's not null then must check if user is already in it then decide.
            for (int i = 0; i < requestingUserIDs.size(); i++) {
                if (requestingUserIDs.get(i).equals(mModel.getUserIdFromSharePref())) {
                    mView.onAlreadyRequested();
                    canRequest = false;
                }
            }
        }
        if (canRequest) {
            requestingUserIDs.add(mModel.getUserIdFromSharePref());
            post.setRequestingUser(requestingUserIDs);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference postPref;
                    postPref = database.getReference("posts").child("" + post.getPostId()).child("requestingUser");
                    postPref.setValue(post.getRequestingUser());
                    mView.onRequestSent();
                }
            });
        }
    }

    private void editPost() {
        if (post.getStatus() == Constants.STATUS_OPEN)
            mView.OnGoToEdit(post.getPostId());
        else mView.onShowError(context.getResources().getString(R.string.post_can_not_edit));
    }

    private void initChat() {
        String id1 = dataUser.getId();
        String id2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mView.OnStartConversation(id1, id2);
    }

    void chooseUser(String userId) {

        post.setStatus(Constants.STATUS_CLOSE);
        post.setRequestingUser(null);
        post.setGrantedUser(userId);
        savePost(userId);
        uploadGrantedNotification(userId);
    }

    private void savePost(String userid) {
        DatabaseReference postPref;
        postPref = database.getReference("posts").child(post.getPostId() + "");
        postPref.setValue(post);
        mView.onGrantedDone(userid);
    }

    void handleChatOrClose() {
        if (isUserOwner) {

            editPost();
        } else {
            initChat();
        }
    }

    String getDistance() {
        return Utils.getDistance(mModel.getUserLat(), mModel.getUserLng(), post.getLocation().latitude, post.getLocation().longitude);
    }

    String getStatus() {
        if (post.getStatus() == 1)
            return context.getResources().getString(R.string.opening);
        return context.getResources().getString(R.string.closed);
    }

    private void getOwnerData(final String UserId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                DatabaseReference userPref;
                userPref = database
                        .getReference("users")
                        .child(UserId);
                userPref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataUser = dataSnapshot.getValue(User.class);
                        dataUser.setOnline((Boolean) dataSnapshot.child("isOnline").getValue());
                        mView.getOwnerDone(dataUser);
                        getUserImageLink(dataUser.getAvatarLink());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void getRequestingUserList() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (requestingUserIDs != null) {
                    for (int i = 0; i < requestingUserIDs.size(); i++) {

                        DatabaseReference userPref;
                        userPref = database
                                .getReference("users")
                                .child(requestingUserIDs.get(i));

                        userPref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    User user = dataSnapshot.getValue(User.class);
                                    requestingUsers.add(user);
                                    mView.onGetRequestingUserDone(1);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });
    }

    private void getUserImageLink(String link) {

        mStorageRef = FirebaseStorage.getInstance()
                .getReference().child(link);
        mStorageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mView.getOwnerImageDone(uri);
                    }
                });
    }

    void getImageLinksFromId(final String postId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 6; i++) {
                    mStorageRef = FirebaseStorage
                            .getInstance()
                            .getReference()
                            .child("postImages")
                            .child(postId)
                            .child("image_no_" + i + ".jpg");
                    mStorageRef.getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imagePostList.add(uri.toString());
                                    mView.getLinkDone();
                                }
                            });
                }
            }
        });

    }

    public class DownloadImgTask extends AsyncTask<String, Void, List<Bitmap>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bms = new ArrayList<>();
            for (int i = 0; i < imagePostList.size(); i++) {
                Bitmap bm;
                try {
                    InputStream in = new java.net.URL(imagePostList.get(i)).openStream();
                    bm = BitmapFactory.decodeStream(in);
                    bms.add(bm);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
            return bms;
        }

        protected void onPostExecute(List<Bitmap> result) {
            mView.onPostFb(result);
        }
    }
}
