package com.miriamlaurel

import com.miriamlaurel.fxcore.SafeDouble
import com.miriamlaurel.fxcore.asset.AssetClass

package object aggro {
  type Inventory = Map[AssetClass, SafeDouble]
}
