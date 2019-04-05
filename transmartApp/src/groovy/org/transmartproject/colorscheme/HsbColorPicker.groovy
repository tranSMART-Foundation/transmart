package org.transmartproject.colorscheme

import groovy.transform.CompileStatic

import java.awt.Color

@CompileStatic
class HsbColorPicker {

    public static final float DEFAULT_COLOR_SATURATION = 1.0f
    public static final float DEFAULT_COLOR_BRIGHTNESS = 0.8f

    public static final float HUE_START = 0.7f

    private final float minValue
    private final float maxValue
    private final float colorSaturation
    private final float colorBrightness

    HsbColorPicker(float minValue, float maxValue, float colorSaturation, float colorBrightness) {
        this.minValue = minValue
        this.maxValue = maxValue
        this.colorSaturation = colorSaturation
        this.colorBrightness = colorBrightness
    }

    HsbColorPicker(float minValue, float maxValue) {
	this(minValue, maxValue, DEFAULT_COLOR_SATURATION, DEFAULT_COLOR_BRIGHTNESS)
    }

    /**
     * Return color in the color range from `HUE_START` (0.7 - dark blue) to 1 (red)
     * depending on value.
     */
    List<Integer> scaleLinearly(float value) {
	float k = (float) ((value - minValue) / (maxValue - minValue))
        if (k > 1) {
            k = 1
	}
	else if (k < 0) {
            k = 0
        }

	float colorHue = (float) (HUE_START * (1 - k))
	Color color = Color.getHSBColor(colorHue, colorSaturation, colorBrightness)

	[color.red, color.green, color.blue] as List
    }
}
