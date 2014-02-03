/*
 * Display of the axis tick and labels only with special highlighting
 * 
 */
package com.pfizer.mrbt.genomics.axisregion;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.View;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.userpref.UserPrefListener;
import com.pfizerm.mrbt.axis.AxisScale;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Peter v. Henstock
 */
public abstract class AxisRegion extends JComponent {
    public final static int UNSELECTED = -100;
    public final static int TICK_LENGTH = 10;
    protected boolean isDragging = false;
    protected int dragStart = UNSELECTED;
    protected int dragEnd = UNSELECTED;
    public final static Color MUSTARD = new Color(238, 221, 130);
    protected View view;

    public AxisRegion(View view) {
        this.view = view;
        UserPrefController userPrefController = new UserPrefController();
        Singleton.getUserPreferences().addListener(userPrefController);

    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Singleton.getUserPreferences().getFrameColor());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Singleton.getUserPreferences().getFrameTextColor());
    }
    
    /**
     * Draws the region between dragStart and dragEnd if applicable
     */
    protected void drawDragZone(Graphics2D g2) {
        if(dragStart != UNSELECTED  && dragEnd != UNSELECTED) {
            int top = Math.min(dragStart, dragEnd);
            int diff = Math.abs(dragStart - dragEnd);
            g2.setColor(new Color(143, 188, 143));
            g2.fillRect(0, top, getWidth(), diff);
        }
    }    
    


    protected abstract void drawAxisTicks(Graphics2D g2);

    /**
     * Draws the position numbers of the ticks across the horizontal axis
     *
     * @param g2 Graphics2D
     * @param xCenter int
     */
    protected void drawTitleInformation(Graphics2D g2) {
        //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.BOLD, 16);
        DataModel dataModel = Singleton.getDataModel();
        String delimiter1 = "";
        String delimiter2 = "";
        String str = "XAxisLabel";
        if (str.length() > 0) {
            TextLayout tl = new TextLayout(str, font, frc);
            int x = 5;
            int y = getHeight() - 4;
            tl.draw(g2, x, y);
        }
    }

    protected void showPopupMenu(int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem zoomToSelection = new JMenuItem("Zoom to Region");
        zoomToSelection.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                zoomToRangeCall();
            }
        });
        popup.add(zoomToSelection);

        JMenuItem zoomOut = new JMenuItem("Zoom Out");
        popup.add(zoomOut);
        zoomOut.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                zoomOutCall();
            }
        });
        popup.add(zoomOut);

        JMenuItem zoomOriginal = new JMenuItem("Zoom to Original");
        zoomOriginal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                zoomToOriginalCall();
            }
        });
        popup.add(zoomOriginal);

        JMenuItem zoomIn = new JMenuItem("Zoom In");
        zoomIn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                zoomInCall();
                System.out.println("Selected zoom to region");
            }
        });
        popup.add(zoomIn);

        JMenuItem clearSelection = new JMenuItem("Clear Selection");
        clearSelection.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                System.out.println("Selected clear");
                clearDrag();
            }
        });
        popup.add(clearSelection);
        popup.show(this, x, y);
        //popup.addMenuKeyListener()
    }

    protected abstract void zoomToRangeCall();

    protected abstract void zoomInCall();

    protected abstract void zoomOutCall();

    protected abstract void zoomToOriginalCall();
    
    /**
     * Clears the drag region objects and redraws the screen
     */
    protected void clearDrag() {
        isDragging = false;
        dragStart = UNSELECTED;
        dragEnd = UNSELECTED;
        repaint();
    }

    /**
     * Assigns the local view to the new view followed by a repaint()
     *
     * @param view
     */
    public void setView(View view) {
        this.view = view;
        repaint();
    }

    public class UserPrefController implements UserPrefListener {
        @Override
        public void colorChanged(ChangeEvent ce) {
            repaint();
        }
    }
}
