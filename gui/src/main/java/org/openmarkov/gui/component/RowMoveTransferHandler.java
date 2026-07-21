package org.openmarkov.gui.component;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RowMoveTransferHandler extends TransferHandler {
    
    
    private final Supplier<? extends DefaultTableModel> getModel;
    private final Supplier<int[]> getSelectedRows;
    private final BiConsumer<? super Integer, ? super Integer> setSelectedRows;
    private final ArrayList<OnRowMoved> onRowMoved;
    
    private final DataFlavor localObjectFlavor;
    
    private boolean transferSourceIsTablePanel;
    private boolean isMovingRows;
    
    public RowMoveTransferHandler(Supplier<? extends DefaultTableModel> getModel,
                                  Supplier<int[]> getSelectedRows,
                                  BiConsumer<? super Integer, ? super Integer> setSelectedRows) {
        this.getModel = getModel;
        this.getSelectedRows = getSelectedRows;
        this.setSelectedRows = setSelectedRows;
        this.transferSourceIsTablePanel = false;
        this.onRowMoved = new ArrayList<>();
        try {
            this.localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"[I\"");
        } catch (ClassNotFoundException e) {
            throw new UnreachableException(e);
        }
    }
    
    public void onRowMoved(OnRowMoved onRowMoved) {
        this.onRowMoved.add(onRowMoved);
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        // Register this parent pane as the initiator of the drag action
        this.transferSourceIsTablePanel = true;
        
        // Grab selected rows using the pane's helper method 
        // (which looks at selection on whichever table has focus)
        int[] selectedRows = this.getSelectedRows.get();
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{RowMoveTransferHandler.this.localObjectFlavor};
            }
            
            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return RowMoveTransferHandler.this.localObjectFlavor.equals(flavor);
            }
            
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (isDataFlavorSupported(flavor)) {
                    return selectedRows;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }
    
    @Override
    public boolean canImport(TransferSupport info) {
        return this.transferSourceIsTablePanel &&
                info.isDrop() &&
                info.isDataFlavorSupported(this.localObjectFlavor);
    }
    
    public boolean importData(TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        
        JTable.DropLocation dropLocation = (JTable.DropLocation) info.getDropLocation();
        int targetRow = dropLocation.getRow();
        DefaultTableModel model = this.getModel.get();
        
        List<Integer> indexesOfRowsToMove;
        try {
            Object transferData = info.getTransferable().getTransferData(this.localObjectFlavor);
            indexesOfRowsToMove = Arrays.stream((int[]) transferData)
                                        .boxed()
                                        .sorted()
                                        .toList();
        } catch (UnsupportedFlavorException | IOException e) {
            throw new UnrecoverableException(e);
        }
        if (indexesOfRowsToMove.isEmpty()) {
            return false;
        }
        
        // This holds the order the rows should have after the movements have been applied.
        List<Integer> desiredRowOrder = new ArrayList<>();
        
        IntStream.range(0, targetRow)
                 .filter(i -> !indexesOfRowsToMove.contains(i))
                 .forEach(desiredRowOrder::add);
        
        desiredRowOrder.addAll(indexesOfRowsToMove);
        
        IntStream.range(0, model.getRowCount())
                 .filter(index -> !desiredRowOrder.contains(index))
                 .forEach(desiredRowOrder::add);
        
        // The starting order is every row from 0 to the last row index.
        List<Integer> currentRowOrder = IntStream.range(0, model.getRowCount()).boxed().collect(Collectors.toList());
        
        // We visit every row to check their index is the one they are expected to be in.
        for (int currentRow = 0; currentRow < model.getRowCount(); currentRow++) {
            int desiredIndex = desiredRowOrder.get(currentRow);
            int realIndex = currentRowOrder.indexOf(desiredIndex);
            
            //If the index is not the desired one, then move them to the desired one.
            if (realIndex != currentRow) {
                this.isMovingRows = true;
                model.moveRow(realIndex, realIndex, currentRow);
                this.isMovingRows = false;
                for (var onRowMoved : this.onRowMoved) {
                    onRowMoved.onRowMoved(realIndex, currentRow);
                }
                
                // Update the current order.
                Integer item = currentRowOrder.remove(realIndex);
                currentRowOrder.add(currentRow, item);
            }
        }
        
        // Re-select the rows the user was selecting.
        int finalDestinationStart = desiredRowOrder.indexOf(indexesOfRowsToMove.getFirst());
        this.setSelectedRows.accept(finalDestinationStart, finalDestinationStart + indexesOfRowsToMove.size() - 1);
        
        return true;
    }
    
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        this.transferSourceIsTablePanel = false;
    }
    
    public boolean isMovingRows() {
        return this.isMovingRows;
    }
    
    @FunctionalInterface
    public interface OnRowMoved {
        public void onRowMoved(int from, int to);
    }
    
}