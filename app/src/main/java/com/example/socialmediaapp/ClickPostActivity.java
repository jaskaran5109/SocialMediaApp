package com.example.socialmediaapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    ImageView postImage;
    TextView postDescription;
    Button editButton, deleteButton;
    String PostKey, databaseUserId, description, Image, currUserID;
    DatabaseReference databaseReference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        auth = FirebaseAuth.getInstance();
        currUserID = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Posts")
                .child(PostKey);

        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        editButton = (Button) findViewById(R.id.editBtn);
        deleteButton = (Button) findViewById(R.id.deleteBtn);

        deleteButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.INVISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    description = snapshot.child("description").getValue().toString();
                    Image = snapshot.child("postImage").getValue().toString();
                    databaseUserId = snapshot.child("uid").getValue().toString();

                    postDescription.setText(description);

                    Picasso.get().load(Image).into(postImage);
                    if (currUserID.equals(databaseUserId)) {
                        deleteButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.VISIBLE);
                    }
                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditCurrPost(description);
                        }
                    });
                    postImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(ClickPostActivity.this,ImageViewerActivity.class);
                            intent.putExtra("url",Image);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentPost();
                Toast.makeText(ClickPostActivity.this, "Post has been deleted", Toast.LENGTH_SHORT).show();
            }
        });




    }

    private void EditCurrPost(String description)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputfield=new EditText(ClickPostActivity.this);
        inputfield.setText(description);
        builder.setView(inputfield);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                databaseReference.child("description").setValue(inputfield.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Updated Successfully...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.text);
    }

    private void DeleteCurrentPost() {
        databaseReference.removeValue();
        Intent intent = new Intent(ClickPostActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}