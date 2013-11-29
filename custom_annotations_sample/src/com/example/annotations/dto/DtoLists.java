package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement
public class DtoLists {
    @XmlElement
    public ArrayList<Boolean> booleanList;

    @XmlElement
    public LinkedList<Integer> integerList;

    @XmlElement
    public List<Long> longList;

    @XmlElement
    public List<Short> shortList;

    @XmlElement
    public List<Double> doubleList;

    @XmlElement
    public List<Float> floatList;

    @XmlElement
    public List<Character> characterList;

    @XmlElement
    public List<String> stringList;
}
