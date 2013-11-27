package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Primitives {
    @XmlElement(defaultValue = "true")
    public boolean _boolean;

    @XmlElement(defaultValue = "true")
    public Boolean _Boolean;

    @XmlElement(defaultValue = "42")
    public int _int;

    @XmlElement(defaultValue = "42")
    public Integer _Integer;

    @XmlElement(defaultValue = "1234567890123456789l")
    public long _long;

    @XmlElement(defaultValue = "1234567890123456789l")
    public Long _Long;

    @XmlElement(defaultValue = "12345")
    public short _short;

    @XmlElement(defaultValue = "12345")
    public Short _Short;

    @XmlElement(defaultValue = "123.456789")
    public double _double;

    @XmlElement(defaultValue = "123.456789")
    public Double _Double;

    @XmlElement(defaultValue = "123.4567")
    public float _float;

    @XmlElement(defaultValue = "123.4567")
    public Float _Float;

    @XmlElement(defaultValue = "S")
    public char _char;

    @XmlElement(defaultValue = "S")
    public Character _Character;

    @XmlElement(defaultValue = "Trinity")
    public String _String;
}
