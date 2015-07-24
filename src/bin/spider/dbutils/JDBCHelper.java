package bin.spider.dbutils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.KeyedHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

public class JDBCHelper {

	/*1、SqlNullCheckedResultSet ：
	 * 该类是用来对sql语句执行完成之后的的数值进行null的替换；
	 * 2、StringTrimmedResultSet ：
	 * 去除ResultSet中中字段的左右空格；
	 */
	private final static QueryRunner run = new QueryRunner();

	public static Connection getConn() throws SQLException {
		return BonecpConnPool.getInstance().getConnection();
	}
	
	/**
	 * 执行INSERT/UPDATE/DELETE语句
	 * @param sql
	 * @param params
	 * @return boolean - true成功,false失败
	 */
	public static boolean update(String sql, Object... params) {
		boolean returnSts = false;
		Connection conn = null;
		try{
			conn = getConn();
			if(conn != null){
				if(run.update(conn, sql, params) > 0){
					returnSts = true;
			}
		}
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnSts;
	}
	
	/**
	 * 执行INSERT/UPDATE/DELETE语句，带事务
	 * @param sql
	 * @param params
	 * @return boolean - true成功,false失败
	 */
	public static boolean updateWithBatch(String sql, Object[][] params) {
		boolean returnSts = false;
		Connection conn = null;
		try{
			conn = getConn();
			conn.setAutoCommit(false);
			if(conn != null){
				run.batch(conn, sql, params);
				conn.commit();
				returnSts = true;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnSts;
	}
	
	@SuppressWarnings("serial")
	private final static List<Class<?>> PrimitiveClasses = new ArrayList<Class<?>>(){{
		add(Long.class);
		add(Integer.class);
		add(String.class);
		add(BigDecimal.class);
	}};
	
	private final static boolean _IsPrimitive(Class<?> cls) {
		return cls.isPrimitive() || PrimitiveClasses.contains(cls) ;
	}
	
	/**
	 * ArrayHandler,将ResultSet中第一行的数据转化成对象数组
	 * @param sql
	 * @param params
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] readObjectArray(String sql, Object...params) {
		T[] ObjArray = null;
		Connection conn = null;
		try{
			conn = getConn();
			ObjArray = (T[]) run.query(conn, sql, new ArrayHandler(), params);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return ObjArray;
	}
	
	/**
	 * ArrayListHandler,将ResultSet中所有的数据转化成List，List中存放的是Object[]
	 * @param sql
	 * @param params
	 * @return List<T>
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> readObjectArrayList(String sql, Object...params) {
		List<T> ObjList = null;
		Connection conn = null;
		try{
			conn = getConn();
			ObjList = (List<T>) run.query(conn, sql, new ArrayListHandler(), params);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return ObjList;
	}
	
	/**
	 * BeanHandler,将ResultSet中第一行的数据转化成类对象；
	 * @param sql
	 * @param beanClass
	 * @param params
	 * @return T
	 */
	public static <T> T readClass(String sql,Class<T> beanClass, Object... params) {
		if(!_IsPrimitive(beanClass)){
			return null;
		}
		T returnObj = null;
		Connection conn = null;
		try{
			conn = getConn();
			returnObj = (T) run.query(sql, new BeanHandler<T>(beanClass), params);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnObj;		
	}
	
	/**
	 * BeanListHandler,将ResultSet中所有的数据转化成List，List中存放的是类对象
	 * @param sql
	 * @param beanClass
	 * @param params
	 * @return T
	 */
	public static <T> List<T> readClassList(String sql,Class<T> beanClass, Object... params) {
		if(!_IsPrimitive(beanClass)){
			return null;
		}
		List<T> returnObj = null;
		Connection conn = null;
		try{
			conn = getConn();
			returnObj = (List<T>) run.query(conn,sql, new BeanListHandler<T>(beanClass), params);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnObj;	
	}
	
	/**
	 * ColumnListHandler,将ResultSet中某一列的数据存成List，List中存放的是Object对象
	 * @param sql
	 * @param beanClass
	 * @param params
	 * @return T
	 */
	public static <T> List<T> readColumnList(String sql,Integer index, Object... params) {
		List<T> returnObj = null;
		Connection conn = null;
		try{
			conn = getConn();
			returnObj = (List<T>) run.query(conn,sql, new ColumnListHandler<T>(index), params);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnObj;	
	}
	
	/**
	 * KeyedHandler,将ResultSet中存成映射，key为某一列对应为Map。Map中存放的是数据
	 * @param sql
	 * @param beanClass
	 * @param params
	 * @return T
	 */
	public static Map <Object,Map<String,Object>> readKeyed(String sql,String key,Object... params) {
		Map <Object,Map<String,Object>> returnObj = null;
		Connection conn = null;
		try{
			conn = getConn();
			returnObj = (Map <Object,Map<String,Object>>) 
					run.query(conn,sql, new KeyedHandler<Object>(key), params);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnObj;	
	}
	
	/**
	 * MapHandler,将ResultSet中第一行的数据存成Map映射
	* @param sql
	 * @param params
	 * @return
	 */
	public static Map<String,Object> readMap(String sql, Object...params) {
		Map<String,Object> returnMap = new HashMap<String,Object>();
		Connection conn = null;
		try{
			conn = getConn();
			returnMap = run.query(conn, sql,new MapHandler(), params);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnMap;
	}
	
	/**
	 * MapListHandler,将ResultSet中所有的数据存成List。List中存放的是Map
	 * @param sql
	 * @param params
	 * @return
	 */
	public static List<Map<String,Object>> readMapList(String sql, Object...params) {
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		Connection conn = null;
		try{
			conn = getConn();
			returnList = run.query(conn, sql,new MapListHandler(), params);			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return returnList;
	}
	
	/**
	 * ScalarHandler,将ResultSet中一条记录的其中某一列的数据存成Object
	 * @param sql
	 * @param index
	 * @param params
	 * @return T
	 */
	public static <T> T readRowObject(String sql,Integer index,Object...params){
		Connection conn = null;
		T returnObj = null;
		try{
			conn = getConn();//获取新增记录的自增主键 
			returnObj = (T)run.query(conn, sql, new ScalarHandler<T>(index),params);
         } catch (SQLException e) { 
             e.printStackTrace(); 
         } finally { 
        	 DbUtils.closeQuietly(conn);
         }
		return returnObj;
	}
	
}
