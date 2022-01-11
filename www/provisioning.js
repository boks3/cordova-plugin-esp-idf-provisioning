/* global cordova, module */
"use strict";

module.exports = {

  searchESPDevices: function (devicePrefix, transport, security, success, failure) {
    cordova.exec(success, failure, 'ESPIdfProvisioning', 'searchESPDevices', [devicePrefix, transport, security]);
  },

  stopSearchingESPDevices: function (success, failure) {
    cordova.exec(success, failure, 'ESPIdfProvisioning', 'stopSearchingESPDevices');
  },

  connectBLEDevice: function (espDeviceName, primaryServiceUuid, proofOfPossession, success, failure) {
    cordova.exec(success, failure, 'ESPIdfProvisioning', 'connectBLEDevice', [espDeviceName, primaryServiceUuid, proofOfPossession]);
  },

  disconnectBLEDevice: function (espDeviceName, success, failure) {
    cordova.exec(success, failure, 'ESPIdfProvisioning', 'disconnectBLEDevice', [espDeviceName]);
  },

  scanNetworks: function (espDeviceName, success, failure) {
    cordova.exec(success, failure, 'ESPIdfProvisioning', 'scanNetworks', [espDeviceName]);
  },

  provision: function (espDeviceName, ssid, passphrase, success, failure) {
    cordova.exec(success, failure, 'ESPIdfProvisioning', 'provision', [espDeviceName, ssid, passphrase]);
  }
}
