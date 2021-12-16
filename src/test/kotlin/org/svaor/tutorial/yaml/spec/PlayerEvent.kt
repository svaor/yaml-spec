package org.svaor.tutorial.yaml.spec

import java.time.LocalTime

data class PlayerEvent(val time: LocalTime, val player: String, val action: String)
