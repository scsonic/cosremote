//
//  MyExt.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/2/18.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import CoreBluetooth
import UIKit

extension UIImageView {
    public func imageFromUrl(urlString: String) {
        if let url = NSURL(string: urlString) {
            let request = NSURLRequest(URL: url)
            
            NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()) {
                (response: NSURLResponse?, data: NSData?, error: NSError?) -> Void in
                if let imageData = data as NSData? {
                    self.image = UIImage(data: imageData)
                }
            }
        }
    }
}