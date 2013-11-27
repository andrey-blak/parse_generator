package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DtoMenu {
    @XmlElement(name = "Id")
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
    public InnerOuterClass.DtoMenuItem menuItem;

    @XmlElement
    public Color color;

    public static class DtoPopup {
        //@XmlElement(name = "menuitem")
        public List<InnerOuterClass.DtoMenuItem> menuitems;

        public InnerOuterClass.DtoMenuItem menuItem;

        @XmlElement
        void setMenuMenu(InnerOuterClass.DtoMenuItem value) {
            menuItem = value;
        }
    }

    public static class InnerOuterClass {
        @XmlRootElement
        public static class DtoMenuItem {
            @XmlElement(name = "onClick")
            public String onclick;

            @XmlElement
            public Float fraction;

            public byte _byte;

            private String value;

            @XmlElement(name = "value")
            public void setValue(String value) {
                this.value = value;
            }

            @XmlElement
            public void setByte(Byte value) {
                _byte= value;
            }

            @XmlElement
            public String getValue() {
                return value;
            }
        }
    }

    public enum Color {
        RED, GREEN, BLUE,
    }
}
