package com.xrosstools.idea.gef.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public enum DataTypeEnum {
    STRING("String"),

    INTEGER("Integer"),

    LONG("Long"),

    FLOAT("Float"),

    DOUBLE("Double"),

    BOOLEAN("Boolean"),

    DATE("Date"),

    TIME_UNIT("Time unit"),

    OBJECT("Object");

    private DataTypeEnum(String name) {
        this.name = name;
    }

    private String name;

    public String getDisplayName() {
        return name;
    }

    public static final String[] CONFIGURABLE_NAMES = new String[] {
            STRING.getDisplayName(),
            INTEGER.getDisplayName(),
            LONG.getDisplayName(),
            FLOAT.getDisplayName(),
            DOUBLE.getDisplayName(),
            BOOLEAN.getDisplayName(),
            DATE.getDisplayName(),
            TIME_UNIT.getDisplayName(),
    };

    public static DataTypeEnum findByDisplayName(String name) {
        for(DataTypeEnum e: DataTypeEnum.values())
            if(e.getDisplayName().equals(name))
                return e;

        return DataTypeEnum.STRING;
    }

    public IPropertyDescriptor createDescriptor() {
        switch (this) {
            case STRING: return new StringPropertyDescriptor();
            case INTEGER: return new IntegerPropertyDescriptor();
            case LONG: return new LongPropertyDescriptor();
            case FLOAT: return new FloatPropertyDescriptor();
            case DOUBLE: return new DoublePropertyDescriptor();
            case BOOLEAN: return new ListPropertyDescriptor(new Boolean[]{Boolean.FALSE, Boolean.TRUE});
            case DATE: return new StringPropertyDescriptor();
            case TIME_UNIT: return new ListPropertyDescriptor(TimeUnit.values());
            case OBJECT: return new StringPropertyDescriptor();
            default: throw new IllegalArgumentException(this.toString());
        }
    }

    public Object defaultValue() {
        switch (this) {
            case STRING: return "";
            case INTEGER: return 0;
            case LONG: return 0L;
            case FLOAT: return 0.0f;
            case DOUBLE: return 0.0d;
            case BOOLEAN: return Boolean.FALSE;
            case DATE: return new Date();
            case TIME_UNIT: return TimeUnit.SECONDS;
            case OBJECT: return null;
            default: throw new IllegalArgumentException(this.toString());
        }
    }

    public static DataTypeEnum typeOf(Object obj) {
        if(obj == null)
            return OBJECT;

        if(obj instanceof String)
            return STRING;

        if(obj instanceof Integer)
            return INTEGER;

        if(obj instanceof Long)
            return LONG;

        if(obj instanceof Float)
            return FLOAT;

        if(obj instanceof Double)
            return DOUBLE;

        if(obj instanceof Boolean)
            return BOOLEAN;

        if(obj instanceof Date)
            return DATE;

        if(obj instanceof TimeUnit)
            return TIME_UNIT;

        return OBJECT;
    }
}
