package com.yourdomain.voicescribe.core.data.local.db

import androidx.room.TypeConverter

/** Only primitives are stored directly by entities in this schema, so this
 * class is intentionally minimal — kept as the single extension point if a
 * future entity needs a richer converted type (e.g. an enum column). */
class Converters {
    @TypeConverter
    fun fromNullableFloat(value: Float?): Float? = value

    @TypeConverter
    fun toNullableFloat(value: Float?): Float? = value
}
