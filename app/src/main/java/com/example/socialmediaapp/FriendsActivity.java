package com.example.socialmediaapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class FriendsActivity extends AppCompatActivity {

    RecyclerView friends_list;
    FirebaseAuth auth;
    DatabaseReference friendsRef,userRef;
    String onlineUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        auth=FirebaseAuth.getInstance();
        onlineUserId=auth.getCurrentUser().getUid();
        friendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");

        friends_list=(RecyclerView) findViewById(R.id.friends_list);
        friends_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friends_list.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();

    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        friendsRef

                ) {
            @Override
            protected void populateViewHolder(FriendsViewHolder friendsViewHolder, Friends friends, int i) {
                friendsViewHolder.setDate(friends.getDate());

                final String usersIds=getRef(i).getKey();
                userRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            final String fullName=snapshot.child("fullName").getValue().toString();
                            final String profileImage=snapshot.child("profile_picture").getValue().toString();
                            final String type;
                            if(snapshot.hasChild("userState"))
                            {
                                type=snapshot.child("userState").child("type").getValue().toString();
                                if(type.equals("online"))
                                {
                                    friendsViewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    friendsViewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            friendsViewHolder.setFullName(fullName);
                            friendsViewHolder.setProfile_picture(profileImage);
                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CharSequence options[]=new CharSequence[]{
                                            fullName+" profile",
                                            "Send Message"
                                    };
                                    AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i==0)
                                            {
                                                Intent intent=new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                intent.putExtra("visited_user_id",usersIds);
                                                startActivity(intent);
                                            }
                                            if(i==1)
                                            {
                                                Intent  intent=new Intent(FriendsActivity.this,ChatsActivity.class);
                                                intent.putExtra("visited_user_id",usersIds);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        friends_list.setAdapter(firebaseRecyclerAdapter);
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

        userRef.child(onlineUserId).child("userState")
                .updateChildren(currStateMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView onlineStatusView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            onlineStatusView=(ImageView) mView.findViewById(R.id.all_users_online_icon);
        }
        public void setProfile_picture(String profile_picture)
        {
            CircleImageView myImage=(CircleImageView) mView.findViewById(R.id.all_users_profileImage);
            Picasso.get().load(profile_picture).placeholder(R.drawable.profile).into(myImage);
        }
        public void setFullName(String fullName)
        {
            TextView myName=(TextView) mView.findViewById(R.id.all_users_profileName);
            myName.setText(fullName);
        }
        public  void setDate(String date)
        {
            TextView mydate=(TextView) mView.findViewById(R.id.all_users_profileStatus);
            mydate.setText("Friends since: "+date);
        }
    }
}