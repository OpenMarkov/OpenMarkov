package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.ThereIsNoPotentialInNodeException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.DefaultStates;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.action.MoveNodeEdit;
import org.openmarkov.gui.bindings.Binding;
import org.openmarkov.gui.bindings.BindingCommandGroup;
import org.openmarkov.gui.bindings.BindingCommandReactor;
import org.openmarkov.gui.bindings.Bindings;
import org.openmarkov.gui.dialog.node.NodeTypeInfo;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.graphic.VisualState;
import org.openmarkov.gui.loader.element.CursorLoader;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenu;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.util.GUIDefaultStates;

import javax.swing.ToolTipManager;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Handles all mouse and keyboard input for the {@link NetworkEditorPanel}.
 */
public class EditorCommandHandlerGenerator {
    
    private final NetworkEditorPanel networkEditorPanel;
    private final BindingCommandReactor bindingReactor;
    
    private final VisualNetwork visualNetwork;
    private final Graphics2D graphics;
    
    private double diffX;
    private double diffY;
    
    private final MouseAdapter preCommandMouseAdapter;
    private final KeyListener preCommandKeyListener;
    private final MouseAdapter postCommandMouseAdapter;
    private final KeyListener postCommandKeyListener;
    
    EditorCommandHandlerGenerator(NetworkEditorPanel networkEditorPanel) {
        this.networkEditorPanel = networkEditorPanel;
        this.visualNetwork = this.networkEditorPanel.getVisualNetwork();
        this.graphics = (Graphics2D) networkEditorPanel.getGraphics();
        
        this.preCommandMouseAdapter = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.requestFocus();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.requestFocus();
                // requestFocusInWindow(); Activate if nodes can't be moved by arrows.
                Graphics2D g = (Graphics2D) EditorCommandHandlerGenerator.this.networkEditorPanel.getGraphics();
                EditorCommandHandlerGenerator.this.cursorPosition.setLocation(EditorCommandHandlerGenerator.this.networkEditorPanel.getZoomManager()
                                                                                                                                   .screenToPanel(e.getX()),
                                                                              EditorCommandHandlerGenerator.this.networkEditorPanel.getZoomManager()
                                                                                                                                   .screenToPanel(e.getY()));
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                Graphics2D g = (Graphics2D) EditorCommandHandlerGenerator.this.networkEditorPanel.getGraphics();
                Point2D.Double point = new Point2D.Double(EditorCommandHandlerGenerator.this.networkEditorPanel.getZoomManager()
                                                                                                               .screenToPanel(e.getX()), EditorCommandHandlerGenerator.this.networkEditorPanel.getZoomManager()
                                                                                                                                                                                              .screenToPanel(e.getY()));
                EditorCommandHandlerGenerator.this.diffX = point.getX() - EditorCommandHandlerGenerator.this.cursorPosition.getX();
                EditorCommandHandlerGenerator.this.diffY = point.getY() - EditorCommandHandlerGenerator.this.cursorPosition.getY();
                
                EditorCommandHandlerGenerator.this.cursorPosition.setLocation(point);
                EditorCommandHandlerGenerator.this.lastMousePos = point;
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                Graphics2D g = (Graphics2D) EditorCommandHandlerGenerator.this.networkEditorPanel.getGraphics();
                Point2D.Double point = new Point2D.Double(EditorCommandHandlerGenerator.this.networkEditorPanel.getZoomManager()
                                                                                                               .screenToPanel(e.getX()), EditorCommandHandlerGenerator.this.networkEditorPanel.getZoomManager()
                                                                                                                                                                                              .screenToPanel(e.getY()));
                EditorCommandHandlerGenerator.this.cursorPosition.setLocation(point);
                EditorCommandHandlerGenerator.this.lastMousePos = point;
                if (EditorCommandHandlerGenerator.this.selectionState == SelectionState.CREATING_LINK) {
                    EditorCommandHandlerGenerator.this.networkEditorPanel.getVisualNetwork()
                                                                         .updateLinkCreation(point, g);
                    EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
                }
            }
            
        };
        this.preCommandKeyListener = new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {
            }
            
            @Override public void keyPressed(KeyEvent e) {
            }
            
            @Override public void keyReleased(KeyEvent e) {
            }
        };
        this.postCommandMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (EditorCommandHandlerGenerator.this.selectionState == SelectionState.CREATING_LINK) {
                    EditorCommandHandlerGenerator.this.networkEditorPanel.getVisualNetwork()
                                                                         .updateLinkCreation(EditorCommandHandlerGenerator.this.lastMousePos, EditorCommandHandlerGenerator.this.graphics);
                    EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
                }
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                //Tooltip of node
                if (EditorCommandHandlerGenerator.this.selectionState == SelectionState.NOTHING) {
                    if (EditorCommandHandlerGenerator.this.visualNodeOfToolTip != EditorCommandHandlerGenerator.this.networkEditorPanel.getVisualNetwork()
                                                                                                                                       .whatNodeInPosition(EditorCommandHandlerGenerator.this.cursorPosition, EditorCommandHandlerGenerator.this.graphics)) {
                        
                        EditorCommandHandlerGenerator.this.networkEditorPanel.setToolTipText(null);
                        //This forces to reset the tooltip "enter" timer when moving between visual elements.
                        ToolTipManager.sharedInstance().mousePressed(new MouseEvent(
                                EditorCommandHandlerGenerator.this.networkEditorPanel,
                                MouseEvent.MOUSE_EXITED,
                                System.currentTimeMillis(),
                                0,
                                0, 0,
                                0, false
                        ));
                    }
                    EditorCommandHandlerGenerator.this.visualNodeOfToolTip = EditorCommandHandlerGenerator.this.networkEditorPanel.getVisualNetwork()
                                                                                                                                  .whatNodeInPosition(EditorCommandHandlerGenerator.this.cursorPosition, EditorCommandHandlerGenerator.this.graphics);
                    if (EditorCommandHandlerGenerator.this.visualNodeOfToolTip instanceof VisualNode visualNode) {
                        EditorCommandHandlerGenerator.this.networkEditorPanel.setToolTipText(visualNode.getNode()
                                                                                                       .getComment());
                    }
                }
                // Cursor
                switch (EditorCommandHandlerGenerator.this.selectionState) {
                    case NOTHING -> {
                        switch (EditorCommandHandlerGenerator.this.networkEditorPanel.getBaseTool()) {
                            case SELECTION -> {
                            }
                            case LINK ->
                                    EditorCommandHandlerGenerator.this.networkEditorPanel.setCursor(CursorLoader.CURSOR_LINK.get());
                            case NODE -> {
                                boolean wouldCreateANode = EditorCommandHandlerGenerator.this.networkEditorPanel.getVisualNetwork()
                                                                                                                .whatNodeInPosition(EditorCommandHandlerGenerator.this.cursorPosition, EditorCommandHandlerGenerator.this.graphics) == null
                                        && EditorCommandHandlerGenerator.this.networkEditorPanel.getVisualNetwork()
                                                                                                .whatLinkInPosition(EditorCommandHandlerGenerator.this.cursorPosition, EditorCommandHandlerGenerator.this.graphics) == null;
                                Cursor cursor = null;
                                if (wouldCreateANode) {
                                    cursor = NodeTypeInfo.of(EditorCommandHandlerGenerator.this.networkEditorPanel.getPreferredNodeToCreate()).cursor.get();
                                }
                                EditorCommandHandlerGenerator.this.networkEditorPanel.setCursor(cursor);
                            }
                        }
                    }
                    case MOVING, CREATING_LINK, SELECTING -> {
                    }
                }
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void mouseClicked(MouseEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void mousePressed(MouseEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void mouseReleased(MouseEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void mouseEntered(MouseEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void mouseExited(MouseEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
        };
        this.postCommandKeyListener = new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ALT) {
                    e.consume();
                }
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
            
            @Override public void keyReleased(KeyEvent e) {
                EditorCommandHandlerGenerator.this.networkEditorPanel.repaint();
            }
        };
        
        
        this.bindingReactor = new BindingCommandReactor(
                BindingCommandGroup.of(Bindings.General.EXIT, Binding.MeetCondition.ON_GAIN, true, InputEvent.class, List.of(BindingCommandGroup.OnEvent.KEY_PRESSED, BindingCommandGroup.OnEvent.MOUSE_WHEEL_MOVED, BindingCommandGroup.OnEvent.MOUSE_RELEASED))
                                   .withAction((_, _) -> {
                                       switch (this.selectionState) {
                                           case NOTHING -> {
                                               this.networkEditorPanel.setBaseTool(NetworkEditorPanel.BaseTool.SELECTION);
                                           }
                                           case MOVING, SELECTING -> {
                                           }
                                           case CREATING_LINK -> {
                                               this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
                                               this.setSelectionState(SelectionState.NOTHING);
                                           }
                                       }
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.SELECT_STATE_EVIDENCE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.visualNetwork.getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .whatStateInPosition(this.cursorPosition, this.graphics) != null
                                           && !this.networkEditorPanel.getVisualNetwork()
                                                                      .whatNodeInPosition(this.cursorPosition, this.graphics)
                                                                      .isPreResolutionFinding()
                                   )
                                   .withAction((_, _) -> {
                                       VisualState visualState = this.networkEditorPanel.getVisualNetwork()
                                                                                        .whatStateInPosition(this.cursorPosition, this.graphics);
                                       VisualNode visualNode = this.networkEditorPanel.getVisualNetwork()
                                                                                      .whatNodeInPosition(this.cursorPosition, this.graphics);
                                       try {
                                           this.networkEditorPanel.getEvidenceManager()
                                                                  .toggleFinding(visualNode, visualState);
                                       } catch (IncompatibleEvidenceException | NotEvaluableNetworkException |
                                                NonProjectablePotentialException | NotEnoughMemoryException |
                                                DoEditException | CannotNormalizePotentialException |
                                                ConstraintViolatedException | ThereIsNoPotentialInNodeException ex) {
                                           throw new UnreachableException(ex);
                                       }
                                   }),
                
                
                BindingCommandGroup.of(Bindings.Network.SELECTION_ELEMENT, Binding.MeetCondition.ON_GAIN, false, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING &&
                                           this.visualNetwork.getElementInPosition(this.cursorPosition, this.graphics) != null)
                                   .withAction((_, _) -> {
                                       VisualElement selectedElement = this.visualNetwork.getElementInPosition(this.cursorPosition, this.graphics);
                                       this.networkEditorPanel.getVisualNetwork().setSelectedAllObjects(false);
                                       this.networkEditorPanel.getVisualNetwork()
                                                              .setSelectionOfElement(selectedElement, true);
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.ADD_ELEMENT_TO_SELECTION, Binding.MeetCondition.ON_GAIN, false, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING &&
                                           this.visualNetwork.getElementInPosition(this.cursorPosition, this.graphics) != null)
                                   .withAction((_, _) -> {
                                       VisualElement selectedElement = this.visualNetwork.getElementInPosition(this.cursorPosition, this.graphics);
                                       boolean isSelected = this.visualNetwork.isSelected(selectedElement);
                                       this.networkEditorPanel.getVisualNetwork()
                                                              .setSelectionOfElement(selectedElement, !isSelected);
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.SELECTION_RECTANGLE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION && this.selectionState == SelectionState.NOTHING &&
                                           this.visualNetwork.getElementInPosition(this.cursorPosition, this.graphics) == null)
                                   .withAction((_, _) -> {
                                       this.visualNetwork.setSelectedAllObjects(false);
                                       this.visualNetwork.startSelectionRectangle(this.cursorPosition);
                                       this.setSelectionState(SelectionState.SELECTING);
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.SELECTION_RECTANGLE, Binding.MeetCondition.HELD, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_MOVED, BindingCommandGroup.OnEvent.MOUSE_DRAGGED))
                                   .processIf((_, _) -> networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION && this.selectionState == SelectionState.SELECTING)
                                   .withAction((_, _) -> this.networkEditorPanel.getVisualNetwork()
                                                                                .updateSelectionRectangle(this.diffX, this.diffY, this.graphics)),
                
                BindingCommandGroup.of(Bindings.Network.SELECTION_RECTANGLE, Binding.MeetCondition.ON_LOSE, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_RELEASED))
                                   .processIf((_, _) -> networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION && this.selectionState == SelectionState.SELECTING)
                                   .withAction((e, _) -> {
                                       Point2D.Double position = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                                                           .screenToPanel(e.getX()),
                                                                                    this.networkEditorPanel.getZoomManager()
                                                                                                           .screenToPanel(e.getY()));
                                       this.networkEditorPanel.getVisualNetwork().finishSelectionRectangle(position);
                                       this.setSelectionState(SelectionState.NOTHING);
                                   }),
                
                
                BindingCommandGroup.of(Bindings.Network.CREATE_NEW_NODE_IN_NODE_CREATION, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.NODE
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null)
                                   .withAction((_, _) -> {
                                       EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), this.networkEditorPanel.getPreferredNodeToCreate(), this.cursorPosition, this.networkEditorPanel);
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.CREATE_NEW_LINK_IN_LINK_CREATION, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.LINK
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .whatNodeInPosition(this.cursorPosition, this.graphics) != null)
                                   .withAction((_, _) -> {
                                       var node = this.networkEditorPanel.getVisualNetwork()
                                                                         .whatNodeInPosition(this.cursorPosition, this.graphics);
                                       if (!this.networkEditorPanel.getVisualNetwork()
                                                                   .getSelectedNodes()
                                                                   .contains(node)) {
                                           this.networkEditorPanel.getVisualNetwork().setSelectedAllObjects(false);
                                           this.networkEditorPanel.getVisualNetwork().setSelectionOfElement(node, true);
                                       }
                                       this.networkEditorPanel.getVisualNetwork()
                                                              .startLinkCreation(this.cursorPosition, this.graphics, VisualNetwork.LinkCreationSourceDirection.PARENT,
                                                                                 false, this.networkEditorPanel.getVisualNetwork()
                                                                                                               .getSelectedNodes());
                                       this.setSelectionState(SelectionState.CREATING_LINK);
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.CREATE_LINK_SHORTCUT, Binding.MeetCondition.ON_GAIN, true, InputEvent.class, List.of(BindingCommandGroup.OnEvent.KEY_PRESSED, BindingCommandGroup.OnEvent.MOUSE_WHEEL_MOVED))
                                   .processIf((_, _) -> this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.selectionState == SelectionState.NOTHING
                                           && !this.networkEditorPanel.getVisualNetwork()
                                                                      .getSelectedNodes()
                                                                      .isEmpty())
                                   .withAction((_, _) -> {
                                       this.networkEditorPanel.getVisualNetwork()
                                                              .startLinkCreation(this.cursorPosition, (Graphics2D) this.networkEditorPanel.getGraphics(), VisualNetwork.LinkCreationSourceDirection.PARENT,
                                                                                 false, this.networkEditorPanel.getVisualNetwork()
                                                                                                               .getSelectedNodes());
                                       this.setSelectionState(SelectionState.CREATING_LINK);
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.CREATE_LINK_SHORTCUT, Binding.MeetCondition.ON_LOSE, true, InputEvent.class, List.of(BindingCommandGroup.OnEvent.KEY_RELEASED, BindingCommandGroup.OnEvent.MOUSE_WHEEL_MOVED))
                                   .processIf((_, _) -> this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.selectionState == SelectionState.CREATING_LINK)
                                   .withAction((_, _) -> {
                                       this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
                                       this.setSelectionState(SelectionState.NOTHING);
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.CREATE_NEW_LINK_IN_LINK_CREATION, Binding.MeetCondition.ON_LOSE, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_RELEASED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.CREATING_LINK && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.LINK)
                                   .withAction((e, _) -> {
                                       if (this.networkEditorPanel.getVisualNetwork()
                                                                  .whatNodeInPosition(this.cursorPosition, this.graphics) == null) {
                                           this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
                                       }
                                       Point2D.Double position = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                                                           .screenToPanel(e.getX()),
                                                                                    this.networkEditorPanel.getZoomManager()
                                                                                                           .screenToPanel(e.getY()));
                                       try {
                                           this.networkEditorPanel.getVisualNetwork()
                                                                  .finishLinkCreation(position, this.graphics);
                                       } catch (DoEditException ex) {
                                           Thread.currentThread()
                                                 .getUncaughtExceptionHandler()
                                                 .uncaughtException(Thread.currentThread(), ex);
                                           this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
                                           this.setSelectionState(SelectionState.NOTHING);
                                       } finally {
                                           this.setSelectionState(SelectionState.NOTHING);
                                       }
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.CREATE_NEW_LINK_IN_LINK_CREATION, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.CREATING_LINK && this.networkEditorPanel.getBaseTool() != NetworkEditorPanel.BaseTool.LINK)
                                   .withAction((e, _) -> {
                                       List<VisualNode> selectedNodes = this.networkEditorPanel.getVisualNetwork()
                                                                                               .getSelectedNodes();
                                       var dir = this.networkEditorPanel.getVisualNetwork().newLinksSourceDirection();
                                       Point2D.Double position = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                                                           .screenToPanel(e.getX()),
                                                                                    this.networkEditorPanel.getZoomManager()
                                                                                                           .screenToPanel(e.getY()));
                                       try {
                                           this.networkEditorPanel.getVisualNetwork()
                                                                  .finishLinkCreation(position, this.graphics);
                                       } catch (DoEditException ex) {
                                           Thread.currentThread()
                                                 .getUncaughtExceptionHandler()
                                                 .uncaughtException(Thread.currentThread(), ex);
                                           this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
                                           this.setSelectionState(SelectionState.NOTHING);
                                           return;
                                       }
                                       if (Bindings.Network.CREATE_LINK_SHORTCUT.isMet(Binding.MeetCondition.HELD, true)) {
                                           this.networkEditorPanel.getVisualNetwork()
                                                                  .startLinkCreation(position, this.graphics, VisualNetwork.LinkCreationSourceDirection.PARENT,
                                                                                     true, selectedNodes);
                                           this.setSelectionState(SelectionState.CREATING_LINK);
                                       }
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.TOGGLE_DIRECTION_IN_LINK_CREATION, Binding.MeetCondition.ON_GAIN, true, InputEvent.class, List.of(BindingCommandGroup.OnEvent.KEY_PRESSED, BindingCommandGroup.OnEvent.MOUSE_WHEEL_MOVED, BindingCommandGroup.OnEvent.MOUSE_RELEASED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.CREATING_LINK)
                                   .withAction((_, _) -> this.networkEditorPanel.getVisualNetwork()
                                                                                .toggleLinkCreationSource(this.lastMousePos)),
                
                BindingCommandGroup.of(Bindings.Network.EDIT_POTENTIAL, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.networkEditorPanel.getVisualNetwork()
                                                                               .whatNodeInPosition(this.cursorPosition, this.graphics) != null)
                                   .withAction((_, _) -> {
                                       var node = this.networkEditorPanel.getVisualNetwork()
                                                                         .whatNodeInPosition(this.cursorPosition, this.graphics);
                                       if (!node.isSelected()) {
                                           this.networkEditorPanel.getVisualNetwork().setSelectedAllObjects(false);
                                           this.networkEditorPanel.getVisualNetwork().setSelectedNode(node, true);
                                       }
                                       try {
                                           this.networkEditorPanel.showPotentialDialog(this.networkEditorPanel.getNetworkEditorPanel()
                                                                                                              .getWorkingMode() != NetworkEditorPanel.WorkingMode.EDITION);
                                       } catch (NotEvaluableNetworkException | IncompatibleEvidenceException |
                                                NonProjectablePotentialException | NotEnoughMemoryException |
                                                ConstraintViolatedException | CannotNormalizePotentialException |
                                                ThereIsNoPotentialInNodeException e) {
                                           throw new UnrecoverableException(e);
                                       } finally {
                                           this.networkEditorPanel.repaint();
                                       }
                                   }),
                
                BindingCommandGroup.of(Bindings.Network.DEFINE_CHANCE_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.CHANCE)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet())
                                   )
                                   
                                   .withAction((_, _) -> requestPropertiesAfterCreateNode(
                                           EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.CHANCE, this.cursorPosition, this.networkEditorPanel))),
                
                BindingCommandGroup.of(Bindings.Network.DEFINE_DECISION_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.DECISION)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet()))
                                   .withAction((_, _) -> requestPropertiesAfterCreateNode(
                                           EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.DECISION, this.cursorPosition, this.networkEditorPanel))),
                
                
                BindingCommandGroup.of(Bindings.Network.DEFINE_UTILITY_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.UTILITY)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet()))
                                   .withAction((_, _) -> requestPropertiesAfterCreateNode(
                                           EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.UTILITY, this.cursorPosition, this.networkEditorPanel))),
                
                
                BindingCommandGroup.of(Bindings.Network.DEFINE_EVENT_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.EVENT)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet()))
                                   .withAction((_, _) -> requestPropertiesAfterCreateNode(
                                           EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.EVENT, this.cursorPosition, this.networkEditorPanel))),
                
                
                BindingCommandGroup.of(Bindings.Network.CREATE_CHANCE_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.CHANCE)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet())
                                   )
                                   
                                   .withAction((_, _) ->
                                                       EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.CHANCE, this.cursorPosition, this.networkEditorPanel)),
                
                BindingCommandGroup.of(Bindings.Network.CREATE_DECISION_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.DECISION)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet()))
                                   .withAction((_, _) ->
                                                       EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.DECISION, this.cursorPosition, this.networkEditorPanel)),
                
                
                BindingCommandGroup.of(Bindings.Network.CREATE_UTILITY_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.UTILITY)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet()))
                                   .withAction((_, _) ->
                                                       EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.UTILITY, this.cursorPosition, this.networkEditorPanel)),
                
                
                BindingCommandGroup.of(Bindings.Network.CREATE_EVENT_NODE, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> this.selectionState == SelectionState.NOTHING
                                           && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.SELECTION
                                           && this.networkEditorPanel.getVisualNetwork()
                                                                     .getElementInPosition(this.cursorPosition, this.graphics) == null
                                           && NodeTypeInfo.of(NodeType.EVENT)
                                                          .canBeUsedInProbnet(this.visualNetwork.getProbNet()))
                                   .withAction((_, _) ->
                                                       EditorCommandHandlerGenerator.createNode(this.networkEditorPanel.getProbNet(), NodeType.EVENT, this.cursorPosition, this.networkEditorPanel)),
                
                
                BindingCommandGroup.of(Bindings.Network.CHANGE_PROPERTIES, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .processIf((_, _) -> switch (this.visualNetwork.getWorkingMode()) {
                                       case EDITION -> true;
                                       case INFERENCE -> this.networkEditorPanel.getVisualNetwork()
                                                                                .whatInnerBoxInPosition(this.cursorPosition, this.graphics) == null;
                                   })
                                   .withAction((_, _) -> {
                                       if (this.networkEditorPanel.getVisualNetwork()
                                                                  .whatLinkInPosition(this.cursorPosition, this.graphics) instanceof VisualLink link) {
                                           this.networkEditorPanel.changeLinkProperties(link);
                                       } else if (this.networkEditorPanel.getVisualNetwork()
                                                                         .whatNodeInPosition(this.cursorPosition, this.graphics) instanceof VisualNode node) {
                                           try {
                                               this.networkEditorPanel.changeNodeProperties(node, false);
                                           } catch (NotEvaluableNetworkException | NonProjectablePotentialException |
                                                    NotEnoughMemoryException | IncompatibleEvidenceException |
                                                    ConstraintViolatedException | CannotNormalizePotentialException |
                                                    ThereIsNoPotentialInNodeException e) {
                                               throw new UnrecoverableException(e);
                                           }
                                       } else {
                                           this.networkEditorPanel.changeNetworkProperties();
                                       }


//                                           node = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
//                                           if (node == null) {
//                                               createNode(this.networkEditorPanel.getProbNet(), NodeType.CHANCE, this.cursorPosition, this.networkEditorPanel);
//                                               node = this.networkEditorPanel.getVisualNetwork()
//                                                                             .whatNodeInPosition(this.cursorPosition, g);
//                                               this.lastLeftClickProducedANode = true;
//                                           }
//                                           try {
//                                               boolean userAcceptedChanges = this.networkEditorPanel.changeNodeProperties(node, this.lastLeftClickProducedANode);
//                                               if (!userAcceptedChanges && this.lastLeftClickProducedANode) {
//                                                   ArrayList<PNEdit> undone;
//                                                   do {
//                                                       undone = this.networkEditorPanel.getNetworkEditorPanel()
//                                                                                       .getProbNet()
//                                                                                       .getPNESupport()
//                                                                                       .undo();
//                                                   } while (undone != null && undone.stream().noneMatch(AddNodeEdit.class::isInstance));
//                                                   this.networkEditorPanel.getNetworkEditorPanel()
//                                                                          .getProbNet()
//                                                                          .getPNESupport()
//                                                                          .removeUndoneEdits();
//                                               }
//                                           } catch (NotEvaluableNetworkException | NonProjectablePotentialException |
//                                                    NotEnoughMemoryException |
//                                                    IncompatibleEvidenceException | ConstraintViolatedException |
//                                                    NotSupportedOperationException |
//                                                    CannotNormalizePotentialException ex) {
//                                               this.networkEditorPanel.repaint();
//                                               throw new UnrecoverableException(ex);
//                                           }
                                   
                                   
                                   }),
                
                
                BindingCommandGroup.of(Bindings.General.OPEN_CONTEXTUAL_MENU, Binding.MeetCondition.ON_GAIN, true, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_PRESSED))
                                   .withAction((e, _) -> {
                                       VisualElement selectedElement = this.visualNetwork.getElementInPosition(this.cursorPosition, this.graphics);
                                       ContextualMenu contextualMenu;
                                       if (selectedElement != null) {
                                           contextualMenu = this.getContextualMenu(selectedElement, this.networkEditorPanel);
                                           if (!this.visualNetwork.isSelected(selectedElement)) {
                                               this.visualNetwork.setSelectedAllObjects(false);
                                           }
                                           this.visualNetwork.setSelectionOfElement(selectedElement, true);
                                       } else {
                                           boolean canBeExpanded = this.networkEditorPanel.getNetworkEditorPanel()
                                                                                          .getProbNet()
                                                                                          .thereAreTemporalNodes();
                                           contextualMenu = this.contextualMenuFactory.getNetworkContextualMenu(canBeExpanded);
                                       }
                                       contextualMenu.show(this.networkEditorPanel, e.getX(), e.getY());
                                   }),
                
                
                BindingCommandGroup.of(Bindings.Network.MOVE_NODE_MOUSE, Binding.MeetCondition.HELD, false, MouseEvent.class, List.of(BindingCommandGroup.OnEvent.MOUSE_DRAGGED, BindingCommandGroup.OnEvent.MOUSE_MOVED))
                                   .processIf((_, _) -> switch (this.selectionState) {
                                       case NOTHING ->
                                               !this.networkEditorPanel.getVisualNetwork().getSelectedNodes().isEmpty();
                                       case MOVING -> true;
                                       case SELECTING, CREATING_LINK -> false;
                                   })
                                   .withAction((_, _) -> {
                                       this.setSelectionState(SelectionState.MOVING);
                                       this.networkEditorPanel.getVisualNetwork()
                                                              .moveSelectedElements(this.diffX, this.diffY);
                                   }),
                
                BindingCommandGroup.of(new BindingCommandGroup.BindingGroup.BindingList(Bindings.Network.MOVE_NODE_KEY_UP, Bindings.Network.MOVE_NODE_KEY_DOWN, Bindings.Network.MOVE_NODE_KEY_LEFT, Bindings.Network.MOVE_NODE_KEY_RIGHT),
                                       Binding.MeetCondition.HELD, false, InputEvent.class, List.of(BindingCommandGroup.OnEvent.KEY_PRESSED, BindingCommandGroup.OnEvent.MOUSE_WHEEL_MOVED))
                                   .processIf((_, _) -> switch (this.selectionState) {
                                       case NOTHING ->
                                               !this.networkEditorPanel.getVisualNetwork().getSelectedNodes().isEmpty();
                                       case MOVING -> true;
                                       case SELECTING, CREATING_LINK -> false;
                                   })
                                   .withAction((_, _) -> {
                                       int diffX = 0, diffY = 0;
                                       if (Bindings.Network.MOVE_NODE_KEY_UP.isMet(Binding.MeetCondition.HELD, true)) {
                                           diffY -= EditorCommandHandlerGenerator.NODE_SPEED_ON_ARROW_PRESS;
                                       }
                                       if (Bindings.Network.MOVE_NODE_KEY_DOWN.isMet(Binding.MeetCondition.HELD, true)) {
                                           diffY += EditorCommandHandlerGenerator.NODE_SPEED_ON_ARROW_PRESS;
                                       }
                                       if (Bindings.Network.MOVE_NODE_KEY_RIGHT.isMet(Binding.MeetCondition.HELD, true)) {
                                           diffX += EditorCommandHandlerGenerator.NODE_SPEED_ON_ARROW_PRESS;
                                       }
                                       if (Bindings.Network.MOVE_NODE_KEY_LEFT.isMet(Binding.MeetCondition.HELD, true)) {
                                           diffX -= EditorCommandHandlerGenerator.NODE_SPEED_ON_ARROW_PRESS;
                                       }
                                       if (diffX == 0 && diffY == 0) {
                                           return;
                                       }
                                       this.setSelectionState(SelectionState.MOVING);
                                       this.networkEditorPanel.getVisualNetwork().moveSelectedElements(diffX, diffY);
                                   }),
                
                BindingCommandGroup.of(new BindingCommandGroup.BindingGroup.BindingList(Bindings.Network.MOVE_NODE_MOUSE, Bindings.Network.MOVE_NODE_KEY_UP, Bindings.Network.MOVE_NODE_KEY_DOWN, Bindings.Network.MOVE_NODE_KEY_LEFT, Bindings.Network.MOVE_NODE_KEY_RIGHT),
                                       Binding.MeetCondition.NOT_PRESENT, true, InputEvent.class, List.of(BindingCommandGroup.OnEvent.KEY_RELEASED, BindingCommandGroup.OnEvent.MOUSE_WHEEL_MOVED, BindingCommandGroup.OnEvent.MOUSE_RELEASED))
                                   .reactingOn(Binding.MeetCondition.HELD, BindingCommandGroup.GroupReaction.ANY, Binding.MeetCondition.NOT_PRESENT, BindingCommandGroup.GroupReaction.ALL)
                                   .processIf((_, _) -> this.selectionState == SelectionState.MOVING)
                                   .withAction((_, _) -> this.tryFinishNodesMovements())
        
        
        );
    }
    
    private void requestPropertiesAfterCreateNode(VisualNode visualNode) {
        try {
            boolean userAcceptedChanges = this.networkEditorPanel.changeNodeProperties(visualNode, true);
            if (!userAcceptedChanges) {
                ArrayList<PNEdit> undone;
                do {
                    undone = this.networkEditorPanel.getNetworkEditorPanel()
                                                    .getProbNet()
                                                    .getPNESupport()
                                                    .undo();
                } while (undone != null && undone.stream().noneMatch(AddNodeEdit.class::isInstance));
                this.networkEditorPanel.getNetworkEditorPanel()
                                       .getProbNet()
                                       .getPNESupport()
                                       .removeUndoneEdits();
            }
        } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                 IncompatibleEvidenceException | ConstraintViolatedException | NotSupportedOperationException |
                 CannotNormalizePotentialException | ThereIsNoPotentialInNodeException ex) {
            this.networkEditorPanel.repaint();
            throw new UnrecoverableException(ex);
        }
    }
    
    private static final int NODE_SPEED_ON_ARROW_PRESS = 2;
    
    private SelectionState selectionState = SelectionState.NOTHING;
    
    
    private void tryFinishNodesMovements() {
        List<VisualNode> movedNodes = this.networkEditorPanel.getVisualNetwork().fillVisualNodesSelected();
        try {
            new MoveNodeEdit(movedNodes).executeEdit();
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        }
        this.networkEditorPanel.adjustPanelDimension();
        this.setSelectionState(SelectionState.NOTHING);
    }
    
    /**
     * Changes the state of the selection and carries out the necessary actions
     * in each case.
     *
     * @param newState new mouse state.
     */
    private void setSelectionState(SelectionState newState) {
        this.networkEditorPanel.setCursor(newState.getCursor());
        this.selectionState = newState;
    }
    
    
    private Point2D.Double lastMousePos;
    
    
    private VisualNode visualNodeOfToolTip;
    
    
    /**
     * Position of the mouse cursor when it is pressed.
     */
    private final Point2D.Double cursorPosition = new Point2D.Double();
    
    /**
     * Object that creates the contextual menus.
     */
    private ContextualMenuFactory contextualMenuFactory = null;
    
    
    /**
     * Retrieves the contextual menu that corresponds to the selectedElement.
     *
     * @return the contextual menu corresponding the the parameter.
     */
    private @Nullable ContextualMenu getContextualMenu(VisualElement selectedElement, NetworkEditorPanel panel) {
        return Optional.ofNullable(this.contextualMenuFactory)
                       .map(menuFactory -> menuFactory.getContextualMenu(selectedElement, panel))
                       .orElse(null);
    }
    
    public static VisualNode createNode(ProbNet currentNetwork, NodeType nodeType, Point2D.Double position, NetworkEditorPanel networkEditorPanel) {
        HashSet<String> existingNames = new HashSet<>();
        for (Node node : currentNetwork.getNodes()) {
            String name = node.getName();
            if (name.contains("[")) {
                existingNames.add(name.substring(0, name.indexOf(" [")));
            } else {
                existingNames.add(node.getName());
            }
        }
        String nodeName = Util.getNextNodeName(nodeType, existingNames);
        State[] states = DefaultStates.getStatesNodeType(nodeType, currentNetwork.getDefaultStates());
        for (int i = 0; i < states.length; i++) {
            states[i] = new State(GUIDefaultStates.getString(states[i].getName()));
        }
        Variable variable = new Variable(nodeName, states);
        if (currentNetwork.onlyTemporal()) {
            // default value
            variable.setBaseName(nodeName);
            variable.setTimeSlice(0);
        }
        List<Criterion> decisionCriteria = currentNetwork.getDecisionCriteria();
        if (nodeType == NodeType.UTILITY && decisionCriteria != null) {
            variable.setDecisionCriterion(decisionCriteria.getFirst());
        }
        try {
            currentNetwork.getPNESupport().setWithUndo(true);
            currentNetwork.getPNESupport().openNewSubEditHistory();
            PNEdit addNodeEdit = new AddNodeEdit(currentNetwork, variable, nodeType, position);
            addNodeEdit.executeEdit();
            var visualNode = networkEditorPanel.getVisualNetwork()
                                               .getAllNodes()
                                               .stream()
                                               .filter(node -> node.getNode().getVariable() == variable)
                                               .findFirst()
                                               .get();
            var visualNodeShape = visualNode.getShape((Graphics2D) networkEditorPanel.getGraphics());
            visualNode.setTemporalCoordinateX(visualNode.getTemporalPosition().x - (visualNodeShape.getBounds2D()
                                                                                                   .getWidth() / 2));
            visualNode.setTemporalCoordinateY(visualNode.getTemporalPosition().y - (visualNodeShape.getBounds2D()
                                                                                                   .getHeight() / 2));
            new MoveNodeEdit(List.of(visualNode)).executeEdit();
            currentNetwork.getPNESupport().closeSubEditHistory();
            return visualNode;
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        } finally {
            networkEditorPanel.adjustPanelDimension();
            networkEditorPanel.repaint();
        }
        
    }
    
    public boolean startLinkCreation(Point2D.Double cursorPosition, VisualNetwork.LinkCreationSourceDirection sourceDirection) {
        if (this.networkEditorPanel.getVisualNetwork().getSelectedNodes().isEmpty()) {
            return false;
        }
        this.networkEditorPanel.getVisualNetwork().startLinkCreation(cursorPosition,
                                                                     (Graphics2D) this.networkEditorPanel.getGraphics(),
                                                                     sourceDirection,
                                                                     false,
                                                                     this.networkEditorPanel.getVisualNetwork()
                                                                                            .getSelectedNodes());
        this.setSelectionState(SelectionState.CREATING_LINK);
        return true;
    }
    
    public VisualNode getVisualNodeOfToolTip() {
        return this.visualNodeOfToolTip;
    }
    
    void setContextualMenuFactory(ContextualMenuFactory contextualMenuFactory) {
        this.contextualMenuFactory = contextualMenuFactory;
    }
    
    public void applyListeners() {
        this.networkEditorPanel.removeMouseListener(this.preCommandMouseAdapter);
        this.networkEditorPanel.removeMouseMotionListener(this.preCommandMouseAdapter);
        this.networkEditorPanel.removeMouseWheelListener(this.preCommandMouseAdapter);
        this.networkEditorPanel.removeKeyListener(this.preCommandKeyListener);
        
        this.networkEditorPanel.removeMouseListener(this.postCommandMouseAdapter);
        this.networkEditorPanel.removeMouseMotionListener(this.postCommandMouseAdapter);
        this.networkEditorPanel.removeMouseWheelListener(this.postCommandMouseAdapter);
        this.networkEditorPanel.removeKeyListener(this.postCommandKeyListener);
        
        this.bindingReactor.addListenersTo(this.networkEditorPanel);
        
        this.networkEditorPanel.addMouseListener(this.preCommandMouseAdapter);
        this.networkEditorPanel.addMouseMotionListener(this.preCommandMouseAdapter);
        this.networkEditorPanel.addMouseWheelListener(this.preCommandMouseAdapter);
        this.networkEditorPanel.addKeyListener(this.preCommandKeyListener);
        
        this.networkEditorPanel.addMouseListener(this.postCommandMouseAdapter);
        this.networkEditorPanel.addMouseMotionListener(this.postCommandMouseAdapter);
        this.networkEditorPanel.addMouseWheelListener(this.postCommandMouseAdapter);
        this.networkEditorPanel.addKeyListener(this.postCommandKeyListener);
    }
    
}
