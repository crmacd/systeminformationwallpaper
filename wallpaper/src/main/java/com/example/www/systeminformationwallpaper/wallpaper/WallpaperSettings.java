package com.example.www.systeminformationwallpaper.wallpaper;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class WallpaperSettings extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_TELEPHONY = 0;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 0;
    private static final int PERMISSION_REQUEST_WIFI = 0;
    private static final int PERMISSION_REQUEST_INTERNET = 0;
    private static final int PERMISSION_REQUEST_WALLPAPER = 0;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 0;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 0;
    private String strBluetooth = "";
    private String strIMEI = "";
    private String strIMSI = "";
    private String strPhoneNumber = "";
    private String strVersion = "";
    private String strWireless = "";

    /**
     * Default stock onCreate for this Activity with a minor change to the
     * Floating action button to allow for updating the strings and saving
     * the wallpaper.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        DisplayMetrics objDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(objDisplayMetrics);
        int intWidth = objDisplayMetrics.widthPixels;
        int intHeight = objDisplayMetrics.heightPixels;

        EditText objWidthText = findViewById(R.id.txtWidth);
        EditText objHeightText = findViewById(R.id.txtHeight);

        objWidthText.setText(intWidth + "");
        objHeightText.setText(intHeight + "");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStrings();
                createWallpaper();
                Snackbar.make(view, "Saving Wallpaper", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * Attempts to fill in the mac address if possible using regular system calls.
     * If it can't it will then attempt to get the information from the internet
     * connections.
     * <p>
     * Will also check for the permissions for that information if needed request
     * permissions.
     *
     * @return String of the mac address detected to have wifi access.
     */
    private String getWifiMacAddress() {
        String strDefault = "02:00:00:00:00:00";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, PERMISSION_REQUEST_WIFI);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
        }
        WifiManager objWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String strMacAddress = objWifi.getConnectionInfo().getMacAddress();
        //Log.d("getWifiMacAddress", "Default Call:" + strMacAddress);
        if (strMacAddress.equalsIgnoreCase(strDefault)) {
            try {
                List<NetworkInterface> listNet = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : listNet) {
                    if (!nif.getName().equalsIgnoreCase("wlan0")) {
                        continue;
                    }
                    byte[] bMac = nif.getHardwareAddress();
                    if (bMac != null) {
                        StringBuilder strMac = new StringBuilder();
                        for (byte bTemp : bMac) {
                            // Log.d("getWifiMacAddress", "Building MAC: " + strMac);
                            strMac.append(String.format("%02X:", bTemp));
                        }
                        if (strMac.length() > 0) {
                            strMac.deleteCharAt(strMac.length() - 1);
                        }
                        strMacAddress = strMac.toString();
                        //Log.d("getWifiMacAddress", "Found: " + strMacAddress);
                        break;
                    }

                }
            } catch (Exception exProblem) {
                Log.d("getWifiMacAddress", "Exception: " + strMacAddress);
            }
        }

        return strMacAddress;
    }

    /**
     * Calls all the functions needed to get the different string information for the
     * eventual display.
     *
     * @return True if all the strings were updated successfully, false otherwise.
     */

    public boolean updateStrings() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_TELEPHONY);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_BLUETOOTH);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SET_WALLPAPER}, PERMISSION_REQUEST_WALLPAPER);
            }
            TelephonyManager objTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //strBluetooth = "BT:" + objBluetooth.getAdapter().getAddress();
            strBluetooth = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
            if (strBluetooth == null) {
                strBluetooth = "02:00:00:00:00:00";
            }
            strBluetooth = "BT: " + strBluetooth;
            strIMEI = "IMEI: " + objTelephony.getDeviceId();
            strIMSI = objTelephony.getSubscriberId();
            if (strIMSI == null) {
                strIMSI = "No Sim";
            }
            strIMSI = "IMSI: " + strIMSI;
            strPhoneNumber = objTelephony.getLine1Number();
            if (strPhoneNumber == null) {
                strPhoneNumber = "No Number Assigned";
            }
            strPhoneNumber = "P#: " + strPhoneNumber;
            strVersion = "Version: " + Build.VERSION.RELEASE;
            strWireless = "Wifi: " + this.getWifiMacAddress();
        } catch (Exception expProblem) {
            Log.d("updateStrings", "BT: " + strBluetooth);
            Log.d("updateStrings", "IMEI: " + strIMEI);
            Log.d("updateStrings", "IMSI: " + strIMSI);
            Log.d("updateStrings", "P#: " + strPhoneNumber);
            Log.d("updateStrings", "Version: " + strVersion);
            Log.d("updateStrings", "Wifi: " + strWireless);
            return false;
        }
        return true;
    }

    /**
     * Builds a wallpaper, fills the screen a color, using the strings adds the
     * information to the wallpaper and attempts to write it to both the screen
     * and lock screen.
     *
     * @return Returns true if the wallpaper updated successfully, false otherwise
     */
    public boolean createWallpaper() {
        //Setup Wallpaper
        EditText objWidthText = findViewById(R.id.txtWidth);
        EditText objHeightText = findViewById(R.id.txtHeight);
        int intWidth = Integer.parseInt(objWidthText.getText().toString());
        int intHeight = Integer.parseInt(objHeightText.getText().toString());

        int intBeginningY = intHeight / 8;
        int intFontSize = intHeight / 32;
        Bitmap objWallpaper = Bitmap.createBitmap(intWidth, intHeight, Bitmap.Config.ARGB_8888);
        Canvas objCanvas = new Canvas(objWallpaper);
        Paint objPaint = new Paint();

        //Set Background
        objPaint.setStyle(Paint.Style.FILL);
        EditText objRGBText = findViewById(R.id.txtColor);
        try {
            objPaint.setColor(Color.parseColor(objRGBText.getText().toString()));
            objCanvas.drawPaint(objPaint);
        } catch (Exception expProblem) {
            Log.d("createWallpaper", objRGBText.getText().toString());
            objPaint.setColor(Color.parseColor("#880000"));
            objCanvas.drawPaint(objPaint);
        }

        //Set Text Foreground
        objPaint.setTextSize(intFontSize);
        CheckBox objCheckBox = findViewById(R.id.chbTextInvert);
        if (objCheckBox.isChecked()) {
            objPaint.setColor(Color.BLACK);
        } else {
            objPaint.setColor(Color.WHITE);
        }

        //Write strings to screen based on text box selections.
        float fltVersionCenter = (objWallpaper.getWidth() - objPaint.measureText(this.strVersion)) / 2;
        objCheckBox = findViewById(R.id.chbVersion);
        if (objCheckBox.isChecked()) {
            objCanvas.drawText(this.strVersion, fltVersionCenter, intBeginningY += intFontSize, objPaint);
        }
        float fltPhoneNumberCenter = (objWallpaper.getWidth() - objPaint.measureText(this.strPhoneNumber)) / 2;
        objCheckBox = findViewById(R.id.chbPhoneNumber);
        if (objCheckBox.isChecked()) {
            objCanvas.drawText(this.strPhoneNumber, fltPhoneNumberCenter, intBeginningY += intFontSize, objPaint);
        }
        float fltIMEICenter = (objWallpaper.getWidth() - objPaint.measureText(this.strIMEI)) / 2;
        objCheckBox = findViewById(R.id.chbIMEI);
        if (objCheckBox.isChecked()) {
            objCanvas.drawText(this.strIMEI, fltIMEICenter, intBeginningY += intFontSize, objPaint);
        }
        float fltIMSICenter = (objWallpaper.getWidth() - objPaint.measureText(this.strIMSI)) / 2;
        objCheckBox = findViewById(R.id.chbIMSI);
        if (objCheckBox.isChecked()) {
            objCanvas.drawText(this.strIMSI, fltIMSICenter, intBeginningY += intFontSize, objPaint);
        }
        float fltBluetoothCenter = (objWallpaper.getWidth() - objPaint.measureText(this.strBluetooth)) / 2;
        objCheckBox = findViewById(R.id.chbBluetooth);
        if (objCheckBox.isChecked()) {
            objCanvas.drawText(this.strBluetooth, fltBluetoothCenter, intBeginningY += intFontSize, objPaint);
        }
        float fltWirelessCenter = (objWallpaper.getWidth() - objPaint.measureText(this.strWireless)) / 2;
        objCheckBox = findViewById(R.id.chbWireless);
        if (objCheckBox.isChecked()) {
            objCanvas.drawText(this.strWireless, fltWirelessCenter, intBeginningY += intFontSize, objPaint);
        }

        //Write wallpaper
        WallpaperManager objWallPaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            objWallPaperManager.setBitmap(objWallpaper);
            if (Build.VERSION.SDK_INT >= 24) {
                objWallPaperManager.setBitmap(objWallpaper, null, true, WallpaperManager.FLAG_LOCK);
            }
        } catch (IOException expWallpaper) {
            Log.d("createWallpaper", "Bitmap Failed");
            return false;
        }
        return true;
    }

    /**
     * Default Android onCreateOptionsMenu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wallpaper_settings, menu);
        return true;
    }

    /**
     * Default Android onOptionsItemSelected with a minor change to check or
     * uncheck all checkboxes.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.MnuSelectAll) {
            CheckBox objCheckBox = findViewById(R.id.chbBluetooth);
            objCheckBox.setChecked(true);
            objCheckBox = findViewById(R.id.chbIMEI);
            objCheckBox.setChecked(true);
            objCheckBox = findViewById(R.id.chbIMSI);
            objCheckBox.setChecked(true);
            objCheckBox = findViewById(R.id.chbPhoneNumber);
            objCheckBox.setChecked(true);
            objCheckBox = findViewById(R.id.chbVersion);
            objCheckBox.setChecked(true);
            objCheckBox = findViewById(R.id.chbWireless);
            objCheckBox.setChecked(true);
            return true;
        }

        if (id == R.id.MnuSelectNone) {
            CheckBox objCheckBox = findViewById(R.id.chbBluetooth);
            objCheckBox.setChecked(false);
            objCheckBox = findViewById(R.id.chbIMEI);
            objCheckBox.setChecked(false);
            objCheckBox = findViewById(R.id.chbIMSI);
            objCheckBox.setChecked(false);
            objCheckBox = findViewById(R.id.chbPhoneNumber);
            objCheckBox.setChecked(false);
            objCheckBox = findViewById(R.id.chbVersion);
            objCheckBox.setChecked(false);
            objCheckBox = findViewById(R.id.chbWireless);
            objCheckBox.setChecked(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
