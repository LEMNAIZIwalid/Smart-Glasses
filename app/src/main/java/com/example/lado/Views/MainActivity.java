package com.example.lado.Views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import com.example.lado.R;

public class MainActivity extends AppCompatActivity {

    private ImageView imageBonjour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageBonjour = findViewById(R.id.imageBonjour);

        // Lorsqu'on clique sur l'image → ouvre LoginActivity
        imageBonjour.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
