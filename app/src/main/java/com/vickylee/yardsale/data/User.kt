package com.vickylee.yardsale.data

import java.util.*
import kotlin.collections.ArrayList

data class User(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var address: String = "",
    var password: String = "",
    var userType: String = "",
) {}