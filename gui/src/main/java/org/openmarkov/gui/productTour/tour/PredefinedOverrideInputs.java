package org.openmarkov.gui.productTour.tour;

import java.awt.Component;
import java.awt.event.MouseEvent;

public class PredefinedOverrideInputs {
    
    
    public static OverrideInput allowHover(Component target) {
        return new OverrideInput(target, true)
                .allowProcessMouseEventWhen((keyEvent, component) ->
                                                    component == target && (
                                                            keyEvent.getID() == MouseEvent.MOUSE_ENTERED || keyEvent.getID() == MouseEvent.MOUSE_EXITED
                                                                    || keyEvent.getID() == MouseEvent.MOUSE_MOVED
                                                    )
                );
        
    }
    
}
