package com.example.annotations.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DtoMenu {
    @XmlElement(name = "id", defaultValue = "42")
    public int id;

    @XmlElement
    public String name;

    @XmlElement(name = "popup")
    public DtoPopup popup;

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
    }
}
