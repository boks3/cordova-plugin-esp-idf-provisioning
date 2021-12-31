package com.boks.cordova.esp.idf.provisioning;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.espressif.provisioning.listeners.BleScanListener;

import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

class ESPIdfProvisioningPluginBleScanListener implements BleScanListener {

    private static final String TAG = ESPIdfProvisioningPluginBleScanListener.class.getSimpleName();

    CallbackContext callbackContext;
    Map<String, BleDevice> bleDevices;

    public ESPIdfProvisioningPluginBleScanListener(CallbackContext callbackContext, Map<String, BleDevice> bleDevices) {
        this.callbackContext = callbackContext;
        this.bleDevices = bleDevices;
    }

    @Override
    public void scanStartFailed() {
        // Toast.makeText(BLEProvisionLanding.this, "Please turn on Bluetooth to connect BLE device", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "scanStartFailed");
    }

    @Override
    public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {

        Log.d(TAG, "====== onPeripheralFound ===== " + device.getName());
        boolean deviceExists = false;
        String serviceUuid = "";

        if (scanResult.getScanRecord().getServiceUuids() != null && scanResult.getScanRecord().getServiceUuids().size() > 0) {
            serviceUuid = scanResult.getScanRecord().getServiceUuids().get(0).toString();
        }
        Log.d(TAG, "Add service UUID : " + serviceUuid);

        String deviceName = scanResult.getScanRecord().getDeviceName();
        if (!bleDevices.containsKey(deviceName)) {
            BleDevice bleDevice = new BleDevice();
            bleDevice.setName(deviceName);
            bleDevice.setBluetoothDevice(device);
            bleDevices.put(deviceName, bleDevice);

            JSONObject json = new JSONObject();
            try {
                json.put("name", deviceName);
                json.put("primaryServiceUuid", serviceUuid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PluginResult result = new PluginResult(PluginResult.Status.OK, json);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    @Override
    public void scanCompleted() {
        Log.d(TAG, "scanCompleted");
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onFailure(Exception e) {
        Log.e(TAG, e.getMessage());
        e.printStackTrace();
    }
}
