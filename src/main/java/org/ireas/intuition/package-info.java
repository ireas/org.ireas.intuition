/**
 * Java wrapper for the Intuition internalization framework.  To access
 * Intuition messages, use the {@code Intuition} class.  This package also
 * contains {@code IntuitionResourceBundle}, a resource bundle implementation
 * that converts messages returned by the Intuition API to Java messages that
 * can be handled by {@code MessageFormat}, and {@code IntuitionLoader}, a
 * class performing requests to the Intuition API.
 * <p>
 * A simple example of the Intuition API is:
 * <pre>
 * // initialize Intuition
 * Intuition intuition = new Intuition("domain");
 * // get a simple message
 * intuition.get("message-key-1");
 * // get a formatted message
 * intuition.get("message-key-2", "argument 1", "argument 2");
 * </pre>
 * <p>
 * Per default, this library accesses the Intuition installation on {@code
 * tools.wmflabs.org}.  To change the Intuition installation to use, call the
 * {@code IntuitionLoader.setIntuitionUrl} method.
 * <p>
 * For more information about Intuition itself, see <a 
 * href="https://github.com/Krinkle/intuition/">Intuition on GitHub</a> and <a
 * href="https://tools.wmflabs.org/intuition/">Intuition on Wikimedia Labs</a>.
 */
package org.ireas.intuition;

