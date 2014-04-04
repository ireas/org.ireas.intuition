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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.ireas.intuition.IntuitionResourceBundle.IntuitionControl;

import com.google.common.base.Preconditions;

/**
 * Provides the messages served by the Intuition API.  In fact, this is a
 * wrapper around an {@link IntuitionResourceBundle} and a {@link
 * MessageFormat}.
 * <p>
 * Per default, this class accesses the Intuition installation on {@code
 * tools.wmflabs.org}.  To change the Intuition installation to use, call
 * {@link IntuitionLoader#setIntuitionUrl(String)
 * IntuitionLoader.setIntuitionUrl}.
 *
 * @author ireas
 */
public final class Intuition {

    private static ResourceBundle getResourceBundle(final String domain,
            final Locale locale) {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(locale);
        Preconditions.checkArgument(!domain.isEmpty());

        return ResourceBundle.getBundle(domain, locale, Intuition.class
                .getClassLoader(), new IntuitionControl());
    }

    private final ResourceBundle resourceBundle;

    private final MessageFormat messageFormat;

    private Intuition(final ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        messageFormat = new MessageFormat("", resourceBundle.getLocale());
    }

    /**
     * Creates a new Intuition instance for the specified domain and the
     * default locale.
     *
     * @param domain the domain to get the messages for
     * @throws NullPointerException if the specified domain is null
     * @throws MissingResourceException if the specified domain does not exist
     *         or the Intuition installation specified in {@link
     *         IntuitionLoader} is broken
     * @throws IllegalArgumentException if the specified domain is empty
     */
    public Intuition(final String domain) {
        this(domain, Locale.getDefault());
    }

    /**
     * Creates a new Intuition instance for the specified domain and the
     * specified locale.
     *
     * @param domain the domain to get the messages for
     * @param locale the locale to get the messages for
     * @throws NullPointerException if the specified domain or the specified
     *         locale is null
     * @throws MissingResourceException if the specified domain does not exist
     *         or the Intuition installation specified in {@link
     *         IntuitionLoader} is broken
     * @throws IllegalArgumentException if the specified domain is empty
     */
    public Intuition(final String domain, final Locale locale) {
        this(getResourceBundle(domain, locale));
    }

    /**
     * Returns the message for the specified key formatted with the specified
     * arguments.  The count of the specified arguments must match the count of
     * arguments used in the message.
     *
     * @param key the key to get the message for
     * @param arguments the arguments to format the message with
     * @return the message for the specified key formatted using the specified
     *         arguments
     * @throws NullPointerException if the specified key or one of the
     *         specified arguments is null
     * @throws IllegalArgumentException if the specified key is empty or the
     *         count of the specified arguments does not match the count of
     *         arguments used in the message
     * @throws MissingResourceException if no message for the specified key can
     *         be found
     */
    public String get(final String key, final String... arguments) {
        Preconditions.checkNotNull(key);
        for (String argument : arguments) {
            Preconditions.checkNotNull(argument);
        }

        String pattern = resourceBundle.getString(key);
        if (getArgumentsCount(pattern) != arguments.length) {
            throw new IllegalArgumentException();
        }

        messageFormat.applyPattern(pattern);
        return messageFormat.format(arguments);
    }

    private int getArgumentsCount(final String pattern) {
        int i = -1;
        boolean cont = true;
        while (cont) {
            i++;
            String placeholder = "{" + i + "}";
            if (!pattern.contains(placeholder)) {
                cont = false;
            }
        }
        return i;
    }

}
