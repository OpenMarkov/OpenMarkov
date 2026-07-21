package org.openmarkov.java.langUtils;

import java.util.function.Consumer;
import java.util.function.Function;

public class SwitchUtils {
    
    public static <Input, Output> Output switchYieldInstance(Input input, Output defaultOutput, Case.FunctionBranch<? extends Input, Output>... branches) {
        for (var branch : branches) {
            if (input == branch.value) {
                return branch.apply();
            }
        }
        return defaultOutput;
    }
    
    public static <Input> void switchInstance(Input input, Case.ConsumerBranch<? extends Input>... branches) {
        for (var branch : branches) {
            if (input == branch.value) {
                branch.accept();
                return;
            }
        }
    }
    
    
    public sealed interface Case<T, Output> {
        
        public static final class FunctionBranch<T, Output> implements Case<T, Output> {
            private final T value;
            private final Function<T, Output> function;
            
            private FunctionBranch(T value, Function<T, Output> function) {
                this.value = value;
                this.function = function;
            }
            
            private Output apply() {
                return this.function.apply(this.value);
            }
        }
        
        public static final class ConsumerBranch<T> implements Case<T, Void> {
            private final T value;
            private final Consumer<T> consumer;
            
            public ConsumerBranch(T value, Consumer<T> consumer) {
                this.value = value;
                this.consumer = consumer;
            }
            
            private void accept() {
                this.consumer.accept(this.value);
            }
        }
        
        static <T, Output> FunctionBranch<T, Output> of(T value, Function<T, Output> function) {
            return new FunctionBranch<>(value, function);
        }
        
        static <T> ConsumerBranch<T> of(T value, Consumer<T> consumer) {
            return new ConsumerBranch<>(value, consumer);
        }
        
    }
    
    
}
