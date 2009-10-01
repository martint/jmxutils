package mt.jmx.guice;

import com.google.inject.Key;

class Mapping
{
    private String name;
    private Key<?> key;

    Mapping(String name, Key<?> key)
    {
        this.name = name;
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public Key<?> getKey()
    {
        return key;
    }
}
