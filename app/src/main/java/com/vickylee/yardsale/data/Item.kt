package com.vickylee.yardsale.data

import com.google.firebase.Timestamp

data class Item(
    var itemName: String = "",
    var itemDescription: String = "",
    var itemPrice: Double = 0.0,
    var isItemAvailable: Boolean = true,
    var creationTimestamp: Timestamp = Timestamp.now()
)