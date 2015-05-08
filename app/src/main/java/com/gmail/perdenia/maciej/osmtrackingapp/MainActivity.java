package com.gmail.perdenia.maciej.osmtrackingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
                                     GoogleApiClient.OnConnectionFailedListener, LocationListener,
        FloatingActionButton.OnCheckedChangeListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String LOCATION_KEY = "location-key";
    private static final String LAST_UPDATE_TIME_KEY = "last-update-time-key";
    private static final String REQUESTING_TRACKING_KEY = "requesting-tracking-key";
    private static final String MAP_VIEW_CENTER_LAT_KEY = "map-view-center-lat-key";
    private static final String MAP_VIEW_CENTER_LON_KEY = "map-view-center-lon-key";
    private static final String MAP_VIEW_ZOOM_KEY = "map-view-zoom-key";
    private static final String TRACK_OVERLAY_POINTS_KEY = "track-overlay-points-key";
    private static final String GPX_FILENAME_KEY = "gpx-filename-key";
    private static final String TRACK_POINTS_KEY = "track-points-key";

    private static final String USER_ID_KEY = "user-id-key";
    private static final String USER_NAME_KEY = "user-name-key";
    private static final String USER_SURNAME_KEY = "user-surname-key";

    public static final int PREFERRED_ZOOM = 18;
    public static final int UPDATE_INTERVAL_IN_MILLIS = 5000;
    public static final int FASTEST_UPDATE_INTERVAL_IN_MILLIS =
            UPDATE_INTERVAL_IN_MILLIS / 2;

    private LocationManager mLocationManager;
    private ConnectivityManager mConnectivityManager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Date mLastUpdateTime;

    private MapView mMapView;
    private MapController mMapController;

    private GeoPoint mCurrentGeoPoint;
    private GeoPoint mSavedMapCenter;

    private FloatingActionButton mGoToLocationFab;

    private boolean mRequestingTracking;
    private org.osmdroid.bonuspack.overlays.Polyline mTrackOverlay;
    private ArrayList<GeoPoint> mTrackOverlayPoints;
    private ArrayList<WayPoint> mTrackPoints;
    private String mGpxFilename;

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener;
    private User mUser;
    private ArrayList<User> mOtherUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        init();
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
    }

    private void init() {
        Log.i(TAG, "Inicjalizacja zmiennych (init())");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setMultiTouchControls(true);
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(PREFERRED_ZOOM);

        mRequestingTracking = false;
        mGpxFilename = "";

        mGoToLocationFab = (FloatingActionButton) findViewById(R.id.fab_go_to_location);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        int id = Integer.valueOf(sharedPreferences.getString(USER_ID_KEY, "-1"));
        String name = sharedPreferences.getString(USER_NAME_KEY, "");
        String surname = sharedPreferences.getString(USER_SURNAME_KEY, "");
        mUser = new User(id, name, surname);
        mPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                mUser.setId(Integer.valueOf(sharedPreferences.getString(USER_ID_KEY, "-1")));
                mUser.setName(sharedPreferences.getString(USER_NAME_KEY, ""));
                mUser.setSurname(sharedPreferences.getString(USER_SURNAME_KEY, ""));
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
        mOtherUsers = new ArrayList<>();
    }

    private void updateUI() {
        Log.i(TAG, "Odświeżanie widoku mapy (updateUI())");

        mMapView.getOverlays().clear();

        mMapView.getOverlays().add(new ScaleBarOverlay(this));

        if (mRequestingTracking) {
            mTrackOverlayPoints.add(mCurrentGeoPoint);
            mTrackOverlay.setPoints(mTrackOverlayPoints);
            mMapView.getOverlays().add(mTrackOverlay);
        }

        ArrayList<OverlayItem> overlayItems = new ArrayList<>(1);
        OverlayItem currentLocationItem =
                new OverlayItem("Ty", "Twoje aktualne położenie", mCurrentGeoPoint);
        overlayItems.add(currentLocationItem);
        ItemizedIconOverlay<OverlayItem> currentLocationOverlay =
                new ItemizedIconOverlay<>(overlayItems,
                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(final int index,
                                                             final OverlayItem item) {
                                Toast.makeText(getApplicationContext(), item.getTitle() + "\n"
                                                + item.getSnippet(), Toast.LENGTH_LONG).show();
                                return true;
                            }
                            @Override
                            public boolean onItemLongPress(final int index,
                                                           final OverlayItem item) {
                                return false;
                            }
                }, new DefaultResourceProxyImpl(this));
        mMapView.getOverlays().add(currentLocationOverlay);

        if (mOtherUsers.size() > 0) {
            ArrayList<OverlayItem> usersOverlayItems = new ArrayList<>(mOtherUsers.size());
            for (User u : mOtherUsers) {
                String fullName = u.getName() + " " + u.getSurname();
                OverlayItem userItem = new OverlayItem(fullName,
                        "Położenie użytkownika " + fullName, new GeoPoint(
                        u.getWayPoint().getLatitude(), u.getWayPoint().getLongitude()));
                usersOverlayItems.add(userItem);
            }
            ItemizedIconOverlay<OverlayItem> usersOverlay =
                    new ItemizedIconOverlay<>(usersOverlayItems,
                            new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                @Override
                                public boolean onItemSingleTapUp(final int index,
                                                                 final OverlayItem item) {
                                    Toast.makeText(getApplicationContext(), item.getTitle() + "\n"
                                            + item.getSnippet(), Toast.LENGTH_LONG).show();
                                    return true;
                                }
                                @Override
                                public boolean onItemLongPress(final int index,
                                                               final OverlayItem item) {
                                    return false;
                                }
                            }, new DefaultResourceProxyImpl(this));
            mMapView.getOverlays().add(usersOverlay);
        }

        mMapView.invalidate();
    }

    private void buildGoogleApiClient() {
        Log.i(TAG, "Tworzenie GoogleApiClient (buildGoogleApiClient()");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        Log.i(TAG, "Tworzenie żądania położenia (createLocationRequest())");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLIS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLIS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Połączono z GoogleApiClient (onConnected())");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mCurrentLocation != null) {
                mCurrentGeoPoint = new GeoPoint(
                        mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mLastUpdateTime = new Date();

                mUser.setWayPoint(new WayPoint(
                        mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                        mLastUpdateTime, mCurrentLocation.getAltitude()));
                if (isOnline()) {
                    new Thread(new Client(this, mUser)).start();
                }

                updateUI();
                mMapController.setCenter(mCurrentGeoPoint);
            }
        }
        if (mSavedMapCenter != null) mMapController.setCenter(mSavedMapCenter);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Log.i(TAG, "Uruchomiono aktualizacje położenia (startLocationUpdates())");

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        Log.i(TAG, "Zatrzymano aktualizacje położenia (stopLocationUpdates())");

        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Zawieszono połączenie (onConnectionSuspended())");

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Połączenie się nie powiodło: ConnectionResult.getErrorCode() = " +
                connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Nowe położenie (onLocationChanged()): " + location.getLatitude() +
                "/" + location.getLongitude());

        mCurrentLocation = location;
        mCurrentGeoPoint = new GeoPoint(
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mLastUpdateTime = new Date();

        mUser.setWayPoint(new WayPoint(
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mLastUpdateTime,
                mCurrentLocation.getAltitude()));
        if (isOnline()) {
            new Thread(new Client(this, mUser)).start();
        }

        if (mRequestingTracking) {
            WayPoint trackPoint = new WayPoint(
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    mLastUpdateTime, mCurrentLocation.getAltitude());
            mTrackPoints.add(trackPoint);
        }

        updateUI();
    }

    public void goToCurrentLocation(View view) {
        if (!isGpsEnabled()) {
            buildGpsAlertDialog();
        }

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            mCurrentGeoPoint = new GeoPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
            mLastUpdateTime = new Date();

            mUser.setWayPoint(new WayPoint(
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    mLastUpdateTime, mCurrentLocation.getAltitude()));
            if (isOnline()) {
                new Thread(new Client(MainActivity.this, mUser)).start();
            }

            updateUI();
            mMapController.setZoom(PREFERRED_ZOOM);
            mMapController.animateTo(mCurrentGeoPoint);
        }
    }

/*    public void startTracking(View view) {
        if (!isGpsEnabled()) {
            buildGpsAlertDialog();
        }

        if (!mRequestingTracking) {
            if (mCurrentLocation != null) {
                mTrackOverlay = new Polyline(new DefaultResourceProxyImpl(this));
                mTrackOverlay.setColor(getResources().getColor(R.color.accent));
                mTrackOverlayPoints = new ArrayList<>();

                mTrackPoints = new ArrayList<>();
                WayPoint trackPoint = new WayPoint(
                        mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                        mLastUpdateTime, mCurrentLocation.getAltitude());
                mTrackPoints.add(trackPoint);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
                mGpxFilename = sdf.format(mLastUpdateTime) + "_" + mUser.getSurname() + "_"
                        + mUser.getName();

                mRequestingTracking = true;
                setTrackingButtonsState();
            }
        }
    }

    public void stopTracking(View view) {
        if (mRequestingTracking) {
            mRequestingTracking = false;
            setTrackingButtonsState();
            if (!mTrackPoints.isEmpty()) {
                buildUploadAlertDialog(null);
            }
        }
    }

    private void setTrackingButtonsState() {
        if (mRequestingTracking) {
            mStartTrackingBtn.setEnabled(false);
            mStopTrackingBtn.setEnabled(true);
        } else {
            mStartTrackingBtn.setEnabled(true);
            mStopTrackingBtn.setEnabled(false);
        }
    }*/

    public void saveWayPoint(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        mGpxFilename = sdf.format(mLastUpdateTime) + "_" + mUser.getSurname() + "_"
                + mUser.getName();
        WayPoint wayPoint = new WayPoint(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(), mLastUpdateTime, mCurrentLocation.getAltitude());
        buildUploadAlertDialog(wayPoint);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putSerializable(LAST_UPDATE_TIME_KEY, mLastUpdateTime);

        savedInstanceState.putBoolean(REQUESTING_TRACKING_KEY, mRequestingTracking);

        savedInstanceState.putDouble(
                MAP_VIEW_CENTER_LAT_KEY, mMapView.getMapCenter().getLatitude());
        savedInstanceState.putDouble(
                MAP_VIEW_CENTER_LON_KEY, mMapView.getMapCenter().getLongitude());
        savedInstanceState.putInt(MAP_VIEW_ZOOM_KEY, mMapView.getZoomLevel());
        savedInstanceState.putParcelableArrayList(TRACK_OVERLAY_POINTS_KEY, mTrackOverlayPoints);

        savedInstanceState.putString(GPX_FILENAME_KEY, mGpxFilename);
        savedInstanceState.putParcelableArrayList(TRACK_POINTS_KEY, mTrackPoints);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Pobieranie wartości z bundle'a (updateValuesFromBundle())");

        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                mCurrentGeoPoint = new GeoPoint(
                        mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            }

            if(savedInstanceState.keySet().contains(LAST_UPDATE_TIME_KEY)) {
                mLastUpdateTime = (Date) savedInstanceState.getSerializable(LAST_UPDATE_TIME_KEY);
            }

            if (savedInstanceState.keySet().contains(MAP_VIEW_CENTER_LAT_KEY) &&
                    savedInstanceState.keySet().contains(MAP_VIEW_CENTER_LON_KEY)) {
                mSavedMapCenter =
                        new GeoPoint(savedInstanceState.getDouble(MAP_VIEW_CENTER_LAT_KEY),
                                savedInstanceState.getDouble(MAP_VIEW_CENTER_LON_KEY));
            }

            if (savedInstanceState.keySet().contains(MAP_VIEW_ZOOM_KEY)) {
                mMapController.setZoom(savedInstanceState.getInt(MAP_VIEW_ZOOM_KEY));
            }

            if (savedInstanceState.keySet().contains(REQUESTING_TRACKING_KEY)) {
                mRequestingTracking = savedInstanceState.getBoolean(REQUESTING_TRACKING_KEY);
                //setTrackingButtonsState();
            }

            if (savedInstanceState.keySet().contains(TRACK_OVERLAY_POINTS_KEY) &&
                    mRequestingTracking) {
                mTrackOverlay = new Polyline(new DefaultResourceProxyImpl(this));
                mTrackOverlay.setColor(
                        getResources().getColor(getResources().getColor(R.color.accent)));
                mTrackOverlayPoints =
                        savedInstanceState.getParcelableArrayList(TRACK_OVERLAY_POINTS_KEY);
                mTrackOverlay.setPoints(mTrackOverlayPoints);
            }

            if (savedInstanceState.keySet().contains(GPX_FILENAME_KEY)) {
                mGpxFilename = savedInstanceState.getString(GPX_FILENAME_KEY);
            }

            if (savedInstanceState.keySet().contains(TRACK_POINTS_KEY)) {
                mTrackPoints = savedInstanceState.getParcelableArrayList(TRACK_POINTS_KEY);
            }

            updateUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    public void openSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private boolean isGpsEnabled() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void buildGpsAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.dialog_text_enable_gps));
        dialog.setPositiveButton(getResources().getString(R.string.dialog_btn_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 1);
                    }
                });
        dialog.setNegativeButton(getString(R.string.dialog_btn_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isOnline() {
        if (mConnectivityManager == null) {
            mConnectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    public void setOtherUsers(ArrayList<User> otherUsers) {
        mOtherUsers = otherUsers;
    }

    private void buildUploadAlertDialog(final WayPoint wayPoint) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.dialog_text_upload_name));

        final EditText input = new EditText(this);
        dialog.setView(input);

        dialog.setPositiveButton(getResources().getString(R.string.dialog_btn_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        String name = input.getText().toString();
                        saveAndSendGpx(name, wayPoint);
                    }
        });

        dialog.setNegativeButton(getResources().getString(R.string.dialog_btn_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        saveAndSendGpx(mGpxFilename, wayPoint);
                    }
                });

        dialog.show();
    }

    private void saveAndSendGpx(String name, WayPoint wayPoint) {
        char[] gpx;
        if (wayPoint != null) {
            gpx = GpxCreator.createGpxWayPoint(
                    MainActivity.this, mGpxFilename, name, wayPoint);
            GpxCreator.saveOnInternalStorage(
                    MainActivity.this, mGpxFilename, new String(gpx));
            GpxCreator.saveOnExternalStorage(mGpxFilename, new String(gpx));
        } else {
            gpx = GpxCreator.createGpxTrack(
                    MainActivity.this, mGpxFilename, name, mTrackPoints);
            GpxCreator.saveOnInternalStorage(
                    MainActivity.this, mGpxFilename, new String(gpx));
            GpxCreator.saveOnExternalStorage(mGpxFilename, new String(gpx));
        }
        if (isOnline()) {
            new Thread(
                    new Client(MainActivity.this, mUser, new String(gpx))).start();
        }
    }

    @Override
    public void onCheckedChanged(FloatingActionButton fabView, boolean isChecked) {
        // TODO reakcja na zmianę stanu fab'a
    }
}
