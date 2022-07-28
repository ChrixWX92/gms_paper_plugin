package com.gms.paper.usability;

import com.gms.paper.util.TextFormat;

import java.util.ArrayList;
import java.util.List;

public class ProfanityFilter {

    static List<String> knownProfanities = Profanities.englishProfanities;

    static int largestWordLength = 0;

    public static ArrayList<String> profanitiesFound(String input) {
        if(input == null) {
            return new ArrayList<>();
        }

        // Leetspeak
        input = input.replaceAll("1","i");
        input = input.replaceAll("!","i");
        input = input.replaceAll("3","e");
        input = input.replaceAll("4","a");
        input = input.replaceAll("@","a");
        input = input.replaceAll("5","s");
        input = input.replaceAll("7","t");
        input = input.replaceAll("0","o");
        input = input.replaceAll("9","g");

        for (String word : knownProfanities) {
            if (word.length() > largestWordLength) largestWordLength = word.length();
        }

        ArrayList<String> badWords = new ArrayList<>();
        input = input.toLowerCase().replaceAll("[^a-zA-Z]", "");

        for(int start = 0; start < input.length(); start++) {
            for(int offset = 1; offset < (input.length()+1 - start) && offset < largestWordLength; offset++)  {
                String wordToCheck = input.substring(start, start + offset);
                if(knownProfanities.contains(wordToCheck)) {
                    boolean ignore = false;
                    // Change specific examples here
                    if(!ignore) {
                        badWords.add(wordToCheck);
                    }
                }
            }
        }

        return badWords;

    }

    public static String filterText(String input, String username) {
        ArrayList<String> badWords = profanitiesFound(input);
        if(badWords.size() > 0) {
            return TextFormat.GOLD + "This action was prevented, as a profanity was detected. If you believe this word should not be censored, please contact us.";
        }
        //TODO: Talk to backend with username to flag profanity use
        return input;
    }




}
