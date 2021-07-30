package com.sqcw.notenapp.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.db.entities.MetaInformation

data class MetaInformationAndFaecher(
    @Embedded val metaInformation: MetaInformation,
    @Relation(
        parentColumn = "halbjahr",
        entityColumn = "halbjahr"
    )
    val faecher: List<Fach>
)
