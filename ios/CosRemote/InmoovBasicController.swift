//
//  InmoovBasicController.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/3/7.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import UIKit


class InmoovBasicController: UIViewController {
    
    
    @IBOutlet var slArm: UISlider!
    @IBOutlet var slIndex: UISlider!
    @IBOutlet var slMiddle: UISlider!
    @IBOutlet var slRing: UISlider!
    @IBOutlet var slLittle: UISlider!
    @IBOutlet var slThumb: UISlider!
    
    
    var servoArr:[UISlider] = []
    
    var sliderThread:NSThread?
    var sliderBuffer = [Int: Int]() ;
    override func viewWillAppear(animated: Bool) {
        
        
        self.navigationItem.title = ""
        
        let right = UIBarButtonItem(title: "Inmoov", style: UIBarButtonItemStyle.Done, target: self, action: #selector(InmoovBasicController.inmoonUrlPress)) ;
        self.navigationItem.rightBarButtonItem = right ;
        
        servoArr = [slArm, slIndex, slMiddle, slRing, slLittle, slThumb ] ;
        for slider in servoArr {
            slider.addTarget(self, action: #selector(InmoovBasicController.onSlideChange(_:)), forControlEvents: UIControlEvents.ValueChanged)
        }
        
    }
    
    func onSlideChange( sender:UISlider ) {
        print("slide valu = \( sender.value )")
        var sliderIndex = -1 ;
        for (index, slider) in servoArr.enumerate() {
            if slider == sender {
                //print("find sender = \( index ) ")
                sliderIndex = index ;
            }
        }
        
        if ( sliderIndex != -1 ) {
            sliderBuffer[sliderIndex] = Int(sender.value)
            
            if let remote = Common.global.cosRemote {
                remote.setFinger(sliderIndex, degree: Int(sender.value) )
            }
            /*
            if sliderThread == nil {
                sliderThread = NSThread(target: self, selector: "run", object: nil);
            }
            */
        }
        
    }
    
    
    func run() {
        
        while sliderBuffer.count > 0 {
            if let remote = Common.global.cosRemote {
                print(remote)
            }
        }
    }
    
    
    func inmoonUrlPress() {
        UIApplication.sharedApplication().openURL(NSURL(string: "http://inmoov.fr/")! );
    }
    

    @IBAction func btnAllZeroPress(sender: AnyObject) {
        if let remote = Common.global.cosRemote {
            remote.setAllServo(0) ;
        }
    }
    
    @IBAction func btnAll180Press(sender: AnyObject) {
        if let remote = Common.global.cosRemote {
            remote.setAllServo(180) ;
        }
    }
    
    @IBAction func btnFingerZeroPress(sender: AnyObject) {
        if let remote = Common.global.cosRemote {
            remote.setAllFinger(0)
        }
    }
    
    @IBAction func btnFinger90Press(sender: AnyObject) {
        if let remote = Common.global.cosRemote {
            remote.setAllFinger(90)
        }
    }

    @IBAction func btnFinger45Press(sender: AnyObject) {
        if let remote = Common.global.cosRemote {
            remote.setAllFinger(45)
        }
    }
    @IBAction func btnFinger180Press(sender: AnyObject) {
        if let remote = Common.global.cosRemote {
            remote.setAllFinger(180)
        }
    }
}