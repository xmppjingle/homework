package com.github.xmppjingle

import com.github.xmppjingle.geo.CityNames
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class CityNamesNormalizationTest {

    @Test
    fun `simple city name test`() {
        val m = CityNames.createNormalizedCityNames(File("src/main/resources/geonames-worldwide.csv"))

        Assertions.assertEquals("The Hague".lowercase(), m.getCityName("NL", "den haag"))
        Assertions.assertEquals("Uberlandia".lowercase(), m.getCityName("BR","uberlândia"))
        Assertions.assertEquals("Uberlandia".lowercase(), m.getCityName("BR","uberlandija"))

    }


}