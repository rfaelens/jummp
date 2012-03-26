package net.biomodels.jummp.webapp

import javax.xml.transform.stream.StreamSource
import javax.xml.transform.stream.StreamResult

class JummpTagLib {
    static namespace = "jummp"

    /**
     * Renders the HTML code for a JUMMP styled button.
     * That is including the active glow.
     * @attr id The HTML id (optional)
     * @attr class The HTML class (optional)
     **/
    def button = { attrs, body ->
        out << render(template: "/templates/buttonTemplate", model: [attrs: attrs, body: body()])
    }

    /**
    * Renders a compact title of a publication.
    * Format: Journal Year Month; Volume (Issue) Pages
    * All fields are optional.
    * @attr publication REQUIRED The PublicationTransportCommand
    */
   def compactPublicationTitle = { attrs ->
       def publication = attrs.publication
       String publicationSummary = publication.journal
       if (publication.year) {
           publicationSummary += " " + publication.year
           if (publication.month) {
               publicationSummary += " " + publication.month
           }
       }
       if (publication.volume) {
           publicationSummary += "; " + publication.volume
       }
       if (publication.issue) {
           publicationSummary += "(" + publication.issue + ")"
       }
       if (publication.pages) {
           publicationSummary += ": " + publication.pages
       }
       out << publicationSummary
   }

   /**
    * Renders a span element with an alert icon and initially hidden.
    */
   def errorField = { attrs ->
       out << "<span class=\"ui-icon ui-icon-alert\" style=\"display: none\"></span>"
   }

   /**
    * Renders a list of Annotations. An annotation is a map consisting of following key/value pairs:
    * @li biologicalQualifier
    * @li modelQualifier
    * @li qualifier
    * @li resources
    * A description of the keys can be found in ISbmlService.getAnnotations().
    * @attr annotations List of Annotation maps (required)
    * @attr biological Boolean attribute whether to include annotations with biological qualifier (optional, default @c true)
    * @attr model Boolean attribute whether to include annotations with model qualifier (optional, decault @c true)
    * @todo move into an SBML taglib
    */
   def annotations = { attrs ->
       boolean biologicalAnnotations = true
       boolean modelAnnotations = true
       if (attrs.containsKey("biological")) {
           biologicalAnnotations = Boolean.parseBoolean(attrs.biological)
       }
       if (attrs.containsKey("model")) {
           modelAnnotations = Boolean.parseBoolean(attrs.model)
       }
       List<String> renderedAnnotations = []
       attrs.annotations.each { annotation ->
           if (annotation.qualifier == "BQB_UNKNOWN" || annotation.qualifier == "BQM_UNKNOWN") {
               return
           }
           if (annotation.biologicalQualifier && !biologicalAnnotations) {
               return
           }
           if (annotation.modelQualifier && !modelAnnotations) {
               return
           }
           renderedAnnotations << renderAnnotation(annotation)
       }
       if (renderedAnnotations.size() == 1) {
           out << renderedAnnotations.first()
       } else {
           out << "<ul>"
           renderedAnnotations.each {
               out << "<li>${it}</li>"
           }
           out << "</ul>"
       }
   }

   /**
    * Renders one Annotations Map.
    * @see annotations
    * @todo move into an SBML taglib
    */
   def renderAnnotation = { annotation ->
       renderQualifer(annotation.qualifier)
       if (annotation.resources.size() == 1) {
           out << "&nbsp;"
           out << renderURN(annotation.resources[0])
       } else {
           out << "<ul>"
           annotation.resources.each {
               out << "<li>"
               out << renderURN(it)
               out << "</li>"
           }
           out << "</ul>"
       }
   }

   /**
    * Renders a single MIRIAM URN from an annotation resource.
    * @todo move into an SBML taglib
    */
   def renderURN = { attrs ->
       Map miriam = attrs
       if (attrs.resource) {
           miriam = attrs.resource
       }
       if (miriam.containsKey("dataTypeLocation") && miriam.containsKey("dataTypeName") && miriam.containsKey("name") && miriam.containsKey("url")) {
           out << "<a target=\"_blank\" href=\"${miriam["dataTypeLocation"]}\">${miriam["dataTypeName"]}</a>"
           out << "&nbsp;"
           out << "<a target=\"_blank\" href=\"${miriam["url"]}\">${miriam.name}</a>"
       } else if (miriam.containsKey("name")) {
           out << miriam["name"]
       } else if (miriam.containsKey("urn")) {
           out << miriam["urn"]
       } else {
           out << miriam
       }
   }

   /**
    * Renders a Biomodels.net annotation qualifier.
    * The qualifier has to be one of org.sbml.jsbml.CVTerm.Qualifier
    * @todo move into an SBML taglib
    */
   def renderQualifer = { qualifier ->
       switch (qualifier) {
       case "BQB_ENCODES":
           out << g.message(code: "sbml.qualifier.bqb.encodes")
           break
       case "BQB_HAS_PART":
           out << g.message(code: "sbml.qualifier.bqb.hasPart")
           break
       case "BQB_HAS_PROPERTY":
           out << g.message(code: "sbml.qualifier.bqb.hasProperty")
           break
       case "BQB_HAS_VERSION":
           out << g.message(code: "sbml.qualifier.bqb.hasVersion")
           break
       case "BQB_IS":
           out << g.message(code: "sbml.qualifier.bqb.is")
           break
       case "BQB_IS_DESCRIBED_BY":
           out << g.message(code: "sbml.qualifier.bqb.isDescribedBy")
           break
       case "BQB_IS_ENCODED_BY":
           out << g.message(code: "sbml.qualifier.bqb.isEncodedBy")
           break
       case "BQB_IS_HOMOLOG_TO":
           out << g.message(code: "sbml.qualifier.bqb.isHomologTo")
           break
       case "BQB_IS_PART_OF":
           out << g.message(code: "sbml.qualifier.bqb.isPartOf")
           break
       case "BQB_IS_PROPERTY_OF":
           out << g.message(code: "sbml.qualifier.bqb.isPropertyOf")
           break
       case "BQB_IS_VERSION_OF":
           out << g.message(code: "sbml.qualifier.bqb.isVersionOf")
           break
       case "BQB_OCCURS_IN":
           out << g.message(code: "sbml.qualifier.bqb.occursIn")
           break
       case "BQM_IS":
           out << g.message(code: "sbml.qualifier.bqm.is")
           break
       case "BQM_IS_DERIVED_FROM":
           out << g.message(code: "sbml.qualifier.bqm.isDerivedFrom")
           break
       case "BQM_IS_DESCRIBED_BY":
           out << g.message(code: "sbml.qualifier.bqm.isDescribedBy")
           break
       default:
           out << ""
           break
       }
   }

   def contentMathML = { attrs ->
       if (!transformer) {
           def factory = TransformerFactory.newInstance()
           transformer = factory.newTransformer(new StreamSource(new File(servletContext.getRealPath("/xsl/mathmlc2p.xsl"))))
       }
       transformer.transform(new StreamSource(new StringReader(attrs.mathML)), new StreamResult(out))
   }

   /**
    * Renders a table row with the resolved SBO term.
    * The primary use for this tag is inside of the tooltips for various SBML elements
    * The tag expects an attribute sbo containing the integer value of the sbo term.
    * In case the tag is not set or an empty string the table row is not rendered.
    * @attr sbo REQUIRED the numerical SBO term without the urn header
    */
   def sboTableRow = { attrs ->
       Map sbo = attrs
       if (attrs.sbo instanceof Map) {
           sbo = attrs.sbo
       }
       if (!sbo || sbo.isEmpty()) {
           return
       }
       out << render(template: "/templates/sboTableRow", model: [urn: sbo])
   }

   /**
    * Renders a table row with the given annotations.
    * The primary use for this tag is inside of the tooltips for various SBML elements.
    * The tag expects an attribute annotations which is a list of the annotations to be rendered.
    * In case the list is empty the table row is not rendered.
    * @attr annotations REQUIRED The list of annotations
    */
   def annotationsTableRow = { attrs ->
       if (!attrs.annotations) {
           return;
       }
       out << render(template: "/templates/annotationsTableRow", model: [annotations: attrs.annotations])
   }
}