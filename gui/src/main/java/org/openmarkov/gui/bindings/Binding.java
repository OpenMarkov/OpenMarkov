package org.openmarkov.gui.bindings;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.configuration.UserPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Binding implements BindingCommandGroup.BindingGroup {
    
    public final @Nullable String locator;
    public final @NotNull String name;
    public final @Nullable String description;
    
    public final @NotNull String tab;
    public final @NotNull String sectionInTab;
    
    
    public final @NotNull InputCombination defaultCombination;
    public final @NotNull Binding.KeyAcceptance keyAcceptance;
    public final @NotNull Binding.MouseAcceptance mouseAcceptance;
    private final Set<InvalidReason> invalidReasons;
    public final Exclusivity exclusivity;
    
    public Binding(@Nullable String locator,
                   @NotNull String name,
                   @Nullable String description,
                   @NotNull String tab,
                   @Nullable String sectionInTab,
                   @NotNull KeyAcceptance keyAcceptance,
                   @NotNull MouseAcceptance mouseAcceptance,
                   @NotNull InputCombination defaultCombination,
                   Exclusivity exclusivity
    ) {
        this.locator = locator;
        this.name = name;
        this.description = description;
        this.tab = tab;
        this.sectionInTab = Optional.ofNullable(sectionInTab).orElse("");
        this.defaultCombination = defaultCombination;
        this.keyAcceptance = keyAcceptance;
        this.mouseAcceptance = mouseAcceptance;
        this.invalidReasons = new HashSet<>();
        this.exclusivity = exclusivity;
    }
    
    public boolean isMet(MeetCondition metCondition, boolean directionIsForward) {
        if (!this.invalidReasons.isEmpty() || this.inputCombination().inputs().findFirst().isEmpty()) {
            return false;
        }
        return this.inputCombination().isMet(metCondition, this.exclusivity, directionIsForward);
    }
    
    public @NotNull InputCombination inputCombination() {
        if (this.locator == null) {
            return defaultCombination;
        }
        
        HashMap<String, InputCombination> combinations = UserPreferences.COMBINATIONS.get();
        InputCombination userPreference = combinations.get(this.locator);
        if (userPreference != null) {
            return userPreference;
        }
        return this.defaultCombination;
    }
    
    public boolean isModifiable() {
        return this.locator != null;
    }
    
    public boolean isSetByUser() {
        return this.locator != null && UserPreferences.COMBINATIONS.get().containsKey(this.locator);
    }
    
    public void setInputCombination(InputCombination inputCombination) {
        if (this.locator == null) {
            return;
        }
        UserPreferences.COMBINATIONS.use(combinations -> {
            combinations.put(this.locator, inputCombination);
        });
    }
    
    public void resetInputCombination(InputCombination inputCombination) {
        if (this.locator == null) {
            return;
        }
        UserPreferences.COMBINATIONS.use(combinations -> {
            combinations.remove(this.locator);
        });
    }
    
    public enum MeetCondition {
        ON_GAIN,
        ON_LOSE,
        HELD,
        NOT_PRESENT,
    }
    
    public enum Exclusivity {
        EXCLUSIVE,
        INCLUSIVE
    }
    
    public boolean isInvalid() {
        return !this.invalidReasons.isEmpty();
    }
    
    public Optional<String> invalidReasonsAsString() {
        if (this.invalidReasons.isEmpty()) {
            return Optional.empty();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html><p>This combination is not valid for the following reasons:</p><ul>");
        if (this.invalidReasons.stream().anyMatch(InvalidReason.DoesNotMeetKeyAcceptance.class::isInstance)) {
            sb.append("<li>").append(this.keyAcceptance.toString()).append("</li>");
        }
        if (this.invalidReasons.stream().anyMatch(InvalidReason.DoesNotMeetMouseAcceptance.class::isInstance)) {
            sb.append("<li>").append(this.mouseAcceptance.toString()).append("</li>");
        }
        var clashes = this.invalidReasons.stream()
                                         .filter(InvalidReason.ClashesWithBinding.class::isInstance)
                                         .map(InvalidReason.ClashesWithBinding.class::cast)
                                         .toList();
        if (!clashes.isEmpty()) {
            sb.append("<li>It clashes with the following bindings:<ul>");
            for (var clash : clashes) {
                sb.append("<li>")
                  .append(clash.clashingBinding.tab)
                  .append(" - ")
                  .append(clash.clashingBinding.name)
                  .append("</li>");
            }
            sb.append("</ul></li>");
        }
        sb.append("</ul></html>");
        return Optional.of(sb.toString());
    }
    
    public static void checkValidity(@Nullable Binding binding) {
        var sourcesToCheck = switch (binding) {
            case Binding source -> List.of(source);
            case null -> Bindings.ALL_BINDINGS;
        };
        sourcesToCheck.forEach(bind -> bind.invalidReasons.clear());
        for (Binding source : sourcesToCheck) {
            source.checkValidity();
        }
    }
    
    private void checkValidity() {
        if (this.inputCombination().inputs().findFirst().isEmpty()) {
            return;
        }
        this.checkKeyAcceptance();
        this.checkMouseAcceptance();
        this.checkIncompatibilityWithOthers();
    }
    
    private void checkKeyAcceptance() {
        switch (this.keyAcceptance) {
            case ALLOWED -> {
            }
            case REQUIRES_AT_LEAST_ONE -> {
                boolean thereIsAKeyAsInput = this.inputCombination()
                                                 .inputs()
                                                 .anyMatch(input -> input instanceof Input.Key || input instanceof Input.MouseWheel);
                if (!thereIsAKeyAsInput) {
                    this.invalidReasons.add(new InvalidReason.DoesNotMeetKeyAcceptance());
                }
            }
            case FORBIDDEN -> {
                boolean thereIsAKeyAsInput = this.inputCombination()
                                                 .inputs()
                                                 .anyMatch(input -> input instanceof Input.Key || input instanceof Input.MouseWheel);
                if (thereIsAKeyAsInput) {
                    this.invalidReasons.add(new InvalidReason.DoesNotMeetKeyAcceptance());
                }
            }
        }
    }
    
    private void checkMouseAcceptance() {
        switch (this.mouseAcceptance) {
            case ALLOWED, ALLOWED_WITH_JUST_ONE_MOUSE, ALLOWED_WITH_SINGLE_CLICK -> {
            }
            case REQUIRED -> {
                boolean thereIsAMouseAsInput = this.inputCombination()
                                                   .inputs()
                                                   .anyMatch(Input.Click.class::isInstance);
                if (!thereIsAMouseAsInput) {
                    this.invalidReasons.add(new InvalidReason.DoesNotMeetMouseAcceptance());
                }
            }
            case REQUIRED_WITH_JUST_ONE_MOUSE -> {
                long mouses = this.inputCombination().inputs().filter(Input.Click.class::isInstance).count();
                if (mouses != 1) {
                    this.invalidReasons.add(new InvalidReason.DoesNotMeetMouseAcceptance());
                }
            }
            case REQUIRED_WITH_SINGLE_CLICK -> {
                long mouses = this.inputCombination().inputs().filter(Input.Click.class::isInstance).count();
                long clicks = this.inputCombination()
                                  .inputs()
                                  .filter(Input.Click.class::isInstance)
                                  .map(Input.Click.class::cast)
                                  .mapToInt(Input.Click::times)
                                  .findFirst()
                                  .orElse(0);
                if (mouses != 1 || clicks != 1) {
                    this.invalidReasons.add(new InvalidReason.DoesNotMeetMouseAcceptance());
                }
            }
            case FORBIDDEN -> {
                boolean thereIsAMouseAsInput = this.inputCombination()
                                                   .inputs()
                                                   .anyMatch(Input.Click.class::isInstance);
                if (thereIsAMouseAsInput) {
                    this.invalidReasons.add(new InvalidReason.DoesNotMeetMouseAcceptance());
                }
            }
        }
    }
    
    private void checkIncompatibilityWithOthers() {
        boolean hasInputs = this.inputCombination().inputs().findAny().isPresent();
        for (var incompatibilityGroup : Bindings.INCOMPATIBLE_GROUPS) {
            if (!incompatibilityGroup.contains(this)) {
                continue;
            }
            for (var possiblyIncompatibleBinding : incompatibilityGroup) {
                if (this == possiblyIncompatibleBinding) {
                    break;
                }
                if (this.inputCombination().equals(possiblyIncompatibleBinding.inputCombination()) && hasInputs) {
                    this.invalidReasons.add(new InvalidReason.ClashesWithBinding(possiblyIncompatibleBinding));
                    possiblyIncompatibleBinding.invalidReasons.add(new InvalidReason.ClashesWithBinding(this));
                }
            }
        }
    }
    
    public enum KeyAcceptance {
        ALLOWED,
        REQUIRES_AT_LEAST_ONE,
        FORBIDDEN;
        
        @Override public String toString() {
            return switch (this) {
                case ALLOWED -> "Can use keys from the keyboard or mouse wheel";
                case REQUIRES_AT_LEAST_ONE -> "Requires at least one key from the keyboard or to use the mouse wheel";
                case FORBIDDEN -> "Cannot use keys nor the mouse wheel";
            };
        }
    }
    
    public enum MouseAcceptance {
        REQUIRED,
        REQUIRED_WITH_JUST_ONE_MOUSE,
        REQUIRED_WITH_SINGLE_CLICK,
        ALLOWED,
        ALLOWED_WITH_JUST_ONE_MOUSE,
        ALLOWED_WITH_SINGLE_CLICK,
        FORBIDDEN;
        
        @Override public String toString() {
            return switch (this) {
                case REQUIRED -> "Requires the use of mouse";
                case REQUIRED_WITH_JUST_ONE_MOUSE -> "Requires the use of exactly one mouse button";
                case REQUIRED_WITH_SINGLE_CLICK ->
                        "Requires the use of exactly one mouse button, but cannot use more than 1 click";
                case ALLOWED -> "Can use mouse";
                case ALLOWED_WITH_JUST_ONE_MOUSE -> "Can use mouse, but only one mouse button";
                case ALLOWED_WITH_SINGLE_CLICK -> "Can use mouse, but only one mouse button and with just 1 click";
                case FORBIDDEN -> "Cannot use mouse";
            };
        }
    }
    
    public sealed interface InvalidReason {
        record ClashesWithBinding(Binding clashingBinding) implements InvalidReason {
        }
        
        record DoesNotMeetKeyAcceptance() implements InvalidReason {
        }
        
        record DoesNotMeetMouseAcceptance() implements InvalidReason {
        }
    }
    
    /// Tries to add or delete the input.
    ///
    /// It will return the input if it could be added, and it might be different from the original input, for example,
    /// if the input is Left Click 3 times, but it has a restriction of only 1 click.
    public static @Nullable Input tryUpdateBinding(@NotNull Binding binding, Input input, boolean selected, Consumer<? super Input> onInputIsRemoved) {
        if (selected) {
            if (binding.keyAcceptance == KeyAcceptance.FORBIDDEN && (input instanceof Input.Key || input instanceof Input.MouseWheel)) {
                return null;
            }
            if (input instanceof Input.Click click) {
                if (binding.mouseAcceptance == MouseAcceptance.FORBIDDEN) {
                    return null;
                }
                switch (binding.mouseAcceptance) {
                    case ALLOWED, REQUIRED -> {
                    }
                    case ALLOWED_WITH_JUST_ONE_MOUSE, REQUIRED_WITH_JUST_ONE_MOUSE -> {
                        binding.inputCombination()
                               .inputs()
                               .filter(Input.Click.class::isInstance)
                               .map(Input.Click.class::cast)
                               .filter(oClick -> oClick.mouseButton() != click.mouseButton())
                               .toList()
                               .forEach(oClick -> {
                                   var newInput = new java.util.ArrayList<>(binding.inputCombination()
                                                                                   .inputs()
                                                                                   .toList());
                                   newInput.remove(oClick);
                                   binding.setInputCombination(new InputCombination(newInput));
                                   onInputIsRemoved.accept(oClick);
                               });
                    }
                    case ALLOWED_WITH_SINGLE_CLICK, REQUIRED_WITH_SINGLE_CLICK -> {
                        binding.inputCombination()
                               .inputs()
                               .filter(Input.Click.class::isInstance)
                               .map(Input.Click.class::cast)
                               .filter(oClick -> oClick.mouseButton() != click.mouseButton())
                               .toList()
                               .forEach(oClick -> {
                                   var newInput = new java.util.ArrayList<>(binding.inputCombination()
                                                                                   .inputs()
                                                                                   .toList());
                                   newInput.remove(oClick);
                                   binding.setInputCombination(new InputCombination(newInput));
                                   onInputIsRemoved.accept(oClick);
                               });
                        input = new Input.Click(click.mouseButton(), 1);
                    }
                }
            }
        }
        var newCombination = new HashSet<Input>(binding.inputCombination()
                                                       .inputs()
                                                       .collect(Collectors.toSet()));
        Input finalInput = input;
        newCombination.removeIf(oldBinding -> switch (Tuples.record(finalInput, oldBinding)) {
            case Tuple2Record(Input.Click click, Input.Click otherClick) ->
                    click.mouseButton() == otherClick.mouseButton();
            case Tuple2Record(Input.Key key, Input.Key otherKey) -> key.key() == otherKey.key();
            case Tuple2Record(Input.MouseWheel _, Input.MouseWheel _) -> true;
            default -> false;
        });
        if (selected) {
            newCombination.add(input);
        }
        binding.setInputCombination(new InputCombination(newCombination));
        return input;
    }
    
    
}
