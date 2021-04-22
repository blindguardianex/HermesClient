package client.model.entity;

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Role {

    @Expose
    private int id;
    @Expose
    private String name;
}
