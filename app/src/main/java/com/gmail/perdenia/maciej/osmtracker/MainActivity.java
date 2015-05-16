package com.gmail.perdenia.maciej.osmtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GpsDialogFragment.OkCancelListener, UploadDialogFragment.OkCancelListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String GPS_DIALOG_TAG = "gps-dialog-tag";
    private static final String UPLOAD_DIALOG_TAG = "upload-dialog-tag";

    private static final String LOCATION_KEY = "location-key";
    private static final String LAST_UPDATE_TIME_KEY = "last-update-time-key";
    private static final String REQUESTING_TRACKING_KEY = "requesting-tracking-key";
    private static final String MAP_CENTER_KEY = "map-center-key";
    private static final String MAP_ZOOM_KEY = "map-zoom-key";
    private static final String TRACK_OVERLAY_POINTS_KEY = "track-overlay-points-key";
    private static final String GPX_FILENAME_KEY = "gpx-filename-key";
    private static final String TRACK_POINTS_KEY = "track-points-key";
    private static final String WAY_POINT_KEY = "way-point-key";

    private static final String USER_ID_KEY = "user-id-key";
    private static final String USER_NAME_KEY = "user-name-key";
    private static final String USER_SURNAME_KEY = "user-surname-key";

    private static final String SERVER_IP_KEY = "server-ip-key";
    private static final String SERVER_PORT_KEY = "server-port-key";

    private static final int PREFERRED_ZOOM = 18;
    private static final int UPDATE_INTERVAL_IN_MILLIS = 5000;
    private static final int FASTEST_UPDATE_INTERVAL_IN_MILLIS = UPDATE_INTERVAL_IN_MILLIS / 2;

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

    private FloatingActionButton mLocationFab;
    private FloatingActionButton mTrackingFab;

    private boolean mRequestingTracking;
    private org.osmdroid.bonuspack.overlays.Polyline mTrackOverlay;
    private ArrayList<GeoPoint> mTrackOverlayPoints;
    private ArrayList<WayPoint> mTrackPoints;
    private WayPoint mWayPoint;
    private String mGpxFilename;

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener;
    private User mUser;
    private ArrayList<User> mOtherUsers;
    private String mServerIp;
    private int mServerPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        init();
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
    }

    private void init() {
        Log.i(TAG, "Inicjalizacja zmiennych (init())");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        mLocationFab = (FloatingActionButton) findViewById(R.id.fab_location);
        mLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGpsEnabled()) {
                    new GpsDialogFragment().show(getFragmentManager(), GPS_DIALOG_TAG);
                }
                if (mLocationFab.isChecked()) {
                    saveWayPoint();
                } else {
                    goToCurrentLocation();
                }
            }
        });

        mTrackingFab = (FloatingActionButton) findViewById(R.id.fab_tracking);
        mTrackingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTrackingFab.isChecked()) {
                    if (!isGpsEnabled()) {
                        new GpsDialogFragment().show(getFragmentManager(), GPS_DIALOG_TAG);
                    }
                    startTracking();
                } else {
                    stopTracking();
                }
                mTrackingFab.toggle();
            }
        });

        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setMultiTouchControls(true);
        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                setLocationFabState();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                setLocationFabState();
                return true;
            }
        });
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(6);
        GeoPoint poland = new GeoPoint(52.0, 20.0);
        mMapController.setCenter(poland);

        mRequestingTracking = false;
        mGpxFilename = "";

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        int id = Integer.valueOf(sharedPreferences.getString(USER_ID_KEY, "0"));
        String name = sharedPreferences.getString(USER_NAME_KEY, "");
        String surname = sharedPreferences.getString(USER_SURNAME_KEY, "");
        mUser = new User(id, name, surname);
        mServerIp = sharedPreferences.getString(SERVER_IP_KEY, "");
        mServerPort = Integer.valueOf(sharedPreferences.getString(SERVER_PORT_KEY, "0"));
        mPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                switch (key) {
                    case USER_ID_KEY:
                        String newUserId = sharedPreferences.getString(USER_ID_KEY, "0");
                        mUser.setId(Integer.valueOf(newUserId));
                        Log.i(TAG, "Nowe ID użytkownika: " + newUserId);
                        break;
                    case USER_NAME_KEY:
                        String newUserName = sharedPreferences.getString(USER_NAME_KEY, "");
                        mUser.setName(newUserName);
                        Log.i(TAG, "Nowe imię użytkownika: " + newUserName);
                        break;
                    case USER_SURNAME_KEY:
                        String newUserSurname = sharedPreferences.getString(USER_SURNAME_KEY, "");
                        mUser.setSurname(newUserSurname);
                        Log.i(TAG, "Nowe nazwisko użytkownika: " + newUserSurname);
                        break;
                    case SERVER_IP_KEY:
                        String newServerIp = sharedPreferences.getString(SERVER_IP_KEY, "");
                        mServerIp = newServerIp;
                        Log.i(TAG, "Nowe IP serwera: " + newServerIp);
                        break;
                    case SERVER_PORT_KEY:
                        String newServerPort = sharedPreferences.getString(SERVER_PORT_KEY, "0");
                        mServerPort = Integer.valueOf(newServerPort);
                        Log.i(TAG, "Nowy port serwera: " + newServerPort);
                        break;
                    default:
                        break;
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(mPrefChangeListener);

        mOtherUsers = new ArrayList<>();
    }

    private void updateUI() {
        Log.i(TAG, "Odświeżanie widoku mapy (updateUI())");

        mMapView.getOverlays().clear();

//        mMapView.getOverlays().add(new ScaleBarOverlay(this));

        if (mRequestingTracking) {
            mTrackOverlayPoints.add(mCurrentGeoPoint);
            mTrackOverlay.setPoints(mTrackOverlayPoints);
            mMapView.getOverlays().add(mTrackOverlay);
        }

        Marker userMarker = new Marker(mMapView);
        userMarker.setPosition(mCurrentGeoPoint);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userMarker.setIcon(getResources().getDrawable(R.drawable.map_icon_user_location));
        userMarker.setTitle(mUser.getName() + " " + mUser.getSurname());
        userMarker.setSnippet("Twoje aktualne położenie");
        mMapView.getOverlays().add(userMarker);

        if (mOtherUsers.size() > 0) {
            for (User u : mOtherUsers) {
                Marker otherUserMarker = new Marker(mMapView);
                otherUserMarker.setPosition(new GeoPoint(
                        u.getWayPoint().getLatitude(), u.getWayPoint().getLongitude()));
                otherUserMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                otherUserMarker.setIcon(
                        getResources().getDrawable(R.drawable.map_icon_other_users_location));
                otherUserMarker.setTitle(u.getName() + " " + u.getSurname());
                otherUserMarker.setSnippet("Położenie powyższego użytkownika");
                mMapView.getOverlays().add(otherUserMarker);
            }
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
                    new Thread(new Client(mServerIp, mServerPort, this, mUser)).start();
                }

                updateUI();
                mMapController.setZoom(PREFERRED_ZOOM);
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
            new Thread(new Client(mServerIp, mServerPort, this, mUser)).start();
        }

        if (mRequestingTracking) {
            WayPoint trackPoint = new WayPoint(
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    mLastUpdateTime, mCurrentLocation.getAltitude());
            mTrackPoints.add(trackPoint);
        }

        updateUI();
    }

    private void goToCurrentLocation() {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            mCurrentGeoPoint = new GeoPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
            mLastUpdateTime = new Date();

            mUser.setWayPoint(new WayPoint(
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    mLastUpdateTime, mCurrentLocation.getAltitude()));
            if (isOnline()) {
                new Thread(new Client(mServerIp, mServerPort, this, mUser)).start();
            }

            updateUI();
            mMapController.setZoom(PREFERRED_ZOOM);
            mMapController.animateTo(mCurrentGeoPoint);
        }
    }

    private void saveWayPoint() {
        if (mCurrentLocation != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
            mGpxFilename = sdf.format(mLastUpdateTime) + "_" + mUser.getSurname() + "_"
                    + mUser.getName();
            mWayPoint = new WayPoint(
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    mLastUpdateTime, mCurrentLocation.getAltitude());
            new UploadDialogFragment().show(getFragmentManager(), UPLOAD_DIALOG_TAG);
        }
    }

    private void setLocationFabState() {
        if (mCurrentLocation != null) {
            BoundingBoxE6 boundingBox = mMapView.getBoundingBox();
            double latNorth = (double) boundingBox.getLatNorthE6() / 1000000;
            double lonWest = (double) boundingBox.getLonWestE6() / 1000000;
            double latSouth = (double) boundingBox.getLatSouthE6() / 1000000;
            double lonEast = (double) boundingBox.getLonEastE6() / 1000000;

            double lat = mCurrentLocation.getLatitude();
            double lon = mCurrentLocation.getLongitude();

            int zoom = mMapView.getZoomLevel();

            if (lat < latNorth && lat > latSouth &&
                    lon > lonWest && lon < lonEast && zoom == PREFERRED_ZOOM) {
                mLocationFab.setChecked(true);
            } else {
                mLocationFab.setChecked(false);
            }
        }
    }

    private void startTracking() {
        if (!mRequestingTracking) {
            if (mCurrentLocation != null) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Toast.makeText(this, "Rozpoczęto śledzenie", Toast.LENGTH_LONG).show();

                mTrackOverlay = new Polyline(new DefaultResourceProxyImpl(this));
                mTrackOverlay.setColor(getResources().getColor(R.color.blue_A700));
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
            }
        }
    }

    private void stopTracking() {
        if (mRequestingTracking) {
            mRequestingTracking = false;
            if (!mTrackPoints.isEmpty()) {
                new UploadDialogFragment().show(getFragmentManager(), UPLOAD_DIALOG_TAG);
            }
            updateUI();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
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

        Location mapCenter = new Location("dummyprovider");
        mapCenter.setLatitude(mMapView.getMapCenter().getLatitude());
        mapCenter.setLongitude(mMapView.getMapCenter().getLongitude());
        savedInstanceState.putParcelable(MAP_CENTER_KEY, mapCenter);

        savedInstanceState.putInt(MAP_ZOOM_KEY, mMapView.getZoomLevel());
        savedInstanceState.putParcelableArrayList(TRACK_OVERLAY_POINTS_KEY, mTrackOverlayPoints);

        savedInstanceState.putString(GPX_FILENAME_KEY, mGpxFilename);
        savedInstanceState.putParcelableArrayList(TRACK_POINTS_KEY, mTrackPoints);
        savedInstanceState.putParcelable(WAY_POINT_KEY, mWayPoint);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Pobieranie wartości z bundle'a (updateValuesFromBundle())");

        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                if (mCurrentLocation != null) {
                    mCurrentGeoPoint = new GeoPoint(
                            mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                }
            }

            if(savedInstanceState.keySet().contains(LAST_UPDATE_TIME_KEY)) {
                mLastUpdateTime = (Date) savedInstanceState.getSerializable(LAST_UPDATE_TIME_KEY);
            }

            if (savedInstanceState.keySet().contains(MAP_CENTER_KEY)) {
                Location mapCenter = savedInstanceState.getParcelable(MAP_CENTER_KEY);
                mSavedMapCenter = new GeoPoint(mapCenter.getLatitude(), mapCenter.getLongitude());
            }

            if (savedInstanceState.keySet().contains(MAP_ZOOM_KEY)) {
                mMapController.setZoom(savedInstanceState.getInt(MAP_ZOOM_KEY));
            }

            if (savedInstanceState.keySet().contains(REQUESTING_TRACKING_KEY)) {
                mRequestingTracking = savedInstanceState.getBoolean(REQUESTING_TRACKING_KEY);
                mTrackingFab.setChecked(mRequestingTracking);
            }

            if (savedInstanceState.keySet().contains(TRACK_OVERLAY_POINTS_KEY) &&
                    mRequestingTracking) {
                mTrackOverlay = new Polyline(new DefaultResourceProxyImpl(this));
                mTrackOverlay.setColor(getResources().getColor(R.color.blue_A700));
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

            if (savedInstanceState.keySet().contains(WAY_POINT_KEY)) {
                mWayPoint = savedInstanceState.getParcelable(WAY_POINT_KEY);
            }
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

    private void saveAndSendGpx(String name) {
        char[] gpx;
        if (mWayPoint != null) {
            gpx = GpxCreator.createGpxWayPoint(
                    MainActivity.this, mGpxFilename, name, mWayPoint);
            GpxCreator.saveOnInternalStorage(
                    MainActivity.this, mGpxFilename, new String(gpx));
            GpxCreator.saveOnExternalStorage(mGpxFilename, new String(gpx));
            mWayPoint = null;
        } else {
            gpx = GpxCreator.createGpxTrack(
                    MainActivity.this, mGpxFilename, name, mTrackPoints);
            GpxCreator.saveOnInternalStorage(
                    MainActivity.this, mGpxFilename, new String(gpx));
            GpxCreator.saveOnExternalStorage(mGpxFilename, new String(gpx));
        }
        if (isOnline()) {
            new Thread(
                    new Client(mServerIp, mServerPort, this, mUser, new String(gpx))).start();
        } else {
            Toast.makeText(
                    this, "Niepowodzenie - brak dostępu do Internetu", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void gpsOnOk() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, 1);
    }

    @Override
    public void gpsOnCancel() {

    }

    @Override
    public void uploadOnOk(EditText input) {
        String name = input.getText().toString();
        if (name.equals("")) {
            name = mGpxFilename;
        }
        saveAndSendGpx(name);
    }

    @Override
    public void uploadOnCancel() {

    }
}
