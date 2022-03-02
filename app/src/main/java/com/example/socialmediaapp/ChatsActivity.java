package com.example.socialmediaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsActivity extends AppCompatActivity {


    RecyclerView userMessageList;
    ImageButton imageBtn, sendBtn;
    EditText inputTextMsg;
    String id, name;
    TextView username, userlastSeen;
    CircleImageView user_profile_image;
    FirebaseAuth auth;
    String msgsenderId, saveCurrentDate, saveCurrentTime;
    DatabaseReference databaseReference, userRef;
    final List<Messages> messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessagesAdapter messagesAdapter;
    String checker = "", muUrl = "";
    Uri fileUri;
    StorageTask uploadtask;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

//        getSupportActionBar().hide();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        auth = FirebaseAuth.getInstance();
        msgsenderId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        id = getIntent().getExtras().get("visited_user_id").toString();

        imageBtn = (ImageButton) findViewById(R.id.chat_ImageBtn);
        sendBtn = (ImageButton) findViewById(R.id.chat_sendBtn);
        inputTextMsg = (EditText) findViewById(R.id.chat_input);
        loadingBar = new ProgressDialog(this);

        username = (TextView) findViewById(R.id.custom_profile_name);
        user_profile_image = (CircleImageView) findViewById(R.id.custom_profile_image);
        userlastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);

        DisplayReceiverInfo();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        FetchMessages();

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{
                        "Image",
                        "Pdf files",
//                        "Ms Word files"

                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatsActivity.this);
                builder.setTitle("Select option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            launchSomeActivity.launch(intent);
                        }
                        if (i == 1) {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            launchSomeActivity.launch(intent);
                        }
                        if (i == 2) {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                            launchSomeActivity.launch(intent);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Intent data = result.getData();

                    if (result.getResultCode() == Activity.RESULT_OK && data.getData() != null && data != null) {
                        loadingBar.setTitle("Uploading image");
                        loadingBar.setMessage("Please wait, while we are sending your file");
                        loadingBar.show();

                        loadingBar.setCanceledOnTouchOutside(true);
                        fileUri = data.getData();

                        if (!checker.equals("image")) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                            String message_sender_ref = "Messages/" + msgsenderId + "/" + id;
                            String message_receiver_ref = "Messages/" + id + "/" + msgsenderId;
                            DatabaseReference user_message_key = databaseReference.child("Messages").child(msgsenderId).child(id)
                                    .push();
                            String message_push_id = user_message_key.getKey();
                            StorageReference filePath = storageReference.child(message_push_id + "." + checker);
                            filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        HashMap messageMap = new HashMap();
                                        messageMap.put("message", task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                                        messageMap.put("name", fileUri.getLastPathSegment());
                                        messageMap.put("time", saveCurrentTime);
                                        messageMap.put("date", saveCurrentDate);
                                        messageMap.put("from", msgsenderId);
                                        messageMap.put("to", id);
                                        messageMap.put("type", checker);
                                        messageMap.put("to", id);
                                        messageMap.put("messageId", message_push_id);
                                        HashMap messageBodyDetail = new HashMap();
                                        messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageMap);
                                        messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageMap);

                                        databaseReference.updateChildren(messageBodyDetail);
                                        loadingBar.dismiss();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    double p = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                    loadingBar.setMessage((int) p + " % Uploading");
                                }

                                ;
                            });


                        } else if (checker.equals("image")) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                            String message_sender_ref = "Messages/" + msgsenderId + "/" + id;
                            String message_receiver_ref = "Messages/" + id + "/" + msgsenderId;
                            DatabaseReference user_message_key = databaseReference.child("Messages").child(msgsenderId).child(id)
                                    .push();
                            String message_push_id = user_message_key.getKey();
                            StorageReference filePath = storageReference.child(message_push_id + "." + "jpg");

                            uploadtask = filePath.putFile(fileUri);
                            uploadtask.continueWithTask(new Continuation() {
                                @Override
                                public Object then(@NonNull Task task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    return filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUrl = task.getResult();
                                        muUrl = downloadUrl.toString();

                                        HashMap messageMap = new HashMap();
                                        messageMap.put("message", muUrl);
                                        messageMap.put("name", fileUri.getLastPathSegment());
                                        messageMap.put("time", saveCurrentTime);
                                        messageMap.put("date", saveCurrentDate);
                                        messageMap.put("type", checker);
                                        messageMap.put("from", msgsenderId);
                                        messageMap.put("to", id);
                                        messageMap.put("messageId", message_push_id);

                                        HashMap messageBodyDetail = new HashMap();
                                        messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageMap);
                                        messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageMap);

                                        databaseReference.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ChatsActivity.this, "Image send successfully", Toast.LENGTH_SHORT).show();
                                                    inputTextMsg.setText("");
                                                    loadingBar.dismiss();
                                                } else {
                                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    inputTextMsg.setText("");
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });

                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ChatsActivity.this, "Nothing Selected", Toast.LENGTH_SHORT).show();
                        }


                    }
                }
            });

    private void FetchMessages() {
        databaseReference.child("Messages").child(msgsenderId).child(id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    Messages messages = snapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void updateUserStatus(String state) {
        String saveCurrentDate, saveCurrentTime;
        Calendar callDate = Calendar.getInstance();
        SimpleDateFormat currDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currDate.format(callDate.getTime());

        Calendar callTime = Calendar.getInstance();
        SimpleDateFormat currTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currTime.format(callTime.getTime());

        HashMap currStateMap = new HashMap();
        currStateMap.put("time", saveCurrentTime);
        currStateMap.put("date", saveCurrentDate);
        currStateMap.put("type", state);

        userRef.child(msgsenderId).child("userState")
                .updateChildren(currStateMap);
    }

    private void SendMessage() {
        updateUserStatus("online");
        String msgtext = inputTextMsg.getText().toString();
        if (TextUtils.isEmpty(msgtext)) {
            Toast.makeText(ChatsActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            String message_sender_ref = "Messages/" + msgsenderId + "/" + id;
            String message_receiver_ref = "Messages/" + id + "/" + msgsenderId;
            DatabaseReference user_message_key = databaseReference.child("Messages").child(msgsenderId).child(id)
                    .push();
            String message_push_id = user_message_key.getKey();

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currTime.format(callForDate.getTime());

            HashMap messageMap = new HashMap();
            messageMap.put("message", msgtext);
            messageMap.put("time", saveCurrentTime);
            messageMap.put("date", saveCurrentDate);
            messageMap.put("type", "text");
            messageMap.put("from", msgsenderId);
            messageMap.put("to", id);
            messageMap.put("messageId", message_push_id);

            HashMap messageBodyDetail = new HashMap();
            messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageMap);
            messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageMap);

            databaseReference.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                        inputTextMsg.setText("");
                    } else {
                        Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());

                        inputTextMsg.setText("");
                    }
                }
            });


        }

    }


    private void DisplayReceiverInfo() {

        databaseReference.child("Users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileImage = snapshot.child("profile_picture").getValue().toString();
                    String userName = snapshot.child("fullName").getValue().toString();
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(user_profile_image);
                    username.setText(userName);
                    if (snapshot.hasChild("userState")) {
                        String lastSeen = snapshot.child("userState").child("type").getValue().toString();
                        String lastDate = snapshot.child("userState").child("date").getValue().toString();
                        String lastTime = snapshot.child("userState").child("time").getValue().toString();
                        if (lastSeen.equals("online")) {
                            userlastSeen.setText(lastSeen);
                        } else {
                            userlastSeen.setText("last seen: " + lastTime + " " + lastDate);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}