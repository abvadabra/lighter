package ru.redenergy.lighter

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import org.yanex.telegram.Message
import org.yanex.telegram.Response
import org.yanex.telegram.TelegramBot
import org.yanex.telegram.User
import retrofit.RestAdapter
import ru.redenergy.lighter.handler.ChatListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Mod(modid = "lighter", modLanguageAdapter = "io.drakon.forgelin.KotlinAdapter")
object Lighter {

    val threadPool = Executors.newFixedThreadPool(2)

    var token = "unknown"
    var chatId = "-1"
    var blockedUsers = arrayListOf<Long>();

    lateinit var bot: TelegramBot
        public get
        private get
    lateinit var config: Configuration
        public get
        private get

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent){
        config = Configuration(event.suggestedConfigurationFile)
        this.token = config.getString("bot-token", "Telegram", "unknown", "Bot Token")
        this.chatId = config.getString("chat-id", "Telegram", "unknown", "Chat ID")
        this.blockedUsers = config.getStringList("blocked-users", "Telegram", arrayOf<String>(), "User ids which can't send message through Telegram")
                            .map { it.toLong() }
                            .toArrayList()
        config.save()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent){
        bot = TelegramBot(token, RestAdapter.LogLevel.NONE)
        setupListeningThread()
        MinecraftForge.EVENT_BUS.register(ChatListener(chatId))
    }

    fun handleIngameMessage(player: EntityPlayer, message: String){
        threadPool.submit {
            val text = "<${player.displayName}> $message"
            bot.sendMessage(chatId = chatId, text = text)
        }
    }

    fun setupListeningThread(){
        Thread({ bot.listen {
            try {
                handleTelegramMessage(it)
            } catch(ex: Exception) {ex.printStackTrace()}
        }}).start()
    }

    fun handleTelegramMessage(message: Message){
        val text = message.text!!
        if(text.startsWith("/id")){
            bot.sendMessage(chatId = message.chat.id.toString(), text = "Chat ID: " + message.chat.id)
        } else {
            val sender = message.from ?: return
            if(!canPublishMessage(sender)) return
            publishTelegramMessage(sender.userName ?: return, text)
        }
    }

    fun publishTelegramMessage(sender: String, text: String){
        val text = "<TL> <$sender> $text"
        MinecraftServer.getServer().configurationManager.playerEntityList.forEach {
            (it as EntityPlayer).addChatMessage(ChatComponentText(text))
        }
    }

    fun canPublishMessage(user: User): Boolean = !this.blockedUsers.contains(user.id)

}