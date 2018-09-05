/*
 * Dialog for creating or editing a line
 */
package com.pfizer.mrbt.genomics.hline;

import com.pfizer.mrbt.genomics.Singleton;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author henstockpv
 */
public class LineCreationPane extends JComponent {
    private JComboBox lineStyleComboBox;
    private JComboBox scopeComboBox;
    private AbstractButton colorButton;
    private JTextField levelField;
    private JTextField nameField;
    private Color initColor = Color.DARK_GRAY;
    private Color selectedColor = initColor;
    private String[] lineStyleOptions = {"Solid", "Dashed", "Dotted", "Dash Dot"};
    private String[] scopeOptions = {"This plot only", "All plots with same gene", "All plots with same model", "All plots "};
    public static final int COLOR_ICON_SIZE = 18;

    
    public LineCreationPane() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0,0,5,5);
        JLabel nameLabel = new JLabel("<html>Line name<br/><font size=\"2\">(optional))</font></html>");
        add(nameLabel, gbc);
        
        gbc.gridx = 20;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(15);
        add(nameField, gbc);
        
        gbc.gridx = 10;
        gbc.gridy = 20;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel levelLabel = new JLabel("-log10 P-Value");
        add(levelLabel, gbc);
        
        gbc.gridx = 20;
        gbc.gridy = 20;
        gbc.anchor = GridBagConstraints.WEST;
        
        levelField = new JTextField(7);
        add(levelField, gbc);
        
        gbc.gridx = 10;
        gbc.gridy = 30;
        JLabel scopeLabel = new JLabel("Scope of line:");
        gbc.anchor = GridBagConstraints.EAST;
        add(scopeLabel, gbc);
        
        gbc.gridx = 20;
        gbc.gridy = 30;
        gbc.anchor = GridBagConstraints.WEST;
        add(getScopeComboBox(), gbc);
        
        gbc.gridx = 10;
        gbc.gridwidth = 20;
        gbc.gridy = 33;
        JLabel multipleModelLabel = new JLabel(
                    "<html>Since the current view has multiple models<br/>" +
                    "only the first will be considered for setting the<br/>" +
                    "scope to all plots with the same model");
        add(multipleModelLabel, gbc);

        if(Singleton.getState().getMainView().getModels().size() <= 1) {
            multipleModelLabel.setVisible(false);
        }
    
        gbc.gridx = 10;
        gbc.gridy = 40;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel styleLabel = new JLabel("Line style");
        add(styleLabel, gbc);
        
        gbc.gridx = 20;
        gbc.gridy = 40;
        gbc.anchor = GridBagConstraints.WEST;
        add(getLineStyleComboBox(), gbc);
        
        gbc.gridx = 10;
        gbc.gridy = 50;
        JLabel colorLabel = new JLabel("Color:");
        gbc.anchor = GridBagConstraints.EAST;
        add(colorLabel, gbc);
        
        gbc.gridx = 20;
        gbc.gridy = 50;
        gbc.anchor = GridBagConstraints.WEST;
        add(getColorButton(initColor), gbc);
        
    }

    /**
     * Fills in all the fields with the appropriate values in hline
     * @param hline 
     */
    public void updateFields(HLine hline) {
        nameField.setText(hline.getLineName());

        DecimalFormat format = new DecimalFormat("#0.00");
        levelField.setText(format.format(hline.getyValue()));
        scopeComboBox.setSelectedIndex(hline.getLineScope());
        lineStyleComboBox.setSelectedIndex(hline.getLineStyle());
        changeColorButton(hline.getLineColor());
    }
    
    protected JComboBox getScopeComboBox() {
        if(scopeComboBox == null) {
            scopeComboBox = new JComboBox(scopeOptions);
        }
        return scopeComboBox;
    }
    
    protected JComboBox getLineStyleComboBox() {
        if(lineStyleComboBox == null) {
            lineStyleComboBox = new JComboBox(lineStyleOptions);
        }
        return lineStyleComboBox;
    }
    
        protected AbstractButton getColorButton(Color initColor) {
        if(colorButton == null) {
            colorButton = new JButton();
            changeColorButton(initColor);
            colorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    Color returnColor = JColorChooser.showDialog(null,
                            "Choose Filter Color", selectedColor);
                    if(returnColor != null) {
                        selectedColor = returnColor;
                        changeColorButton(selectedColor);
                    }
                }
            });
        }
        return colorButton;
    }

    /**
     * Changes the color of colorButton to a square icon of the given color
     * @param newColor
     */
    protected void changeColorButton(Color newColor) {
            Image colorImg = new BufferedImage(COLOR_ICON_SIZE, COLOR_ICON_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D) colorImg.getGraphics();
            g2.setColor(newColor);
            g2.fillRect(0, 0, COLOR_ICON_SIZE, COLOR_ICON_SIZE);
            ImageIcon icon = new ImageIcon(colorImg);
        colorButton.setIcon(icon);
    }
    
    /**
     * Returns the string entered in the levelField
     * @return 
     */
    public String getLogPValue() {
        return levelField.getText().trim();
    }
    
    public void setLogPValue(float value) {
        DecimalFormat format = new DecimalFormat("#0.00");
        levelField.setText(format.format(value));
    }
    
    /**
     * Returns the index of the selected line style
     * @return 
     */
    public int getLineStyle() {
            return lineStyleComboBox.getSelectedIndex();
    }
    
    public void setLineStyle(int index) {
        lineStyleComboBox.setSelectedIndex(index);
    }
    
    /**
     * Returns the name of the line
     * @return 
     */
    public String getLineName() {
        return nameField.getText().trim();
    }
    
    public void setLineName(String name) {
        nameField.setText(name);
    }
    
    public Color getLineColor() {
        return selectedColor;
    }
    
    public void setLineColor(Color color) {
        this.initColor = color;
        this.selectedColor = color;
    }
    
    /**
     * Returns the scope index of the combo box that has been selected
     */
    public int getLineScopeIndex() {
        return scopeComboBox.getSelectedIndex();
    }
    
    public void setScopeIndex(int scopeIndex) {
        scopeComboBox.setSelectedIndex(scopeIndex);
    }
    
    /**
     * Returns true if the line is valid--namely if it has a numeric y value
     * @return 
     */
    public boolean validateLine() {
        try {
            Float yval = Float.parseFloat(levelField.getText());
            return true;
        } catch(NumberFormatException nfe) {
            return false;
        }
    }
}
