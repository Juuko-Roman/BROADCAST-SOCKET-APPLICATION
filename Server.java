import java.net.*;
import java.util.*;

public class Server{
       public static ArrayList<String> groupNames=new ArrayList<String>();
       public static ArrayList<String> groupIp=new ArrayList<String>();
       public static int ipCounter=0;

       //this hash map contains group names and respective ip addresses
       public static HashMap<String,String> groupDetails= new HashMap<String,String>();

       //this hash map contains group names and respective times after which messages should be received     
       public static HashMap<String,String> groupMessageTimeInterval= new HashMap<String,String>();       
       public static void main(String[] args)throws Exception {

 		DatagramSocket socket = new DatagramSocket (2000);
              MulticastSocket multicast = new MulticastSocket();
              System.out.println("SERVER STARTED SUCCESSFULLY...");
              String multicast_msg=null;
              String receivedMessage=null;
              String[] receivedMessgArray;
              

               
              byte[] clientMsgByte=new byte[1024];

              //this packet receives messages fromboth super client and the other clients
              DatagramPacket clientMsg=new DatagramPacket(clientMsgByte,clientMsgByte.length);
              
              while (true) {
                     //Receiving packets from clients
                     socket.receive(clientMsg);

                     receivedMessage = new String(clientMsgByte,0,clientMsg.getLength(),"UTF-8");

                     //this is an array containing broken down received message from clients
                     receivedMessgArray=receivedMessage.split("\\s");

                     //here we check if received message is from clients other than super client 
                     if(receivedMessgArray[0].equalsIgnoreCase("CLIENT")){

                            //checks whether client entered one name
                            if(receivedMessgArray.length==2){
                                   System.out.println("-new client \""+receivedMessgArray[1]+"\" has connected");
                            }
                            else{
                                   //this executes if client entered two names
                                   System.out.println("-new client \""+receivedMessgArray[1]+" "+receivedMessgArray[2]+"\" has connected");
                            }
                            if(groupDetails.size()==0){
                                   //if no groups are created then error message is sent to client
                                   socket.send(new DatagramPacket("Error".getBytes(),"Error".getBytes().length,clientMsg.getAddress(),clientMsg.getPort()));
                            }
                            else{
                                   //sending details about a group to client
                                   socket.send(createGroupPackets(clientMsg));
                                   socket.send(createGroupIpPackets(clientMsg));
                                   socket.send(createTimeIntervalPacket(clientMsg));
                            }


                            //all these if statements below are about packets received from super client 
                     }
                     else if(receivedMessgArray[0].equalsIgnoreCase("CREATE")){
                            createGroup(receivedMessgArray[1],receivedMessgArray[2]);
                            System.out.println("-new group \""+receivedMessgArray[1]+"\" created on the server");
                            continue;
                     }
                     else if(receivedMessgArray[0].equalsIgnoreCase("DELETE")){
                            if(groupDetails.size()==0){
                                   
                                   socket.send(new DatagramPacket("Error".getBytes(),"Error".getBytes().length,clientMsg.getAddress(),clientMsg.getPort()));
                            }
                            else{
                                   groupDetails.remove(receivedMessgArray[1]);
                                   System.out.println("\"-"+receivedMessgArray[1]+"\" group deleted from server");
                            }      socket.send(new DatagramPacket("OK".getBytes(),"OK".getBytes().length,clientMsg.getAddress(),clientMsg.getPort()));
                     }  
                     else{
                            if(groupDetails.size()==0){
                                   
                                   socket.send(new DatagramPacket("Error".getBytes(),"Error".getBytes().length,clientMsg.getAddress(),clientMsg.getPort()));                                   
                                   continue;
                            }
                            
                            socket.send(new DatagramPacket("OK".getBytes(),"OK".getBytes().length,clientMsg.getAddress(),clientMsg.getPort()));
                            socket.send(new DatagramPacket(groupMessageTimeInterval.get(receivedMessgArray[0]).getBytes(),groupMessageTimeInterval.get(receivedMessgArray[0]).getBytes().length,clientMsg.getAddress(),clientMsg.getPort()));                        
                            socket.receive(clientMsg);
                            receivedMessage = new String(clientMsgByte,0,clientMsg.getLength(),"UTF-8");
                            receivedMessgArray=receivedMessage.split("\\s");    
                            
                            

                            for(int i=1;i<receivedMessgArray.length;i++){
                                   if(i==1) {
                                          multicast_msg=receivedMessgArray[1];
                                          continue;
                                   }
                                   multicast_msg += " "+receivedMessgArray[i];
                            }

                            Thread.sleep(Integer.parseInt(groupMessageTimeInterval.get(receivedMessgArray[0]))*1000);
                            multicast.send(packet(multicast_msg,groupDetails.get(receivedMessgArray[0])));

                            if(receivedMessgArray[1].equalsIgnoreCase("EXIT")){
                                   System.out.println("\nSUPER CLIENT HAS DISCONNECTED.  EXITING...");
                                   socket.close();
                                   multicast.close();
                                   Thread.sleep(2*1000);
                                   break;
                            }
 
                     }                            
  
               }
 	}

       //this method creates packets to be sent to other clients except super client
       public static DatagramPacket packet(String message, String groupIp) throws UnknownHostException{
              byte[] messgByte=message.getBytes();
              return new DatagramPacket(messgByte,messgByte.length,InetAddress.getByName(groupIp),3000);
              
       }

       //this method creates Ips for the respective groups created by the super client
       public static void createGroup(String stringToAppend, String timeInterval){
              groupNames.add(stringToAppend);
              groupIp.add("224.3.4."+ipCounter);
              groupDetails.put(groupNames.get(ipCounter),groupIp.get(ipCounter));
              groupMessageTimeInterval.put(groupNames.get(ipCounter),timeInterval);
              ipCounter++;
       }

       //this method deletes a group from the server as specified by the super client
       public static void deleteGroup(String groupToDelete){
              groupNames.add(groupToDelete);
              groupIp.add("224.3.4."+ipCounter);
              ipCounter++;
       }

       //this method collects all created groups and sends them to clients
       public static DatagramPacket createGroupPackets(DatagramPacket datagramPacket){
           String grps=null;
           int j=0;   
           for (String i : groupDetails.keySet()) {
              if(j==0){grps=i;}
              else grps+=" "+i;
              j++;              
           }
              byte[] grpsByte=grps.getBytes();
              return new DatagramPacket(grpsByte,grpsByte.length,datagramPacket.getAddress(),datagramPacket.getPort());
       }

       //this method collects all created group IPs and sends them to clients       
       public static DatagramPacket createGroupIpPackets(DatagramPacket datagramPacket){
           String grpIp=null;
           int j=0;   
           for (String i : groupDetails.values()) {
              if(j==0){grpIp=i;}
              else grpIp+=" "+i;
              j++;              
           }
              byte[] grpIpByte=grpIp.getBytes();
              return new DatagramPacket(grpIpByte,grpIpByte.length,datagramPacket.getAddress(),datagramPacket.getPort());
       }

       //this method collects all created Time intervals for messages to be sent to respective groups and sends them to clients       
       public static DatagramPacket createTimeIntervalPacket(DatagramPacket datagramPacket){
           String gpInt=null;
           int j=0;   
           for (String i : groupMessageTimeInterval.values()) {
              if(j==0){gpInt=i;}
              else gpInt+=" "+i;
              j++;              
           }
              byte[] gpIntByte=gpInt.getBytes();
              return new DatagramPacket(gpIntByte,gpIntByte.length,datagramPacket.getAddress(),datagramPacket.getPort());
       }       
}