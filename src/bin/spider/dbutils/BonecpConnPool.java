package bin.spider.dbutils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class BonecpConnPool {

	/** database driver class name */
	private static String SQL_DRIVER = "";
	/** database URL associated with the URL */
	private static String SQL_URL = "";
	/** user name of the database */
	private static String SQL_USERNAME = "";
	/** password for the current user */
	private static String SQL_PASSWORD = "";
	
	private static BoneCP connectionPool = null;  

	private static class PoolHolder {  
		private static final BonecpConnPool INSTANCE = new BonecpConnPool();
		static{
			// 加载驱动
			try {
				Class.forName(SQL_DRIVER);
				//设置连接池配置信息  
		        BoneCPConfig config = new BoneCPConfig();  
		        //数据库的JDBC URL  
		        config.setJdbcUrl(SQL_URL);   
		        //数据库用户名  
		        config.setUsername(SQL_USERNAME);
		        //数据库用户密码  
		        config.setPassword(SQL_PASSWORD);  
		        //数据库连接池每个分区中的最小连接数  
		        config.setMinConnectionsPerPartition(10);  
		        //数据库连接池每个分区中的最大连接数  
		        config.setMaxConnectionsPerPartition(50); 
		        // 只设置1个分区 
		        config.setPartitionCount(1);  
				// 连接释放处理
		        //config.setReleaseHelperThreads(3);
				// 当连接池中的连接耗尽的时候 BoneCP一次同时获取的连接数
		        // 设置连接空闲时间(分钟)
		        config.setIdleMaxAgeInMinutes(5);
		        // 每60秒检查所有连接池中的空闲连接
		        config.setIdleConnectionTestPeriodInSeconds(60);
		        config.setAcquireIncrement(10);
		        //设置数据库连接池  
				connectionPool = new BoneCP(config);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static final BonecpConnPool getInstance() {  
		return PoolHolder.INSTANCE;
	}
	
	/**
	 * 该静态块用来获得属性文件中的driver,url,username,password
	 */
	static {
		Properties prop = new Properties();
		InputStream inStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("jdbc.properties");
		try {
			// 获得相应的键值对
			prop.load(inStream);
			// 根据相应的键获得对应的值
			SQL_DRIVER = prop.getProperty("driver");
			SQL_URL = prop.getProperty("url");
			SQL_USERNAME = prop.getProperty("username");
			SQL_PASSWORD = prop.getProperty("password");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BonecpConnPool() {
	}

	// 从连接池获取连接
	public Connection getConnection() throws SQLException {
		return connectionPool.getConnection();
	}
	
	public void shutDown() throws SQLException {
		connectionPool.shutdown();
	}
	
	
}
