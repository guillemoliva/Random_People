package com.olivaguillem.RandomPeople;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    ImageView fullScreen;
    String image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreen = findViewById(R.id.fullScreen);
        Intent intent = getIntent();
        image = intent.getStringExtra("image");
        Picasso.with(getApplicationContext()).load(Uri.parse(image)).into(fullScreen);
    }
}
