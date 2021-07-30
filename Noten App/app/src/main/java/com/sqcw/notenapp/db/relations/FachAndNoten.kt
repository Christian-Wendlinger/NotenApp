package com.sqcw.notenapp.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.db.entities.Note

data class FachAndNoten(
    @Embedded val fach: Fach,
    @Relation(
        parentColumn = "id",
        entityColumn = "fachId"
    )
    val noten: List<Note>
)
