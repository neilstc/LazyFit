package com.lazyfit.app.lazyfit;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    private View feedFragmentView;
    private ListView list_feed_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_feeds;
    private DatabaseReference currentUserRef,databaseReference;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Map<String, String> post_UID;

    public FeedFragment() {
        //  empty constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        feedFragmentView = inflater.inflate(R.layout.fragment_feed, container, false);
        initializeFields();
        retrieveAndDisplayFeed();
        list_feed_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String messageKey = adapterView.getItemAtPosition(position).toString();
                String messageValue = post_UID.get(messageKey);
                showPopup(view,messageValue);
            }
        });

        return feedFragmentView;
    }

    private void retrieveAndDisplayFeed() {
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                if(dataSnapshot.hasChild("Messages")) {
                    DataSnapshot data = dataSnapshot.child("Messages");
                    for (DataSnapshot message : data.getChildren()) {
                        String uid = message.getKey();
                        String value = message.getValue().toString();
                        post_UID.put(value, uid);
                        set.add(value);
                    }
                }
                list_of_feeds.clear();
                list_of_feeds.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        list_of_feeds = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        list_feed_view = feedFragmentView.findViewById(R.id.feed_list_view);
        arrayAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, list_of_feeds);
        list_feed_view.setAdapter(arrayAdapter);
        post_UID =new HashMap<>();
    }

    public void showPopup(View anchorView, final String messageValue) {
        View popupView = getLayoutInflater().inflate(R.layout.item_post, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        final TextView popup_Title =  popupView.findViewById(R.id.title_of_popup);
        final TextView popup_Author = popupView.findViewById(R.id.author_of_popup);
        final TextView popup_Body = popupView.findViewById(R.id.body_of_popup);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("posts")){
                    DataSnapshot data =dataSnapshot.child("posts").child(messageValue);
                    String title,author,body;
                    title = data.child("title").getValue().toString();
                    author = data.child("author").getValue().toString();
                    body = data.child("body").getValue().toString();
                    popup_Title.setText("Group: "+title);
                    popup_Author.setText("Sender: "+author);
                    popup_Body.setText(body);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        int location[] = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }
}
