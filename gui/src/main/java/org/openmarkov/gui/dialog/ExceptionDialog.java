package org.openmarkov.gui.dialog;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.IOpenMarkovException;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Allows to display exception details in a dialog box with a method called {@link ExceptionDialog#show(Throwable)}.
 *
 * @author jrico
 */
public class ExceptionDialog {
    
    /**
     * Gets the title and message of an exception and then shows them with a dialog.
     *
     * @param throwable the exception to show.
     */
    public static void show(Throwable throwable) {
        ExceptionDialog.show(null, throwable, null);
    }
    
    /**
     * Gets the title and message of an exception and then shows them with a dialog.
     *
     * @param throwable the exception to show.
     */
    public static void show(Throwable throwable, @Nullable Component frame) {
        ExceptionDialog.show(null, throwable, frame);
    }
    
    /**
     * Gets the title and message of an exception and then shows them with a dialog.
     *
     * @param throwable the exception to show.
     */
    public static void show(@Nullable String messagePrexif, Throwable throwable) {
        ExceptionDialog.show(messagePrexif, throwable, null);
    }
    
    /**
     * Gets the title and message of an exception and then shows them with a dialog.
     *
     * @param throwable the exception to show.
     * @param frame     the parent component from which the dialog should pop.
     */
    public static void show(@Nullable String messagePrexif, Throwable throwable, @Nullable Component frame) {
        TitleAndMessage titleAndMessage = getTitleAndMessage(throwable);
        String title = titleAndMessage.title;
        String message = titleAndMessage.message;
        if (messagePrexif != null) {
            if (!messagePrexif.endsWith(" ")) {
                messagePrexif += " ";
            }
            message = messagePrexif + message;
        }
        if (title != null) {
            title = title.replace("\\n", "\n");
        }
        if (message != null) {
            message = message.replace("\\n", "\n");
        }
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Gets a title and a message for an {@link Exception}. This is different for different exception classes:
     * <ul>
     *   <li>If the class extends {@link IOpenMarkovException}, the title and message will come from
     *   {@code IOpenMarkovException#getExceptionTitle()} and {@code IOpenMarkovException#getExceptionMessage()}.</li>
     *   <li>If the exception is in a bundle file, it will take the title and message from the
     *   {@link StringDatabase#getUniqueInstance()}</li>
     *   <li>In any other case, the title is the localization of the key {@code "UnlocalizedJavaException.title"} and the
     *   message is the localization of the key {@code "UnlocalizedJavaException.value"} followed by the
     *   {@link Exception}'s stacktrace.
     *   </li>
     * </ul>
     *
     * @param throwable the exception to extract a title and message to represent it.
     * @return a title and a message representing the exception.
     */
    private static TitleAndMessage getTitleAndMessage(Throwable throwable) {
        if (throwable instanceof IOpenMarkovException openMarkovException) {
            /*
            try {
                var getExceptionTitle = IOpenMarkovException.class.getDeclaredMethod("getExceptionTitle");
                getExceptionTitle.setAccessible(true);
                String title = (String) getExceptionTitle.invoke(openMarkovException);
                var getExceptionMessage = IOpenMarkovException.class.getDeclaredMethod("getExceptionMessage");
                getExceptionMessage.setAccessible(true);
                String message = (String) getExceptionMessage.invoke(openMarkovException);
                return new TitleAndMessage(title, message);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                throw new UnreachableException(ex);
            }
             */
            return new TitleAndMessage(openMarkovException.getExceptionTitle(), openMarkovException.getExceptionMessage());
        }
        @Nullable Class<Exception> exceptionClass = (Class<Exception>) throwable.getClass();
        while (exceptionClass != null) {
            String exceptionClassName = exceptionClass.getName();
            if (exceptionClassName.contains(".")) {
                exceptionClassName = exceptionClassName.substring(exceptionClassName.lastIndexOf('.') + 1);
            }
            String title = StringDatabase.getUniqueInstance()
                                         .getNullableString(exceptionClassName + ".title");
            String message = StringDatabase.getUniqueInstance()
                                           .getNullableString(exceptionClassName + ".message");
            if (title != null || message != null) {
                return new TitleAndMessage(title, message);
            }
            try {
                exceptionClass = (Class<Exception>) exceptionClass.getSuperclass();
                if (exceptionClass.isAssignableFrom(Exception.class) && exceptionClass != Exception.class) {
                    exceptionClass = null;
                }
            } catch (ClassCastException ignored) {
                exceptionClass = null;
            }
        }
        String title = StringDatabase.getUniqueInstance().getString("UnlocalizedJavaException.title");
        String stackTrace = Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString)
                                  .filter(s -> !s.isBlank())
                                  .map(s -> "\tat " + s)
                                  .collect(Collectors.joining("\n"));
        String message = StringDatabase.getUniqueInstance()
                                       .getString("UnlocalizedJavaException.message") + "\n" + throwable + "\n" + stackTrace;
        return new TitleAndMessage(title, message);
    }
    
    /**
     * Represents a title and a message of an {@link IOpenMarkovException}.
     */
    private record TitleAndMessage(@Nullable String title, @Nullable String message) {
    }
    
}