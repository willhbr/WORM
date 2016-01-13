package abstractation;

import junit.framework.TestCase;

import java.sql.SQLException;

import jdbcimpl.JDBCConnection;
import related.SampleRow;
import related.TableWithKey;

/**
 * Created by will on 13/01/16.
 */
public class ConnectionTest extends TestCase {

  private Connection con;

  public void setUp() throws Exception {
    con = new JDBCConnection();
    Log.logAt(Log.Level.VERBOSE);
  }

  public void testOpen() throws Exception {
    con.open();
    int count = new Query(con).select("1").count();
    assertEquals(count, 1);
  }

  public void testClose() throws Exception {
    con.open();
    int count = new Query(con).select("1").count();
    assertEquals(count, 1);
    con.close();
    try {
      new Query(con).select("1").count();
      assertTrue(false);
    } catch(SQLException sqle) {
      assertTrue(true);
    } catch(NullPointerException sqle) {
      assertTrue(true);
    }

  }

  public void testIsClosed() throws Exception {
    con.open();
    assertFalse(con.isClosed());
    assertTrue(con.close());
    assertTrue(con.isClosed());
  }

  public void testGlobalize() throws Exception {
    con.open().globalize();
    int count = new Query().select("1").count();
    assertEquals(count, 1);
  }

  public void testGetGlobal() throws Exception {
    con.open().globalize();
    assertEquals(con, Connection.getGlobal());
  }

  public void testSave() throws Exception {
    con.loadTables(SampleRow.class);
    con.open();
    con.create(SampleRow.class);
    SampleRow st = new SampleRow();
    con.save(st);
    assertEquals(st.rowid, 1);
    int c = new Query(con).from(SampleRow.class).count();
    assertEquals(c, 1);
    con.save(st);
    assertEquals(st.rowid, 1);
    c = new Query(con).from(SampleRow.class).count();
    assertEquals(c, 1);
  }

  public void testInsert() throws Exception {
    con.open();
    con.loadTables(SampleRow.class);
    con.create(SampleRow.class);
    SampleRow sr = new SampleRow();
    con.insert(sr);
    int c = new Query(con).from(SampleRow.class).count();
    assertEquals(c, 1);
    con.insert(sr);
    c = new Query(con).from(SampleRow.class).count();
    assertEquals(c, 2);
  }

  public void testUpdate() throws Exception {
    con.open();
    con.loadTables(SampleRow.class, TableWithKey.class);
    con.create(SampleRow.class);
    con.create(TableWithKey.class);
    SampleRow sr = new SampleRow();
    sr.theString = "String";
    con.insert(sr);
    int c = new Query(con).from(SampleRow.class).count();
    String str = (String) new Query(con).from(SampleRow.class).scalar("thestring");
    assertEquals(c, 1);
    assertEquals(str, "String");
    sr.theString = "new string";
    con.update(sr);
    c = new Query(con).from(SampleRow.class).count();
    str = (String) new Query(con).from(SampleRow.class).scalar("thestring");
    assertEquals(c, 1);
    assertEquals(str, "new string");
    // With keys instead
    TableWithKey twk = new TableWithKey();
    twk.theKey = "the key";
    twk.theNumber = 5;
    con.insert(twk);
    c = new Query(con).from(TableWithKey.class).count();
    int n = (Integer) new Query(con).from(TableWithKey.class).scalar("thenumber");
    assertEquals(c, 1);
    assertEquals(n, 5);
    twk.theNumber = 987;
    con.update(twk);
    c = new Query(con).from(TableWithKey.class).count();
    n = (Integer) new Query(con).from(TableWithKey.class).scalar("thenumber");
    assertEquals(c, 1);
    assertEquals(n, 987);
  }

  public void testCreate() throws Exception {
    con.open();
    con.loadTables(SampleRow.class);
    con.create(SampleRow.class);
    int c = new Query(con).from("sqlite_master").where("type=?", "table").count();
    assertEquals(c, 1);
    String sql = (String) new Query(con).from("sqlite_master").where("type=?", "table").scalar("sql");
    assertEquals(sql + ";", con.getTable(SampleRow.class).createStatement());
  }

  public void testGetTable() throws Exception {
    con.open();
    con.loadTables(SampleRow.class);
    assertEquals("samplerow", con.getTable(SampleRow.class).name);
    try{
      con.getTable(TableWithKey.class);
    } catch(SQLException sqle) {
      assertEquals("Table isn't a table", sqle.getMessage());
    }
  }
}