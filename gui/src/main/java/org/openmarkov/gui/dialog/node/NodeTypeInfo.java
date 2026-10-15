package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoEventNodes;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.gui.loader.element.CursorLoader;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.java.initialization.Lazy;

import java.awt.Cursor;
import java.util.Arrays;
import java.util.function.Predicate;

public final class NodeTypeInfo {

    public static NodeTypeInfo of(NodeType nodeType) {
        return NodeTypeInfo.NODE_TYPE_INFOS[nodeType.ordinal()];
    }

    public static final NodeTypeInfo[] NODE_TYPE_INFOS = Arrays.stream(NodeType.values()).map(nodeType -> switch (nodeType) {
        case CHANCE ->
                new NodeTypeInfo(nodeType, "Chance", "Chance", true, IconBind.CHANCE_ENABLED, CursorLoader.CURSOR_NODE_CHANCE, _ -> true);
        case DECISION ->
                new NodeTypeInfo(nodeType, "Decision", "Decision", true, IconBind.DECISION_ENABLED, CursorLoader.CURSOR_NODE_DECISION, probNet -> !probNet.hasConstraintOfClass(OnlyChanceNodes.class));
        case UTILITY ->
                new NodeTypeInfo(nodeType, "Utility", "Utility", true, IconBind.UTILITY_ENABLED, CursorLoader.CURSOR_NODE_UTILITY, probNet -> !probNet.hasConstraintOfClass(OnlyChanceNodes.class));
        case EVENT ->
                new NodeTypeInfo(nodeType, "Event", "Event", true, IconBind.EVENT_ENABLED, CursorLoader.CURSOR_NODE_EVENT, probNet -> !probNet.hasConstraintOfClass(OnlyChanceNodes.class) && !probNet.hasConstraintOfClass(NoEventNodes.class));
        case SV_SUM -> new NodeTypeInfo(nodeType, "sum", null, false, null, null, _ -> false);
        case SV_PRODUCT -> new NodeTypeInfo(nodeType, "product", null, false, null, null, _ -> false);
    }).toArray(NodeTypeInfo[]::new);

    public final NodeType nodeType;
    public final String codeName;
    public final String visualName;
    public final IconBind iconBind;
    public final boolean isVisuallyRepresented;
    public final Lazy<Cursor> cursor;
    private final Predicate<ProbNet> canBeUsedInProbnet;
    
    private NodeTypeInfo(NodeType nodeType, String codeName, String visualName, boolean isVisuallyRepresented, IconBind iconBind, Lazy<Cursor> cursor, Predicate<ProbNet> canBeUsedInProbnet) {
        this.nodeType = nodeType;
        this.codeName = codeName;
        this.visualName = visualName;
        this.isVisuallyRepresented = isVisuallyRepresented;
        this.iconBind = iconBind;
        this.cursor = cursor;
        this.canBeUsedInProbnet = canBeUsedInProbnet;
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public IconBind getIconBind() {
        return this.iconBind;
    }

    public Cursor getCursor() {
        return this.cursor.get();
    }

    public boolean isVisuallyRepresented() {
        return this.isVisuallyRepresented;
    }

    public String getVisualName() {
        return this.visualName;
    }

    public boolean canBeUsedInProbnet(ProbNet probNet) {
        return this.canBeUsedInProbnet.test(probNet);
    }

}
