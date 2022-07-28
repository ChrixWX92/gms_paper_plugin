package com.gms.paper.data;

import com.gms.paper.MCServer;
import com.gms.paper.util.Helper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Activity extends DBContent {
    public static class Type {
        public static String s_tickets = "tickets";
        public static String s_subject = "subject";
        public static String s_lesson = "lesson";
        public static String s_childAdded = "child_added";
        public static String s_childDeleted = "child_deleted";
    }

    public String type;
    public Date timeEnter;
    public Date timeExit;
    public JsonObject metadata;

    public boolean hasEnded = false;

    public static Activity begin(String type, Map<String, String> meta) {
        if (meta != null) {
            JsonObject metadata = new JsonObject();

            for (Map.Entry<String, String> metaArg : meta.entrySet())
                metadata.addProperty(metaArg.getKey(), metaArg.getValue());

            return begin(type, metadata);
        }

        return begin(type, (JsonObject)null);
    }

    public static Activity begin(String type, String[][] metadata_) {
        if (metadata_ != null) {
            Map<String, String> meta = Stream.of(metadata_).collect(Collectors.toMap(data -> data[0], data -> data[1]));
            return begin(type, meta);
        }

        return begin(type, (JsonObject)null);
    }

    public void tick() {
        MCServer.getAPI().put(String.format("activities/%s", _id), null);
    }

    public static Activity begin(String type, JsonObject metadata) {
        JsonObject body = new JsonObject();
        body.addProperty("type", type);

        if (metadata == null)
            metadata = new JsonObject();

        body.add("metadata", metadata);
        String jsonResult = MCServer.getAPI().post("activities-begin", body);
        Activity activity = (new Gson()).fromJson(jsonResult, Activity.class);

        return activity;
    }

    public void end() throws RuntimeException {
        if (Helper.isLocalDev())
            return;

        if (_id.isEmpty())
            throw new RuntimeException("Invalid activity with no valid _id.");

        if (hasEnded)
            throw new RuntimeException("Trying to end an activity3 that has already ended.");

        JsonObject body = new JsonObject();
        MCServer.getAPI().put(String.format("activities-end/%s", _id), null);

        hasEnded = true;
    }

    public static void endAll(Date timestamp) {
        if (Helper.isLocalDev())
            return;

        if (timestamp == null)
            timestamp = new Date();

        long epochTimestampMS = timestamp.getTime();

        MCServer.getAPI().put("activities-end", new String[][] {
            { "timestamp", Long.toString(epochTimestampMS) }
        });
    }

    public static void beginSubject(Subject subject) {
    }
}
