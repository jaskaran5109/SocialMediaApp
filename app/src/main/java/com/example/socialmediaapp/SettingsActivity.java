package com.example.socialmediaapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    DatePicker datePicker;
    CircleImageView userprofileImage;
    EditText userStatus,userName,userFullname,userCountry,userGender,userRelationStatus;
    Button updateAccountsettings;
    TextView userDob;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    String currUserId;
    ProgressDialog loadingBar;
    FirebaseStorage firebaseStorage;
    int year=2001;
    int month=1;
    int dayOfMonth=1;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        auth=FirebaseAuth.getInstance();
        currUserId=auth.getCurrentUser().getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(currUserId);
        loadingBar=new ProgressDialog(this);
        firebaseStorage=FirebaseStorage.getInstance();
        userName=(EditText) findViewById(R.id.setting_username);
        userStatus=(EditText) findViewById(R.id.setting_status);
        userFullname=(EditText) findViewById(R.id.setting_fullname);
        userCountry=(EditText) findViewById(R.id.setting_country);
        userDob=(TextView) findViewById(R.id.setting_dob);
        userGender=(EditText) findViewById(R.id.setting_gender);
        userRelationStatus=(EditText) findViewById(R.id.setting_relationshipStatus);
        userprofileImage=(CircleImageView) findViewById(R.id.setting_profile_image);
        updateAccountsettings=(Button) findViewById(R.id.update_account_settings_btn);


        userDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(SettingsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                userDob.setText(day + "/" + (month+1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);
//                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
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
                    userName.setText(username);
                    userCountry.setText(country);
                    userDob.setText(dateOfBirth);
                    userGender.setText(gender);
                    userRelationStatus.setText(relationShipStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
        updateAccountsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeAccount();
            }
        });

        userprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                launchSomeActivity.launch(intent);
            }
        });
    }


//    public String getCurrentDate(){
//        StringBuilder builder=new StringBuilder();;
//        builder.append((datePicker.getMonth() + 1)+"/");//month is 0 based
//        builder.append(datePicker.getDayOfMonth()+"/");
//        builder.append(datePicker.getYear());
//        return builder.toString();
//    }
    private void ChangeAccount() 
    {
        String username=userName.getText().toString();
        String name=userFullname.getText().toString();
        String status=userStatus.getText().toString();
        String country=userCountry.getText().toString();
        String dob=userDob.getText().toString();
        String gender=userGender.getText().toString();
        String relationShip=userRelationStatus.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(name) || TextUtils.isEmpty(country) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(relationShip))
        {
            Toast.makeText(SettingsActivity.this, "Fields Cannot be Empty", Toast.LENGTH_SHORT).show();
        }else
        if(TextUtils.isEmpty(status))
        {
            Toast.makeText(SettingsActivity.this, "Please enter your status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            UpdateAccountInfo(username,name,status,country,dob,gender,relationShip);
        }

    }


    private void UpdateAccountInfo(String username, String name, String status, String country, String dob, String gender, String relationShip)
    {
        HashMap userMap=new HashMap<>();
        userMap.put("userName",username);
        userMap.put("fullName",name);
        userMap.put("status",status);
        userMap.put("country",country);
        userMap.put("dob",dob);
        userMap.put("gender",gender);
        userMap.put("relationShipStatus",relationShip);

        databaseReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    SendUsertoMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SendUsertoMainActivity()
    {
        Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    loadingBar.setTitle("Saving Information");
                    loadingBar.setMessage("Please wait, while we are updating your profile");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);
                    Intent data = result.getData();

                    if (result.getResultCode() == Activity.RESULT_OK && data.getData() != null) {

                        Uri sfile = data.getData();
//                        circleImageView.setImageURI(sfile);

                        final StorageReference reference = firebaseStorage.getReference().child("profile_picture")
                                .child(FirebaseAuth.getInstance().getUid());

                        reference.putFile(sfile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        databaseReference
                                                .child("profile_picture").setValue(uri.toString());
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        });
                    }
                }
            });
}