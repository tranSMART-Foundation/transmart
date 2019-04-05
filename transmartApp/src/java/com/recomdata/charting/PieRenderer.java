package com.recomdata.charting;

import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;

import java.awt.Color;
import java.util.List;

public class PieRenderer {
    private Color[] color;

    public PieRenderer(Color[] color) {
        this.color = color;
    }

    public void setColor(PiePlot plot, PieDataset dataset) {
        List<Comparable> keys = dataset.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            plot.setSectionPaint(keys.get(i), color[i % color.length]);
        }
    }
}
