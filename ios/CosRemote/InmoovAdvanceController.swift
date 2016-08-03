//
//  InmoovBasicController.swift
//  CosRemote
//
//  Created by 郭 又鋼 on 2016/3/7.
//  Copyright © 2016年 郭 又鋼. All rights reserved.
//

import Foundation
import UIKit


class InmoovAdvanceController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate {
    
    @IBOutlet var collectionView: UICollectionView!
    
    var gestures = [InmoovGesture]() ;
    
    
    override func viewDidLoad() {
        gestures.appendContentsOf(InmoovGesture.Gestures.getAll());
        
        
        
        self.collectionView.delegate = self
        self.collectionView.dataSource = self ;
        
        
    }
    
    override func viewWillAppear(animated: Bool) {
        self.navigationItem.title = ""
        let right = UIBarButtonItem(title: "Inmoov", style: UIBarButtonItemStyle.Done, target: self, action: #selector(InmoovAdvanceController.inmoonUrlPress)) ;
        self.navigationItem.rightBarButtonItem = right ;


    }

    func inmoonUrlPress() {
        UIApplication.sharedApplication().openURL(NSURL(string: "http://inmoov.fr/")! );
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("GestureCell", forIndexPath: indexPath) as! GestureCell
        
        cell.lbTitle.text = self.gestures[indexPath.item].title
        return cell
    }
    

    func collectionView(collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        sizeForItemAtIndexPath  indexPath: NSIndexPath) -> CGSize
    {
        let requiredWidth = collectionView.bounds.size.width
        let targetSize = CGSize(width: requiredWidth / 3 - 0.67 , height: requiredWidth / 3 - 0.67 )
        /// NOTE: populate the sizing cell's contents so it can compute accurately
        //self.sizingCell.label.text = items[indexPath.row]
        //let adequateSize = self.sizingCell.preferredLayoutSizeFittingSize(targetSize)
        return targetSize
    }
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        print("did select \(indexPath.item)")
        
        let gesture = self.gestures[indexPath.item] ;
        InmoovGesture.appendQueue(gesture) ;
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.gestures.count
    }
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
}