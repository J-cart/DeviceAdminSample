package com.tutorials.deviceadminsample

data class User(val email:String = "", val uid:String = "", val deviceToken:List<String> = emptyList(),val accountType:String ="",val commandType:String="",val alarmTime:String ="0")