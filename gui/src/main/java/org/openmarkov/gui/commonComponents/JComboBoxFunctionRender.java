package org.openmarkov.gui.commonComponents;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class JComboBoxFunctionRender<T> extends DefaultListCellRenderer {
    
    private final Function<T, String> mapper;
    
    public JComboBoxFunctionRender(Function<T, String> mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value == null) {
            return new JLabel();
        }
        this.setText(this.mapper.apply((T) value));
        return this;
    }
    
    
}
