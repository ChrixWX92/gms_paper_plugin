package com.gms.paper.interact.puzzles.utils;

import com.gms.mc.util.Log;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextToImage {

    public TextToImage(){}

    public static String fmError = "Error acquiring font metrics during image generation.";

    public static BufferedImage generate(String text, boolean transparent) {
        return generate(text, 0, 0, transparent, false);
    }


    public static BufferedImage generate(String text, int x, int y, boolean transparent, boolean custom) {
    /*
       Because font metrics is based on a graphics context, we need to create
       a small, temporary image so we can ascertain the width and height
       of the final image
     */

        //int size = 48;
        //if (text.length() > 3) {
        //    size = (int) Math.floor(48F/(text.length()-3F));
        //}

        String[] lines = textSplitter(text, 18, " ");
        String longestString = longestString(lines);

        int width;
        int height = custom ? y : 200;

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        FontMetrics fm = getFontMetrics(g2d);
        if (fm == null) return null;
        Font font = fm.getFont();

        //UNIFONT ASPECT = ~0.52 (best estimate)
        //ARIAL ASPECT = 0.46
        if (!custom) {
            if (longestString.length() < 3) {
                if (longestString.length() == 0) {
                    Log.error("Text length should never be zero");
                }
                width = fm.stringWidth(longestString) * (3 / longestString.length());
            } else width = fm.stringWidth(longestString + 2); // 2 is the margin
        } else {
            width = x == 0 ? y : x; // when x is zero, Graphics2D does not set rendering and exits
        }

        g2d.dispose();

        //Log.debug(text + " = " + String.valueOf(fm.stringWidth(text)));

        if (transparent) img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        else img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);

        if (transparent) {
            g2d.setColor(Color.WHITE);
            int margin = 0;
            int yMargin = 0;
            if (longestString.length() == 1) {
                margin = Math.floorDiv((int) Math.floor(img.getHeight()*0.46), 2);
            }
            if (longestString.length() == 2) {
                margin = Math.floorDiv((int) Math.floor(img.getHeight()*0.92), 2);
            }
            if (longestString.length() > 2) {
                int maxMargin = (int) Math.floor(128*0.46);
                for (int i = 0 ; i < longestString.length() ; i++){
                    maxMargin = (int) Math.floor(maxMargin*0.46);
                }
                yMargin = 64-maxMargin;
            }

            g2d.fillRect(margin, yMargin, 128-margin, 128-yMargin); //TODO Altering these to always be within certain parameters should fix our issues - make y start and end points contingent on text
            g2d.setColor(Color.BLACK);
            //Log.debug(text + " = " + margin + ", " + yMargin + ", " + (128-margin) + ", " + (128-yMargin));
        }

        for(int i = 0; i < lines.length; i++){
            float xOffset = (int) (width/2F - fm.stringWidth(lines[i])/2F);
            float yOffset = (int) ((height/2F) - ((lines.length/2F - i)*(fm.getAscent())) + fm.getAscent()*0.8);
            
            g2d.drawString(lines[i], xOffset, yOffset); //g2d.drawString(text, 0, fm.getAscent()); //  g2d.drawString(text, (int)(fm.getAscent()*0.46), fm.getAscent());

        }
        g2d.dispose();

        /*
        try {
            ImageIO.write(img, "png", new File("Text.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

        return img;

    }

    public static String[] textSplitter(String text, int maxCharacters, String splitIdentifier) {
        String[] allLines = text.split(splitIdentifier);
        StringBuilder textWithLineBreaks = new StringBuilder();
        StringBuilder charLimit = new StringBuilder();

        for (int i = 0; i < allLines.length; i++) {
            if (charLimit.length() + allLines[i].length() < maxCharacters) {
                charLimit.append(allLines[i]);

                if ((i + 1 != allLines.length)) {
                    charLimit.append(" ");
                }
            }

            else{
                textWithLineBreaks.append(charLimit).append("/n");
                charLimit = new StringBuilder();
                i--;
            }
        }

        textWithLineBreaks.append(charLimit);

        return textWithLineBreaks.toString().split("/n");
    }


    public static FontMetrics getFontMetrics(Graphics2D g2d) { // Passing by value!
        FontMetrics fm;
        try {
            Font font = new Font("Unifont", Font.PLAIN, 48); //Default size = 48
            g2d.setFont(font);
            fm = g2d.getFontMetrics();
        } catch (Exception e) {
            Log.error("Unifont unavailable on host machine. Switching to Arial font metrics.");

            try { // Nested try/catches are nasty
                Font font = new Font("Arial", Font.PLAIN, 48);
                g2d.setFont(font);
                fm = g2d.getFontMetrics();
            } catch (Exception ex) {
                Log.error("Arial unavailable on host machine. Please install Unifont or Arial to allow puzzle generation.");
                return null;
            }
        }
        return fm;
    }

    public static String longestString(String[] strings) {

        int length = 0;
        String longestString = "";

        for (String string : strings)
            if (string.length() > length){
                length = string.length();
                longestString = string;
            }

        return longestString;

    }

}
