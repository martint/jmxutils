package org.weakref.jmx;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestNG compatible Assert API to aid migration to JUnit
 *
 * @deprecated for migration period only
 */
// TODO all these methods to be inlined or migrated separately
@Deprecated
public final class Assert
{
    private Assert() {}

    public static <T> void assertEquals(T actual, T expected)
    {
        assertThat(actual).isEqualTo(expected);
    }

    public static <T> void assertEquals(T actual, T expected, String message)
    {
        assertThat(actual).describedAs(message).isEqualTo(expected);
    }

    public static void fail(String message)
    {
        throw new AssertionError(message);
    }

    public static void assertFalse(boolean actual)
    {
        assertThat(actual).isFalse();
    }

    public static void assertFalse(boolean actual, String message)
    {
        assertThat(actual).describedAs(message).isFalse();
    }

    public static void assertTrue(boolean actual)
    {
        assertThat(actual).isTrue();
    }

    public static void assertTrue(boolean actual, String message)
    {
        assertThat(actual).describedAs(message).isTrue();
    }

    public static void assertNull(Object actual)
    {
        assertThat(actual).isNull();
    }

    public static void assertNull(Object actual, String message)
    {
        assertThat(actual).describedAs(message).isNull();
    }

    public static void assertNotNull(Object actual)
    {
        assertThat(actual).isNotNull();
    }

    public static void assertNotNull(Object actual, String message)
    {
        assertThat(actual).describedAs(message).isNotNull();
    }
}
