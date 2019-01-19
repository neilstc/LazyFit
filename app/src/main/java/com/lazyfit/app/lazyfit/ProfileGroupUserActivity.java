package com.lazyfit.app.lazyfit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileGroupUserActivity extends AppCompatActivity {

    private String receiverUserID,receiverUserName,group_name ,currentUserID,currentUserName,groupManagerID;
    private CircleImageView userProfileImage;
    private TextView userProfileName , userProfileAboutMe, userLastSeen;
    private Button deleteMemberButton , sendRemainderButton , sendMessageButton;
    private DatabaseReference usersRef,groupRef , notificationRef ,databaseReference;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_group_user);
        initializeFields();
        retrieveUserInfo();
        sendRemainderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPushNotification();
            }
        });
        deleteMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMember();
            }
        });
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Send Message");
        alert.setMessage("Enter text here..");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String body = input.getText().toString();
                writeNewPost(receiverUserID,currentUserName,group_name,body);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void writeNewPost(String userId, String author, String title, String body) {
        String key = databaseReference.push().getKey();
//        Post post = new Post(userId, username, title, body);
        Post post = new Post(author, title, body);

        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        childUpdates.put("/Users/"+receiverUserID+"/Messages/"+key,"Message From "+group_name+" at: "+currentDateTime);
        childUpdates.put("/posts/" + key, postValues);
        databaseReference.updateChildren(childUpdates);
    }

    private void sendPushNotification() {
            HashMap<String, String> massegeNotificationMap = new HashMap<>();
            massegeNotificationMap.put("from", currentUserID);
            massegeNotificationMap.put("type", "BroadCast");
            notificationRef.child(receiverUserID).push()
                    .setValue(massegeNotificationMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                notificationRef.child(receiverUserID).removeValue();
                            }
                        }
                    });

    }

    private void deleteMember(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileGroupUserActivity.this);
        builder.setTitle("Deletion Confirmation");
        builder.setMessage("Are You Sure You Want To Delete " + receiverUserName + "?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.setTitle("Deleting");
                progressDialog.setMessage("Please Wait..");
                progressDialog.show();
                groupRef.child("Members").child(receiverUserID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileGroupUserActivity.this, "Deleted "+receiverUserName+" From Group", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ProfileGroupUserActivity.this,GroupActivity.class));
//                                    finish();
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        group_name = getIntent().getExtras().get("group_name").toString();
        receiverUserName =getIntent().getExtras().get("user_name").toString();
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_name);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userProfileImage = findViewById(R.id.group_profile_image);
        userProfileName = findViewById(R.id.group_user_name);
        userProfileAboutMe = findViewById(R.id.group_user_aboutMe);
        deleteMemberButton = findViewById(R.id.delete_member_button);
        sendMessageButton = findViewById(R.id.send_message_button);
        sendRemainderButton = findViewById(R.id.push_notification_button);
        userLastSeen = findViewById(R.id.lastSeen);
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mToolbar = findViewById(R.id.group_member_add_bar);
        notificationRef =FirebaseDatabase.getInstance().getReference().child("Notifications");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account");
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    groupManagerID = dataSnapshot.child("Manager").getValue().toString();
                    if(currentUserID.equals(groupManagerID) && ! receiverUserID.equals(groupManagerID)){
                        deleteMemberButton.setVisibility(View.VISIBLE);
                        sendRemainderButton.setVisibility(View.VISIBLE);
                        sendMessageButton.setVisibility(View.VISIBLE);
                    }else{
                        deleteMemberButton.setVisibility(View.INVISIBLE);
                        sendRemainderButton.setVisibility(View.INVISIBLE);
                        sendMessageButton.setVisibility(View.INVISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void retrieveUserInfo() {
        usersRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() ){
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userAboutMe = dataSnapshot.child("aboutMe").getValue().toString();
                    String time = dataSnapshot.child("lastSeen").child("time").getValue().toString();
                    String date = dataSnapshot.child("lastSeen").child("date").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileAboutMe.setText(userAboutMe);
                    userLastSeen.setText("Last Seen : "+time+" "+date);
                }
                if (dataSnapshot.hasChild("image")){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
