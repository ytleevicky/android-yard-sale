package com.vickylee.yardsale.data

import android.net.Uri
import java.util.*

data class User (var id : String = UUID.randomUUID().toString(),
                 var name: String = "",
                 var email : String = "",
                 var phone: String = "",
                 var address : String = "",
                 var password : String = "",
                 var userType : String = "",
                 var profilePic: String = ""
){}