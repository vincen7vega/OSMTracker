package com.gmail.perdenia.maciej.osmtrackingapp;

import android.app.DialogFragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
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

// TODO pomyśleć nad używaniem fragmentów

public class MainActivity extends ActionBarActivity
                          implements GoogleApiClient.ConnectionCallbacks,
                                     GoogleApiClient.OnConnectionFailedListener,
                                     LocationListener {

    public static final String TAG = "MainActivity";
    public static final String GPX_UPLOAD_DIALOG_TAG = "GpxUploadDialog";

    private static final String LOCATION_KEY = "location-key";
    private static final String LAST_UPDATE_TIME_KEY = "last-update-time";
    private static final String REQUESTING_TRACKING_KEY = "requesting-tracking-key";
    private static final String MAP_VIEW_CENTER_LAT_KEY = "map-view-center-lat-key";
    private static final String MAP_VIEW_CENTER_LON_KEY = "map-view-center-lon-key";
    private static final String MAP_VIEW_ZOOM_KEY = "map-view-zoom-key";
    private static final String TRACK_OVERLAY_POINTS_KEY = "track-overlay-points-key";
    private static final String GPX_FILENAME_KEY = "gpx-filename-key";
    private static final String TRACK_POINTS_KEY = "track-points-key";

    public static final int PREFERRED_ZOOM = 18;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final String PREFERRED_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private SimpleDateFormat mSimpleDateFormat;
    private String mLastUpdateTime;

    private MapView mMapView;
    private MapController mMapController;

    private GeoPoint mCurrentGeoPoint;
    private GeoPoint mSavedMapCenter;
    private ResourceProxy mResourceProxy;

    private Button mStartTrackingBtn;
    private Button mStopTrackingBtn;

    private boolean mRequestingTracking;
    private org.osmdroid.bonuspack.overlays.Polyline mTrackOverlay;
    private ArrayList<GeoPoint> mTrackOverlayPoints;
    private ArrayList<TrackPoint> mTrackPoints;
    private String mGpxFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        initUI();

        mResourceProxy = new DefaultResourceProxyImpl(this);
        mSimpleDateFormat = new SimpleDateFormat(PREFERRED_DATE_FORMAT);
        mLastUpdateTime = "";
        mGpxFilename = "";
        mSavedMapCenter = null;
        mRequestingTracking = false;
        setButtonsEnabledState();

        updateValuesFromBundle(savedInstanceState);

        buildGoogleApiClient();
    }

    private void initUI() {
        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(PREFERRED_ZOOM);

        mStartTrackingBtn = (Button) findViewById(R.id.button_start_tracking);
        mStopTrackingBtn = (Button) findViewById(R.id.button_stop_tracking);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                mCurrentGeoPoint = new GeoPoint(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude());
            }

            if(savedInstanceState.keySet().contains(LAST_UPDATE_TIME_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATE_TIME_KEY);
            }

            if (savedInstanceState.keySet().contains(REQUESTING_TRACKING_KEY)) {
                mRequestingTracking = savedInstanceState.getBoolean(REQUESTING_TRACKING_KEY);
                setButtonsEnabledState();
            }

            if (savedInstanceState.keySet().contains(MAP_VIEW_CENTER_LAT_KEY) &&
                    savedInstanceState.keySet().contains(MAP_VIEW_CENTER_LON_KEY)) {
                mSavedMapCenter = new GeoPoint(
                        savedInstanceState.getDouble(MAP_VIEW_CENTER_LAT_KEY),
                        savedInstanceState.getDouble(MAP_VIEW_CENTER_LON_KEY));
            }

            if (savedInstanceState.keySet().contains(MAP_VIEW_ZOOM_KEY)) {
                mMapController.setZoom(savedInstanceState.getInt(MAP_VIEW_ZOOM_KEY));
            }

            if (savedInstanceState.keySet().contains(TRACK_OVERLAY_POINTS_KEY) &&
                    mRequestingTracking) {
                mTrackOverlay = new Polyline(mResourceProxy);
                mTrackOverlay.setColor(getResources().getColor(R.color.blue));
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

    private void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mCurrentGeoPoint = new GeoPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
            mLastUpdateTime = mSimpleDateFormat.format(new Date());

            updateUI();

            mMapController.setCenter(mCurrentGeoPoint);
        }

        if (mSavedMapCenter != null) mMapController.setCenter(mSavedMapCenter);

        startLocationUpdates();
    }

    private void updateUI() {
        mMapView.getOverlays().clear();

        mMapView.getOverlays().add(new ScaleBarOverlay(this));

        if (mRequestingTracking) {
            mTrackOverlayPoints.add(mCurrentGeoPoint);
            mTrackOverlay.setPoints(mTrackOverlayPoints);
            mMapView.getOverlays().add(mTrackOverlay);
        }

        ArrayList<OverlayItem> overlayItems = new ArrayList<>(1);
        OverlayItem currentLocationItem = new OverlayItem("Current location",
                "Overlay item associated with current location", mCurrentGeoPoint);
        overlayItems.add(currentLocationItem);
        ItemizedIconOverlay<OverlayItem> currentLocationOverlay =
                new ItemizedIconOverlay<>(overlayItems, null, mResourceProxy);
        mMapView.getOverlays().add(currentLocationOverlay);

        mMapView.invalidate();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    public void showLocationButtonHandler(View view) {
        mMapController.setZoom(PREFERRED_ZOOM);
        mMapController.animateTo(mCurrentGeoPoint);
    }

    public void startTrackingButtonHandler(View view) {
        if (!mRequestingTracking) {
            mTrackOverlay = new Polyline(mResourceProxy);
            mTrackOverlay.setColor(getResources().getColor(R.color.blue));
            mTrackOverlayPoints = new ArrayList<>();

            mTrackPoints = new ArrayList<>();
            TrackPoint trackPoint = new TrackPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(), mLastUpdateTime);
            mTrackPoints.add(trackPoint);
            mGpxFilename = getResources().getString(R.string.app_name) + "_" + mLastUpdateTime;

            mRequestingTracking = true;
            setButtonsEnabledState();
        }
    }

    public void stopTrackingButtonHandler(View view) {
        if (mRequestingTracking) {
            mRequestingTracking = false;
            setButtonsEnabledState();
            if (!mTrackPoints.isEmpty()) {
                GpxCreator.saveAsGpxOnInternalStorage(this, mGpxFilename, mTrackPoints);
                GpxCreator.saveAsGpxOnExternalStorage(this, mGpxFilename, mTrackPoints);

                DialogFragment dialogFragment = GpxUploadDialogFragment.newInstance(mGpxFilename);
                dialogFragment.show(getFragmentManager(), GPX_UPLOAD_DIALOG_TAG);
            }
        }
    }

    private void setButtonsEnabledState() {
        if (mRequestingTracking) {
            mStartTrackingBtn.setEnabled(false);
            mStopTrackingBtn.setEnabled(true);
        } else {
            mStartTrackingBtn.setEnabled(true);
            mStopTrackingBtn.setEnabled(false);
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
        stopLocationUpdates();
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
        savedInstanceState.putString(LAST_UPDATE_TIME_KEY, mLastUpdateTime);

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

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
                connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mCurrentGeoPoint = new GeoPoint(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude());
        mLastUpdateTime = mSimpleDateFormat.format(new Date());
        updateUI();
        if (mRequestingTracking) {
            TrackPoint trackPoint = new TrackPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(), mLastUpdateTime);
            mTrackPoints.add(trackPoint);
        }
    }
}
