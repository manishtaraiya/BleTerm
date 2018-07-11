package com.manishtaraiya.bleterm;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import mehdi.sakout.fancybuttons.FancyButton;

public class ChatActivity extends AppCompatActivity implements DialogFrag.Communicator {

    private final static String TAG = ChatActivity.class.getSimpleName();
    //View params
    private EditText sendtext;
    private TextView RxText, Connection;
    private TextView mDeviceRssiView;
    //private Button readbutton;//, notifybutton;
    //private Button writebutton;
    //Intent params
    private String mDeviceAddress, mDeviceName;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    //Connection params
    private boolean mConnected = false;
    private boolean mConnecting = false;

    private boolean Log_flag = false;
    //Store all service and Characteristic
    private ArrayList<BluetoothGattCharacteristic> mReadCharacteristics, mWriteCharacteristics, mNotifyCharacteristics;
    private ArrayList<BluetoothGattService> mReadServices, mWriteServices, mNotifyServices;
    private BluetoothGattCharacteristic mNotifyCharacteristic, mReadCharacteristic, mWriteCharacteristic;
    List<BluetoothGattCharacteristic> chars = null;
    String readuuid, writeuuid, notifyuuid;

    //RSSI Params
    private Handler mTimerHandler = new Handler();
    private int RSSI_UPDATE_TIME_INTERVAL = 2000;
    private boolean mTimerEnabled = false;

    //Select hex and char
    private boolean rxhex = false, txhex = false;
    // Key for setting read /write/notify
    int selectkey = 0;

    /*public static final String READ_SERVICE = "READ_SERVICE";
    public static final String READ_CHARACTERISTIC = "READ_CHARACTERISTIC";
    public static final String WRITE_SERVICE = "WRITE_SERVICE";
    public static final String WRITE_CHARACTERISTIC = "WRITE_CHARACTERISTIC";
    public static final String NOTIFY_SERVICE = "NOTIFY_SERVICE";
    public static final String NOTIFY_CHARACTERISTIC = "NOTIFY_CHARACTERISTIC";*/


    private String service, charselect;

    boolean writecharselect = false, readcharselect = false, notifycharselect = false;
    InterstitialAd mInterstitialAd;
    AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*MobileAds.initialize(getApplicationContext(), "ca-app-pub-7701780769580238~4807134900");
        AdView mAdView = (AdView) findViewById(R.id.adViewchat);
        adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                .addTestDevice("3F662D6DA6DC09A602ED691F1BD6CDEB")
                .addTestDevice("41C24EA2F184CB0C12DF064FBA671882")
                .build();
        mAdView.loadAd(adRequest);*/


        //Request fullScreen Ads
        //requestNewInterstitial();
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);


        //Configure ActionBar

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff3498DB));//(0xff20b2aa)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.blelogo24);
        FancyButton notifybutton = new FancyButton(this);
        FancyButton readbutton = new FancyButton(this);
        FancyButton writebutton = new FancyButton(this);
        //Init View Params
        writebutton = (FancyButton) findViewById(R.id.writebutton);
        readbutton = (FancyButton) findViewById(R.id.readbutton);
        notifybutton = (FancyButton) findViewById(R.id.notifybutton);
        sendtext = (EditText) findViewById(R.id.sendtext);
        Connection = (TextView) findViewById(R.id.connect);
        RxText = (TextView) findViewById(R.id.Rxtext);
        RxText.setMovementMethod(new ScrollingMovementMethod());
        mDeviceRssiView = (TextView) findViewById(R.id.Rssi_val);


        mReadServices = new ArrayList<BluetoothGattService>();
        mReadCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
        mWriteServices = new ArrayList<BluetoothGattService>();
        mWriteCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
        mNotifyServices = new ArrayList<BluetoothGattService>();
        mNotifyCharacteristics = new ArrayList<BluetoothGattCharacteristic>();

        //Read SharedPreferences and init params
        //SharedPreferences myprefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Receive format select
        //charselect = myprefs.getString("prefcharrxselect", "100");

        rxhex = false;
        txhex = false;
        sendtext.setHint("Enter String");

        /*if (charselect.equals("1")) {
            rxhex = true;

        } else {
            rxhex = false;
            myprefs.edit().putString("prefcharrxselect", "0").apply();
        }

        //Transmit format select
        charselect = myprefs.getString("prefchartxselect", "100");
        if (charselect.equals("1")) {
            txhex = true;
            sendtext.setHint("Enter Byte Array");
        } else {
            txhex = false;
            sendtext.setHint("Enter String");
            myprefs.edit().putString("prefchartxselect", "0").apply();
        }*/


        //TxCharacteristic = "d973f2e1-b19e-11e2-9e96-0800200c9a66";
        // RxCharacteristic = "d973f2e2-b19e-11e2-9e96-0800200c9a66";


        writebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mConnected) {
                    if (!writecharselect) {
                        selectkey = 0;
                        ShowDialog("Select Write Characteristic", mWriteServices, mWriteCharacteristics);
                    } else {
                        if (txhex) {

                            String newValue = sendtext.getText().toString().toLowerCase(Locale.getDefault());
                            if (newValue.length() != 0) {
                                if (newValue.contains("0x")) {
                                    byte[] dataToWrite = parseHexStringToBytes(newValue);
                                    if (dataToWrite.length != 0) {
                                        if (mWriteCharacteristic != null) {
                                            writeDataToCharacteristic(mWriteCharacteristic, dataToWrite);
                                        }
                                    }
                                } else
                                    showMessage("Enter char array eg. 0x1234");
                            } else
                                showMessage("Text box cannot be empty");

                        } else {
                            byte[] bytes = sendtext.getText().toString().getBytes();
                            if (bytes.length != 0) {
                                if (mWriteCharacteristic != null) {
                                    writeDataToCharacteristic(mWriteCharacteristic, bytes);
                                    String text  = sendtext.getText().toString();
                                    RxText.append("\n\r" + text);
                                    if (Log_flag)
                                        writeToFile("\n\r" + text, text.length() + 2);
                                    sendtext.setText("");
                                }
                            } else
                                showMessage("Text box cannot be empty");
                        }
                    }

                } else
                    showMessage("Connect with device first");
            }

        });
        readbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mConnected) {
                    if (!readcharselect) {
                        selectkey = 1;
                        ShowDialog("Select Read Characteristic", mReadServices, mReadCharacteristics);

                    } else {
                        if (mReadCharacteristic != null)
                            readCharacteristic(mReadCharacteristic);
                    }

                } else
                    showMessage("Connect with device first");
            }

        });

        notifybutton.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                if (mConnected) {
                                                    if (!notifycharselect) {
                                                        selectkey = 2;
                                                        ShowDialog("Select Notify Characteristic", mNotifyServices, mNotifyCharacteristics);

                                                    } else
                                                        showMessage("Already selected");
                                                } else
                                                    showMessage("Connect with device first");
                                            }
                                        }

        );


        /*readbutton.setOnLongClickListener(new View.OnLongClickListener() {

                                              @Override
                                              public boolean onLongClick(View v) {
                                                  if (mConnected) {
                                                      selectkey = 1;
                                                      ShowDialog("Select Read Characteristic", mReadServices, mReadCharacteristics);
                                                  } else
                                                      showMessage("Connect with device first");
                                                  return false;

                                              }
                                          }

        );
        writebutton.setOnLongClickListener(new View.OnLongClickListener() {
                                               @Override
                                               public boolean onLongClick(View v) {
                                                   if (mConnected) {
                                                       selectkey = 0;
                                                       ShowDialog("Select Write Characteristic", mWriteServices, mWriteCharacteristics);
                                                   } else
                                                       showMessage("Connect with device first");
                                                   return false;
                                               }
                                           }

        );
        notifybutton.setOnLongClickListener(new View.OnLongClickListener()

                                            {
                                                @Override
                                                public boolean onLongClick(View v) {
                                                    if (mConnected) {
                                                        selectkey = 2;
                                                        ShowDialog("Select Notify Characteristic", mNotifyServices, mNotifyCharacteristics);
                                                    } else
                                                        showMessage("Connect with device first");
                                                    return false;
                                                }
                                            }

        );*/

    }

    /*String readchar = null, writechar = null, notifychar = null;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        readchar = savedInstanceState.getString("Readchar");
        writechar = savedInstanceState.getString("Writechar");
        notifychar = savedInstanceState.getString("Notifychar");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Readchar", mReadCharacteristic.getUuid().toString());
        outState.putString("Writechar", mWriteCharacteristic.getUuid().toString());
        outState.putString("Notifychar", mNotifyCharacteristic.getUuid().toString());
    }*/


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.tx_radio_string:
                if (checked) {
                    txhex = false;
                    sendtext.setHint("Enter String");
                }
                break;
            case R.id.tx_radio_byte:
                if (checked) {
                    txhex = true;
                    sendtext.setHint("Enter Byte Array");
                }
                break;


            case R.id.rx_radio_string:
                if (checked) {
                    rxhex = false;
                }
                break;
            case R.id.rx_radio_byte:
                if (checked) {
                    rxhex = true;
                }
                break;
        }
    }


    public boolean writeToFile(String data, int size) {

        String name = Environment.getExternalStorageDirectory() + File.separator + "BleTerm" + File.separator + "BleTerm LogFile" + ".txt";
        try {
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "BleTerm");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File file = new File(name);

            if (!file.exists()) file.createNewFile();


            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.append(data,0,size);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
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

    private boolean clearFile() {

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
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.append("");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    private void displayRSSI(final int data) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConnected)
                    mDeviceRssiView.setText("RSSI: " + data + "db");
            }
        });

    }




    private byte[]  mReceiveBuffer = new byte[10 * 1024];
    private ByteQueue mByteQueue = new ByteQueue(10 * 1024);
    public static final int UPDATE = 1;
    /**
     * Our message handler class. Implements a periodic callback.
     */
    private final Handler mHandler = new Handler() {
        /**
         * Handle the callback message. Call our enclosing class's update
         * method.
         * */
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE) {
                update();
            }
        }
    };


    private Runnable mCheckSize = new Runnable() {
        public void run() {
            update();
            mHandler.postDelayed(this, 500);
        }
    };
    public void write(byte[] buffer, int length) {
        try {
            mByteQueue.write(buffer, 0, length);

        } catch (InterruptedException e) {
        }
        //mHandler.sendMessage( mHandler.obtainMessage(UPDATE));
    }


    /**
     * Look for new input from the ptty, send it to the terminal emulator.
     */
    private void update() {
        int bytesAvailable = mByteQueue.getBytesAvailable();
        if(bytesAvailable>0) {
            int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
            try {
                int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
                //String stringRead = new String(mReceiveBuffer, 0, bytesRead);

                displayData(mReceiveBuffer,bytesRead);
                //textView.append(new String(mReceiveBuffer), 0, bytesRead);

            /*if(mRecording) {
                this.writeLog( stringRead );
            }*/
            } catch (InterruptedException e) {
            }
        }
    }
    private void displayData(final byte[] bytes , final int size) {
        if (bytes != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final StringBuilder stringBuilder = new StringBuilder(bytes.length);
                    if (rxhex) {

                        for (int i=0;i<size;i++) {
                            String hex ="0x" +String.format("%02X", bytes[i])+ "  ";
                            stringBuilder.append(hex);
                        }

                        RxText.append(stringBuilder.toString());
                        if (Log_flag)
                            writeToFile(stringBuilder.toString(),stringBuilder.toString().length());
                    } else {
                        RxText.append(new String(bytes) ,0, size);
                        if (Log_flag)
                            writeToFile(new String(bytes), size);
                    }
                }
            });
        }
    }

    public byte[] parseHexStringToBytes(final String hex) {
        byte[] nullbyte = new byte[0];
        if (hex.length() < 2)
            return nullbyte;

        String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
        //RxText.append("\n\r" + tmp);//stringBuilder.toString());

        sendtext.setText("");
        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally
        final StringBuilder stringBuilder = new StringBuilder(bytes.length);
        String part = "";
        RxText.append("\n\r");
        if (Log_flag)
            writeToFile("\n\r",2);

        for (int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i * 2, i * 2 + 2);
            bytes[i] = Long.decode(part).byteValue();

            String data = "0x" + Integer.toHexString(bytes[i]) + "  ";
            stringBuilder.append(data);
        }

        if (Log_flag)
            writeToFile(stringBuilder.toString(),stringBuilder.toString().length());

        RxText.append(stringBuilder.toString());
        return bytes;
    }

    /* request new RSSi value for the connection*/
    public void readPeriodicalyRssiValue(final boolean repeat) {

        mTimerEnabled = repeat;
        if (!mConnected || !mTimerEnabled) {
            mTimerEnabled = false;
            return;
        }
        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mTimerEnabled)
                    ReadRSSI();// request RSSI value
                // add call it once more in the future
                readPeriodicalyRssiValue(mTimerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);


    }

    public void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(), theMsg, Toast.LENGTH_SHORT);
        msg.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();
        final boolean result = connect(mDeviceAddress);
        mHandler.postDelayed(mCheckSize, 500);

       /* String filename = "myfile";
        String string = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mCheckSize);
        readPeriodicalyRssiValue(false);
        disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        readPeriodicalyRssiValue(false);
        close();
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Connection.setText(resourceId);
            }
        });
    }

    private void Init_variables() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceRssiView.setText("___db");
                ClearVariables();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);

        if (mConnected) {
            menu.findItem(R.id.connect).setVisible(false);
            menu.findItem(R.id.disconnect).setVisible(true);
            menu.findItem(R.id.connecting).setVisible(false);
        } else if (mConnecting) {
            menu.findItem(R.id.connect).setVisible(false);
            menu.findItem(R.id.disconnect).setVisible(false);
            menu.findItem(R.id.connecting).setVisible(true);

        } else {
            menu.findItem(R.id.connect).setVisible(true);
            menu.findItem(R.id.disconnect).setVisible(false);
            menu.findItem(R.id.connecting).setVisible(false);
        }
        if (Log_flag) {
            menu.findItem(R.id.log).setVisible(false);
            menu.findItem(R.id.stoplog).setVisible(true);
        } else {
            menu.findItem(R.id.log).setVisible(true);
            menu.findItem(R.id.stoplog).setVisible(false);

        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                disconnect();
                finish();
                return true;
            case R.id.connect:
                mConnecting = true;
                connect(mDeviceAddress);
                invalidateOptionsMenu();
                return true;
            case R.id.disconnect:
                disconnect();
                invalidateOptionsMenu();
                return true;
            case R.id.clear:
                RxText.setText("");
                return true;
            case R.id.log:
                Log_flag = true;
                if (CheckFileStatus()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Old log found")
                            .setMessage("want to clear log or append?")
                            .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    clearFile();
                                }
                            })
                            .setNegativeButton("Append", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    //writeToFile("\n");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                //do action on logging
                invalidateOptionsMenu();
                return true;
            case R.id.stoplog:
                Log_flag = false;
                //stop logging
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    int CHUNK_SIZE = 20;
    boolean flag = true;

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }


    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            //return  (mBluetoothGatt.connect());

            mBluetoothGatt.close();
        }


        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mchatbleCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;

        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mConnected = false;
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private final BluetoothGattCallback mchatbleCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true;
                mConnecting = false;
                Init_variables();
                readPeriodicalyRssiValue(true);
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                mBluetoothGatt.discoverServices();


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                readPeriodicalyRssiValue(false);
                Init_variables();
                mConnected = false;
                mConnecting = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();


                /*onDisconnectInit();*/
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                mConnected = false;
                mConnecting = true;
                updateConnectionState(R.string.connecting);
                invalidateOptionsMenu();

            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (BluetoothGattService service : getSupportedGattServices()) {
                        //addService(service);
                        String serviceuuid = service.getUuid().toString();
                        chars = service.getCharacteristics();
                        if (chars.size() > 0) {
                            for (BluetoothGattCharacteristic characteristic : chars) {
                                if (characteristic != null) {
                                    String charuuid = characteristic.getUuid().toString();
                                    final int charaProp = characteristic.getProperties();
                                    final int read = charaProp & BluetoothGattCharacteristic.PROPERTY_READ;
                                    final int write = charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE;
                                    final int notify = charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY;

                                    if (read > 0) {
                                        // If there is an active notification on a characteristic, clear
                                        // it first so it doesn't update the data field on the user interface.
                                           /*mReadCharacteristic = characteristic;
                                                setCharacteristicNotification(mReadCharacteristic, false);
                                                readCharacteristic(characteristic);*/
                                        mReadServices.add(service);
                                        mReadCharacteristics.add(characteristic);
                                    }
                                    if (notify > 0) {/*
                                             mNotifyCharacteristic = characteristic;
                                                setCharacteristicNotification(characteristic, true);*/

                                        mNotifyServices.add(service);
                                        mNotifyCharacteristics.add(characteristic);
                                    }

                                    if (write > 0) {/*
                                            mWriteCharacteristic = characteristic;*/
                                        //addCharacteristic(characteristic);
                                        mWriteServices.add(service);
                                        mWriteCharacteristics.add(characteristic);
                                    }

                                }
                            }
                        }
                    }
                    //handle on savedinstance here
                   /* selectkey = 0;
                    SetCharacteristic(writechar);
                    selectkey = 1;
                    SetCharacteristic(readchar);
                    selectkey = 2;
                    SetCharacteristic(notifychar);*/
                }


            });
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {

            write(characteristic.getValue(),characteristic.getValue().length);

            //displayData(characteristic.getValue());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            flag = false;
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

            write(characteristic.getValue(),characteristic.getValue().length);
            //displayData(characteristic.getValue());
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                displayRSSI(rssi);
        }
    };
   /* private void onDisconnectInit(){

        mConnected = false;
        mConnecting = false;
        updateConnectionState(R.string.disconnected);
        invalidateOptionsMenu();
        mDeviceRssiView.setText("___db");
        //for reconnect
        readuuid = mReadCharacteristic.getUuid().toString();
        writeuuid = mWriteCharacteristic.getUuid().toString();
        notifyuuid = mNotifyCharacteristic.getUuid().toString();

    }*/

    private void ClearVariables() {

        writecharselect = false;
        readcharselect = false;
        notifycharselect = false;

        mWriteCharacteristics.clear();
        mWriteServices.clear();
        mReadCharacteristics.clear();
        mReadServices.clear();
        mNotifyCharacteristics.clear();
        mNotifyServices.clear();
    }

    private void ShowDialog(String title, ArrayList<BluetoothGattService> service,
                            ArrayList<BluetoothGattCharacteristic> characteristics) {
        String[] serviceuuid = new String[service.size()];
        String[] charuuid = new String[service.size()];
        if (service.size() == characteristics.size()) {
            for (int i = 0; i < service.size(); i++) {
                serviceuuid[i] = service.get(i).getUuid().toString();
                charuuid[i] = characteristics.get(i).getUuid().toString();
            }
            final FragmentManager fm = getSupportFragmentManager();
            // FragmentManager fm = getSupportFragmentManager();
            final DialogFrag d = new DialogFrag();
            Bundle args = new Bundle();
            args.putStringArray("service", serviceuuid);
            args.putStringArray("char", charuuid);
            args.putString("title", title);
            d.setArguments(args);
            d.show(this.getFragmentManager(), "");
        }

    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        boolean success = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor rxdescriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        rxdescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(rxdescriptor);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    /* set new value for particular characteristic */
    public void writeDataToCharacteristic(final BluetoothGattCharacteristic ch, final byte[] dataToWrite) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null) return;

        for (int start = 0; start < dataToWrite.length; start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, dataToWrite.length);
            byte[] chunk = Arrays.copyOfRange(dataToWrite, start, end);
            // first set it locally....
            ch.setValue(chunk);
            mBluetoothGatt.writeCharacteristic(ch);
            //while (flag) ;

        }

    }

    public void ReadRSSI() {
        if (mBluetoothGatt == null) return;
        mBluetoothGatt.readRemoteRssi();

    }

    @Override
    public void onDialogMessage(String service, String chars) {

        //Toast.makeText(this, service+chars, Toast.LENGTH_SHORT).show();
        SetCharacteristic(chars);

    }

    private BluetoothGattCharacteristic getCharacteristic(String CharsUuid, ArrayList<BluetoothGattCharacteristic> characteristics) {
        for (int i = 0; i < characteristics.size(); i++) {
            if (characteristics.get(i).getUuid().toString().equals(CharsUuid))
                return characteristics.get(i);
        }
        return null;
    }

    private void SetCharacteristic(String chars) {
        switch (selectkey) {
            case 0:
                if (chars != null) {
                    writecharselect = true;
                    RxText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    //RxText.setGravity(Gravity.BOTTOM);
                    mWriteCharacteristic = getCharacteristic(chars, mWriteCharacteristics);
                }
                break;
            case 1:
                if (chars != null) {
                    readcharselect = true;
                    RxText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    // RxText.setGravity(Gravity.BOTTOM);
                    mReadCharacteristic = getCharacteristic(chars, mReadCharacteristics);
                    if (mReadCharacteristic != null)
                        readCharacteristic(mReadCharacteristic);
                }
                break;
            case 2:
                if (chars != null) {
                    notifycharselect = true;
                    RxText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    //RxText.setGravity(Gravity.BOTTOM);
                /*if (mNotifyCharacteristic != null)
                    setCharacteristicNotification(mNotifyCharacteristic, false);//clear previous notify*/
                    mNotifyCharacteristic = getCharacteristic(chars, mNotifyCharacteristics);
                    if (mNotifyCharacteristic != null)
                        setCharacteristicNotification(mNotifyCharacteristic, true);
                    break;
                }
        }
    }

}