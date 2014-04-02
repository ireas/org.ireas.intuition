package org.ireas.intuition;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.ireas.intuition.IntuitionResourceBundle.IntuitionControl;
import org.junit.Assert;
import org.junit.Test;

public class IntuitionTests {

    private static final String PB_GROUP = "pb";

    @Test
    public void testFormatting() {
        Intuition intuition = new Intuition(PB_GROUP, Locale.ENGLISH);
        String message = intuition.get("pb-current-users", "13");
        Assert.assertEquals(
                message,
                "At the moment, 13 users participate in the <em>Personal Acquaintances</em> project.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormattingMissingArguments() {
        Intuition intuition = new Intuition(PB_GROUP, Locale.ENGLISH);
        String message = intuition.get("pb-user-heading", "Ireas");
        System.out.println(message);
    }

    @Test
    public void testFormattingMulti() {
        Intuition intuition = new Intuition(PB_GROUP, Locale.ENGLISH);
        String message =
                intuition.get("pb-current-confirmations", "3", "4", "5");
        Assert.assertEquals(
                message,
                "There are 3 confirmations in total, this is about <abbr title=\"4\">5</abbr> confirmations per day.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormattingNoArguments() {
        Intuition intuition = new Intuition(PB_GROUP, Locale.ENGLISH);
        String message = intuition.get("pb-user-heading");
        System.out.println(message);
    }

    @Test
    public void testFormattingRepeated() {
        Intuition intuition = new Intuition(PB_GROUP, Locale.ENGLISH);
        String message =
                intuition.get("pb-user-heading", "Ireas", "http://ireas");
        Assert.assertEquals(
                message,
                "Information for <a href=\"http://ireas\" title=\"User page of Ireas\">Ireas</a>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormattingTooMuchArguments() {
        Intuition intuition = new Intuition(PB_GROUP, Locale.ENGLISH);
        String message =
                intuition.get("pb-user-heading", "Ireas", "http://ireas",
                        "Ireas");
        System.out.println(message);
    }

    @Test(expected = MissingResourceException.class)
    public void testMissingMessage() {
        Intuition intuition = new Intuition(PB_GROUP, Locale.ENGLISH);
        intuition.get("pb-test-test");
    }

    @Test
    public void testResourceBundleDefault() {
        ResourceBundle bundle =
                ResourceBundle.getBundle(PB_GROUP, new IntuitionControl());
        Assert.assertEquals(bundle.getLocale(), Locale.getDefault());
    }

    @Test
    public void testResourceBundleLocaleEnglish() {
        Locale locale = Locale.ENGLISH;
        ResourceBundle bundle =
                ResourceBundle.getBundle(PB_GROUP, locale, getClass()
                        .getClassLoader(), new IntuitionControl());
        Assert.assertEquals(bundle.getLocale(), Locale.ENGLISH);
        Assert.assertTrue(bundle.keySet().contains("pb-current-figures"));
        Assert.assertEquals(bundle.getString("pb-current-figures"),
                "Current figures");
    }

    @Test
    public void testResourceBundleLocaleGerman() {
        Locale locale = Locale.GERMAN;
        ResourceBundle bundle =
                ResourceBundle.getBundle(PB_GROUP, locale, getClass()
                        .getClassLoader(), new IntuitionControl());
        Assert.assertEquals(bundle.getLocale(), Locale.GERMAN);
        Assert.assertTrue(bundle.keySet().contains("pb-current-figures"));
        Assert.assertEquals(bundle.getString("pb-current-figures"),
                "Aktuelle Zahlen");
    }

}
