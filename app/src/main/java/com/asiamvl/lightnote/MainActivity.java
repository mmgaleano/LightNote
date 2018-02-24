package com.asiamvl.lightnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;


/** The main Activity of the APP.
 * @author Michael Mora
 * @version 1.0
 * @since 1.0
 */

public class MainActivity extends AppCompatActivity {

    public static final String DEBUG_TAG = "MIKE_DEBUG";
    public static final String TEXTFILE = "ligh_note.txt";
    public static final String FILESAVED = "filesaved";
    public static final int TAKE_PICTURE = 1;
    public static final int BROWSE_GALLERY = 2;
    private Button bSave;
    private Button bLock;
    private EditText etInput;
    private File tookPicFile;
    Toolbar toolbar;


    /** Overriding the onCreate method
     * @param savedInstanceState Bundle instance
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //3 lines solution for API > 23, otherwise it will crash when creating files
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        initGUI();

        addListeners();

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean fileSaved = prefs.getBoolean(FILESAVED, false);

        if(fileSaved) {
            loadFile();
        }
    }


    /** Method for initializing the GUI elements.
     */

    private void initGUI(){
        bSave = (Button)findViewById(R.id.bSave);
        bLock = (Button)findViewById(R.id.bLock);
        etInput = (EditText)findViewById(R.id.etInput);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    /** Method for setting the listeners to the GUI elements.
     */

    private void addListeners(){
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveFile();
            }
        });

        bLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                setResult(ImageActivity.REQUEST_CODE_ONE_LOCK, intent);
                finish();
            }
        });
    }


    /** Overriding the onCreateoptionsMenu method for bringing up the
     * Toolbar.
     * @param menu Menu to inflate
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /** Overriding the onOptionsItemSelected for setting the menu options
     * Toolbar.
     * @param item Menu to inflate
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id){
            case R.id.reset_passpoints:

                Intent intent = new Intent();
                setResult(ImageActivity.REQUEST_CODE_ONE_RESET, intent);
                finish();

                return true;

            case R.id.change_image:

                showDialog();

                return true;

            case R.id.set_defaults:

                SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("DEF_IMG", null);
                editor.putBoolean("CHANGED_IMG", true);
                editor.apply();

                Intent setDefault = new Intent(MainActivity.this, ImageActivity.class);
                setDefault.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(setDefault);
                finish();

                return true;

            case R.id.close_menu:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /** Method for loading a File displaying into the EditText
     */

    private void loadFile(){
        try {
            FileInputStream fis = openFileInput(TEXTFILE);
            DataInputStream dis = new DataInputStream(fis);
            InputStreamReader isr = new InputStreamReader(dis);
            BufferedReader br = new BufferedReader(isr);

            String line;

            while((line = br.readLine()) != null){
                etInput.append(line);
                etInput.append("\n");
            }
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Method for saving the EditText content into a File.
     */

    public void saveFile(){

        String text = etInput.getText().toString();

        try {
            FileOutputStream fos = openFileOutput(TEXTFILE, Context.MODE_PRIVATE);
            fos.write(text.getBytes());

            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FILESAVED, true);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, R.string.cannot_save_excep, Toast.LENGTH_SHORT).show();
        }
        Log.d(DEBUG_TAG, "Click on button");
        Toast.makeText(MainActivity.this, R.string.save_toast, Toast.LENGTH_SHORT).show();

    }


    /** Method for taking a picture.
     */

    public void takePicture() {

        File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MikeCamera" + File.separator);
        boolean res = root.mkdirs();
        String fname = "img_"+ System.currentTimeMillis() + ".jpg";
        tookPicFile = new File(root, fname);


        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tookPicFile));
        startActivityForResult(i, TAKE_PICTURE);

    }


    /** Method for browsing the gallery.
     */

    public void browseGallery() {

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, BROWSE_GALLERY);

    }


    /** Overriding the onActivityResult method.
     * @param requestCode integer request code number.
     * @param resultCode integer describing the result code.
     * @param intent returning Intent
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if(requestCode == TAKE_PICTURE && resultCode == RESULT_OK){

            Intent intentChecker = new Intent(this, ImageChecker.class);
            intentChecker.putExtra("CALL_TYPE", 1);
            intentChecker.putExtra("URI_STRING", Uri.fromFile(tookPicFile).toString());
            startActivity(intentChecker);
            finish();

        }
        else if(requestCode == BROWSE_GALLERY && resultCode == RESULT_OK){
            Intent intentChecker = new Intent(this, ImageChecker.class);
            intentChecker.putExtra("CALL_TYPE", 2);
            intentChecker.putExtra("URI_STRING", intent.getData().toString());
            startActivity(intentChecker);
            finish();

        }

    }


    /** Method for showing a dialog and letting the user choose between camera or gallery.
     */

    public void showDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("--- Please select the prefer method ---");  //Optional
        builder.setMessage("Please select your preferred method");

        final AlertDialog confirmDialog = builder.create();

        //Action for Button Neutral = take picture
        confirmDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Take picture",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if(!hasCamera()){
                            Toast.makeText(MainActivity.this, "This device has no camera available", Toast.LENGTH_SHORT).show();
                        }

                        else{
                            takePicture();
                            confirmDialog.dismiss();
                        }
                    }
                });

        //Action for Button Positive = browse gallery
        confirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Browse Gallery",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        browseGallery();
                        confirmDialog.dismiss();

                    }
                });

        //Setting button's colors
        confirmDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                confirmDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLUE);
                confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            }
        });

        confirmDialog.show();

    }


    /** Overriding the onPause.
     */

    @Override
    protected void onPause() {
        super.onPause();

        saveFile();

    }


    /** Method for checking if device has any camera.
     */

    public boolean hasCamera(){

        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

    }
}
