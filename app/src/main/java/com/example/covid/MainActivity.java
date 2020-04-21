package com.example.covid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity  implements BeaconConsumer {
    ChipNavigationBar bottomNav;
    FragmentManager fragmentManager;
    public static final String CHANNEL_ID = "exampleForegroundServiceChannel";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    public static final String sysIdKey = "sharedPrefKey";
    Set<String> remote_beacon_ids_set = new HashSet<String>();
    private BeaconManager beaconManager;
    ListView nearByBeaconLv = null;
    ArrayAdapter adapter = null;
    TextView myIdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //transparent statusBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //hide actionBar
        try
        {
            Objects.requireNonNull(this.getSupportActionBar()).hide();
        }
        catch (NullPointerException ignored){}
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        //default fragment
        setFragment(new HomeFragment());

        verifyBluetooth();

        myIdView = findViewById(R.id.myid);
        nearByBeaconLv = findViewById((R.id.remoteBeacons));

//        checkPermission();
        String uid = getsystemID();
        remote_beacon_ids_set.add("My ID : "+ uid);
        myIdView.setText(uid);
        startserviceBroadcast(uid);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                Fragment selectedFragment = null;

                switch (id) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_favorites:
                        selectedFragment = new CheckFragment();
                        break;
                    case R.id.nav_search:
                        selectedFragment = new LocationFragment();
                        break;
                }

                if (selectedFragment!=null){
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

            }
        });
    }

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();
    }

    private String getsystemID(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String systemID = sharedPref.getString(sysIdKey, null);
        if (systemID == null){
            systemID = Utils.generateUidNamespace();
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(sysIdKey, systemID);
        editor.commit();
        return systemID;
    }

    private void startserviceBroadcast(String uid) {
        Intent serviceIntent = new Intent(this, bgService.class);
        serviceIntent.putExtra(sysIdKey, uid);

//        application.enableMonitoring();
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App application = ((App) this.getApplicationContext());
        beaconManager.bind(this);
        application.setMonitoringActivity(this);
        updateLog(application.getLog());
    }
//
//    private boolean checkPermission() {
//
//        if (Build.VERSION.SDK_INT >= Build.VESRSION_CODES.M) {
//            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                        builder.setTitle("This app needs background location access");
//                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
//                        builder.setPositiveButton(android.R.string.ok, null);
//                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                            @TargetApi(23)
//                            @Override
//                            public void onDismiss(DialogInterface dialog) {
//                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                                        PERMISSION_REQUEST_BACKGROUND_LOCATION);
//                            }
//                        });
//                        builder.show();
//                    }
//                    else {
//
//                    }
//
//                }
//            } else {
//                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                            PERMISSION_REQUEST_FINE_LOCATION);
//                }
//                else {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("Functionality limited");
//                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
//                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//
//                        }
//                    });
//                    builder.show();
//                }
//
//            }
//        }
//        return false;
//    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();

        }

    }

    public void updateLog(final String log) {
        Log.i("testing_log_adapater",log);


    }

    @Override
    public void onBeaconServiceConnect() {

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.d("test_ble", "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                    Beacon firstBeacon = beacons.iterator().next();
                    String beaconLog = "The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.";
                    Log.d("test_ble_Id1",String.valueOf(firstBeacon.getId1()));
                    String beaconID = String.valueOf(firstBeacon.getId1());
                    remote_beacon_ids_set.add( "Remote beacon : "+ beaconID.substring(2,beaconID.length()) );

//                    logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                    Log.d("test_ble",beaconLog);

                    String[] nearByBeaconsArray = remote_beacon_ids_set.toArray(new String[0]);
                    Arrays.sort( nearByBeaconsArray );
                    Log.d("totalnearbybeacons",""+ String.valueOf(nearByBeaconsArray.length-1));

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_2, android.R.id.text1, nearByBeaconsArray);

                    nearByBeaconLv.setAdapter(adapter);
                }
            }

        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {   }
    }

}
