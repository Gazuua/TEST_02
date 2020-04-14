
public class InventoryInfo {
	private String inv_code;
	private int max_row;
	private int max_col;
	private int size;

	public InventoryInfo(String code, int maxrow, int maxcol, int size) {
		this.inv_code = code;
		this.max_row = maxrow;
		this.max_col = maxcol;
		this.size = size;
	}

	public String getInv_code() {
		return inv_code;
	}

	public void setInv_code(String inv_code) {
		this.inv_code = inv_code;
	}

	public int getMax_row() {
		return max_row;
	}

	public void setMax_row(int max_row) {
		this.max_row = max_row;
	}

	public int getMax_col() {
		return max_col;
	}

	public void setMax_col(int max_col) {
		this.max_col = max_col;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
