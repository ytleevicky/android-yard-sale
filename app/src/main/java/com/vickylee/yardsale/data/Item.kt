package com.vickylee.yardsale.data

import com.google.firebase.Timestamp
import java.util.*

data class Item(
    var sellerID: String = "",
    var itemID: String = UUID.randomUUID().toString(),
    var itemName: String = "",
    var itemDescription: String = "",
    var itemPrice: Double = 0.0,
    var isItemAvailable: Boolean = true,
    var creationTimestamp: Timestamp = Timestamp.now()
) : java.io.Serializable