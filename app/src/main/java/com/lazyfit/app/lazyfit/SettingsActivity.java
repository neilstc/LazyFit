package com.lazyfit.app.lazyfit;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity{

    private Button b_updateAccountSetting;
    private EditText et_UserName, etAboutMe;
    private CircleImageView cv_userProfileImage;
    private String currentUserID, setUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private StorageReference userProfileImageRef ;

    private static final  int galleryPick = 1;
    private Toolbar settingsTollBar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        initializeFields();
        retrieveUserInfo();
        et_UserName.setVisibility(View.INVISIBLE);

        b_updateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });



        cv_userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Picture"),galleryPick);
            }
        });
    }


    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Users").child(currentUserID).child("profileImage");
        b_updateAccountSetting =findViewById(R.id.update_setting_button);
        et_UserName =  findViewById(R.id.set_user_name);
        etAboutMe =  findViewById(R.id.set_about_me);
        cv_userProfileImage = findViewById(R.id.set_profile_image);
        settingsTollBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsTollBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Setting");
        textView = findViewById(R.id.twSettings);
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    setUserName =dataSnapshot.child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

//TODO fix all the bugs in the add picture functionality
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryPick && resultCode ==RESULT_OK && data !=null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(requestCode ==RESULT_OK){
                Uri resultUri = result.getUri();
                StorageReference filePath = userProfileImageRef.child(currentUserID+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String message =task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettings(){
        setUserName = et_UserName.getText().toString();
        String setUserAboutMe = etAboutMe.getText().toString();
        if(TextUtils.isEmpty((setUserName))){
            Toast.makeText(this,"Enter User Name Please",Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("aboutMe",setUserAboutMe);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                Toast.makeText(SettingsActivity.this,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
                                finish();
                                sendUserToMainActivity();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error: "+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity(){
        Intent mainIntent =new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void retrieveUserInfo(){
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("profileImage")){
                            setUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserAboutMe = dataSnapshot.child("aboutMe").getValue().toString();
                            String retrieveUserProfileImage = dataSnapshot.child("profileImage").getValue().toString();

                            et_UserName.setText(setUserName);
                            etAboutMe.setText(retrieveUserAboutMe);

                        }else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){
                            setUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserAboutMe = dataSnapshot.child("aboutMe").getValue().toString();
                            et_UserName.setText(setUserName);
                            etAboutMe.setText(retrieveUserAboutMe);

                        }else{
                            et_UserName.setVisibility((View.VISIBLE));
                            Toast.makeText(SettingsActivity.this,"Please set & update your profile information",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
