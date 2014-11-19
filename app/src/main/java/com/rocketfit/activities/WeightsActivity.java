package com.rocketfit.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import projects.rocketfit.R;

public class WeightsActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    public int numberOfSets = 0;
    public List<EditText> allReps = new ArrayList<EditText>();
    public List<EditText> allWeights = new ArrayList<EditText>();

    private TextView mMachineName;
    private NfcAdapter mNfcAdapter;
    private Spinner mSelectMachine;
    private ImageView mMachineImage;
    private String[] mImages;

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding BaseActivity requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weights);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mMachineName = (TextView) findViewById(R.id.machineName);
        mSelectMachine = (Spinner) findViewById(R.id.selectMachine);
        mMachineImage = (ImageView) findViewById(R.id.machinePic);
        mImages = getResources().getStringArray(R.array.images);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            handleIntent(getIntent());
        } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

            if (mNfcAdapter == null) {
                //NFC is disabled or phone doesn't have NFC
                //mMachineName.setText("Please select a machine.");
            } else {
                //NFC is enabled
                //mMachineName.setText("Please select a machine below, or tap one nearby.");
            }
            mSelectMachine.setVisibility(View.VISIBLE);
            mSelectMachine.setOnItemSelectedListener(new SpinnerOnItemSelectedListener());
        }
    }

    private void handleIntent(Intent intent) {

        String type = intent.getType();
        if (MIME_TEXT_PLAIN.equals(type)) {
            mSelectMachine.setVisibility(View.INVISIBLE);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask().execute(tag);
        } else {
            Log.d(TAG, "Wrong mime type: " + type);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        if (mNfcAdapter != null) {
            stopForegroundDispatch(this, mNfcAdapter);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        if (mNfcAdapter != null) {
            setupForegroundDispatch(this, mNfcAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weights, menu);
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

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }
            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //set machine name
                String name = "string/" + result;
                int nameId = getResources().getIdentifier(name, null, getPackageName());
                mMachineName.setText(getResources().getString(nameId));
            }
            //set imageview to drawable of machine
            String uri = "drawable/" + result;
            int machineImageResource = getResources().getIdentifier(uri, null, getPackageName());

            try {
                Drawable machine = getResources().getDrawable(machineImageResource);
                mMachineImage.setImageDrawable(machine);
            } catch (Resources.NotFoundException e) {
                Toast.makeText(WeightsActivity.this, "No image found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addSet(View view) {
        //add new set
        if (numberOfSets > 0 && (allReps.get(numberOfSets-1).getText().toString().matches("") || allWeights.get(numberOfSets-1).getText().toString().matches("")))  {
                      Toast.makeText(getApplicationContext(),"Please finish current set before adding another",Toast.LENGTH_SHORT).show();
        } else if (numberOfSets < 10) {
                //create a new row to add
                TableRow row = new TableRow(WeightsActivity.this);

                //add Layouts to your new row
                EditText reps = new EditText(WeightsActivity.this);
                EditText weight = new EditText(WeightsActivity.this);

                // add reps/weights to your table
                row.addView(reps);
                row.addView(weight);

                reps.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1f));    // Center reps and weights on screen
                reps.setGravity(Gravity.CENTER);                                                                                                // Center user input
                reps.setRawInputType(Configuration.KEYBOARD_QWERTY);                                                                            // Set num keyboard
                reps.setLines(1);
                reps.setSingleLine();
                reps.setHint("Enter Reps");                                                                                                     // Set hint for user
                reps.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                reps.setInputType(InputType.TYPE_CLASS_NUMBER);
                weight.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
                weight.setGravity(Gravity.CENTER);
                weight.setRawInputType(Configuration.KEYBOARD_QWERTY);
                weight.setSingleLine();
                weight.setLines(1);
                weight.setHint("Enter Weight");
                weight.setInputType(InputType.TYPE_CLASS_NUMBER);
                weight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

                allReps.add(reps);                                                                                                              // Add to the list of reps
                allWeights.add(weight);

                //add your new row to the TableLayout:
                TableLayout table = (TableLayout) findViewById(R.id.workoutTable);
                table.addView(row);
                numberOfSets++;
        }
    }

    public void submitSets(View view) {

        // Remove the garbage
        for(int i=0; i < allReps.size(); i++){
            if (allReps.get(i).getText().toString().matches("") || allWeights.get(i).getText().toString().matches(""))  {
               allReps.remove(i);
               allWeights.remove(i);
               i = -1;
            }
        }

        // Query for machine that the user selected and send data to parse
        ParseQuery<ParseObject> pQuery = new ParseQuery<ParseObject>("Machine");

        pQuery.whereEqualTo("name", mMachineName.getText().toString().toLowerCase());
        pQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Toast.makeText(getApplicationContext(),"Please select a machine before submitting",Toast.LENGTH_SHORT).show();
                } else {
                    int[] reps = new int[allReps.size()];
                    int[] weights = new int[allWeights.size()];
                    TableLayout table = (TableLayout) findViewById(R.id.workoutTable);           // Used to reset the reps / sets

                    for (int i = 0; i < allReps.size(); i++) {
                        reps[i] = Integer.parseInt(allReps.get(i).getText().toString());
                        weights[i] = Integer.parseInt(allWeights.get(i).getText().toString());
                        Log.i("REP", Integer.toString(reps[i]));
                        Log.i("Weights", Integer.toString(weights[i]));

                        // Create the set
                        ParseObject mySet = new ParseObject("Set");
                        // Add a relation between the Post with objectId "1zEcyElZ80" and the comment
                        mySet.put("parent", ParseObject.createWithoutData("Set", object.getObjectId()));
                        mySet.put("repetitions", reps[i]);
                        mySet.put("resistance", weights[i]);

                        // This will save the set
                        mySet.saveInBackground();

                        // Reset the layouts for the user
                        table.removeAllViews();
                        mMachineName.setText("Select a machine");
                        mMachineImage.getResources().getDrawable(R.drawable.weight);
                        mSelectMachine.setSelection(0);
                    }
                }
            }
        });
    }

    private class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            String machineString = parent.getItemAtPosition(pos).toString();
//            Toast.makeText(parent.getContext(),
//                    "On Item Select : \n" + machineString,
//                    Toast.LENGTH_LONG).show();

            mMachineName.setText(machineString);

            String resource = "drawable/" + mImages[pos];
            int machineImageResource = getResources().getIdentifier(resource, null, getPackageName());
            Drawable machine = getResources().getDrawable(machineImageResource);
            mMachineImage.setImageDrawable(machine);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }
}

