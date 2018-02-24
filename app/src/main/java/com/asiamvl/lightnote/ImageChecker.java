package com.asiamvl.lightnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import java.io.File;
import java.io.IOException;

/** Represents an image checker for setting the default image from
 *  File or Camera.
 * @author Michael Mora
 * @version 1.0
 * @since 1.0
 */

/** ImageChecker constructor
 */

public class ImageChecker extends AppCompatActivity {

    private ImageView imageView;
    String defaultIMG;
    public static final int TAKE_PICTURE_CODE = 1;
    public static final int BROWSE_GALLERY_CODE = 2;
    private File tookPicFile;
    private int callType;


    /** Overrides the onCreate Method.
     * @param savedInstanceState A reference from the Bundle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_checker);

        imageView = (ImageView)findViewById(R.id.ivCheck);

        Bundle extras = getIntent().getExtras();

        if(extras != null){

            String imagePath= extras.getString("URI_STRING");
            callType = extras.getInt("CALL_TYPE");
            Uri fileUri = Uri.parse(imagePath);

            mainFunction(fileUri);
        }
    }

    /** Main Activity method for setting the seleteced image into the ImageView
     * @param fileUri A Uri of the image to be placed into the ImageView
     */

    public void mainFunction(Uri fileUri){

        defaultIMG = fileUri.toString();

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            imageView.setImageBitmap(bitmap);

            showDialog();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /** Method that displays a dialog for confirming the image or to redo the
     * previous action: Take Picture of Browse Gallery
     */

    public void showDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("--- Confirm picture ---");
        builder.setMessage("Keep this picture as default ?");

        final AlertDialog confirmDialog = builder.create();

        confirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", //Action for OK
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("DEF_IMG", defaultIMG);
                        editor.putBoolean("CHANGED_IMG", true);
                        editor.apply();

                        confirmDialog.dismiss();

                        Intent intent = new Intent(ImageChecker.this, ImageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    }
                });

        confirmDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", //Action for cancel
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(ImageChecker.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });


        confirmDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "SELECT ANOTHER", //Action for another
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if(callType == 1) //Checks if the action to redo is gallery or camera
                            takePicture();
                        else
                            browseGallery();

                    }
                });


        confirmDialog.setOnShowListener(new DialogInterface.OnShowListener() { //Set the dialog button colors
            @Override
            public void onShow(DialogInterface dialog) {
                confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                confirmDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLUE);
                confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
            }
        });


        AsyncTask<Void,Void,Void> sleeper= new AsyncTask<Void, Void, Void>(){ //Async task for closing dialog

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                confirmDialog.show();

            }
        };

        sleeper.execute();

    }

    /** Method for taking pictures.
     */

    public void takePicture() {

        File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MikeCamera" + File.separator);
        root.mkdirs();
        String fname = "img_"+ System.currentTimeMillis() + ".jpg";
        tookPicFile = new File(root, fname);


        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tookPicFile));
        startActivityForResult(i, TAKE_PICTURE_CODE);

    }


    /** Method for browsing the gallery.
     */

    public void browseGallery() {

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, BROWSE_GALLERY_CODE);

    }


    /** Overriding the onActivityResult method.
     * @param requestCode integer request code number.
     * @param resultCode integer describing the result code.
     * @param intent returning Intent
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if(requestCode == TAKE_PICTURE_CODE && resultCode == RESULT_OK){

            mainFunction(Uri.fromFile(tookPicFile));

        }
        else if(requestCode == BROWSE_GALLERY_CODE && resultCode == RESULT_OK){

            mainFunction(intent.getData());

        }

    }

}
