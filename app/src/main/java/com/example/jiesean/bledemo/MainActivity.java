package com.example.jiesean.bledemo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

/**
 * This program is designed for reading ANCS test.
 *
 */
public class MainActivity extends AppCompatActivity {

    private String Tag = "MainActivity";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;

    private BluetoothDevice mBondDevice;

    private BluetoothGattService ancs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intentFilter=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        BondReceiver mBondReceiver = new BondReceiver();
        this.registerReceiver(mBondReceiver, intentFilter);

        mScanCallback = new LeScanCallback();

        boolean bluetoothState = initBluetoothAdapter();

//        if (bluetoothState == true) {
//            startScanLeDevices();
//        }


    }

    /**
     * init bluetooth
     */
    private boolean initBluetoothAdapter() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Log.d(Tag, "BluetoothAdapter is null");
            mBluetoothAdapter.enable();
        }
//        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
////            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
////            startActivityForResult(enableBtIntent, 1);
//
//        }
        return true;
    }

    /**
     * start Ble scan
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScanLeDevices() {
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(mScanCallback);
       // mBluetoothAdapter.startLeScan(mLeScanCallback);
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            if (bluetoothDevice != null) {
                mBondDevice = bluetoothDevice;
                boolean isBond = bluetoothDevice.createBond();
            }
        }
    };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (gatt != null) {
                boolean isDiscovering = gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(Tag, "onServicesDiscovered");
            ancs = gatt.getService(UUID.fromString("7905F431-B5CE-4E99-A40F-4B1E122D00D0"));
            if (ancs != null) {
                BluetoothGattCharacteristic notificationSourceChar = ancs.getCharacteristic(UUID.fromString("9FBF120D-6301-42D9-8C58-25E699A21DBD"));
                gatt.setCharacteristicNotification(notificationSourceChar, true);
                BluetoothGattDescriptor config = notificationSourceChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(config);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(Tag, "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(Tag, "onDescriptorWrite");
        }
    };

    public class BondReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mBondDevice.connectGatt(getBaseContext(), false, mGattCallback);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class LeScanCallback  extends ScanCallback{
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(Tag, "onScanResult");
        }
    }
}
