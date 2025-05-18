package com.rettungshundeEinsatzApp.functions

import org.osmdroid.util.GeoPoint
import kotlin.math.*

fun calculatePolygonArea(points: List<GeoPoint>): Double {
    if (points.size < 3) return 0.0

    val earthRadius = 6378137.0 // in meters
    var area = 0.0

    for (i in points.indices) {
        val p1 = points[i]
        val p2 = points[(i + 1) % points.size]

        val lat1 = Math.toRadians(p1.latitude)
        val lon1 = Math.toRadians(p1.longitude)
        val lat2 = Math.toRadians(p2.latitude)
        val lon2 = Math.toRadians(p2.longitude)

        area += (lon2 - lon1) * (2 + sin(lat1) + sin(lat2))
    }

    area = area * earthRadius * earthRadius / 2.0
    return abs(area) // in m²
}