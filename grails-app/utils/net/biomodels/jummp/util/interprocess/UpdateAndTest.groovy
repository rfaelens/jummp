package net.biomodels.jummp.util.interprocess

/* Convenience class to allow the multi-process test components to have the same
** model update and verification mechanism. */ 

class UpdateAndTest implements Runnable {
        File myDir
        File modelFolder
        def manager
        boolean allTestsPass=false
        
        public void setModel(File model) {
        	modelFolder=model
        }
        
        public void init(File exchange, int counter) {
        	Random random=new Random()
        	counter+=random.nextInt(100)
        	myDir=new File(exchange, ""+counter)
        	myDir.mkdirs()
        }
        
        public void setManager(def m) {
        	manager=m
        }
        
        
        public UpdateAndTest() {
        }
        
        public UpdateAndTest(def m, File model, File exchange, int counter) {
            init(exchange, counter)
            setModel(model)
            setManager(m)
        }
        
        boolean dotestsPass() {
        	return allTestsPass
        }
        
        void run() {
            // loops a fixed number of times, updating a file with a random number
            // and saving it in the model, then retrieving the model and verifying
            // the file is correct. Sleeps after each iteration to allow competing
            // processes access to the repository lock.
            File importFile=new File(myDir, "importTest.txt")
            boolean testResult=true
            for (int i=0; i<200; i++) {
                String testText=""+(Math.random()*System.currentTimeMillis())
                importFile.setText(testText)
                String rev=manager.updateModel(modelFolder, [importFile], "Update by ${myDir.getName()} iteration ${i}")
                List<File> files=manager.retrieveModel(modelFolder, rev);
                testResult = testResult & testFileCorrectness(files, "importTest.txt", testText)
                Thread.sleep(80)
            }
            allTestsPass=testResult
        }

        boolean testFileCorrectness(List<File> files, String filename, String filetext)
        {
        	boolean testResult=true
        	int fileIndex=-1
        	files.eachWithIndex { file, i -> 
        		if (file.name == filename) fileIndex=i
        	};
        	testResult= testResult & fileIndex>-1
        	if (!testResult) {
        		return testResult
        	}
        	List<String> lines = files.get(fileIndex).readLines()
        	testResult = testResult & lines.size()==1
        	if (!testResult) {
        		return testResult
        	}
        	testResult = testResult & filetext==lines[0]
        	return testResult
        }
}
    

