package com.boks.cordova.esp.idf.provisioning;

import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.listeners.ProvisionListener;

import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;

public class EspIfProvisioningPluginProvisionListener implements ProvisionListener {

    CallbackContext callbackContext;

    public EspIfProvisioningPluginProvisionListener(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void createSessionFailed(Exception e) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "CREATE_SESSION_FAILED");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void wifiConfigSent() {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "WIFI_CONFIG_SENT");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void wifiConfigFailed(Exception e) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "WIFI_CONFIG_FAILED");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void wifiConfigApplied() {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "WIFI_CONFIG_APPLIED");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void wifiConfigApplyFailed(Exception e) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "WIFI_CONFIG_APPLY_FAILED");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "PROVISIONING_FAILED_FROM_DEVICE");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void deviceProvisioningSuccess() {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "DEVICE_PROVISIONING_SUCCESS");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onProvisioningFailed(Exception e) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "PROVISIONING_FAILED");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }
}
