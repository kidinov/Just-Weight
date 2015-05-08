package org.kidinov.justweight.model;

import com.orm.SugarRecord;

/**
 * Created by akid on 17/04/15.
 */
public class Weight extends SugarRecord<Weight> {
    private long date;
    private int value;
    private String unit;

    public Weight() {
    }

    public Weight(long date, int value, String unit) {
        this.date = date;
        this.value = value;
        this.unit = unit;
    }

    public long getDate() {
        return date;
    }

    public int getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Weight weight = (Weight) o;

        return date == weight.date;

    }

    @Override
    public int hashCode() {
        return (int) (date ^ (date >>> 32));
    }
}
