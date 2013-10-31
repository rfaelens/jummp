/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


import groovy.xml.MarkupBuilder
import java.text.SimpleDateFormat

target(weceem: "Create WeCeem Export File") {
    ant.delete(dir: "build/weceem")
    ant.mkdir(dir: "build/weceem")
    ant.mkdir(dir: "build/weceem/files")
    ant.mkdir(dir: "build/weceem/files/Image")
    ant.mkdir(dir: "build/weceem/files/resources")

    def writer = new FileWriter(new File("build/weceem/content.xml"))
    def xml = new MarkupBuilder(writer)

    def directory = new File("weceem")
    int counter = 1
    int order = 1
    int defaultBlogTemplate = 0

    SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy")
    String date = df.format(new Date())

    xml.content() {
        directory.eachDir { subdir ->
            int directoryId = counter++
            'org.weceem.content.WcmFolder'() {
                id(class: "java.lang.Long", directoryId)
                aliasURI(class: "java.lang.String", subdir.name.toLowerCase())
                changedBy(class: "java.lang.String", "unknown")
                changedOn(class: "java.sql.Timestamp", date)
                children(class: 'org.hibernate.collection.PersistentSortedSet') {
                    int subdirCounter = counter
                    subdir.eachFile { file ->
                        if (file.isDirectory()) {
                            return // continue
                        }
                        if (subdir.name.toLowerCase() == "templates") {
                            'org.weceem.content.WcmTemplate'(subdirCounter++)
                        } else if (subdir.name.toLowerCase() == "widgets") {
                            'org.weceem.content.WcmWidget'(subdirCounter++)
                        } else {
                            'org.weceem.html.WcmHTMLContent'(subdirCounter++)
                        }
                    }
                }
                createdBy(class: 'java.lang.String', "unknown")
                createdOn(class: 'java.sql.Timestamp', date)
                orderIndex(class: 'java.lang.Integer', order++)
                publishFrom(class: 'java.sql.Timestamp', date)
                status(class: 'org.weceem.content.WcmStatus', 400)
                title(class: 'java.lang.String', subdir.name)
                validFor(class: 'java.lang.Integer', 86400)
                tags()
            }
            int subDirOrder = 1
            subdir.eachFile { file ->
                if (file.isDirectory() || file.name.endsWith("~")) {
                    return // continue
                }
                String name = file.name.tokenize(".")[0]
                if (subdir.name.toLowerCase() == "templates") {
                    if (name.toLowerCase().equals("default-blog-template")) {
                        defaultBlogTemplate = counter + 1
                    }
                    'org.weceem.content.WcmTemplate'() {
                        id(class: "java.lang.Long", counter++)
                        aliasURI(class: "java.lang.String", name.toLowerCase())
                        changedBy(class: "java.lang.String", "unknown")
                        changedOn(class: "java.sql.Timestamp", date)
                        children(class: 'org.hibernate.collection.PersistentSortedSet')
                        content(class: "java.lang.String", file.text)
                        createdBy(class: 'java.lang.String', "unknown")
                        createdOn(class: 'java.sql.Timestamp', date)
                        language(class: 'java.lang.String', "eng")
                        orderIndex(class: 'java.lang.Integer', subDirOrder++)
                        parent(class: 'org.weceem.content.WcmFolder', directoryId)
                        publishFrom(class: 'java.sql.Timestamp', date)
                        status(class: 'org.weceem.content.WcmStatus', 400)
                        title(class: 'java.lang.String', name)
                        userSpecificContent(class: 'java.lang.Boolean', false)
                        validFor(class: 'java.lang.Integer', 86400)
                        tags()
                    }
                } else if (subdir.name.toLowerCase() == "widgets") {
                    'org.weceem.content.WcmWidget'() {
                        id(class: "java.lang.Long", counter++)
                        aliasURI(class: "java.lang.String", name.toLowerCase())
                        changedBy(class: "java.lang.String", "unknown")
                        changedOn(class: "java.sql.Timestamp", date)
                        children(class: 'org.hibernate.collection.PersistentSortedSet')
                        content(class: "java.lang.String", file.text)
                        createdBy(class: 'java.lang.String', "unknown")
                        createdOn(class: 'java.sql.Timestamp', date)
                        language(class: 'java.lang.String', "eng")
                        orderIndex(class: 'java.lang.Integer', subDirOrder++)
                        parent(class: 'org.weceem.content.WcmFolder', directoryId)
                        publishFrom(class: 'java.sql.Timestamp', date)
                        status(class: 'org.weceem.content.WcmStatus', 400)
                        title(class: 'java.lang.String', name)
                        userSpecificContent(class: 'java.lang.Boolean', false)
                        validFor(class: 'java.lang.Integer', 86400)
                        tags()
                    }
                } else {
                    'org.weceem.html.WcmHTMLContent'() {
                        id(class: "java.lang.Long", counter++)
                    }
                }
            }
        }
        int subDirOrder = 1
        directory.eachFile { file ->
            if (file.isDirectory() || file.name.endsWith("~")) {
                return // continue
            }
            String name = file.name.tokenize(".")[0]
            'org.weceem.html.WcmHTMLContent'() {
                id(class: "java.lang.Long", counter++)
                aliasURI(class: "java.lang.String", name.toLowerCase())
                allowGSP(class: 'java.lang.Boolean', true)
                changedBy(class: "java.lang.String", "unknown")
                changedOn(class: "java.sql.Timestamp", date)
                children(class: 'org.hibernate.collection.PersistentSortedSet')
                content(class: "java.lang.String", file.text)
                createdBy(class: 'java.lang.String', "unknown")
                createdOn(class: 'java.sql.Timestamp', date)
                language(class: 'java.lang.String', "eng")
                orderIndex(class: 'java.lang.Integer', subDirOrder++)
                publishFrom(class: 'java.sql.Timestamp', date)
                status(class: 'org.weceem.content.WcmStatus', 400)
                title(class: 'java.lang.String', name)
                userSpecificContent(class: 'java.lang.Boolean', false)
                validFor(class: 'java.lang.Integer', 86400)
                tags()
            }
        }
        // news blog
        'org.weceem.blog.WcmBlog'() {
            id(class: 'java.lang.Long', counter++)
            aliasURI(class: 'java.lang.String', "news")
            changedBy(class: "java.lang.String", "unknown")
            changedOn(class: "java.sql.Timestamp", date)
            children(class: 'org.hibernate.collection.PersistentSortedSet')
            commentMarkup(class: 'java.lang.String')
            createdBy(class: 'java.lang.String', "unknown")
            createdOn(class: 'java.sql.Timestamp', date)
            maxEntriesToDisplay(class: 'java.lang.Integer', 5)
            orderIndex(class: 'java.lang.Integer', order++)
            publishFrom(class: 'java.sql.Timestamp', date)
            status(class: 'org.weceem.content.WcmStatus', 400)
            title(class: 'java.lang.String', "News")
            template(class: 'org.weceem.content.WcmTemplate', defaultBlogTemplate)
            validFor(class: 'java.lang.Integer', 86400)
            tags()
        }
    }
    ant.zip(destfile: "src/groovy/weceem-jummp-default-space.zip", basedir: "build/weceem")
}
