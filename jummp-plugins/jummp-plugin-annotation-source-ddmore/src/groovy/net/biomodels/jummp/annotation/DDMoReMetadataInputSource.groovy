/**
 * Copyright (C) 2010-2015 EMBL-European Bioinformatics Institute (EMBL-EBI),
 * Deutsches Krebsforschungszentrum (DKFZ)
 *
 * This file is part of Jummp.
 *
 * Jummp is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 */

package net.biomodels.jummp.annotation

import edu.uci.ics.jung.graph.DelegateTree
import edu.uci.ics.jung.graph.ObservableGraph
import edu.uci.ics.jung.graph.event.GraphEventListener
import eu.ddmore.metadata.api.*
import eu.ddmore.metadata.api.domain.Id
import eu.ddmore.metadata.api.domain.enums.ValueSetType
import eu.ddmore.metadata.api.domain.properties.Property
import eu.ddmore.metadata.api.domain.values.*
import eu.ddmore.metadata.api.domain.enums.PropertyRange
import eu.ddmore.metadata.api.domain.sections.*
import groovy.transform.CompileStatic
import net.biomodels.jummp.core.model.RevisionTransportCommand
import ontologies.OntologySource

@CompileStatic
class DDMoReMetadataInputSource implements MetadataInputSource {
    MetadataInformationService service
    final String MODEL_CONCEPT_NAME = "Model"
    final String MODEL_CONCEPT_URI = "http://www.pharmml.org/ontology/PHARMMLO_0000001"
    final Id modelId = new Id(MODEL_CONCEPT_NAME, MODEL_CONCEPT_URI)
    /**
     * Compare two Values based on either their rank or their label.
     * The latter are assumed to never be empty or undefined.
     */
    final Comparator VALUE_COMPARATOR = { Value v1, Value v2 ->
        String v1Name = v1.valueId?.label
        String v2Name = v2.valueId?.label
        String v1Rank = v1.valueRank
        String v2Rank = v2.valueRank
        if (v1Rank && v2Rank) {
            return v1Rank <=> v2Rank
        } else {
            return v1Name <=> v2Name
        }
    } as Comparator

    boolean supports(RevisionTransportCommand revision) {
        "PharmML" == revision?.format?.name
    }

    List<SectionContainer> buildObjectModel(RevisionTransportCommand revision = null) {
        //should use ObservableGraph with an event listener that updates the tree!
        def graph = new DelegateTree<SimpleVertex, SimpleEdge>()
        def root = new SimpleVertex(value: 'root')
        graph.addVertex(root)

        Map<SectionContainer, List<DDMoReSectionAdapter>> sectionsMap = buildSectionContainers(modelId)
        def sections = []
        sectionsMap.each { SectionContainer s, List<DDMoReSectionAdapter> adapters ->
            graph.addChild(new SimpleEdge(), root, s)
            adapters.each { DDMoReSectionAdapter adapter ->
                String n = String.valueOf(adapter.sectionNumber)
                String name = adapter.label
                String tooltip = adapter.tooltip
                def thisSection = new CompositeSection(modelId, n, name)
                //currently only have 1 property per section, but this could change
                List<Property> props = service.findPropertiesForSection(thisSection)
                props.each { Property p ->
                    Id pId = p.propertyId
                    def pVertex = new PropertyContainer(value: name, uri: pId.uri,
                            tooltip: tooltip, range: buildPropertyRange(p.range))
                    s.annotationProperties.add(pVertex)
                    graph.addChild new SimpleEdge(), s, pVertex
                    buildValuesForProperty(p, pVertex, graph)
                }
            }
            sections << s
        }
        sections
    }

    private void buildValuesForProperty(Property p, PropertyContainer vtx, DelegateTree g) {
        List<Value> values;
        if(p.getValueSetType().equals(ValueSetType.ONTOLOGY)){
            //TODO: connect to ols to get values
        } else {
            values = service.findValuesForProperty(p)
        }

        TreeMap orderedValues = new TreeMap(VALUE_COMPARATOR)
        values.each { Value v ->
            ValueContainer vVertex = visit(v)
            orderedValues.put(v, vVertex)
            g.addChild(new SimpleEdge(), vtx, vVertex)
        }
        vtx.values.addAll(orderedValues.values())
    }

    private ValueContainer visit(Value v) {
        Id vId = v.valueId
        String name = vId.label
        String uri = vId.uri
        ValueContainer vVertex
        if (v.isValueTree()) {
            vVertex = new CompositeValueContainer(value: name, uri: uri)
            List<Value> childValues = ((CompositeValue) v).getValues()
            TreeMap orderedValues = new TreeMap(VALUE_COMPARATOR)
            childValues.each { Value child ->
                ValueContainer thisValueContainer = visit(child)
                orderedValues.put(child, thisValueContainer)
            }
            vVertex.children.addAll orderedValues.values()
        } else {
            vVertex = new ValueContainer(value: name, uri: uri)
        }
        vVertex
    }

    private Map<SectionContainer, List<DDMoReSectionAdapter>> buildSectionContainers(Id modelId) {
        List<Section> sections = service.findSectionsForConcept(modelId)
        List<DDMoReSectionAdapter> bookkeepingRegion = []
        List<DDMoReSectionAdapter> ctxOfUseRegion = []
        sections.each { Section s ->
            double n = Double.parseDouble(s.sectionNumber)
            def dsa = new DDMoReSectionAdapter(sectionNumber: n, label: s.sectionLabel,
                    tooltip: s.toolTip)
            if (n < 2) {
                bookkeepingRegion.add dsa
            } else {
                ctxOfUseRegion.add dsa
            }
        }
        Map<SectionContainer, List<DDMoReSectionAdapter>> result = [:]
        SectionContainer bk = new SectionContainer(name: 'Bookkeeping', id: "section1")
        SectionContainer cou = new SectionContainer(name: 'Context of Use', id: "section2")
        result.put(bk, bookkeepingRegion)
        result.put(cou, ctxOfUseRegion)
        result
    }

    private AnnotationPropertyRange buildPropertyRange(PropertyRange r) {
        AnnotationPropertyRange.valueOf(r.toString())
    }
}

/**
 * Adapter between {@link eu.ddmore.metadata.api.domain.sections.Section}
 * and our own {@link net.biomodels.jummp.annotation.PropertyContainer}
 */
class DDMoReSectionAdapter {
    double sectionNumber
    String label
    String tooltip
}

