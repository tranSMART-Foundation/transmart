/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author henstockpv
 */
public class FileSaveDialog extends JComponent {
    private JTextField filenameField;
    private AbstractButton selectButton;
    public FileSaveDialog() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 10;
        gbc.gridy = 10;
        add(new JLabel("Select output filename"), gbc);
        
        gbc.gridx = 20;
        gbc.gridy = 10;
        filenameField = new JTextField(30);
        add(filenameField, gbc);
        
        gbc.gridx = 30;
        gbc.gridy = 10;
        add(getSelectButton(), gbc);
    }
    
    protected AbstractButton getSelectButton() {
        if(selectButton == null) {
            selectButton = new JButton("Select");
            selectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JFileChooser jfc = new JFileChooser();
                    int returnVal = jfc.showSaveDialog(FileSaveDialog.this);
                    if(returnVal == JFileChooser.SAVE_DIALOG);
                }
            });
        }
        return selectButton;
    }
}
