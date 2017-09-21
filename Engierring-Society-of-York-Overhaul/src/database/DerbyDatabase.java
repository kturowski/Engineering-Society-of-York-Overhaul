package database;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBUtil;
import database.DerbyDatabase;
import database.IDatabase;
import database.PersistenceException;
import model.User;




public class DerbyDatabase implements IDatabase {
	static {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (Exception e) {
			throw new IllegalStateException("Could not load Derby driver");
		}
	}
	
	private interface Transaction<ResultType> {
		public ResultType execute(Connection conn) throws SQLException;
	}
	
	private static final int MAX_ATTEMPTS = 100;
	
	
	//Get user account Information
	@Override
	public List<User> getAccountInfo(final String name) {

		return executeTransaction(new Transaction<List<User>>() {
			@Override
			public List<User> execute(Connection conn) throws SQLException {
				PreparedStatement stmt = null;
				ResultSet resultSet = null;

				try{
					stmt = conn.prepareStatement(
							" select * from users " +
							" where user_userName = ? "
							);
						
					stmt.setString(1, name);
					resultSet = stmt.executeQuery();

					//if anything is found, return it in a list format
					List<User> result = new ArrayList<User>();
					Boolean found = false;
					while (resultSet.next()) {
						found = true;

						User u = new User();
						loadUser(u, resultSet, 1);
						result.add(u);
					}

					//Check if the user was found 
					if (!found) {
						System.out.println("<" + name + "> was not found in the Users table");
					}

					return result;


				} finally {
					DBUtil.closeQuietly(resultSet);
					DBUtil.closeQuietly(stmt);
				}
			}
		});
	}
	
	//Match user with password, for the purposes of logging in and authentication
		@Override
		public List<User> matchUsernameWithPassword(final String name) {

			return executeTransaction(new Transaction<List<User>>() {
				@Override
				public List<User> execute(Connection conn) throws SQLException {
					PreparedStatement stmt = null;
					ResultSet resultSet = null;


					try {
						stmt = conn.prepareStatement(
								" select * from users " +
										//until we get the forgein key issue cleared up this is out due to not working
										//I want to get the servlets working for the milestone
										//" select * from users " +
										//" where users.user_id = user_positions.user_id " +
										" where user_userName = ? "
								);

						stmt.setString(1, name);
						List<User> result = new ArrayList<User>();
						resultSet = stmt.executeQuery();

						//if anything is found, return it in a list format
						Boolean found = false;

						while (resultSet.next()) {
							found = true;

							User u = new User();
							loadUser(u, resultSet, 1);
							result.add(u);
						}

						// check if the title was found
						if (!found) {
							System.out.println("<" + name + "> was not found in the Users table");
						}

						return result;


					} finally {
						DBUtil.closeQuietly(resultSet);
						DBUtil.closeQuietly(stmt);
					}
				}
			});
		}
		
		//Add a new user to the system 
		@Override
		public List<User> addUserToDatabase(final String name, final String pswd, final String email, final String type, final String first,
				final String last) {
			return executeTransaction(new Transaction<List<User>>() {
				int user_Id = -1;
				@Override
				public List<User> execute(Connection conn) throws SQLException {
					PreparedStatement stmt = null;
					PreparedStatement stmt2 = null;
					ResultSet resultSet = null;

					//to save employee number


					//Don't need to edit this method to work with the junction
					try {
						System.out.println("prepareStatement addUser");
						stmt = conn.prepareStatement(
								" insert into users(user_userName, user_passWord, user_email, user_accountType, user_firstName, user_lastName) " +
										" values(?, ?, ?, ?, ?, ?) "
								);
						stmt.setString(1, name);
						stmt.setString(2, pswd);
						stmt.setString(3, email);
						stmt.setString(4, type);
						stmt.setString(5, first);
						stmt.setString(6, last);
						System.out.println("execure addUser");
						stmt.executeUpdate();

						stmt2 = conn.prepareStatement(
								" select * " +
										" from users " +
										" where user_userName = ?"
								);
						stmt2.setString(1, name);

						resultSet = stmt2.executeQuery();

						//if anything is found, return it in a list format
						Boolean found = false;
						List<User> result = new ArrayList<User>();

						/*//This doesn't seem to be working using example from lab 6 by D.Hake					
						while (resultSet.next()) {
							found = true;
							User u = new User();
							loadUser(u, resultSet, 1);
							result.add(u);
						}
						 */
						if (resultSet.next())
						{
							user_Id = resultSet.getInt(1);
							System.out.println("New User <" + name + "> ID: " + user_Id);						
						}
						else	// really should throw an exception here 
						{
							System.out.println("New user <" + name + "> not found in Users table (ID: " + user_Id);
						}
						// check if the title was found
						if (!found) {
							System.out.println("<" + name + "> was not found in the users table");
						}

						return result;


					} finally {
						DBUtil.closeQuietly(resultSet);
						DBUtil.closeQuietly(stmt);
						DBUtil.closeQuietly(stmt2);
					}
				}
			});
		}
		
		//find all users
		//this is going to be an Admin only function
		//this method needs re tooling 
		@Override
		public List<User> findAllUsers() {
			return executeTransaction(new Transaction<List<User>>(){
				//@Override
				public List<User>execute(Connection conn)throws SQLException{
					PreparedStatement stmt = null;
					ResultSet resultSet = null;

					try{
						stmt = conn.prepareStatement(
								" select * from users "
								);
						resultSet = stmt.executeQuery();
						//if anything is found, return it in a list format


						List<User> result = new ArrayList<User>();

						while(resultSet.next()) {

							User u = new User();
							loadUser(u, resultSet, 1);
							result.add(u);
						}
						return result;
					}
					finally {
						DBUtil.closeQuietly(conn);
						DBUtil.closeQuietly(stmt);
						DBUtil.closeQuietly(resultSet);
					}

				}

			});


		}
	
		//this one may be tricky to work out 
		@Override
		public List<User> DeleteUserFromDatabase(final String name, final String pswd) {
			return executeTransaction(new Transaction<List<User>>() {
				@Override
				public List<User> execute(Connection conn) throws SQLException {
					PreparedStatement stmt = null;
					PreparedStatement stmt2 = null; 
					PreparedStatement stmt3 = null;


					ResultSet resultSet = null;

					try {


						stmt = conn.prepareStatement(
								" select users.* " +
										" from users, user_positions " +
										" where users.user_id = user_positions.user_id " +
										" and users_userName = ? " +
										" and users_passWord = ? "
								);
						stmt.setString(1, name);
						stmt.setString(2, pswd);
						resultSet = stmt.executeQuery();


						//Using the remove book by title as a guide here
						List<User> Users = new ArrayList<User>();



						while (resultSet.next()) {
							User u = new User();
							loadUser(u, resultSet, 1);
							Users.add(u);
						}

						// check if the title was found
						if (Users.size() == 0) {
							System.out.println("<" + name + "> was not found: users list is empty");
						}

						stmt2 = conn.prepareStatement(
								" delete from user_positions " +
										"where user_id = ? "
								);
						stmt2.setInt(1, Users.get(0).getUserID());
						stmt2.executeUpdate();

						System.out.println("Deleting the user from the junction table");

						stmt3 = conn.prepareStatement(
								" delete from users " +
										" where users.userName = ? " +
										" and users.passWord = ? "
								);

						stmt3.setString(1, name);
						stmt3.setString(2, pswd);
						stmt3.executeUpdate();



						return Users;


					} finally {
						DBUtil.closeQuietly(resultSet);
						DBUtil.closeQuietly(stmt);
						DBUtil.closeQuietly(stmt2);
					}
				}
			});
		}

		@Override
		public List<User> changePassword(final String name, final String pswd, final String newPassword) {
			return executeTransaction(new Transaction<List<User>>() {
				@Override
				public List<User> execute(Connection conn) throws SQLException {
					System.out.println(name);
					System.out.println(pswd);
					System.out.println(newPassword);
					PreparedStatement stmt = null;
					PreparedStatement stmt2 = null;

					ResultSet resultSet2 = null;

					try {

						System.out.println("about to change PW");
						stmt = conn.prepareStatement(
								" update users " +
										" set user_passWord = ? " +
										" where user_userName = ? "+ 
										" and user_passWord = ? "
								);

						stmt.setString(1, newPassword);
						stmt.setString(2, name);
						stmt.setString(3, pswd);
						stmt.executeUpdate();
						System.out.printf("Querry Completed: Update user's password");

						// return all users and see that the one entered was deleted

						stmt2 = conn.prepareStatement(
								" select * from users " 	+
										" where user_userName = ? " +
										" and user_password = ? "
								);
						//ensure new userName is in database
						stmt2.setString(1, name);
						stmt2.setString(2, newPassword);

						resultSet2 = stmt2.executeQuery();


						//if anything is found, return it in a list format
						List<User> result = new ArrayList<User>();
						Boolean found = false;

						while (resultSet2.next()) {
							found = true;

							User u = new User();
							loadUser(u, resultSet2, 1);
							result.add(u);
						}

						// check if the title was found
						if (!found) {
							System.out.println("<" + name + "> was not in users list");
						}

						return result;


					} finally {

						DBUtil.closeQuietly(resultSet2);
						DBUtil.closeQuietly(stmt);
						DBUtil.closeQuietly(stmt2);
					}
				}
			});
		}
	//changePassword
	
	public<ResultType> ResultType executeTransaction(Transaction<ResultType> txn) {
		try {
			return doExecuteTransaction(txn);
		} catch (SQLException e) {
			throw new PersistenceException("Transaction failed", e);
		}
	}

	public<ResultType> ResultType doExecuteTransaction(Transaction<ResultType> txn) throws SQLException {
		Connection conn = connect();

		try {
			int numAttempts = 0;
			boolean success = false;
			ResultType result = null;

			while (!success && numAttempts < MAX_ATTEMPTS) {
				try {
					result = txn.execute(conn);
					conn.commit();
					success = true;
				} catch (SQLException e) {
					if (e.getSQLState() != null && e.getSQLState().equals("41000")) {
						// Deadlock: retry (unless max retry count has been reached)
						numAttempts++;
					} else {
						// Some other kind of SQLException
						throw e;
					}
				}
			}

			if (!success) {
				throw new SQLException("Transaction failed (too many retries)");
			}

			// Success!
			return result;
		} finally {
			DBUtil.closeQuietly(conn);
		}
	}

	private Connection connect() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:derby:test.db;create=true");

		// Set autocommit to false to allow multiple the execution of
		// multiple queries/statements as part of the same transaction.
		conn.setAutoCommit(false);

		return conn;
	}
	
	//these build the collections to return to the servlets, controlles
		private void loadUser(User user, ResultSet resultSet, int index) throws SQLException {
			user.setUsername(resultSet.getString(index++));
			user.setPassword(resultSet.getString(index++));
			user.setEmail(resultSet.getString(index++));
			user.setFirstname(resultSet.getString(index++));
			user.setLastname(resultSet.getString(index++));
			user.setAsMember();//(resultSet.getString(index++));
		}

	public void createTables() {
		executeTransaction(new Transaction<Boolean>() {
			@Override
			public Boolean execute(Connection conn) throws SQLException {
				PreparedStatement stmt1 = null;
				
				try {
					//create the user table 
				}
				finally {
					DBUtil.closeQuietly(stmt1);
				}
				return null;
			}
			
		});
	}	
	
	public void loadInitialData() {
		
	}
	public static void main(String[] args) throws IOException {
		System.out.println("Creating tables...");
		DerbyDatabase db = new DerbyDatabase();
	}	
}