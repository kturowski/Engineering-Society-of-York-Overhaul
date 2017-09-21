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
	
	//add user
	//remove user 
	//changePassword
	//findallUsers
	//matchusernamewithpassword
	
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