package org.openmarkov.gui.commonComponents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.Localizable;

public class GeneralMode<T extends Localizable> {
    
    private final boolean specifyEach;
    private final String alternateNameInSpecifyEach;
    private final @Nullable T applyOnAll;
    
    private GeneralMode(boolean specifyEach, String alternateNameInSpecifyEach, @Nullable T applyOnAll) {
        this.specifyEach = specifyEach;
        this.alternateNameInSpecifyEach = alternateNameInSpecifyEach;
        this.applyOnAll = applyOnAll;
    }
    
    public static <T extends Localizable> GeneralMode<T> asSetAllTo(T applyOnAll) {
        return new GeneralMode<>(false, null, applyOnAll);
    }
    
    public static <T extends Localizable> GeneralMode<T> manuallySpecifyingEach(String nameInSpecifyEach) {
        return new GeneralMode<>(true, nameInSpecifyEach,  null);
    }
    
    public boolean isSpecifyEach() {
        return specifyEach;
    }
    
    public @Nullable T commonValueToSet() {
        return this.applyOnAll;
    }
    
    @Override public @NotNull String toString() {
        if (this.specifyEach) {
            if(this.alternateNameInSpecifyEach!=null){
                return this.alternateNameInSpecifyEach;
            }
            return "Specify each";
        }
        return "Set all to: " + applyOnAll.localize();
    }
    
}
