package com.gms.paper.interact.puzzles.utils;

import com.gms.mc.util.Log;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextCleaner {

    private final String sourceText;
    private final String cleanedText;
    private final String cleanedASCII;
    private final String cleanedLatin;

    // Watch the order
    private final Map<String, String> substitutions = Stream.of(new String[][] {
        {"¬", ","}, // Comma - contents creators use the NOT SIGN as an escape character for commas, due to their role as delimiters in CSVs (Unicode: U+002C, ASCII: 44, hex: 0x2C)
        {"¦", "\n"}, // New Line, the Carriage Return of which is later removed (Unicode: (U+000D, U+000A), ASCII: (13, 10), hex: (0x0D, 0x0A) (two values - one for Line Feed, the other for Carriage Return))
        {"\r", ""}, // Carriage Return (Unicode: U+000D, ASCII: 13, hex: 0x0d)
        {"‘", "'"}, // Left single quotation marks (Unicode: U+2018, ASCII: N/A, hex:0xE2 0x80 0x98 (e28098))
        {"’", "'"}, // Right single quotation marks (Unicode: U+2019, ASCII: N/A, hex: 0xE2 0x80 0x99 (e28099))
        {"…", "..."}, // Horizontal ellipsis (Unicode: U+2026, ASCII: N/A, hex: 0xE2 0x80 0xA6 (e280a6))
        {"–", "-"}, // En Dash (Unicode: U+2013, ASCII: N/A, hex: 0xE2 0x80 0x93 (e28093))
        {"“", "\""}, // Left double quotation marks (Unicode: U+201C, ASCII: N/A, hex: 0xE2 0x80 0x9C (e2809c))
        {"”", "\""}, // Right double quotation marks (Unicode: U+201D, ASCII: N/A, hex: 0xE2 0x80 0x9D (e2809d))
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public TextCleaner(String sourceText){
        this.sourceText = sourceText;
        this.cleanedText = cleanText(sourceText, StandardCharsets.UTF_8);
        this.cleanedASCII = cleanText(sourceText, StandardCharsets.US_ASCII);
        this.cleanedLatin = cleanText(sourceText, StandardCharsets.ISO_8859_1);
    }

    private String cleanText(String sourceText, Charset charset){
        String cleanText = sourceText;

        if (sourceText.contains("\"")) cleanText = fixDoubleQuotes(sourceText);

        for (Map.Entry<String, String> e : substitutions.entrySet()) {
            if (cleanText.contains(e.getKey())) cleanText = cleanText.replaceAll(e.getKey(), e.getValue());
        }

        switch (charset.name()){
            case "UTF-8" -> {
                return utf8Encoder(cleanText);
            }
            case "US-ASCII" -> {
                return asciiEncoder(cleanText);
            }
            case "ISO-8859-1" -> {
                return latinEncoder(cleanText);
            }
            default -> {
                Log.error("Invalid or unsupported charset passed to TextCleaner.");
                return sourceText;
            }
        }

    }

    private String fixDoubleQuotes(String sourceText) {
        String newText = sourceText;
        StringBuilder sb = new StringBuilder(sourceText);
        if (sourceText.contains("\"")) {
            if (sourceText.charAt(0) ==  '\"' && sourceText.charAt(sourceText.length()-1) ==  '\"') {
                sb.deleteCharAt(sourceText.length()-1);
                sb.deleteCharAt(0);
                newText = sb.toString();
                newText = newText.replaceAll("\"\"", "\"");
            }
        }
        return newText;
    }

    private String utf8Encoder(String rawString){
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(rawString);
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    private String asciiEncoder(String rawString){
        ByteBuffer buffer = StandardCharsets.US_ASCII.encode(rawString);
        return StandardCharsets.US_ASCII.decode(buffer).toString();
    }

    private String latinEncoder(String rawString){

        int[] sectionSigns = new int[StringUtils.countMatches(rawString, '§')];
        int index = 0;
        for (int i = 0; i < rawString.length(); i++) {
            if (rawString.toCharArray()[i] == '§') {
                sectionSigns[index] = i;
                index++;
            }
        }

        ByteBuffer buffer = StandardCharsets.ISO_8859_1.encode(rawString);
        StringBuilder sb = new StringBuilder(StandardCharsets.ISO_8859_1.decode(buffer).toString());

        for (int i : sectionSigns) {
            sb.setCharAt(i,'§');
        }

        return sb.toString();
    }

    public String getSourceText() { return sourceText; }
    public String getCleanedText() { return cleanedText; }
    public String getCleanedASCII() { return cleanedASCII; }
    public String getCleanedLatin() { return cleanedLatin; }
}
