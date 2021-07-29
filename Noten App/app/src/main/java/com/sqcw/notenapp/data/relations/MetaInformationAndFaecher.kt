package com.sqcw.notenapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation

data class MetaInformationAndFaecher(
    @Embedded val metaInformation: MetaInformation,
    @Relation(
        parentColumn = "halbjahr",
        entityColumn = "halbjahr"
    )
    val faecher: List<Fach>
)
