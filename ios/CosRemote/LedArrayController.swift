//
//  LedArrayController.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/2/27.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import UIKit

class LedArrayController: UIViewController {
    
    @IBOutlet var ivImage: UIImageView!
    @IBOutlet var lbCurrentColor: UILabel!
    
    
    var fRed : CGFloat = 0
    var fGreen : CGFloat = 0
    var fBlue : CGFloat = 0
    var fAlpha: CGFloat = 0
    
    var r = 0 ;
    var g = 0 ;
    var b = 0 ;
    var a = 0 ;

    var lastSelectTime = NSDate() ;
    
    var naviTitle:String = "" ;
    var type:Int = 0 ;
    
    override func viewDidLoad() {
        
    }
    
    var callback:(( view:LedArrayController,remote:CosRemoteV1, r:Int, g:Int, b:Int ) -> Void )?
    
    
    override func viewDidAppear(animated: Bool) {
        
        let gesture = UITapGestureRecognizer(target: self, action: #selector(LedArrayController.imageTouched(_:)))
        gesture.numberOfTapsRequired = 1
        gesture.numberOfTouchesRequired = 1;
        
        self.ivImage.addGestureRecognizer(gesture) ;

        
        
        let pan = UIPanGestureRecognizer(target: self, action: #selector(LedArrayController.imagePaned(_:)))
        self.ivImage.addGestureRecognizer(pan) ;
        
        ivImage.userInteractionEnabled = true ;
    }
    
    override func viewWillAppear(animated: Bool) {
        self.navigationItem.title = self.naviTitle
    }
    
    func imageTouched(gesture:UIGestureRecognizer) {
        print( "touched ") ;
        print( "xy= \(gesture.locationInView(self.ivImage).x), \( gesture.locationInView(self.ivImage).y )" )
        
        
        let pointColor = self.getPixelColorAtPoint(gesture.locationInView(self.ivImage))
        self.lbCurrentColor.backgroundColor = pointColor
        toRGB( pointColor )
        
        self.switchType()
    }
    
    func setSelectColorCallback( callback: ( view:LedArrayController,remote:CosRemoteV1, r:Int, g:Int, b:Int ) -> Void ) {
        self.callback = callback
    }
    
    var index = 0 ;
    func imagePaned( gesture:UIPanGestureRecognizer ) {
        print( "xy= \(gesture.locationInView(self.ivImage).x), \( gesture.locationInView(self.ivImage).y )" )
        
        let pointColor = self.getPixelColorAtPoint(gesture.locationInView(self.ivImage))
        self.lbCurrentColor.backgroundColor = pointColor
        toRGB( pointColor )
        
        self.switchType()
    }
    

    
    func toRGB( color:UIColor) {


        if color.getRed(&fRed, green: &fGreen, blue: &fBlue, alpha: &fAlpha) {
            r = Int(fRed * 255.0)
            g = Int(fGreen * 255.0)
            b = Int(fBlue * 255.0)
            a = Int(fAlpha * 255.0)
            
            
            print( "rgb = \(r),\(g),\(b) " ) ;
            
            
        } else {
            // just do nothing XD
        }
    }
    func getPixelColorAtPoint(point:CGPoint) -> UIColor{
        
        let pixel = UnsafeMutablePointer<CUnsignedChar>.alloc(4)
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        let bitmapInfo = CGBitmapInfo(rawValue: CGImageAlphaInfo.PremultipliedLast.rawValue)
        let context = CGBitmapContextCreate(pixel, 1, 1, 8, 4, colorSpace, bitmapInfo.rawValue)
        
        CGContextTranslateCTM(context, -point.x, -point.y)
        view.layer.renderInContext(context!)
        let color:UIColor = UIColor(red: CGFloat(pixel[0])/255.0, green: CGFloat(pixel[1])/255.0, blue: CGFloat(pixel[2])/255.0, alpha: CGFloat(pixel[3])/255.0)
        
        pixel.dealloc(4)
        return color
    }
    
    
    func switchType() {

        switch self.type {
        case 0:
            self.type0()
            break ;
        case 1:
            self.type1() ;
            break ;
        case 2:
            self.type2() ;
            break;
        case 3:
            self.type3() ;
            break
        default:
            self.type0() ;
        }
        
    }
    
    
    // 一顆一顆循序
    func type0() {
        self.index = self.index + 1;
        self.index = self.index % 60 ;
        if let remote = Common.global.cosRemote {
            remote.setLed(index, r: self.r, g: self.g, b: self.b)
        }
    }
    
    //跑馬燈v2
    func type1() {
        self.index = self.index + 1;
        self.index = self.index % 60 ;
        if let remote = Common.global.cosRemote {
            remote.setLed(index, r: self.r, g: self.g, b: self.b)
        }
    }
    
    //全部單一
    func type2() {
        if let remote = Common.global.cosRemote {
            remote.setLed(255, r: self.r, g: self.g, b: self.b)
        }
    }
    
    //轉hsl
    func type3() {
        if let remote = Common.global.cosRemote {
            remote.setLed(0, r: self.r, g: self.g, b: self.b)
        }
    }
}