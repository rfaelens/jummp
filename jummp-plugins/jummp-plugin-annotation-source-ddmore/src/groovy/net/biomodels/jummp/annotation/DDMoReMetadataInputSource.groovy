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
import eu.ddmore.metadata.api.domain.properties.Property
import eu.ddmore.metadata.api.domain.values.*
import eu.ddmore.metadata.api.domain.enums.PropertyRange
import eu.ddmore.metadata.api.domain.sections.*
import groovy.transform.CompileStatic
import net.biomodels.jummp.core.model.RevisionTransportCommand

@CompileStatic
class DDMoReMetadataInputSource implements MetadataInputSource {
    MetadataInformationService service

    boolean supports(RevisionTransportCommand revision) {
        println "ddmore mis supports only PharmML."
        "PharmML" == revision?.format?.name
    }

    List<SectionContainer> buildObjectModel(RevisionTransportCommand revision = null) {
        //should use ObservableGraph with an event listener that updates the tree!
        def graph = new DelegateTree<SimpleVertex, SimpleEdge>()
        def root = new SimpleVertex(value: 'root')
        graph.addVertex(root)

        Id modelId = new Id("Model","http://www.pharmml.org/ontology/PHARMMLO_0000001")
        List<SectionContainer> sections = buildSectionContainers(modelId)
        sections.each { SectionContainer s ->
            graph.addChild(new SimpleEdge(), root, s)
            s.children.each { double n, String name ->
                def thisSection = new CompositeSection(modelId, n, name)
                //currently only have 1 property per section, but this could change
                List<Property> props = service.findPropertiesForSection(thisSection)
                props.first().dump()
                props.each { Property p ->
                    Id pId = p.propertyId
                    def pVertex = new PropertyContainer(value: name, uri: pId.uri,
                            range: buildPropertyRange(p.range))
                    s.annotationProperties.add(pVertex)
                    graph.addChild new SimpleEdge(), s, pVertex
                    buildValuesForProperty(p, pVertex, graph)
                }
            }
        }
        sections
    }

    private void buildValuesForProperty(Property p, PropertyContainer vtx, DelegateTree g) {
        List<Value> values = service.findValuesForProperty(p)
        values.each { Value v ->
            ValueContainer vVertex = visit(v)
            vtx.values.add(vVertex)
            g.addChild(new SimpleEdge(), vtx, vVertex)
        }
    }

    private ValueContainer visit(Value v) {
        Id vId = v.valueId
        String name = vId.label
        String uri = vId.uri
        ValueContainer vVertex
        if (v.valueTree) {
            vVertex = new CompositeValueContainer(value: name, uri: uri)
            List<Value> childValues = ((CompositeValue) v).getValues()
            List<ValueContainer> children = childValues.collect { Value child ->
                visit(child)
            }
            vVertex.children = children
        } else {
            vVertex = new ValueContainer(value: name, uri: uri)
        }
        vVertex
    }

    private List<SectionContainer> buildSectionContainers(Id modelId) {
        List<Section> sections = service.findSectionsForConcept(modelId)
        Map<Double, String> bookkeepingRegion = [:]
        Map<Double, String> ctxOfUseRegion = [:]
        sections.each { Section s ->
            double n = s.sectionNumber
            if (n < 2) {
                bookkeepingRegion.put(n, s.sectionLabel)
            } else {
                ctxOfUseRegion.put(n, s.sectionLabel)
            }
        }
        SectionContainer bk = new SectionContainer(name: 'Bookkeeping', id: "section1",
                children: bookkeepingRegion)
        SectionContainer cou = new SectionContainer(name: 'Context of Use', id: "section2",
                children: ctxOfUseRegion)
        [bk, cou]
    }

    private AnnotationPropertyRange buildPropertyRange(PropertyRange r) {
        AnnotationPropertyRange.valueOf(r.toString())
    }
}

