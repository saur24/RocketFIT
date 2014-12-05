package com.rocketfit.activities;

import android.annotation.TargetApi;
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
import android.os.AsyncTask;
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
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rocketfit.fragments.TabFourFragment;
import com.rocketfit.fragments.TabOneFragment;
import com.rocketfit.fragments.TabThreeFragment;
import com.rocketfit.fragments.TabTwoFragment;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;




import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import projects.rocketfit.R;

public class ProfileActivity extends FragmentActivity {
    static final String TAG = "FileUtils";
    private static final boolean DEBUG = false; // Set to true to enable logging
    public static final int KITKAT_VALUE = 1002;
    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;
    private TextView memberSinceView;
    private TextView userNameView;
    private FragmentTabHost mTabHost;
    private String mName;
    private Uri profileImgUri;
    private ImageView profileImage;

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

        // Set the date format
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date date = currentUser.getCreatedAt();
        String sDate = df.format(date);

        // Set member since ..... view
        memberSinceView.setText("Member since " + sDate);

        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            userNameView.setText(currentUser.get("fullname").toString());
        }

        if (currentUser.getString("fullname") == null) {
            userNameView.setText("(Click edit icon to add name)");
            userNameView.setTextColor(Color.GRAY);
        } else {
            userNameView.setText(currentUser.get("fullname").toString());
        }

        // attempt to send current user to tab Fragment
        Bundle b = new Bundle();
        b.putString("user", currentUser.getUsername());
        //

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator("Recent", null),
                TabOneFragment.class, b);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator("Friends", null),
                TabTwoFragment.class, b);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator("Awards", null),
                TabThreeFragment.class, b);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator("Stats", null),
                TabFourFragment.class, b);


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
                if (!isGooglePhotosUri(profileImgUri)) {
                    if (getOrientation(getApplicationContext(), mImageUri) != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(getOrientation(getApplicationContext(), mImageUri));
                        if (rotateImage != null)
                            rotateImage.recycle();
                        rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix, true);
                        Image = rotateImage;
                        profileImage.setImageBitmap(Image);
                    } else
                        profileImage.setImageBitmap(Image);
            } else {
                    profileImage.setImageBitmap(Image);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImageToParse(Bitmap Image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Image.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] data = stream.toByteArray();

        final ParseFile file = new ParseFile(data);
        try{
            file.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ParseUser.getCurrentUser().put("fileImage", file);
        try{
            ParseUser.getCurrentUser().save();
        } catch (ParseException e1){
            e1.printStackTrace();
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

        new BitmapWorkerTask(profileImage).execute();
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(19)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth,
                                                     int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            String pImg, realPath = "";

            if (ParseUser.getCurrentUser().get("profileImage") != null) {
                pImg = ParseUser.getCurrentUser().get("profileImage").toString();
                profileImgUri = Uri.parse(pImg);

                try {
                    if (Build.VERSION.SDK_INT < 19){
                        realPath = getPath(getApplicationContext(), profileImgUri);
                        Image = decodeSampledBitmapFromPath(realPath, 60, 60);
                    } else {
                        realPath = getPath(getApplicationContext(), profileImgUri);
                        Image = decodeSampledBitmapFromPath(realPath, 60, 60);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Image = null;
            }
            return Image;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
                new SaveImageTask(bitmap).execute();
            } else {
                profileImage.setImageDrawable(getResources().getDrawable(R.drawable.default_profile));
            }
        }
    }

    private class SaveImageTask extends AsyncTask<Integer, Void, Bitmap> {
        // Decode image in background.
        Bitmap toParse;

        public SaveImageTask(Bitmap bitmap) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            toParse = bitmap;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {

            saveImageToParse(toParse);

            return toParse;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {

        }
    }
}