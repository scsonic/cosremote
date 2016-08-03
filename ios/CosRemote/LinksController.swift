//
//  LinksController.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/2/18.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import UIKit

class LinksController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    
    
    var urlArray = NSMutableArray() ;
    
    @IBOutlet var tableView: UITableView!
    
    class UrlItem {
        var title:String = "";
        var url:String = "";
        var image:String = "" ;
        
        init( dic:NSDictionary ) {
            
            if let t = dic["title"] as? String {
                self.title = t
            }
            if let url = dic["url"] as? String {
                self.url = url ;
            }
            if let i = dic["image"] as? String {
                self.image = i ;
            }
            
        }
    }
    
    override func viewDidLoad() {
        self.navigationItem.title = "連結"
    }
    
    override func viewDidAppear(animated: Bool) {
        
        self.tableView.delegate = self ;
        self.tableView.dataSource = self ;
        
        let url = "http://ygk.no-ip.org/mycosalbum/v3/links.php?type=ios" ;
        let req = NSURLRequest( URL: NSURL(string: url)!) ;
        
        if self.urlArray.count == 0 {
            let ai = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.WhiteLarge)
            ai.center = self.view.center ;
            ai.color = UIColor.blackColor() ;
            self.view.addSubview(ai) ;
            ai.startAnimating() ;
            
            Common.httpGet(req) { (data:NSData?, error:NSError? ) -> Void in
                
                do {
                    let json:NSArray = try NSJSONSerialization.JSONObjectWithData(data!, options: .AllowFragments) as! NSArray
                    
                    for dic in json {
                        self.urlArray.addObject( dic ) ;
                    }
                    print(" url request ok =\(json)")
                } catch {
                    print("json error \(error)")
                }
                dispatch_async(dispatch_get_main_queue(),{
                    ai.hidden = true ;
                    ai.removeFromSuperview() ;
                    self.tableView.reloadData()
                })
            }
        }
    }
    
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCellWithIdentifier("LinksCell") as! LinksCell
        let dic = self.urlArray[indexPath.item] ;
        
        cell.lbContent.text = "" ;
        cell.ivImage.image = nil ;
        cell.lbContent.text = "" ;
        
        if let title = dic["title"] as? String {
            cell.lbTitle.text = title ;
        }
        if let icon = dic["icon"] as? String {
            cell.ivImage.imageFromUrl( icon ) ;
            print("icon = \(icon)")
        }
        if let desc = dic["desc"] as? String {
            cell.lbContent.text = desc ;
        }
        return cell
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.urlArray.count
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        let dic = self.urlArray[indexPath.item]
        if let url = dic["url"] as? String {
        let url : NSURL = NSURL(string: url)!
            if UIApplication.sharedApplication().canOpenURL(url) {
                UIApplication.sharedApplication().openURL(url)
            }
        }
    }
}