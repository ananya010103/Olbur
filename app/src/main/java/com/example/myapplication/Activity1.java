package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class Activity1 extends AppCompatActivity implements View.OnClickListener{

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;
    private ImageCapture imageCapture;
    private Button bCapture;
    private TextView tConfidence;
    private ImageView iConfidence;
    public Uri uploadedUri;
    public String storageLink;
    private static final int CAMERA_REQUEST = 100;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

        previewView = findViewById(R.id.previewView);
        bCapture = findViewById(R.id.button_verify);
        tConfidence = findViewById(R.id.confidence);
        iConfidence = (ImageView)findViewById(R.id.image_verify);

        bCapture.setOnClickListener(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {

        //check the status of the earlier action on this button and clear labels and images, if any
        if (bCapture.getText().toString().equals("TRY AGAIN")){
            iConfidence.setImageResource(0);
            iConfidence.setBackgroundColor(0);
            tConfidence.setText(null);
        }
        else if(tConfidence.getText().equals("Face not detected"))
        {
            iConfidence.setImageResource(0);
            iConfidence.setBackgroundColor(0);
        }

        //Capture the photo and upload to Firebase for further use
        capturePhoto();
    }


    private void capturePhoto() {
            File file = new File( getExternalCacheDir().getPath() + System.currentTimeMillis() + ".png");
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(file).build();

            imageCapture.takePicture(outputFileOptions, getExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            Log.d("info", Environment.getExternalStorageDirectory().getPath());
                            Toast.makeText(Activity1.this, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();

                            //now upload the file to Firebase
                            Uri contentUri = Uri.fromFile(file);
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();

                            //sign-in anonymously to Firebase
                            Task<AuthResult> task = mAuth.signInAnonymously();
                            task.addOnSuccessListener(authResult -> {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("checking", "signInAnonymously:success");

                                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                StorageReference imagesRef = storageRef.child("images");
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String filename = mAuth.getUid() + "_" + timeStamp + ".jpg";
                                Log.d("checking", "Filename" + filename + "is being uploaded to " + contentUri.getPath());
                                StorageReference fileRef = imagesRef.child(filename);
                                UploadTask uploadTask = fileRef.putFile(contentUri);
                                uploadTask.addOnFailureListener(exception -> {
                                    // Handle unsuccessful uploads
                                    Toast.makeText(Activity1.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                }).addOnSuccessListener(taskSnapshot -> {
                                    uploadedUri = taskSnapshot.getUploadSessionUri();

                                    Log.d("checking", "encoded url is: " + uploadedUri.getEncodedPath());
                                    Log.d("checking", "Path from metadata is " + taskSnapshot.getMetadata().getPath());
                                    Toast.makeText(Activity1.this, "Photo has been uploaded successfully to Firebase", Toast.LENGTH_SHORT).show();

                                    //No single method seemed to be available for accessing the file URL directly,
                                    // hence building the same using various individuals components
                                    storageLink = "https://";
                                    storageLink += uploadedUri.getEncodedPath().substring(uploadedUri.getEncodedPath().indexOf('o'), uploadedUri.getEncodedPath().indexOf('m')+1);
                                    storageLink += ".storage.googleapis.com/";
                                    storageLink += taskSnapshot.getMetadata().getPath();
                                    Log.d("checking", "Final link of photo that needs verification is: " + storageLink);

                                    //Azure Face API verify using the uploaded URI
                                    // against existing faces added from persons
                                    verify(storageLink);
                                });

                                task.addOnFailureListener(e -> {
                                    Log.w("checking", "signInAnonymously:failure", task.getException());
                                    Toast.makeText(Activity1.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                });
                            });
                        }

                        @Override
                        public void onError(ImageCaptureException exception) {
                            Log.e("checking", "Image could not be captured");
                        }
                    });
    }

    @SuppressLint("SetTextI18n")
    private void verify(String fileURL) {
        //now verify if they are the same person using face API
        FaceDetect face1 = new FaceDetect();
        int c1 = getResources().getColor(R.color.green);
        int c2 = getResources().getColor(R.color.red);


        if (fileURL != null) {
            Log.d("checking", "verifying the photo taken" + fileURL);
            String returnVal = face1.verify(fileURL);
            Log.d("checking", "Return value from verify is: " + returnVal);

            //set the labels in the UI correctly and appropriately
            if(returnVal.equals("true")) {
                tConfidence.setText("Verified");
                iConfidence.setImageResource(R.drawable.tick);
                iConfidence.setBackgroundColor(c1);
            }
            else if(returnVal.equals("false")) {
                tConfidence.setText(R.string.textview_verify);
                bCapture.setText(R.string.button_verify);

                iConfidence.setImageResource(R.drawable.cross);
                iConfidence.setBackgroundColor(c2);
            }
            else {
                tConfidence.setText(returnVal);
                bCapture.setText(R.string.button_verify1);
            }
        }
    }

    //This is to ensure that the application is to allow the user to take a picture
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "CameraX permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "CameraX permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}