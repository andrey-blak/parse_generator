package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SameNames {
    @XmlElement
    ClassA name;

    @XmlElement
    void setName(String value) {
    }

    @XmlElement
    String getName() {
        return null;
    }

    @XmlElement
    void name(int value) {
    }

    @XmlElement(name = "name")
    char symb;

    static class ClassA {
        @XmlElement
        public ClassB name;
    }

    static class ClassB {
        @XmlElement
        String nameString;
    }
}
