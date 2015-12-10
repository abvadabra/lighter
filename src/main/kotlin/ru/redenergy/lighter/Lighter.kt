package ru.redenergy.lighter

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import org.yanex.telegram.TelegramBot
import retrofit.RestAdapter
import ru.redenergy.lighter.handler.ChatListener

@Mod(modid = "lighter", modLanguageAdapter = "io.drakon.forgelin.KotlinAdapter")
object Lighter {

    var token = "unknown"
    var chatId = "-1"

    lateinit var bot: TelegramBot
        public get
        private get

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent){
        val config = Configuration(event.suggestedConfigurationFile)
        this.token = config.getString("bot-token", "Telegram", "unknown", "Bot Token")
        this.chatId = config.getString("chat-id", "Telegram", "unknown", "Bot Token")
        config.save()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent){
        bot = TelegramBot(token, RestAdapter.LogLevel.NONE)
        Thread({
            bot.listen { action ->
                println(action)
                try {
                    val text = "<TL> <${action.from?.userName}>: ${action.text}"
                    MinecraftServer.getServer().configurationManager.playerEntityList
                            .forEach { (it as EntityPlayer).addChatMessage(ChatComponentText(text)) }
                } catch(ex: Exception){ex.printStackTrace()}
            }
        }).start()
        MinecraftForge.EVENT_BUS.register(ChatListener(chatId))
    }

}