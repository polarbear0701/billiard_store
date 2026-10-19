package com.billiards.store.common.enumerated;

/** Bucket size for time-series reports. The SQL unit is whitelisted here, never taken from user input. */
public enum Granularity {
    HOUR("hour"),
    DAY("day"),
    MONTH("month");

    private final String sqlUnit;

    Granularity(String sqlUnit) {
        this.sqlUnit = sqlUnit;
    }

    public String getSqlUnit() {
        return sqlUnit;
    }
}
