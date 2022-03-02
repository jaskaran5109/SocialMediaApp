package com.example.socialmediaapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    RecyclerView postList;
    Toolbar mtoolbar;
    FirebaseAuth auth;
    DatabaseReference databaseReference, postReference ,likeReference;
    FirebaseDatabase database;
    CircleImageView navProfile;
    TextView nav_profileUsername;
    ImageButton add_post_btn;
    Boolean likeCheck=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
//        setSupportActionBar(mtoolbar);
//        getSupportActionBar().hide();
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();


        database = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeReference=FirebaseDatabase.getInstance().getReference().child("Likes");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.navigation_layout);
//        add_post_btn = (ImageButton) findViewById(R.id.add_new_post_btn);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfile = (CircleImageView) navView.findViewById(R.id.profile_image);
        nav_profileUsername = (TextView) navView.findViewById(R.id.user_full_name);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        databaseReference.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("fullName")) {
                        String name = snapshot.child("fullName").getValue().toString();
                        nav_profileUsername.setText(name);
                    }
                    if (snapshot.hasChild("profile_picture")) {
                        String image = snapshot.child("profile_picture").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfile);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile does not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

//        add_post_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendusertoPostActivity();
//            }
//        });

        DisplayAllUsersPost();


    }
    public void updateUserStatus(String state)
    {
        String saveCurrentDate,saveCurrentTime;
        Calendar callDate=Calendar.getInstance();
        SimpleDateFormat currDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currDate.format(callDate.getTime());

        Calendar callTime=Calendar.getInstance();
        SimpleDateFormat currTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currTime.format(callTime.getTime());

        HashMap currStateMap=new HashMap();
        currStateMap.put("time",saveCurrentTime);
        currStateMap.put("date",saveCurrentDate);
        currStateMap.put("type",state);

        databaseReference.child(auth.getCurrentUser().getUid()).child("userState")
                .updateChildren(currStateMap);
    }
    private void DisplayAllUsersPost() {

        Query sortPostsInDesc=postReference.orderByChild("timeStamp");

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (
                        Posts.class,
                        R.layout.all_post_layout,
                        PostsViewHolder.class,
                        sortPostsInDesc
                ) {
            @Override
            protected void populateViewHolder(PostsViewHolder postsViewHolder, Posts posts, int i)
            {
                final String PostKey=getRef(i).getKey();
                postsViewHolder.setFullName(posts.getFullName());
                postsViewHolder.setDate(posts.getDate());
                postsViewHolder.setTime(posts.getTime());
                postsViewHolder.setDescription(posts.getDescription());
                postsViewHolder.setProfileImage(getApplicationContext(),posts.getProfileImage());
                postsViewHolder.setPostImage(getApplicationContext(),posts.getPostImage());
                postsViewHolder.setLikeButtonStatus(PostKey);


                postsViewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(MainActivity.this,ClickPostActivity.class);
                        intent.putExtra("PostKey",PostKey);
                        startActivity(intent);
                    }
                });

                postsViewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        likeCheck=true;

                        likeReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                               if(likeCheck.equals(true))
                               {
                                   if(snapshot.child(PostKey).hasChild(auth.getCurrentUser().getUid()))
                                   {
                                       likeReference.child(PostKey).child(auth.getCurrentUser().getUid()).removeValue();
                                       likeCheck=false;
                                   }
                                   else
                                   {
                                       likeReference.child(PostKey).child(auth.getCurrentUser().getUid()).setValue(true);
                                       likeCheck=false;
                                   }
                               }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });

                postsViewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(MainActivity.this,CommentsActivity.class);
                        intent.putExtra("PostKey",PostKey);
                        startActivity(intent);
                    }
                });

            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mview;
        ImageButton likeBtn,commentBtn;
        TextView noOfLikes;
        int countLikes;
        String currUserId;
        DatabaseReference Likeref;
        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
            likeBtn=(ImageButton) mview.findViewById(R.id.likeBtn);
            commentBtn=(ImageButton) mview.findViewById(R.id.commentBtn);
            noOfLikes=(TextView) mview.findViewById(R.id.no_of_likes);
            Likeref=FirebaseDatabase.getInstance().getReference().child("Likes");
            currUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setFullName(String fullName) {
            TextView userName = (TextView) mview.findViewById(R.id.post_user_name);
            userName.setText(fullName);
        }

        public void setProfileImage(Context ctx,String profileImage) {
            CircleImageView imageView = (CircleImageView) mview.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileImage).into(imageView);
        }

        public void setTime(String time) {
            TextView postTime = (TextView) mview.findViewById(R.id.post_time);
            postTime.setText("   "+time);
        }

        public void setDate(String date) {
            TextView postDate = (TextView) mview.findViewById(R.id.post_date);
            postDate.setText("   "+date);
        }

        public void setDescription(String description) {
            TextView desc = (TextView) mview.findViewById(R.id.post_description);
            desc.setText(description);
        }

        public void setPostImage(Context ctx, String postImage) {
            ImageView imageView = (ImageView) mview.findViewById(R.id.post_image);
            Picasso.get().load(postImage).into(imageView);
        }

        public void setLikeButtonStatus(String postKey)
        {
            Likeref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postKey).hasChild(currUserId))
                    {
                        countLikes=(int) snapshot.child(postKey).getChildrenCount();
                        likeBtn.setImageResource(R.drawable.like3);
                        noOfLikes.setText((Integer.toString(countLikes))+" Likes");
                    }
                    else
                    {
                        countLikes=(int) snapshot.child(postKey).getChildrenCount();
                        likeBtn.setImageResource(R.drawable.dislike3);
                        noOfLikes.setText((Integer.toString(countLikes))+" Likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void sendusertoPostActivity() {
        Intent intent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            sendUsertoLoginActivity();
        } else {
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        String userId = auth.getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(userId)) {
                    sendUsertoSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendUsertoSetupActivity() {
        Intent intent = new Intent(MainActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUsertoLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_profile:
                Intent intent3 = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent3);
                break;
            case R.id.nav_home:
                Toast.makeText(MainActivity.this, "Home Page", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Intent intent5 = new Intent(MainActivity.this, FriendsActivity.class);
                startActivity(intent5);
                break;
            case R.id.nav_findFriends:
                Intent intent4 = new Intent(MainActivity.this, FindFriendsActivity.class);
                startActivity(intent4);
                break;
            case R.id.nav_messages:
                Intent intent6=new Intent(MainActivity.this,FriendsActivity.class);
                startActivity(intent6);
                break;
            case R.id.nav_notifications:
                Intent intent7=new Intent(MainActivity.this,NotificationActivity.class);
                startActivity(intent7);
                break;
            case R.id.nav_settings:
                Intent intent2 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_logout:
                updateUserStatus("offline");
                auth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}