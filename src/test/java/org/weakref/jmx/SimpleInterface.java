package org.weakref.jmx;

public interface SimpleInterface
{
    @Managed
    boolean isBooleanValue();

    @Managed
    void setBooleanValue(boolean booleanValue);

    @Managed
    Boolean isBooleanBoxedValue();

    @Managed
    void setBooleanBoxedValue(Boolean booleanBoxedValue);

    @Managed
    byte getByteValue();

    @Managed
    void setByteValue(byte byteValue);

    @Managed
    Byte getByteBoxedValue();

    @Managed
    void setByteBoxedValue(Byte byteBoxedValue);

    @Managed
    short getShortValue();

    @Managed
    void setShortValue(short shortValue);

    @Managed
    Short getShortBoxedValue();

    @Managed
    void setShortBoxedValue(Short shortBoxedValue);

    @Managed
    int getIntegerValue();

    @Managed
    void setIntegerValue(int integerValue);

    @Managed
    Integer getIntegerBoxedValue();

    @Managed
    void setIntegerBoxedValue(Integer integerBoxedValue);

    @Managed
    long getLongValue();

    @Managed
    void setLongValue(long longValue);

    @Managed
    Long getLongBoxedValue();

    @Managed
    void setLongBoxedValue(Long longBoxedValue);

    @Managed
    float getFloatValue();

    @Managed
    void setFloatValue(float floatValue);

    @Managed
    Float getFloatBoxedValue();

    @Managed
    void setFloatBoxedValue(Float floatBoxedValue);

    @Managed
    double getDoubleValue();

    @Managed
    void setDoubleValue(double doubleValue);

    @Managed
    Double getDoubleBoxedValue();

    @Managed
    void setDoubleBoxedValue(Double doubleBoxedValue);

    @Managed
    String getStringValue();

    @Managed
    void setStringValue(String stringValue);

    void setNotManaged(int value);

    int getNotManaged();

    @Managed
    Object echo(Object value);

    @Managed
    Object getObjectValue();

    @Managed
    void setObjectValue(Object objectValue);

    int getWriteOnly();

    @Managed
    void setWriteOnly(int writeOnly);

    @Managed
    int getReadOnly();

    void setReadOnly(int readOnly);

    @Managed(description = "epic description")
    int getDescribedInt();
}
