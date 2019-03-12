package com.atheeshproperty.textrecognier;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button openCamera, detect;
    private TextView resultsDisplay;
    Bitmap finalBit;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openCamera = findViewById(R.id.OpenCamera);
        detect = findViewById(R.id.detect);
        resultsDisplay = findViewById(R.id.resultDisplay);

        img = findViewById(R.id.capturedImage);

        openCameraToTakeThePicture();
        detection();


    }

    private void openCameraToTakeThePicture() {

        openCamera.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                boolean cam = (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
                boolean galleryView = (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
                boolean galleryRead = (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);

                if (!cam) {
                    Log.d("Permission status", "No permissions.Permission requested.");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(intent, 0);


                } else {

                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    Log.d("Permission status", "No permissions.Permission requested.");

                    return;
                }


            }
        });


    }

    private void detection() {

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalBit == null) {
                    Log.e("Bitmap", "Is null");

                } else {

                    FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(finalBit);
                    FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer();
                    firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    process(firebaseVisionText);
                                }
                            });

                }

            }
        });
    }

    public void process(FirebaseVisionText firebaseVisionText) {

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();

        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No text detected", Toast.LENGTH_LONG).show();

        } else {
            for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                String text = block.getText();
                resultsDisplay.setText(text);

            }

        }


    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postRotate(90);
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 0) {

            switch (resultCode) {

                case Activity.RESULT_OK:

                    byte[] imageAsBytes = null;

                    Log.e("Image received", "Yes");

                    Uri selectedImage = data.getData();//get image uri
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Log.e("Image uri", " this is the uri: " + selectedImage);
                    assert selectedImage != null;
                    @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //file path of captured image
                    String filePath = cursor.getString(columnIndex);
                    //file path of captured image

                    cursor.close();

                    Bitmap capturedImage = BitmapFactory.decodeFile(filePath);
                    finalBit = getResizedBitmap(capturedImage, 2800, 1000);

                    img.setImageBitmap(finalBit);


                    // Bitmap b = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                    // img.setImageBitmap(Bitmap.createScaledBitmap(b, 120, 120, false));
                    // b.recycle();

                  /* int rotate = 0;

                    Uri selectedImage = data.getData();//get image uri
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Log.e("Image uri"," this is the uri: "+selectedImage);
                    assert selectedImage != null;
                    @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //file path of captured image
                    String filePath = cursor.getString(columnIndex);


                    try {
                        getContentResolver().notifyChange(selectedImage, null);
                        File imageFile = new File(filePath);
                        ExifInterface exif = new ExifInterface(
                                imageFile.getAbsolutePath());
                        int orientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL);

                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotate = 270;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotate = 180;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotate = 90;
                                break;
                        }
                        Log.e("Orientation", "Exif orientation: " + orientation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /****** Image rotation ****/
                    /*Matrix matrix = new Matrix();
                    matrix.postRotate(-90);
                    Bitmap cropped = Bitmap.createBitmap(scaled, 0, 0, 1000, 1000, matrix, true); */


                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "Nothing saved ", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

    }
}
