//
//  SelectFuncController.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/3/2.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import UIKit


class SelectFunctionController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    
    
    @IBOutlet var tableView: UITableView!
    
    var list = [TitleControler]() ;
    
    class TitleControler {
        
        var title:String
        var viewController:UIViewController
        
        var type:Int = 0 ;
        
        init( t:String, v:UIViewController ) {
            self.title = t ;
            self.viewController = v ;
        }
        
        init( t:String, v:UIViewController, type:Int) {
            self.title = t ;
            self.viewController = v ;
            self.type = type ;
        }
    }
    
    func initList() {
        self.list.removeAll() ;
        
        let ledArrayView = self.storyboard?.instantiateViewControllerWithIdentifier("LedArrayController") as! LedArrayController ;
        
        
        list.append(TitleControler(t: "Led跑馬燈-1", v: ledArrayView, type: 0 ))
        list.append(TitleControler(t: "Led跑馬燈-2", v: ledArrayView, type: 1 ))
        list.append(TitleControler(t: "單一顏色",    v: ledArrayView, type: 2 ))
        list.append(TitleControler(t: "火焰顏色控制",    v: ledArrayView, type: 3 )) // HSL format
        
        let accview = self.storyboard?.instantiateViewControllerWithIdentifier("AccController") ;
        list.append(TitleControler(t: "三軸加速度感測器", v: accview!))
        
        let inmoovbasic = self.storyboard?.instantiateViewControllerWithIdentifier("InmoovBasicController") ;
        list.append(TitleControler(t: "機器手臂基本控制", v: inmoovbasic!))
        
        
        let inmoovAdvance = self.storyboard?.instantiateViewControllerWithIdentifier("InmoovAdvanceController") ;
        list.append(TitleControler(t: "機器手臂快速手勢", v: inmoovAdvance!))
        
    }
    
    
    override func viewDidLoad() {
        
        initList() ;
        self.tableView.delegate = self ;
        self.tableView.dataSource = self ;
        
        
    }
    
    override func viewDidAppear(animated: Bool) {
        self.navigationItem.title = "功能"
    }
    
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.list.count
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        let item = self.list[ indexPath.item ]
        
        if let ledarray = item.viewController as? LedArrayController {
            ledarray.naviTitle = item.title ;
            ledarray.type = item.type ;
        }
        
        self.navigationController?.pushViewController(item.viewController, animated: true) ;
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let item = self.list[ indexPath.item ]
        let cell = tableView.dequeueReusableCellWithIdentifier("SelectFuncCell") as! SelectFuncCell
        
        cell.lbTitle.text = item.title
        
        return cell ;
    }

    
}