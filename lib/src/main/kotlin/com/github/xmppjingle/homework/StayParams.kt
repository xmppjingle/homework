package com.github.xmppjingle.homework

data class StayParams(

    val durationInSec:Int,
    val hourOfDay: Int,
    val dayType:DayOfWeekType,
    val roundLocation:String

)

enum class DayOfWeekType {
    WORKDAY, WEEKEND
}

enum class PeriodOfDay {
    MORNING, AFTERNOON, EVENING, NIGHT
}
