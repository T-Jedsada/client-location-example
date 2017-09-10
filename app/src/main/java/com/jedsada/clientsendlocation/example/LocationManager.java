package com.jedsada.clientsendlocation.example;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

public class LocationManager {
    private static final int LOCATION_NOTIFICATION_ID = 2000;
    private static final int LOCATION_REQUEST_CODE = 2001;

    private static boolean isLocationServicesEnabled(Context context) {
        return SmartLocation.with(context).location().state().locationServicesEnabled();
    }

    public static boolean isGpsAvailable(Context context) {
        return SmartLocation.with(context).location().state().isGpsAvailable();
    }

    public static boolean isNetworkAvailable(Context context) {
        return SmartLocation.with(context).location().state().isNetworkAvailable();
    }

    public static boolean isAnyProviderAvailable(Context context) {
        return SmartLocation.with(context).location().state().isAnyProviderAvailable();
    }

    public static void requestHighPowerLocationUpdate(Context context, LocationListener listener) {
        requestLocationUpdate(context, LocationAccuracy.HIGH, 5000, 0, listener);
    }

    public static void requestBalancePowerLocationUpdate(Context context, LocationListener listener) {
        requestLocationUpdate(context, LocationAccuracy.MEDIUM, 5000, 100, listener);

    }

    public static void requestLowPowerLocationUpdate(Context context, LocationListener listener) {
        requestLocationUpdate(context, LocationAccuracy.LOW, 10000, 500, listener);
    }

    private static void requestLocationUpdate(final Context context, LocationAccuracy locationAccuracy, long interval, float distance, final LocationListener listener) {
        if (isLocationServicesEnabled(context)) {
            LocationParams locationParams = new LocationParams.Builder()
                    .setAccuracy(locationAccuracy)
                    .setInterval(interval)
                    .setDistance(distance)
                    .build();

            SmartLocation.with(context)
                    .location()
                    .continuous()
                    .config(locationParams)
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            listener.onLocationChanged(location);
                        }
                    });
        } else {
            createLocationNotification(context);
            if (listener != null)
                listener.onLocationUnavailable();
        }
    }

    public static void removeLocationUpdate(Context context) {
        SmartLocation.with(context).location().stop();
    }

    private static void removeGeocoderUpdate(Context context) {
        SmartLocation.with(context).geocoding().stop();
    }

    public static void openLocationSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }


    public static Location getLastLocattion(Context context) {
        return SmartLocation.with(context).location().getLastLocation();
    }

    private static void createLocationNotification(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, LOCATION_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1");
        mBuilder.setSmallIcon(R.drawable.ic_settings_white)
                .setSound(alarmSound)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{0, 400})
                .setColor(ContextCompat.getColor(context, R.color.notification_color))
                .setLights(ContextCompat.getColor(context, R.color.colorPrimaryDark), 1000, 1000)
                .setContentTitle(context.getString(R.string.enable_location_service))
                .setContentText(context.getString(R.string.open_location_settings))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(LOCATION_NOTIFICATION_ID, mBuilder.build());
    }


    public interface LocationListener {
        void onLocationChanged(Location location);

        void onLocationUnavailable();
    }

    public interface AddressCallback {
        void onAddressResolved(Address country);

        void onLocationUnavailable();
    }
}
