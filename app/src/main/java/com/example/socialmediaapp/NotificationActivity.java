package com.example.socialmediaapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView request_list;
    DatabaseReference requestRef, userRef, friendsRef;
    FirebaseAuth auth;
    String currUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        auth = FirebaseAuth.getInstance();
        currUserId = auth.getCurrentUser().getUid();
        requestRef = FirebaseDatabase.getInstance().getReference().child("FriendsRequests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        request_list = (RecyclerView) findViewById(R.id.user_request_list);
        request_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        request_list.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        Query searchPeople = requestRef.child(currUserId);
        FirebaseRecyclerAdapter<FindFriends, RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, RequestViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        NotificationActivity.RequestViewHolder.class,
                        searchPeople
                ) {
            @Override
            protected void populateViewHolder(RequestViewHolder requestViewHolder, FindFriends findFriends, int i) {
                requestViewHolder.acceptBtn.setVisibility(View.VISIBLE);
                requestViewHolder.cancelBtn.setVisibility(View.VISIBLE);

                String listUserId = getRef(i).getKey();
                DatabaseReference getType = getRef(i).child("request_type").getRef();
                getType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String type = snapshot.getValue().toString();
                            if (type.equals("received")) {
                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("profile_picture")) {
                                            String requestimage = snapshot.child("profile_picture").getValue().toString();

                                            Picasso.get().load(requestimage).into(requestViewHolder.myImage);


                                        }
                                        String requestname = snapshot.child("fullName").getValue().toString();
                                        String requeststatus = snapshot.child("status").getValue().toString();
                                        requestViewHolder.myName.setText(requestname);
                                        requestViewHolder.myStatus.setText(requeststatus);


                                        requestViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
                                                builder.setTitle("Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (i == 0) {
                                                            Calendar callForDate = Calendar.getInstance();
                                                            SimpleDateFormat currDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                            String saveCurrentDate = currDate.format(callForDate.getTime());

                                                            friendsRef.child(currUserId).child(listUserId).child("date").setValue(saveCurrentDate)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                friendsRef.child(listUserId).child(currUserId).child("date").setValue(saveCurrentDate)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {

                                                                                                    requestRef.child(currUserId).child(listUserId)
                                                                                                            .removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()) {
                                                                                                                        requestRef.child(listUserId).child(currUserId)
                                                                                                                                .removeValue()
                                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                    @Override
                                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                        if (task.isSuccessful()) {
                                                                                                                                            Toast.makeText(NotificationActivity.this, "Friend request accepted successfully", Toast.LENGTH_SHORT).show();
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                });
                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                        if (i == 1) {
                                                            requestRef.child(currUserId).child(listUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                requestRef.child(listUserId).child(currUserId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Toast.makeText(NotificationActivity.this, "Friend request declined successfully", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });


                                                        }
                                                    }
                                                });
                                                builder.show();

                                            }
                                        });
                                        requestViewHolder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                requestRef.child(currUserId).child(listUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    requestRef.child(listUserId).child(currUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(NotificationActivity.this, "Friend request declined successfully", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        };
        request_list.setAdapter(firebaseRecyclerAdapter);

        super.onStart();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        Button acceptBtn, cancelBtn;
        CircleImageView myImage;
        TextView myName, myStatus;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            myImage = (CircleImageView) itemView.findViewById(R.id.all_users_profileImage);
            myName = (TextView) itemView.findViewById(R.id.all_users_profileName);
            myStatus = (TextView) itemView.findViewById(R.id.all_users_profileStatus);
            acceptBtn = (Button) itemView.findViewById(R.id.accept_btn);
            cancelBtn = (Button) itemView.findViewById(R.id.cancel_btn);
        }

    }
}