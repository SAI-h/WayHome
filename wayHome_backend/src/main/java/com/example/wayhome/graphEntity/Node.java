package com.example.wayhome.graphEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Node {
    long id;
    double g;
    double f;
}