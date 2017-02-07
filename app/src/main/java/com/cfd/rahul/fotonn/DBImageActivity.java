package com.cfd.rahul.fotonn;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class DBImageActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbimage);
        imageView = (ImageView) findViewById(R.id.imageView121);


        Bundle bundle = getIntent().getExtras();
        String param = bundle.getString("uri");

        if (param != null) {
            imageView.setImageURI(Uri.parse(param));
        }
    }
}
