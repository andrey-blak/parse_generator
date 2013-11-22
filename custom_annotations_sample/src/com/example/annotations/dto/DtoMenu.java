package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DtoMenu {
    @XmlElement(name = "Id", defaultValue = "42")
    public int id;

    @XmlElement
    public Integer amount;

    @XmlElement
    public char symbol;

    @XmlElement
    public Float fraction;

    @XmlElement
    public byte veryShort;

    @XmlElement
    public String name;

    @XmlElement(name = "Popup")
    public DtoPopup popup;

    @XmlElement
    DtoMenuItem menuItem;

    public static class DtoPopup {
        @XmlElement(name = "menuitem")
        public List<DtoMenuItem> menuitems;
    }

    public static class DtoMenuItem {
        @XmlElement(name = "onclick")
        public String onclick;

        private String value;

        @XmlElement(name = "value")
        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
