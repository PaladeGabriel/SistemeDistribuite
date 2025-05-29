package com.sd.laborator.services

import org.json.JSONObject
import org.springframework.stereotype.Service
import java.net.URL

@Service
class FilterLocationService {

    //Iaşi
    val blacklist= listOf("Cluj","București")

    fun getLocation():String{

        val apiUrl = "https://ipinfo.io/json"
        val response = URL(apiUrl).readText()
        val jsonResponse = JSONObject(response)
        val location = jsonResponse.getString("city")

        return location
    }

    fun location_acces():Int
    {
        val location=getLocation()
        if(blacklist.contains(location))
            return -1
        else
            return 1
    }

}