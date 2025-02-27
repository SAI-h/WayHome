package com.example.wayhome.MedianValueSerialization;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ListSerialization <T> implements Serializable {

    private List<T> _data;

}
