package au.edu.unimelb.tcp.client;

public class State {

	private String identity;
	private String roomId;
	private String password;
	
	public State(String identity, String password, String roomId) {
		this.identity = identity;
		this.roomId = roomId;
    this.password = password;
		
	}
	
	public synchronized String getRoomId() {
		return roomId;
	}
	public synchronized void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	public String getIdentity() {
		return identity;
	}

  public String getPassword() {
    return password;
  }
	
	
}
