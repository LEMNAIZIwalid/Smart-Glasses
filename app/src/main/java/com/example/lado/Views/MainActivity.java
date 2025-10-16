package com.example.lado.Views;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.lado.R;
import com.example.lado.Controller.MainController;

public class MainActivity extends AppCompatActivity {

    private TextView textBonjour;
    private MainController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textBonjour = findViewById(R.id.textBonjour);
        controller = new MainController(this);
        controller.afficherMessage(textBonjour);
    }
}
