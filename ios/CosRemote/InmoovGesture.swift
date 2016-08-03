//
//  InmoovAction.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/3/8.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation

class InmoovGesture {
    
    struct global {
        static var isRunning:Bool = false
        static var thread:NSThread? ;
        static var queue = [InmoovGesture]() ;
        static let gestureQueue = dispatch_queue_create("com.ygk.cosremote.Gesture", nil)
        static let run = dispatch_queue_create("com.ygk.cosremote.GestureRun", nil)
    }
    
    var title = ""
    var imageName:String = ""
    
    var actions = [InmoovAction]() ;

    
    // old
    class func execute(gesture:InmoovGesture) {
        
        dispatch_async(InmoovGesture.global.gestureQueue) { () -> Void in
            InmoovGesture.global.queue.append(gesture) ;
            
            if let t = InmoovGesture.global.thread {
                // has already in thread @@
                print(t) ;
            }
            else {
                InmoovGesture.global.thread = NSThread(target: self, selector: #selector(NSRunLoop.run), object: nil)
                InmoovGesture.global.thread?.start() ;
            }
        }
    }
    
    class func appendQueue( gesture:InmoovGesture ) {
        self.global.queue.append(gesture) ;
        executeRunQueue() ;
    }
    class func executeRunQueue() {
        
        
        dispatch_async(InmoovGesture.global.gestureQueue) { () -> Void in
            var gesture:InmoovGesture? ;
            if InmoovGesture.global.queue.count > 0 {
                gesture = InmoovGesture.global.queue.removeFirst();
                print("execute one gesture act.count= \( gesture?.actions.count )") ;
                
                dispatch_async(InmoovGesture.global.run ) { () -> Void in
                    
                    if let g = gesture {
                        for action in g.actions {
                            action.processAction() ;
                        }
                    }
                }
            }
        }
    }
    
    /*
    class func run() {
        var gesture:InmoovGesture? ;
        while ( true ) {
            dispatch_async(InmoovGesture.global.gestureQueue) { () -> Void in
                if InmoovGesture.global.queue.count > 0 {
                    gesture = InmoovGesture.global.queue.removeFirst();
                }
                else {
                    break ;
                }
            }
            
            // process action @@
            if let g = gesture {
            }
        }
    }
    */
    
    class Gestures {
        
        class func getAll() -> [InmoovGesture] {
            return [InmoovGesture.Gestures.getFuck(),InmoovGesture.Gestures.getNico(),InmoovGesture.Gestures.getOne(),InmoovGesture.Gestures.getTwo(),InmoovGesture.Gestures.getThree(),InmoovGesture.Gestures.getFour(),InmoovGesture.Gestures.getFive(), InmoovGesture.Gestures.getComeOn(),InmoovGesture.Gestures.getWave(),InmoovGesture.Gestures.getYaya() ]
        }
        class func getFuck() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Fuck"
            gesture.actions.append( InmoovAction(t: InmoovType.thumb, v: 175) )
            gesture.actions.append( InmoovAction(t: InmoovType.ring, v: 175) )
            gesture.actions.append( InmoovAction(t: InmoovType.index, v: 175) )
            gesture.actions.append( InmoovAction(t: InmoovType.little, v: 175) )
            gesture.actions.append( InmoovAction(t: InmoovType.middle, v: 5) )
            return gesture ;
        }
        
        class func getNico() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Nico"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 175, 175, 5, 175]) )
            
            return gesture ;
        }
        
        class func getOne() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "One"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 175, 175, 175, 175]) )
            return gesture ;
        }
        
        class func getTwo() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Two"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 175, 175, 175]) )
            return gesture ;
        }
        class func getThree() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Three"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 5, 175, 175]) )
            return gesture ;
        }
        class func getFour() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Four"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 5, 5, 175]) )
            return gesture ;
        }
        class func getFive() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Five"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 5, 5, 5]) )
            return gesture ;
        }
        
        class func getComeOn() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "ComeOn"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 5, 5, 175] , delay: 600) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 175, 175, 175, 175, 175]) )
            gesture.actions.append(InmoovAction.MakeDelay(600) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 5, 5, 175]) )
            gesture.actions.append(InmoovAction.MakeDelay(600) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 175, 175, 175, 175, 175]) )
            gesture.actions.append(InmoovAction.MakeDelay(600) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 5, 5, 175]) )
            gesture.actions.append(InmoovAction.MakeDelay(600) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 175, 175, 175, 175, 175]) )
            gesture.actions.append(InmoovAction.MakeDelay(600) )
            
            return gesture ;
        }
        
        class func getWave() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Wave"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 5, 5, 5, 5, 0] , delay: 400) )
            
            
            for i in 1 ... 30 {
                
                let up = InmoovType(rawValue: ( i % 4 + 1 ))!
                gesture.actions.append(InmoovAction.init(t: up, v: 5))
                
                if ( i > 3 ) {
                    let down = InmoovType(rawValue: ((i-3) % 4 + 1))!
                    gesture.actions.append(InmoovAction.init(t: down, v: 175))
                }
                
                gesture.actions.append(InmoovAction.MakeDelay(300)) ;
            }
            
            
            return gesture ;
        }
        
        
        // 抓抓的意思
        class func getGarbGarb() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "GarbGarb"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )

            return gesture ;
        }
        
        // yaya 其實是finger sex …
        class func getYaya() -> InmoovGesture {
            let gesture = InmoovGesture() ;
            gesture.title = "Yaya"
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 10, 10, 10, 10, 10] , delay: 400) )
            gesture.actions.appendContentsOf( InmoovAction.MakeFinger([ 170, 170, 170, 170, 170] , delay: 400) )
            
            return gesture ;
        }
    }
    
}