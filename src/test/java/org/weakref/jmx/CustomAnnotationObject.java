package org.weakref.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class CustomAnnotationObject
        implements SimpleInterface
{
    private boolean booleanValue;
    private Boolean booleanBoxedValue;
    private byte byteValue;
    private Byte byteBoxedValue;
    private short shortValue;
    private Short shortBoxedValue;
    private int integerValue;
    private Integer integerBoxedValue;
    private long longValue;
    private Long longBoxedValue;
    private float floatValue;
    private Float floatBoxedValue;
    private double doubleValue;
    private Double doubleBoxedValue;
    private String stringValue;
    private Object objectValue;
    private int privateValue;

    private int notManaged;
    private int writeOnly;
    private int readOnly;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @ManagedAnnotation
    public @interface Managed1
    {
        String description() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @ManagedAnnotation
    public @interface Managed2
    {
        String description() default "";
    }


    @Managed1
    public boolean isBooleanValue()
    {
        return booleanValue;
    }

    @Managed2
    public void setBooleanValue(boolean booleanValue)
    {
        this.booleanValue = booleanValue;
    }

    @Managed2
    public Boolean isBooleanBoxedValue()
    {
        return booleanBoxedValue;
    }

    @Managed1
    public void setBooleanBoxedValue(Boolean booleanBoxedValue)
    {
        this.booleanBoxedValue = booleanBoxedValue;
    }

    @Managed1
    public byte getByteValue()
    {
        return byteValue;
    }

    @Managed1
    public void setByteValue(byte byteValue)
    {
        this.byteValue = byteValue;
    }

    @Managed2
    public Byte getByteBoxedValue()
    {
        return byteBoxedValue;
    }

    @Managed2
    public void setByteBoxedValue(Byte byteBoxedValue)
    {
        this.byteBoxedValue = byteBoxedValue;
    }

    @Managed2
    public short getShortValue()
    {
        return shortValue;
    }

    @Managed2
    public void setShortValue(short shortValue)
    {
        this.shortValue = shortValue;
    }

    @Managed1
    public Short getShortBoxedValue()
    {
        return shortBoxedValue;
    }

    @Managed1
    public void setShortBoxedValue(Short shortBoxedValue)
    {
        this.shortBoxedValue = shortBoxedValue;
    }

    @Managed1
    public int getIntegerValue()
    {
        return integerValue;
    }

    @Managed2
    public void setIntegerValue(int integerValue)
    {
        this.integerValue = integerValue;
    }

    @Managed2
    public Integer getIntegerBoxedValue()
    {
        return integerBoxedValue;
    }

    @Managed1
    public void setIntegerBoxedValue(Integer integerBoxedValue)
    {
        this.integerBoxedValue = integerBoxedValue;
    }

    @Managed1
    public long getLongValue()
    {
        return longValue;
    }

    @Managed2
    public void setLongValue(long longValue)
    {
        this.longValue = longValue;
    }

    @Managed1
    public Long getLongBoxedValue()
    {
        return longBoxedValue;
    }

    @Managed1
    public void setLongBoxedValue(Long longBoxedValue)
    {
        this.longBoxedValue = longBoxedValue;
    }

    @Managed2
    public float getFloatValue()
    {
        return floatValue;
    }

    @Managed2
    public void setFloatValue(float floatValue)
    {
        this.floatValue = floatValue;
    }

    @Managed1
    public Float getFloatBoxedValue()
    {
        return floatBoxedValue;
    }

    @Managed2
    public void setFloatBoxedValue(Float floatBoxedValue)
    {
        this.floatBoxedValue = floatBoxedValue;
    }

    @Managed1
    public double getDoubleValue()
    {
        return this.doubleValue;
    }

    @Managed2
    public void setDoubleValue(double doubleValue)
    {
        this.doubleValue = doubleValue;
    }

    @Managed1
    public Double getDoubleBoxedValue()
    {
        return doubleBoxedValue;
    }

    @Managed1
    public void setDoubleBoxedValue(Double doubleBoxedValue)
    {
        this.doubleBoxedValue = doubleBoxedValue;
    }

    @Managed2
    public String getStringValue()
    {
        return stringValue;
    }

    @Managed2
    public void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }

    public void setNotManaged(int value)
    {
        this.notManaged = value;
    }

    public int getNotManaged()
    {
        return notManaged;
    }

    @Managed2
    public Object echo(Object value)
    {
        return value;
    }

    @Managed1
    public Object getObjectValue()
    {
        return objectValue;
    }

    @Managed2
    public void setObjectValue(Object objectValue)
    {
        this.objectValue = objectValue;
    }

    public int getWriteOnly()
    {
        return writeOnly;
    }

    @Managed1
    public void setWriteOnly(int writeOnly)
    {
        this.writeOnly = writeOnly;
    }

    @Managed2
    public int getReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(int readOnly)
    {
        this.readOnly = readOnly;
    }

    @Managed1
    private int getPrivateValue()
    {
        return privateValue;
    }

    @Managed2
    private void setPrivateValue(int privateValue)
    {
        this.privateValue = privateValue;
    }

    @Managed1(description = "epic description")
    public int getDescribedInt()
    {
        return 1;
    }
}
