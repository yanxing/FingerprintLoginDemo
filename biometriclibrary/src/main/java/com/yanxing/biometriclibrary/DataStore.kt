package com.yanxing.biometriclibrary

import android.content.Context

/**
 * @author 李双祥 on 2021/3/11.
 */
fun saveInfo(context: Context,info:String){
    val preferences=context.getSharedPreferences("e",Context.MODE_PRIVATE)
    preferences.edit().putString("i",info).apply()
}

fun getInfo(context: Context):String{
    val preferences=context.getSharedPreferences("e",Context.MODE_PRIVATE)
    return preferences.getString("i","")!!
}

fun saveInfoIV(context: Context,infoIV:String){
    val preferences=context.getSharedPreferences("e",Context.MODE_PRIVATE)
    preferences.edit().putString("iiv",infoIV).apply()
}

fun getInfoIV(context: Context):String{
    val preferences=context.getSharedPreferences("e",Context.MODE_PRIVATE)
    return preferences.getString("iiv","")!!
}