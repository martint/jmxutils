package org.weakref.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class CustomAnnotationObject
        extends SimpleObject
{

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ManagedAnnotation
    public @interface Managed1
    {
        String description() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ManagedAnnotation
    public @interface Managed2
    {
        String description() default "";
    }


    @Override
    @Managed1
    public boolean isBooleanValue()
    {
        return super.isBooleanValue();
    }

    @Override
    @Managed2
    public void setBooleanValue(boolean booleanValue)
    {
        super.setBooleanValue(booleanValue);
    }

    @Override
    @Managed2
    public Boolean isBooleanBoxedValue()
    {
        return super.isBooleanBoxedValue();
    }

    @Override
    @Managed1
    public void setBooleanBoxedValue(Boolean booleanBoxedValue)
    {
        super.setBooleanBoxedValue(booleanBoxedValue);
    }

    @Override
    @Managed1
    public byte getByteValue()
    {
        return super.getByteValue();
    }

    @Override
    @Managed1
    public void setByteValue(byte byteValue)
    {
        super.setByteValue(byteValue);
    }

    @Override
    @Managed2
    public Byte getByteBoxedValue()
    {
        return super.getByteBoxedValue();
    }

    @Override
    @Managed2
    public void setByteBoxedValue(Byte byteBoxedValue)
    {
        super.setByteBoxedValue(byteBoxedValue);
    }

    @Override
    @Managed2
    public short getShortValue()
    {
        return super.getShortValue();
    }

    @Override
    @Managed2
    public void setShortValue(short shortValue)
    {
        super.setShortValue(shortValue);
    }

    @Override
    @Managed1
    public Short getShortBoxedValue()
    {
        return super.getShortBoxedValue();
    }

    @Override
    @Managed1
    public void setShortBoxedValue(Short shortBoxedValue)
    {
        super.setShortBoxedValue(shortBoxedValue);
    }

    @Override
    @Managed1
    public int getIntegerValue()
    {
        return super.getIntegerValue();
    }

    @Override
    @Managed2
    public void setIntegerValue(int integerValue)
    {
        super.setIntegerValue(integerValue);
    }

    @Override
    @Managed2
    public Integer getIntegerBoxedValue()
    {
        return super.getIntegerBoxedValue();
    }

    @Override
    @Managed1
    public void setIntegerBoxedValue(Integer integerBoxedValue)
    {
        super.setIntegerBoxedValue(integerBoxedValue);
    }

    @Override
    @Managed1
    public long getLongValue()
    {
        return super.getLongValue();
    }

    @Override
    @Managed2
    public void setLongValue(long longValue)
    {
        super.setLongValue(longValue);
    }

    @Override
    @Managed1
    public Long getLongBoxedValue()
    {
        return super.getLongBoxedValue();
    }

    @Override
    @Managed1
    public void setLongBoxedValue(Long longBoxedValue)
    {
        super.setLongBoxedValue(longBoxedValue);
    }

    @Override
    @Managed2
    public float getFloatValue()
    {
        return super.getFloatValue();
    }

    @Override
    @Managed2
    public void setFloatValue(float floatValue)
    {
        super.setFloatValue(floatValue);
    }

    @Override
    @Managed1
    public Float getFloatBoxedValue()
    {
        return super.getFloatBoxedValue();
    }

    @Override
    @Managed2
    public void setFloatBoxedValue(Float floatBoxedValue)
    {
        super.setFloatBoxedValue(floatBoxedValue);
    }

    @Override
    @Managed1
    public double getDoubleValue()
    {
        return super.getDoubleValue();
    }

    @Override
    @Managed2
    public void setDoubleValue(double doubleValue)
    {
        super.setDoubleValue(doubleValue);
    }

    @Override
    @Managed1
    public Double getDoubleBoxedValue()
    {
        return super.getDoubleBoxedValue();
    }

    @Override
    @Managed1
    public void setDoubleBoxedValue(Double doubleBoxedValue)
    {
        super.setDoubleBoxedValue(doubleBoxedValue);
    }

    @Override
    @Managed2
    public String getStringValue()
    {
        return super.getStringValue();
    }

    @Override
    @Managed2
    public void setStringValue(String stringValue)
    {
        super.setStringValue(stringValue);
    }

    @Override
    public void setNotManaged(int value)
    {
        super.setNotManaged(value);
    }

    @Override
    public int getNotManaged()
    {
        return super.getNotManaged();
    }

    @Override
    @Managed2
    public Object echo(Object value)
    {
        return super.echo(value);
    }

    @Override
    @Managed1
    public Object getObjectValue()
    {
        return super.getObjectValue();
    }

    @Override
    @Managed2
    public void setObjectValue(Object objectValue)
    {
        super.setObjectValue(objectValue);
    }

    @Override
    public int getWriteOnly()
    {
        return super.getWriteOnly();
    }

    @Override
    @Managed1
    public void setWriteOnly(int writeOnly)
    {
        super.setWriteOnly(writeOnly);
    }

    @Override
    @Managed2
    public int getReadOnly()
    {
        return super.getReadOnly();
    }

    @Override
    public void setReadOnly(int readOnly)
    {
        super.setReadOnly(readOnly);
    }

    @Override
    @Managed1(description = "epic description")
    public int getDescribedInt()
    {
        return super.getDescribedInt();
    }

}
