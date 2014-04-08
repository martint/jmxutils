package org.weakref.jmx;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.weakref.jmx.ObjectNames.quoteValueIfNecessary;

public class ObjectNameBuilder
{
    private static final Pattern BAD_PACKAGENAME_PATTERN = Pattern.compile("[:?*]");
    private final StringBuilder objectName;
    private final Set<String> properties = new HashSet<String>();

    public ObjectNameBuilder(String packageName)
    {
        checkNotNull(packageName, "packageName is null");
        checkArgument(!BAD_PACKAGENAME_PATTERN.matcher(packageName).find(), "packageName is invalid");
        this.objectName = new StringBuilder(packageName);
    }

    public ObjectNameBuilder withProperty(String name, String value)
    {
        if (properties.isEmpty()) {
            objectName.append(":");
        }
        else {
            objectName.append(",");
        }

        checkArgument(properties.add(name), "Duplicate property name " + name);

        objectName.append(name).append('=').append(quoteValueIfNecessary(value));
        return this;
    }

    public String build()
    {
        return objectName.toString();
    }
}
