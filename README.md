# BleDemo

> 更详细的内容请查看文章[Android BLE开发之初识GATT](http://www.jianshu.com/p/29a730795294)

### 应用开发
##### 添加权限
进行蓝牙APP的开发，需要在manifest文件中加入如下的权限：
```
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```
BLUETOOTH权限使得你的APP可以使用蓝牙的对话功能，例如建连和数据的传输。
BLUETOOTH_ADMIN权限允许APP启动设备的被发现以及操作蓝牙的settings。
其他更详细的查看官网上的说明[BLE开发指南](https://developer.android.google.cn/guide/topics/connectivity/bluetooth-le.html)
##### 获得蓝牙
要想进行ble的开发首先要获得设备上的蓝牙适配器并保证蓝牙是使能的，这样才能进一步的进行ble的相关操作。

1. 获得蓝牙适配器

系统启动的时候蓝牙相关的系统服务已经开启，这时候我们首先要获得系统的蓝牙服务：
```
BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
```
通过蓝牙的系统服务得到蓝牙适配器：
```
BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
```
得到蓝牙适配器之后，我们就能进行蓝牙的相关操作，无论是经典蓝牙还是ble都可以，当然进行这些操作之前我们首先使能蓝牙。

2. 蓝牙的使能

这个当然是为了保证蓝牙是开着的，给蓝牙芯片使能。
```
if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
}
```
这段代码是调用了系统提供的开启蓝牙的对话框，点击即可使能蓝牙，其实本质也是调用BluetoothAdapter.enable()。

3. 扫描Ble设备

```
//1. Android 4.3以上，Android 5.0以下
mBluetoothAdapter.startLeScan(BluetoothAdapter.LeScanCallback LeScanCallback)
//2. Android 5.0以上，扫描的结果在mScanCallback中进行处理
mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
mBluetoothLeScanner.startScan(ScanCallback mScanCallback);
```
注意传入的callback参数是不同的。以下我们都按照5.0的API进行。

4. 得到扫描结果

在扫描结果的callback函数中将扫描到的设备的名称和地址进行打印。
```
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(result != null){
                System.out.println("扫面到设备：" + result.getDevice().getName() + "  " + result.getDevice().getAddress()); 
            }
        }
```
因为扫描本身是个耗电操作，因此扫描到目标设备后应该立即体停止扫描
```
if(mTargetDeviceName.equal(result.getDevice().getName())){
        mBluetoothLeScanner.stopScan(mScanCallback);
}
```

5. 对目标设备进行连接

```
result.getDevice().connectGatt(MainActivity.this, false, mGattCallback);
```
传入的BluetoothGattCallback对象中对连接结果做处理，以及通过GATT进行通信的绝大多数的操作都在这个对象中。
