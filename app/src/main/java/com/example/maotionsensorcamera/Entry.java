package com.example.maotionsensorcamera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Entry extends AppCompatActivity {
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        editText=(EditText)findViewById(R.id.editext_address);
        editText.setText("http://servlet-servlet.1d35.starter-us-east-1.openshiftapps.com/");
    }
    public void submit(View v){
        SharedPreferences preferences=getSharedPreferences("address", Context.MODE_PRIVATE);
        preferences.edit().putString("address",editText.getText().toString()).apply();
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}
