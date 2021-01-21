package com.olivaguillem.RandomPeople.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.olivaguillem.RandomPeople.Model.Chat;
import com.olivaguillem.RandomPeople.Model.User;
import com.olivaguillem.RandomPeople.R;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat, myLastMsg, isSeenBoolean;
    private int languageCode, userLanguageCode, numMessages;


    private String theLastMessage, image;
    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat) {

        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.lastImg.setVisibility(View.GONE);
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        userLanguageCode = user.getLanguageCode();

        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.drawable.default_user);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat){
            lastMessage(user.getId(), holder.lastMsg, holder.lastImg);
        } else {
            holder.lastMsg.setVisibility(View.GONE);
        }
        isSeen(user.getId(),holder.isSeen, holder.numberMsg);

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        ImageView profile_image;
        private TextView lastMsg;
        private ImageView isSeen;
        private TextView numberMsg;
        private ImageView lastImg;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_username);
            profile_image=itemView.findViewById(R.id.user_profile_image);
            lastMsg = itemView.findViewById(R.id.user_lastMsg);
            isSeen = itemView.findViewById(R.id.unseenMsg);
            numberMsg = itemView.findViewById(R.id.numberMsg);
            lastImg = itemView.findViewById(R.id.lastImg);


        }
    }

    private void isSeen(final String userid, final ImageView isSeen, final TextView numberMsg){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numMessages = 0;
                isSeenBoolean = true;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
                        if(!chat.isIsseen()){
                            numMessages++;
                        }
                        isSeenBoolean = chat.isIsseen();
                    }
                }
                if (isSeenBoolean){
                    isSeen.setVisibility(View.INVISIBLE);
                }else{
                    isSeen.setVisibility(View.VISIBLE);
                    numberMsg.setText(Integer.toString(numMessages));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userid, final TextView lastMsg, final ImageView lastImg){

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        referenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                languageCode = user.getLanguageCode();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    image = null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
                            theLastMessage = chat.getMessage();
                            image = chat.getImage();
                            myLastMsg=false;
                    } else if(chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        theLastMessage = chat.getMessage();
                        image = chat.getImage();
                        myLastMsg=true;
                    }

                }
                if(theLastMessage!=null && !myLastMsg) {
                    translateMessage(theLastMessage, lastMsg, languageCode, userLanguageCode);

                }else if(theLastMessage!=null && myLastMsg){
                    lastMsg.setText(theLastMessage);

                }else if(image!=null){
                    lastMsg.setText(R.string.image);
                    lastImg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void translateMessage(String messageReceived, final TextView lastMsg, int languageCode, int userLanguageCode) {

        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(userLanguageCode)
                        .setTargetLanguage(languageCode)
                        .build();
        final FirebaseTranslator translator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

        translator.translate(messageReceived)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                onTranslationCompleted(translatedText, lastMsg);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

    }

    private void onTranslationCompleted(String translation, TextView lastMsg){

        lastMsg.setText(translation);

    }
}
