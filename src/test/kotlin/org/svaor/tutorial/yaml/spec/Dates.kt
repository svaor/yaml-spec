package org.svaor.tutorial.yaml.spec

import com.fasterxml.jackson.annotation.JsonFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class Dates(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.S'Z'") val canonical: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd't'HH:mm:ss.SSz") val iso8601: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SS X") val spaced: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd") val date: LocalDate,
)