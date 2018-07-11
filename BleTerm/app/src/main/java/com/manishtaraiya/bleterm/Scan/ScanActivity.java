package com.manishtaraiya.bleterm.Scan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.manishtaraiya.bleterm.ChatActivity;
import com.manishtaraiya.bleterm.Filter.FilterActivity;
import com.manishtaraiya.bleterm.R;
import com.manishtaraiya.bleterm.Scan_CustomAdapter;
import com.manishtaraiya.bleterm.Setting;
import com.wang.avi.AVLoadingIndicatorView;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import mehdi.sakout.fancybuttons.FancyButton;


public class ScanActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CustomListAdapter.customButtonListener {

    /*ArrayList<String> name;
    ArrayList<String> address;
    ArrayList<Integer> device_rssi;
    Scan_CustomAdapter customAdapter;*/
    ListView devicelist;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_GPS = 2;
    private static final int REQUEST_ENABLE_STORAGE = 3;
    private static final int REQUEST_ALL = 4;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private Handler mHandler;
    // Stops scanning after 60 seconds.
    private static long SCAN_PERIOD = 10000;


    //AdRequest.Builder.addTestDevice("41C24EA2F184CB0C12DF064FBA671882")

    String[] Permission = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    //private AVLoadingIndicatorView search;
    //private TextView searchtext;
    NativeExpressAdView nativeExpressAdView;

    FancyButton fancyButton;
    TextView filter_holder;
    boolean ad_loaded = false;
    Timer updateAdsTimer, scanTimer;
    AdRequest adRequest;
    CustomListAdapter adapter;
    SharedPreferences sharedpreferences;
    ArrayList<SearchElement> device_list = new ArrayList<SearchElement>();
    int progress_result = 0;
    String name_filter, mac;


    String[] name_array  = new String[20];/*= {
            "Android", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X",
            "Android", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X",
            "Android", "IPhone", "WindowsMobile", "Blackberry"
    };*/

    String[] mac_array = {
            "11:22:33:44:55:66", "12:23:34:44:55:66", "13:24:35:44:55:66", "14:25:36:44:55:66",
            "11:22:33:44:55:66", "12:23:34:44:55:66", "13:24:35:44:55:66", "14:25:36:44:55:66",
            "11:22:33:44:55:66", "12:23:34:44:55:66", "13:24:35:44:55:66", "14:25:36:44:55:66",
            "11:22:33:44:55:66", "12:23:34:44:55:66", "13:24:35:44:55:66", "14:25:36:44:55:66",
            "11:22:33:44:55:66", "12:23:34:44:55:66", "13:24:35:44:55:66", "14:25:36:44:55:66"
    };
    int[] rssi_val_array = {
            -100, -70, -60, -40,
            -100, -70, -60, -40,
            -100, -70, -60, -40,
            -100, -70, -60, -40,
            -100, -70, -60, -40,
    };

    String raw_data="0x1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Set navigation drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Set screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //
        View header = View.inflate(this, R.layout.listview_header_layout, null);
        filter_holder = (TextView) header.findViewById(R.id.filter_holder_text);
        fancyButton = (FancyButton) header.findViewById(R.id.filter_clear_button);
        devicelist = (ListView) findViewById(R.id.devicelist);
        devicelist.addHeaderView(header, null, false);
        /*for (int i = 0; i < name_array.length; i++) {
            SearchElement wp = new SearchElement(name_array[i], mac_array[i], rssi_val_array[i],raw_data);

            // Binds all strings into an array
            device_list.add(wp);
        }*/
        adapter = new CustomListAdapter(this, device_list);

        adapter.setCustomButtonListner(this);
        devicelist.setAdapter(adapter);
        //devicelist.setEmptyView(findViewById(R.id.empty));
      /*  searchtext = (TextView) findViewById(R.id.ble_search_txt);

        String indicator = getIntent().getStringExtra("indicator");
        search = (AVLoadingIndicatorView) findViewById(R.id.search_anim);
        search.setIndicator(indicator);*/


        fancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!filter_holder.getText().toString().contains("No filter applied")) {
                    SharedPreferences.Editor editor = getSharedPreferences(FilterActivity.MyPREFERENCES, MODE_PRIVATE).edit();
                    editor.putString(FilterActivity.Name, "");
                    editor.putString(FilterActivity.Mac, "");
                    editor.putInt(FilterActivity.Rssi, 0);
                    editor.apply();

                    name_filter = "";
                    mac = "";
                    progress_result = 0;
                    filter_holder.setText("No filter applied");

                    adapter.filter("", "", 0);
                }

            }
        });



        /*name = new ArrayList<String>();
        address = new ArrayList<String>();
        device_rssi = new ArrayList<Integer>();



        customAdapter = new Scan_CustomAdapter(this, name, address, device_rssi);
        devicelist.setAdapter(customAdapter);
        devicelist.setEmptyView(findViewById(R.id.empty));
        searchtext = (TextView) findViewById(R.id.ble_search_txt);
*/


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7701780769580238~4807134900");//"ca-app-pub-7701780769580238/4667534109");
        //AdView mAdView = (AdView) findViewById(R.id.adView);
        nativeExpressAdView = (NativeExpressAdView) findViewById(R.id.adView_native);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adRequestBuilder.addTestDevice("41C24EA2F184CB0C12DF064FBA671882");
        adRequestBuilder.addTestDevice("3F662D6DA6DC09A602ED691F1BD6CDEB");
        adRequest = adRequestBuilder.build();
        nativeExpressAdView.loadAd(adRequest);

        nativeExpressAdView.setVisibility(View.GONE);
        initUpdateAdsTimer();

        nativeExpressAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                nativeExpressAdView.setVisibility(View.VISIBLE);
                ad_loaded = true;

            }
        });


        if (!hasPermissions(this, Permission)) {
            ActivityCompat.requestPermissions(this, Permission, REQUEST_ALL);
        }

        /*search.smoothToHide();
        searchtext.setText("");*/

        /*devicelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getBaseContext(), name.get(position) + " " + address.get(position), Toast.LENGTH_SHORT).show();

                final Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRAS_DEVICE_NAME, name.get(position));
                intent.putExtra(ChatActivity.EXTRAS_DEVICE_ADDRESS, address.get(position));
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                startActivity(intent);
            }
        });*/


        getSupportActionBar().setTitle("Scan Device");
        //getActionBar().setBackgroundDrawable(new ColorDrawable(0xff20b2aa));
        mHandler = new Handler();
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initUpdateAdsTimer() {
        updateAdsTimer = new Timer();
        updateAdsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!ad_loaded)
                            nativeExpressAdView.loadAd(adRequest);
                    }
                });
            }
        }, 10 * 1000, 10 * 1000);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.filter) {
            Intent intent = new Intent(this, FilterActivity.class);
            startActivity(intent);
        } /*else if (id == R.id.setting) {

            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
            Toast.makeText(this, "frequency", Toast.LENGTH_SHORT).show();
        } */else if (id == R.id.log) {
            if (CheckFileStatus())
                SendLogFile();
            else
                showMessage("No Log file available!");

            //Toast.makeText(this,"Share log",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.app_share) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "https://play.google.com/store/apps/details?id=com.manishtaraiya.bleterm";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Take a look at \"BleTerm Scanner & Terminal\"");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));


            //Toast.makeText(this, "app share", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.rate_app) {

            Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    /*Intent.FLAG_ACTIVITY_NEW_DOCUMENT |*/
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
            }

        }
        /*else if (id == R.id.help) {


            //Toast.makeText(this, "help", Toast.LENGTH_SHORT).show();
        } */else if (id == R.id.contact) {
            //Toast.makeText(this, "feedback", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.parse("mailto:manishbleterm@gmail.com?subject=" + "Feedback - From Bleterm App" + "&body=" + "");
            intent.setData(data);
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(), theMsg, Toast.LENGTH_SHORT);
        msg.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();
        // Initialize the map with both permissions
        perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (requestCode) {

            case REQUEST_ALL: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    }
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since storage access has not been granted, this app will not be able to do logging.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {

                                finish();

                            }

                        });
                        builder.show();
                    }
                }

            }
            return;
           /* case REQUEST_ENABLE_GPS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "coarse location permission granted");
                } else {

                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {

                            finish();

                        }

                    });
                    builder.show();
                }
                return;
            }
            case REQUEST_ENABLE_STORAGE:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "coarse location permission granted");
                } else {

                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since storage access has not been granted, this app will not be able to do logging.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }*/
        }
    }


    @Override
    protected void onResume() {
        super.onResume();


        /*String frequency;
        SharedPreferences myprefs = PreferenceManager.getDefaultSharedPreferences(this);
        frequency = myprefs.getString("prefScanFrequency", "100");
        if (frequency.equals("10"))
            SCAN_PERIOD = 10 * 1000;
        else if (frequency.equals("20"))
            SCAN_PERIOD = 20 * 1000;
        else if (frequency.equals("30"))
            SCAN_PERIOD = 30 * 1000;
        else if (frequency.equals("60"))
            SCAN_PERIOD = 60 * 1000;
        else {
            SCAN_PERIOD = 60 * 1000;
            SharedPreferences mypref = PreferenceManager.getDefaultSharedPreferences(this);
            mypref.edit().putString("prefScanFrequency", "60").apply();
        }*/

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            StartScanTimer();
            //scanLeDevice(true);
        }


        sharedpreferences = this.getSharedPreferences(FilterActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        name_filter = sharedpreferences.getString(FilterActivity.Name, null);
        mac = sharedpreferences.getString(FilterActivity.Mac, null);
        progress_result = sharedpreferences.getInt(FilterActivity.Rssi, 0);

        ApplyFilter();

    }

    boolean start_scan = true;

    private void StartScanTimer() {

        start_scan = true;
        scanTimer = new Timer();
        scanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (start_scan)
                            scanLeDevice(true);
                    }
                });
            }
        }, 0, 7 * 1000);

    }

    private void ApplyFilter() {


        if (name_filter == null || mac == null)
            filter_holder.setText("No filter applied");
        else if ((name_filter.equals("") && mac.equals("") && progress_result == 0))
            filter_holder.setText("No filter applied");
        else {
            String l_name = name_filter.toLowerCase(Locale.getDefault());
            String l_mac = mac.toLowerCase(Locale.getDefault());
            adapter.filter(l_name, l_mac, progress_result - 100);


            if (name_filter.equals(""))
                l_name = "NULL";
            else
                l_name = name_filter;
            if (mac.equals(""))
                l_mac = "NULL";
            else
                l_mac = mac;
            filter_holder.setText(l_name + "," + l_mac + "," + (progress_result - 100) + "dBm");


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanTimer.purge();
            start_scan =false;
            scanLeDevice(false);
        }
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //search.smoothToHide();
                    //searchtext.setText("No BLE Device found");
                    //invalidateOptionsMenu();
                }
            }, 5000/*SCAN_PERIOD*/);

            if(!mScanning) {
                mScanning = true;
                invalidateOptionsMenu();
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //searchtext.setText("Searching for BLE device");
            //search.smoothToShow();

        } else {
            mScanning = false;
            //searchtext.setText("No BLE Device found");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            //search.smoothToHide();
            invalidateOptionsMenu();
        }

    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*if (!address.contains(device.getAddress())) {
                        name.add(device.getName());
                        address.add(device.getAddress());
                        device_rssi.add(rssi);
                    }


                    customAdapter.update(name, address, device_rssi);*/
                    final StringBuilder stringBuilder = new StringBuilder(scanRecord.length);
                    stringBuilder.append("0x");
                    for (byte byteChar : scanRecord) {
                        String hex =String.format("%02X", byteChar);
                        stringBuilder.append(hex);
                    }


                    boolean device_added = false;
                    SearchElement wp = new SearchElement(device.getName(), device.getAddress(), rssi,stringBuilder.toString().trim());

                    adapter.filter("", "", 0);
                    for (int i = 0; i < device_list.size(); i++) {
                        if (device_list.get(i).getMac_address().equals(device.getAddress())) {
                            device_list.remove(i);
                            device_list.add(i, wp);
                            device_added = true;
                        }
                    }
                    if (!device_added)
                        device_list.add(wp);

                    adapter.update(device_list);
                    ApplyFilter();

                }
            });
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_scan_menu, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                StartScanTimer();
                //scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanTimer.purge();
                start_scan = false;
                scanLeDevice(false);
                break;
            /*case R.id.setting:
                Intent intent = new Intent(this, Setting.class);
                startActivity(intent);
                break;
            case R.id.share_log:
                if (CheckFileStatus()) {
                    SendLogFile();
                } else {
                    showMessage("No Log file available!");
                }
                break;*/
        }
        return true;
    }


    private void SendLogFile() {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("*/*");
        //emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"me@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "BleTerm Log file " + currentDateTimeString);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
                File.separator + "BleTerm" + File.separator + "BleTerm LogFile" + ".txt")));
        startActivity(Intent.createChooser(emailIntent, "Send log file from..."));
    }

    private boolean CheckFileStatus() {
        try {
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "BleTerm");
            if (!folder.exists()) folder.mkdir();
            File file = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "BleTerm" + File.separator + "BleTerm LogFile" + ".txt");

            if (!file.exists()) {
                file.createNewFile();
                return false;
            }
            if (file.length() > 0)
                return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void onButtonClickListner(int position, String name, String mac, int rssi) {
       /* Toast.makeText(ScanActivity.this, " " + name + " " + mac + " " + rssi,
                Toast.LENGTH_SHORT).show();*/


        final Intent intent = new Intent(getBaseContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRAS_DEVICE_NAME, name);
        intent.putExtra(ChatActivity.EXTRAS_DEVICE_ADDRESS, mac);
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
        /*int size = name_list.size();
        if(size<19){

            name_list.add(name_array[size]);
            mac_list.add(mac_array[size]);
            rssi_list.add(rssi_val_array[size]);
            adapter.update(name_list,mac_list,rssi_list);

        }*/

    }

    @Override
    public void onRawDataClickListner(int position, String raw_data) {
       // Toast.makeText(ScanActivity.this, "Raw data at " + position, Toast.LENGTH_SHORT).show();

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.raw_dialog);
        dialog.setTitle("Custom Dialog");
        TextView text = (TextView) dialog.findViewById(R.id.textDialog);
        text.setText(raw_data);
        dialog.show();
        FancyButton declineButton = (FancyButton) dialog.findViewById(R.id.declineButton);
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });
    }
}
