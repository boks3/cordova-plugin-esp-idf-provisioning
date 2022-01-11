//
//  ESPIdfProvisioningPlugin.swift
//  Boks preprod
//
//  Created by Jacques Lemarchand on 07/01/2022.
//

import Foundation
import ESPProvision

@objc(ESPIdfProvisioning)
class ESPIdfProvisioning : CDVPlugin, ESPDeviceConnectionDelegate {
    var espDevices = [String : ESPDevice]()
    var proofOfPossession: String = ""
    
    
    @objc func searchESPDevices (_ command: CDVInvokedUrlCommand) {
        let deviceNamePrefix = command.arguments[0] as! String
        ESPProvisionManager.shared.searchESPDevices(devicePrefix: deviceNamePrefix, transport: .ble, security: .secure) { bleDevices, error in
            self.espDevices = [String : ESPDevice]()
            if let bleDevices = bleDevices {
                for espDevice in bleDevices {
                    self.espDevices[espDevice.name] = espDevice
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
                        "name": espDevice.name,
                        "primaryServiceUuid": "abc"
                    ])
                    pluginResult?.setKeepCallbackAs(true)
                    self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
                }
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "OK")
                self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
            }
           
        }
    }
    
    @objc func stopSearchingESPDevices (_ command: CDVInvokedUrlCommand){
        
    }
    
    @objc func connectBLEDevice (_ command: CDVInvokedUrlCommand){
        let deviceName = command.arguments[0] as! String
        self.proofOfPossession = command.arguments[2] as! String
        if let espDevice = self.espDevices[deviceName] {
            espDevice.connect(delegate: self) { status in
                switch status {
                    case .connected:
                        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
                            "name": espDevice.name,
                            "primaryServiceUuid": "abc"
                        ])
                        self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
                    case let .failedToConnect(error):
                        NSLog("error %s", error.description)
                        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                        self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
                    default:
                        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                        self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
                        
                }
            }
        }

    }

    @objc func disconnectBLEDevice (_ command: CDVInvokedUrlCommand){
        let deviceName = command.arguments[0] as! String
        if let espDevice = self.espDevices[deviceName] {
            espDevice.disconnect()
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
        } else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Device not found")
            self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
        }
    }

    @objc func scanNetworks (_ command: CDVInvokedUrlCommand) {
        let deviceName = command.arguments[0] as! String
        if let espDevice = self.espDevices[deviceName] {
            espDevice.scanWifiList { wifiList, _ in
                if let list = wifiList {
                    let wifiDetailList = list.sorted { $0.rssi > $1.rssi }
                    let wifiListJSON = wifiDetailList.map { ["ssid": $0.ssid, "rssi": $0.rssi ] }
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: wifiListJSON)
                    self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
                }
            }
        }
    }
    
    @objc func provision (_ command: CDVInvokedUrlCommand) {
        let deviceName = command.arguments[0] as! String
        let ssid = command.arguments[1] as! String
        let passphrase = command.arguments[2] as! String
        if let espDevice = self.espDevices[deviceName] {
            espDevice.provision(ssid: ssid, passPhrase: passphrase) { status in
                let pluginResult: CDVPluginResult
                switch status {
                    case .configApplied:
                        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "WIFI_CONFIG_APPLIED")
                    case let .failure(error):
                        switch error {
                            case .sessionError:
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "CREATE_SESSION_FAILED")
                            case .configurationError(_):
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "WIFI_CONFIG_FAILED")
                            case .wifiStatusError(_):
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "WIFI_STATUS_ERROR")
                            case .wifiStatusDisconnected:
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "WIFI_STATUS_DISCONNECTED")
                            case .wifiStatusAuthenticationError:
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "WIFI_STATUS_AUTHENTICATION_ERROR")
                            case .wifiStatusNetworkNotFound:
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "WIFI_STATUS_NETWORK_NOT_FOUND")
                            case .wifiStatusUnknownError:
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "WIFI_STATUS_UNKNOWN_ERROR")
                            case .unknownError:
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "UNKNOWN_ERROR")
                        }

                    case .success:
                        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "DEVICE_PROVISIONING_SUCCESS")
                }
                pluginResult.setKeepCallbackAs(true)
                self.commandDelegate!.send(pluginResult, callbackId:command.callbackId)
            }
        }
    }
    
    func getProofOfPossesion(forDevice: ESPDevice, completionHandler: @escaping (String) -> Void) {
        completionHandler(self.proofOfPossession)
    }
}
