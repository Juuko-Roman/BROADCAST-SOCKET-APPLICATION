import java.net.*;
import java.util.*;

public class SuperClient{
	public static void main(String[] args) throws Exception {
		DatagramSocket socket=new DatagramSocket();
		Scanner scanner= new Scanner(System.in);
		int choice=0;
		
		String groupName=null;
		String message=null;
		byte[] serverMsg=new byte[1024];
		DatagramPacket packetReceived=new DatagramPacket(serverMsg,serverMsg.length);
		String groupStatus=null;
		int timeInterval;
		String messageInterval=null;
		
		execution: while(true){

		//this is the list of options for super client	
		menu();

		try{
			choice=Integer.parseInt(scanner.nextLine());
		}catch(NumberFormatException e){
			System.out.println("\tPlease enter a numerical value");
			continue;
		}			
			switch(choice){
				case 1:
					System.out.println("\n\t------CREATING NEW GROUP-------");
					System.out.print("\tEnter the name of the new group: ");
					groupName=scanner.nextLine();
					System.out.print("\n\tEnter time after which messages should be sent (in seconds eg 5)\n\tTime: ");
					try{
						timeInterval=Integer.parseInt(scanner.nextLine());
					}catch(NumberFormatException e){
						System.out.println("\tPlease enter a numerical value");
						break;
					}					
					socket.send(packet("CREATE",groupName+" "+timeInterval));
					System.out.println("\n\tCreating group please wait...");
					Thread.sleep(2*1000);
					System.out.println("\tGroup \""+groupName+"\" created successfully");
					break;
				case 2:
					System.out.println("\n\t------SENDING TO A GROUP-------");
					System.out.print("\tEnter the name of the group to send to: ");
					groupName=scanner.nextLine();

					System.out.println("\n\tServer is searching for this group. Wait a moment...");
					Thread.sleep(2*1000);
					//super client is sending group name to the server to check whether it exists
					socket.send(packet(groupName," "));

					//here server sends a response telling super ient about the status of the group ie whether it exists or not
					socket.receive(packetReceived);
					groupStatus=new String(serverMsg,0,packetReceived.getLength(),"UTF-8");

					//an error occurs if no groups are found created on the server
					if(groupStatus.equalsIgnoreCase("Error")){
						System.out.println("\n\t"+groupName+" group does not exist. \n\tHINT: First Create it Using the menu below");
						break;
					}	

					//here we receive the time interval for the group we are sending to
					socket.receive(packetReceived);
					messageInterval=new String(serverMsg,0,packetReceived.getLength(),"UTF-8");

					System.out.println("\n\tSERVER RESPONSE: success group \""+groupName+"\" found");
					System.out.println("\t++Messages will be sent to \""+groupName+"\" group"+" after "+messageInterval+" "+"seconds");
					System.out.println("\n\tPreparing Typing area... please Wait...");
					Thread.sleep(3*1000);
					System.out.println("\n\tStart Typing and press Enter to send");
					System.out.println("\t\tNB:To stop sending, type stop\n");

					//with thius loop, the super client continues to send messages to the server to be multi casted to other clients
					while(true){
						System.out.print("\tMessage: ");
						message=scanner.nextLine();
						if(message.equalsIgnoreCase("stop")){
							break;
						}
						else if(message.equalsIgnoreCase("exit")){
							System.out.println("\tEXITING... BYE...");
							Thread.sleep(3*1000);
							socket.send(packet(groupName,"EXIT"));
							break execution;
						}
						else{
							//here super client sends a message to be multi casted by the servewr to other clients
							socket.send(packet(groupName,message));
						}
					}
					break;
				case 3:
					System.out.println("\n\t------DELETING GROUP-------");
					System.out.print("\tEnter the name of the group to delete: ");
					groupName=scanner.nextLine();
					socket.send(packet("DELETE",groupName));
					System.out.println("\n\tWait for a moment. Deleting...");
					Thread.sleep(3*1000);
					
					socket.receive(packetReceived);
					String deleteStatus=new String(serverMsg,0,packetReceived.getLength(),"UTF-8");
					if(deleteStatus.equalsIgnoreCase("Error")){
						System.out.println("\t"+deleteStatus+": No groups found. please first create atleast one group");
					}else{
					System.out.println("\t\""+groupName+"\" group deleted successfully...");}
					break;
				case 4:
					System.out.println("\tEXITING... BYE...");
					Thread.sleep(3*1000);
					socket.send(packet(groupName,"EXIT"));
					break execution;
				default:
					System.out.println("\tInvalid Choice\n");
					break;
			}
		}
		
		
	}
	
	//this method generates a menu for the super  client
	public static void menu(){
		System.out.println("\n\t\t********MENU*********");
		System.out.println("\tChoose one of the following options");
		System.out.println("\t1. Create Group");
		System.out.println("\t2. Send to a group");
		System.out.println("\t3. Delete Group");
		System.out.println("\t4. Exit");
		System.out.print("\n\tChoice: ");
	}

    //this method creates packets to be sent to server
	public static DatagramPacket packet(String keyword, String message) throws UnknownHostException{
		String messg=keyword+" "+message;
		byte[] messgByte=messg.getBytes();
		return new DatagramPacket(messgByte,messgByte.length,InetAddress.getLocalHost(),2000);
		
	}
}