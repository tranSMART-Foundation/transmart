/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.annotation;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.axisregion.AxisRegionYLeft;
import com.pfizer.mrbt.genomics.userpref.UserPrefListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public class AnnotationCorner extends JComponent {
    public AnnotationCorner() {
        super();
        UserPrefController userPrefController = new UserPrefController();
        Singleton.getUserPreferences().addListener(userPrefController);
    }
    
    /*public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        //g2.setColor(Singleton.getUserPreferences().getAnnotationColor());
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());
        System.out.println("Paint AnnotCorner " + this.getWidth() + "\t" + this.getHeight() + "\t" + this.getMinimumSize().getWidth());
    }*/
    
    public class UserPrefController implements UserPrefListener {
        @Override
        public void colorChanged(ChangeEvent ce) {
            repaint();
        }
    }
}
