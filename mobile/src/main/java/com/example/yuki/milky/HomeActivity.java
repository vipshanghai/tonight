package com.example.yuki.milky;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.yuki.milky.model.Hotel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class HomeActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {


    private static final String LOG_TAG = "tonight";
    private LocationClient mLocationClient;
    private Button sendButton;
    private Location location;

    private RequestQueue mQueue;
    private ArrayList<Hotel> hotelList;

    private Response.Listener<InputStream> mListener;

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
                callHotel();
            }
        } else {
            Toast.makeText(this, "not connected", Toast.LENGTH_LONG).show();
            mLocationClient.connect();
        }
    }

    private void callHotel() {
        String url =
                "http://yoyaq.com/api/NeighborHotel?Latitude="+location.getLatitude()+"&Longitude="+location.getLongitude()+"&DispNum=1";

        mQueue = Volley.newRequestQueue(this);
        InputStreamRequest request = new InputStreamRequest(url,
                new Response.Listener<InputStream>() {

                    @Override
                    public void onResponse(InputStream in) {
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
                        try {
                            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
                            xmlPullParser.setInput(bufferedInputStream, "UTF-8");
                            int eventType = xmlPullParser.getEventType();

                            hotelList = new ArrayList<Hotel>();
                            Hotel hotel = new Hotel();
                            while (true) {
                                if (eventType == XmlPullParser.START_DOCUMENT) {
                                    Log.d("XmlPullParser", "文書開始");
                                } else if (eventType == XmlPullParser.END_DOCUMENT) {
                                    Log.d("XmlPullParser", "文書終了");
                                    hotelList.add(hotel);
                                    break;
                                } else if (eventType == XmlPullParser.START_TAG) {
                                    String name = xmlPullParser.getName();
                                    Log.d("XmlPullParser", "開きタグ: " + name);
                                    if (StringUtils.equals("HotelName", name)) {
                                        hotel = new Hotel();
                                        hotel.hotelName = xmlPullParser.nextText();
                                    } else if (StringUtils.equals("ImageUrl", name)) {
                                        hotel.imageUrl = xmlPullParser.nextText();
                                    }
                                } else if (eventType == XmlPullParser.END_TAG) {
                                    Log.d("XmlPullParser", "閉じタグ: " + xmlPullParser.getName());
                                }
                                eventType = xmlPullParser.next();
                            }
                        } catch (XmlPullParserException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        } catch (IOException e) {

                            Log.e(LOG_TAG, e.getMessage());
                        } finally {
                            if (bufferedInputStream != null) {
                                try {
                                    bufferedInputStream.close();
                                } catch (IOException e) {
                                    Log.e(LOG_TAG, e.getMessage());
                                }
                            }
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    Log.e(LOG_TAG, e.getMessage());
                                }
                            }
                            getHotelImages();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // error
            }
        });
        mQueue.add(request);
    }

    private void getHotelImages() {
        if (hotelList != null && hotelList.size() > 0) {
            mQueue.add(new ImageRequest(hotelList.get(0).imageUrl,new Response.Listener<Bitmap>() {

                @Override
                public void onResponse(Bitmap response) {
                    hotelList.get(0).hotelBitmap = response;
                    sendNotification();
                }
            }, 0, 0, null, null));
        }
    }

    private void sendNotification() {
        if (location != null && hotelList != null && hotelList.size() > 0) {

            // time
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle("終電まで残り…")
                    .setContentText("30分")
                    .setSmallIcon(R.drawable.mono_uberx)
                    .extend(new NotificationCompat.WearableExtender().setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.train)))
                    ;

            ArrayList<Notification> pages = new ArrayList<Notification>();

            // uber
            Notification uber = new NotificationCompat.Builder(this)
                    .setContentTitle("uber")
                    .setContentText("東京タクシー")
                    .extend(new NotificationCompat.WearableExtender().setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.bk)))
                    .build();

            pages.add(uber);

            // hotel
            Notification hotel = new NotificationCompat.Builder(this)
                    .setContentTitle("hotel")
                    .setContentText(hotelList.get(0).hotelName)
                    .extend(new NotificationCompat.WearableExtender().setBackground(hotelList.get(0).hotelBitmap))
                    .build();

            pages.add(hotel);

            builder.extend(new NotificationCompat.WearableExtender().addPages(pages));

            NotificationManagerCompat.from(this).notify(
                    0, builder.build());

            Toast.makeText(this, "sent", Toast.LENGTH_LONG).show();

        }else {
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

    class InputStreamRequest extends Request<InputStream> {

        /**
         *
         * @param method
         * @param url
         * @param listener
         * @param errorListener
         */
        public InputStreamRequest(int method, String url,
                                  Response.Listener<InputStream> listener,
                                  Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            mListener = listener;
        }

        /**
         *
         * @param url
         * @param listener
         * @param errorListener
         */
        public InputStreamRequest(String url, Response.Listener<InputStream> listener,
                                  Response.ErrorListener errorListener) {
            this(Method.GET, url, listener, errorListener);
        }

        @Override
        protected void deliverResponse(InputStream response) {
            mListener.onResponse(response);
        }

        @Override
        protected Response<InputStream> parseNetworkResponse(NetworkResponse response) {
            InputStream is = new ByteArrayInputStream(response.data);
            return Response.success(is, HttpHeaderParser.parseCacheHeaders(response));
        }
    }
}
