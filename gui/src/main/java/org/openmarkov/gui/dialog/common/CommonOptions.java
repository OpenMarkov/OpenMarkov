package org.openmarkov.gui.dialog.common;

public class CommonOptions {
    
    public enum YesNo implements OptionDialog.ToOptionsDialog {
        YES,
        NO;
        
        @Override public String toString() {
            String lowerCase = super.toString().toLowerCase();
            return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
        }
    }
    
    public enum YesNoCancel implements OptionDialog.ToOptionsDialog {
        YES,
        NO,
        CANCEL;
        
        @Override public String toString() {
            String lowerCase = super.toString().toLowerCase();
            return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
        }
    }
    
    
}