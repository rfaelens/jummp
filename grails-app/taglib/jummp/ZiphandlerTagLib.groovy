package jummp
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.FileVisitResult
import org.apache.commons.io.FilenameUtils
import java.text.DateFormat

class ZiphandlerTagLib {
	static namespace="Ziphandler"
	
	 def loadedZips;
     def zipSupported;
	
	class ZipVisitor extends SimpleFileVisitor<Path> {
		boolean jsOutput=true;
		StringBuilder builder;
		Path zipfile;
		
		public ZipVisitor(boolean js, StringBuilder b, Path zip) {
			jsOutput=js
			builder=b;
			zipfile=zip;
		}
		
		
		
		@Override
		public FileVisitResult visitFile(Path visiting, BasicFileAttributes attrs) throws IOException {
			try
			{
				if (jsOutput) {
					visitFileJS(visiting, attrs);
				}
				return FileVisitResult.CONTINUE;
			}
			catch(Exception e) {
				e.printStackTrace()
			}
		}
	
	}
	
	private void visitFileJS(StringBuilder builder, Path zipfile, Path visiting, BasicFileAttributes attrs) {
			builder.append("fileData[\"")
			builder.append(zipfile.getFileName().toString())
			builder.append(visiting.toString())
			builder.append("\"]=new Object();")
			addFileAttributesJS(builder, zipfile.getFileName().toString()+visiting.toString(), "isInternal", "true", false); 
			addFileAttributesJS(builder, zipfile.getFileName().toString()+visiting.toString(), "Name", FilenameUtils.getName(visiting.toString()), true); 
			addFileAttributesJS(builder, zipfile.getFileName().toString()+visiting.toString(), "Size", "window.readablizeBytes(${attrs.size()});", false); 
			if (attrs.lastModifiedTime()) {
				addFileAttributesJS(builder, zipfile.getFileName().toString()+visiting.toString(), "Last_Modified", "${new Date(attrs.lastModifiedTime().toMillis())}".toString(), true); 
			}
	}
	
	private void addFileAttributesJS(StringBuilder builder, String filename,String prop, String value, boolean quotes) {
		builder.append("fileData[\"").append(filename).
				append("\"].").append(prop).append("=");
		if (quotes) {
			builder.append("'")
		}
		builder.append(value);
		if (quotes) {
			builder.append("'")
		}
		builder.append(";")
	}
	
	private void handleZip(StringBuilder builder, boolean JS, String filePath) {
		 try {
  				Path zipfile = Paths.get(filePath);
		 	 	final URI uri = URI.create("jar:file:" + zipfile.toUri().getPath());
		 	 	final Map<String, String> env = new HashMap<>();
	 			FileSystem fs = null;
	 			if (!loadedZips.containsKey(uri)) {
	 				fs=FileSystems.newFileSystem(uri, env);
	 				loadedZips.put(uri, fs)
	 			}
	 			else {
	 				fs=loadedZips.get(uri)
	 			}
  	 			final Path root = fs.getPath("/");
  	 			Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path visiting, BasicFileAttributes attrs) throws IOException {
                    	if (JS) {
                    		visitFileJS(builder, zipfile, visiting, attrs)
                    		return FileVisitResult.CONTINUE;
                    	}
                    	else {
                    		builder.append('''<li><a><span class="pointerhere">''')
                    		builder.append(zipfile.getFileName().toString())
                    		builder.append(visiting.toString())
                    		builder.append("</span></a></li>");
                    		return FileVisitResult.CONTINUE;
                    	}
                    }
                });
  	 			zipSupported[filePath]=true
  	 	 }
  	 	 catch(Exception e) {
  	 	 	 e.printStackTrace()
  	 	 	 zipSupported[filePath]=false;
  	 	 }
	}
	
	private void processFilesJS(def repFiles, StringBuilder builder) {
		try
        {
        	repFiles.each {
				File file=new File(it.path)
				BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class)
				builder.append("fileData[\"").append(file.name).append("\"]=new Object();");
				addFileAttributesJS(builder, file.name, "Name", FilenameUtils.getName(it.path), true); 
				addFileAttributesJS(builder, file.name, "Size", "window.readablizeBytes(${attr.size()})", false); 
				if (!it.mainFile) {
					addFileAttributesJS(builder, file.name, "Description", it.description, true)
				}
				addFileAttributesJS(builder,file.name,"Created","${new Date(attr.creationTime().toMillis())}".toString(), true) 
				addFileAttributesJS(builder,file.name,"Last_Modified","${new Date(attr.lastModifiedTime().toMillis())}".toString(), true) 
				addFileAttributesJS(builder,file.name,"isInternal","false", false) 
				
				if (it.mimeType.contains('zip')) {
						 handleZip(builder, true, it.path)
				}
     		}
     		builder.append("</script>")
        }
        catch(Exception e) {
        	e.printStackTrace()
        }
	
	}
	
	def outputFileInfoAsJS = { attrs ->
        StringBuilder builder=new StringBuilder('''<script>function readablizeBytes(bytes) {
			var s = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];
			var e = Math.floor(Math.log(bytes) / Math.log(1024));
			return (bytes / Math.pow(1024, e)).toFixed(2) + " " + s[e]; 
		}var fileData=new Array();''')
		if (!attrs.repFiles || attrs.loadedZips==null || attrs.zipSupported==null) {
        	System.out.println("RepFiles"+ attrs.repFiles)	
			System.out.println("loadedZips"+ attrs.loadedZips)	
			System.out.println("zipSupported"+ attrs.zipSupported)	
			return
        }
        loadedZips=attrs.loadedZips;
        zipSupported=attrs.zipSupported;
        processFilesJS(attrs.repFiles, builder)	
		out<<builder.toString()
	}
	
	
	def outputFileInfoAsHtml = { attrs ->
		StringBuilder builder=new StringBuilder()
		try {
			if (!attrs.repFiles || attrs.loadedZips==null || attrs.zipSupported==null || attrs.mainFile==null) {
        		return
        	}
        	loadedZips=attrs.loadedZips;
        	zipSupported=attrs.zipSupported;
        	attrs.repFiles.each {
        		if (attrs.mainFile==it.mainFile) {
        			builder.append('''<li rel="file"><a><span class="pointerhere">''')
        			File f=new File(it.path);
        			builder.append(f.name).append("</a>")
        			if (it.mimeType.contains('zip')) {
        				builder.append("<ul>")
        				handleZip(builder, false, it.path)
        				builder.append("</ul>")
	 			    }
	 			    builder.append("</span></li>")
        		}
        	}
		}
		catch(Exception e) {
			e.printStackTrace()
		}
		out<<builder.toString();
	}
	
	
}
