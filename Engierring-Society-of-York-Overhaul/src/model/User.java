package model;

public class User {

	private String username = null;
	private String password = null;
	private int loginId = -1;
	private String firstname = null;
	private String lastname = null;
	private String email = null;
	private boolean membership = false; 
	private int userID; 
	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public User(){
		
	}
	
	public User(String user, String pass, int id, String fisrtname, String lastname, String email, boolean membership){
		this.username = user;
		this.password = pass;
		this.loginId = id;
		this.firstname = firstname;
		this.setLastname(lastname); 
		this.email = email;
		this.membership = membership;
		
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public int getLoginId(){
		return this.loginId;
	}
	
	public String getName(){
		return this.firstname;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public boolean isMember(){
		return this.membership;
	}
	
	
	
	public void setUsername(String user){
		this.username = user;
	}
	
	public void setPassword(String pass){
		this.password = pass;
	}
	
	public void setLoginId(int id){
		this.loginId = id;
	}
	
	public void setFirstname(String firstname){
		this.firstname = firstname;
	}
	
	public void setEmail(String email){
			this.email = email;
	}
	
	public void setAsMember() {
		this.membership = true;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

}
