package client.model.entity;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс, описывающий структурное подразделение
 */
@Data
@AllArgsConstructor
public class Department {
    @Expose
    private int id;
    @Expose
    private String name;
}
