package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DtoArrays {
    @XmlElement
    public Boolean[] booleanArray;

    @XmlElement
    public Integer[] integerArray;

    @XmlElement
    public long[] longArray;

    @XmlElement
    public Short[] shortArray;

    @XmlElement
    public double[] doubleArray;

    @XmlElement
    public Float[] floatArray;

    @XmlElement
    public Character[] characterArray;

    @XmlElement
    public String[] stringArray;
}
