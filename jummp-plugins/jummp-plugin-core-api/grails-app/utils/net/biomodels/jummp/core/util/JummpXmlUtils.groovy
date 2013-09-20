package net.biomodels.jummp.core.util

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

public class JummpXmlUtils {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    public static String findModelElement(final File model, final String elementName) {
        if (!model || !model.canRead() || !elementName) {
            log.error("Refusing to find element ${elementName} in file ${model.properties}.")
            return ""
        }
        String elem = parseXmlFile.curry(model)({ XMLStreamReader r ->
            if (elementName.equals(r.getLocalName())) {
                return r.getElementText()
            }
            return false
        })
        return elem ? elem : ""
    }

    public static def parseXmlFile = { File f, Closure action ->
        // deal with sanitisation elsewhere
        assert f && f.canRead()
        def fileReader = new FileReader(f)
        String theResult
        XMLInputFactory factory = XMLInputFactory.newInstance()
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE)
        XMLStreamReader xmlReader
        try {
            xmlReader = factory.createXMLStreamReader(fileReader)
            boolean found = false
            while (xmlReader.hasNext() && !found) {
                xmlReader.next()
                if (xmlReader.startElement) {
                    def result = action(xmlReader)
                    if (result) {
                        theResult = result
                        found = true
                    } else {
                        xmlReader.next()
                    }
                }
            }
        } catch (XMLStreamException e) {
            log.error("Error while parsing XML file ${model.properties}: ${e.message}.", e)
        } finally {
            xmlReader?.close()
            fileReader?.close()
            return theResult
        }
    }

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
        String attr = parseXmlFile.curry(model)({ XMLStreamReader reader ->
            if (elementName.equals(reader.getLocalName())) {
                if (attributeName.startsWith("xmlns")) {
                    if (attributeName.contains(":")) {
                        // Can't use substring() as the underlying array reference has been dropped
                        String ns = attributeName.dropWhile{it != ':'}.drop(1)
                        return reader.getNamespaceURI(ns)
                    }
                    return reader.getNamespaceURI()
                }
                return reader.getAttributeValue(null, attributeName)
            }
            return ""
       })
    }
}
