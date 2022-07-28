package com.gms.paper.util;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum TextFormat {
    BLACK('0', 0),
    DARK_BLUE('1', 1),
    DARK_GREEN('2', 2),
    DARK_AQUA('3', 3),
    DARK_RED('4', 4),
    DARK_PURPLE('5', 5),
    GOLD('6', 6),
    GRAY('7', 7),
    DARK_GRAY('8', 8),
    BLUE('9', 9),
    GREEN('a', 10),
    AQUA('b', 11),
    RED('c', 12),
    LIGHT_PURPLE('d', 13),
    YELLOW('e', 14),
    WHITE('f', 15),
    MINECOIN_GOLD('g', 22),
    OBFUSCATED('k', 16, true),
    BOLD('l', 17, true),
    STRIKETHROUGH('m', 18, true),
    UNDERLINE('n', 19, true),
    ITALIC('o', 20, true),
    RESET('r', 21);

    public static final char ESCAPE = '§';
    private static final Pattern CLEAN_PATTERN = Pattern.compile("(?i)§[0-9A-GK-OR]");
    private static final Map<Integer, TextFormat> BY_ID = Maps.newTreeMap();
    private static final Map<Character, TextFormat> BY_CHAR = new HashMap();
    private final int intCode;
    private final char code;
    private final boolean isFormat;
    private final String toString;

    private TextFormat(char code, int intCode) {
        this(code, intCode, false);
    }

    private TextFormat(char code, int intCode, boolean isFormat) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.toString = new String(new char[]{'§', code});
    }

    public static TextFormat getByChar(char code) {
        return (TextFormat)BY_CHAR.get(code);
    }

    public static TextFormat getByChar(String code) {
        return code != null && code.length() > 1 ? (TextFormat)BY_CHAR.get(code.charAt(0)) : null;
    }

    public static String clean(String input) {
        return clean(input, false);
    }

    public static String clean(String input, boolean recursive) {
        if (input == null) {
            return null;
        } else {
            String result = CLEAN_PATTERN.matcher(input).replaceAll("");
            return recursive && CLEAN_PATTERN.matcher(result).find() ? clean(result, true) : result;
        }
    }

    public static String colorize(char altFormatChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altFormatChar && "0123456789AaBbCcDdEeFfGgKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static String colorize(String textToTranslate) {
        return colorize('&', textToTranslate);
    }

    public static String getLastColors(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();

        for(int index = length - 1; index > -1; --index) {
            char section = input.charAt(index);
            if (section == 167 && index < length - 1) {
                char c = input.charAt(index + 1);
                TextFormat color = getByChar(c);
                if (color != null) {
                    result.insert(0, color.toString());
                    if (color.isColor() || color.equals(RESET)) {
                        break;
                    }
                }
            }
        }

        return result.toString();
    }

    public char getChar() {
        return this.code;
    }

    public String toString() {
        return this.toString;
    }

    public boolean isFormat() {
        return this.isFormat;
    }

    public boolean isColor() {
        return !this.isFormat && this != RESET;
    }

    static {
        TextFormat[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            TextFormat color = var0[var2];
            BY_ID.put(color.intCode, color);
            BY_CHAR.put(color.code, color);
        }

    }
