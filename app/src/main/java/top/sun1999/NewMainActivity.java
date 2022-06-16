package top.sun1999;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NewMainActivity extends AppCompatActivity {

    Button beginVerifyButton;
    Button addPalmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        beginVerifyButton = findViewById(R.id.beginVerifying);
        addPalmButton = findViewById(R.id.addPalmButton);


        beginVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NewMainActivity.this, VerifyActivity.class);
                startActivity(i);
            }
        });

        addPalmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NewMainActivity.this, AddPalmActicity.class);
                startActivity(i);
            }
        });


    }



}