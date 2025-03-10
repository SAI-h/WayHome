package com.example.wayhome.graphEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Edge<T> {
    Long nextID;
    T value;
}
