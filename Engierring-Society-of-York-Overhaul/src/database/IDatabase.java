package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import model.User;

//i pulled everything out of here that i couldn't migrate over to derby. 
//What was written is in the text document in this package

public interface IDatabase {
	
	//user methods 
	List<User> getAccountInfo(final String name);
	List<User> matchUsernameWithPassword(final String name);
	List<User> addUserToDatabase(final String name, final String pswd, final String email, final String type, final String first,
			final String last);
	List<User> findAllUsers();
	List<User> changePassword(final String name, final String pswd, final String newPassword);
	List<User> DeleteUserFromDatabase(final String name, final String pswd);
	
}
