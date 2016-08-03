//
//  CosRemoteV1.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/2/27.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import CoreBluetooth

class CosRemoteV1: NSObject, CBPeripheralDelegate {
    
    let lockQueue = dispatch_queue_create("com.ygk.cosremote.CosRemoveV1", nil)

    
    var device:CBPeripheral
    var manager:CBCentralManager
    
    struct Command {
        static var ALLLED = 255
        static var SKIP = 254 ;
        static var SHIFT = 253 ;
        
        
        static var COMMAND = 252 ;
        static var CMD_PIXEL_COUNT = 1
        static var CMD_BRITENESS = 2
        static var CMD_DEBUG = 3
        
        static var HAND_COMMAND = 251 ;
        static var ALL_FINGER = 255 ;
        static var ALL_SERVO = 254 ;
        
        /* 
            thumb
            fore finger / index finger
            middle finger
            ring finger
            little finger
        */
    }
    
    /*
    #define HAND_COMMAND 251
    #define ALL_FINGER 255
    #define ALL_SERVO 254
    // pin DO NOT USE PIN3 FOR INPACTABLE WITH LED XD
    
    Servo inmoov[6] ; // arm = 0, thumb=5
    */

    var writeQueue = [NSData]() ;
    var writeLock = false ;
    
    var thread:NSThread?
    
    init( device:CBPeripheral ) {

        self.device = device ;

        if let m = Common.global.cbManager {
            self.manager = m
        }
        else {
            self.manager = CBCentralManager()
        }


        super.init() ;
        self.device.delegate = self ;
        self.thread = NSThread(target: self, selector: #selector(CosRemoteV1.run), object: nil)
        
        self.thread!.start()
    }
    
    
    func run() {
        
        while ( self.isConnect() ) {
            //NSThread.sleepForTimeInterval(1)
            //
            
            if self.writeLock == false {
                
                dispatch_sync(lockQueue) {

                    if self.writeQueue.count > 0 {
                        print( "queue.count=\( self.writeQueue.count)")
                        //var data = self.writeQueue.removeFirst()
                        let data = NSMutableData(data: self.writeQueue.removeFirst() )
                        
                        while ( (self.writeQueue.count > 0) && ( 20 > ( data.length + self.writeQueue[0].length ))) {
                            data.appendData( self.writeQueue.removeFirst()) ;
                        }
                        
                        self.writeLock = true ;
                        print("write \( data.length ) byte at once ^^, queue=\( self.writeQueue.count )") ;
                        self.device.writeValue(data, forCharacteristic: Common.global.char!, type: CBCharacteristicWriteType.WithResponse)
                    }
                }
            }
            else {
                //print( "locking @@") ;
                usleep(200)
            }
        }
        
        print("CosRemoteThread due to disconnect closed") ;
    }
    
    
    /*
    func sync(lock: AnyObject, closure: () -> Void) {
        objc_sync_enter(lock)
        closure()
        objc_sync_exit(lock)
    }
    */

    
    
    func isConnect() -> Bool {
        
        if self.device.state == CBPeripheralState.Connected {
            return true ;
        }
        else {
            return false
        }
    }
    
    
    // depre
    func connect( callback:( (ret:Bool) -> Void )) {
        
    }
    
    
    
    func setLed( index:Int, r:Int, g:Int, b:Int) {
        
        let data = NSData(bytes: [ UInt8(index), UInt8(r), UInt8(g), UInt8(b) ] as [UInt8], length: 4)

        dispatch_sync(lockQueue) {
            self.writeQueue.append(data) ;
        }
    }
    
    func setAllLed( r:Int, g:Int, b:Int ) {
        
        self.setLed(255, r: r, g: g, b: b) ;
    }
    
    func setLed_Old( index:Int, r:Int, g:Int, b:Int) {
        
        let data = NSData(bytes: [ UInt8(index), UInt8(r), UInt8(g), UInt8(b) ] as [UInt8], length: 4)
        self.device.writeValue(data, forCharacteristic: Common.global.char!, type: CBCharacteristicWriteType.WithoutResponse)
        
        print("writed data\(data), \(Common.global.char), i=\(index) rgb=\(r) \(g) \(b) ")
    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverCharacteristicsForService service: CBService, error: NSError?) {
        
    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverDescriptorsForCharacteristic characteristic: CBCharacteristic, error: NSError?) {

    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverServices error: NSError?) {
        
    }
    
    func peripheral(peripheral: CBPeripheral, didReadRSSI RSSI: NSNumber, error: NSError?) {
        
        print("rssi=\(RSSI)")
    }
    
    func peripheral(peripheral: CBPeripheral, didWriteValueForCharacteristic characteristic: CBCharacteristic, error: NSError?) {
        
    
        writeLock = false ;
        //print("did write to err=\(error)") ;
        print( "did write and, queue.count=\( self.writeQueue.count)")
    }
    
    
    func peripheral(peripheral: CBPeripheral, didUpdateValueForCharacteristic characteristic: CBCharacteristic, error: NSError?) {
        print ("didUpdateValueForCharacteristic")
        let str = NSString(data: characteristic.value!, encoding: NSASCIIStringEncoding)
        //var str = NSString(data: characteristic.value(), encoding: NSASCIIStringEncoding)
        if str != nil {
            print ("read:(\(str!.length)) \(str!)")
        }
    }
    
    
    // function about finger 
    func setAllFinger( degree:Int ) {
        let data = NSData(bytes: [ UInt8(Command.HAND_COMMAND), UInt8(Command.ALL_FINGER), UInt8(degree)] as [UInt8], length: 3)
        
        dispatch_sync(lockQueue) {
            self.writeQueue.append(data) ;
        }
    }
    func setAllServo( degree:Int ) {
        let data = NSData(bytes: [ UInt8(Command.HAND_COMMAND), UInt8(Command.ALL_SERVO), UInt8(degree)] as [UInt8], length: 3)
        
        dispatch_sync(lockQueue) {
            self.writeQueue.append(data) ;
        }
    }
    
    func setFinger( index:Int, degree:Int ) {
        if ( index >= 0 && index <= 5 ) {
            let data = NSData(bytes: [ UInt8(Command.HAND_COMMAND), UInt8(index), UInt8(degree)] as [UInt8], length: 3)
            
            dispatch_sync(lockQueue) {
                self.writeQueue.append(data) ;
            }
        }
        else {
            print("finger index error, maybe out of index @@")
        }
    }
    
    func setArm( degree:Int ) {
        let data = NSData(bytes: [ UInt8(Command.HAND_COMMAND), UInt8(0), UInt8(degree)] as [UInt8], length: 3)
        
        dispatch_sync(lockQueue) {
            self.writeQueue.append(data) ;
        }
    }
}