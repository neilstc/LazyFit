package com.lazyfit.app.lazyfit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference groupRef, notificationRef;
    private ArrayList<String> groupMembers;
    private ProgressDialog progressDialog;
    private Map<String, String> member_Name_UID;

    private ArrayAdapter<String> arrayAdapter;
    private ListView list_of_users;

    private String groupName, currentUserId, managerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initializeFields();
        retrieveAndDisplayUsers();

        list_of_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String visit_user_name = adapterView.getItemAtPosition(position).toString();
                String visit_user_id =member_Name_UID.get(visit_user_name);
                Intent profileIntent =new Intent(GroupActivity.this,ProfileGroupUserActivity.class);
                profileIntent.putExtra("visit_user_id",visit_user_id);
                profileIntent.putExtra("group_name",groupName);
                profileIntent.putExtra("user_name",visit_user_name);
                startActivity(profileIntent);
                finish();
            }
        });


    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        groupName = getIntent().getExtras().getString("groupName");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        notificationRef =FirebaseDatabase.getInstance().getReference().child("Notifications");
        mToolbar = findViewById(R.id.group_bar_layout);
        progressDialog = new ProgressDialog(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        member_Name_UID = new HashMap<>();
        groupMembers = new ArrayList<>();
        list_of_users = findViewById(R.id.list_group_user_view);
        arrayAdapter = new ArrayAdapter<>(GroupActivity.this, android.R.layout.simple_list_item_1, groupMembers);
        list_of_users.setAdapter(arrayAdapter);

        groupRef.child(groupName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    managerID = dataSnapshot.child("Manager").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void retrieveAndDisplayUsers() {
        groupRef.child(groupName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                DataSnapshot mem = dataSnapshot.child("Members");
                for (DataSnapshot info : mem.getChildren()) {
                    String name = info.getValue().toString();
                    String uid = info.getKey();
                    set.add(name);
                    member_Name_UID.put(name, uid);
                }

                groupMembers.clear();
                groupMembers.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.group_options_menu, menu);
        invalidateOptionsMenu();
        MenuItem addMember = menu.findItem(R.id.group_addMember_option);
        MenuItem broadcastMessage = menu.findItem(R.id.group_broadcastMessage_option);
        MenuItem deleteGroup = menu.findItem(R.id.group_deleteGroup_option);
        MenuItem leaveGroup = menu.findItem(R.id.group_leave_option);
        if (TextUtils.equals(managerID, currentUserId)) {
            addMember.setVisible(true);
            broadcastMessage.setVisible(true);
            deleteGroup.setVisible(true);
            leaveGroup.setVisible(false);
        } else {
            addMember.setVisible(false);
            broadcastMessage.setVisible(false);
            deleteGroup.setVisible(false);
            leaveGroup.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_addMember_option:
                addMembers();
                break;
            case R.id.group_broadcastMessage_option:
                sendBroadcastMessage();
                break;
            case R.id.group_deleteGroup_option:
                deleteGroup();
                break;
            case R.id.group_leave_option:
                leaveGroup();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    private void deleteGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
        builder.setTitle("Deletion Confirmation");
        builder.setMessage("Are You Sure You Want To Delete " + groupName + "?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(GroupActivity.this, "The Group :" + groupName + " Was Deleted", Toast.LENGTH_SHORT).show();
                Intent groupIntent = new Intent(GroupActivity.this, MainActivity.class);
                startActivity(groupIntent);
                groupRef.child(groupName).removeValue();
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

    private void addMembers() {
        Intent addMember = new Intent(GroupActivity.this, FindUsersActivity.class);
        addMember.putExtra("group_name", groupName);
        startActivity(addMember);
        finish();
    }

    private void leaveGroup() {
        progressDialog.setTitle("Leaving "+groupName+" Group");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        groupRef.child(groupName).child("Members").child(currentUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        startActivity(new Intent(GroupActivity.this,GroupsFragment.class));
                        finish();
                    }
                });
    }

    private void sendBroadcastMessage(){
        for(String member: groupMembers){
            final String userId =member_Name_UID.get(member);
            if(!userId.equals(currentUserId)) {
                HashMap<String, String> massegeNotificationMap = new HashMap<>();
                massegeNotificationMap.put("from", currentUserId);
                massegeNotificationMap.put("type", "BroadCast");
                notificationRef.child(userId).push()
                        .setValue(massegeNotificationMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //TODO check correctness
                                    notificationRef.child(userId).removeValue();
                                }
                            }
                        });
            }
        }

    }
}