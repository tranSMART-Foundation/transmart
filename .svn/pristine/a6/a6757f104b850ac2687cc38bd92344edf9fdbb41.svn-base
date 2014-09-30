/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.axisregion;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.AxisChangeEvent;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.View;
import com.pfizer.mrbt.genomics.state.ViewListener;
import com.pfizer.mrbt.genomics.userpref.UserPrefListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public abstract class CornerSquare extends JComponent {
    protected View view;
    protected AxisRegion xAxisRegion;
    protected AxisRegion yAxisRegion;
    protected AxisRegion yRightAxisRegion;
    public CornerSquare(View view, AxisRegion xAxisRegion, AxisRegion yAxisRegion, AxisRegion yRightAxisRegion) {
        this.view = view;
        this.xAxisRegion = xAxisRegion;
        this.yAxisRegion = yAxisRegion;
        this.yRightAxisRegion = yRightAxisRegion;
        
        UserPrefController userPrefController = new UserPrefController();
        Singleton.getUserPreferences().addListener(userPrefController);
        
        ViewController viewController = new ViewController();
        view.addListener(viewController);

        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        Graphics2D g2 = (Graphics2D) g;
        //g2.setColor(new Color(238, 221, 130));
        g2.setColor(Singleton.getUserPreferences().getFrameColor());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Singleton.getUserPreferences().getFrameTextColor());
        drawYAxisTicks(g2);
        drawRightYAxisTicks(g2);
        drawXAxisTicks(g2);
        drawBorder(g2);
    }
    
    
    protected abstract void drawXAxisTicks(Graphics2D g2);
    
    protected abstract void drawYAxisTicks(Graphics2D g2);
    
    protected abstract void drawRightYAxisTicks(Graphics2D g2);
    
    protected abstract void drawBorder(Graphics2D g2);
    
    public class UserPrefController implements UserPrefListener {
        @Override
        public void colorChanged(ChangeEvent ce) {
            repaint();
        }
        
    }
    
    public class ViewController implements ViewListener {
        @Override
        public void zoomChanged(AxisChangeEvent ce) {
                repaint();
        }
    }
    
    public class StateController implements StateListener {
      public void currentChanged(ChangeEvent ce) {}
      public void mainPlotChanged(ChangeEvent ce) {
        repaint();
      }
      public void thumbnailsChanged(ChangeEvent ce) {}
      public void currentAnnotationChanged(ChangeEvent ce) {}
      public void selectedAnnotationChanged(ChangeEvent ce) {}
      public void averagingWindowChanged(ChangeEvent ce) {}
      public void legendSelectedRowChanged(ChangeEvent ce) {}
      public void heatmapChanged(ChangeEvent ce) { }

    }
}
