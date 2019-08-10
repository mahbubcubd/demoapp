package ml.kohinur.demoapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import in.mayanknagwanshi.imagepicker.imageCompression.ImageCompressionListener;
import in.mayanknagwanshi.imagepicker.imagePicker.ImagePicker;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    public ImagePicker imagePicker;
    public ImageView imageView;
    public TextView textView;
    Button buttonSubmit, buttonImage;
    public String encodedImage;
    public Bitmap image_bits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.monitor);
        imageView = (ImageView) findViewById(R.id.im);
        buttonSubmit = (Button) findViewById(R.id.submit);

        //hooking onclick listener of button | When You submit The picture
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String URL = "https://kohinurjosna.pythonanywhere.com/ml/default/index";

                final ProgressDialog loading = ProgressDialog.show(MainActivity.this,"Uploading...","Please wait...",false,false);
            // Android Volley Post Starts Here
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                        new Response.Listener<String>() {
                     // If Data posted successfully, This block will work
                            @Override
                            public void onResponse(String response) {
                                // Do something with the response
                                Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_LONG).show();
                                loading.dismiss();
                                Log.v(TAG,"Response: "+response);
                            }
                        },
                        new Response.ErrorListener() {
                    // If any error occurred e.g: internet connection, empty data, post permissions etc, this block will throw error
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Handle error
                                loading.dismiss();

                                Toast.makeText(MainActivity.this,"An unexpected error occurred",Toast.LENGTH_LONG).show();
                                String body;
                                //get response body and parse with appropriate encoding, This will return if server faces error
                                if(error.networkResponse.data!=null) {
                                    try {
                                        body = new String(error.networkResponse.data,"UTF-8");
                                        Log.i("Error Body",body);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }


                            }
                        }){
                    //adding parameters to the request | This block contains data as keypair e.g: {"imgData" : imagedata} to post to the server
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();

                        params.put("imgData",encodedImage);
                        return params;
                    }


                };
        //This block keep the http request in queue to send one after another. mySingleton has been created at mySingleton.java
                mySingleton.getInstance(MainActivity.this).addTorequestrue(stringRequest);








            }
        });}



// This is a function which is called when PICK IMAGE button is clicked. In activity_main.xml, android:onClick="uploader"
// has been hooked in a <Button> view
    public void uploader(View view){
        // This is Image Picker Plugin Stuff, It can pick image from any source on android
        imagePicker = new ImagePicker();
        imagePicker.withActivity(this) //calling from activity
                .chooseFromGallery(true) //default is true
                .chooseFromCamera(true) //default is true
                .withCompression(true) //default is true
                .start();
    }



    // This block take image from source then shows it on imageView and convert the image size and then fills the encodedImage
    // variable with string image so that the volley can post it to the server
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {

        try {

            switch (requestCode) {
                case (ImagePicker.SELECT_IMAGE): {

                    if (resultCode == Activity.RESULT_OK) {
                        //Add compression listener if withCompression is set to true
                        imagePicker.addOnCompressListener(new ImageCompressionListener() {
                            @Override
                            public void onStart() {

                            }



                            @Override
                            public void onCompressed(String filePath) {//filePath of the compressed image
                                //convert to bitmap easily
                                // Do nothing, but Image Picker need this parameter

                            }
                        });
                    }

                    // This Block Will Compress, resize and return the encoded image to post
                    Uri selectedImageUri = data.getData(); //Get image data
                    final String filePath = getPathFromURI(selectedImageUri);
                    if (filePath != null) {//filePath will return null if compression is set to true
                        Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                        int bitWidth = selectedImage.getWidth();
                        int outWidth = 200;
                        float rate = bitWidth / outWidth;
                        int bitHeight = selectedImage.getHeight() / (int) rate;
                        Bitmap resized = Bitmap.createScaledBitmap(selectedImage, outWidth, bitHeight, true);
                        image_bits = resized;
                        imageView.setImageBitmap(resized);
                        getStringImage(resized); // Call the function getStringImage(Bitmap bmp) to return encoded image

                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Somethig went embarassing",Toast.LENGTH_LONG).show();
        }
    }





// This function is called while compressing image
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.i("encodedImage",encodedImage);
        return encodedImage;
    }



// This function is called while compressing image
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}





