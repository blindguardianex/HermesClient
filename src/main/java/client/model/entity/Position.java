package client.model.entity;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс, описывающий должность пользователя
 */
@Data
@AllArgsConstructor
public class Position {
    @Expose
    private int id;
    @Expose
    private String name;
}
