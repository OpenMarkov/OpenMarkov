package org.openmarkov.full.productTours.essentials;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.productTour.tour.MessagePlacement;
import org.openmarkov.gui.productTour.tour.Tour;
import org.openmarkov.gui.productTour.tour.TourEffects;
import org.openmarkov.gui.productTour.tour.action.ActionRequester;
import org.openmarkov.gui.productTour.tour.action.ClickKind;
import org.openmarkov.gui.productTour.tour.action.ClickRequest;
import org.openmarkov.gui.productTour.tour.action.TextRequest;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JMenuItem;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Ellipse2D;


public class FirstTour extends Tour {
    
    public static FirstTour INSTANCE = new FirstTour();
    
    public FirstTour() {
        super("Onboarding");
    }
    
    private MainGUI mainGUI;
    private @Nullable NetworkEditorPanel networkEditor;
    
    @Override protected void execute(ActionRequester actionRequester) {
        mainGUI = (MainGUI) this.getActiveWindow();
        var previousNetworkEditor = MainPanel.getCurrentNetworkEditorPanel();
        
        actionRequester.requestPopInfo(this.mainGUI.mainPanel, "Welcome to OpenMarkov.\nWe are going to create a network.",
                                       MessagePlacement.CENTER_OF_COMPONENT);
        
        actionRequester.requestClick(ClickRequest.of(this.findComponent("ButtonOpenNewNetwork"))
                                                 .reactionMode(ClickRequest.ReactionMode.BEFORE_LISTENERS)
                                                 .withMessage("First, click on the 'create network button'.", MessagePlacement.BOTTOM));
        
        actionRequester.requestPopInfo(this.findComponent("jComboBoxNetworkTypes"), "This is the type of network we are going to create.",
                                       MessagePlacement.BOTTOM,
                                       new TourEffects.DrawBorderOn(Color.GREEN, this.findComponent("jComboBoxNetworkTypes")));
        
        actionRequester.requestClick(ClickRequest.of(this.findComponent("jButtonApply"))
                                                 .reactionMode(ClickRequest.ReactionMode.BEFORE_LISTENERS)
                                                 .withMessage("Lets accept to create this network.", MessagePlacement.TOP));
        
        actionRequester.requestPopInfo(this.findComponent(org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel.class),
                                       "This area is the network area.\nThe network will be printed here.\nLet's start creating a new node.",
                                       MessagePlacement.CENTER_OF_COMPONENT);
        
        while (MainPanel.getCurrentNetworkEditorPanel() == previousNetworkEditor) {
        
        }
        this.networkEditor = MainPanel.getCurrentNetworkEditorPanel();
        
        
        actionRequester.requestClick(ClickRequest.of(this.networkEditor)
                                                 .at(() -> new Ellipse2D.Double(50, 50, 75, 75))
                                                 .clickKind(ClickKind.RIGHT_CLICK)
                                                 .reactionMode(ClickRequest.ReactionMode.BEFORE_LISTENERS)
                                                 .withMessage("First, do a right click here.", MessagePlacement.BOTTOM));
        
        actionRequester.requestClick(ClickRequest.of(this.findComponent("jmenuItemCreateChanceNode"))
                                                 .at(Component::getBounds),
                                     new TourEffects.ShowMessage(this.networkEditor, "Now go to Add, and then to Chance node", MessagePlacement.TOP_INSIDE));
        
        
        actionRequester.requestClick(ClickRequest.of(this.networkEditor)
                                                 .at(() -> new Ellipse2D.Double(150, 50, 75, 75))
                                                 .clickKind(ClickKind.RIGHT_CLICK)
                                                 .reactionMode(ClickRequest.ReactionMode.BEFORE_LISTENERS)
                                                 .withMessage("Now lets add a second node, do a right click here.", MessagePlacement.BOTTOM));
        
        actionRequester.requestClick(ClickRequest.of(this.findComponent("jmenuItemCreateChanceNode"))
                                                 .at(Component::getBounds),
                                     new TourEffects.ShowMessage(this.networkEditor, "Now go to Add, and then to Chance node", MessagePlacement.TOP_INSIDE));
        
        
        actionRequester.requestClick(ClickRequest.of(this.networkEditor, this.networkEditor.getVisualNetwork()
                                                                                           .getAllNodes()
                                                                                           .getFirst())
                                                 .clickKind(ClickKind.RIGHT_CLICK)
                                                 .withMessage("We are going to add a link from A to B.\nFirst, do a right click on A", MessagePlacement.TOP)
        );
        
        actionRequester.requestClick(ClickRequest.of(this.findComponent(JMenuItem.class, "NodeContextualMenuCreateLink"))
                                                 .at(Component::getBounds),
                                     new TourEffects.ShowMessage(this.networkEditor, "Now go select 'create link'", MessagePlacement.TOP_INSIDE
                                     ));
        
        actionRequester.requestClick(ClickRequest.of(this.networkEditor, this.networkEditor.getVisualNetwork()
                                                                                           .getAllNodes()
                                                                                           .get(1))
                                                 .withMessage("Now click on B to create the link", MessagePlacement.RIGHT)
        );
        
        
        actionRequester.requestClick(ClickRequest.of(this.networkEditor, this.networkEditor.getVisualNetwork()
                                                                                           .getAllNodes()
                                                                                           .get(1))
                                                 .reactionMode(ClickRequest.ReactionMode.BEFORE_LISTENERS)
                                                 .clickKind(ClickKind.DOUBLE_CLICK)
                                                 .withMessage("Now lets change B's name.\nFirst, double click on B", MessagePlacement.RIGHT)
        );
        
        actionRequester.requestText(new TextRequest(this.findComponent(JTextField.class, "jTextFieldNodeName"),
                                                    "C",
                                                    textComponent -> textComponent.getText().equalsIgnoreCase("c"))
                                            .withMessage("Change where it says 'B' to say 'C'", MessagePlacement.BOTTOM));
        
        actionRequester.requestClick(ClickRequest.of(this.findComponent("jButtonApply"))
                                                 .withMessage("Now click on accept", MessagePlacement.TOP)
        );
        
        actionRequester.requestClick(ClickRequest.of(this.networkEditor, this.networkEditor.getVisualNetwork()
                                                                                           .getAllNodes()
                                                                                           .get(1))
                                                 .reactionMode(ClickRequest.ReactionMode.BEFORE_LISTENERS)
                                                 .clickKind(ClickKind.DOUBLE_CLICK)
                                                 .withMessage("Now lets change A's name.\nFirst, double click on A", MessagePlacement.RIGHT)
        );
        
        actionRequester.requestText(new TextRequest(this.findComponent(JTextField.class, "jTextFieldNodeName"),
                                                    "My variable",
                                                    textComponent -> textComponent.getText()
                                                                                  .equalsIgnoreCase("My variable"))
                                            .withMessage("Change where it says 'A' to say 'My variable'", MessagePlacement.BOTTOM));
        
        actionRequester.requestClick(ClickRequest.of(this.findComponent("jButtonApply"))
                                                 .withMessage("Now click on accept", MessagePlacement.TOP)
        );
        
        
        NetworkEditorPanel component = this.findComponent(NetworkEditorPanel.class);
        actionRequester.requestPopInfo(component,
                                       "You have finished the tutorial.",
                                       MessagePlacement.TOP_INSIDE);
        
    }
    
    @Override protected void cleanUp() {
        if (this.networkEditor != null) {
            this.mainGUI.mainPanel.forceClose(this.networkEditor);
        }
    }
}
