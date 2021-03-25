package dev.reeve.graphing

import org.bukkit.Location
import org.bukkit.World

class Config(
	var origin: OriginLocation? = null,
	var interval: Double = 0.5,
	var size: Double = 2.5,
	var equation: String = "x^2 + y^2"
) {
	class OriginLocation(var x: Double, var y: Double, var z: Double)
}

fun Config.OriginLocation.toLocation(world: World): Location {
	return Location(world, x, y, z)
}

fun Location.toOriginLocation(): Config.OriginLocation {
	return Config.OriginLocation(x, y, z)
}