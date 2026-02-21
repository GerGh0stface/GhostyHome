package de.ghosty.ghostyhome.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    // Matches &#RRGGBB or {#RRGGBB}
    private static final Pattern HEX_PATTERN_AMP   = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_BRACE = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");
    // Matches <#RRGGBB>
    private static final Pattern HEX_PATTERN_ANGLE = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    /**
     * Colorizes a string by replacing hex codes and & color codes.
     *
     * Supported hex formats:
     *   &#RRGGBB
     *   {#RRGGBB}
     *   <#RRGGBB>
     *
     * @param text The text to colorize.
     * @return The colorized text.
     */
    public static String colorize(String text) {
        if (text == null) return "";

        // Replace hex &#RRGGBB
        Matcher m1 = HEX_PATTERN_AMP.matcher(text);
        StringBuffer sb1 = new StringBuffer();
        while (m1.find()) {
            m1.appendReplacement(sb1, ChatColor.of("#" + m1.group(1)).toString());
        }
        m1.appendTail(sb1);
        text = sb1.toString();

        // Replace hex {#RRGGBB}
        Matcher m2 = HEX_PATTERN_BRACE.matcher(text);
        StringBuffer sb2 = new StringBuffer();
        while (m2.find()) {
            m2.appendReplacement(sb2, ChatColor.of("#" + m2.group(1)).toString());
        }
        m2.appendTail(sb2);
        text = sb2.toString();

        // Replace hex <#RRGGBB>
        Matcher m3 = HEX_PATTERN_ANGLE.matcher(text);
        StringBuffer sb3 = new StringBuffer();
        while (m3.find()) {
            m3.appendReplacement(sb3, ChatColor.of("#" + m3.group(1)).toString());
        }
        m3.appendTail(sb3);
        text = sb3.toString();

        // Replace & color codes
        text = ChatColor.translateAlternateColorCodes('&', text);

        return text;
    }

    /**
     * Strips all color codes from a string.
     */
    public static String strip(String text) {
        return ChatColor.stripColor(colorize(text));
    }
}
