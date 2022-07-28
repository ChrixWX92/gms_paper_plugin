package com.gms.paper.util;

import com.gms.paper.data.CmsApi;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;

public class Log {
    public static @NotNull ConsoleCommandSender s_console;
    private static String s_ipAddress = "0.0.0.0";

    public static void setIPAddress(String ipAddress) {
        s_ipAddress = ipAddress;
    }

    private static String formatMsg(TextFormat format, String msg) {
        String str = "[GMS@" + s_ipAddress + "] " + msg;
        return !Helper.isProd() ? format + str : str;
    }

    public static String debugMsg(String msg) {
        return formatMsg(TextFormat.GREEN, msg);
    }

    public static void debug(String msg) {
        s_console.sendMessage(debugMsg(msg));
    }

    public static String warnMsg(String msg) {
        return formatMsg(TextFormat.YELLOW, msg);
    }

    public static String infoMsg(String msg) {
        return formatMsg(TextFormat.AQUA, msg);
    }

    public static void warn(String msg) {
        s_console.sendMessage(warnMsg(msg));
    }

    public static void info(String msg) {
        s_console.sendMessage(infoMsg(msg));
    }

    public static String errorMsg(String msg) {
        return formatMsg(TextFormat.RED, msg);
    }
    public static String fatalMsg(String msg) {
        return formatMsg(TextFormat.BOLD, errorMsg(msg));
    }

    public static void error(String msg) {
        s_console.sendMessage(errorMsg(msg));
    }

    public static void exception(Throwable e, String msg) {
        s_console.sendMessage(fatalMsg(msg));
        s_console.sendMessage(fatalMsg(e.getMessage()));
        s_console.sendMessage(fatalMsg(Helper.getStackTrace(e)));
//        e.printStackTrace();
//        throw new RuntimeException(msg);
    }

    public static void logGeneric(CommandSender sender, String msg) {
        s_console.sendMessage(msg);
        sender.sendMessage(msg);
    }

    public static void logAndSend(CommandSender sender, String msg_) {
        String msg = errorMsg(msg_);

        s_console.sendMessage(msg);
        sender.sendMessage(msg);
    }

    public static void httpError(String url, HttpResponse<String> response, String jwt) throws RuntimeException {
        String body = response.body();
        int statusCode = response.statusCode();

        Log.error(String.format("Http request failed with code: %d. Details: \n\t - URL: %s \n\t - JWT: %s \n\t - Body: %s", statusCode, url, jwt, body));
    }
}

