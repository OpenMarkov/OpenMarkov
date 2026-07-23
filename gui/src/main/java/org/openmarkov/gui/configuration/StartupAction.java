package org.openmarkov.gui.configuration;

public enum StartupAction {
    SHOW_CREATE_NEW_NETWORK,
    RESTORE_LAST_GUI_DIMENSIONS,
    RESTORE_LAST_SESSION;
    
    public String checkBoxText() {
        return switch (this) {
            case SHOW_CREATE_NEW_NETWORK -> "Show 'create new network'";
            case RESTORE_LAST_GUI_DIMENSIONS -> "Restore window dimensions";
            case RESTORE_LAST_SESSION -> "Restore last session networks";
        };
    }
    
    public String toolTipText() {
        return switch (this) {
            case SHOW_CREATE_NEW_NETWORK -> """
                    <html>
                    When starting OpenMarkov, the "New network" dialog is opened.<br><br>
                    This only applies if no networks are opened when starting OpenMarkov.
                    </html>
                    """;
            case RESTORE_LAST_GUI_DIMENSIONS -> """
                    <html>
                    When starting OpenMarkov, the app's window will take the same size<br>
                    and location as the one used in your last session.
                    </html>
                    """;
            case RESTORE_LAST_SESSION -> """
                    <html>
                    When starting OpenMarkov, the networks used in last session are reopened.<br><br>
                    This only applies to networks saved into files. Unsaved networks or networks<br>
                    from URLs will not be restored.<br><br>
                    If you open multiple instances of OpenMarkov at the same time, only one the <br>
                    first one will restore the session.
                    </html>
                    """;
        };
    }
    
    
}
