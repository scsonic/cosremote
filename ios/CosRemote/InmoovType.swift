//
//  InmoovType.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/4/11.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation


public enum InmoovType: Int {
    case arm = 0
    case index = 1
    case middle = 2
    case ring = 3
    case little = 4
    case thumb = 5
    
    case delay = 100
}

/*
public enum CVScrollDirection {
    case None
    case Right
    case Left
    
    var description: String {
        get {
            switch self {
            case .Left: return "Left"
            case .Right: return "Right"
            case .None: return "None"
            }
        }
    }
}
*/