package com.cfd.rahul.fotonn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public class FotoNN extends AppCompatActivity {

    private static final int SELECT_IMAGE = 100;
    //JSON Node Names
    private static final String CAPTION = "caption";
    private static final String ID = "image_id";
    private static String url = "http://11204fbc.ngrok.io/vis.json";
    private ImageView imageView;
    private Uri imageUri;
    private int imageLength;
    private Button uploadImageButton;
    private ImageManager imageManager;
    private TextView imageText;
    private  ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_nn);

        Button selectImageButton = (Button) findViewById(R.id.Select);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImageFromGallery();
            }
        });

        this.uploadImageButton = (Button) findViewById(R.id.Upload);
        this.uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });
        this.uploadImageButton.setEnabled(false);

        this.imageView = (ImageView) findViewById(R.id.imageView);
        this.imageText = (TextView) findViewById(R.id.imageText);
        imageManager = new ImageManager(this);
        progress = new ProgressDialog(this);
    }

    private void ListImages() {
        Intent intent = new Intent(getBaseContext(), ListImagesActivity.class);
        startActivity(intent);
    }

    private void SelectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    private void UploadImage() {
        try {
            progress.setCancelable(false);
            progress.setMessage("Uploading Image");
            progress.show();
            final InputStream imageStream = getContentResolver().openInputStream(this.imageUri);
            final int imageLength = imageStream.available();

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {
                        final String imageName = ImageManager.UploadImage(imageStream, imageLength);
                        handler.post(new Runnable() {
                            public void run() {
                                progress.setMessage("Processing Image. Please wait for 7 seconds.");
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        new GetContacts().execute();
                                    }
                                }, 7000);
                            }
                        });
                    } catch (Exception ex) {
                        final String exceptionMessage = ex.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                progress.dismiss();
                                Toast.makeText(getApplicationContext(), exceptionMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            th.start();
        } catch (Exception ex) {
            progress.dismiss();
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    this.imageUri = imageReturnedIntent.getData();
                    this.imageView.setImageURI(this.imageUri);
                    this.uploadImageButton.setEnabled(true);
                }
        }
    }

    public MainActivity getMainContext() {
        return (MainActivity) this.getApplicationContext();
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    JSONObject c = jsonArray.getJSONObject(0);
                    String id = c.getString(ID);
                    final String caption = c.getString(CAPTION);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            imageText.setText(caption);
                        }
                    });


                } catch (final JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }
}