package com.example.socialmediaapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference postRef,userRef,likeRef;
    String currUserId;
    RecyclerView myPostList;
    Boolean likeCheck=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        getSupportActionBar().setTitle("Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth=FirebaseAuth.getInstance();
        currUserId=auth.getCurrentUser().getUid();
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef= FirebaseDatabase.getInstance().getReference().child("Posts");

        myPostList=(RecyclerView) findViewById(R.id.my_all_post_list);
        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);

        DisplayPost();
    }

    private void DisplayPost()
    {
        Query postQuery=postRef.orderByChild("uid").startAt(currUserId).endAt(currUserId+"\uf8ff");

            FirebaseRecyclerAdapter<Posts, MainActivity.PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MainActivity.PostsViewHolder>
                (
                        Posts.class,
                        R.layout.all_post_layout,
                        MainActivity.PostsViewHolder.class,
                        postQuery
                ) {
            @Override
            protected void populateViewHolder(MainActivity.PostsViewHolder postsViewHolder, Posts posts, int i) {
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
                        Intent intent=new Intent(PostsActivity.this,ClickPostActivity.class);
                        intent.putExtra("PostKey",PostKey);
                        startActivity(intent);
                    }
                });

                postsViewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        likeCheck=true;

                        likeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(likeCheck.equals(true))
                                {
                                    if(snapshot.child(PostKey).hasChild(auth.getCurrentUser().getUid()))
                                    {
                                        likeRef.child(PostKey).child(auth.getCurrentUser().getUid()).removeValue();
                                        likeCheck=false;
                                    }
                                    else
                                    {
                                        likeRef.child(PostKey).child(auth.getCurrentUser().getUid()).setValue(true);
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
                        Intent intent=new Intent(PostsActivity.this,CommentsActivity.class);
                        intent.putExtra("PostKey",PostKey);
                        startActivity(intent);
                    }
                });
            }
        };
            myPostList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mview;
        ImageButton likeBtn,commentBtn;
        TextView noOfLikes;
        int countLikes;
        String currUserId;
        DatabaseReference Likeref;
        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
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

        public void setProfileImage(Context ctx, String profileImage) {
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
}