package model;

public class User {

	private String username = null;
	private String password = null;
	private int loginId = -1;
	private String name = null;
	private String email = null;
	private boolean membership = false; 
	
	public User(){
		
	}
	
	public User(String user, String pass, int id, String name, String email, boolean membership){
		this.username = user;
		this.password = pass;
		this.loginId = id;
		this.name = name;
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
		return this.name;
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
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setEmail(String email){
			this.email = email;
	}
	
	public void setAsMember() {
		this.membership = true;
	}

}
