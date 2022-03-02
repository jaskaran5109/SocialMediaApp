package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    CircleImageView userprofileImage;
    TextView userStatus, userName, userFullname, userCountry, userDob, userGender, userRelationStatus;
    DatabaseReference databaseReference, friendRequestReference, friendsRef;
    FirebaseAuth auth;
    String senderUserId, receiverUserId, currUserId;
    Button sendRequest, declineRequest;
    String CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);


        auth = FirebaseAuth.getInstance();
        currUserId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("FriendsRequests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        receiverUserId = getIntent().getExtras().get("visited_user_id").toString();
        senderUserId = auth.getCurrentUser().getUid();


        userName = (TextView) findViewById(R.id.person_userName);
        userStatus = (TextView) findViewById(R.id.person_status);
        userFullname = (TextView) findViewById(R.id.person_fullName);
        userCountry = (TextView) findViewById(R.id.person_country);
        userDob = (TextView) findViewById(R.id.person_dob);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelationStatus = (TextView) findViewById(R.id.person_relationShipStatus);
        userprofileImage = (CircleImageView) findViewById(R.id.person_profile_pic);

        sendRequest = (Button) findViewById(R.id.person_sendFriendRequest);
        declineRequest = (Button) findViewById(R.id.person_deleteFriendRequest);

        CURRENT_STATE = "not_friends";



        databaseReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String image = snapshot.child("profile_picture").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();
                    String fullName = snapshot.child("fullName").getValue().toString();
                    String username = snapshot.child("userName").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String dateOfBirth = snapshot.child("dob").getValue().toString();
                    String gender = snapshot.child("gender").getValue().toString();
                    String relationShipStatus = snapshot.child("relationShipStatus").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(userprofileImage);
                    userStatus.setText(status);
                    userFullname.setText(fullName);
                    userName.setText("@" + username);
                    userCountry.setText("Country: " + country);
                    userDob.setText("DOB: " + dateOfBirth);
                    userGender.setText("Gender: " + gender);
                    userRelationStatus.setText("Relationship: " + relationShipStatus);
                    userprofileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(PersonProfileActivity.this,ImageViewerActivity.class);
                            intent.putExtra("url",image);
                            startActivity(intent);
                        }
                    });
                    MaintananceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        declineRequest.setVisibility(View.INVISIBLE);
        declineRequest.setEnabled(false);

        if (!senderUserId.equals(receiverUserId)) {
            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendRequest.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends")) {
                        SendFriendRequestFunc();
                    }
                    if (CURRENT_STATE.equals("request_sent")) {
                        CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        UnfriendExistingFriend();
                    }
                }
            });
        } else {
            declineRequest.setVisibility(View.INVISIBLE);
            sendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void UnfriendExistingFriend() {
        friendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendRequest.setText("Send Friend Request");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void AcceptFriendRequest() {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currDate.format(callForDate.getTime());

        friendsRef.child(senderUserId).child(receiverUserId)
                .child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        friendRequestReference.child(senderUserId).child(receiverUserId)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestReference.child(receiverUserId).child(senderUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                sendRequest.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendRequest.setText("Unfriend");

                                                                                declineRequest.setVisibility(View.INVISIBLE);
                                                                                declineRequest.setEnabled(false);
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


    private void MaintananceOfButtons() {
        friendRequestReference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserId)) {
                    String request_type = snapshot.child(receiverUserId)
                            .child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        CURRENT_STATE = "request_sent";
                        sendRequest.setText("Cancel Friend Request");
                        declineRequest.setVisibility(View.INVISIBLE);
                        declineRequest.setEnabled(false);
                    } else if (request_type.equals("received")) {
                        CURRENT_STATE = "request_received";
                        sendRequest.setText("Accept Friend Request");
                        declineRequest.setVisibility(View.VISIBLE);
                        declineRequest.setEnabled(true);
                        declineRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelFriendRequest();
                            }
                        });

                    }
                }else {

                        friendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(receiverUserId)) {
                                    CURRENT_STATE = "friends";
                                    sendRequest.setText("Unfriend");
                                    declineRequest.setVisibility(View.INVISIBLE);
                                    declineRequest.setEnabled(false);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SendFriendRequestFunc() {
        friendRequestReference.child(senderUserId)
                .child(receiverUserId)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestReference.child(receiverUserId)
                                    .child(senderUserId)
                                    .child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendRequest.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendRequest.setText("Cancel Friend Request");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestReference.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestReference.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendRequest.setText("Send Friend Request");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
