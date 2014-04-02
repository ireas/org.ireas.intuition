package org.ireas.intuition;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Resource bundle parsing the result of an Intuition API request.  To create
 * an instance of this class, use the {@code ResourceBundle.getBundle} methods
 * and the {@code IntuitionControl} class as control.  For easy access to
 * Intuition messages, use the {@code Intuition} class.
 *
 * @author ireas
 */
public final class IntuitionResourceBundle extends ResourceBundle {

    /**
     * Provides callback methods for the {@code ResourceBundle.getBundle}
     * factory methods so that an {@code IntuitionResourceBundle} can be
     * created.  To initialize the resource bundle, an API request is performed
     * using the {@code IntuitionLoader} class.  For easy access to Intuition
     * messages, use the {@code Intuition} class.
     *
     * @author ireas
     */
    public static final class IntuitionControl extends ResourceBundle.Control {

        /**
         * The only formatted supported by the {@code IntuitionResourceBundle},
         * the JSON result of the Intuition API.
         */
        public static final String INTUITION_JSON_FORMAT = "intuition.json";

        @Override
        public List<String> getFormats(final String baseName) {
            Preconditions.checkNotNull(baseName);
            return Arrays.asList(INTUITION_JSON_FORMAT);
        }

        @Override
        @Nullable
        public ResourceBundle newBundle(final String baseName,
                final Locale locale, final String format,
                final ClassLoader loader, final boolean reload)
                throws IllegalAccessException, InstantiationException,
                IOException {
            Preconditions.checkNotNull(baseName);
            Preconditions.checkNotNull(locale);
            Preconditions.checkNotNull(format);
            Preconditions.checkNotNull(loader);

            ResourceBundle resourceBundle = null;

            if (format.equals(INTUITION_JSON_FORMAT)) {
                IntuitionLoader intuitionLoader =
                        new IntuitionLoader(baseName, locale.getLanguage());
                Optional<Map<String, String>> messages =
                        intuitionLoader.loadMessages();
                if (messages.isPresent()) {
                    resourceBundle =
                            new IntuitionResourceBundle(messages.get());
                }
            }

            return resourceBundle;
        }

    }

    private final Map<String, String> data;

    /**
     * Constructs a new Intuition resource bundle using the specified map as
     * messages.  The map must contain the messages as returned by the
     * Intuition API.  This class takes care of transforming the messages so
     * that they can be used with a {@code MessageFormat}.
     *
     * @param data the messages to use in this bundle
     * @throws NullPointerException if the specified data map is null
     */
    public IntuitionResourceBundle(final Map<String, String> data) {
        Preconditions.checkNotNull(data);
        this.data = data;
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(data.keySet());
    }

    @Override
    @Nullable
    protected Object handleGetObject(final String key) {
        Preconditions.checkNotNull(key);
        String value = data.get(key);
        if (value != null) {
            value = handleValue(value);
        }
        return value;
    }

    private String handleValue(final String value) {
        Preconditions.checkNotNull(value);

        String newValue = value;

        // replace placeholders
        int i = 1;
        boolean cont = true;
        while (cont) {
            String placeholder = "$" + i;
            String newPlaceholder = "{" + (i - 1) + "}";
            if (newValue.contains(placeholder)) {
                newValue = newValue.replace(placeholder, newPlaceholder);
            } else {
                cont = false;
            }
            i++;
        }

        // TODO plural support

        return newValue;
    }

}
