package test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import bin.spider.bean.FINISH;
import bin.spider.dbutils.JDBCHelper;

public class JDBCHelperTest {
	
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testGetConn() {
		Connection conn = null;
		try {
			conn = JDBCHelper.getConn();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (null!=conn) {
			System.out.println("Connection DB succeed");
		}
	}

	@Test
	public void testUpdate() {
		String sql = "INSERT INTO TOCRAWL (geturl) VALUES(?)";
		String p1 = "www.netnews.com.cn";
		System.out.println(JDBCHelper.update(sql, p1));
	}

	@Test
	public void testUpdateWithBatch() {
		String sql = "INSERT INTO TOCRAWL (geturl) VALUES(?)";
		String[][] params = {{"www.163news.com.cn"},{"www.jdnews.com"}};
		//If parameters aren't need, pass an empty array.
		//存在BUG，传入空的数组时，不会报错，但最后没有成功Insert
		boolean bool = JDBCHelper.updateWithBatch(sql, params);
		System.out.println(bool);
	}

	@Test
	public void testReadObjectArray() {
		String sql = "select * from TOCRAWL where id = '2'";
//		String[] params = {"2"};
		Object[] object = JDBCHelper.readObjectArray(sql, null);
		for (Object o: object) {
			System.out.println(o);
		}
	}

	@Test
	public void testReadObjectList() {
		String sql = "select * from TOCRAWL";
		List<Object[]> list = JDBCHelper.readObjectArrayList(sql, null);
		for (Object[] obArray: list) {
			for (Object object : obArray) {
				System.out.println(object);
			}
		}
	}

	@Test
	public void testReadClass() {
		String sql = "select * from FINISH";
		System.out.println(JDBCHelper.readClass(sql, FINISH.class,""));
	}

	@Test
	public void testReadClassList() {
		String sql = "select * from FINISH";
		System.out.println((FINISH)JDBCHelper.readClassList(sql, FINISH.class,null));
	}

	@Test
	public void testReadColumnList() {
		String sql = "select * from TOCRAWL where id=?";
		Integer id = 1;
		System.out.println(JDBCHelper.readColumnList(sql,2,id));
	}

	@Test
	public void testReadKeyed() {
		String sql = "select * from FINISH where id=?";
		Integer id = 4;
		System.out.println(JDBCHelper.readKeyed(sql, "visited", id));
	}

	@Test
	public void testRead() {
		String sql = "select * from FINISH where id=?";
		Integer id = 4;
		System.out.println(JDBCHelper.readMap(sql, id));
	}

	@Test
	public void testQuery() {
		String sql = "select * from FINISH where id=?";
		Integer id = 4;
		System.out.println(JDBCHelper.readMapList(sql, id));
	}

	@Test
	public void testReadRow() {
		String sql = "select * from FINISH where id=?";
		Integer id = 4;
		System.out.println(JDBCHelper.readRowObject(sql, 2, id));
	}

}
