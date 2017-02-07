package com.cfd.rahul.fotonn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FotoNN extends AppCompatActivity {

    private static final int SELECT_IMAGE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;


    //JSON Node Names
    private static final String CAPTION = "caption";
    private static final String ID = "image_id";
    private static String url = "http://ea7de590.ngrok.io/vis.json";
    String mCurrentPhotoPath;
    private ImageView imageView;
    private Uri imageUri;
    private int imageLength;
    private Button uploadImageButton;
    private ImageManager imageManager;
    private TextView imageText;
    private ProgressDialog progress;
    private TextToSpeech t1;
    private File photoFile;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_nn);
        this.imageText = (TextView) findViewById(R.id.imageText);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
        Button selectImageButton = (Button) findViewById(R.id.Select);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageText.setText("Please upload image to get results!");
                SelectImageFromGallery();
            }
        });

        this.uploadImageButton = (Button) findViewById(R.id.Upload);
        this.uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraIntent();
            }
        });

        this.imageView = (ImageView) findViewById(R.id.imageView);
        imageManager = new ImageManager(this);
        progress = new ProgressDialog(this);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Bundle bundle = getIntent().getExtras();
        boolean param = bundle.getBoolean("flag");
        boolean paramCamera = bundle.getBoolean("camera");

        if (param)
            if (paramCamera)
                openCameraIntent();
            else
                SelectImageFromGallery();
    }

    private void openCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(FotoNN.this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    private void SelectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    private void UploadImage() {
        t1.speak("Uploading Image", TextToSpeech.QUEUE_FLUSH, null);
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
                                t1.speak("Image uploaded successfully. Now please wait for 7 seconds.", TextToSpeech.QUEUE_FLUSH, null);
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
                        t1.speak("Server error. Please try again.", TextToSpeech.QUEUE_FLUSH, null);
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
            t1.speak("Server error. Please try again.", TextToSpeech.QUEUE_FLUSH, null);
            progress.dismiss();
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageFromCamera() {
        t1.speak("Uploading Image", TextToSpeech.QUEUE_FLUSH, null);
        try {
            progress.setCancelable(false);
            progress.setMessage("Uploading Image");
            progress.show();
            final InputStream imageStream = getContentResolver().openInputStream(photoURI);
            final int imageLength = imageStream.available();

            final Handler handler = new Handler();

            Thread th = new Thread(new Runnable() {
                public void run() {
                    try {
                        final String imageName = ImageManager.UploadImage(imageStream, imageLength);
                        handler.post(new Runnable() {
                            public void run() {
                                t1.speak("Image uploaded successfully. Now please wait for 7 seconds.", TextToSpeech.QUEUE_FLUSH, null);
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
                        t1.speak("Server error. Please try again.", TextToSpeech.QUEUE_FLUSH, null);
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
            t1.speak("Server error. Please try again.", TextToSpeech.QUEUE_FLUSH, null);
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
                    UploadImage();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    this.imageView.setImageURI(this.photoURI);
                    uploadImageFromCamera();
                }
        }
    }

    public MainActivity getMainContext() {
        return (MainActivity) this.getApplicationContext();
    }

    @Override
    protected void onDestroy() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
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

                            t1.speak("Content which best describes this picture is, "
                                    + caption, TextToSpeech.QUEUE_FLUSH, null);
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
                                "Server down! Please try after some time.",
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