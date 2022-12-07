package com.vickylee.yardsale.data

import java.util.*

data class User (var id : String = UUID.randomUUID().toString(),
                 var name: String = "",
                 var email : String = "",
                 var password : String = "",
                 var userType : String = ""
)