package dev.protonova

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import kotlin.math.round

@Suppress("Unused")
object TpsMeter : ModInitializer {
    private val logger = LoggerFactory.getLogger("tps-meter")
	private val bossBar = ServerBossBar(Text.literal("TPS"), BossBar.Color.GREEN, BossBar.Style.NOTCHED_20)

	override fun onInitialize() {
		logger.info("Hello Fabric world!")

		CommandRegistrationCallback.EVENT.register {dispatcher, _, _ ->
			dispatcher.register(CommandManager.literal("tpsmeter").executes { context ->
				val player = context.source.player
				if (player !is ServerPlayerEntity) {
					context.source.sendFeedback({ Text.literal("/tpsmeter must be run by a player!")}, false)
					return@executes 0
				}

				if (player !in bossBar.players) bossBar.addPlayer(player) else bossBar.removePlayer(player)

				context.source.sendFeedback({ Text.literal("Toggled TPS Meter!") }, false)

				return@executes 1
			})
		}

		ServerTickEvents.END_SERVER_TICK.register { server ->
			val tps = if (1000f / server.averageTickTime < 20) 1000f / server.averageTickTime else 20f

			bossBar.name = Text.literal("${round(tps * 10) / 10} TPS")
			bossBar.percent = tps / 20f

			if (tps > 17) {
				bossBar.color = BossBar.Color.GREEN
			} else if (tps > 10) {
				bossBar.color = BossBar.Color.YELLOW
			} else {
				bossBar.color = BossBar.Color.RED
			}
		}
	}
}