/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
**/

package net.biomodels.jummp.plugins.mdl

/**
 * Tag library providing helpers for generating the MDL-specific tabs.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class MdlTagLib {
    /**
     * Default encoding for the tags in this library.
     */
    static defaultEncodeAs = 'html'
    /**
     * Map of custom encodings.
     */
    static encodeAsForTags = [addMenuItem: 'raw', renderMdlFile: 'raw', renderDataFile: 'raw']
    /**
     * The namespace of this tag library
     */
    static namespace = "mdl"

    /**
     * Generates the anchors for MDL-specific tabs.
     * @attr file REQUIRED the file which will be displayed in this tab.
     */
    def addMenuItem = { attrs, body ->
        if (!attrs.file) {
            return
        }
        String name = attrs.file.name
        String fileName = name
        String fileNameHash = "mdl${name.encodeAsSHA256().substring(0, 10)}"
        String fileHref = "#$fileNameHash"
        assert fileHref != "#mdl" && fileHref.length() == 14
        out << body("fileHref": fileHref, "fileName": fileName)
    }

    /**
     * Renders a file which contains MDL.
     * @attr file REQUIRED the file which should be displayed.
     */
    def renderMdlFile = { attrs, body ->
        if (!(attrs.file)) {
            return
        }
        final File FILE = attrs.file
        String id = "mdl${FILE.name.encodeAsSHA256().substring(0, 10)}"
        String htmlText = FILE.text.encodeAsHTML()
        out << render(template: "/templates/renderMdl", model: [id: id, text: htmlText])
    }

    /**
     * Renders a CSV file.
     * @attr file REQUIRED the file which should be displayed.
     */
    def renderDataFile = { attrs ->
        if (!(attrs.file)) {
            return
        }
        final File FILE = attrs.file
        String id = "mdl${FILE.name.encodeAsSHA256().substring(0, 10)}"
        String text = FILE.text.encodeAsJavaScript()
        out << render(template: "/templates/renderCsv", model: [id: id, text: text])
    }
}
