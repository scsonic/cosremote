//
//  InmoovAction.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/4/11.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation


class InmoovAction {
    var type:InmoovType = InmoovType.delay
    var value:Int = 0
    
    init( t:InmoovType, v:Int) {
        self.type = t ;
        self.value = v ;
    }
    
    func processAction() {
        
        if let remote = Common.global.cosRemote {
            switch type {
            case InmoovType.delay:
                print("delay for \( self.value)") ;
                NSThread.sleepForTimeInterval(Double( self.value ) / 1000.0) ; //這個東西執行單位是秒 @@
                print("delay end ")
                break ;
                
            default:
                print(" setfinger\( self.type), degree=\(self.value)")
                remote.setFinger( self.type.rawValue, degree: self.value)
                break ;
            }
        }
    }
    
    class func Make( index:Int?, middle:Int?, ring:Int?, little:Int?, thumb:Int? ) -> [InmoovAction] {
        var actions = [InmoovAction]() ;
        let arr:[Int?] = [ index, middle, ring, little, thumb ]
        var i = 1 ;
        for finger in arr {
            if let f = finger {
                if let t = InmoovType(rawValue: i) {
                    actions.append( InmoovAction(t: t, v: f))
                }
            }
            i = i + 1
        }
        return actions
    }
    
    class func MakeFinger( arr:[Int], delay:Int = 0 ) -> [InmoovAction]{
        var actions = [InmoovAction]() ;
        var i = 1 ;
        for finger in arr {
            if let t = InmoovType(rawValue: i) {
                actions.append( InmoovAction(t: t, v: finger))
            }
            i = i + 1
        }
        
        if delay != 0 {
            actions.append( MakeDelay(delay) )
        }
        return actions
    }
    
    class func MakeArm( degree:Int) -> InmoovAction {
        return InmoovAction(t: InmoovType.arm, v: degree)
    }
    
    class func MakeDelay( ms:Int ) -> InmoovAction {
        return InmoovAction(t: InmoovType.delay, v: ms)
    }
}