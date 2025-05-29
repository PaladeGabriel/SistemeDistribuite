package com.sd.laborator.services

import com.sd.laborator.interfaces.LocationSearchInterface
import com.sd.laborator.interfaces.WeatherForecastInterface
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class WeatherOrchestrator{
    @Autowired
    private lateinit var locationSearchService: LocationSearchInterface

    @Autowired
    private lateinit var weatherForecastService: WeatherForecastInterface

    @Autowired
    private lateinit var filterLocationService: FilterLocationService

    fun getWeatherForecast(location: String): String {
        if (filterLocationService.location_acces() == -1) {
            return "Nu puteti accesa"
        }

        val locationId = locationSearchService.getLocationId(location)

        if (locationId == "-1,-1") {
            return "Nu s-au putut gasi date meteo pentru  \"$location\"!"
        }


        val forecastData = weatherForecastService.getForecastData(locationId)


        return forecastData.toString()
    }

}

//Ar fi trebuit ca fiecare serviciu folosit aici sa fie un proiect separat,fiecare pe port personal