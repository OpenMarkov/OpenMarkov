package org.openmarkov.gui.component;

import org.openmarkov.gui.configuration.Theme;

import javax.swing.JFrame;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.util.stream.Stream;

public class StickyColumnsTablePane_With_RowMoveTransferHandler_Demo extends JFrame {
    
    
    static void main(String[] args) throws Exception {
        Theme.updateInterfaceToLook();
        
        JFrame dialog = new JFrame();
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dialog.setLocationRelativeTo(null);
        
        StickyColumnsTablePane tablePane = new StickyColumnsTablePane(
                OMTableModel.construct(true,
                                       Stream.of(
                                               Stream.of("Name", "Age", "DNI"),
                                               Stream.of("Jorge1", 23, "111111111111111"),
                                               Stream.of("Jorge2", 23, "222222222222222"),
                                               Stream.of("Jorge3", 23, "333333333333333"),
                                               Stream.of("Jorge4", 23, "444444444444444"),
                                               Stream.of("Jorge5", 23, "555555555555555"),
                                               Stream.of("Jorge6", 23, "666666666666666"),
                                               Stream.of("Jorge7", 23, "777777777777777")
                                       )
                )
                , 1
        
        );
        tablePane.makeRowsMovable().onRowMoved((from, to) -> {
            System.out.println("Detected movement from " + from + " to " + to);
        });
        tablePane.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getFirstRow() != e.getLastRow()) {
                System.out.println("Moved from " + e.getFirstRow() + " to " + e.getLastRow());
            }
            
            System.out.println("Model Changed");
            System.out.println("Type is " + (e.getType() == TableModelEvent.UPDATE ? "Update" : e.getType() == TableModelEvent.INSERT ? "Insert" : "Delete"));
            System.out.println(e.getFirstRow());
            System.out.println(e.getLastRow());
        });
        
        dialog.getContentPane().add(tablePane, BorderLayout.CENTER);
        dialog.pack();
        dialog.setVisible(true);
    }
}
