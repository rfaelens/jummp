/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.biomodels.jummp.util.interprocess
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Small utility class for coordinating execution between two processes. Allows
 * for 'sendMessage' and 'waitForMessage' type interactions. 
 * @author raza
 */
class InterJummpSync {
    boolean client=true
    int port
    //Data structures for buffering messages
    Set<String> messagesToSend = Collections.synchronizedSet(new HashSet<String>())
    BlockingQueue<String> messageBuffer = new LinkedBlockingQueue<String>()
    Set<String> messagesToCommunicator = Collections.synchronizedSet(new HashSet<String>())
    Set<String> messagesRecieved=new HashSet<String>()
    boolean isActive=true
        
    
    class Communicator implements Runnable {
        
        InterJummpSync owner
        
        Communicator(InterJummpSync owner) {
            this.owner=owner
        }
        
        void run() {
            // Establish the connection according to whether you are client or server
            // and enter the messageLoop
            if (client) {
               Socket socket=new Socket("localhost", port) 
               messageLoop(socket)
            }
            else {
                ServerSocket server = new ServerSocket(port)
                server.accept { socket ->
                    messageLoop(socket)
                }
            }
            
        }
        
        void messageLoop(def socket) {
            //
            socket.withStreams { input, output ->
                BufferedInputStream reader = new BufferedInputStream(input)
                StringBuilder buffer=new StringBuilder();
                while (true) {
                	// send all the messages currently buffered
                        messagesToSend.each {
                            output<<it+"\n"
                            output.flush()
                        }
                        messagesToSend.clear()
                    try {
                    	    // recieve messages available without blocking
                    	    // would have been nice to use the built in
                    	    // reader.readLine, but its behaviour was rather hairy.
                    	    // when it came to non-blocking.
                            while (reader.available()>0) {
                                char readme=(char) reader.read()
                                if (readme=='\n') {
                                    String msg=buffer.toString()
                                    messageBuffer.put(msg)
                                    buffer=new StringBuilder();
                                }
                                else {
                                    buffer.append(readme)
                                }
                            }
                    }
                    catch(SocketTimeoutException ignore) {
                        Thread.sleep(10000)
                    }
                    catch(Exception e) {
                        e.printStackTrace()
                        System.exit(0)
                    }
                    // Check if the owner wants to end things
                    if (messagesToCommunicator.contains("terminate")) {
                            break
                    }
                    // Be nice and relinquish CPU
                    Thread.sleep(100)
                }
               isActive=false;
            }
        }
    }
    
    public InterJummpSync() {
        
    }
    
    public void start(boolean client, int port) {
        this.client=client
        this.port=port
        new Thread(new Communicator(this)).start()
    }
    
    public void sendMessage(String msg) {
        messagesToSend.add(msg)
    }
    
    public void waitForMessage(String msg) {
        // Keep looping and taking from the (blocking) messageBuffer
        // and adding the latest message to the messagesRecieved
        // collection, until it contains what we are looking for
    	while (isActive) {
    	    String recv=messageBuffer.poll(500, TimeUnit.MILLISECONDS)
    	    if (recv==msg) {
    	    	    break
    	    }
    	    messagesRecieved.add(recv)
        }
    }
    
    public String waitForMessageContaining(String msg) {
        // Keep looping and taking from the (blocking) messageBuffer
        // and adding the latest message to the messagesRecieved
        // collection, until it contains what we are looking for
    	while (isActive) {
    	    String recv=messageBuffer.poll(500, TimeUnit.MILLISECONDS)
    	    if (recv && recv.contains(msg)) {
    	    	    return recv;
    	    }
    	    else if (recv) {
    	    	    System.out.println("WAITING FOR: "+msg+" GOT "+recv)
    	    }
    	    messagesRecieved.add(recv)
        }
        return null;
    }
    
    public Set<String> getMessages() {
    	// return both the recieved and buffered messages
    	Set<String> totalMessages=new HashSet<String>()
    	messagesRecieved.each {
    		totalMessages.add(it)
    	}
    	messageBuffer.each {
    		totalMessages.add(it)
    	}
        return totalMessages
    }
    
    public void terminate() {
            messagesToCommunicator.add("terminate")
    }
    
    
        
}

