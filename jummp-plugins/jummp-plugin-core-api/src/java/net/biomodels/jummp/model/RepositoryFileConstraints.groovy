import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata

constraints = {
        path(blank: false, nullable: false,
            validator: { p, rf ->
                System.out.println("CALLING VALIDATION ON REP FILE!")
                if (!p || !new File(p).exists()) {
                    return false
                }
                def f = new File(p).getCanonicalFile()
                def sherlock = new DefaultDetector()
                String properType = sherlock.detect(new BufferedInputStream(
                        new FileInputStream(f)), new Metadata()).toString()
                if (!rf.mimeType.equals(properType)) {
                    rf.mimeType = properType
                }
                return true
            })
        description(nullable: true, maxSize:500)
        //content type detection is performed above, when we validate the path of the file
        mimeType(nullable: true)
}