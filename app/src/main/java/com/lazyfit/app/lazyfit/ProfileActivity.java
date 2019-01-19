package com.lazyfit.app.lazyfit;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity  {

    private String receiverUserID,group_name  ,currentState,currentUserID;
    private CircleImageView userProfileImage;
    private TextView userProfileName , userProfileAboutMe;
    private Button sendAddRequestButton;
    private DatabaseReference userRef , requestRef,groupRef;
    private FirebaseAuth mAuth;
    private ArrayList<String> groupMembers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initializeFields();
        retrieveUserInfo();
        groupRef.child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserID)){
                    sendAddRequestButton.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Group Add Request");
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        group_name = getIntent().getExtras().get("group_name").toString();
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_name);
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileAboutMe = findViewById(R.id.visit_user_aboutMe);
        sendAddRequestButton = findViewById(R.id.send_add_request_button);
        currentUserID = mAuth.getCurrentUser().getUid();
        currentState = "new";
        getGroupList();
    }

    private void getGroupList() {
        groupMembers =new ArrayList<>();
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    DataSnapshot mem =dataSnapshot.child("Members");
                    for(DataSnapshot info : mem.getChildren()) {
                        String uid = info.getKey();
                        groupMembers.add(uid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userAboutMe = dataSnapshot.child("aboutMe").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileAboutMe.setText(userAboutMe);
                    manageAddRequest();
                }else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userAboutMe = dataSnapshot.child("aboutMe").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileAboutMe.setText(userAboutMe);
                    manageAddRequest();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageAddRequest() {
        requestRef.child(receiverUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(group_name)){
                            String requestType = dataSnapshot.child(group_name).child("add_status").getValue().toString();
                            if(requestType.equals("request_sent")){
                                currentState ="request_sent";
                                sendAddRequestButton.setText("Cancel Request");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        boolean isEqual =false;
        sendAddRequestButton.setVisibility(View.INVISIBLE);
        for(String userID : groupMembers){
            if(receiverUserID.equals(userID)){
                isEqual =true;
                break;
            }
        }
        if(!isEqual){
            sendAddRequestButton.setVisibility(View.VISIBLE);
            sendAddRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendAddRequestButton.setEnabled(false);
                    if(currentState.equals("new")){
                        sendAddRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelRequest();
                    }
                }
            });
        }
    }

    private void cancelRequest() {
        requestRef.child(receiverUserID).child(group_name)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    sendAddRequestButton.setEnabled(true);
                    sendAddRequestButton.setText("Send add Request");
                    currentState = "new";
                }
            }
        });
    }

    private void sendAddRequest() {
        requestRef.child(receiverUserID).child(group_name)
                .child("add_status").setValue("request_sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            sendAddRequestButton.setEnabled(true);
                            currentState ="request_sent";
                            sendAddRequestButton.setText("Cancel Request");
                            requestRef.child(receiverUserID).child(group_name).child("name").setValue(group_name);
                            requestRef.child(receiverUserID).child(group_name).child("sender").setValue(currentUserID);
                            requestRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.child(receiverUserID).hasChild(group_name)){
                                        sendAddRequestButton.setEnabled(true);
                                        sendAddRequestButton.setText("Send add Request");
                                        currentState = "new";
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                });
    }
}
