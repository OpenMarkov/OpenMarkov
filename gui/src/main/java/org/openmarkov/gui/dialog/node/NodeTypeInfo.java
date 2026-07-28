package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoEventNodes;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.gui.loader.element.IconBind;

import java.util.Arrays;
import java.util.function.Predicate;

public final class NodeTypeInfo {

    public final NodeType nodeType;
    public final IconBind iconBind;
    public final boolean isVisuallyRepresented;

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public IconBind getIconBind() {
        return this.iconBind;
    }

    public boolean isVisuallyRepresented() {
        return this.isVisuallyRepresented;
    }

    private final Predicate<ProbNet> canBeUsedInProbnet;

    public static final NodeTypeInfo[] NODE_TYPE_INFOS = Arrays.stream(NodeType.values()).map(nodeType -> switch (nodeType) {
        case CHANCE -> new NodeTypeInfo(nodeType, true, IconBind.CHANCE_ENABLED, _ -> true);
        case DECISION ->
                new NodeTypeInfo(nodeType, true, IconBind.DECISION_ENABLED, probNet -> !probNet.hasConstraintOfClass(OnlyChanceNodes.class));
        case UTILITY ->
                new NodeTypeInfo(nodeType, true, IconBind.UTILITY_ENABLED, probNet -> !probNet.hasConstraintOfClass(OnlyChanceNodes.class));
        case EVENT ->
                new NodeTypeInfo(nodeType, true, IconBind.EVENT_ENABLED, probNet -> !probNet.hasConstraintOfClass(OnlyChanceNodes.class) && !probNet.hasConstraintOfClass(NoEventNodes.class));
        case SV_SUM, SV_PRODUCT -> new NodeTypeInfo(nodeType, false, null, _ -> false);
    }).toArray(NodeTypeInfo[]::new);

    private NodeTypeInfo(NodeType nodeType, boolean isVisuallyRepresented, IconBind iconBind, Predicate<ProbNet> canBeUsedInProbnet) {
        this.nodeType = nodeType;
        this.isVisuallyRepresented = isVisuallyRepresented;
        this.iconBind = iconBind;
        this.canBeUsedInProbnet = canBeUsedInProbnet;
    }

    public boolean canBeUsedInProbnet(ProbNet probNet) {
        return this.canBeUsedInProbnet.test(probNet);
    }

    public static NodeTypeInfo of(NodeType nodeType) {
        return NodeTypeInfo.NODE_TYPE_INFOS[nodeType.ordinal()];
    }
}
