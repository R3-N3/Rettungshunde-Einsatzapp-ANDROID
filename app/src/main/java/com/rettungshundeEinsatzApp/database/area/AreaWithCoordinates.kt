package com.rettungshundeEinsatzApp.database.area

import androidx.room.Embedded
import androidx.room.Relation

data class AreaWithCoordinates(
    @Embedded val area: AreaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "areaId"
    )
    val coordinates: List<AreaCoordinateEntity>
)