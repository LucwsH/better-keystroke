package com.better.keystrokes.settings.impl;

import com.better.keystrokes.settings.Setting;

public class SliderSetting extends Setting {
    private double value;
    private double min, max;
    private int decimalPlaces;

    public SliderSetting(String name, double value, double min, double max, int decimalPlaces) {
        this.name = name;
        this.value = value;
        this.min = min;
        this.max = max;
        this.decimalPlaces = decimalPlaces;
    }

    public double getValue() {
        return value;
    }

    public double getMinValue() {
        return min;
    }



    public double getMaxValue() {
        return max;
    }

    public void setValue(double value) {
        double precision = Math.pow(10, decimalPlaces);
        this.value = Math.round(Math.max(min, Math.min(max, value)) * precision) / precision;
    }
}