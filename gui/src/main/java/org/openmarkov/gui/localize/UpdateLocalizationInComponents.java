package org.openmarkov.gui.localize;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.component.LastRecentFilesMenuItem;
import org.openmarkov.gui.menutoolbar.toolbar.ZoomComboBox;

import javax.swing.*;
import java.awt.*;

/**
 * Utility that recursively updates the foreground of all Swing components in a container
 * when the application language changes at runtime. Uses the component's {@code name}
 * property as the resource key for lookup in {@link StringDatabase}.
 */
public class UpdateLocalizationInComponents {
    
    /**
     * Method to change behaviours in a container using Java Reflection API. All
     * the different objects must comply with a strictly naming convention to
     * prevent string not to be updated properly. When the objects will be
     * created by programmers, the "name" property of the object must be set as
     * "ContainerOwner.ComponentVariableName" where the ContainerOwner is the
     * name of the container where the component belongs to. All other objects
     * must implement there own listeners.
     *
     * @param c the container to be updated
     */
    public static void allComponentsUpdateSetText(Container c) {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        String temp;
        Component[] listComponents = c.getComponents();
        for (Component item : listComponents) {
            if (item instanceof JButton) {
                if (!((JButton) item).getText().isEmpty()) {
                    temp = item.getName() + ".Text";
                    ((JButton) item).setText(stringDatabase.getString(temp));
                }
            } else if (item instanceof JDialog) {
                temp = item.getName() + ".Title.Text";
                ((JDialog) item).setTitle(stringDatabase.getString(temp));
                allComponentsUpdateSetText((Container) item);
            } else if (item instanceof JFrame) {
                temp = item.getName() + ".Title.Text";
                ((JFrame) item).setTitle(stringDatabase.getString(temp));
                allComponentsUpdateSetText((Container) item);
            } else if (item instanceof JLabel) {
                temp = item.getName() + ".Text";
                ((JLabel) item).setText(stringDatabase.getString(temp));
            } else if (item instanceof JMenu) {
                temp = item.getName();
                ((JMenu) item).setText(stringDatabase.getString(temp));
                temp = item.getName() + ".Mnemonic";
                ((JMenu) item).setMnemonic(stringDatabase.getString(temp).charAt(0));
                allComponentsUpdateSetText((Container) item);
            } else if (item instanceof JMenuItem) {
                temp = item.getName();
                ((JMenuItem) item).setText(stringDatabase.getString(temp));
                temp = item.getName() + ".Mnemonic";
                ((JMenuItem) item).setMnemonic(stringDatabase.getString(temp).charAt(0));
            } else if (item instanceof JPanel) {
                allComponentsUpdateSetText((Container) item);
            } else if (item instanceof JTextArea) {
                temp = item.getName() + ".Text";
                ((JTextArea) item).setText(stringDatabase.getString(temp));
            } else if (item instanceof JTextField) {
                temp = item.getName() + ".Text";
                ((JTextField) item).setText(stringDatabase.getString(temp));
            } else if (item instanceof ZoomComboBox) {
                temp = (String) ((ZoomComboBox) item).getSelectedItem();
                ((ZoomComboBox) item).setSelectedItem(temp);
            } else if (item instanceof Container) {
                allComponentsUpdateSetText((Container) item);
            }
            // do nothing for non registered objects as
            // those objects must implement the listener.
            // if required this method can be expanded
            
        } // end-for
        if (c instanceof JMenu) {
            temp = c.getName();
            ((JMenu) c).setText(stringDatabase.getString(temp));
            // extract JMenuItems
            int itemCount = ((JMenu) c).getItemCount();
            for (int i = 0; i < itemCount; i++) {
                JMenuItem item = ((JMenu) c).getItem(i);
                if (item instanceof JMenu) {
                    temp = item.getName();
                    item.setText(stringDatabase.getString(temp));
                    temp = item.getName() + ".Mnemonic";
                    item.setMnemonic(stringDatabase.getString(temp).charAt(0));
                    allComponentsUpdateSetText(item);
                } else if (item instanceof LastRecentFilesMenuItem) {
                    // do not change
                } else if (item instanceof JMenuItem) {
                    temp = item.getName();
                    item.setText(stringDatabase.getString(temp));
                    temp = item.getName() + ".Mnemonic";
                    item.setMnemonic(stringDatabase.getString(temp).charAt(0));
                }
            }
        }
    }
    
}
