package com.rocketfit.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.TaskStackBuilder;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rocketfit.fragments.TabOneFragment;
import com.rocketfit.fragments.TabThreeFragment;
import com.rocketfit.fragments.TabTwoFragment;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import projects.rocketfit.R;

public class ProfileActivity extends FragmentActivity {
    // Attempt to pull facebook shit
    private ProfilePictureView profilePictureView;
    private TextView memberSinceView;
    private TextView userNameView;
    private FragmentTabHost mTabHost;
    private String mName;
    private String pathToImage;
    private Uri profileImgUri;

    // messing around with image
    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;
    private ImageView profileImage;
    private static final int GALLERY = 1;
    public static final int KITKAT_VALUE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage    = (ImageView) findViewById(R.id.profile_image);
        userNameView    = (TextView)  findViewById(R.id.userNameView);
        memberSinceView = (TextView)  findViewById(R.id.memberSinceView);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImage.setImageBitmap(null);
                if (Image != null) {
                    Image.recycle();
                    Image = null;
                }
                Intent intent;

                if (Build.VERSION.SDK_INT < 19){
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, KITKAT_VALUE);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, KITKAT_VALUE);
                }

//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY);
            }
         });

        // Get current parse user
        ParseUser currentUser = ParseUser.getCurrentUser();
        try { currentUser.fetch(); } catch (ParseException e) { Log.d("MyApp", e.toString()); }

      //  Toast.makeText(ProfileActivity.this, currentUser.getString("firstname").toString(), Toast.LENGTH_SHORT).show();

        // Set the date format
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date date = currentUser.getCreatedAt();
        String sDate = df.format(date);

       // Toast.makeText(ProfileActivity.this, sDate, Toast.LENGTH_SHORT).show();
        //Toast.makeText(ProfileActivity.this, currentUser.get("fullname").toString(), Toast.LENGTH_LONG).show();

        // Set member since ..... view
        memberSinceView.setText("Member since " + sDate);

        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            userNameView.setText(currentUser.get("fullname").toString());
        }

        if (currentUser.getString("fullname") == null) {
        //    Toast.makeText(ProfileActivity.this, "hey sexy daaddyy", Toast.LENGTH_SHORT).show();

            userNameView.setText("(Click edit icon to add name)");
            userNameView.setTextColor(Color.GRAY);
        } else {
         //   Toast.makeText(ProfileActivity.this, "hey sexy mama", Toast.LENGTH_SHORT).show();

            userNameView.setText(currentUser.get("fullname").toString());
        }

        // ProfilePictureView profilePicture = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator("Recent", null),
                TabOneFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator("Friends", null),
                TabTwoFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator("Awards", null),
                TabThreeFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator("Stats", null),
                TabThreeFragment.class, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getMenuInflater().inflate(R.menu.menu_profilefb, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_profile, menu);
        }

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
       // searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        if (id == R.id.action_search) {

            return true;
        }

        if (id == R.id.action_edit) {

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Edit Name")
                    .setMessage("Enter name below")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable value = input.getText();
                            mName = value.toString();

                            if (mName.matches("")) {
//                                Toast toast = Toast.makeText(ProfileActivity.this, "Please enter your name", Toast.LENGTH_SHORT);
//                                toast.setGravity(Gravity.TOP, 25, 25);
//                                toast.show();
                            } else {
                                userNameView.setText(mName);
                                userNameView.setTextColor(Color.BLACK);
                                ParseUser.getCurrentUser().put("fullname", mName);

                                if (mName.contains(" ")) {
                                    String[] names = mName.split(" ");
                                    ParseUser.getCurrentUser().put("firstname", names[0]);
                                    ParseUser.getCurrentUser().put("lastname", names[1]);
                                } else {
                                    ParseUser.getCurrentUser().put("firstname", mName);
                                }
                                ParseUser.getCurrentUser().saveEventually();
                            }
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();

            return true;
        }

        if (id == R.id.action_logout) {
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

            finish();
            //return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == KITKAT_VALUE && resultCode == Activity.RESULT_OK) {
            Uri mImageUri = data.getData();
            profileImgUri = mImageUri;
            String pImg = profileImgUri.toString();
            ParseUser.getCurrentUser().put("profileImage", pImg);
            ParseUser.getCurrentUser().saveEventually();

            try {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                AssetFileDescriptor fileDescriptor = null;
                fileDescriptor = this.getContentResolver().openAssetFileDescriptor(mImageUri, "r");

                Image = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);

               // Image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                if (getOrientation(getApplicationContext(), mImageUri) != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getOrientation(getApplicationContext(), mImageUri));
                    if (rotateImage != null)
                        rotateImage.recycle();
                    rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix,true);
                    Image = rotateImage;
                    profileImage.setImageBitmap(Image);
                } else
                    profileImage.setImageBitmap(Image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    protected void onResume() {
        super.onResume();
        String pImg;

        Toast.makeText(ProfileActivity.this, ParseUser.getCurrentUser().get("profileImage").toString(), Toast.LENGTH_SHORT).show();

        if (ParseUser.getCurrentUser().get("profileImage") != null) {
            pImg = ParseUser.getCurrentUser().get("profileImage").toString();
            profileImgUri = Uri.parse(pImg);

//                try {
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 4;
//
//                    AssetFileDescriptor fileDescriptor = null;
//                    fileDescriptor = this.getContentResolver().openAssetFileDescriptor(profileImgUri, "r");
//
//                    Image = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
//
//                    //Image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), profileImgUri);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }

            profileImage.setImageBitmap(Image);
        } else {
            profileImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        }
    }
}