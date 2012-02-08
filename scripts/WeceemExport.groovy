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
                if (file.isDirectory()) {
                    return // continue
                }
                String name = file.name.tokenize(".")[0]
                if (subdir.name.toLowerCase() == "templates") {
                    if (name.toLowerCase().equals("default-blot-template")) {
                        defaultBlogTemplate = counter
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
            if (file.isDirectory()) {
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
