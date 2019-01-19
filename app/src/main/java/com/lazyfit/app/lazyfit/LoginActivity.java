package com.lazyfit.app.lazyfit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewRegister, forgetPassword;
    private Button buttonLogin;
    private EditText editTextEmail,editTextPassword;

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){
            //profile activity here
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
        initialize();

        forgetPassword.setClickable(true);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });


    }

    private void initialize() {
        progressDialog = new ProgressDialog(this);
        textViewRegister =  findViewById(R.id.tvRegister);
        buttonLogin =  findViewById(R.id.bRegister);
        editTextEmail =  findViewById(R.id.etUserEmail);
        editTextPassword =  findViewById(R.id.etUserPassword);
        forgetPassword = findViewById(R.id.forget_password_link);
        textViewRegister.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        userRef =FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void userLogin(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.length() == 0) {
            //email is empty
            Toast.makeText(this,"Please enter email",Toast.LENGTH_SHORT).show();
            //stop the function executing further
            return;
        }
        if(password.length()==0 ){
            //password is empty
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
            //stop the function executing further
            return;
        }
        progressDialog.setTitle("Sign In");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()){
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            userRef.child(currentUserId).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                //start the profile activity
                                                sendUserToMainActivity();
                                                finish();
                                            }
                                        }
                                    });
                        }else{
                            progressDialog.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(LoginActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view){
        if(view ==buttonLogin ){
            userLogin();
        }else if(view == textViewRegister){
            startActivity(new Intent(this,RegisterActivity.class));
            finish();
        }
    }

    private void sendUserToMainActivity(){
        Intent mainIntent =new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
