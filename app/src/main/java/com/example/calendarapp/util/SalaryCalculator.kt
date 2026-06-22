package com.example.calendarapp.util

import com.example.calendarapp.data.model.WorkPlace
import com.example.calendarapp.data.model.WorkShift
import java.util.Calendar

object SalaryCalculator {

    data class SalaryResult(
        val grossPay: Double,
        val netPay: Double,
        val normalMinutes: Long,
        val nightMinutes: Long,
        val holidayMinutes: Long
    )

    fun calculateShiftPay(shift: WorkShift, workPlace: WorkPlace, deductTax: Boolean): SalaryResult {
        var normalMinutes = 0L
        var nightMinutes = 0L
        var holidayMinutes = 0L

        var current = shift.startDateTime
        val end = shift.endDateTime

        while (current < end) {
            val cal = Calendar.getInstance().apply { timeInMillis = current }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val isHoliday = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

            val isNight = hour >= 22 || hour < 5

            val minuteEnd = minOf(current + 60_000L, end)
            val minuteDuration = (minuteEnd - current) / 60_000L

            when {
                isNight && isHoliday -> {
                    if (workPlace.useHigherWage) {
                        val nightRate = workPlace.nightWage ?: workPlace.baseWage
                        val holidayRate = workPlace.holidayWage ?: workPlace.baseWage
                        if (nightRate >= holidayRate) nightMinutes += minuteDuration
                        else holidayMinutes += minuteDuration
                    } else {
                        nightMinutes += minuteDuration
                        holidayMinutes += minuteDuration
                    }
                }
                isNight -> nightMinutes += minuteDuration
                isHoliday -> holidayMinutes += minuteDuration
                else -> normalMinutes += minuteDuration
            }

            current = minuteEnd
        }

        val workMinutes = (end - shift.startDateTime) / 60_000L - shift.breakMinutes
        val actualWorkMinutes = maxOf(0L, workMinutes)

        val totalMinutes = normalMinutes + nightMinutes + holidayMinutes
        val ratio = if (totalMinutes > 0) actualWorkMinutes.toDouble() / totalMinutes else 0.0

        val adjNormal = (normalMinutes * ratio).toLong()
        val adjNight = (nightMinutes * ratio).toLong()
        val adjHoliday = (holidayMinutes * ratio).toLong()

        val normalPay = adjNormal / 60.0 * workPlace.baseWage
        val nightPay = adjNight / 60.0 * (workPlace.nightWage ?: workPlace.baseWage)
        val holidayPay = adjHoliday / 60.0 * (workPlace.holidayWage ?: workPlace.baseWage)

        val grossPay = normalPay + nightPay + holidayPay

        val netPay = if (deductTax) {
            estimateNetPay(grossPay)
        } else {
            grossPay
        }

        return SalaryResult(
            grossPay = grossPay,
            netPay = netPay,
            normalMinutes = adjNormal,
            nightMinutes = adjNight,
            holidayMinutes = adjHoliday
        )
    }

    private fun estimateNetPay(gross: Double): Double {
        val annualGross = gross * 12
        val socialInsurance = annualGross * 0.15
        val taxableIncome = annualGross - socialInsurance - 480000
        val incomeTax = when {
            taxableIncome <= 1_950_000 -> taxableIncome * 0.05
            taxableIncome <= 3_300_000 -> taxableIncome * 0.10 - 97_500
            taxableIncome <= 6_950_000 -> taxableIncome * 0.20 - 427_500
            else -> taxableIncome * 0.23 - 636_000
        }
        val monthlyTax = maxOf(0.0, incomeTax / 12)
        val monthlySocialInsurance = socialInsurance / 12
        return gross - monthlyTax - monthlySocialInsurance
    }

    fun calculateMonthlyPay(
        shifts: List<WorkShift>,
        workPlaces: Map<Int, WorkPlace>,
        deductTax: Boolean
    ): Double {
        return shifts.sumOf { shift ->
            val workPlace = workPlaces[shift.workPlaceId] ?: return@sumOf 0.0
            calculateShiftPay(shift, workPlace, deductTax).netPay
        }
    }
}
