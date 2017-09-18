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

public interface IDatabase {
	
}

//public class IDatabase {
	static{
		try{
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		}catch(Exception e){
			throw new IllegalStateException("Could not load the driver");
		}
	}

	private static final int TIMEOUT = 10;

	private interface Query<ReturnType>{
		public ReturnType query(Connection conn) throws SQLException;
	}

	/*
	 * ----------------MAIN QUERY METHODS------------------------------------------------------------
	 */
	
	public int queryForLoginIdByUsername(final String username){
		try{
			return doQueryLoop(new Query<Integer>(){
				@Override
				public Integer query(Connection conn) throws SQLException{
					PreparedStatement stmt = null;
					ResultSet set = null;
					int loginId = -1;
					try{
						stmt = conn.prepareStatement(
								" SELECT login_id FROM accounts "
								+ " WHERE username = ?");
						stmt.setString(1, username);
						set = stmt.executeQuery();
						
						if(set.next()){
							loginId = set.getInt(1);
						}
					}finally{
						DBUtil.closeQuietly(stmt);
						DBUtil.closeQuietly(set);
					}
					return loginId;
				}
			});
		}catch(SQLException e){
			System.out.println("queryForLoginIdByUsername: "+e.getMessage());
			return -1;
		}
	}
	
	public boolean updateLoginIdByUsername(final String username, final int loginId){
		try{
			return doQueryLoop(new Query<Boolean>(){
				@Override 
				public Boolean query(Connection conn)throws SQLException{
					boolean success = false;
					PreparedStatement stmt = null;
					
					try{
						stmt = conn.prepareStatement(
								"UPDATE accounts "
								+ " SET login_id = ? "
								+ " WHERE username = ? ");
						stmt.setInt(1, loginId);
						stmt.setString(2, username);
						success = true;
					}finally{
						DBUtil.closeQuietly(stmt);
					}
					return success;
				}
			});
		}catch(SQLException e){
			System.out.println("updateLoginIdByUsername: "+e.getMessage());
			return false;
		}
	}
	
	private boolean insertUserAccount(Connection conn, User account) throws SQLException{
		boolean success = false;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;

		ResultSet set = null;
		
		try{
			stmt1 = conn.prepareStatement(
					"INSERT INTO accounts (username, password, login_id, name, email) "
					+ " VALUES(?,?,?,?,?)");
			stmt1.setString(1, account.getUsername());
			stmt1.setString(2, account.getPassword());
			stmt1.setInt(3, account.getLoginId());
			stmt1.setString(4, account.getName());
			stmt1.setString(5, account.getEmail());
			
			
		
			stmt1.executeUpdate();
			
			stmt2 = conn.prepareStatement(
					"SELECT user_id FROM accounts "
					+ " WHERE username = ?");
			stmt2.setString(1, account.getUsername());
			set = stmt2.executeQuery();
			

		}finally{
			DBUtil.closeQuietly(stmt1);
			DBUtil.closeQuietly(stmt2);
			DBUtil.closeQuietly(set);
		}
		return success;
	}
	
	public boolean insertNewAccountIntoDatabase(final User user){
		try{
			return doQueryLoop(new Query<Boolean>(){
				@Override
				public Boolean query(Connection conn) throws SQLException{
					boolean success = false;
					if(!userAccountExists(conn, user.getUsername())){
						if(insertUserAccount(conn,user));
							success = true;
					}
					return success;
				}
			});
		}catch(SQLException e){
			System.out.println("insertNewAccountIntoDatabase: "+e.getMessage());
			return false;
		}
	}
	
	private boolean userAccountExists(Connection conn, String username) throws SQLException{
		boolean registered = false;
		PreparedStatement stmt = null;
		ResultSet set = null;
		
		try{
			stmt = conn.prepareStatement(
					"SELECT * from accounts WHERE username=? ");
			stmt.setString(1, username);
			set = stmt.executeQuery();
			if(set.next()){
				registered = true;
			}
		}finally{
			DBUtil.closeQuietly(stmt);
			DBUtil.closeQuietly(set);
		}
		return registered;
	}


	/*
	 * -----------------------HELPER METHODS FOR STREAMLINING SQL QUERIES----------------------------------------------------
	 */
	
	private String getUserPassword(Connection conn,String username) throws SQLException{
		String password = null;
		PreparedStatement stmt = null;
		ResultSet set = null;
		try{
			stmt = conn.prepareStatement(
					" SELECT password FROM accounts WHERE username=? ");
			stmt.setString(1,username);
			set = stmt.executeQuery();
			
			if(set.next()){
				password = set.getString(1);
			}
		}finally{
			DBUtil.closeQuietly(stmt);
			DBUtil.closeQuietly(set);
		}
		return password;
	}
	

	/*
	 * ------------------------------------CORE DATABASE FUNCTIONALITY METHODS------------------------------------------------------------
	 */

	private Connection connect() {
		Connection conn = null;
		try{
			conn =  DriverManager.getConnection("jdbc:derby:../database/bookstore.db;create=true");	
			conn.setAutoCommit(false);
		} catch(SQLException e){
			System.out.println(e.getSQLState());
		}
		return conn;
	}

	private<ReturnType> ReturnType doQueryLoop(Query<ReturnType> query) throws SQLException{
		Connection conn = connect();

		ReturnType ret = null;
		int times = 0;
		boolean done = false;
		try{
			while(!done && times < TIMEOUT){
				try{
					ret = query.query(conn);
					conn.commit();
					done = true;
				}catch(SQLException e){
					if (e.getSQLState() != null && e.getSQLState().equals("41000")) {
						times++;
					} else {
						throw e;
					}
				}
			}

			if (!done) {
				throw new SQLException("Query Failed, TIMEOUT. ");
			}
			return ret;
		}finally{
			DBUtil.closeQuietly(conn);
		}
	}

	/*
	 * --------------------------STATIC METHODS FOR MODIFING THE DATABASE OUTSIDE OF THE WEB APP------------------------------------
	 */
	private boolean createTables(Connection conn){
		//Table Names: authors, books, authored, books_for_sale_by_user, accounts
		PreparedStatement stmt1 = null;
		try {
			stmt1 = conn.prepareStatement(
					"CREATE TABLE accounts ("+
							" user_id integer primary key "+
							"     generated always as identity (start with 1, increment by 1), "
							+" username varchar(20), "
							+" password varchar(20), "
							+" login_id integer, "
							+" name varchar(30),"
							+" email varchar(30), "
							+")"	
					);
			stmt1.execute();

			conn.commit();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}finally{
			DBUtil.closeQuietly(stmt1);

		}
		return true;
	}

	private boolean dropTables(Connection conn){
		PreparedStatement stmt1 = null;
		try{		
			stmt1 = conn.prepareStatement("DROP TABLE accounts");		
			stmt1.executeUpdate();					
			conn.commit();
		}catch(SQLException e){
			System.out.println(e.getMessage());
			return false;
		}finally{
			DBUtil.closeQuietly(stmt1);
		}
		return true;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		System.out.println("----Loading Database Driver---- ");
		IDatabase db = DatabaseProvider.getDatabase();

		System.out.println("----Connecting to Database---- ");
		Connection conn = db.connect();

		System.out.println("(C)reate table or (D)rop tables: ");
		Scanner in = new Scanner(System.in);
		
		if(in.nextLine().toUpperCase().equals("C")){
			System.out.println("----Creating Tables---- ");
			if(db.createTables(conn)){
				System.out.println("----Successfully Created Tables---- ");
			}

			else{
				System.out.println("----Failed to Create Tables---- ");
			}
		}
		else{
			System.out.println("----Preparing to Drop Tables---- ");
			if(db.dropTables(conn)){
				System.out.println("----Successfully Dropped Tables---- ");
			}
			else{
				System.out.println("----Failed To Drop Table---- ");
			}
		}
		in.close();
		DBUtil.closeQuietly(conn);
	}
}
