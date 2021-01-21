package com.olivaguillem.RandomPeople;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.olivaguillem.RandomPeople.Adapter.MessageAdapter;
import com.olivaguillem.RandomPeople.Dialogs.Checkpoint1;
import com.olivaguillem.RandomPeople.Dialogs.Checkpoint2;
import com.olivaguillem.RandomPeople.Dialogs.Checkpoint2Accept;
import com.olivaguillem.RandomPeople.Dialogs.WriteCorrectly;
import com.olivaguillem.RandomPeople.Model.Chat;
import com.olivaguillem.RandomPeople.Model.Chatlist;
import com.olivaguillem.RandomPeople.Model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;



public class MessageActivity extends AppCompatActivity {

    CircleImageView profileImage;
    public static TextView username;
    ImageButton buttonSend;
    public static ImageButton buttonImage;
    EditText textSend;
    public static String userid;
    String myUrl;
    public static FirebaseUser fuser;
    DatabaseReference reference, referenceCurrentUser;
    ValueEventListener seenListener;
    int languageCode;
    Uri fileUri;
    StorageTask uploadTask;
    boolean checkpoint1=false;
    boolean checkpoint2Fuser=false;
    boolean checkpoint2User=false;
    ProgressDialog loadingBar;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        buttonSend = findViewById(R.id.buttonSendMessage);
        buttonImage = findViewById(R.id.buttonImage);
        textSend = findViewById(R.id.editTextSendMessage);
        loadingBar = new ProgressDialog(this);
        Toolbar toolbar = findViewById(R.id.messageToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        recyclerView = findViewById(R.id.recyclerViewMessage);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.messageUsername);


        final Intent intent = getIntent();
        userid = intent.getStringExtra("userid");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = textSend.getText().toString().trim();
                if (!msg.isEmpty()){
                    sendMessage(fuser.getUid(), userid, msg);
                }
                textSend.setText("");
            }
        });

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 2);

            }
        });




        textSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(textSend.getText().toString().equals("")){
                    if(checkpoint1) {
                        buttonImage.setVisibility(View.VISIBLE);
                    }
                }else{
                    buttonImage.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist").child(userid).child(fuser.getUid());
        chatRefReceiver.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                HashMap map = new HashMap();

                try {
                    if(chatlist.isCheckpoint2()) {
                        checkpoint2User = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(checkpoint2Fuser && checkpoint2User){
                    username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MessageActivity.this, Profile2Activity.class);
                            intent.putExtra("id", userid);
                            startActivity(intent);
                        }
                    });
                    try {
                        if(!chatlist.isCheckpoint2AcceptMsg()){
                            DialogFragment checkpoint2A = new Checkpoint2Accept();
                            checkpoint2A.setCancelable(false);
                            checkpoint2A.show(getSupportFragmentManager(), "Checkpoint 2");
                            map.put("checkpoint2AcceptMsg", true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                chatRefReceiver.updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid()).child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                HashMap map = new HashMap();
                try {
                    chatlist.isWritecorrectly();
                } catch (Exception e) {
                    DialogFragment writeCorrectly = new WriteCorrectly();
                    writeCorrectly.setCancelable(false);
                    writeCorrectly.show(getSupportFragmentManager(), "writeCorrectly");
                }
                try {
                    checkpoint1 = chatlist.isCheckpoint1();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(checkpoint1){
                    buttonImage.setVisibility(View.VISIBLE);
                }

                try {
                    if(chatlist.isCheckpoint2()){
                        checkpoint2Fuser=true;
                    }
                } catch (Exception e) {}
                try {
                    if (chatlist.getCheckpoint() > 10) {
                        map.put("checkpoint1", true);
                        if(!chatlist.isCheckpoint1Msg()) {
                            DialogFragment checkpoint1 = new Checkpoint1();
                            checkpoint1.setCancelable(false);
                            checkpoint1.show(getSupportFragmentManager(), "Checkpoint 1");
                            map.put("checkpoint1Msg", true);
                        }
                    }
                    if (chatlist.getCheckpoint() > 20) {
                        if(!chatlist.isCheckpoint2Msg()) {
                            DialogFragment checkpoint2 = new Checkpoint2();
                            checkpoint2.setCancelable(false);
                            checkpoint2.show(getSupportFragmentManager(), "Checkpoint 2");
                            map.put("checkpoint2Msg", true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                chatRef.updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        referenceCurrentUser = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        referenceCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                languageCode = user.getLanguageCode();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.drawable.default_user);
                }else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }

                readMesagges(fuser.getUid(), userid, user.getImageURL(), languageCode, user.getLanguageCode());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);

    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode==RESULT_OK && data!=null && data.getData()!= null) {

            fileUri = data.getData();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image");
            DatabaseReference userMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(fuser.getUid()).child(userid).push();
            final String messagePushID = userMessageKeyRef.getKey();
            final StorageReference filePath = storageReference.child(messagePushID + "." + "image");

            uploadTask = filePath.putFile(fileUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadURL = task.getResult();
                        myUrl = downloadURL.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", fuser.getUid());
                        hashMap.put("receiver", userid);
                        hashMap.put("image", myUrl);
                        hashMap.put("name", fileUri.getLastPathSegment());
                        hashMap.put("isseen", false);

                        reference.child("Chats").push().setValue(hashMap);

                        Date date = new Date();
                        final long time = date.getTime();

                        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid()).child(userid);

                        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);

                                int checkpoint = 1;
                                try {
                                    checkpoint = chatlist.getCheckpoint()+1;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (!dataSnapshot.exists()) {
                                    chatRef.child("id").setValue(userid);
                                    HashMap map = new HashMap();
                                    map.put("time", time);
                                    map.put("checkpoint", 1);
                                    chatRef.updateChildren(map);
                                } else {
                                    HashMap map = new HashMap();
                                    map.put("time", time);
                                    map.put("checkpoint", checkpoint);
                                    chatRef.updateChildren(map);
                                }
                                DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist").child(userid).child(fuser.getUid());
                                chatRefReceiver.child("id").setValue(fuser.getUid());
                                chatRefReceiver.child("time").setValue(time);
                                try {
                                    chatRefReceiver.child("checkpoint").setValue(checkpoint);
                                } catch (Exception e) {
                                    chatRefReceiver.child("checkpoint").setValue(1);
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

    void sendMessage(String sender, final String receiver, String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        Date date = new Date();
        final long time = date.getTime();

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid()).child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                int checkpoint = 1;
                try {
                    checkpoint = chatlist.getCheckpoint()+1;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                    HashMap map = new HashMap();
                    map.put("time", time);
                    map.put("checkpoint", 1);
                    map.put("checkpoint1", false);
                    map.put("checkpoint2", false);
                    map.put("writecorrectly", true);
                    try {
                        chatlist.isCheckpoint1Msg();
                    } catch (Exception e) {
                        map.put("checkpoint1Msg", false);
                    }
                    try {
                        chatlist.isCheckpoint2Msg();
                    } catch (Exception e) {
                        map.put("checkpoint2Msg", false);
                    }
                    try {
                        chatlist.isCheckpoint2AcceptMsg();
                    } catch (Exception e) {
                        map.put("checkpoint2AcceptMsg", false);
                    }
                    chatRef.updateChildren(map);
                } else {
                    HashMap map = new HashMap();
                    map.put("time", time);
                    map.put("checkpoint", checkpoint);
                    try {
                        if (chatlist.getCheckpoint() >= 4) {
                            checkpoint1 = true;
                            map.put("checkpoint1", true);
                            if(!chatlist.isCheckpoint1Msg()) {
                                DialogFragment checkpoint1 = new Checkpoint1();
                                checkpoint1.setCancelable(false);
                                checkpoint1.show(getSupportFragmentManager(), "Checkpoint 1");
                                map.put("checkpoint1Msg", true);
                            }
                        }
                        if (chatlist.getCheckpoint() >= 10) {
                            if(!chatlist.isCheckpoint2Msg()) {
                                DialogFragment checkpoint2 = new Checkpoint2();
                                checkpoint2.setCancelable(false);
                                checkpoint2.show(getSupportFragmentManager(), "Checkpoint 2");
                                map.put("checkpoint2Msg", true);
                            }
                        }
                    } catch (Exception e) {}
                    chatRef.updateChildren(map);
                }
                final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist").child(userid).child(fuser.getUid());
                chatRefReceiver.child("id").setValue(fuser.getUid());
                chatRefReceiver.child("time").setValue(time);
                try {
                    chatRefReceiver.child("checkpoint").setValue(checkpoint);
                } catch (Exception e) {
                    chatRefReceiver.child("checkpoint").setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }





    private void readMesagges(final String myid, final String userid, final String imageurl, final int languageCode, final int userLanguageCode){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl, languageCode, userLanguageCode);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
    }

    public void setLocale(String lang){

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();

    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

}
