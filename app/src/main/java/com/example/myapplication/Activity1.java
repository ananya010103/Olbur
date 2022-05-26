package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
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
    public Uri uploadedUri;
    public String storageLink;

    public void setUploadedUri(Uri uri){
        this.uploadedUri = uri;
    }

    public Uri getUploadedUri(){
        return this.uploadedUri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        previewView = findViewById(R.id.previewView);
        bCapture = findViewById(R.id.button_verify);
        tConfidence = findViewById(R.id.confidence);
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
                            //Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(file);
                            //mediaScanIntent.setData(contentUri);
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();

                            //sign-in anonymously
                            Task<AuthResult> task = mAuth.signInAnonymously();
                            task.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("checking", "signInAnonymously:success");

                                    //uploadImageToFirebase(file.getName(),contentUri);

                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference imagesRef = storageRef.child("images");
                                    //StorageReference userRef = imagesRef.child(mAuth.getUid());
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                    String filename = mAuth.getUid() + "_" + timeStamp + ".jpg";
                                    Log.d("checking", "Filename" + filename + "is being uploaded to " + contentUri.getPath());
                                    StorageReference fileRef = imagesRef.child(filename);
                                    UploadTask uploadTask = fileRef.putFile(contentUri);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                            Toast.makeText(Activity1.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            uploadedUri = taskSnapshot.getUploadSessionUri();


                                            Log.d("checking", "encoded url is: " + uploadedUri.getEncodedPath());
                                            Log.d("checking", "Path from metadata is " + taskSnapshot.getMetadata().getPath());
                                            //Log.d("checking", "Uploaded URL is: " + uploadedUri.getPath().toString());
                                            Toast.makeText(Activity1.this, "Photo has been uploaded successfully to Firebase", Toast.LENGTH_SHORT).show();
                                            storageLink = "https://";
                                            storageLink += uploadedUri.getEncodedPath().substring(uploadedUri.getEncodedPath().indexOf('o'), uploadedUri.getEncodedPath().indexOf('m')+1);
                                            storageLink += ".storage.googleapis.com/";
                                            storageLink += taskSnapshot.getMetadata().getPath();
                                            Log.d("checking", "Final link of photo that needs verification is: " + storageLink);
                                            verify(storageLink);
                                        }
                                    });

                                    task.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("checking", "signInAnonymously:failure", task.getException());
                                            Toast.makeText(Activity1.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            //updateUI(null);
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onError(ImageCaptureException exception) {

                        }
                    });
    }

    private void verify(String fileURL) {
        //now verify if they are the same person using face API
        FaceDetect face1 = new FaceDetect();

        if (fileURL != null) {
            Log.d("checking", "verifying the photo taken" + fileURL);
            String returnVal = face1.verify(fileURL);
            if(returnVal.equals("true")) {
                tConfidence.setText("Identical");
            }
            else if(returnVal.equals("false")) {
                tConfidence.setText("Not Identical");
            }
            else {
                tConfidence.setText("Face not detected, try again");
            }
        }
    }

    private void uploadImageToFirebase(String name, Uri contentUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference image = storageRef.child("pictures/" + name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
                    }
                });

                Toast.makeText(Activity1.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Activity1.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}