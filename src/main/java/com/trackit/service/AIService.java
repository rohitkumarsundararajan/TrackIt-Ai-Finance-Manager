package com.trackit.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Calls OpenRouter chat completions (OpenAI-compatible) to turn a financial summary into advice text.
 * <p>
 * API key: {@code OPEN_ROUTER_KEY}. Optional: {@code OPEN_ROUTER_MODEL} (defaults to a stable OpenRouter model id).
 * </p>
 */
public class AIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String ENV_API_KEY = "OPEN_ROUTER_KEY";
    /** Override with e.g. {@code openai/gpt-3.5-turbo} or {@code openai/gpt-4o-mini} */
    private static final String ENV_MODEL = "OPEN_ROUTER_MODEL";
    private static final String DEFAULT_MODEL = "openai/gpt-4o-mini";

    private static final String HEADER_REFERER = "HTTP-Referer";
    private static final String HEADER_TITLE = "X-Title";

    private final Gson gson = new Gson();

    /**
     * Sends the summary to the model and returns plain advice text, or a safe error string on failure.
     *
     * @param financialSummary user-formatted summary (totals, categories, prompt)
     * @return advice text only (no JSON wrapper)
     */
    public String generateAdvice(String financialSummary) {
        String apiKey = System.getenv(ENV_API_KEY);
        if (apiKey == null || apiKey.isBlank()) {
            return "AI advice is unavailable: set the " + ENV_API_KEY + " environment variable.";
        }

        String model = resolveModel();

        try {
            URL url = URI.create(API_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(25_000);
            connection.setReadTimeout(120_000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            // OpenRouter recommends these for attribution; some setups behave better with Referer set
            connection.setRequestProperty(HEADER_REFERER, "https://localhost/");
            connection.setRequestProperty(HEADER_TITLE, "TrackIT Finance Manager");

            byte[] payload = buildRequestBody(model, financialSummary).getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload);
            }

            int code = connection.getResponseCode();
            String responseBody = readStream(
                    code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream()
            );

            if (code < 200 || code >= 300) {
                return "Could not get AI advice (HTTP " + code + "). " + parseOpenRouterError(responseBody);
            }

            return parseChatCompletionContent(responseBody);
        } catch (IOException ex) {
            return "Could not reach the AI service: " + ex.getMessage();
        } catch (RuntimeException ex) {
            return "Could not parse AI response: " + ex.getMessage();
        }
    }

    private String resolveModel() {
        String fromEnv = System.getenv(ENV_MODEL);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return DEFAULT_MODEL;
    }

    /**
     * OpenRouter-compatible body: model, messages (system + user), non-streaming.
     */
    private String buildRequestBody(String model, String financialSummary) {
        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        root.addProperty("stream", false);
        root.addProperty("temperature", 0.6);
        root.addProperty("max_tokens", 512);

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content",
                "You are a concise personal finance coach. "
                        + "Reply with 2–4 short sentences of practical advice only. "
                        + "Do not repeat raw numbers unless needed. No markdown headings.");

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", financialSummary);

        JsonArray messages = new JsonArray();
        messages.add(systemMsg);
        messages.add(userMsg);
        root.add("messages", messages);

        return gson.toJson(root);
    }

    /**
     * Parses OpenRouter / OpenAI-style error JSON: {"error":{"message":"...","code":...}}
     */
    private String parseOpenRouterError(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            JsonElement el = JsonParser.parseString(body);
            if (!el.isJsonObject()) {
                return truncate(body);
            }
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("error") && obj.get("error").isJsonObject()) {
                JsonObject err = obj.getAsJsonObject("error");
                if (err.has("message")) {
                    String msg = err.get("message").getAsString();
                    String code = err.has("code") ? String.valueOf(err.get("code")) : "";
                    return code.isEmpty() ? msg : msg + " (code: " + code + ")";
                }
                return err.toString();
            }
            return truncate(body);
        } catch (RuntimeException ex) {
            return truncate(body);
        }
    }

    /**
     * Extracts assistant text from chat completion JSON. Handles:
     * <ul>
     *     <li>{@code choices[0].message.content} as string</li>
     *     <li>content as array (multimodal / some providers)</li>
     * </ul>
     */
    private String parseChatCompletionContent(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Could not get AI advice: empty response body.";
        }

        JsonElement rootEl;
        try {
            rootEl = JsonParser.parseString(responseBody);
        } catch (RuntimeException ex) {
            return "Could not get AI advice: invalid JSON — " + ex.getMessage();
        }

        if (!rootEl.isJsonObject()) {
            return "Could not get AI advice: unexpected JSON root.";
        }

        JsonObject root = rootEl.getAsJsonObject();
        if (root.has("error")) {
            return "OpenRouter returned an error: " + parseOpenRouterError(responseBody);
        }

        if (!root.has("choices") || !root.get("choices").isJsonArray()) {
            return "Could not get AI advice: missing choices array.";
        }

        JsonArray choices = root.getAsJsonArray("choices");
        if (choices.isEmpty()) {
            return "Could not get AI advice: choices is empty.";
        }

        JsonObject first = choices.get(0).getAsJsonObject();
        if (!first.has("message") || !first.get("message").isJsonObject()) {
            return "Could not get AI advice: missing message object.";
        }

        JsonObject message = first.getAsJsonObject("message");
        String text = extractMessageContentText(message);
        if (text == null || text.isBlank()) {
            return "Could not get AI advice: empty assistant content.";
        }
        return text.trim();
    }

    private String extractMessageContentText(JsonObject message) {
        if (!message.has("content")) {
            return null;
        }
        JsonElement content = message.get("content");
        if (content.isJsonPrimitive() && ((JsonPrimitive) content).isString()) {
            return content.getAsString();
        }
        if (content.isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement part : content.getAsJsonArray()) {
                if (!part.isJsonObject()) {
                    continue;
                }
                JsonObject o = part.getAsJsonObject();
                if (o.has("type") && "text".equals(o.get("type").getAsString()) && o.has("text")) {
                    sb.append(o.get("text").getAsString());
                } else if (o.has("text")) {
                    sb.append(o.get("text").getAsString());
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        }
        if (content.isJsonPrimitive()) {
            return content.getAsString();
        }
        return null;
    }

    private static String truncate(String body) {
        if (body.length() > 400) {
            return body.substring(0, 400) + "...";
        }
        return body;
    }

    private static String readStream(java.io.InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
