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
import android.view.View;

import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private String Tag = "MainActivity";

    //目标设备的名称，可根据自己目标设备的不同去修改该名称来完成连接
    private String mTargetDeviceName = "MI";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<BluetoothGattService> mServiceList;
    private ScanCallback mScanCallback;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanCallback = new LeScanCallback();

        initBluetooth();
    }

    /**
     * enable bluetooth
     */
    private void initBluetooth() {
        //get Bluetooth service
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //get Bluetooth Adapter
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {//platform not support bluetooth
            Log.d(Tag, "Bluetooth is not support");
        }
        else{
            int status = mBluetoothAdapter.getState();
            //bluetooth is disabled
            if (status == BluetoothAdapter.STATE_OFF) {
                // enable bluetooth
                mBluetoothAdapter.enable();
            }
        }
    }

    /**
     * start Ble scan，扫描按钮的处理函数
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScanLeDevices(View view) {
        //Android 4.3以上，Android 5.0以下
        //mBluetoothAdapter.startLeScan(BluetoothAdapter.LeScanCallback)

        //Android 5.0以上，扫描的结果在mScanCallback中进行处理
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(mScanCallback);

    }

    /**
     * LE设备扫描结果返回
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class LeScanCallback  extends ScanCallback{

        /**
         * 扫描结果的回调，每次扫描到一个设备，就调用一次。
         * @param callbackType
         * @param result
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.d(Tag, "onScanResult");
            if(result != null){
                System.out.println("扫面到设备：" + result.getDevice().getName() + "  " + result.getDevice().getAddress());

                //此处，我们尝试连接MI 设备
                if (result.getDevice().getName() != null && mTargetDeviceName.equals(result.getDevice().getName())) {
                    //扫描到我们想要的设备后，立即停止扫描
                    result.getDevice().connectGatt(MainActivity.this, false, mGattCallback);
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
            }
        }
     }

    /**
     * gatt连接结果的返回
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /**
         * Callback indicating when GATT client has connected/disconnected to/from a remote GATT server
         *
         * @param gatt 返回连接建立的gatt对象
         * @param status 返回的是此次gatt操作的结果，成功了返回0
         * @param newState 每次client连接或断开连接状态变化，STATE_CONNECTED 0，STATE_CONNECTING 1,STATE_DISCONNECTED 2,STATE_DISCONNECTING 3
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(Tag, "onConnectionStateChange status:" + status + "  newState:" + newState);
            if (status == 0) {
                gatt.discoverServices();
            }
        }

        /**
         * Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated, ie new services have been discovered.
         *
         * @param gatt 返回的是本次连接的gatt对象
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(Tag, "onServicesDiscovered status" + status);
            mServiceList = gatt.getServices();
            if (mServiceList != null) {
                System.out.println(mServiceList);
                System.out.println("Services num:" + mServiceList.size());
            }

            for (BluetoothGattService service : mServiceList){
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                System.out.println("扫描到Service：" + service.getUuid());

                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    System.out.println("characteristic: " + characteristic.getUuid() );
                }
            }
        }

        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(Tag, "onCharacteristicChanged");
        }

        /**
         * Callback indicating the result of a characteristic write operation.
         *
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Tag, "onCharacteristicWrite");
        }

        /**
         *Callback reporting the result of a characteristic read operation.
         *
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Tag, "onCharacteristicRead");
        }

        /**
         * Callback indicating the result of a descriptor write operation.
         *
         * @param gatt
         * @param descriptor
         * @param status
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(Tag, "onDescriptorWrite");
        }

    };
}
