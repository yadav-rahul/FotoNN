package com.cfd.rahul.fotonn;

import android.Manifest;
import android.app.*;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetectFace extends AppCompatActivity {

    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    // Configure the connection-string with your values
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;" +
                    "AccountName=fotonn;" +
                    "AccountKey=WAz24m3agTsSDLwZ3/bWSpUikiDu8n2w0eiYPVQQpp/2aR9wwaDieNAH+AYEdFHljka0a4N/Fm3Ggl6liFk1fA==";
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 3;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private final int PICK_IMAGE = 1;
    Uri imageUri = null;
    private ProgressDialog detectionProgressDialog;
    private FaceServiceClient faceServiceClient =
            new FaceServiceRestClient("8c480e9e3f0d475aaf4fcd3e33d88c4c");
    private TextView textView;
    private String mCurrentPhotoPath;
    private Uri mCapturedImageURI;
    private TextToSpeech t1;
    private File photoFile;
    private Uri photoURI;
    private Button cameraButton;

    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Log.d("Detect Frame", "Started");
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    private void selectImage() {
        textView.setText("Please upload image to find number of faces!");
        Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
        gallIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(gallIntent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_face);
        Button button1 = (Button) findViewById(R.id.button1);
        cameraButton = (Button) findViewById(R.id.upload_emotion_image);
        textView = (TextView) findViewById(R.id.number_face_detected);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraIntent();
            }
        });
        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        detectionProgressDialog = new ProgressDialog(this);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

        Bundle bundle = getIntent().getExtras();
        boolean param = bundle.getBoolean("flag");
        boolean paramCamera = bundle.getBoolean("camera");

        if (param)
            if (paramCamera)
                openCameraIntent();
            else
                selectImage();

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
                photoURI = FileProvider.getUriForFile(DetectFace.this,
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

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(DetectFace.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(DetectFace.this,
                                new String[]{Manifest.permission.CAMERA},
                                REQUEST_TAKE_PHOTO);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(DetectFace.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(DetectFace.this);
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(DetectFace.this, ALLOW_KEY, true);
                        }
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void openCamera() {


        String fileName = "Camera_Example.jpg";

        // Create parameters for Intent with filename

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

        // imageUri is the current activity attribute, define and save it for later usage

        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        /**** EXTERNAL_CONTENT_URI : style URI for the "primary" external storage volume. ****/


        // Standard Intent action that can be sent to have the camera
        // application capture an image and return it.

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

        /*************************** Camera Intent End ************************/

    }

    private File createPhotoFile() throws IOException {
        // Create an image file name using timestamp
        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save image path to send to PhotoSendActivity
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                imageView.setImageBitmap(bitmap);
                detectAndFrame(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView imageView = (ImageView) findViewById(R.id.imageView1);
            imageView.setImageURI(photoURI);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                detectAndFrame(bitmap);
            } catch (IOException e) {
                t1.speak("Fatal error occurred! Please try again..", TextToSpeech.QUEUE_FLUSH, null);
                e.printStackTrace();

            }

        }
    }

    private void detectAndFrame(final Bitmap imageBitmap) {
        Log.d("Detect Frame", "Started");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            t1.speak("Detecting faces. Please wait for some time.", TextToSpeech.QUEUE_FLUSH, null);
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null           // returnFaceAttributes: a string like "age, gender"
                            );
                            if (result == null) {

                                publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(
                                    //Log.d("Detect Frame","Success");
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                            return result;
                        } catch (Exception e) {
                            Log.d("Detect Frame", "Exception");
                            publishProgress("Detection failed");
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }

                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }

                    @Override
                    protected void onPostExecute(final Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result == null) {
                                    t1.speak("Detecting finished. No face found!", TextToSpeech.QUEUE_FLUSH, null);
                                    textView.setText("Number of face detected : " + 0);
                                } else {
                                    t1.speak("Detecting finished. " + result.length + " face found!", TextToSpeech.QUEUE_FLUSH, null);
                                    textView.setText("Number of face detected : " + result.length);
                                }
                            }
                        });
                        if (result == null) return;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                        imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();

                    }
                };
        detectTask.execute(inputStream);
    }

    @Override
    protected void onDestroy() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //open list of images classified into differnt categories
            Intent i = new Intent(this, ListActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
