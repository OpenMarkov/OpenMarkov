package org.openmarkov.gui.productTour.tour;

public enum MessagePlacement {
    CENTER_OF_COMPONENT,
    TOP,
    BOTTOM,
    RIGHT,
    LEFT,
    TOP_INSIDE,
    BOTTOM_INSIDE,
    RIGHT_INSIDE,
    LEFT_INSIDE;
    
    public enum VerticalPlacement{
        TOP,
        CENTER,
        BOTTOM,
    }
    
    public enum HorizontalPlacement{
        LEFT,
        CENTER,
        RIGHT,
    }
    
    VerticalPlacement verticalPlacement(){
        return switch (this){
            case TOP, TOP_INSIDE -> VerticalPlacement.TOP;
            case BOTTOM, BOTTOM_INSIDE -> VerticalPlacement.BOTTOM;
            case RIGHT, RIGHT_INSIDE, CENTER_OF_COMPONENT, LEFT, LEFT_INSIDE -> VerticalPlacement.CENTER;
        };
    }
    
    HorizontalPlacement horizontalPlacement(){
        return switch (this){
            case TOP, TOP_INSIDE, CENTER_OF_COMPONENT, BOTTOM, BOTTOM_INSIDE -> HorizontalPlacement.CENTER;
            case RIGHT, RIGHT_INSIDE -> HorizontalPlacement.RIGHT;
            case LEFT, LEFT_INSIDE -> HorizontalPlacement.LEFT;
        };
    }
    
    boolean isInside(){
        return switch (this){
            case CENTER_OF_COMPONENT, LEFT_INSIDE, RIGHT_INSIDE, BOTTOM_INSIDE, TOP_INSIDE -> true;
            case TOP, LEFT, RIGHT, BOTTOM -> false;
        };
    }
    
}
