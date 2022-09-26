package com.example.uploadfirst;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
public class MainActivity extends AppCompatActivity {
    Button button_selectfile , button_upload;
    TextView TextView_notification;
    Uri uri;

    FirebaseStorage storage; // used for uploading files
    FirebaseDatabase database; // used to store URLs of uploaded files
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = FirebaseStorage.getInstance();// return an object of FireBase Storage
        database = FirebaseDatabase.getInstance();// return an object of FireBase DataBase

        button_selectfile = findViewById(R.id.button_selectfile);
        button_upload = findViewById(R.id.button_upload);
        TextView_notification = findViewById(R.id.TextView_notification);

        button_selectfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    select();
                }
                else
                    {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                    }
            }
        });

        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri!=null){
                    uploadFile(uri);
                }else {
                    Toast.makeText(MainActivity.this,"Select a File", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadFile(final Uri uri) {

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String fileName = System.currentTimeMillis()+"";
        StorageReference storageReference =storage.getReference(); // returns root path
        storageReference.child("Uploads").child(fileName).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url = taskSnapshot.getUploadSessionUri().toString(); // return the url of you uploaded file..
                        //Toast.makeText(MainActivity.this,fileName,Toast.LENGTH_LONG).show(); hena kont bagrb a4of el (url) wa (file Name) hygebhom wla l2 w EL 7amd llah gabhom
                        // store the url in realtime database
                        DatabaseReference reference= database.getReference(); // return the path to root


                        reference.child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                    // ( fileName ) This used value Name of File
                                    Toast.makeText(MainActivity.this,"File Successfuly uploaded",Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"File not Successfuly uploaded",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                // track the progress of = our upload..
                int currentProgress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==9 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {
            select();
        }else
            {
                Toast.makeText(MainActivity.this,"please provide permission..",Toast.LENGTH_LONG).show();
            }
    }

    private void select() {
        // to offer user to select a file using manager
        // we will be using Intent
        Intent intent= new Intent();
        // setType("*/ *") Select any type File (PDF/Word/PowrPoint/Viduo/Image ;) / .....)
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT); // to fetch files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==86 && resultCode==RESULT_OK && data!=null)
        {
            uri=data.getData();// return the url of selected file
            TextView_notification.setText("A file is Selected : " + data.getData().getLastPathSegment());
        }else {
            Toast.makeText(MainActivity.this,"Please select a file",Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}