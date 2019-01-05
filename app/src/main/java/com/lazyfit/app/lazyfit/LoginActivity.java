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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewRegister, forgetPassword;
    private Button buttonLogin;
    private EditText editTextEmail,editTextPassword;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null){
            //profile activity here
            finish();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
        progressDialog = new ProgressDialog(this);

        textViewRegister = (TextView) findViewById(R.id.tvRegister);
        buttonLogin = (Button) findViewById(R.id.bRegister);
        editTextEmail = (EditText) findViewById(R.id.etUserEmail);
        editTextPassword = (EditText) findViewById(R.id.etUserPassword);
        forgetPassword =(TextView) findViewById(R.id.forget_password_link);
        textViewRegister.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);

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
        progressDialog.setMessage("Login in progress");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()){
                            //start the profile activity
                            finish();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view){
        if(view ==buttonLogin ){
            userLogin();
        }else if(view == textViewRegister){
            finish();
            startActivity(new Intent(this,RegisterActivity.class));
        }
    }
}
