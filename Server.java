import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

//mpos   server  tool

 /*
javac -encoding UTF-8 Server.java
java Server
 */
 /*
 stroe*************************************
 */
public class Server extends ServerSocket {
	public static final int SERVERPORT = 8896;

	public Server() throws Exception{
		super(SERVERPORT);
	}

	public void load() throws Exception{
		while(true){
			//System.out.println("Server: Connecting...");
			Socket socket = this.accept();
			System.out.println("Server: Connected");
			new Thread(new Task(socket)).start();
		}
	}

   	class Task implements Runnable{
		private Socket socket;
		private long deltaTime;

		public Task(Socket socket){
			this.socket = socket;
		}
		@Override
	   	public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String str = in.readLine();
				System.out.println("Server: Received: '" + str + "'");

				if(str.substring(0,"TIME:".length()).equals("TIME:")){
					int phoneTime = Integer.parseInt(str.substring("TIME:".length(),str.length()));
					System.out.println("phoneTime: " + phoneTime);
			
					long computerTime = getTodayMS();
					System.out.println("computerTime: " + computerTime);
						
					deltaTime = phoneTime - computerTime;
					System.out.println("deltaTime: " + deltaTime+"ms");
					//out.flush();
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
					out.println("deltaTime: " +deltaTime+"ms");								
					out.flush();
				}
				else {
					try{
						Thread.currentThread().sleep(1000);//毫秒
					}catch(Exception e){
						e.printStackTrace();
					}	
							
					// 发送给客户端的消息 
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
					out.println("sent to android message is:" +str);
					out.flush();
				}
			} catch (Exception e) {
				System.out.println("Server: Error");
				e.printStackTrace();
			} finally {
				try {
                    socket.close();
                } catch (Exception e) {}
				System.out.println("Server: Done.");
			}
		}
	}
   
   	private long getTodayMS(){
	    Calendar calendar = Calendar.getInstance();
		int h = calendar.get(Calendar.HOUR);
		int m = calendar.get(Calendar.MINUTE);
		int s = calendar.get(Calendar.SECOND);
		int ms = calendar.get(Calendar.MILLISECOND);
		long res = ((h*60+m)*60+s)*1000+ms;
		return res;
	}

	/**
	 * 入口
	 */
	public static void main(String a[]) {
		try{
			Server server = new Server();
			server.load();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}