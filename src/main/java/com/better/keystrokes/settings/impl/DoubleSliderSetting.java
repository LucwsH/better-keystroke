package com.better.keystrokes.settings.impl;

import com.better.keystrokes.settings.Setting;

public class DoubleSliderSetting extends Setting {
    public double valueMin, valueMax;
    public double min, max;
    public int decimalPlaces;

    public DoubleSliderSetting(String name, double valueMin, double valueMax, double min, double max, int decimalPlaces) {
        this.name = name;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        this.min = min;
        this.max = max;
        this.decimalPlaces = decimalPlaces;
    }

    public double getMinValue() { return valueMin; }
    public double getMaxValue() { return valueMax; }
    public double getSliderMinValue() { return min; }
    public double getSliderMaxValue() { return max; }

    private double round(double value) {
        double precision = Math.pow(10, decimalPlaces);
        return Math.round(Math.max(min, Math.min(max, value)) * precision) / precision;
    }

    public void setMinValue(double value) {
        this.valueMin = round(value);
        if (this.valueMin > this.valueMax) {
            this.valueMax = this.valueMin;
        }
    }

    public void setMaxValue(double value) {
        this.valueMax = round(value);
        if (this.valueMax < this.valueMin) {
            this.valueMin = this.valueMax;
        }
    }
}