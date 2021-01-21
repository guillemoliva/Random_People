package com.olivaguillem.RandomPeople.Adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.olivaguillem.RandomPeople.FullScreenImageActivity;
import com.olivaguillem.RandomPeople.Model.Chat;
import com.olivaguillem.RandomPeople.R;
import com.squareup.picasso.Picasso;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageURL;
    private int languageCode, userLanguageCode;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageURL, int languageCode, int userLanguageCode){
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageURL = imageURL;
        this.languageCode = languageCode;
        this.userLanguageCode = userLanguageCode;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (imageURL.equals("default")){
            holder.profile_image.setImageResource(R.drawable.default_user);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profile_image);
        }


        if(mChat.get(position).getMessage() !=null) {
            holder.show_image.setVisibility(View.GONE);
            if (mChat.get(position).getSender().equals(fuser.getUid())) {
                holder.show_message.setText(mChat.get(position).getMessage());

            } else {
                translateMessage(mChat.get(position).getMessage(), holder, languageCode, userLanguageCode);
            }
        }else {
            holder.show_image.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(Uri.parse(mChat.get(position).getImage())).into(holder.show_image);
        }

        holder.show_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Text", holder.show_message.getText());
                clipboard.setPrimaryClip(clip);
            }
        });

        holder.show_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullScreenImageActivity.class);
                intent.putExtra("image", mChat.get(position).getImage());
                mContext.startActivity(intent);
            }
        });


    }


    public void onTranslationCompleted(String translation, MessageAdapter.ViewHolder holder){

        holder.show_message.setText(translation);

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public ImageView profile_image;
        public ImageView show_image;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.showMessage);
            profile_image = itemView.findViewById(R.id.chatProfileImage);
            show_image = itemView.findViewById(R.id.showImage);

        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public void translateMessage(String messageReceived, final MessageAdapter.ViewHolder holder, int languageCode, int userLanguageCode) {

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
                                onTranslationCompleted(translatedText, holder);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

    }

}