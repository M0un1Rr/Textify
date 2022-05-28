
package com.mounirgaiby.textify.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mounirgaiby.textify.R;
import com.mounirgaiby.textify.databases.DatabaseHelper;
import com.mounirgaiby.textify.databinding.ActivityAccountBinding;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class AccountActivity extends AppCompatActivity  {
private  ActivityAccountBinding binding;
private PreferenceManager preferenceManager;
private DatabaseHelper db;
private FirebaseFirestore DB;
private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        fillInfo();
        setListener();
    }
    public void setListener(){
        binding.btnSignOut.setOnClickListener(v->{
            signOut();
        });
        binding.back.setOnClickListener(v->onBackPressed());
        binding.btnChangePass.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(),ChangePasswordActivity.class)));
        editListeners();

    }
    public void editListeners(){
        binding.imageProfile.setOnClickListener(v->editImage());
        binding.editbtnNom.setOnClickListener(v->editNom());
        binding.editbtnPropos.setOnClickListener(v->editPropos());
        binding.editbtnNomU.setOnClickListener(v->editNomU());
        binding.layoutEdit.setOnClickListener(v->{
            binding.layoutEdit.setVisibility(View.GONE);
        });
        binding.btnImageEdit.setOnClickListener(v->changeImage());
        binding.layoutImage.setOnClickListener(v-> binding.layoutImage.setVisibility(View.GONE));
        binding.btnValider.setOnClickListener(v->edit());
        binding.btnValiderImage.setOnClickListener(v->validerImageChange());
    }



    private void edit() {
        if(binding.txtEditType.getText().equals("A propos")){
            DB.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.User_UID))
                    .update(Constants.KEY_APROPOS,binding.txtEdit.getText().toString())
                    .addOnSuccessListener(unused -> {
                        showToast("Le mis a jour est reussie");
                        preferenceManager.putString(Constants.KEY_APROPOS,binding.txtEdit.getText().toString());
                        updateInfo("A propos");
                    })
                    .addOnFailureListener(unused -> showToast("Echec"));


        }
        else if(binding.txtEditType.getText().equals("Nom complet")){
            DB.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.User_UID))
                    .update(Constants.KEY_NOMCOMPLET,binding.txtEdit.getText().toString())
                    .addOnSuccessListener(unused -> {
                        showToast("Le mis a jour est reussie");
                        preferenceManager.putString(Constants.KEY_NOMCOMPLET,binding.txtEdit.getText().toString());
                        updateInfo("Nom complet");
                    })
                    .addOnFailureListener(unused -> showToast("Echec"));
        }
        else if(binding.txtEditType.getText().equals("Nom d'utilisateur")){
            DB.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.User_UID))
                    .update(Constants.KEY_NAME,binding.txtEdit.getText().toString())
                    .addOnSuccessListener(unused -> {
                        showToast("Le mis a jour est reussie");
                        preferenceManager.putString(Constants.KEY_NAME,binding.txtEdit.getText().toString());
                        updateInfo("Nom d'utilisateur");
                    })
                    .addOnFailureListener(unused -> showToast("Echec"));
        }

        binding.layoutEdit.setVisibility(View.GONE);

    }

    private void validerImageChange() {
        DB.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.User_UID))
                .update(Constants.KEY_IMAGE,encodedImage)
                .addOnSuccessListener(task ->{
                       Bitmap bitmap = decodeImage(encodedImage);
                       binding.imageProfile.setImageBitmap(bitmap);
                       preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                })
                .addOnFailureListener(task -> {
                    showToast("Echec");
                });
        binding.layoutImage.setVisibility(View.GONE);
    }

    private void editImage() {
        binding.layoutImage.setVisibility(View.VISIBLE);
        binding.imageEdit.setImageBitmap(decodeImage(preferenceManager.getString(Constants.KEY_IMAGE)));
    }
    private void changeImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImage.launch(intent);
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = BITMAP_RESIZER(bitmap,previewWidth,previewHeight);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }


    public Bitmap BITMAP_RESIZER(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }




    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream is = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            binding.imageEdit.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        }catch(FileNotFoundException ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
    );


    private void editPropos() {
        binding.txtEditType.setText("A propos");
        binding.layoutEdit.setVisibility(View.VISIBLE);
        binding.txtEdit.setSingleLine(false);
        binding.txtEdit.setText(preferenceManager.getString(Constants.KEY_APROPOS));

    }
    private void editNomU() {
        binding.txtEditType.setText("Nom d'utilisateur");
        binding.layoutEdit.setVisibility(View.VISIBLE);
        binding.txtEdit.setSingleLine(true);
        binding.txtEdit.setText(preferenceManager.getString(Constants.KEY_NAME));
    }
    private void editNom(){
        binding.txtEditType.setText("Nom complet");
        binding.layoutEdit.setVisibility(View.VISIBLE);
        binding.txtEdit.setSingleLine(true);
        binding.txtEdit.setText(preferenceManager.getString(Constants.KEY_NOMCOMPLET));
    }

    public void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        db = new DatabaseHelper(this);
        DB = FirebaseFirestore.getInstance();
    }
    private void fillInfo(){
        String name = preferenceManager.getString(Constants.KEY_NAME);
        String email = preferenceManager.getString(Constants.KEY_EMAIL);
        String fullName = preferenceManager.getString(Constants.KEY_NOMCOMPLET);
        String apropos = preferenceManager.getString(Constants.KEY_APROPOS);
        Bitmap bitmap = decodeImage(preferenceManager.getString(Constants.KEY_IMAGE));
        binding.txtPropos.setText(apropos);
        binding.txtNomU.setText(name);
        binding.txtNomComplet.setText(fullName);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.txtName.setText(name);
        binding.txtEmail.setText(email.toLowerCase());
    }
    private Bitmap decodeImage(String image){
        byte[] bytes = Base64.decode(image,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }
    private void updateInfo(String type){
        if(type.equals("A propos")){
            String apropos = preferenceManager.getString(Constants.KEY_APROPOS);
            binding.txtPropos.setText(apropos);}
        else if(type.equals("Nom complet")){
            String fullName = preferenceManager.getString(Constants.KEY_NOMCOMPLET);
            binding.txtNomComplet.setText(fullName);
        }
        else if(type.equals("Nom d'utilisateur")){
            String usern = preferenceManager.getString(Constants.KEY_NAME);
            binding.txtNomU.setText(usern);
        }
    }



    private void signOut(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference dr = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getUid());
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        dr.update(updates)
                .addOnSuccessListener(unused -> {
                    FirebaseAuth.getInstance().signOut();
                    showToast(getString(R.string.Disconnected));
                    Intent intent = new Intent(getApplicationContext(),SigninActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> showToast(getString(R.string.unable_disconnect)));





    }








    public void showToast(String m){
        Toast.makeText(getApplicationContext(),m,Toast.LENGTH_SHORT).show();
    }


}