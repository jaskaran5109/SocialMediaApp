package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView userprofileImage;
    TextView userStatus,userName,userFullname,userCountry,userDob,userGender,userRelationStatus;
    DatabaseReference databaseReference,friendsRef,postRef;
    FirebaseAuth auth;
    String currUserId;
    Button posts,friends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        auth=FirebaseAuth.getInstance();
        currUserId=auth.getCurrentUser().getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(currUserId);
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        postRef=FirebaseDatabase.getInstance().getReference().child("Posts");

        userName=(TextView) findViewById(R.id.my_profile_userName);
        userStatus=(TextView) findViewById(R.id.my_profile_status);
        userFullname=(TextView) findViewById(R.id.my_profile_fullName);
        userCountry=(TextView) findViewById(R.id.my_profile_country);
        userDob=(TextView) findViewById(R.id.my_profile_dob);
        userGender=(TextView) findViewById(R.id.my_profile_gender);
        userRelationStatus=(TextView) findViewById(R.id.my_profile_relationShipStatus);
        userprofileImage=(CircleImageView) findViewById(R.id.my_profile_pic);
        posts=(Button) findViewById(R.id.my_post_button);
        friends=(Button) findViewById(R.id.my_friends_button);

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,FriendsActivity.class);
                startActivity(intent);
            }
        });
        posts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,PostsActivity.class);
                startActivity(intent);
            }
        });

        friendsRef.child(currUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    int count= (int) snapshot.getChildrenCount();
                    if(count==1)
                    {
                        friends.setText(Integer.toString(count) + " Friend");
                    }
                    else {
                        friends.setText(Integer.toString(count) + " Friends");
                    }
                }
                else
                {
                    friends.setText("0 Friend");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postRef.orderByChild("uid").startAt(currUserId).endAt(currUserId+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    int count= (int) snapshot.getChildrenCount();
                    if(count==1)
                    {
                        posts.setText(Integer.toString(count) + " Post");
                    }
                    else {
                        posts.setText(Integer.toString(count) + " Posts");
                    }
                }
                else
                {
                    friends.setText("0 Post");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String image=snapshot.child("profile_picture").getValue().toString();
                    String status=snapshot.child("status").getValue().toString();
                    String fullName=snapshot.child("fullName").getValue().toString();
                    String username=snapshot.child("userName").getValue().toString();
                    String country=snapshot.child("country").getValue().toString();
                    String dateOfBirth=snapshot.child("dob").getValue().toString();
                    String gender=snapshot.child("gender").getValue().toString();
                    String relationShipStatus=snapshot.child("relationShipStatus").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(userprofileImage);
                    userStatus.setText(status);
                    userFullname.setText(fullName);
                    userName.setText("@"+username);
                    userCountry.setText("Country: "+country);
                    userDob.setText("DOB: "+dateOfBirth);
                    userGender.setText("Gender: "+gender);
                    userRelationStatus.setText("Relationship: "+relationShipStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}