package me.gemini.gravespro.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextUtil {

    public static Component parse(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public static String format(String text) {
        return LegacyComponentSerializer.legacySection().serialize(parse(text));
    }
}
