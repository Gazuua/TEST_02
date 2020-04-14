
public class InventoryCode {
	private String inv_code;
	private String inv_code_nm;

	public InventoryCode(String code, String codename) {
		this.inv_code = code;
		this.inv_code_nm = codename;
	}

	public String getInv_code() {
		return inv_code;
	}

	public void setInv_code(String inv_code) {
		this.inv_code = inv_code;
	}

	public String getInv_code_nm() {
		return inv_code_nm;
	}

	public void setInv_code_nm(String inv_code_nm) {
		this.inv_code_nm = inv_code_nm;
	}

}
