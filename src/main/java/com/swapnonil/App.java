package com.swapnonil;

import extra.AddressType;
import xsd.PersonType;

import javax.xml.bind.*;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Example of making xs:any work.
 */
public class App {
    private static String read;

    private static String xmlString;

    public static void main(String[] args) {
        writeDocument();
        readDocument();
    }

    private static void writeDocument() {
        try {
            JAXBContext context = JAXBContext.newInstance(PersonType.class, AddressType.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            StringWriter writer = new StringWriter();
            m.marshal(getPersonType(), writer);
            xmlString = writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static void readDocument() {

        try {
            JAXBContext context = JAXBContext.newInstance(PersonType.class, AddressType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xmlString);
            JAXBElement<PersonType> personType = (JAXBElement<PersonType>) unmarshaller.unmarshal(reader);
            PersonType value = personType.getValue();
            AddressType any = (AddressType) value.getAny();
            System.out.println(any.getStreet());
            System.out.println(any.getCity());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static PersonType getPersonType() {
        PersonType root = new PersonType();
        root.setFirstname("Swapnonil");
        root.setLastname("Mukherjee");
        root.setAny(getAddressType());
        return root;
    }

    private static AddressType getAddressType() {
        AddressType addressType = new AddressType();
        addressType.setStreet("Shirley Road");
        addressType.setCity("London");
        return addressType;
    }
}
