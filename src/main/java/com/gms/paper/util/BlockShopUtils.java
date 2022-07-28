package com.gms.paper.util;

import cn.nukkit.utils.Utils;
import com.gms.mc.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class BlockShopUtils extends Utils {
    public static HashMap<String, String> getPrices() throws IOException {
        File root = new File(Main.s_plugin.getServer().getDataPath(), "reference");
        File filepath = new File(root, "Prices.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            HashMap<String, String> prices = new HashMap<>();
            String line = br.readLine();

            while (line != null) {
                if (!line.isEmpty()) {
                    String[] attrs = line.split(",");
                    if (attrs.length == 2) {
                        prices.put(attrs[0], attrs[1]);
                    }
                }
                line = br.readLine();
            }

            return prices;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        }
    }
}

