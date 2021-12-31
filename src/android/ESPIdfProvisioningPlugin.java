// (c) 2021 Quentin Farizon
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.boks.cordova.esp.idf.provisioning;

import static com.espressif.provisioning.ESPConstants.SecurityType.SECURITY_1;
import static com.espressif.provisioning.ESPConstants.TransportType.TRANSPORT_BLE;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import org.apache.cordova.LOG;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.BleScanListener;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.WiFiScanListener;

public class ESPIdfProvisioningPlugin extends CordovaPlugin {

    private static final String TAG = ESPIdfProvisioningPlugin.class.getSimpleName();

    // actions
    private static final String SEARCH_ESP_DEVICES = "searchESPDevices";
    private static final String STOP_SEARCHING_ESP_DEVICES = "stopSearchingESPDevices";
    private static final String CONNECT_BLE_DEVICE = "connectBLEDevice";
    private static final String SCAN_NETWORKS = "scanNetworks";
    private static final String PROVISION = "provision";

    Map<String, BleDevice> bleDevices = new LinkedHashMap<String, BleDevice>();
    Map<String, ESPDevice> espDevices = new LinkedHashMap<String, ESPDevice>();
    private ESPProvisionManager provisionManager;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        provisionManager = ESPProvisionManager.getInstance(cordova.getActivity());
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "action = %s", action);

        boolean validAction = true;

        if (action.equals(SEARCH_ESP_DEVICES)) {
            String deviceNamePrefix = args.getString(0);
            bleDevices.clear();
            BleScanListener bleScanListener = new ESPIdfProvisioningPluginBleScanListener(callbackContext, bleDevices);
            provisionManager.searchBleEspDevices(deviceNamePrefix, bleScanListener);

        } else if (action.equals(STOP_SEARCHING_ESP_DEVICES)) {
            provisionManager.stopBleScan();
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } else if (action.equals(CONNECT_BLE_DEVICE)) {
            String deviceName = args.getString(0);
            String primaryDeviceUuid = args.getString(1);
            String proofOfPossession = args.getString(2);
            BleDevice bleDevice = bleDevices.get(deviceName);
            ESPDevice espDevice = provisionManager.createESPDevice(TRANSPORT_BLE, SECURITY_1);
            espDevice.connectBLEDevice(bleDevice.getBluetoothDevice(), primaryDeviceUuid);
            espDevice.setProofOfPossession(proofOfPossession);
            espDevices.put(espDevice.getDeviceName(), espDevice);

            JSONObject json = new JSONObject();
            try {
                json.put("name", espDevice.getDeviceName());
                json.put("primaryServiceUuid", espDevice.getPrimaryServiceUuid());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PluginResult result = new PluginResult(PluginResult.Status.OK, json);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } else if (action.equals(SCAN_NETWORKS)) {
            String deviceName = args.getString(0);
            ESPDevice espDevice = espDevices.get(deviceName);
            espDevice.scanNetworks(new WiFiScanListener() {
                @Override
                public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    JSONArray jsonArray = new JSONArray();

                    for (WiFiAccessPoint wifi: wifiList) {
                        JSONObject wifiJsonObject = new JSONObject();
                        try {
                            wifiJsonObject.put("ssid", wifi.getWifiName());
                            wifiJsonObject.put("rssi", wifi.getRssi());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArray.put(wifiJsonObject);
                    }

                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonArray);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }

                @Override
                public void onWiFiScanFailed(Exception e) {
                    callbackContext.error(e.toString());
                }
            });

        } else if (action.equals(PROVISION)) {
            String deviceName = args.getString(0);
            String ssid = args.getString(1);
            String passphrase = args.getString(2);
            ESPDevice espDevice = espDevices.get(deviceName);
            espDevice.provision(ssid, passphrase, new EspIfProvisioningPluginProvisionListener(callbackContext));

        } else {
            validAction = false;
        }

        return validAction;
    }
}
