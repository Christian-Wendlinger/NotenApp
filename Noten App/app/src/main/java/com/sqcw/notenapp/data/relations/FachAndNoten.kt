package com.sqcw.notenapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.Note

data class FachAndNoten(
    @Embedded val fach: Fach,
    @Relation(
        parentColumn = "id",
        entityColumn = "fachId"
    )
    val noten: List<Note>
)
