package org.openmarkov.gui.component;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spinner that only accepts numeric values.
 *
 * @param <N> can be any from {@link Integer}, {@link Short}, {@link Byte}, {@link Long}, {@link BigInteger},
 *            {@link Double}, {@link Float} and {@link BigDecimal}.
 *
 * @author jrico
 */
public class NumericSpinner<N extends Number & Comparable<N>> extends JSpinner {
    
    public final NumericModel<N> model;
    private final NumericInfo<N> numericInfo;
    
    public NumericSpinner(Class<N> numericClass) {
        this.numericInfo = (NumericInfo<N>) NUMERIC_INFOS.get(numericClass);
        this.model = new NumericModel<>(this.numericInfo, this);
        this.setModel(this.model);
        JFormattedTextField textField = ((DefaultEditor) this.getEditor()).getTextField();
        textField.setEditable(true);
        textField.setColumns(Math.max(this.numericInfo.min.toString().length(),
                                      this.numericInfo.max.toString().length()));
        textField.setHorizontalAlignment(JTextField.RIGHT);

        while(this.getChangeListeners().length>0){
            this.removeChangeListener(this.getChangeListeners()[0]);
        }
        var listeners = this.getChangeListeners();
        System.out.println(listeners);
    }
    
    public void setMinimum(N minimum) {
        this.model.minimum = minimum;
        this.model.recheckValue();
    }
    
    public void setCurrentValue(N value) {
        this.model.value = value;
        this.model.recheckValue();
    }
    
    public void setCurrentValueNoListener(N value) {
        boolean oldUseListeners = this.model.useListeners;
        this.model.useListeners = false;
        this.setCurrentValue(value);
        this.model.useListeners = oldUseListeners;
    }
    
    public void setMaximum(N maximum) {
        this.model.maximum = maximum;
        this.model.recheckValue();
    }
    
    public void executeWithoutListeners(Runnable runnable) {
        boolean oldUseListeners = this.model.useListeners;
        this.model.useListeners = false;
        runnable.run();
        this.model.useListeners = oldUseListeners;
    }
    
    public void setStepSize(N stepSize) {
        this.model.stepSize = stepSize;
    }
    
    public NumericInfo<N> getNumericInfo() {
        return this.numericInfo;
    }
    
    public N getMinimum() {
        return this.model.minimum;
    }
    
    public N getMaximum() {
        return this.model.maximum;
    }
    
    public N getStepSize() {
        return this.model.stepSize;
    }
    
    public N getCurrentValue() {
        return this.model.value;
    }
    
    static class NumericModel<N extends Number & Comparable<N>> implements SpinnerModel {
        private final NumericInfo<N> numericInfo;
        private final NumericSpinner spinner;
        public boolean useListeners;
        
        private N minimum;
        private N value;
        private N maximum;
        private N stepSize;
        
        public NumericModel(NumericInfo<N> numericInfo, NumericSpinner spinner) {
            this.numericInfo = numericInfo;
            this.spinner = spinner;
            this.useListeners = true;
            this.value = this.numericInfo.unit;
            this.minimum = this.numericInfo.min;
            this.maximum = this.numericInfo.max;
            this.stepSize = this.numericInfo.unit;
        }
        
        @Override public Object getValue() {
            return this.value;
        }
        
        @Override public void setValue(Object value) {
            N newValue;
            try {
                String valueStr = value.toString();
                newValue = this.numericInfo.valueGetter.apply(valueStr);
            } catch (RuntimeException e) {
                return;
            }
            var oldValue = this.value;
            if (newValue.compareTo(this.minimum) == -1) {
                this.value = this.minimum;
            } else if (newValue.compareTo(this.maximum) == 1) {
                this.value = this.maximum;
            } else {
                this.value = newValue;
            }
            
            ((DefaultEditor) this.spinner.getEditor()).getTextField().setText(this.value.toString());
            boolean changed = oldValue.compareTo(this.value) != 0;
            if (this.useListeners) {
                for(ChangeListener changeListener:this.spinner.getChangeListeners()){
                    changeListener.stateChanged(new ChangeEvent(this));
                }
            }
        }
        
        @Override public Object getNextValue() {
            try {
                return this.numericInfo.sum.apply(this.value, this.stepSize);
            } catch (RuntimeException e) {
                return this.maximum;
            }
        }
        
        @Override public Object getPreviousValue() {
            try {
                return this.numericInfo.substract.apply(this.value, this.stepSize);
            } catch (RuntimeException e) {
                return this.minimum;
            }
        }
        
        @Override public void addChangeListener(ChangeListener l) {
            this.spinner.addChangeListener(l);
        }
        
        @Override public void removeChangeListener(ChangeListener l) {
            this.spinner.removeChangeListener(l);
        }
        
        public void recheckValue() {
            this.setValue(this.value);
        }
        
    }
    
    
    record NumericInfo<N extends Number>(Class<N> sourceClass, Function<String, N> valueGetter, N min, N max, N unit,
                                         BiFunction<N, N, N> sum, BiFunction<N, N, N> substract) {
    }
    
    
    private static final NumericInfo<Integer> INTEGER_NUMERIC_INFO = new NumericInfo<>(
            Integer.class, Integer::valueOf, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, (a, b) -> a + b, (a, b) -> a - b);
    
    private static final NumericInfo<Byte> BYTE_NUMERIC_INFO = new NumericInfo<>(
            Byte.class, Byte::valueOf, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 1, (a, b) -> (byte) (a + b), (a, b) -> (byte) (a - b));
    
    private static final NumericInfo<Long> LONG_NUMERIC_INFO = new NumericInfo<>(
            Long.class, Long::valueOf, Long.MIN_VALUE, Long.MAX_VALUE, (long) 1, (a, b) -> a + b, (a, b) -> a - b);
    
    private static final NumericInfo<Short> SHORT_NUMERIC_INFO = new NumericInfo<>(
            Short.class, Short::valueOf, Short.MIN_VALUE, Short.MAX_VALUE, (short) 1, (a, b) -> (short) (a + b), (a, b) -> (short) (a - b));
    
    private static final NumericInfo<BigInteger> BIG_INTEGER_NUMERIC_INFO = new NumericInfo<>(
            BigInteger.class, BigInteger::new, BigInteger.valueOf(Long.MIN_VALUE), BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ONE, BigInteger::add, BigInteger::subtract);
    
    
    private static final NumericInfo<Double> DOUBLE_NUMERIC_INFO = new NumericInfo<>(
            Double.class, Double::valueOf, Double.MIN_VALUE, Double.MAX_VALUE, 1.0, (a, b) -> a + b, (a, b) -> a - b);
    
    private static final NumericInfo<Float> FLOAT_NUMERIC_INFO = new NumericInfo<>(
            Float.class, Float::valueOf, Float.MIN_VALUE, Float.MAX_VALUE, (float) 1, (a, b) -> a + b, (a, b) -> a - b);
    
    private static final NumericInfo<BigDecimal> BIG_DECIMAL_NUMERIC_INFO = new NumericInfo<>(
            BigDecimal.class, BigDecimal::new, BigDecimal.valueOf(Double.MIN_VALUE), BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal.ONE, BigDecimal::add, BigDecimal::subtract);
    
    
    private static final Map<Class<? extends Number>, NumericInfo<? extends Number>> NUMERIC_INFOS = Stream.of(
            INTEGER_NUMERIC_INFO,
            BYTE_NUMERIC_INFO,
            LONG_NUMERIC_INFO,
            SHORT_NUMERIC_INFO,
            BIG_INTEGER_NUMERIC_INFO,
            DOUBLE_NUMERIC_INFO,
            FLOAT_NUMERIC_INFO,
            BIG_DECIMAL_NUMERIC_INFO
    ).collect(Collectors.toMap(numericInfo -> numericInfo.sourceClass, numericInfo -> numericInfo));
}
