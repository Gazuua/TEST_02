import java.sql.*;
import java.util.*;

public class DBManager {

	// Connection 관련 멤버들
	private Connection conn = null;
	// DB 접속 정보를 수정하고 싶으실 경우 여기서 수정하시면 됩니다.
	private String dbip = "localhost";
	private String dbport = "3306";
	private String dbname = "NEOPLE";
	private String user = "root";
	private String password = "root";

	private String url = "jdbc:mysql://" + dbip + ":" + dbport + "/" + dbname
			+ "?useUnicode=true&characterEncoding=UTF8&verifyServerCertificate=false&useSSL=false&serverTimezone=UTC";
	private boolean isConnected = false;

	// 쿼리 실행을 위한 stmt 인스턴스 준비
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	// Singleton Pattern 구현
	private static class Singleton {
		private static final DBManager instance = new DBManager();
	}

	public static DBManager getInstance() {
		return Singleton.instance;
	}

	// getInstance 메서드를 최초로 호출할 때 생성자가 호출된다.
	protected DBManager() {
		// DB 서버로 연결한다.
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, password);
			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("MYSQL DB 연결에 실패하였습니다.");
		}

		isConnected = true;
		System.out.println("MYSQL DB에 연결되었습니다.");
	}

	public boolean isConnected() {
		return isConnected;
	}

	// DML을 정형화한 메서드 집합들

	// 입력한 닉네임이 DB에 있는지 여부를 알려주는 메서드
	public boolean selectNickName(String nickName) throws Exception {
		boolean ret = false;
		String sql = "SELECT * FROM TB_CHARACTER_INFO WHERE CHAR_NAME = ?";

		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, nickName);
		rs = pstmt.executeQuery();

		System.out.println("[selectNickName] : " + sql);

		while (rs.next()) {
			if (rs.getString("CHAR_NAME").equals(nickName)) {
				ret = true;
			}
		}
		closeConnection();
		return ret;
	}

	public List<InventoryCode> selectInventoryCode() throws Exception {
		connect();

		List<InventoryCode> codelist = new ArrayList<InventoryCode>();
		String sql = "SELECT * FROM TB_INVENTORY_CODE";

		pstmt = conn.prepareStatement(sql);
		rs = pstmt.executeQuery();

		System.out.println("[selectInventoryCode] : " + sql);

		while (rs.next()) {
			String code = rs.getString("INV_CODE");
			String codename = rs.getString("INV_CODE_NAME");

			codelist.add(new InventoryCode(code, codename));
		}

		closeConnection();
		return codelist;
	}

	public List<InventoryInfo> selectInventoryInfo(String nickName) throws Exception {
		connect();

		List<InventoryInfo> infolist = new ArrayList<InventoryInfo>();
		String sql = "SELECT * FROM TB_INVENTORY_INFO WHERE CHAR_NAME = ?";

		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, nickName);
		rs = pstmt.executeQuery();

		System.out.println("[selectInventoryInfo] : " + sql);

		while (rs.next()) {
			String code = rs.getString("INV_CODE");
			int maxrow = rs.getInt("INV_MAXROW");
			int maxcol = rs.getInt("INV_MAXCOL");
			int size = rs.getInt("INV_SIZE");

			infolist.add(new InventoryInfo(code, maxrow, maxcol, size));
		}

		closeConnection();
		return infolist;
	}

	public void selectInventory(String nickName, Map<String, Inventory> inventoryMap) throws Exception {
		connect();

		String sql = "SELECT * FROM TB_INVENTORY WHERE CHAR_NAME = ? AND DEL_YN = 'N'";

		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, nickName);
		rs = pstmt.executeQuery();

		System.out.println("[selectInventory] : " + sql);

		while (rs.next()) {
			String code = rs.getString("INV_CODE");
			String itemName = rs.getString("ITEM_NAME");
			boolean itemDupFlag = false;
			if (rs.getString("ITEM_DUPFLAG") == "Y") {
				itemDupFlag = true;
			}
			int maxcnt = rs.getInt("ITEM_MAXCNT");
			int cnt = rs.getInt("ITEM_CNT");
			int x = rs.getInt("INV_ROW");
			int y = rs.getInt("INV_COL");

			Point position = new Point(x, y);
			Item item = new Item(code, itemName, maxcnt, cnt, itemDupFlag);
			Inventory nowInventory = inventoryMap.get(code);
			nowInventory.setSize(nowInventory.getSize() + 1);

			nowInventory.getInventoryMap()[position.getX()][position.getY()] = item;
		}

		closeConnection();
	}

	public void insertItem(Item item, Point position, Inventory inventory) throws Exception {
		connect();

		String sql = "INSERT INTO TB_INVENTORY VALUES(?, ?, NULL, ?, ?, ?, ?, ?, ?, ?)";

		System.out.println("[insertItem] : " + sql);

		pstmt = conn.prepareStatement(sql);

		pstmt.setString(1, inventory.getNickName());
		pstmt.setString(2, inventory.getCode());
		pstmt.setString(3, item.getName());
		if (item.getDupFlag()) {
			pstmt.setString(4, "Y");
		} else {
			pstmt.setString(4, "N");
		}
		pstmt.setInt(5, item.getLimitCnt());
		pstmt.setInt(6, item.getCnt());
		pstmt.setInt(7, position.getX());
		pstmt.setInt(8, position.getY());
		pstmt.setString(9, "N");

		pstmt.executeUpdate();

		closeConnection();
	}

	public void updateItem(Point position, int cnt, Inventory inventory) throws Exception {
		connect();

		String sql = "UPDATE TB_INVENTORY SET ITEM_CNT = ? WHERE CHAR_NAME = ? AND INV_CODE = ? AND INV_ROW = ? AND INV_COL = ?";
		pstmt = conn.prepareStatement(sql);

		System.out.println("[updateItem by position] : " + sql);

		pstmt.setInt(1, cnt);
		pstmt.setString(2, inventory.getNickName());
		pstmt.setString(3, inventory.getCode());
		pstmt.setInt(4, position.getX());
		pstmt.setInt(5, position.getY());

		pstmt.executeUpdate();

		closeConnection();
	}

	public void updateItem(Item item, int cnt, Inventory inventory) throws Exception {
		connect();

		String sql = "UPDATE TB_INVENTORY SET ITEM_CNT = ? WHERE CHAR_NAME = ? AND INV_CODE = ? AND ITEM_NAME = ?";
		pstmt = conn.prepareStatement(sql);

		System.out.println("[updateItem by item] : " + sql);

		pstmt.setInt(1, cnt);
		pstmt.setString(2, inventory.getNickName());
		pstmt.setString(3, inventory.getCode());
		pstmt.setString(4, item.getName());

		pstmt.executeUpdate();

		closeConnection();
	}

	public int updateItemPosition(Item item, Point p1, Point p2, Inventory inventory) throws Exception {
		connect();

		String sql = "UPDATE TB_INVENTORY SET INV_ROW = ?, INV_COL = ? WHERE INV_CODE = ? AND INV_ROW = ? AND INV_COL = ?";
		pstmt = conn.prepareStatement(sql);

		System.out.println("[updateItemPosition] : " + sql);

		pstmt.setInt(1, p2.getX());
		pstmt.setInt(2, p2.getY());
		pstmt.setString(3, inventory.getCode());
		pstmt.setInt(4, p1.getX());
		pstmt.setInt(5, p1.getY());

		int ret = pstmt.executeUpdate();

		closeConnection();

		return ret;
	}

	public void deleteItem(Point position, Inventory inventory) throws Exception {
		connect();

		String sql = "UPDATE TB_INVENTORY SET INV_ROW = -1, INV_COL = -1, DEL_YN = ? WHERE INV_CODE = ? AND INV_ROW = ? AND INV_COL = ?";
		pstmt = conn.prepareStatement(sql);

		System.out.println("[deleteItem] : " + sql);

		pstmt.setString(1, "Y");
		pstmt.setString(2, inventory.getCode());
		pstmt.setInt(3, position.getX());
		pstmt.setInt(4, position.getY());

		pstmt.executeUpdate();

		closeConnection();
	}

	public int changeItemPosition(Point p1, Point p2, Inventory inventory) throws Exception {
		connect();

		int seq1 = 0;
		int seq2 = 0;

		// 1번째 아이템의 ITEM_SEQ 획득
		// ==============================================================
		String selectSql1 = "SELECT ITEM_SEQ FROM TB_INVENTORY WHERE INV_CODE = ? AND INV_ROW = ? AND INV_COL = ?";
		pstmt = conn.prepareStatement(selectSql1);

		pstmt.setString(1, inventory.getCode());
		pstmt.setInt(2, p1.getX());
		pstmt.setInt(3, p1.getY());

		rs = pstmt.executeQuery();

		System.out.println("[changeItemPosition select One] : " + selectSql1);

		while (rs.next()) {
			seq1 = rs.getInt("ITEM_SEQ");
		}

		// 2번째 아이템의 ITEM_SEQ 획득
		// ==============================================================
		String selectSql2 = "SELECT ITEM_SEQ FROM TB_INVENTORY WHERE INV_CODE = ? AND INV_ROW = ? AND INV_COL = ?";
		pstmt = conn.prepareStatement(selectSql2);

		pstmt.setString(1, inventory.getCode());
		pstmt.setInt(2, p2.getX());
		pstmt.setInt(3, p2.getY());

		rs = pstmt.executeQuery();

		System.out.println("[changeItemPosition select One] : " + selectSql2);

		while (rs.next()) {
			seq2 = rs.getInt("ITEM_SEQ");
		}

		// 1번째 아이템 위치 변경
		// ======================================================================
		String sql1 = "UPDATE TB_INVENTORY SET INV_ROW = ?, INV_COL = ? WHERE ITEM_SEQ = ?";
		pstmt = conn.prepareStatement(sql1);

		System.out.println("[changeItemPosition] : " + sql1);

		pstmt.setInt(1, p2.getX());
		pstmt.setInt(2, p2.getY());
		pstmt.setInt(3, seq1);

		int ret = pstmt.executeUpdate();

		// 2번째 아이템 위치 변경
		// ======================================================================
		String sql2 = "UPDATE TB_INVENTORY SET INV_ROW = ?, INV_COL = ? WHERE ITEM_SEQ = ?";
		pstmt = conn.prepareStatement(sql2);

		System.out.println("[changeItemPosition] : " + sql2);

		pstmt.setInt(1, p1.getX());
		pstmt.setInt(2, p1.getY());
		pstmt.setInt(3, seq2);

		ret += pstmt.executeUpdate();

		closeConnection();

		return ret;
	}

	public int extendInventory(String nickName, String invCode) throws Exception {
		connect();

		String sql = "UPDATE TB_INVENTORY_INFO SET INV_MAXROW = INV_MAXROW + 1 WHERE CHAR_NAME = ? AND INV_CODE = ?";
		pstmt = conn.prepareStatement(sql);

		System.out.println("[extendInventory] : " + sql);

		pstmt.setString(1, nickName);
		pstmt.setString(2, invCode);

		int ret = pstmt.executeUpdate();

		closeConnection();

		return ret;
	}

	public int reduceInventory(String nickName, String invCode) throws Exception {
		connect();

		String sql = "UPDATE TB_INVENTORY_INFO SET INV_MAXROW = INV_MAXROW - 1 WHERE CHAR_NAME = ? AND INV_CODE = ?";
		pstmt = conn.prepareStatement(sql);

		System.out.println("[reduceInventory] : " + sql);

		pstmt.setString(1, nickName);
		pstmt.setString(2, invCode);

		int ret = pstmt.executeUpdate();

		closeConnection();

		return ret;
	}

	public void connect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, password);
			conn.setAutoCommit(true);
		} catch (Exception e) {
		}
	}

	public void closeConnection() {
		try {
			if (stmt != null) {
				stmt.close();
			}

			if (pstmt != null) {
				pstmt.close();
			}

			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {

		}
	}
}
