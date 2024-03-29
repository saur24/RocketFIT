package com.rocketfit.activities;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.rocketfit.adapters.TweetListAdapter;
import com.rocketfit.fragments.NavigationDrawerFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import projects.rocketfit.R;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class HomeActivity extends ListActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private ProgressBar mLoading;
    private TextView eMessage;
    private boolean twitWorks = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseFacebookUtils.initialize("844747758892958");
        ParseTwitterUtils.initialize("m2pJ8sVhK9Op5IpDEMFqAbrzp", "kzf5u8iMkBe6zvXdCPnAuz799rh9c07MYQsODwqGxsgtAOhwKC");

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
        }

        ParseTwitterUtils.getTwitter().getScreenName();
        if (ParseTwitterUtils.getTwitter().getScreenName() != null) {
            ParseUser.getCurrentUser().put("username", ParseTwitterUtils.getTwitter().getScreenName());
            ParseUser.getCurrentUser().saveEventually();
        }

        setContentView(R.layout.activity_home);

        mLoading=(ProgressBar)findViewById(R.id.progressbar_loading);
        eMessage=(TextView)findViewById(R.id.errorMessage);

        new GetTweets().execute(null, null, null);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void makeMeRequest() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            // Create a JSON object to hold the profile info
                            JSONObject userProfile = new JSONObject();
                            try {
                                // Populate the JSON object
                                userProfile.put("facebookId", user.getId());
                                userProfile.put("name", user.getName());
                                if (user.getLocation().getProperty("name") != null) {
                                    userProfile.put("location", (String) user
                                            .getLocation().getProperty("name"));
                                }
                                if (user.getProperty("gender") != null) {
                                    userProfile.put("gender",
                                            (String) user.getProperty("gender"));
                                }
                                if (user.getProperty("email") != null) {
                                    userProfile
                                            .put("email",
                                                    (String) user
                                                            .getProperty("email"));
                                }
                                // Now add the data to the UI elements
                                // ...
//                                AlertDialog.Builder builder1 = new AlertDialog.Builder(HomeActivity.this);
//                                builder1.setMessage(user.getBirthday());
//                                builder1.setCancelable(true);
//                                builder1.setPositiveButton("OK",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int id) {
//                                                dialog.cancel();
//                                            }
//                                        });
//
//                                AlertDialog alert1 = builder1.create();
//                                alert1.show();

                                ParseUser.getCurrentUser().put("email", user.getProperty("email"));
                                // sets username to FB email address
                                ParseUser.getCurrentUser().put("username", user.getProperty("email"));
                                ParseUser.getCurrentUser().put("fullname", user.getName());

                                String fullName = user.getName();
                                String[] names = fullName.split(" ");

                                ParseUser.getCurrentUser().put("firstname", names[0]);
                                ParseUser.getCurrentUser().put("lastname", names[1]);
                                ParseUser.getCurrentUser().saveEventually();

                            } catch (JSONException e) {
                                Log.d("My App",
                                        "Error parsing returned user data.");
                            }

                        } else if (response.getError() != null) {
                            // handle error
                        }
                    }
                });
        request.executeAsync();
    }

    private void updateViewsWithProfileInfo() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser.get("profile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {
                if (userProfile.getString("facebookId") != null) {
                    String facebookId = userProfile.get("facebookId")
                            .toString();
                    //userProfilePictureView.setProfileId(facebookId);
                } else {
                    // Show the default, blank user profile picture
                    //userProfilePictureView.setProfileId(null);
                }
                // Set additional UI elements
                // ...
            } catch (JSONException e) {
                // handle error
            }

        }
    }

    public void onWeightsSelected(View view) {
        //exercise button was pressed
        //Toast.makeText(HomeActivity.this, "weights", Toast.LENGTH_SHORT).show();
        Intent weights = new Intent(getApplicationContext(), WeightsActivity.class);
        startActivity(weights);
    }

    public void onRunSelected(View view) {
        //run button was pressed
        //Toast.makeText(HomeActivity.this, "run", Toast.LENGTH_SHORT).show();
        Intent run = new Intent(getApplicationContext(), RunActivity.class);
        startActivity(run);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {

            case 0:
                //RocketFIT
                break;

            case 1:
                //Profile
                Intent profile = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profile);
                break;

            case 2:
                //Run/Walk
                Intent run = new Intent(getApplicationContext(), RunActivity.class);
                startActivity(run);
                break;
            case 3:
                //Weights
                Intent weights = new Intent(getApplicationContext(), WeightsActivity.class);
                startActivity(weights);
                break;
        }
    }

    // NOT USED
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_activity_home);
                break;
            case 2:
                mTitle = getString(R.string.title_section1);
                break;
            case 3:
                mTitle = getString(R.string.title_section2);
                break;
            case 4:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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

    private class GetTweets extends AsyncTask<String, String, List<twitter4j.Status>> {
        @Override
        protected List<twitter4j.Status> doInBackground(String... strings) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("m2pJ8sVhK9Op5IpDEMFqAbrzp")
                    .setOAuthConsumerSecret("kzf5u8iMkBe6zvXdCPnAuz799rh9c07MYQsODwqGxsgtAOhwKC")
                    .setOAuthAccessToken("308752376-bkNABJVf6PCA4PjJB4DIxSTinrqDVpMME7JDacT9")
                    .setOAuthAccessTokenSecret("qoObmUGY6zYqP6sXMT9ynvnohYYDcAYJcaALG61kTvRZs");
            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter4j.Twitter twitter = tf.getInstance();
            List<twitter4j.Status> tweets = new ArrayList();

            try {
                // gets Twitter instance with default credentials
                //User user = twitter.verifyCredentials();
                tweets = twitter.getUserTimeline("UToledoSRC");

            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                twitWorks = false;
            }

            return tweets;
        }

        protected void onPostExecute(List<twitter4j.Status> result) {
            if (!twitWorks) {
                eMessage.setVisibility(View.VISIBLE);
                eMessage.setText("Failed to get News Feed!\nPlease check your Internet connection.");
            }
            mLoading.setVisibility(View.GONE);
            setListAdapter(new TweetListAdapter(HomeActivity.this, result));
        }

    }
}
