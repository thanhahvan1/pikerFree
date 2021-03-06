package ha.thanh.pikerfree.activities.viewProfile;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import ha.thanh.pikerfree.R;
import ha.thanh.pikerfree.models.Notification.MessageNotification;
import ha.thanh.pikerfree.models.Post;
import ha.thanh.pikerfree.models.User;
import ha.thanh.pikerfree.services.GPSTracker;
import ha.thanh.pikerfree.utils.Utils;

public class ViewProfilePresenter {
    private ViewProfileInterface.RequiredViewOps mView;
    private Handler handler;
    private FirebaseDatabase database;
    private List<Post> postList;
    private String currentUserId;
    private List<String> followingIdList;
    private List<User> folowingUserList;
    private Context context;
    private GPSTracker gpsTracker;
    private User user;
    ViewProfilePresenter(Context context, ViewProfileInterface.RequiredViewOps mView, String currentUserId) {
        this.context = context;
        this.mView = mView;
        this.currentUserId = currentUserId;
        handler = new Handler();
        postList = new ArrayList<>();
        followingIdList = new ArrayList<>();
        folowingUserList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        gpsTracker = new GPSTracker(context);
    }

    List<User> getFolowingUserList() {
        return folowingUserList;
    }

    List<Post> getPostList() {
        return postList;
    }

    String getOPId() {
        return user.getId();
    }

    String getUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    void updateRating(double rate) {
        List<String> list;
        list = user.getRatedUsers();
        if (user.getRatedUsers() != null) {
            for (int i = 0; i < list.size(); i++) {
                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equalsIgnoreCase(list.get(i))) {
                    mView.onRatingFail(context.getResources().getString(R.string.already_review));
                    return;
                }
            }
            list.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
            user.setRatedUsers(list);
            double newRate = Math.round((((rate + list.size() * user.getRating()) / (list.size() + 1) * 10) * 10) / 100.0);
            user.setRating(newRate);
            updateUser(newRate);
        } else {
            list = new ArrayList<>();
            list.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
            double newRate = (rate + 5) / 2;
            user.setRating(newRate);
            user.setRatedUsers(list);
            updateUser(newRate);
        }
    }

    private void updateUser(double newRate) {
        DatabaseReference userRatingPref;
        userRatingPref = database.getReference("users").child(user.getId()).child("rating");
        userRatingPref.setValue(user.getRating());
        DatabaseReference ratedUserPref;
        ratedUserPref = database.getReference("users").child(user.getId()).child("ratedUsers");
        ratedUserPref.setValue(user.getRatedUsers());
        mView.onRatingDone(newRate);
    }

    public double getUserLat() {
        return gpsTracker.getLatitude();
    }

    public double getUserLng() {
        return gpsTracker.getLongitude();
    }

    void loadAllMyPost() {
        postList.clear();
        handler.post(new Runnable() {
            @Override
            public void run() {
                final DatabaseReference userPref;
                userPref = database
                        .getReference("users")
                        .child(currentUserId);
                userPref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(User.class);
                        user.setOnline((Boolean) dataSnapshot.child("isOnline").getValue());
                        mView.onGetUserDataDone(user);
                        getUserImageLink(user.getAvatarLink());
                        if (user.getId().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            mView.onIsMyProfile();
                            getFollowingUser();
                        } else {
                            checkFollowStatus();
                            getPostData(user.getPosts());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void getFollowingUser() {

        if (user.getFollowingUsers() != null)
            followingIdList.addAll(user.getFollowingUsers());
        for (String id : followingIdList
                ) {
            DatabaseReference postRef;
            postRef = database
                    .getReference("users")
                    .child(id);
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        folowingUserList.add(dataSnapshot.getValue(User.class));
                        mView.getFollowingUserDone();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    List<String> getFllowingLisst() {
        return followingIdList;
    }

    private void checkFollowStatus() {
        List<String> followingUsers = new ArrayList<>();
        if (user.getFollowingUsers() != null)
            followingUsers = user.getFollowingUsers();

        if (followingUsers.contains(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            mView.onAlreadyFollow();
    }

    void followUser() {
        List<String> followingUsers = new ArrayList<>();
        if (user.getFollowingUsers() != null)
            followingUsers = user.getFollowingUsers();
        followingUsers.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        user.setFollowingUsers(followingUsers);
        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(user.getId())
                .child("followingUsers");
        databaseReference.setValue(followingUsers);
        uploadNotification(getOPId(), getUserId(), "followers", getUserId());
        mView.onFollowSuccess(context.getResources().getString(R.string.follow_done_mess));
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

    void unfollowUser() {

        List<String> followingUsers;
        followingUsers = user.getFollowingUsers();
        followingUsers.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(user.getId())
                .child("followingUsers");
        databaseReference.setValue(followingUsers);
        mView.onUnFollowSuccess(context.getResources().getString(R.string.unfollow_mess));
    }

    private void getUserImageLink(String link) {

        StorageReference mStorageRef;
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

    private void getPostData(final ArrayList<Integer> posts) {
        if (posts != null) {
            for (int i = 0; i < posts.size(); i++) {
                DatabaseReference postRef;
                postRef = database
                        .getReference("posts")
                        .child(posts.get(i).toString());
                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            postList.add(dataSnapshot.getValue(Post.class));
                            mView.onGetUserPostsDone();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
