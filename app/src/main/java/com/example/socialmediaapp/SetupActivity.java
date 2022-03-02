package com.example.socialmediaapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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

public class SetupActivity extends AppCompatActivity {

    Spinner spinner;

    String [] names = {"Afghanistan (AF)","Aland Island (AX)","Albania(AL)","Algeria (DZ)","American Samoa (AS)","Andorra(AD)","Angola (AO)","Anguilla (Al)","Antartica(AQ)","Antigua and Barbuda (AG)","Aregentina(AG)","Armenia (AM)","Aruba (AW)","Australia (AU)","Austria (AT)","Azerbaijan (AZ)","Bahamas (BS)","Bahrain (BH)","Bangladesh (BD)","Barbados (BB)","Belarus (BY)","belgium (BE)","Belize (BZ)","Benin (BJ)","Bermuda (BM)","Bhutan (BT)",

            "Bolivia, Pleurinational State of (BO)","Bosnia and Herzegovina (BA)","Botswana (BW)","Brazil (BR)","British Indian Ocean Territory (IO)","British Virgin Islands (VG)","Brunei Darussalam (BN)","Bulgaria ( BG)","Burkina Faso (BF)","Burundi (BI)","Cambodia (KH)","Cameroon (CM)","Canada (CA)","Cape Verde (CV)","Cayman Islands (KY)","Central African Republic (CF)","Chad (TD)","Chile (CL)","China (CN)","Christmas Island (CX)","Cocos(kelling) Islands (CC)","Colombia (CO)","Comoros (KM)","Congo (CG)","Congo, The Democratic Republic Of The (CD)","Cook Islands (CK)","Costa Rica (CR)","Cote divoire (CL)","Croatia (HR)","Cuba (CU)","Curacao(CW)","Cyprus (CY)","Czech Republic (CZ)","Denmark (DK)","Dijibouti (DJ)","Dominica (DM)","Dominican Republic (DO)","Ecuador (EC)","Egypt (EG)","El Salvador (SV)","Equatorial Guiena(GQ)","Eritrea (ER)","Estonia (EE)","Ethioia (ET)","Falkland Islands (malvinas) (FK)","Faroe Islands (FO)","Fiji (FJ)","Finland (FL)","France (FR)","French Guyana (GF)","French Polynesia (PF)","Gabon (GA)","Gambia (GM)","Georgia (GE)","Germany (DE)","Ghana (GH)","Gibralter (Gl)","Greece (GR)","Greenland (GL)","Grenada (GD)","Guadeloupe (GP)","Guam (GU)","Guatemala (GT)","Guemsey (GG)","Guinea (GN)","Guinea-bissau (GW)","Guyana (GY)","Haiti (HT)","Holy See (Vatican City State) (VA)","Honduras (HN)","Hong Kong (HK)","Hungary (HU)","Iceland (IS)","India (IN)","Indonesia (ID)","Iran, Islamic Republic Of (IR)","Iraq (IQ)","Ireland (IE)","Isle Of Man (IM)","Israel (IL)","Italy (IT)","Jamaica (JM)","Japan (JP)","Jersey (JE)","Jordan (JO)","Kazakhstan (KZ)","Kenya (KE)","Kiribati (Kl)","Kosovo (XK)","Kuwait (KW)","Kyrgyzstan (KG)","Lao People's Democratic Republic (LA)","Latvia (LV)","Lebanon (LB)","Lesotho (LS)","Liberia (LR)","Libya (LY)","Liechtenstein (LI)","Lithuania (LT)","Luxembourg (LU)","Macau (MO)","Macedonia (FYROM) (MK)","Madagascar (MG)","Malawi (MW)","Malaysia (MY)","Maldives (MV)","Mali (ML)","Malta (ML)","Marshall Islands (MH)","Martinique (MQ)","Mauritania (MR)","Mauritius (MU)","Mayotte (YT)","Maxico (MX)","Micronesia, Federated States Of  (FM)","Moldova, Republic Of (MD)","Monaco (MC)","Mangolia (MN)","Montenegro (ME)","Montserrat (MS)","Morocco (MA)","Mozambique (MZ)","Myanmar (MM)","Namibia (NA)","Nauru (NR)","Nepal (Np)","Netherlands (NL)","New Caledonia (NC)","New Zealand (NZ)","Nicaragua (NI)","Niger (NE)","Nigeria (NG)","Niue (NU)","Norfolk islands (NF)","North Korea (KP)","Northern Mariana islands (MP)","Norway (NO)","Oman (OM)","Pakistan (PK)","Palau (PW)","Palestine (PS)","Panama (PA)","Papua New Guinea (PG)","Paraguay (PY)","Peru (PE)","Philipines (PH)","Pitcaim Islands (PN)","Poland (PL)","Portugal (PT)","Puerto Rico (PR)","Qatar (QA)","Reunion (RE)","Romania (RO)","Russian Federation (RU)","Rawanda (RW)","Saint Barthelelemy (BL)","Saint Helena,Ascension And Tristan Da Cunha(SH)","Saint Kitts and Nevis (KN)","Saint Lucia (LC)","Saint Martin (MF)","Saint Pierre And Miquelon (PM)","Saint Vincent & The Grenadines (VC)","Samoa (WS)","San Marino (SM)","Sao Tome And Principe (ST)","Saudi Arabia (SA)","Senegal (SN)","Serbia (RS)","Seychelles (SC)","Sierra Leone (SL)","Singapore (SG)","Sint Marten (SX)","Slovakia (SK)","Slovenia (SL)","Solomon Islands (SB)","Somalia (SO)","South Africa (ZA)","South Korea (KR)","South Sudan (SS)","Spain (ES)","Sri Lanka (LK)","Sudan (SD)","Suriname (SR)","Swaziland (SZ)","Sweden (SE)","Switzerland (CH)","Syrian Arab Republic (SY)","Taiwan (TW)","Tajikistan (TJ)","Tanzania, United Republic Of (TZ)","Thailand (TH)","Timor-leste (TL)","Togo (TG)"," Tokelau(TK)","Tonga (TO)","Trinidad & Tabago (TT)","Tunisisa (TN)","Turkey (TR)","Turkmenistan (TM)","Turks and Caicos Islands (TC)","Tuvalu (TV)","Uganda (UG)","Ukraine (UA)","United Arab Emirates (UAE) (AE)","United Kingdom (GB)","United States (US)","Urguay (UY)","US Virgin Islands (VI)","Uzbekistan (UZ)","Vanuatu (VU)","Venezuela, Bolivarian Republic Of (VE)","Vietnam (VN)","Wallis And Futuna (WF)","Yemen (YE)","Zambia (ZM)","Zimbabwe (ZW)"};

    EditText username, name,gender,country,relationShipStatus;
    Button button;
    CircleImageView circleImageView;
    FirebaseAuth auth;
    DatabaseReference database;
    String curr_user;
    ProgressDialog loadingBar;
    StorageReference userProfileImageRef;
    FirebaseStorage firebaseStorage;
    FirebaseDatabase database2;
    String image="",item="";
    TextView dob;
    int year=2001;
    int month=1;
    int dayOfMonth=1;
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        username = (EditText) findViewById(R.id.userName);
        name = (EditText) findViewById(R.id.fullName);
//        country = findViewById(R.id.country);

        spinner=(Spinner) findViewById(R.id.country);
        ArrayAdapter<CharSequence> dataAdapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item, names);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(dataAdapter);
        spinner.setPrompt("Select Country");


         button = (Button) findViewById(R.id.button_save);
        circleImageView = (CircleImageView) findViewById(R.id.profile_image_icon);
        gender=(EditText) findViewById(R.id.Gender);
        dob=(TextView) findViewById(R.id.dob);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(SetupActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                dob.setText(day + "/" + (month+1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);
//                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
        relationShipStatus=(EditText) findViewById(R.id.relationShipStatus);

        loadingBar = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        curr_user = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference().child("Users").child(curr_user);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");
        firebaseStorage=FirebaseStorage.getInstance();
        database2=FirebaseDatabase.getInstance();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountInformation();
            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                launchSomeActivity.launch(intent);
            }
        });
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if(snapshot.hasChild("profile_picture"))
                    {
                        image=snapshot.child("profile_picture").getValue().toString();
                        if(TextUtils.isEmpty(image))
                        {
                            Toast.makeText(SetupActivity.this, "Please select profile Image", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Picasso.get().load(image).placeholder(R.drawable.profile)
                                    .into(circleImageView);
                        }

                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Profile image error...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    loadingBar.setTitle("Saving Information");
                    loadingBar.setMessage("Please wait, while we are saving your profile");
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
                                        database2.getReference().child("Users")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("profile_picture").setValue(uri.toString());
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        });
                    }
                }
            });

    private void saveAccountInformation() {
        String userName = username.getText().toString();
        String fullName = name.getText().toString();
//        String Usercountry = country.getText().toString();
        String userGender = gender.getText().toString();
        String UserDob = dob.getText().toString();
        String UserRelationship = relationShipStatus.getText().toString();


        if (TextUtils.isEmpty(image) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(String.valueOf(spinner.getSelectedItem())) ||
                TextUtils.isEmpty(userGender) || TextUtils.isEmpty(UserDob) || TextUtils.isEmpty(UserRelationship)
        ) {
            Toast.makeText(SetupActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are saving your data");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            HashMap userMap = new HashMap();
            userMap.put("userName", userName);
            userMap.put("fullName", fullName);
            userMap.put("country", String.valueOf(spinner.getSelectedItem()));
            userMap.put("status", "");
            userMap.put("gender", userGender);
            userMap.put("dob", UserDob);
            userMap.put("relationShipStatus", UserRelationship);
            database.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Account created Successfully", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    } else {
                        Toast.makeText(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}