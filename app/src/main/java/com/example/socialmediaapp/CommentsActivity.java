package com.example.socialmediaapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    RecyclerView commentList;
    ImageButton postCommentBtn;
    EditText commentInput;
    String Post_Key;
    FirebaseAuth auth;
    DatabaseReference databaseReference, postReference;
    String currUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        getSupportActionBar().setTitle("Comments");
        Post_Key = getIntent().getExtras().get("PostKey").toString();
        commentList = (RecyclerView) findViewById(R.id.recyclerViewComments);
        commentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);


        postCommentBtn = (ImageButton) findViewById(R.id.postComment);
        commentInput = (EditText) findViewById(R.id.commentInput);

        auth = FirebaseAuth.getInstance();
        currUserId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child(currUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String userName = snapshot.child("userName").getValue().toString();
                            String profile_picture = snapshot.child("profile_picture").getValue().toString();
                            ValidateCommment(userName, profile_picture);
                            commentInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        postReference


                ) {
            @Override
            protected void populateViewHolder(CommentsViewHolder commentsViewHolder, Comments comments, int i) {
                commentsViewHolder.setUserName(comments.getUserName());
                commentsViewHolder.setDate(comments.getDate());
                commentsViewHolder.setTime(comments.getTime());
                commentsViewHolder.setComment(comments.getComment());
                commentsViewHolder.setProfile_picture(comments.getProfile_picture());
            }
        };
        commentList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment(String comment) {
            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setUserName(String userName) {
            TextView myUserName = (TextView) mView.findViewById(R.id.comment_username);
            myUserName.setText("@" + userName + " ");
        }

        public void setTime(String time) {
            TextView myTime = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText(" Time: " + time);
        }

        public void setDate(String date) {
            TextView myDate = (TextView) mView.findViewById(R.id.comment_date);
            myDate.setText(" Date: " + date);
        }

        public void setProfile_picture(String profile_picture) {
            CircleImageView profileImage = (CircleImageView) mView.findViewById(R.id.comment_profile_image);
            Picasso.get().load(profile_picture).placeholder(R.drawable.profile).into(profileImage);
        }
    }


    private void ValidateCommment(String userName, String profile_picture) {

        String commentText = commentInput.getText().toString();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(CommentsActivity.this, "Please write a comment", Toast.LENGTH_SHORT).show();
        } else {
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currTime.format(callForDate.getTime());

            final String randomKey = currUserId + saveCurrentDate + saveCurrentTime;

            HashMap commentMap = new HashMap();
            commentMap.put("uid", currUserId);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("userName", userName);
            commentMap.put("profile_picture", profile_picture);

            postReference.child(randomKey).updateChildren(commentMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CommentsActivity.this, "Commented successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });


        }

    }
}