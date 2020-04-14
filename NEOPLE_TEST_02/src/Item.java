
public class Item {
	// 인벤토리와 관련이 있는 정보에 한해서만 설계하였습니다.
	private String name; // 아이템 이름
	private int cnt; // 아이템 개수
	private String invCode; // 아이템 종류(장비, 소모품 등)
	private int limitCnt; // 한 칸에 들어갈 수 있는 최대 개수
	private boolean dupFlag; // 한 칸 이상 먹을 수 있는 아이템인지 여부

	public Item(String invCode, String name, int limitCnt, int cnt, boolean dupFlag) {
		this.invCode = invCode;
		this.name = name;
		this.limitCnt = limitCnt;
		this.cnt = cnt;
		this.dupFlag = dupFlag;
	}

	public String getInvCode() {
		return invCode;
	}

	public void setInvCode(String invCode) {
		this.invCode = invCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public int getLimitCnt() {
		return limitCnt;
	}

	public void setLimitCnt(int limitCnt) {
		this.limitCnt = limitCnt;
	}

	public boolean getDupFlag() {
		return dupFlag;
	}

	public void setDupFlag(boolean dupFlag) {
		this.dupFlag = dupFlag;
	}

}
