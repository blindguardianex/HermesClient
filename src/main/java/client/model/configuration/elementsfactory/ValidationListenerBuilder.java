package client.model.configuration.elementsfactory;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Objects;

/**
 * Билдер, создающий слушателя поля (TextField или TextArea), проверяющий вводимые
 * символы на соответствие переданному паттерну
 *
 */
public class ValidationListenerBuilder {

    private String pattern;
    private TextField field;
    private TextArea area;

    private ValidationListenerBuilder(TextField field) {
        this.field = field;
    }

    private ValidationListenerBuilder(TextArea area) {
        this.area = area;
    }

    /**
     * Принимает поле, которое нужно прослушивать
     * @param field
     * @return
     */
    public static ValidationListenerBuilder listenerFor(TextField field){
        return new ValidationListenerBuilder(field);
    }

    /**
     * Принимает поле, которое нужно прослушивать
     * @param area
     * @return
     */
    public static ValidationListenerBuilder listenerFor(TextArea area){
        return new ValidationListenerBuilder(area);
    }

    /**
     * Принмает паттерн, на соответствие которому нужно прослушивать поле
     * @param pattern
     * @return
     */
    public ValidationListenerBuilder byPattern(String pattern){
        this.pattern = pattern;
        return this;
    }

    public ChangeListener<? super String> get(){
        Objects.requireNonNull(pattern);
        if(field!=null) {
            return createListener(pattern, field);
        }
        else {
            return createListener(pattern, area);
        }

    }

    /**
     * Создает слушателя
     * Если введеный символ соответствиет паттерну, тогда символ добавляется в поле
     * Иначе текст остается неизменным
     * @param pattern
     * @param field
     * @return
     */
    private ChangeListener<? super String> createListener(String pattern, TextField field){
        ChangeListener<String>listener = (observableValue, s, t1) -> {
            if (!t1.isEmpty()) {
                if (!t1.matches(pattern)) {
                    field.setText(s);
                } else {
                    field.setText(t1);
                }
            }
        };
        return listener;
    }

    /**
     * Создает слушателя
     * Если введеный символ соответствиет паттерну, тогда символ добавляется в поле
     * Иначе текст остается неизменным
     * @param pattern
     * @param area
     * @return
     */
    private ChangeListener<? super String> createListener(String pattern, TextArea area){
        ChangeListener<String>listener = (observableValue, s, t1) -> {
            if (!t1.isEmpty()) {
                if (!t1.matches(pattern)) {
                    area.setText(s);
                } else {
                    area.setText(t1);
                }
            }
        };
        return listener;
    }
}
