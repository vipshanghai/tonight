package com.example.yuki.milky;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;


public class HomeActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {


    private LocationClient mLocationClient;
    private Button sendButton;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);

        sendButton = (Button) findViewById(R.id.button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocation();
            }
        });
    }

    private void sendLocation() {
        if (mLocationClient.isConnected()) {
            location = mLocationClient.getLastLocation();
            if (location != null) {


                // location
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setContentTitle("現在地")
                        .setContentText(location.getLatitude() + "," + location.getLongitude())
                        .setSmallIcon(R.drawable.ic_launcher);
//                    .setContentIntent(pIntent)
//                    .addAction(R.drawable.ic_launcher, "返信", pIntent)   //…… 1
//                    .addAction(R.drawable.ic_launcher, "転送", pIntent)
//                        .build();

                ArrayList<Notification> pages = new ArrayList<Notification>();

                // time
                Notification time = new NotificationCompat.Builder(this)
                        .setContentTitle("終電まで残り…")
                        .setContentText("time")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .extend(new NotificationCompat.WearableExtender())
                        .build();

                pages.add(time);

                // uber
                Notification uber = new NotificationCompat.Builder(this)
                        .setContentTitle("uber")
                        .setContentText("taxi")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .extend(new NotificationCompat.WearableExtender())
                        .build();

                pages.add(uber);

                // hotel
                Notification hotel = new NotificationCompat.Builder(this)
                        .setContentTitle("hotel")
                        .setContentText("info")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .extend(new NotificationCompat.WearableExtender())
                        .build();

                pages.add(hotel);

                builder.extend(new NotificationCompat.WearableExtender().addPages(pages));

                NotificationManagerCompat.from(this).notify(
                        0, builder.build());

                Toast.makeText(this, "sent", Toast.LENGTH_LONG).show();

            }
        } else {
            Toast.makeText(this, "not connected", Toast.LENGTH_LONG).show();
            mLocationClient.connect();
        }
    }

    /*
 * Called when the Activity becomes visible.
 */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }


    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();

    }
}
