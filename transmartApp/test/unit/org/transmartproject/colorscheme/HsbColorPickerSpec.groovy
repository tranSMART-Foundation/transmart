package org.transmartproject.colorscheme

import spock.lang.Specification

class HsbColorPickerSpec extends Specification {

	void 'test lowest value color'() {
		when:
		HsbColorPicker colorPicker = new HsbColorPicker(0, 100)
		List<Integer> lowestValColor = colorPicker.scaleLinearly(0)
		List<Integer> darkBlueRgb = [41, 0, 204]

		then:
		darkBlueRgb == lowestValColor
	}

	void 'test middle value color'() {
		when:
		HsbColorPicker colorPicker = new HsbColorPicker(0, 100)
		List<Integer> middleValColor = colorPicker.scaleLinearly(50)
		List<Integer> greenRgb = [0, 204, 20]

		then:
		greenRgb == middleValColor
	}

	void 'test highest value color'() {
		when:
		HsbColorPicker colorPicker = new HsbColorPicker(0, 100)
		List<Integer> highestValColor = colorPicker.scaleLinearly(100)
		List<Integer> darkRedRgb = [204, 0, 0]

		then:
		darkRedRgb == highestValColor
	}

	void 'test low bound outlier'() {
		when:
		HsbColorPicker colorPicker = new HsbColorPicker(0, 100)
		List<Integer> lowValColor = colorPicker.scaleLinearly(0)
		List<Integer> lowBoundOutlierColor = colorPicker.scaleLinearly(-1)

		then:
		lowValColor == lowBoundOutlierColor
	}

	void 'test high bound outlier'() {
		when:
		HsbColorPicker colorPicker = new HsbColorPicker(0, 100)

		List<Integer> highValColor = colorPicker.scaleLinearly(100)
		List<Integer> highBoundOutlierColor = colorPicker.scaleLinearly(101)

		then:
		highValColor == highBoundOutlierColor
	}
}
