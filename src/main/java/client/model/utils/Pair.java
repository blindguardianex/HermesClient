package client.model.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Pair<U, T> {

    @Getter
    private final U key;
    @Getter
    private final T value;
}
