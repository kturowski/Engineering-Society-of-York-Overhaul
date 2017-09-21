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
	

}
