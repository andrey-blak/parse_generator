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
    public String name;

    @XmlElement(name = "Popup")
    public DtoPopup popup;

    @XmlElement
    DtoMenuItem menuItem;

    public static class DtoPopup {
        //@XmlElement(name = "menuitem")
        public List<DtoMenuItem> menuitems;

        @XmlElement
        DtoMenuItem menuItem;

        @XmlElement
        void setMenuMenu(DtoMenuItem menuItem) {};
    }

    public static class DtoMenuItem {
        @XmlElement(name = "onClick")
        public String onclick;

        @XmlElement
        public Float fraction;

        @XmlElement
        public byte _byte;

        private String value;

        @XmlElement(name = "value")
        public void setValue(String value) {
            this.value = value;
        }

        @XmlElement
        void setByte(Byte value) {}

        public String getValue() {
            return value;
        }
    }
}
