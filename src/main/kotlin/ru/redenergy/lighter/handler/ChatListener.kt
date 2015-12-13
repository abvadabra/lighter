package ru.redenergy.lighter.handler

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.event.ServerChatEvent
import ru.redenergy.lighter.Lighter

class ChatListener(val chatId: String) {

    @SubscribeEvent
    fun onChatAction(event: ServerChatEvent) {
        Lighter.handleIngameMessage(event.player, event.message)
    }

}
