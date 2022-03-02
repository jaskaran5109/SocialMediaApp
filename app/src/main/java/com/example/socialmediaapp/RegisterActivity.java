package com.example.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText email,password,confirm_password;
    Button create_btn;
    FirebaseAuth auth;
    ProgressDialog loadingBar;
    TextView alreadyHasAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
        auth=FirebaseAuth.getInstance();
        email=(EditText) findViewById(R.id.register_email);
        password=(EditText) findViewById(R.id.register_password);
        confirm_password=(EditText) findViewById(R.id.register_confirm_password);
        create_btn=(Button) findViewById(R.id.register_create_account);
        loadingBar=new ProgressDialog(this);
        alreadyHasAccount=(TextView) findViewById(R.id.already_have_an_account);
        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                createNewAccount();
            }
        });

        alreadyHasAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUsertoLoginActivity();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser!=null)
        {
            sendUsertoMainActivity();
        }
    }
    private void sendUsertoMainActivity()
    {
        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUsertoLoginActivity()
    {
        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void createNewAccount()
    {
        String emaill=email.getText().toString();
        String passwordd=password.getText().toString();
        String confirmPasswordd=confirm_password.getText().toString();

        if(TextUtils.isEmpty(emaill))
        {
            Toast.makeText(RegisterActivity.this, "Email cannot be Empty", Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(passwordd))
        {
            Toast.makeText(RegisterActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(confirmPasswordd))
        {
            Toast.makeText(RegisterActivity.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
        }
        else
        if(!passwordd.equals(confirmPasswordd))
        {
            Toast.makeText(RegisterActivity.this, "Your password does not match", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait, while we are creating your account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            auth.createUserWithEmailAndPassword(emaill,passwordd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                Intent intent=new Intent(RegisterActivity.this,SetupActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message=task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}