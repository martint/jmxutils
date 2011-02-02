package org.weakref.jmx;

public class JmxException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public enum JmxCause
    {
        CONFIG,
        MALFORMED_OBJECT_NAME,
        INSTANCE_ALREADY_EXISTS,
        INSTANCE_NOT_FOUND,
        MBEAN_REGISTRATION
    }

    private final JmxCause jmxCause;

    JmxException(final JmxCause jmxCause, final String message, final Object ... args)
    {
        super(String.format(message, args));
        this.jmxCause = jmxCause;
    }


    JmxException(final JmxCause jmxCause, final Throwable cause, final String message, final Object ... args)
    {
        super(String.format(message, args), cause);
        this.jmxCause = jmxCause;
    }

    public JmxCause getJmxCause()
    {
        return jmxCause;
    }
}
