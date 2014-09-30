/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.axisregion;

import com.pfizer.mrbt.genomics.state.View;
import com.pfizerm.mrbt.axis.AxisScale;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *
 * @author henstockpv
 */
public abstract class AxisRegionY extends AxisRegion {

    protected abstract AxisScale getYAxis();

    protected abstract void drawTickLabel(Graphics2D g2, int yCenter, String str);
    
    protected abstract void drawYAxisTitle(Graphics2D g2);
    
    protected abstract void drawBorder(Graphics2D g2);
    
    public abstract Dimension getPreferredSize();

    public abstract Dimension getMaximumSize();

    public abstract Dimension getMinimumSize();
    
    protected abstract void zoomToRangeCall();
    protected abstract void zoomInCall();
    protected abstract void zoomOutCall();
    protected abstract void zoomToOriginalCall();
    
    public AxisRegionY(View view) {
        super(view);
    }
    
    
    public class MouseController implements MouseListener, MouseMotionListener {
        @Override
        public void mouseEntered(MouseEvent me) { }
        @Override
        public void mouseExited(MouseEvent me) { }
        @Override
        public void mouseClicked(MouseEvent me) { 
            clearDrag();
        }
        @Override
        public void mousePressed(MouseEvent me) { 
            isDragging = true;
            dragStart  = me.getY();
            dragEnd    = me.getY();
            System.out.println("Drag started " + dragStart);
        }
        @Override
        public void mouseReleased(MouseEvent me) { 
            int x = me.getX();
            x = Math.max(0, x);
            x = Math.min(x, getWidth());
            int y = me.getY();
            y = Math.max(0, y);
            y = Math.min(y, getHeight());
            showPopupMenu(x, y);
        }
        public void mouseDragged(MouseEvent me) { 
            dragEnd = me.getY();
            //System.out.println("Drag region " + dragStart + "\t" + dragEnd);
            repaint();
        }
        public void mouseMoved(MouseEvent me) { }
        
    }    

}
