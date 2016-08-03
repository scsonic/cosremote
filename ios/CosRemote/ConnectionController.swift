//
//  ViewController.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/2/17.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import UIKit
import CoreBluetooth

class ConnectionController: UIViewController, CBCentralManagerDelegate, UITableViewDelegate, UITableViewDataSource, CBPeripheralDelegate {
    var cbManager: CBCentralManager? = nil

    @IBOutlet var lbConnectStatus: UILabel!
    @IBOutlet var tableView: UITableView!
    
    @IBOutlet var btnScan: UIButton!
    
    @IBOutlet var aiScaning: UIActivityIndicatorView!
    var bledevices = [CBPeripheral]() ;
    
    @IBAction func btnScanPress(sender: AnyObject) {
        
        btnScan.enabled = false
        btnScan.titleLabel?.text = "掃描"
        cbManager!.scanForPeripheralsWithServices(nil, options: nil)
        NSTimer.scheduledTimerWithTimeInterval(5, target: self, selector: #selector(ConnectionController.stopScan), userInfo: nil, repeats: false)
        
        self.aiScaning.hidden = false ;
        self.aiScaning.startAnimating() ;
    }
    
    override func viewWillAppear(animated: Bool) {
        self.navigationItem.title = "連線"
    }
    
    func stopScan() {
        self.tableView.reloadData()
        cbManager?.stopScan()
        btnScan.titleLabel?.text = "掃描"
        btnScan.enabled = true ;
        
        self.aiScaning.hidden = true ;
        self.aiScaning.stopAnimating()
    }
    
    func centralManager(central: CBCentralManager, didDiscoverPeripheral peripheral: CBPeripheral, advertisementData: [String : AnyObject], RSSI: NSNumber)
    {
        print( "find device:\( peripheral.name), mac=\( peripheral.debugDescription ), \( peripheral.identifier ) ")
        let temp = bledevices.filter { (pl) -> Bool in
            return pl.name == peripheral.name
        }
        
        if temp.count == 0 {
            bledevices.append(peripheral)
            print(" append to array@@")
            //BTRSSI.append(RSSI)
            //BTIsConnectable.append(Int(advertisementData[CBAdvertisementDataIsConnectable]!.description)!)
        }
        tableView.reloadData()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        cbManager = CBCentralManager(delegate: self, queue: nil) ;
        
        if cbManager == nil {
            print("can't get ble @@") ;
            return ;
        }
        self.tableView.dataSource = self ;
        self.tableView.delegate = self ;
        self.aiScaning.hidden = true
        
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.bledevices.count ;
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        self.cbManager?.stopScan()
        let device = bledevices[indexPath.item] ;
        print( device.identifier ) ;
        self.cbManager?.connectPeripheral(device, options: nil) ;
        
    }
    
    
    func peripheral(peripheral: CBPeripheral, didDiscoverServices error: NSError?) {
        
        if error == nil {
            
            print(" this device has \( peripheral.services!.count )")
            for serviceObj in peripheral.services! {
                let service:CBService = serviceObj
                
                print (" getService uuid=\(service.UUID)") ;
                /*
                let isServiceIncluded = self.btServices.filter({ (item: BTServiceInfo) -> Bool in
                    return item.service.UUID == service.UUID
                }).count
                if isServiceIncluded == 0 {
                    btServices.append(BTServiceInfo(service: service, characteristics: []))
                }
                */
                
                if ( "dfb0".caseInsensitiveCompare("\(service.UUID)") == NSComparisonResult.OrderedSame) {
                    peripheral.discoverCharacteristics(nil, forService: service)
                    return ;
                }
            }
            
            self.cbManager?.cancelPeripheralConnection(peripheral) ;
            self.connectNotMyDevice()
        }
        else {
            print("get error with discover service \(error)")
            self.connectFail()
        }
    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverCharacteristicsForService service: CBService, error: NSError?) {
        
        
        if ( service.characteristics != nil ) {
            print("service:\( service.UUID ) discover char ok, count=\( service.characteristics!.count )")
            for char in service.characteristics! {
                print( "____char=\(char.UUID)" ) ;
                if ( "dfb1".caseInsensitiveCompare("\(char.UUID)") == NSComparisonResult.OrderedSame) {
                    
                    print("find!! this is my device!! pass to cos remote") ;
                    self.connectSuccess(peripheral, char: char)
                    
                    dispatch_async(dispatch_get_main_queue(),{
                        self.tableView.reloadData()
                    })

                    return ;
                }
            }
        }
        else {
            print("can't find char in this service") ;
        }
        
        self.connectNotMyDevice();
    }
    
    
    func connectSuccess( device:CBPeripheral, char:CBCharacteristic) {
        Common.global.selectedDevice = device
        Common.global.char = char
        Common.global.cosRemote = CosRemoteV1(device: device) ;

    }
    
    func connectFail() {
        
        let alert = UIAlertController(title: "藍芽連線", message: "連線失敗", preferredStyle: UIAlertControllerStyle.Alert)
        alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
        
        self.presentViewController(alert, animated: true, completion: nil) ;
        
        dispatch_async(dispatch_get_main_queue(),{
            self.tableView.reloadData()
        })
        
    }
    
    func connectNotMyDevice() {
        let alert = UIAlertController(title: "藍芽連線", message: "這個裝置上找不到對應的Service", preferredStyle: UIAlertControllerStyle.Alert)
        alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
        self.presentViewController(alert, animated: true, completion: nil) ;
        
        
        dispatch_async(dispatch_get_main_queue(),{
            self.tableView.reloadData()
        })
    }
    
    
    func centralManager(central: CBCentralManager, didConnectPeripheral peripheral: CBPeripheral) {
        print("connect success XDXD")
        peripheral.delegate = self ;
        peripheral.discoverServices([CBUUID(string: "dfb0") ] )
        
        self.tableView.reloadData()
    }

    func centralManager(central: CBCentralManager, didFailToConnectPeripheral peripheral: CBPeripheral, error: NSError?) {
        print( "connect fail @@")
        
        Common.global.selectedDevice = nil ;
        Common.global.cosRemote = nil ;
        
        self.tableView.reloadData()
    }
    
    func centralManager(central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: NSError?) {
        print("did disconnect @@ err=\(error)")
        
        
        if Common.global.selectedDevice != nil {
            
            //是那種原本連上了才斷的 提示警告
            let alert = UIAlertController(title: "提示", message: "您的裝置已斷線，請重新連線，可能出現問題、離太遠或是沒電。", preferredStyle: UIAlertControllerStyle.Alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
            self.presentViewController(alert, animated: true, completion: nil) ;
        }
        Common.global.selectedDevice = nil ;
        Common.global.cosRemote = nil ;
        
        self.tableView.reloadData()
    }
    
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCellWithIdentifier("DeviceScanCell") as! DeviceScanCell
        let device = bledevices[indexPath.item] ;
        cell.lbDeviceName.text = device.name
        
        
        cell.lbIsConnect.text = ""
        cell.accessoryType = .None
        if let connDevice = Common.global.selectedDevice {
            if device.name == connDevice.name {
                cell.lbIsConnect.text = "已連線"
                cell.accessoryType = .Checkmark
            }
        }

        
        return cell ;
        /*
        let device = bledevices[indexPath.item] ;
        let cell = UITableViewCell() ;
        cell.textLabel?.text = device.name ;
        return cell ;
        */
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func centralManagerDidUpdateState(central: CBCentralManager) {
        switch central.state {
        case CBCentralManagerState.PoweredOn:
            print("BT ON")
            break ;
        case CBCentralManagerState.PoweredOff:
            print("BT OFF")
        case CBCentralManagerState.Resetting:
            print("BT RESSTING")
        case CBCentralManagerState.Unknown:
            print("BT UNKNOW")
        case CBCentralManagerState.Unauthorized:
            print("BT UNAUTHORIZED")
        case CBCentralManagerState.Unsupported:
            print("BT UNSUPPORTED")
            //self.lbConnectStatus.text = "藍芽裝置未啟動"
        }
    }
}

