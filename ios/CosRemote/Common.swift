//
//  Common.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/2/18.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import CoreBluetooth

class Common {
    
    // this is clone version @@ 
    // not used @@
    class func getBlunoSerialUUID() -> String {
        return "0000dfb1-0000-1000-8000-00805f9b34fb"
    }
    class func getBlunoChar() -> String {
        return "DFB1"
    }
    
    struct global {
        static var cbManager:CBCentralManager?

        static var selectedDevice:CBPeripheral?
        
        static var cosRemote:CosRemoteV1?
        static var char:CBCharacteristic?
        
    }
    
    
    //記得要加結尾/
    class func getServerURL() -> String{
        return "http://128.199.211.104/mycosalbum/v2/"
    }
    
    class func httpGet(request: NSURLRequest!, callback: ( NSData?, NSError?) -> Void) {
        let session = NSURLSession.sharedSession()
        let task = session.dataTaskWithRequest(request){
            (data, response, error) -> Void in
            if error != nil {
                callback(data, error)
            } else {
                callback(data, nil)
            }
        }
        task.resume()
    }
}