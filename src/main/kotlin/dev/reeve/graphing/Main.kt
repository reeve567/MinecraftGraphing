package dev.reeve.graphing

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

class Main : JavaPlugin() {
	val configFile = File(dataFolder, "config.json")
	lateinit var config: Config
	var points = mutableListOf<Vec3D>()
	val roundValue = 40.0
	
	
	override fun onEnable() {
		if (!dataFolder.exists()) {
			dataFolder.mkdirs()
		}
		
		if (!configFile.exists()) {
			configFile.createNewFile()
		}
		config = Gson().fromJson(configFile.readText(), Config::class.java)
		
		if (points.isEmpty()) {
			getPoints(config)
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
			if (config.origin == null)
				return@scheduleSyncRepeatingTask
			
			for (point in points) {
				val p = config.origin!!.toLocation(Bukkit.getWorld("world")!!).add(point.x, point.z, point.y)
				//Particle.DustOptions(Color.RED, 1f)
				p.world!!.spawnParticle(Particle.SUSPENDED, p, 1)
			}
		}, 5, 5)
	}
	
	override fun onDisable() {
		configFile.writeText(GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(config))
	}
	
	private fun getPoints(config: Config) {
		points = mutableListOf()
		
		val pointAxisCount = (config.size / config.interval).toInt()
		for (x in -pointAxisCount..pointAxisCount) {
			for (y in -pointAxisCount..pointAxisCount) {
				for (z in -pointAxisCount..pointAxisCount) {
					val value = getValue(x * config.interval, y * config.interval, z * config.interval)
					if (value != null) {
						points.add(Vec3D(x * config.interval * config.size,
							y * config.interval * config.size,
							z * config.interval * config.size))
					}
				}
			}
		}
		
	}
	
	private fun getValue(x: Double, y: Double, z: Double): Double? {
		val checkValue = 0.0
		
		
		val value = x*x - y*y - z
		
		val roundedValue = (value * roundValue).toInt() / roundValue
		
		return if (checkValue == null) value else if (roundedValue == checkValue) value else null
	}
	
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			if (command.label == "setorigin") {
				if (args.isEmpty()) {
					config.origin = sender.location.toOriginLocation()
					
					getPoints(config)
				} else
					config.origin = Config.OriginLocation(args[0].toDouble(), args[1].toDouble(), args[2].toDouble())
			} else if (command.label == "setsize") {
				if (args.isNotEmpty()) {
					config.size = args[0].toDouble()
					getPoints(config)
				}
			} else if (command.label == "setinterval") {
				if (args.isNotEmpty()) {
					config.interval = args[0].toDouble()
					getPoints(config)
				}
			} else if (command.label == "remakepoints") {
				getPoints(config)
			} else if (command.label == "settings") {
				sender.sendMessage("Size: ${config.size}\nInterval: ${config.interval}\nRound value: $roundValue")
			}
			return true
		}
		return false
	}
}