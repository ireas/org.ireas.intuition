/*
 * Copyright (C) 2014 Robin Krahl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.ireas.intuition;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * Loads a message files from the Intuition API parses its content.
 *
 * @author ireas
 */
public final class IntuitionLoader {

    private static final String KEY_MESSAGES = "messages";

    private static String intuitionUrl =
            "https://tools.wmflabs.org/intuition/api.php?domains=%s&lang=%s";

    /**
     * Returns the URL of the {@code api.php} file of the Intuition
     * installation.  Per default, this will point to the installation on
     * {@code tools.wmflabs.org}.  This URL will be formatted using the domain
     * of the messages and the languages and has to point to the API script
     * returning the messages as a JSON object.  Usually, the end of the URL
     * should be {@code "/api.php?domains=%s&lang=%s"}.
     *
     * @return the URL of the Intuition API
     */
    public static String getIntuitionUrl() {
        return intuitionUrl;
    }

    /**
     * Sets the URL of the {@code api.php} file of the Intuition installation
     * to use to the specified value. The URL will be formatted using the
     * domain of the messages and the languages and has to point to the API
     * script returning the messages as a JSON object.  Usually, the end of
     * the URL should be {@code "/api.php?domains=%s&lang=%s"}.
     *
     * @param intuitionUrl the new URL to the Intuition API
     * @throws NullPointerException if the specified URL is null
     * @throws IllegalArgumentException if the specified URL is empty or
     *         illegal
     */
    public static void setIntuitionUrl(final String intuitionUrl) {
        Preconditions.checkNotNull(intuitionUrl);
        Preconditions.checkArgument(!intuitionUrl.isEmpty());
        try {
            new URI(String.format(intuitionUrl, "", ""));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException();
        }

        IntuitionLoader.intuitionUrl = intuitionUrl;
    }

    private final String domain;

    private final String language;

    /**
     * Constructs a new Intuition loader for the specified message domain and
     * language.  The language code must be the same as used in the Wikimedia
     * projects (e. g. {@code "de"} for German or {@code "en"} for English).
     *
     * @param domain the domain to get the messages for
     * @param language the language to get the messages in
     */
    public IntuitionLoader(final String domain, final String language) {
        this.domain = domain;
        this.language = language;
    }

    private Optional<JsonObject> getDomainObject(final JsonElement element) {
        Optional<JsonObject> domainObject = Optional.absent();

        // two types of valid responses:
        // (1) {"messages": {"<domain>": { … } } }
        // --> messages found
        // (2) {"messages": {"<domain>": false} }
        // --> no messages available

        if (!element.isJsonObject()) {
            throw new IllegalArgumentException();
        }
        JsonObject rootObject = element.getAsJsonObject();
        if (!rootObject.has(KEY_MESSAGES)) {
            throw new IllegalArgumentException();
        }
        JsonElement messagesElement = rootObject.get(KEY_MESSAGES);
        if (!messagesElement.isJsonObject()) {
            throw new IllegalArgumentException();
        }
        JsonObject messagesObject = messagesElement.getAsJsonObject();
        if (!messagesObject.has(domain)) {
            throw new IllegalArgumentException();
        }
        JsonElement domainElement = messagesObject.get(domain);

        if (domainElement.isJsonObject()) {
            // valid response (1): messages found
            domainObject = Optional.of(domainElement.getAsJsonObject());
        } else if (domainElement.isJsonPrimitive()) {
            JsonPrimitive domainPrimitive = domainElement.getAsJsonPrimitive();
            if (!domainPrimitive.isBoolean()) {
                throw new IllegalArgumentException();
            }
            boolean domainBoolean = domainPrimitive.getAsBoolean();
            if (domainBoolean) {
                throw new IllegalArgumentException();
            }
            // valid response (2): no messages available
        } else {
            throw new IllegalArgumentException();
        }

        return domainObject;
    }

    private JsonElement loadJsonElement() throws IOException {
        String messages = loadString();
        JsonParser parser = new JsonParser();
        try {
            return parser.parse(messages);
        } catch (JsonSyntaxException exception) {
            throw new IOException(exception);
        }
    }

    /**
     * Loads the messages from the server and returns them as a map.  If the
     * domain passed to the constructor does not exist, an absent value is
     * returned.
     *
     * @return a map containing all messages or an absent value if the
     *         specified domain does not exist
     * @throws IOException if an error occurs during the request
     * @throws IllegalArgumentException if the response returned by the
     *         Intuition API is invalid
     */
    public Optional<Map<String, String>> loadMessages() throws IOException {
        JsonElement rootElement = loadJsonElement();
        Optional<Map<String, String>> messages = Optional.absent();
        Optional<JsonObject> domainObject = getDomainObject(rootElement);
        if (domainObject.isPresent()) {
            messages = Optional.of(parseMessages(domainObject.get()));
        }
        return messages;
    }

    private String loadString() throws IOException {
        // TODO fix SNI / invalid handshake
        System.setProperty("jsse.enableSNIExtension", "false");
        String urlString = String.format(getIntuitionUrl(), domain, language);
        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException exception) {
            // Should not occur: The initial URI is valid, and setIntuitionUrl
            // checks the validity of the new URI
            throw new AssertionError("Invalid URI", exception);
        }
        CloseableHttpClient client = HttpClients.createDefault();
        String result;
        try {
            HttpGet request = new HttpGet(uri);
            HttpResponse httpResponse = client.execute(request);
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } finally {
            client.close();
        }
        return result;
    }

    private Map<String, String> parseMessages(final JsonObject jsonObject) {
        Map<String, String> messages = new HashMap<>();
        Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for (Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            if (element.isJsonPrimitive()) {
                String value = element.getAsString();
                messages.put(key, value);
            }
        }
        return messages;
    }

}
