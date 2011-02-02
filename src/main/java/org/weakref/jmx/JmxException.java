package org.weakref.jmx;

public class JmxException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public enum Reason
    {
        CONFIG,
        MALFORMED_OBJECT_NAME,
        INSTANCE_ALREADY_EXISTS,
        INSTANCE_NOT_FOUND,
        MBEAN_REGISTRATION
    }

    private final Reason reason;

    JmxException(final Reason reason, final String message, final Object ... args)
    {
        super(String.format(message, args));
        this.reason = reason;
    }

    JmxException(final Reason reason, final Throwable cause, final String message, final Object ... args)
    {
        super(String.format(message, args), cause);
        this.reason = reason;
    }

    public Reason getReason()
    {
        return reason;
    }
}
