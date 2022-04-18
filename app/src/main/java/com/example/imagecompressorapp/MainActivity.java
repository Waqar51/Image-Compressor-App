package com.example.imagecompressorapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    public static final int RESULT_IMAGE = 1;

    ImageView imageOriginal, imageCompressed;
    TextView textOriginal, textCompressed, textQuality;
    EditText TextHeight, TextWidth;
    SeekBar seekBar;
    Button ButtonPick, ButtonCompress;
    File originalImage, CompressedImage;
    private static String filepath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myCompressor");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();

        imageOriginal = findViewById(R.id.imageOriginal);
        imageCompressed = findViewById(R.id.imageCompressed);
        textOriginal = findViewById(R.id.textOriginal);
        textCompressed = findViewById(R.id.textCompressed);
        textQuality = findViewById(R.id.textQuality);
        TextHeight = findViewById(R.id.TextHeight);
        TextWidth = findViewById(R.id.TextWidth);
        seekBar = findViewById(R.id.seekQuality);
        ButtonPick = findViewById(R.id.ButtonPick);
        ButtonCompress = findViewById(R.id.ButtonCompress);

        filepath = path.getAbsolutePath();

        if (!path.exists()) {
            path.mkdirs();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textQuality.setText("Quality:  " + progress);
                seekBar.setMax(100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ButtonPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGallery();
            }
        });

        ButtonCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int quality = seekBar.getProgress();
                int width = Integer.parseInt(TextWidth.getText().toString());
                int height = Integer.parseInt(TextHeight.getText().toString());

                try {
                    CompressedImage = new Compressor(MainActivity.this)
                            .setMaxWidth(width)
                            .setMaxHeight(height)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(filepath)
                            .compressToFile(originalImage);

                    File finalFile = new File(filepath, originalImage.getName());
                    Bitmap fileBitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                    imageCompressed.setImageBitmap(fileBitmap);
                    textCompressed.setText("Size: "+ Formatter.formatShortFileSize(MainActivity.this, finalFile.length()));
                    Toast.makeText(MainActivity.this, "Image Compressed & Saved! ", Toast.LENGTH_SHORT).show();

                    

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error while Compressing! ", Toast.LENGTH_SHORT).show();
                }

            }
        });



}

    private void openGallery() {

        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RESULT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            ButtonCompress.setVisibility(View.VISIBLE);
            final Uri imageUri = data.getData();
            try {
                final InputStream imageStream =  getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageOriginal.setImageBitmap(selectedImage);
                originalImage = new File(imageUri.getPath().replace("raw/", ""));
                textOriginal.setText("Size: " + Formatter.formatShortFileSize(this, originalImage.length()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            }
        }

        else {

            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void askPermission() {

        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                        permissionToken.continuePermissionRequest();

                    }
                }).check();

                
    }
    }