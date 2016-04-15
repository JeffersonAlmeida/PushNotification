package push.com.br.pushnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static push.com.br.pushnotification.QuickstartPreferences.*;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1234;

    private ProgressBar myRegistrationProgressBar;
    private TextView info;

    private BroadcastReceiver myRegistrationBroadcastReceiver;
    private SharedPreferences sharedPreferences;

    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        myRegistrationProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        myRegistrationProgressBar.setVisibility(View.VISIBLE);
        info = (TextView) findViewById(R.id.info);

        myRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                myRegistrationProgressBar.setVisibility(View.GONE);
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
                int msg = sentToken ? R.string.gcm_send_message : R.string.token_error_message;
                info.setText(msg);
            }
        };

        registerReceiver();

        if ( checkPlayServices() ){
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerReceiver() {
        if ( !isReceiverRegistered ){
            IntentFilter filter = new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(myRegistrationBroadcastReceiver, filter);

            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();;
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(myRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    /**
     * Ensure Devices Have the Google Play services APK
     */
    private void ensureGooglePlayServices() {
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (response == ConnectionResult.SUCCESS ){
            Log.i(TAG, "SUCCESS");
        }else if (response == ConnectionResult.SERVICE_MISSING ){
            Log.i(TAG, "SERVICE_MISSING");
            GoogleApiAvailability
                    .getInstance()
                    .showErrorDialogFragment(this, response, REQUEST_CODE);
        }

        Log.i(TAG, "response: " + response);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
