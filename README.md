# Objective
This document describes how to add additional information as part of xs Any XSD Type. 

# Background
The XS <any> element enables authors to extend the XML document with elements not specified by the schema. This allows placement of any arbitrary XML data type including simple and complex types. In the schema named person.xsd [below] any tag can be placed after the lastname tag. For example
```xml
<lastname>Mukherjee</lastname>
</otherInfo>Some other info</otherInfo>
```
And
```xml
<lastname>Mukherjee</lastname>
</otherInfo>
	<city></city>
</otherInfo>
```
Are both valid uses of the schema type xs:any.
JAXB has specific ways to dealing with XS Any. There are 2 ways mainly.
1.	Schema Driven: Create additional XSD for any additional schema and create a class that maps onto this additional schema. This document describes this approach.
2.	DOM Driven: Create and Serialize org.w3.dom.Element inside JAXB classes and read the same as DOM element. This approach cannot be schema validated though and hence not described here.
Schema Drive Method
## Step1
Create an XSD Schema which has xs:any as part of its definition. Name it as person.xsd
```xsd
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified"
           xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xs:element name="person" type="personType"/>
    <xs:complexType name="personType">
        <xs:sequence>
            <xs:element name="firstname" type="xs:string"/>
            <xs:element name="lastname" type="xs:string"/>
            <xs:any namespace="##any" processContents="lax"/>
        </xs:sequence>
    </xs:complexType>
</xs:schema>
````
## Step2
Create another XSD schema that describes the additional XML which will form a part of xs:any. Name it person-extra.xsd
```xsd
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified"
           xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xs:element name="address" type="addressType"/>
    <xs:complexType name="addressType">
        <xs:sequence>
            <xs:element name="street" type="xs:string"/>
            <xs:element name="city" type="xs:string"/>
        </xs:sequence>
    </xs:complexType>
</xs:schema>
```
## Step 3
Generate Java Types for person.xsd
```bash
xjc –d [output directory] –p [packagename] person.xsd
```
This will generate two classes PersonType.java and ObjectFactory.java inside the named package.
Also add the annotation XMLRootElement(name=”person”) to the PersonType.java file as JAXB does not add any root element annotation if the root element is an user defined type.
## Step 4
Generate Java Types for person-extra.xsd
```bash
xjc –d [output directory] –p [packagename] person-extra.xsd
```

This will generate two classes PersonType.java and ObjectFactory.java inside the named package.
Also add the annotation XMLRootElement(name=”address”) to the AddressType.java file.
## Step5 
Write the following code to try out this mechanism
```java
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
```
The example above creates a PersonType and also adds an AddressType. It serialises it to a StringWriter and then reads it off again. To enable this ability to read and write both the Peron and Address types it must be included in the JAXB Context ```java JAXBContext context = JAXBContext.newInstance(PersonType.class, AddressType.class);```

