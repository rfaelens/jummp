package net.biomodels.jummp.core.util

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

public class JummpXmlUtils {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    public static String findModelAttribute(final File model, String elementName, String attributeName) {
        if (!model || !model.canRead()) {
            def errMsg = new StringBuilder("Cannot find ").append(elementName).append(".").append(attributeName)
            errMsg.append(" in file ").append(model.properties)
            log.error errMsg.toString()
            return ""
        }
        if (null == elementName) {
            elementName = ""
        }
        if (null == attributeName) {
            attributeName = ""
        }
        if (IS_INFO_ENABLED) {
            def info = new StringBuilder("Extracting attribute ").append(attributeName).
                        append(" of element ").append(elementName).append(" from ").append(model.properties)
            log.info(info.toString())
        }
        String theResult
        def fileReader = new FileReader(model)
        XMLInputFactory factory = XMLInputFactory.newInstance()
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE)
        XMLStreamReader xmlReader
        try {
            xmlReader = factory.createXMLStreamReader(fileReader)
            boolean found = false
            while (xmlReader.hasNext() && !found) {
                xmlReader.next()
                if (xmlReader.startElement) {
                    String result = processXmlElement(xmlReader, elementName, attributeName)
                    if (result) {
                        theResult = result
                        found = true
                    } else {
                        xmlReader.next()
                    }
                }
            }
        } catch (XMLStreamException e) {
            def errorMsg = new StringBuilder("Error while extracting property ").append(elementName).
                        append(".").append(attributeName).append(" from ").append(model.properties)
            errorMsg.append(". The offending file caused ${e.message}.\n")
            log.error (errorMsg.toString(), e)
        } finally {
            xmlReader?.close()
            fileReader?.close()
            return theResult
        }
    }

    private static String processXmlElement(XMLStreamReader reader, String element, String attribute) {
        if (element.equals(reader.getLocalName())) {
            if (attribute.startsWith("xmlns")) {
                if (attribute.contains(":")) {
                    //Strings no longer have an internal reference to arrays, so substring() is out of the question
                    String ns = attribute.dropWhile{it != ':'}.drop(1)
                    return reader.getNamespaceURI(ns)
                }
                return reader.getNamespaceURI()
            }
            return reader.getAttributeValue(null, attribute)
        }
        return null
    }
}
