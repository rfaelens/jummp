package net.biomodels.jummp.plugins.sbml

import org.sbml.jsbml.Model
import org.sbml.jsbml.Reaction
import org.sbml.jsbml.ExplicitRule
import org.sbml.jsbml.Species
import org.sbml.jsbml.ModifierSpeciesReference
import org.sbml.jsbml.SpeciesReference
import org.sbml.jsbml.ListOf
import org.sbml.jsbml.CVTerm
import org.apache.commons.io.FileUtils

/**
 * @short Class to generate a Reaction Graph from SBML.
 *
 * Based on the existing SBML2Dot Converter (uk.ac.ebi.sbml.converter.dot.SBML2Dot.java) adopted to JSBML and Groovy.
 * @todo replace by newer version of converters
 */
class ReactionToDot {
    private Model model
    private Map<Species, Integer> speciesCardinalities = [:]
    private static final int MAX_EDGE_ALLOWED = 4

    // TODO: use MIRIAM service to resolve all urn
    private static Hashtable<String, String> speciesAnnotationMap = [
            "urn:miriam:intact": "complex",
            "urn:miriam:uniprot": "protein",
            "urn:miriam:interpro": "protein",
            "urn:miriam:pirsf": "protein",
            "urn:miriam:clustr": "protein",
            "urn:miriam:obo.chebi": "smallMolecule",
            "urn:miriam:kegg.compound": "smallMolecule"
    ]

    ReactionToDot(Model model) {
        this.model = model
    }

    public void saveDotToFile(File file) {
        file.write generateReactionGraph()
    }

    public byte[] convertToSvg() {
        File dotFile = File.createTempFile("jummp", "dot")
        saveDotToFile dotFile
        File svgFile = File.createTempFile("jummp", "svg")
        def process = "dot -Tsvg -o ${svgFile.absolutePath} ${dotFile.absolutePath}".execute()
        process.waitFor()
        FileUtils.deleteQuietly(dotFile)
        if (process.exitValue()) {
            FileUtils.deleteQuietly(svgFile)
            return null
        }
        byte[] bytes = svgFile.readBytes()
        FileUtils.deleteQuietly(svgFile)
        return bytes
    }

    public String generateReactionGraph() {
        ListOf<Species> species = model.listOfSpecies
        ListOf<Reaction> reactions = model.listOfReactions

        StringWriter writer = new StringWriter()
        // print the beginning of the graph object
        writer << "digraph G {\n"
        writer << "  node [fontsize=10];\n"
        writer << "  graph [rank=same];\n"
        writer << "  graph [rankdir=LR];\n"

        if (species.isEmpty() || reactions.isEmpty()) {
            writer << ' noSpecies[fontsize=14,label="There are no species or no reactions in this model so no graph can be automatically generated.", shape="plaintext"]'
            return writer.toString()
        }

        writer << "  // Species\n"
        species.each {
            if (!speciesCardinalities.containsKey(it)) {
                speciesCardinalities.put(it, 0)
            }

            writer << speciesComment(it)
            writer << "\n"

            if (it.id.equalsIgnoreCase("emptyset") || speciesCardinalities.get(it) > MAX_EDGE_ALLOWED) {
                writer << "\n"
                return
            }
            String displayName = it.name
            if (!displayName || displayName.trim().length() == 0) {
                displayName = it.id
            } else if (displayName.equalsIgnoreCase("emptyset") || displayName.equalsIgnoreCase("empty set")) {
                writer << "\n"
                return
            }

            String shape = getShape(it.annotation.listOfCVTerms)

            if (displayName.length() > 5 && shape.contains("circle")) {
                shape = "\"ellipse\""
            }

            writer << "    ${it.id} [label=\"${displayName}\", shape=${shape}];\n"
        }

        reactions.each {
            String shape = "box"
            writer << "    ${it.id} [label=\"\", shape=box, fixedsize=true, width=0.2, height=0.2];\n"
            ListOf<SpeciesReference> products = it.listOfProducts
            ListOf<SpeciesReference> reactants = it.listOfReactants
            ListOf<ModifierSpeciesReference> modifiers = it.listOfModifiers

            String reactionNodeId = it.id

            if (reactants.isEmpty()) {
                // Create an empty set node
                writer << "    ${it.id}_empty [shape=plaintext, label=\"Ø\", fontsize=15,fontcolor=blue];"
                writer << "      \"${it.id}_empty\" -> \"${it.id}\" [arrowhead=none];\n"
            } else {
                // we create an intermediary node to try to force the edge to start on the middle of the reaction node
                reactionNodeId = createIntermediateReactionNode(writer, it, "r")
            }

            reactants.each { speciesReference ->
                Species specie = speciesReference.model.getSpecies(speciesReference.species)
                String speciesNodeId = specie.id
                String speciesShape = getShape(specie.annotation.listOfCVTerms)

                if (specie.id.equalsIgnoreCase("emptyset")) {
                    writer << "    ${it.id}_empty [shape=plaintext, label=\"Ø\", fontsize=15,fontcolor=blue];"
                    writer << "      \"${it.id}_empty\" -> \"${it.id}\" [arrowhead=none];\n"
                    speciesNodeId = "${it.id}_empty"
                } else if (speciesCardinalities.get(species) > MAX_EDGE_ALLOWED) {
                    String clonedSpeciesNodeId = it.id + "_" + specie.id + "_r"
                    writer << "    ${clonedSpeciesNodeId} [label=\"${specie.name ? specie.name : specie.id}\", shape=${speciesShape};"
                    speciesNodeId = clonedSpeciesNodeId
                }

                // print the arrow / edge
                if (it.reversible) {
                    writer << "      \"${speciesNodeId}\" -> \"${reactionNodeId}\"[arrowhead=none, arrowtail=normal,arrowsize=1.0];\n"
                } else {
                    writer << "      \"${speciesNodeId}\" -> \"${reactionNodeId}\"[arrowhead=none];\n"
                }
            }

            reactionNodeId = it.id
            if (products.isEmpty()) {
                // Create an empty set node
                String emptySetNodeId = "${it.id}_empty";
                writer << "${emptySetNodeId} [shape=plaintext, label=\"Ø\", fontsize=15,fontcolor=blue];"
                writer << "      \"${it.id}\" -> \"${emptySetNodeId}\";\n"
            } else {
                reactionNodeId = createIntermediateReactionNode(writer, it, "p");
            }

            products.each { speciesReference ->
                Species specie = speciesReference.model.getSpecies(speciesReference.species)
                String speciesNodeId = specie.id
                String speciesShape = getShape(specie.annotation.listOfCVTerms)

                if (specie.id.equalsIgnoreCase("emptyset")) {
                    writer << "    ${it.id}_empty [shape=plaintext, label=\"Ø\", fontsize=15,fontcolor=blue];"
                    writer << "      \"${it.id}_empty\" -> \"${it.id}\" [arrowhead=none];\n"
                    speciesNodeId = "${it.id}_empty"
                } else if (speciesCardinalities.get(specie) > MAX_EDGE_ALLOWED) {
                    String clonedSpeciesNodeId = it.id + "_" + specie.id + "_p"
                    writer << "    ${clonedSpeciesNodeId} [label=\"${specie.name ? specie.name : specie.id}\", shape=${speciesShape};"
                    speciesNodeId = clonedSpeciesNodeId
                }

                writer << "      \"${reactionNodeId}\" -> \"${speciesNodeId}\";\n"
            }

            reactionNodeId = it.id
            String arrowHead = "odot"

            if (!modifiers.isEmpty()) {
                reactionNodeId = createIntermediateReactionNode(writer, it, "m")
                arrowHead = "none"
            }

            modifiers.each { speciesReference ->
                Species specie = speciesReference.model.getSpecies(speciesReference.species)
                String speciesNodeId = specie.id

                if (speciesCardinalities.get(specie) > MAX_EDGE_ALLOWED) {
                    String speciesShape = getShape(specie.annotation.listOfCVTerms)
                    String clonedSpeciesNodeId = it.id + "_" + specie.id + "_m"
                    writer << "    ${clonedSpeciesNodeId} [label=\"${specie.name ? specie.name : specie.id}\", shape=${speciesShape};"
                    speciesNodeId = clonedSpeciesNodeId
                }

                // print the arrow / edge
                writer << "      \"${speciesNodeId}\" -> \"${reactionNodeId}\"[arrowhead=${arrowHead}];\n"
            }
        }
        writer << "\n}\n"

        return writer.toString()
    }

    private String speciesComment(Species species) {
        StringBuilder comment = new StringBuilder()
        String id = species.id
        String name = species.name;

        if (!name || name.trim().length() == 0) {
            name = id
        }

        comment << "    // Species: "
        comment << "  id = ${id}"
        comment << ", name = ${name}"

        if (species.constant) {
            comment << ", constant"
        } else {
            // check for boundary condition and then display
            if (species.boundaryCondition) {
                comment << ", involved in a rule "

            } else {
                // affected by rules or kinetic law not both
                if (species.model.listOfRules.find { it instanceof ExplicitRule && it.variable == id }) {
                    comment << ", involved in a rule "
                } else {
                    comment << ", affected by kineticLaw"
                }
            }
        }

        if (speciesCardinalities.containsKey(species)  && speciesCardinalities.get(species) > MAX_EDGE_ALLOWED) {
            comment << ", will be cloned in the graph."
        }

        return comment.toString()
    }

    private String createIntermediateReactionNode(StringWriter writer, Reaction reaction, String suffix) {

        String intermediateReactionNodeId = "${reaction.id}_intermediate_${suffix}"

        writer << "      ${intermediateReactionNodeId} [label=\"\", shape=point,width=0.001,height=0.001];"

        switch (suffix) {
        case "r":
            writer << "      \"${intermediateReactionNodeId}\" -> \"${reaction.id}\"[arrowhead=none];\n"
            break
        case "p":
            writer << "      \"${reaction.id}\" -> \"${intermediateReactionNodeId}\"[arrowhead=none];\n"
            break
        default:
            writer << "      \"${reaction.id}\" -> \"${intermediateReactionNodeId}\"[arrowhead=none, arrowtail=odot];\n"
            break
        }

        return intermediateReactionNodeId;
    }

    private String getShape(List<CVTerm> cvTerms) {

        String shape = "\"ellipse\""

        if (cvTerms.isEmpty()) {
            return shape
        }

        String annotation = "" //represent the annotations one by one

        String physicalEntityType = "physicalEntity" //default value, will change depending the annotations

        Map<String, String> miriamAnnotations = [:]

        cvTerms.each { cvTerm ->
            cvTerm.resources.each {
                Map.Entry<String, String> miriamAnnotation = parseMiriamAnnotation(it)
                if (!miriamAnnotation) {
                    return
                }

                String annotationDB = miriamAnnotation.value
                String annotationIdentifier = miriamAnnotation.key
                String annotationTestEnsembl = ""

                //constraint on a database which have yet an other entry
                //ex:Chebi (small molecule DB) contains DNA and RNA and Ensembl
                String officialAnnotation = null
                try {
                    officialAnnotation = annotationDB + ":" + URLEncoder.encode(annotationIdentifier, "UTF-8")
                } catch (UnsupportedEncodingException e) {
                    officialAnnotation = annotation
                    e.printStackTrace()
                }

                if ((annotationDB.length() + 8) < officialAnnotation.length()) {
                    annotationTestEnsembl = officialAnnotation.substring(0, officialAnnotation.lastIndexOf(":") + 8)
                }

                if (speciesAnnotationMap.containsKey(annotationDB)) {
                    miriamAnnotations.put(miriamAnnotation.key, miriamAnnotation.value)
                    physicalEntityType = speciesAnnotationMap.get(annotationDB)

                    if (speciesAnnotationMap.containsKey(officialAnnotation)) {
                        physicalEntityType = speciesAnnotationMap.get(annotation)
                    }
                } else {
                    // Test for Ensembl annotation, check the 7 first characters of the identifier
                    if (speciesAnnotationMap.containsKey(annotationTestEnsembl)) {
                        if (miriamAnnotations.isEmpty()) {
                            physicalEntityType = speciesAnnotationMap.get(annotationTestEnsembl)
                        }
                        miriamAnnotations.put(miriamAnnotation.key, miriamAnnotation.value)
                    } else {
                        // constraint on a database which have not already an other entry
                        if (speciesAnnotationMap.containsKey(annotation)){
                            physicalEntityType = speciesAnnotationMap.get(annotation)
                            miriamAnnotations.put(miriamAnnotation.key, miriamAnnotation.value)
                        }
                    }
                }
            }
        }

        if (physicalEntityType.equals("smallMolecule")) {
            shape = "\"circle\", fixedsize=true, size=1"
        } else if (physicalEntityType.equals("protein") || physicalEntityType.equals("dna") || physicalEntityType.equals("rna")) {
            shape = "\"rect\", style=\"rounded\""
        }

        return shape
    }

    private Map.Entry<String, String> parseMiriamAnnotation(String annotation) {
        String uri = null
        String id = null

        int indexOfColon = annotation.lastIndexOf(":")

        if (indexOfColon != -1) {
            uri = annotation.substring(0, indexOfColon)

            try {
                id = URLDecoder.decode(annotation.substring(indexOfColon + 1), "UTF-8")
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace()
            }
        }

        if (uri && id) {

            // Could use a call to miriamWS.getMiriamUri(String)
            // but this way avoid a lot of unecessary call to the webservice
            // if the miriam scheme is changing again may be it would be simplier to use the WS function.
            String officialURN = null//ConstraintFileReader.officialURIs.get(uri)

            if (!officialURN) {
                officialURN = uri
            }

            return new AbstractMap.SimpleEntry(id, officialURN)
        }
        return null
    }
}
