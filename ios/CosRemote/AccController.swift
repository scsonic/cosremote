//
//  AccController.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/3/2.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import UIKit
import CoreMotion

class AccController: UIViewController  {
    
    let cmManager = CMMotionManager()

    @IBOutlet var lbZ: UILabel!
    @IBOutlet var lbY: UILabel!
    @IBOutlet var lbX: UILabel!
    
    @IBOutlet var pbX: UIProgressView!
    @IBOutlet var pbY: UIProgressView!
    @IBOutlet var pbZ: UIProgressView!
    
    @IBOutlet var lbCurrentColor: UILabel!
    
    
    override func viewDidLoad() {
        

    }
    
    var r:Int = 0 ;
    var g:Int = 0 ;
    var b:Int = 0 ;
    
    let SKIP_TIMES = 8 // 每n個少一個byte
    
    var skip:Int = 0 ;
    
    override func viewWillAppear(animated: Bool) {
        if cmManager.accelerometerAvailable{
            let queue = NSOperationQueue()
            cmManager.startAccelerometerUpdatesToQueue(queue, withHandler: self.accHandler)
        } else {
            print("Accelerometer is not available")
        }
        
        self.navigationItem.title = "三軸加速度感測器" ;
    }
    
    func accHandler(data:CMAccelerometerData?, error:NSError?) {
        guard let data = data else{
            return
        }
        
        self.r = self.normalize(data.acceleration.x)
        self.g = self.normalize(data.acceleration.y)
        self.b = self.normalize(data.acceleration.z)
        
        print(", \(r) X = \(data.acceleration.x)")
        print(", \(g) Y = \(data.acceleration.y)")
        print(", \(b) Z = \(data.acceleration.z)")
        
        skip = skip + 1 ;
        if ( skip != SKIP_TIMES ) {

        }
        else {
            skip = 0
            
            dispatch_async(dispatch_get_main_queue(),{
                // do ui update
                self.lbX.text = String(format: "X: %3d", self.r)
                self.lbY.text = String(format: "Y: %3d", self.g)
                self.lbZ.text = String(format: "Z: %3d", self.b)
                
                self.pbX.setProgress( Float(self.r) / 255.0, animated: true)
                self.pbY.setProgress( Float(self.g) / 255, animated: true)
                self.pbZ.setProgress( Float(self.b) / 255 , animated: true) ;
                
                self.lbCurrentColor.backgroundColor = UIColor(red: CGFloat(self.pbX.progress), green: CGFloat(self.pbY.progress), blue: CGFloat(self.pbZ.progress), alpha: 1) ;
                
                if let remote = Common.global.cosRemote {
                    remote.setAllLed(self.r, g: self.g, b: self.b) ;
                }
            })
            

        }
    }
    
    func normalize( dd:Double ) -> Int {
        var d = dd ;
        if ( d <= -1 ) {
            return 0 ;
        }
        else if ( d >= 1 ) {
            return 255 ;
        }
        else {
            d = d + 1 ;
            d = d * 255 / 2
        }
        return Int(d) ;
    }
    override func viewDidDisappear(animated: Bool) {
        cmManager.stopAccelerometerUpdates() ;
    }
}