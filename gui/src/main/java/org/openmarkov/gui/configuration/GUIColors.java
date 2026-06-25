package org.openmarkov.gui.configuration;


import org.openmarkov.gui.component.ValuesTableCellRenderer;

import java.awt.Color;
import java.util.List;

public class GUIColors {
    
    private GUIColors() {
    }
    
    public static final class General {
        public static final GUIColor CORRECT = new GUIColor(new Color(0, 255, 0));
        public static final GUIColor WRONG = new GUIColor(new Color(255, 0, 0));
        public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
        
        public static final GUIColor ATTENTION = new GUIColor(new Color(212, 56, 56));
        public static final GUIColor TRANSPARENT = new GUIColor(new Color(0, 0, 0, 0));
    }
    
    public static final class Network {
        
        public static final class Link{
            
            public static final GUIColor FOREGOUND = new GUIColor(new Color(0, 0, 0));
            
            public static final class Creation{
                public static final GUIColor FOREGROUND_ON_SELECTS_NOTHING = new GUIColor(new Color(178, 178, 178));
                public static final GUIColor FOREGROUND_ON_SELECTS_FAILURE = new GUIColor(new Color(255, 0, 0));
                public static final GUIColor FOREGROUND_ON_SELECTS_SUCCESS = new GUIColor(new Color(0, 25, 209))
                        .inDark(new Color(0, 21, 174));
            }
            
            public static final class LinkRestriction {
                public static final GUIColor INCOMPATIBILITY_BACKGROUND = new GUIColor(new Color(255, 88, 88));
                public static final GUIColor COMPATIBILITY_BACKGROUND = new GUIColor(new Color(174, 255, 174));
                
                public static final GUIColor INCOMPATIBILITY_FOREGROUND = new GUIColor(new Color(255, 255, 255));
                public static final GUIColor COMPATIBILITY_FOREGROUND = new GUIColor(new Color(0, 0, 0));
            }
        }
        
        public static final GUIColor BACKGROUND = new GUIColor(new Color(255, 255, 255))
                .inDark(new Color(69, 72, 74));
        public static final GUIColor REVELATION_ARC_VARIABLE = new GUIColor(new Color(128, 0, 0));
        
        public static final GUIColor ALWAYS_OBSERVED = new GUIColor(new Color(128, 0, 0));
        
        public static final class ChanceNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(251, 231, 153));
            public static final GUIColor FOREGROUND = new GUIColor(new Color(184, 153, 29));
            public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
            
            public static final GUIColor BACKGROUND_ON_PRE_RESOLUTION_FINDING = new GUIColor(new Color(208, 185, 87));
            public static final GUIColor BACKGROUND_ON_POST_RESOLUTION_FINDING = new GUIColor(new Color(233, 210, 122));
        }
        
        public static final class DecisionNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(206, 223, 250));
            public static final GUIColor FOREGROUND = new GUIColor(new Color(33, 91, 196));
            public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
            
            public static final GUIColor BACKGROUND_ON_POLICY = new GUIColor(new Color(112, 142, 184));
            public static final GUIColor BACKGROUND_ON_PRE_RESOLUTION_FINDING = new GUIColor(new Color(128, 128, 128));
            public static final GUIColor BACKGROUND_ON_POST_RESOLUTION_FINDING = new GUIColor(new Color(192, 192, 192));
            
        }
        
        public static final class UtilityNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(220, 240, 197));
            public static final GUIColor BACKGROUND_WITH_EVENT = new GUIColor(new Color(93, 155, 49));
            public static final GUIColor FOREGROUND = new GUIColor(new Color(131, 210, 84));
            public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
        }
        
        public static final class EventNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(233, 170, 114));
            public static final GUIColor BACKGROUND_TERMINAL = new GUIColor(new Color(230, 126, 0));
            public static final GUIColor BACKGROUND_INITIAL = new GUIColor(new Color(230, 126, 0));
            
            public static final GUIColor FOREGROUND = new GUIColor(new Color(226, 130, 45));
            public static final GUIColor FOREGROUND_TERMINAL = new GUIColor(new Color(160, 95, 17));
            public static final GUIColor FOREGROUND_INITIAL = new GUIColor(new Color(160, 95, 17));
            public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
        }
        
    }
    
    public static final class DecisionTree {
        public static final GUIColor BACKGROUND = Network.BACKGROUND;
        public static final GUIColor WINDOW = new GUIColor(new Color(0, 0, 255));
    }
    
    public static final class Tables {
        
        public static final List<GUIColor> HEADER_FOREGROUND_COLORS = List.of(
                new GUIColor(new Color(124, 107, 33)),
                new GUIColor(new Color(128, 0, 64)),
                new GUIColor(new Color(10, 51, 188)),
                new GUIColor(new Color(107, 169, 52))
        );
        
        public static final GUIColor HEADER_BACKGROUND = new GUIColor(new Color(220, 220, 220));
        public static final GUIColor FROZEN_CELL_BACKGROUND = new GUIColor(new Color(192, 192, 192));
        public static final GUIColor FROZEN_CELL_FOREGROUND = new GUIColor(new Color(0, 0, 0));
        
        public static final ValuesTableCellRenderer.EditableCellColor EDITABLE_CELL_COLOR = (isSelected, rowIndex, columnIndex) -> {
            ValuesTableCellRenderer.CellColor cellColor = new ValuesTableCellRenderer.CellColor();
            switch (rowIndex % 2) {
                case 0 -> {
                    cellColor.foreground=new GUIColor(new Color(0, 0, 0))
                            .inDark(new Color(255, 255, 255));
                    cellColor.background=new GUIColor(new Color(255, 255, 255))
                            .inDark(new Color(100, 100, 100));
                }
                default -> {
                    cellColor.foreground=new GUIColor(new Color(0, 0, 0))
                            .inDark(new Color(255, 255, 255));
                    cellColor.background=new GUIColor(new Color(238, 242, 255))
                            .inDark(new Color(115, 115, 115));
                }
            };
            if (isSelected) {
                cellColor.foreground=new GUIColor(new Color(255, 255, 255))
                        .inDark(new Color(255, 255, 255));
                cellColor.background=new GUIColor(new Color(82, 82, 82))
                        .inDark(new Color(0, 0, 0));
            }
            return cellColor;
        };
        
        
        public static final class KeyTable {
            public static final GUIColor GRID_COLOR = new GUIColor(new Color(64, 64, 64)).negativizeInDark();
            public static final GUIColor SELECTION_BACKGROUND_COLOR = new GUIColor(new Color(211, 211, 211)).negativizeInDark();
            public static final GUIColor SELECTION_FOREGROUND_COLOR = new GUIColor(new Color(0, 0, 0)).negativizeInDark();
            public static final GUIColor BACKGROUND_COLOR = new GUIColor(new Color(230, 230, 250))
                    .inDark(new Color(61, 61, 68));
            
        }
        
        public static final class ValuesTable {
            public static final GUIColor GRID_COLOR = new GUIColor(new Color(128, 128, 128));
            public static final GUIColor UNCERTAINTY_BACKGROUND = new GUIColor(new Color(255, 255, 255));
            public static final GUIColor OPTIMAL_POLICY = new GUIColor(new Color(80, 220, 95));
        }
    }
    
    public static final class CostEffectiveness {
        public static final GUIColor WTP_SLOPE = new GUIColor(new Color(0, 0, 0));
        public static final GUIColor BACKGROUND = new GUIColor(new Color(255, 255, 255));
    }
    
    public static final class DevelopmentTools {
        public static final class EditHistory {
            public static final GUIColor EDIT_TO_REDO_BACKGROUND = General.CORRECT;
            public static final GUIColor EDIT_TO_UNDO_BACKGROUND = General.WRONG;
        }
    }
    
    public static final class Graphics {
        public static final GUIColor DEFAULT_BACKGROUND_COLOR = new GUIColor(new Color(192, 192, 192)).inDark(new Color(64, 64, 64));
        public static final GUIColor DEFAULT_BOX_BORDER_COLOR = new GUIColor(new Color(0, 0, 0)).inDark(new Color(255, 255, 255));
    }
    
    public static final class SplashScreen {
        
        public static final GUIColor PROGRESS_BAR_FOREGROUND = new GUIColor(new Color(10, 110, 230));
        public static final GUIColor PROGRESS_BAR_BACKGROUND = new GUIColor(new Color(255, 255, 255));
    }
    
    public static final class TemporalEvoluation {
        public static final GUIColor BACKGROUND = new GUIColor(new Color(255, 255, 255));
        public static final GUIColor DOMAIN = new GUIColor(new Color(64, 64, 64));
    }
    
    public static final class Inference {
        public static final GUIColor BOX_BACKGROUND = new GUIColor(new Color(255, 255, 255));
        public static final GUIColor BOX_FOREGROUND = new GUIColor(new Color(0, 0, 0));
        public static final GUIColor BOX_TEXT = new GUIColor(new Color(0, 0, 0));
        public static final GUIColor STATE_BAR_BORDER = new GUIColor(new Color(0, 0, 0));
        
        public record EvidenceCaseColor(GUIColor background, GUIColor foreground) {
        }
        
        public static final List<EvidenceCaseColor> EVIDENCE_CASES_COLORS = List.of(
                new EvidenceCaseColor(new GUIColor(new Color(255, 0, 0)), new GUIColor(new Color(255, 255, 255))),
                new EvidenceCaseColor(new GUIColor(new Color(0, 0, 255)), new GUIColor(new Color(255, 255, 255))),
                new EvidenceCaseColor(new GUIColor(new Color(0, 190, 0)), new GUIColor(new Color(255, 255, 255))),
                new EvidenceCaseColor(new GUIColor(new Color(255, 0, 255)), new GUIColor(new Color(0, 0, 0))),
                new EvidenceCaseColor(new GUIColor(new Color(255, 153, 51)), new GUIColor(new Color(0, 0, 0)))
        );
        
    }
    
    public static final class SensitivityAnalysis {
        public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
        public static final GUIColor POINT_PER_PARAMETER_BACKGROUND = new GUIColor(new Color(255, 255, 255));
        public static final GUIColor PLOT_BACKGROUND = new GUIColor(new Color(0, 0, 255));
        public static final GUIColor CHART_BACKGROUND = new GUIColor(new Color(255, 255, 255));
        
        public static final List<GUIColor> BAR_COLORS = List.of(new GUIColor(new Color(255, 0, 0)),
                                                                new GUIColor(new Color(0, 0, 255)),
                                                                new GUIColor(new Color(0, 255, 0)),
                                                                new GUIColor(new Color(255, 255, 0)),
                                                                new GUIColor(new Color(255, 0, 255)),
                                                                new GUIColor(new Color(0, 255, 255)),
                                                                new GUIColor(new Color(255, 200, 0)),
                                                                new GUIColor(new Color(255, 175, 175)),
                                                                new GUIColor(new Color(128, 128, 128)),
                                                                new GUIColor(new Color(192, 192, 192)),
                                                                new GUIColor(new Color(64, 64, 64)));
        
    }
    
    public static final class FastMenu {
        
        public static final GUIColor OPTION_BACKGROUND = new GUIColor(new Color(255, 255, 255));
        
        public static final class Radial {
            public static final GUIColor CIRCLE_BACKGROUND = new GUIColor(new Color(200, 219, 220, 50));
            public static final GUIColor CIRCLE_OUTLINE = new GUIColor(new Color(130, 178, 180, 140));
            public static final GUIColor CIRCLE_CENTER = new GUIColor(new Color(130, 176, 180, 160));
        }
    }
    
}
