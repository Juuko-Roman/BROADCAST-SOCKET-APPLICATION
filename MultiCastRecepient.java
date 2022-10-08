import java.util.*;
import java.net.*;
public class MultiCastRecepient {
	public static ArrayList<String> groupNames=new ArrayList<String>();
	public static ArrayList<String> groupIp=new ArrayList<String>();
	public static ArrayList<String> TimeIntervals=new ArrayList<String>();	
    public static HashMap<String,String> groupDetails= new HashMap<String,String>();
    public static HashMap<String,String> groupTimeIntervals= new HashMap<String,String>();

	@SuppressWarnings("deprecation")
	public static void main(String[] args)throws Exception {
		DatagramSocket socket=new DatagramSocket();
		
		String group = null;
		byte[] messageBytes=null;
		String receivedMessage=null;
		Scanner scanner=new Scanner(System.in);
		String clientName=null;
		String[] receivedMessageArray;
		
		System.out.println("\t\t*********WELCOME CLIENT*******");
		System.out.print("\tEnter your name: ");
		clientName = scanner.nextLine();

		System.out.println("\n\tConnecting to server. please wait...");

		//sending the server first message so that Server identifies the sender's ip address and port number
		socket.send(packet("CLIENT",clientName));

		Thread.sleep(2*1000);
		//Receiving groups from server
		messageBytes = new byte[1024];
		DatagramPacket packet = new DatagramPacket(messageBytes,messageBytes.length);
		for (int k=0;k<3 ;k++ ) {
			socket.receive(packet);

			receivedMessage = new String(messageBytes,0,packet.getLength(),"UTF-8");
			receivedMessageArray=receivedMessage.split("\\s");

			if(receivedMessageArray[0].equalsIgnoreCase("Error")){
				System.out.println("\n\tServer Response: NO GROUPS CREATED YET. please try again later. \n\tEXITING...");
				Thread.sleep(2*1000);
				System.exit(1);
			}

			//initialising the group names array list
			if(k==0){
				for (String i : receivedMessageArray) {
	 			 groupNames.add(i);
				}
				continue;
			}else if(k==1){
				//initialising the group Ip array list			
				for (String i : receivedMessageArray) {
		 			 groupIp.add(i);
				}
			}else{	
				//initialising the group Time Interval array list			
				for (String i : receivedMessageArray) {
		 			 TimeIntervals.add(i);
				}
			}						
		}

		//creating a hashmaps for group ips and message time intervals 
		for (int m=0;m<groupNames.size() ;m++ ) {
			groupDetails.put(groupNames.get(m),groupIp.get(m));
			groupTimeIntervals.put(groupNames.get(m),TimeIntervals.get(m));			
		}

		System.out.println("\n\tChoose one group from the list below");

		//Generating menu of groups got from server
		for (String i : groupDetails.keySet()) {
		  System.out.println("\t+ "+i);
		}
		System.out.print("\n\tChoice: ");
		group=scanner.nextLine();

		//creating socket for receiving multi cast messages
		MulticastSocket Msocket = new MulticastSocket(3000);
		Msocket.joinGroup(InetAddress.getByName(groupDetails.get(group)));

		while(true){
			Msocket.receive(packet);
			receivedMessage = new String(messageBytes,0,packet.getLength(),"UTF-8");
			if(receivedMessage.equalsIgnoreCase("exit")) {
				break;
			}
			System.out.println("\tThese are the messages from \""+group+"\" group\n");
			System.out.println("\tNB: These messages are received after "+groupTimeIntervals.get(group)+" seconds");
			System.out.println("\tSuper client: " + receivedMessage);
		}
		//closing multi cast connection
		Msocket.leaveGroup(InetAddress.getByName(groupDetails.get(group)));
		Msocket.close();

	}
		//method generates packets to be sent to server
		public static DatagramPacket packet(String keyword, String message) throws UnknownHostException{
		String messg=keyword+" "+message;
		byte[] messgByte=messg.getBytes();
		return new DatagramPacket(messgByte,messgByte.length,InetAddress.getLocalHost(),2000);
		
	}
}	