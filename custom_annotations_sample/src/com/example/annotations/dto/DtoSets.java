package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
public class DtoSets {
    @XmlElement
    public Set<Boolean> booleanSet;

    @XmlElement
    public HashSet<Integer> integerSet;

    @XmlElement
    public Set<Long> longSet;

    @XmlElement
    public Set<Short> shortSet;

    @XmlElement
    public Set<Double> doubleSet;

    @XmlElement
    public Set<Float> floatSet;

    @XmlElement
    public Set<Character> characterSet;

    @XmlElement
    public Set<String> stringSet;
}
