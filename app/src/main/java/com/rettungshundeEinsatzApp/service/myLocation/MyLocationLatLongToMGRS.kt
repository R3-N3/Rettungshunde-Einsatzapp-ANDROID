package com.rettungshundeEinsatzApp.service.myLocation

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class MyLocationLatLongToMGRS {

    fun convert(lat: Double, long: Double): String {
        if (lat < -80) return "Too far South"
        if (lat > 84) return "Too far North"

        val c = 1 + floor((long + 180) / 6)
        val e = c * 6 - 183
        val k = lat * Math.PI / 180
        val l = long * Math.PI / 180
        val m = e * Math.PI / 180
        val n = cos(k)
        val o = 0.006739496819936062 * n.pow(2.0)
        val p = 40680631590769 / (6356752.314 * sqrt(1 + o))
        val q = tan(k)
        val r = q * q
        val t = l - m
        val u = 1.0 - r + o
        val v = 5.0 - r + 9 * o + 4.0 * (o * o)
        val w = 5.0 - 18.0 * r + (r * r) + 14.0 * o - 58.0 * r * o
        val x = 61.0 - 58.0 * r + (r * r) + 270.0 * o - 330.0 * r * o
        val y = 61.0 - 479.0 * r + 179.0 * (r * r) - (r * r * r)
        val z = 1385.0 - 3111.0 * r + 543.0 * (r * r) - (r * r * r)

        var aa = p * n * t + (p / 6.0 * n.pow(3.0) * u * t.pow(3.0)) +
                (p / 120.0 * n.pow(5.0) * w * t.pow(5.0)) +
                (p / 5040.0 * n.pow(7.0) * y * t.pow(7.0))

        var ab = 6367449.14570093 * (k - (0.00251882794504 * sin(2 * k)) +
                (0.00000264354112 * sin(4 * k)) -
                (0.00000000345262 * sin(6 * k)) +
                (0.000000000004892 * sin(8 * k))) +
                (q / 2.0 * p * n.pow(2.0) * t.pow(2.0)) +
                (q / 24.0 * p * n.pow(4.0) * v * t.pow(4.0)) +
                (q / 720.0 * p * n.pow(6.0) * x * t.pow(6.0)) +
                (q / 40320.0 * p * n.pow(8.0) * z * t.pow(8.0))

        aa = aa * 0.9996 + 500000.0
        ab *= 0.9996
        if (ab < 0.0) ab += 10000000.0

        val ad = "CDEFGHJKLMNPQRSTUVWXX"[floor(lat / 8 + 10).toInt()]
        val ae = floor(aa / 100000).toInt()
        val af = arrayOf("ABCDEFGH", "JKLMNPQR", "STUVWXYZ")[(c - 1).toInt() % 3][ae - 1]
        val ag = floor(ab / 100000).toInt() % 20
        val ah = arrayOf("ABCDEFGHJKLMNPQRSTUV", "FGHJKLMNPQRSTUVABCDE")[(c - 1).toInt() % 2][ag]

        fun pad(value: Int): String {
            return when {
                value < 10 -> "0000$value"
                value < 100 -> "000$value"
                value < 1000 -> "00$value"
                value < 10000 -> "0$value"
                else -> value.toString()
            } }

        aa = floor(aa % 100000).toInt().toDouble()
        ab = floor(ab % 100000).toInt().toDouble()
        return "${c.toInt()}$ad $af$ah ${pad(aa.toInt())} ${pad(ab.toInt())}"
    }

}