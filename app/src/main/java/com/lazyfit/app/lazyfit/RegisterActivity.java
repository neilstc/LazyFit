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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewSignin;
    private Button buttonRegister;
    private EditText editTextPassword1;
    private EditText editTextPassword2;
    private EditText editTextEmail;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        if(firebaseAuth.getCurrentUser()!=null){
            //profile activity here
            finish();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }

        progressDialog = new ProgressDialog(this);

        textViewSignin = (TextView) findViewById(R.id.tvRegister);
        buttonRegister = (Button) findViewById(R.id.bRegister);
        editTextEmail = (EditText) findViewById(R.id.etUserEmail);
        editTextPassword1 = (EditText) findViewById(R.id.etUserPassword1);
        editTextPassword2 = (EditText) findViewById(R.id.etUserPassword2);

        textViewSignin.setOnClickListener(this);
        buttonRegister.setOnClickListener(this);
    }


    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password1 = editTextPassword1.getText().toString().trim();
        String password2 = editTextPassword2.getText().toString().trim();

        if (email.length() == 0) {
            //email is empty
            Toast.makeText(this,"Please enter email",Toast.LENGTH_SHORT).show();
            //stop the function executing further
            return;
        }
        if(password1.length()==0 ){
            //password is empty
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
            //stop the function executing further
            return;
        }
        if(!password1.equals(password2)){
            //password and confirm are not the same
            Toast.makeText(this,"The Confirm and The Password Are Not Identical",Toast.LENGTH_SHORT).show();
            //stop the function executing further
            return;
        }
        //if validations are ok
        //we will first show a progressbar

        progressDialog.setMessage("Registering User...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email,password1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            //user is successfully registered and looged in
                            //we will start the profile activity here
                            String currentUserID = firebaseAuth.getCurrentUser().getUid();
                            RootRef.child("Users").child(currentUserID).setValue("");
                            Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else{
                            Toast.makeText(RegisterActivity.this,"Could Not Register Please Try Again",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {

        if(view ==buttonRegister ){
            registerUser();
        }else if(view == textViewSignin){
            finish();
            startActivity(new Intent(this,LoginActivity.class));
        }
    }
}
