package org.openmarkov.gui.configuration;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.gui.configuration.gson.GsonCommon;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * A simplified mechanism to access the contents of a {@link Preferences} node's value, alleviating you from manually
 * serializing and deserializing values to that node, verifying the node, or checking whether it is already written.
 * <p>
 * <strong>Example of use</strong>
 * <p>
 * Consider you have a node such as org/openmarkov/favourite_color to represent an accent {@link java.awt.Color} the
 * user can change, for this, you need to provide:
 * <ul>
 *     <li>The node and the key, in this case: {@code org/openmarkov} and {@code favourite_color}.</li>
 *     <li>The class of the value (Optional), in this case: {@link java.awt.Color}.class, this is used to verify
 *     the contents of the node are correct, in this case, if it was a {@link List} of Integers instead of a
 *     {@link java.awt.Color}, it would be incorrect.</li>
 *     <li>A default value when the user hasn't specified it's favourite color, in this case, this default color is
 *     Green.</li>
 * </ul>
 *
 * <pre>
 * {@code
 * private static final Preferences OPENMARKOV_PREFERENCES = Preferences.userRoot().node("org").node("openmarkov");
 * private static final UserPreference<Color> FAVOURITE_COLOR = new UserPreference<>
 *     (OPENMARKOV_PREFERENCES, "favourite_color", Color.class, () -> new Color(0, 255, 0));
 * }
 * </pre>
 * <br><br>
 * With this {@code FAVOURITE_COLOR} you can use the following functions to help you using the node's value:
 *
 * <ul>
 *     <li>{@link UserPreference#get()}: This will give you the {@link java.awt.Color} calculating it from the node's
 *     value, or giving you the default value if not present.</li>
 *     <li>{@link UserPreference#set(T)}: You set the new {@link java.awt.Color} of the node's value, which will also
 *     be persistently overwritten in the node.</li>
 *     <li>{@link UserPreference#save()}: Persistently saves the current {@link java.awt.Color} to the node.</li>
 *     <li>{@link UserPreference#clear()}: Removes the current {@link java.awt.Color}, and persistently removes the
 *     Preference node.</li>
 * </ul>
 *
 * @param <T> The type of the Value.
 *
 * @author jrico
 */
public final class UserPreference<T> {
    
    /**
     * When set to true, the {@link UserPreference#save()} operation takes no effect, and
     * {@link UserPreference#initialize()} no longer gets the value from the {@link UserPreference#RESOLVE_STRATEGY}.
     */
    public static boolean IGNORE_STORAGE = false;
    
    /**
     * The default resolve strategy is that which can put and clear a value two times.
     */
    private static final UserPreferenceResolveStrategy RESOLVE_STRATEGY = Arrays
            .stream(UserPreferenceResolveStrategy.values())
            .filter(strategy ->
                            strategy.put(List.of("test.test"), "test")
                                    && strategy.clear(List.of("test.test"))
                                    && strategy.put(List.of("test.test"), "test")
                                    && strategy.clear(List.of("test.test"))
            )
            .findFirst()
            .orElse(UserPreferenceResolveStrategy.SESSION);
    
    private final @NotNull List<String> preferencePath;
    private final @NotNull Supplier<? extends T> defaultValue;
    private final @NotNull Predicate<Object> verifyIsInstance;
    private final @NotNull Class<T> tClass;
    private final @NotNull Function<String, T> deserializeWith;
    private final @NotNull Function<T, String> serializeWith;
    private final @Nullable BackupInfo backupInfo;
    
    private @Nullable T value;
    private boolean isInitialized;
    private boolean modified;
    
    
    static <T extends Serializable> UserPreference<T> of(@NotNull String preferencePath, @NotNull Supplier<? extends T> defaultValue, BackupInfo backupInfo, @Nullable TypeToken<T> typeToken) {
        return new UserPreference<>(preferencePath, defaultValue, null, null,
                                    (string) -> GsonCommon.GSON.fromJson(string, typeToken),
                                    (value) -> GsonCommon.GSON.toJson(value, typeToken.getType()), backupInfo);
    }
    
    
    UserPreference(@NotNull String preferencePath, @NotNull Supplier<? extends T> defaultValue, @Nullable Class<T> tClass, @Nullable Predicate<Object> verifyIsInstance, Function<String, T> deserializeWith, Function<T, String> serializeWith, @Nullable BackupInfo backupInfo) {
        this.backupInfo = backupInfo;
        deserializeWith = deserializeWith != null ? deserializeWith : JavaSerializationUtils::deserialize;
        serializeWith = serializeWith != null ? serializeWith : JavaSerializationUtils::serialize;
        verifyIsInstance = verifyIsInstance != null ? verifyIsInstance : o -> true;
        tClass = tClass != null ? tClass : (Class<T>) Object.class;
        
        this.preferencePath = List.of(preferencePath.split("/"));
        this.defaultValue = defaultValue;
        this.tClass = tClass;
        this.verifyIsInstance = verifyIsInstance;
        this.deserializeWith = deserializeWith;
        this.serializeWith = serializeWith;
    }
    
    
    /**
     * Gets the value corresponding to the node.
     * <p>
     * If the node is empty (because it was cleared or wasn't ever written) or its value is not of type {@code T}, it
     * will return the calculated result of {@link UserPreference#defaultValue}.
     * <p>
     * This operation is cached, meaning if the value was calculated in a previous call to
     * {@code get()}, it won't re-calculate it again, so the value is only loaded when needed.
     *
     * @return the value corresponding to the node.
     */
    public T get() {
        this.initialize();
        return this.value;
    }
    
    public void initialize() {
        if (this.isInitialized) return;
        if (UserPreference.IGNORE_STORAGE) {
            this.isInitialized = true;
            this.value = this.defaultValue.get();
            return;
        }
        String nodeValue = UserPreference.RESOLVE_STRATEGY.get(this.preferencePath);
        if (nodeValue == null) {
            this.isInitialized = true;
            this.value = this.defaultValue.get();
            return;
        }
        T value;
        try {
            value = this.deserializeWith.apply(nodeValue);
            boolean isInvalid = !this.verifyIsInstance.test(value) || !this.tClass.isInstance(value);
            if (isInvalid) {
                value = this.defaultValue.get();
            }
        } catch (RuntimeException e) {
            value = this.defaultValue.get();
        }
        this.value = value;
        this.isInitialized = true;
    }
    
    /**
     * Gets whether the node contains a value or not.
     * <p>
     * This does not trigger initialization of the value.
     *
     * @return whether the node contains a value or not.
     */
    public boolean isSet() {
        return this.modified || UserPreference.RESOLVE_STRATEGY.isSet(this.preferencePath);
    }
    
    /**
     * Manually sets the {@link UserPreference#value} of this node, which also implies calling to
     * {@link UserPreference#save()} in order to save your changes persistently.
     *
     * @param newValue the new value, it must not be null.
     */
    public void set(@NotNull T newValue) {
        this.value = newValue;
        this.isInitialized = true;
        this.modified = true;
        this.save();
    }
    
    /**
     * Persistently saves the value into the node.
     * <p>
     * If the value hasn't been loaded by a previous call to {@link UserPreference#get()}, the changes won't be saved.
     */
    @ToCheck(reasonKind = ToCheck.ReasonKind.USER_EXPERIENCE,
            reasonDescription = "Many of the exceptions of this class are ignored, as otherwise, the user would be" +
                    "bombarded with exceptions happening if their OS doesn't allow to use Backing Stores. But, do we " +
                    "want them to be logged nevertheless")
    public void save() {
        if (UserPreference.IGNORE_STORAGE || !this.isInitialized) {
            return;
        }
        try {
            UserPreference.RESOLVE_STRATEGY.put(this.preferencePath, getSerialized());
        } catch (RuntimeException ignored) {
            ignored.printStackTrace();
        }
    }
    
    public String getSerialized() {
        return this.serializeWith.apply(this.value);
    }
    
    public void setSerialized(String serialized) {
        T deserializedValue = this.deserializeWith.apply(serialized);
        boolean isInvalid = !this.verifyIsInstance.test(deserializedValue) || !this.tClass.isInstance(deserializedValue);
        if (!isInvalid) {
            this.set(deserializedValue);
        }
    }
    
    /**
     * Removes the value and also removes it persistently from the {@link Preferences}' node.
     * <p>
     * This implies you won't find this node in your {@link Preferences}, and the next time you call to
     * {@link UserPreference#get()}, you will get the {@link UserPreference#defaultValue}.
     */
    public void clear() {
        UserPreference.RESOLVE_STRATEGY.clear(this.preferencePath);
        this.value = null;
        this.isInitialized = false;
        this.modified = false;
    }
    
    /**
     * This is a shortcut method intended to be used for simple operations where you need to get the value, modify it,
     * and save it, all of it consecutively.
     *
     * @param onValue action to trigger over the value.
     */
    public void use(@NotNull Consumer<? super @NotNull T> onValue) {
        onValue.accept(this.get());
        this.save();
    }
    
    public @NotNull List<String> getPreferencePath() {
        return this.preferencePath;
    }
    
    /**
     * This is a shortcut method intended to be used for simple operations where you need to get the value, modify it,
     * and save it, all of it consecutively.
     *
     * @param onValue action to trigger over the value.
     */
    public <V> V use(@NotNull Function<? super @NotNull T, V> onValue) {
        V returnValue = onValue.apply(this.get());
        this.save();
        return returnValue;
    }
    
    public boolean isBackupable() {
        return this.backupInfo != null;
    }
    
    public @Nullable BackupInfo backupInfo() {
        return this.backupInfo;
    }
    
    public record BackupInfo(boolean backupByDefault, @NotNull String preferenceTitle,
                             @NotNull String preferenceDescription, @NotNull Runnable onBackup) {
        
    }
    
}
