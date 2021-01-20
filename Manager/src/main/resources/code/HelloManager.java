package code;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.OriginateResponseEvent;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.StatusAction;


public class HelloManager implements ManagerEventListener
{
    private ManagerConnection managerConnection;

    public HelloManager() throws IOException, IllegalStateException, AuthenticationFailedException, TimeoutException
    {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                "localhost", "manager", "fas123");

        this.managerConnection = factory.createManagerConnection();
     
     
        managerConnection.login();
     // register for events
        managerConnection.addEventListener(this);
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, ClassNotFoundException, SQLException, InterruptedException
    {
    	String number;
    	ResultSet rs;
    	 Connection con = null;
    	 PreparedStatement stmt = null;
    	 Statement mystatement = null;
		    String url = "jdbc:mysql://localhost:3306/response";
		    String username = "root";
		    String password = "Fas@10";
		    Class.forName("com.mysql.jdbc.Driver");
		      con = DriverManager.getConnection(url, username, password);
		      mystatement = con.createStatement();
        OriginateAction originateAction;
        ManagerResponse originateResponse;
String sql1="SELECT * FROM callinfo";
rs = mystatement.executeQuery(sql1);

while(rs.next())
{
 number= rs.getString("cell_no");
   System.out.println(number);
   
   originateAction = new OriginateAction();
   originateAction.setChannel("SIP/" + number);
   originateAction.setContext("default");
   originateAction.setExten("1300");
   originateAction.setPriority(new Integer(1));
   originateAction.setTimeout(new Integer(30000));
   originateAction.setAsync(true);
   

   
   // connect to Asterisk and log in
 
   
   
   // send the originate action and wait for a maximum of 30 seconds for Asterisk
   // to send a reply
   originateResponse = managerConnection.sendAction(originateAction, 30000);
  
   // print out whether the originate succeeded or not
   
   System.out.println(originateResponse.getResponse());
   

   String userresponse=originateResponse.getResponse();
   
   
   
  if(userresponse.equals("Success"))
  {
	   userresponse="Answered";
  }
  else if(userresponse.equals("Error"))
  {
	   userresponse="Busy";
  }
  
   String sql = "INSERT into receiver(status) VALUES (?)";
   

   stmt = con.prepareStatement(sql);
   
   stmt.setString(1,userresponse);
   
   
   stmt.execute();

   
   // request channel state
   managerConnection.sendAction(new StatusAction());

   // wait 10 seconds for events to come in
   Thread.sleep(10000);
  

   // and finally log off and disconnect
 
}
managerConnection.logoff();
    }


      
    
    

    public static void main(String[] args) throws Exception
    {
        HelloManager helloManager;

        helloManager = new HelloManager();
        helloManager.run();
        
    }

	@Override
	public void onManagerEvent(ManagerEvent event) {
		// just print received events
		System.out.println(event);
	if	(event instanceof OriginateResponseEvent)
		{
			OriginateResponseEvent event1 =  (OriginateResponseEvent) event;
			 String desc = "Unknown error";
				if (event1.getReason() == 1) {
					desc = "Other end has hungup";
				}
				else if (event1.getReason() == 2) {
					desc = "Local Ringing";
				}
				else if (event1.getReason() == 3) {
					desc = "Remote end is ringing";
				}
				else if (event1.getReason() == 4) {
					desc = " Remote end has answered";
				}
				else if (event1.getReason() == 5) {
					desc = "Remote end is busy";
				}
				else if (event1.getReason() == 6) {
					desc = "Make it go off hook";
				}
				else if (event1.getReason() == 7) {
					desc = "Line is off hook";
				}
				else if (event1.getReason() == 8) {
					desc = "Congestion (circuits busy)";
				}
			   
	        System.out.println(desc);
		}
		 
		
}
}