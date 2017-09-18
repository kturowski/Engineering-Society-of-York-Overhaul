package database;

public abstract class DatabaseProvider {
	private static IDatabase databaseInstance;
	
	public static IDatabase getDatabase() {
		if(databaseInstance == null){
			databaseInstance = new IDatabase();
		}
		return databaseInstance;
	}
	
}
