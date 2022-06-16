package top.sun1999;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

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

        readVecs();
    }

    public void readVecs() {
        FileInputStream input = null;
        BufferedReader bufferedReader = null;
        try {
            input = openFileInput("vecs.txt");
            bufferedReader = new BufferedReader(new InputStreamReader(input));
            String s = bufferedReader.readLine();
            while (s!=null) {
                if (s.indexOf('[') >= 0) {
                    s = s.substring(1, s.length() - 1);
                    String[] split = s.split(",");
                    double[] doubles = Arrays.stream(split).mapToDouble(Double::parseDouble).toArray();
                    Util.vecs.add(doubles);
                } else {
                    Util.names.add(s);
                }
                s = bufferedReader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}