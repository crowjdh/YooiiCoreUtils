package com.yooiistudios.coreutils.location;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.yooiistudios.coreutils.BuildConfig;

/**
 * Copyright © Yooii Studios. All rights reserved.<br/>
 * Created by Yun on 15. 1. 25..
 *
 *  Usage 1 - Persistent tracking through activity
 *
 *    1.
 *    @Override
 *    protected void onStart() {
 *       super.onStart();
 *       LocationModule.getInstance(context).startTracking(getSupportFragmentManager(), listener);
 *    }
 *
 *    2.
 *    @Override
 *    protected void onStop() {
 *       LocationModule.getInstance(context).stopTracking();
 *       super.onStop();
 *    }
 *
 *    3.
 *    @Override
 *    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *    boolean handledByLocationModule =
 *           LocationModule.getInstance(context).handleActivityResult(requestCode, responseCode, intent);
 *       if (!handledByLocationModule) {
 *           // normal onActivityResult
 *       }
 *    }
 *
 *
 *   Usage 2 - One shot location fetch
 *    - 1. Request LocationModule for current location.
 *    LocationModule.getInstance(context).requestCurrentLocation(getSupportFragmentManager(), listener);
 *
 *    - 2. cancel request for current location.
 *    LocationModule.getInstance(context).cancelCurrentLocationRequest();
 *
 *    - 3. Same as Usage 1.2 with replacing stopTracking() method to cancelCurrentLocationRequest()
 *    - 4. Usage 1.3
 */
public class LocationModule implements LocationListener
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {
    public interface OnLocationEventListener {
        Activity onResolutionRequired();
        void onLocationChanged(LatLng latLng);
    }

    private enum LocationUpdatePolicy {
        TRACK,
        ONE_SHOT,
        NEVER
    }

    private static final String TAG_FRAGMENT_RESOLVING_ERROR_STATE
            = "tag_fragment_resolving_error_state";
    private static final int RC_RESOLUTION = 1001;
    private static final int SECOND_IN_MILLI = 1000;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5 * SECOND_IN_MILLI;
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = SECOND_IN_MILLI;
    private static final String TAG = LocationModule.class.getSimpleName();

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;

    private FragmentManager mFragmentManager;
    @Nullable
    private OnLocationEventListener mListener;
    private LocationUpdatePolicy mLocationUpdatePolicy;

    private volatile static LocationModule instance;

    public static LocationModule getInstance(Context context) {
        if (instance == null) {
            synchronized (LocationModule.class) {
                if (instance == null) {
                    instance = new LocationModule(context);
                }
            }
        }
        return instance;
    }

    public LocationModule(Context context) {
        initGoogleApiClient(context);
        initLocationRequest();
    }

    private void initGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void initLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
    }

    public void requestCurrentLocation(FragmentManager fm, OnLocationEventListener listener) {
        startTracking(fm, listener);
        mLocationUpdatePolicy = LocationUpdatePolicy.ONE_SHOT;
    }

    public void cancelCurrentLocationRequest() {
        stopTracking();
    }

    public void startTracking(FragmentManager fm, OnLocationEventListener listener) {
        if (!isResolvingError(fm)) {
            mFragmentManager = fm;
            mListener = listener;
            mLocationUpdatePolicy = LocationUpdatePolicy.TRACK;
            mGoogleApiClient.connect();
        }
    }

    /**
     * call this method before super.onStop();
     */
    public void stopTracking() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        mFragmentManager = null;
        mListener = null;
        mLocationUpdatePolicy = LocationUpdatePolicy.TRACK;
    }

    public Location getCurrentLocation() throws LocationException {
        if (mCurrentLocation != null) {
            return mCurrentLocation;
        } else {
            throw new LocationException();
        }
    }

    public LatLng getCurrentLatLng() throws LocationException {
        LatLng curLatLng;
        try {
            Location currentLocation = getCurrentLocation();
            curLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        } catch(LocationException e) {
            if (BuildConfig.DEBUG_MODE) {
                // 에뮬레이터에서 GoogleApiClient 의 onConnect 콜백이 불리지 않기 때문에
                // 디버그 모드에서만 fallback 으로 Dalvik 의 위치를 넣어둠
                curLatLng = new LatLng(65.970738, -18.532690);
            } else {
                throw e;
            }
        }
        return curLatLng;
    }

    public static float distanceInKmBetween(LatLng currentLatLng, LatLng sendLatLng) {
        float distanceMeter = distanceBetween(currentLatLng, sendLatLng);
        return distanceMeter / 1000;
    }

    public static float distanceInMilesBetween(LatLng currentLatLng, LatLng sendLatLng) {
        float distanceMeter = distanceBetween(currentLatLng, sendLatLng);
        return distanceMeter * 0.000621371f;
    }

    public static float distanceBetween(LatLng sourceLatLng, LatLng destinationLatLng) {
        Location locationA = new Location("point A");
        locationA.setLatitude(sourceLatLng.latitude);
        locationA.setLongitude(sourceLatLng.longitude);

        Location locationB = new Location("point B");
        locationB.setLatitude(destinationLatLng.latitude);
        locationB.setLongitude(destinationLatLng.longitude);

        return locationA.distanceTo(locationB);
    }

    public boolean handleActivityResult(int requestCode, int responseCode, Intent intent) {
        boolean handled = false;
        if (requestCode == RC_RESOLUTION) {
            handled = true;

            clearResolvingErrorState();
            if (responseCode == Activity.RESULT_OK) {
                boolean shouldConnect =
                        !mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected();
                if (shouldConnect) {
                    mGoogleApiClient.connect();
                }
            }
        }

        return handled;
    }

    private void showErrorDialog(int errorCode) {
        if (mFragmentManager != null) {
            ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ErrorDialogFragment.DIALOG_ERROR, errorCode);
            dialogFragment.setArguments(args);
            dialogFragment.show(mFragmentManager, "error_dialog");
        }
    }

    private static boolean isResolvingError(FragmentManager fm) {
        boolean isResolving;
        try {
            isResolving = getResolvingErrorStateFragment(fm).isResolvingError();
        } catch (IllegalArgumentException ignored) {
            isResolving = false;
        }
        return isResolving;
    }

    private void saveResolvingErrorState() {
        saveResolvingErrorState(mFragmentManager);
    }

    private static void saveResolvingErrorState(FragmentManager fm) {
        try {
            getResolvingErrorStateFragment(fm).saveResolvingErrorState();
        } catch (IllegalArgumentException ignored) { }
    }

    private void clearResolvingErrorState() {
        clearResolvingErrorState(mFragmentManager);
    }

    private static void clearResolvingErrorState(FragmentManager fm) {
        try {
            getResolvingErrorStateFragment(fm).clearResolvingErrorState();
        } catch (IllegalArgumentException ignored) { }
    }

    @NonNull
    private static ResolvingErrorStateFragment getResolvingErrorStateFragment(FragmentManager fm)
            throws IllegalArgumentException {
        checkFragmentManagerNotNull(fm);

        ResolvingErrorStateFragment fragment =
                (ResolvingErrorStateFragment) fm.findFragmentByTag(TAG_FRAGMENT_RESOLVING_ERROR_STATE);

        if (fragment == null) {
            fragment = new ResolvingErrorStateFragment();
            fm.beginTransaction().add(fragment, TAG_FRAGMENT_RESOLVING_ERROR_STATE).commit();
        }
        return fragment;
    }

    private void notifyCurrentLocation() {
        if (!mLocationUpdatePolicy.equals(LocationUpdatePolicy.NEVER) && mListener != null) {
            try {
                mListener.onLocationChanged(getCurrentLatLng());
                if (mLocationUpdatePolicy.equals(LocationUpdatePolicy.ONE_SHOT)) {
                    mLocationUpdatePolicy = LocationUpdatePolicy.NEVER;
                }
            } catch (LocationException ignored) { }
        }
    }

    private static void checkFragmentManagerNotNull(FragmentManager fm) {
        if (fm == null) {
            throw new IllegalArgumentException("FragmentManager MUST NOT be null!!");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 연결이 되더라도 옵션에 따라(gps, wifi 등) 내 위치를 가져오지 못하는 경우를 대비해 체크
        LocationSettingsRequest.Builder locationSettingsRequestBuilder =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, locationSettingsRequestBuilder.build());
        result.setResultCallback(this);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        // 옵션이 불충분하더라도 최근에 가져온 위치가 있다면 우선 캐싱
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mCurrentLocation != null) {
            notifyCurrentLocation();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (isResolvingError(mFragmentManager)) {
            // ignored
        } else if (connectionResult.hasResolution()) {
            try {
                Activity activity;
                if (mListener != null && (activity = mListener.onResolutionRequired()) != null) {
                    connectionResult.startResolutionForResult(activity, RC_RESOLUTION);
                    saveResolvingErrorState();
                }
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
            saveResolvingErrorState();
        }
    }

    @Override public void onConnectionSuspended(int i) { }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        notifyCurrentLocation();
    }

    // LocationSettingsResult 의 callback
    @Override
    public void onResult(LocationSettingsResult result) {
        Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Activity activity;
                if (mListener != null && (activity = mListener.onResolutionRequired()) != null) {
                    try {
                        saveResolvingErrorState();
                        status.startResolutionForResult(activity, RC_RESOLUTION);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public static class LocationException extends Exception {
        public LocationException() {
            this("CANNOT FIND current location!!");
        }

        public LocationException(String detailMessage) {
            super(detailMessage);
        }

        public LocationException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public LocationException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class ResolvingErrorStateFragment extends Fragment {
        private boolean mIsResolvingError;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public boolean isResolvingError() {
            return mIsResolvingError;
        }

        public void saveResolvingErrorState() {
            mIsResolvingError = true;
        }

        public void clearResolvingErrorState() {
            mIsResolvingError = false;
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private static final String DIALOG_ERROR = "dialog_error";

        public ErrorDialogFragment() { }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), RC_RESOLUTION);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            clearResolvingErrorState(getFragmentManager());
        }
    }
}
