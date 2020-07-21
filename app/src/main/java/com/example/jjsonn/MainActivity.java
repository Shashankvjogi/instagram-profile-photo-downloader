package com.example.jjsonn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button get_response_btn;
    ImageView imageView;
TextView textViewInsta;
    TextView textViewDownload;
    TextView textViewTryAgain;
    EditText editText;
    private int STORAGE_PERMISSION_CODE = 1;

    BitmapDrawable drawable;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        get_response_btn = findViewById(R.id.get_data);
         imageView = findViewById(R.id.image);
         textViewDownload=findViewById(R.id.txtviewDownload);
        textViewTryAgain=findViewById(R.id.tryagain);
        textViewInsta=findViewById(R.id.txtviewInsta);

         editText=findViewById(R.id.edttxt);
         textViewInsta.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Uri webpage = Uri.parse("https://www.instagram.com/?hl=en");
                 Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                 startActivity(webIntent);



             }
         });

            get_response_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    sendGetRequest();
                } else {
                    requestStoragePermission();
                }

                sendGetRequest();
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            }


        });


    }


    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendGetRequest ();
                   } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
        private void sendGetRequest () {

            RequestQueue queue = Volley.newRequestQueue(this);
            final String url = "https://www.instagram.com/"+editText.getText()+"/?__a=1";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try{
                        JSONObject jresponse = new JSONObject(response);
                        String image_url = jresponse.getJSONObject("graphql").getJSONObject("user").getString("profile_pic_url_hd");
                        Glide.with(MainActivity.this)
                                .load(image_url)
                                .fitCenter()
                                .into(imageView);
                        textViewInsta.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        textViewTryAgain.setVisibility(View.INVISIBLE);
                        textViewDownload.setVisibility(View.VISIBLE);


                        textViewDownload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {



                              FileOutputStream fileOutputStream=null;

                                drawable=(BitmapDrawable)imageView.getDrawable();
                                bitmap=drawable.getBitmap();
                                File storage= Environment.getExternalStorageDirectory();
                                File directory=new File(storage.getAbsolutePath()+"/Instagram profile photo /");
                                directory.mkdir();

                                String filename=String.format("%d.jpg",System.currentTimeMillis());
                                File outfile=new File(directory,filename);
                              Toast.makeText(MainActivity.this,"Image saved successfully",Toast.LENGTH_SHORT).show();
                                try {
                                    fileOutputStream = new FileOutputStream(outfile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                                    fileOutputStream.flush();
                                    fileOutputStream.close();

                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    intent.setData(Uri.fromFile(outfile));
                                    sendBroadcast(intent);

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                public void onErrorResponse(VolleyError error) {
                    imageView.setVisibility(View.INVISIBLE);
textViewDownload.setVisibility(View.INVISIBLE);


                    textViewTryAgain.setVisibility(View.VISIBLE);


                }
            });
    queue.add(stringRequest);
    }



}
