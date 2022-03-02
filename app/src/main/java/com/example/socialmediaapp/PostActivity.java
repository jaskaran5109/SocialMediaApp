package com.example.socialmediaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageButton select_post_image;
    EditText post_description;
    Button update_post_btn;
    Uri imageUri;
    String description;
    DatabaseReference databaseReference, postReference;
    StorageReference postImageReference;
    String saveCurrentDate, saveCurrentTime, postRandomName, ImageUrl;
    long countPost;
    FirebaseAuth auth;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        toolbar = (Toolbar) findViewById(R.id.update_post_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");
        select_post_image = (ImageButton) findViewById(R.id.select_post_image);
        post_description = (EditText) findViewById(R.id.post_description);
        update_post_btn = (Button) findViewById(R.id.update_post_btn);
        auth = FirebaseAuth.getInstance();
        postImageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        loadingBar=new ProgressDialog(this);

        select_post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        update_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePostDescription();
            }
        });


    }

    private void validatePostDescription() {
        description = post_description.getText().toString();
        if (imageUri == null) {
            Toast.makeText(PostActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(PostActivity.this, "Please write something about your pic", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait, while we are updating your post");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            storingImagetoFirebaseStorage();
        }
    }

    private void storingImagetoFirebaseStorage() {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currTime.format(callForDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        StorageReference filePath = postImageReference.child("Post Images")
                .child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

//        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                if (task.isSuccessful()) {
//                    ImageUrl = task.getMetadata().getReference().getDownloadUrl().toString();
//                    Toast.makeText(PostActivity.this, "Image Uploaded Successfully to Storage", Toast.LENGTH_SHORT).show();
//
//                    SavingPostInfoToDatabase();
//                } else {
//                    Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task=taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUrl=uri.toString();
                        Toast.makeText(PostActivity.this, "Image Uploaded Successfully to Storage", Toast.LENGTH_SHORT).show();
                        SavingPostInfoToDatabase();

                    }
                });
            }
        });
    }
    private long getcurrentTimeStamp()
    {
        Long timestamp=System.currentTimeMillis()/100;
        return timestamp;
    }


    private void SavingPostInfoToDatabase() {

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    countPost=snapshot.getChildrenCount();
                }
                else
                {
                    countPost=0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userfullName = snapshot.child("fullName").getValue().toString();
                    String profile_image = snapshot.child("profile_picture").getValue().toString();
                    HashMap postMap = new HashMap();
                    postMap.put("uid", auth.getCurrentUser().getUid());
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", description);
                    postMap.put("fullName", userfullName);
                    postMap.put("postImage", ImageUrl);
                    postMap.put("profileImage", profile_image);
                    postMap.put("counter", countPost);
                    postMap.put("timeStamp", getcurrentTimeStamp());

                    postReference.child(auth.getCurrentUser().getUid() + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                sendusertoMainActivity();
                                Toast.makeText(PostActivity.this, "New Post is updated successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void OpenGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 33);
//        launchSomeActivity.launch(intent);
    }
//    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    Intent data = result.getData();
//                    if (result.getResultCode() == Activity.RESULT_OK && data.getData() != null) {
////                        imageUri=data.getData();
////                        select_post_image.setImageURI(imageUri);
//                    }
//                }
//            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data.getData() != null) {
            imageUri = data.getData();
            select_post_image.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            sendusertoMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendusertoMainActivity() {
        Intent intent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(intent);
    }
}