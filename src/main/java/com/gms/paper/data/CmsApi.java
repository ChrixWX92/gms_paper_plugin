package com.gms.paper.data;

import com.gms.paper.util.Log;
import com.gms.paper.util.Helper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.utils.URIBuilder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmsApi {
    public String serverUrl = "http://localhost:1337/";
    private String _jwt = "";
    private HashMap<String, GenericContent> _content = new HashMap<>();

    public static CmsApi s_public = new CmsApi();
    private int _lastFailureStatus = 0;

    public CmsApi(String jwt) {
        _jwt = jwt;

        /// Try to get from the config
        String serverUrl = Helper.getEnv("GMS_CMS_SERVER", true);

        /// If we have a valid url, then we cache it
        if (serverUrl != null && !serverUrl.isEmpty())
            this.serverUrl = serverUrl;
    }

    public CmsApi(String serverUrl, String jwt) {
        _jwt = jwt;
        this.serverUrl = serverUrl;
    }

    public CmsApi() {
        this("");
    }
    protected void populateSecurityHeaders(HttpRequest.Builder builder) {
        if (_jwt != null && !_jwt.isEmpty())
            builder.header("Authorization", "Bearer " + _jwt);
    }

    public String getJWT() {
        return _jwt;
    }

    public int getLastFailureStatus() {
        return _lastFailureStatus;
    }

    public String post(String endPoint, JsonObject body) throws RuntimeException {
        try {
            String url = String.format("%s%s", serverUrl, endPoint);
            URIBuilder uri = new URIBuilder(url);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri.build())
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json");

            populateSecurityHeaders(builder);

            HttpRequest request = null;

            if (body != null)
                request = builder.POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
            else
                request = builder.POST(null).build();


            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode > 300) {
                _lastFailureStatus = statusCode;
                return null;
            }

            return response.body();
        }
        catch (Exception e) {
            Log.exception(e, String.format("POST request failed for end-point: %s", endPoint));
        }

        return "";
    }

    public String post(String endPoint, String[][] args_) throws RuntimeException {
        JsonObject body = null;

        if (args_ != null) {
            Map<String, String> args = Stream.of(args_).collect(Collectors.toMap(data -> data[0], data -> data[1]));

            body = new JsonObject();
            for (Map.Entry<String, String> arg : args.entrySet())
                body.addProperty(arg.getKey(), arg.getValue());
        }

        return post(endPoint, body);
    }

    boolean checkHttpError(String url, HttpResponse<String> response, String jwt) {
        if (!Helper.checkHttpError(url, response, jwt)) {
            _lastFailureStatus = response.statusCode();
            return false;
        }

        return true;
    }

    public String get(String endPoint, String[][] args_) throws RuntimeException {
        try {
            String url = String.format("%s%s", serverUrl, endPoint);
            URIBuilder uri = new URIBuilder(url);

            if (args_ != null) {
                Map<String, String> args = Stream.of(args_).collect(Collectors.toMap(data -> data[0], data -> data[1]));

                for (Map.Entry<String, String> arg : args.entrySet())
                    uri.addParameter(arg.getKey(), arg.getValue());
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri.build())
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json");

            populateSecurityHeaders(builder);

            HttpRequest request = builder.GET().build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (checkHttpError(url, response, _jwt))
                return response.body();

            return null;
        }
        catch (Exception e) {
            Log.exception(e, String.format("GET request failed for end-point: %s", endPoint));
        }

        return null;
    }

    public String put(String endPoint, String[][] args_) throws RuntimeException {
        try {
            String url = String.format("%s%s", serverUrl, endPoint);
            URIBuilder uri = new URIBuilder(url);

            if (args_ != null) {
                Map<String, String> args = Stream.of(args_).collect(Collectors.toMap(data -> data[0], data -> data[1]));

                for (Map.Entry<String, String> arg : args.entrySet())
                    uri.addParameter(arg.getKey(), arg.getValue());
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri.build())
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json");

            populateSecurityHeaders(builder);

            builder = builder.PUT(HttpRequest.BodyPublishers.noBody());
            HttpRequest request = builder.build();

//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(uri.build())
//                    .header("Content-Type", "application/json")
//                    .header("accept", "application/json")
//                    .header("Authorization", "Bearer " + s_jwt)
//                    .PUT(null)
//                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            checkHttpError(url, response, _jwt);

            return response.body();
        }
        catch (Exception e) {
            Log.exception(e, String.format("PUT request failed for end-point: %s [Base URL: %s]", endPoint, serverUrl));
        }

        return "";
    }

    public <T> T getId(String endPoint, String id, Class<T> cls) {
        String json = get(String.format("%s/%s", endPoint, id), null);
        Gson gson = new Gson();
        return gson.fromJson(json, cls);
    }

    public int updateTickets(String endPoint, String[][] args) {
        String contentJson = post(endPoint, args);

        JsonObject object = JsonParser.parseString(contentJson).getAsJsonObject();
        assert object.has("tickets");

        return object.get("tickets").getAsInt();
    }

    public <T extends GenericContent> T getContentFromJson(String contentJson, Class<T> cls, boolean asArray) {
        Gson gson = new Gson();

        JsonObject object;

        if (contentJson == null || contentJson.isEmpty())
            return null;

        if (asArray) {
            JsonArray array = JsonParser.parseString(contentJson).getAsJsonArray();
            if (array.size() == 0)
                return null;
            object = array.get(0).getAsJsonObject();
        }
        else
            object = JsonParser.parseString(contentJson).getAsJsonObject();

        String json = object.toString();
        T content = gson.fromJson(json, cls);

        updateContent(content.contentId, content);

        return content;
    }

    public <T extends GenericContent> T getContentForKey(String endPoint, String key, String contentId, Class<T> cls) {
        T content = (T) _content.get(contentId);

        if (content != null)
            return content;

        String contentJson = get(endPoint, new String[][] {
                { key, contentId }
        });

        if (contentJson == null)
            return null;

        return getContentFromJson(contentJson, cls, true);
    }

    public <T extends ContentProgress> T getProgress(String endPoint, String contentId, Class<T> cls) {
        return getContentForKey(endPoint, "contentId", contentId, cls);
    }

    public <T extends GenericContent> T getContent(String endPoint, String contentId, Class<T> cls) {
        return getContentForKey(endPoint, "contentId", contentId, cls);
    }

    public <T extends GenericContent> T putContent(String endPoint, String contentId, Class<T> cls) {
        T content = (T) _content.get(contentId);

        if (content != null)
            return content;

        String contentJson = put(endPoint, new String[][] {
                { "contentId", contentId }
        });

        return getContentFromJson(contentJson, cls, false);
    }

    public <T extends GenericContent> T getContentFromId(String endPoint, String id, Class<T> cls) {
        T content = (T) _content.get(id);

        if (content != null)
            return content;

        String contentJson = get(String.format("%s/%s", endPoint, id), null);
        return getContentFromJson(contentJson, cls, false);
    }

    public <T extends GenericContent> void updateContent(String contentId, GenericContent content) {
        _content.put(content.contentId, content);
        _content.put(content._id, content);
    }

    public static void initCache() {
        Subject.cacheSubjects();
    }
}
