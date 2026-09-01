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
        
        
        public static final GUIColor ATTENTION_BG = new GUIColor(new Color(212, 56, 56));
        public static final GUIColor ATTENTION_FG = new GUIColor(new Color(255, 255, 255));
        public static final GUIColor TRANSPARENT = new GUIColor(new Color(0, 0, 0, 0));
    }
    
    public static final class Network {
        
        public static final class Link{
            
            /** Black on the light canvas; on the dark one it would sit at 2.3:1, so it is lightened there. */
            public static final GUIColor FOREGOUND = new GUIColor(new Color(0, 0, 0))
                    .inDark(new Color(227, 227, 227));
            
            public static final class Creation{
                // The line that follows the pointer while a link is being drawn. Each colour keeps its
                // meaning in both themes: grey for nothing under the pointer, red for a target that
                // cannot take the link, blue for one that can.
                public static final GUIColor FOREGROUND_ON_SELECTS_NOTHING = new GUIColor(new Color(148, 148, 148))
                        .inDark(new Color(178, 178, 178));
                public static final GUIColor FOREGROUND_ON_SELECTS_FAILURE = new GUIColor(new Color(255, 0, 0))
                        .inDark(new Color(255, 143, 143));
                // The dark variant used to be darker than the light one, which is the wrong way round: on
                // the dark canvas it sat at 1.3:1.
                public static final GUIColor FOREGROUND_ON_SELECTS_SUCCESS = new GUIColor(new Color(0, 25, 209))
                        .inDark(new Color(178, 188, 255));
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
        public static final GUIColor REVELATION_ARC_VARIABLE = new GUIColor(new Color(128, 0, 0))
                .inDark(new Color(217, 141, 141));
        
        public static final GUIColor ALWAYS_OBSERVED = new GUIColor(new Color(128, 0, 0))
                .inDark(new Color(217, 141, 141));
        
        /*
         * The nodes are the same colour in both themes, on purpose: a chance node is yellow wherever it
         * is drawn. What changes with the theme is what is drawn on the canvas around them - the links -
         * because the canvas itself changes.
         *
         * Each type has one outline, and it is the hue of its own fill at 30% lightness and 70%
         * saturation. That keeps the outline saying which type the node is while clearing 3:1 against
         * both the canvas and the lighter fills of its type. The outlines used to be lighter than the
         * fills they surrounded: the outline of a utility node sat at 1.9:1 against the white canvas, so
         * the node had barely any outline at all, and the outline of a chance node with pre-resolution
         * evidence sat at 1.0:1 against its own fill, so it was not there.
         *
         * The text is black on every fill. On a light fill black is the highest contrast there is, and
         * every fill here is light enough that it beats white by a wide margin.
         */
        public static final class ChanceNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(251, 242, 153));
            public static final GUIColor FOREGROUND = new GUIColor(new Color(130, 120, 23));
            public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
            
            public static final GUIColor BACKGROUND_ON_PRE_RESOLUTION_FINDING = new GUIColor(new Color(197, 146, 95));
            public static final GUIColor BACKGROUND_ON_POST_RESOLUTION_FINDING = new GUIColor(new Color(225, 198, 143));
        }
        
        public static final class DecisionNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(205, 222, 249));
            public static final GUIColor FOREGROUND = new GUIColor(new Color(23, 64, 130));
            public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
            
            public static final GUIColor BACKGROUND_ON_POLICY = new GUIColor(new Color(89, 151, 253));
            // These two were the only greys left of the old palette, one grey for two different states.
            // They are now the blue of the decision node, at the same two steps the chance node takes.
            public static final GUIColor BACKGROUND_ON_PRE_RESOLUTION_FINDING = new GUIColor(new Color(95, 134, 197));
            public static final GUIColor BACKGROUND_ON_POST_RESOLUTION_FINDING = new GUIColor(new Color(143, 175, 225));
            
        }
        
        public static final class UtilityNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(208, 230, 178));
            public static final GUIColor BACKGROUND_WITH_EVENT = new GUIColor(new Color(91, 177, 43));
            public static final GUIColor FOREGROUND = new GUIColor(new Color(85, 130, 23));
            public static final GUIColor TEXT = new GUIColor(new Color(0, 0, 0));
        }
        
        public static final class EventNode {
            public static final GUIColor BACKGROUND = new GUIColor(new Color(255, 184, 97));
            public static final GUIColor BACKGROUND_TERMINAL = new GUIColor(new Color(230, 126, 0));
            public static final GUIColor BACKGROUND_INITIAL = new GUIColor(new Color(230, 126, 0));
            
            public static final GUIColor FOREGROUND = new GUIColor(new Color(130, 82, 23));
            public static final GUIColor FOREGROUND_TERMINAL = new GUIColor(new Color(130, 82, 23));
            public static final GUIColor FOREGROUND_INITIAL = new GUIColor(new Color(130, 82, 23));
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
        
        public static final GUIColor EDITING_BACKGROUND = new GUIColor(new Color(54, 54, 54));
        public static final GUIColor EDITING_FOREGROUND = new GUIColor(new Color(255, 255, 255));
        
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
        
        public static final GUIColor PROGRESS_BAR_FOREGROUND = new GUIColor(new Color(250, 203, 141));
        public static final GUIColor PROGRESS_BAR_BACKGROUND = new GUIColor(new Color(80, 70, 50));
        public static final GUIColor MESSAGE_FOREGROUND = new GUIColor(new Color(186, 182, 182));
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
