package com.asiamvl.lightnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** The initial Activity of the APP.
 * @author Michael Mora
 * @version 1.0
 * @since 1.0
 */

public class ImageActivity extends AppCompatActivity implements PointCollectorListener {

    private ImageView ivTouchImage;
    public static final String DEBUG_TAG = "MIKE_DEBUG";
    private PointCollector pointCollector;
    private Database db = new Database(this);
    public static final String PASSWORD_SET = "PASSWORD_SET";
    public final static int POINTS_MAX_DISTANCE = 120;
    public int RESET_COUNTER = 0;
    public static final int REQUEST_CODE_ONE = 100;
    public static final int REQUEST_CODE_ONE_LOCK = 101;
    public static final int REQUEST_CODE_ONE_RESET = 102;
    public static final int CHANGE_PIC_CODE = 103;
    private String defIMG;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;


    /** Overrides the onCreate Method.
     * @param savedInstanceState A reference from the Bundle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        checkAndRequestPermissions();

        pointCollector = new PointCollector();
        initGUI();
        setListeners();

    }


    /** Method for checking the permissions of the APP of the fly
     * @return boolean true if permissions were already granted before execution,
     * false if permissions were not grante by method.
     */

    private  boolean checkAndRequestPermissions() {

        int camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int internet = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET);
        int writeExtStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExtStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.INTERNET);
        }
        if (writeExtStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readExtStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    /** Method initializing the elements of the GUI
     */

    private void initGUI(){
        ivTouchImage = (ImageView)findViewById(R.id.ivTouchImage);

        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        boolean pointsSet = prefs.getBoolean(PASSWORD_SET,false);
        defIMG = prefs.getString("DEF_IMG",null);
        Boolean changedImage = prefs.getBoolean("CHANGED_IMG", false);

        if(!pointsSet){
            showSetPassPointPrompt();  //Only the first time
        }


        if(defIMG == null){ //Enters when the image is not defined yet or got reset it
            Drawable image = ContextCompat.getDrawable(this, R.drawable.bird_2);
            ivTouchImage.setImageDrawable(image);

            if(changedImage) {  //Enters if image got reset it
                setPasspoints(false);
                showSetPassPointPrompt();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("CHANGED_IMG", false);
                editor.apply();
            }

        }
        else{ //Enters if image is defined already

            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(defIMG));
                ivTouchImage.setImageBitmap(bitmap);

            } catch (IOException e) { //Catch error when current image was deleted by user, and sets default states
                e.printStackTrace();

                Drawable image = ContextCompat.getDrawable(this, R.drawable.bird_2);
                ivTouchImage.setImageDrawable(image);

                setPasspoints(false);
                showSetPassPointPrompt();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("CHANGED_IMG", false);
                editor.apply();

            }

            if(changedImage){ //Enters if the image was changed

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("CHANGED_IMG", false);
                editor.apply();

                setPasspoints(false);
                showSetPassPointPrompt();
            }

        }
    }


    /** Method for setting the listeners to the GUI elements.
     */

    private void setListeners(){
        ivTouchImage.setOnTouchListener(pointCollector);
        pointCollector.setListener(this);
    }


    /** Method for showing the dialog with the welcome message.
     */

    private void showSetPassPointPrompt(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setTitle(R.string.prompt_title);
        builder.setMessage(getString(R.string.prompt_message) +
                " after setting them for getting access to your APP");

        final AlertDialog diag = builder.create();


        diag.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                diag.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            }
        });

        diag.show();
    }


    /** Method that implements what pointCollected from PointCollectorListener
     * is going to do. In this case. It will save the points or verifying them.
     * @param points List<Point> to check or save.
     */

    @Override
    public void pointCollected(final List<Point> points) {

        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        boolean pointsSet = prefs.getBoolean(PASSWORD_SET,false);

        if(!pointsSet ){
            savePasspoints(points);
        }
        else{
            verifyPassPoints(points);
        }

    }


    /** Method for saving points from a List<Point>
     * @param points List<Point> to save.
     */

    private void savePasspoints(final List<Point> points){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.saving_message);

        final AlertDialog diag = builder.create();

        diag.setCancelable(false);

        diag.show();

        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                db.storePoints(points);
                //Log.d(DEBUG_TAG, "Points Saved... ");
                setPasspoints(true);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                setPasspoints(true);

                diag.dismiss();
                pointCollector.clear();
            }
        };

        task.execute();
        Toast.makeText(this, R.string.conf_to_insert_pass, Toast.LENGTH_SHORT).show();
    }


    /** Method for saving points from a List<Point>
     * @param pointsp List<Point> to verify.
     */


    private void verifyPassPoints(final List<Point> pointsp){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.verify_message);

        final AlertDialog diag = builder.create();
        diag.show();

        AsyncTask<Void,Void,Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<Point> savedPoints = db.getPoints();

                if(savedPoints.size() != PointCollector.NUM_POINTS || pointsp.size() != PointCollector.NUM_POINTS){
                    return false;
                }

                for(int i = 0; i < PointCollector.NUM_POINTS; i++){
                    Point savedPoint = savedPoints.get(i);
                    Point touchPoint = pointsp.get(i);

                    int xDiff = savedPoint.x - touchPoint.x;
                    int yDiff = savedPoint.y - touchPoint.y;

                    int distSquare = xDiff*xDiff + yDiff*yDiff;

                    if(distSquare > (POINTS_MAX_DISTANCE * POINTS_MAX_DISTANCE)){
                        return false;
                    }

                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean pass) {
                diag.dismiss();
                pointCollector.clear();

                if(pass){
                    Intent intent  = new Intent(ImageActivity.this, MainActivity.class);
                    //startActivity(intent);
                    //finish();

                    startActivityForResult(intent,REQUEST_CODE_ONE);
                }
                else{
                    Toast.makeText(ImageActivity.this, "Access denied !!!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        task.execute();
    }


    /** Overriding the onActivityResult method.
     * @param requestCode integer request code number.
     * @param resultCode integer describing the result code.
     * @param data returning Intent
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == REQUEST_CODE_ONE) {
            if (resultCode == REQUEST_CODE_ONE_LOCK){

                Toast.makeText(ImageActivity.this, "APP locked succesfully!!!", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == REQUEST_CODE_ONE_RESET){

                setPasspoints(false);
                showSetPassPointPrompt();
            }
        }

    }


    /** Method for setting the flag that checks if the passpoints
     * has been set or not.
     * @param flag Boolean for the value to store.
     */

    public void setPasspoints(boolean flag){

        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PASSWORD_SET, flag);
        editor.apply();

    }



}
