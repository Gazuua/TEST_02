
public class Item {
	// �κ��丮�� ������ �ִ� ������ ���ؼ��� �����Ͽ����ϴ�.
	private String name; // ������ �̸�
	private int cnt; // ������ ����
	private String invCode; // ������ ����(���, �Ҹ�ǰ ��)
	private int limitCnt; // �� ĭ�� �� �� �ִ� �ִ� ����
	private boolean dupFlag; // �� ĭ �̻� ���� �� �ִ� ���������� ����

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
