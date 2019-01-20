package org.weakref.jmx;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.weakref.jmx.ObjectNames.quoteValueIfNecessary;

public class ObjectNameBuilder
{
    private static final Pattern BAD_PACKAGENAME_PATTERN = Pattern.compile("[:?*]");
    private final StringBuilder objectName;
    private final Set<String> properties = new HashSet<>();

    public ObjectNameBuilder(String domain)
    {
        requireNonNull(domain, "domain is null");
        checkArgument(!BAD_PACKAGENAME_PATTERN.matcher(domain).find(), "domain is invalid");
        this.objectName = new StringBuilder(domain);
    }

    public ObjectNameBuilder withProperty(String name, String value)
    {
        checkArgument(!properties.contains(name), "Duplicate property name: %s", name);

        if (properties.isEmpty()) {
            objectName.append(":");
        }
        else {
            objectName.append(",");
        }

        objectName.append(name).append('=').append(quoteValueIfNecessary(value));
        properties.add(name);
        return this;
    }

    public String build()
    {
        return objectName.toString();
    }
}
