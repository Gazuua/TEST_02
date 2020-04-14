public class Inventory {

	private DBManager dbManager = DBManager.getInstance();

	private Item[][] inventoryMap;
	private int maxRow;
	private int maxCol;
	private String code;
	private String nickName;
	private int size;

	// ĳ���Ͱ� ���� ������ �� �κ��丮 ������ DB���� �� �޾ƿͼ� �����Ѵ�
	public Inventory(int row, int col, String code, String nickName) {
		this.maxRow = row;
		this.maxCol = col;
		this.code = code;
		this.nickName = nickName;
		this.size = 0;

		this.inventoryMap = new Item[maxRow][maxCol];
		for (int i = 0; i < maxRow; i++)
			this.inventoryMap[i] = new Item[maxCol];
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				this.inventoryMap[i][j] = null;
			}
		}
	}

	// �������� ���� �� ���Ǵ� �޼����, ���� �������� ������ �����Ѵ�.
	public int insertItem(Item item) {
		int ret = 0;
		// ������â�� �� �� ��� �� ���� ������ ó���Ѵ�.
		if (isFull()) {
			return ret;
		}

		// �̹� ������â�� �� �������� �ִ��� Ȯ���Ѵ�.
		Point position = findItemPosition(item);

		// position���� null�̸� ���� �������� ���ٴ� ��
		if (position == null) {
			// �� ĭ�� ã�´�.
			Point insertPosition = findEmptyPosition();
			// ������â�� ���� �� �͵� �ƴѵ� �� ĭ�� ������ �����̹Ƿ� �����Ѵ�.
			if (insertPosition == null) {
				return ret;
			}

			// ���� ������ ���� ĭ ���� �� �ִ� ������(���, �ƹ�Ÿ ��)�� ���
			if (item.getDupFlag()) {
				// �� �ڸ��� �ϳ� ã�Ƽ� ����
				ret = 1;
				inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;

				// DB������ ȹ�� ó���Ͽ� �ش�.
				try {
					dbManager.insertItem(item, insertPosition, this);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ����� �ø���.
				this.size++;
			} else { // ���� ������ �� ĭ�� ���� ���� ���� ������(�Ҹ�ǰ, ��� ��)�� ���
				// �������� ������ ���� �������� ������ üũ
				if (item.getCnt() >= item.getLimitCnt()) {
					// �ִ� ���������� ���� ������ ó���Ѵ�
					ret = item.getCnt() - item.getLimitCnt();
					item.setCnt(item.getLimitCnt());
					inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;
				} else { // �ִ� �������ٴ� ���� ���
					ret = item.getCnt();
					inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;
				}

				// DB���� ȹ�� ó���Ͽ� �ش�.
				try {
					dbManager.insertItem(item, insertPosition, this);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ����� �ø���.
				this.size++;
			}

		} else { // �̹� �κ��丮�� �ش� �������� �����ϴ� ���
			// �� ĭ�� ã�´�.
			Point insertPosition = findEmptyPosition();
			// ������â�� ���� �� �͵� �ƴѵ� �� ĭ�� ������ �����̹Ƿ� �����Ѵ�.
			if (insertPosition == null) {
				return ret;
			}

			// ���� ������ ���� ĭ ���� �� �ִ� ������(���, �ƹ�Ÿ ��)�� ���
			if (item.getDupFlag()) {
				ret = 1;
				inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;

				// DB������ ȹ�� ó���Ͽ� �ش�.
				try {
					dbManager.insertItem(item, insertPosition, this);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ����� �ø���.
				this.size++;
			} else { // ���� ������ �� ĭ�� ���� ���� ���� ������(�Ҹ�ǰ, ��� ��)�� ���
				Item dupItem = inventoryMap[position.getX()][position.getY()];
				int sum = dupItem.getCnt() + item.getCnt();

				// �������� ������ �̹� �ִ����� ���� �κ��丮 �ȿ� ���� �� �ִ� �ִ� �������� ���� ���
				if (sum >= item.getLimitCnt()) {
					// �ִ� ���������� ���� ������ ó���Ѵ�
					ret = dupItem.getLimitCnt() - dupItem.getCnt();
					dupItem.setCnt(item.getLimitCnt());
					inventoryMap[position.getX()][position.getY()] = dupItem;

					// DB���� ������ ������Ʈ ó���Ͽ� �ش�.
					try {
						dbManager.updateItem(item, dupItem.getLimitCnt(), this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else { // ���ĵ� �ִ� �������ٴ� ���� ���
					ret = sum - dupItem.getCnt();
					dupItem.setCnt(sum);
					inventoryMap[position.getX()][position.getY()] = dupItem;

					// DB���� ������ ������Ʈ ó���Ͽ� �ش�.
					try {
						dbManager.updateItem(item, sum, this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return ret;
	}

	// �������� ���� �� ���Ǵ� �޼����, ���� �������� ������ �����Ѵ�.
	public int deleteItem(Point position) {
		int ret = 0;
		// �κ��丮�� ����ִµ� ���� �� �����Ƿ� 0�� �����Ѵ�.
		if (isEmpty()) {
			return ret;
		}

		// ���� �������� ���� ���� �����Ƿ� 0�� �����Ѵ�.
		if (position == null) {
			return ret;
		} else { // �κ��丮�� �ش� �������� �����ϴ� ���
			// ��ٷ� ���� ó���Ͽ� �ش�.
			inventoryMap[position.getX()][position.getY()] = null;
			ret = 1;

			// DB���� delete ó���Ͽ� �ش�.
			try {
				dbManager.deleteItem(position, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// ����� ���δ�.
		this.size--;
		return ret;
	}

	// �������� ���� �� ���Ǵ� �޼����ε�, �Ҹ�ǰó�� �� ĭ�� ���� ���� �������� ���� �� ���� �޼����̴�. ���� �������� ������ �����Ѵ�.
	public int deleteItem(Point position, int cnt) {
		int ret = 0;
		// �κ��丮�� ����ִµ� ���� �� �����Ƿ� 0�� �����Ѵ�.
		if (isEmpty()) {
			return ret;
		}

		// ���� �������� ���� ���� �����Ƿ� 0�� �����Ѵ�.
		if (position == null) {
			return ret;
		} else { // �κ��丮�� �ش� �������� �����ϴ� ���
			Item inItem = inventoryMap[position.getX()][position.getY()];
			int dif = inItem.getCnt() - cnt;

			// ���� ������ �̹� �ִ� �������� ���� ���
			if (dif <= 0) {
				// �� ������.
				ret = inItem.getCnt();
				inventoryMap[position.getX()][position.getY()] = null;

				// ����� ���δ�.
				this.size--;
				// DB���� delete ó���Ͽ� �ش�.
				try {
					dbManager.deleteItem(position, this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else { // �Ϻκи� ���� ���
				// �Ϻκи� ���� ó���Ͽ� �ش�.
				inItem.setCnt(dif);
				inventoryMap[position.getX()][position.getY()] = inItem;
				ret = cnt;

				// DB���� ������ ������Ʈ ó���Ͽ� �ش�.
				try {
					dbManager.updateItem(position, dif, this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return ret;
	}

	// �巡���ؼ� ������ ��ġ�� �ٲٴ� �޼����̴�.
	public int changeItemPosition(Point p1, Point p2) {
		// ���ϴ� �������� max������ ũ�� �ȵǰ� ó���Ѵ�.
		if (p1.getX() >= this.maxRow || p1.getY() >= this.maxCol || p2.getX() >= this.maxRow
				|| p2.getY() >= this.maxCol) {
			return 0;
		}

		Item i1 = inventoryMap[p1.getX()][p1.getY()];
		Item i2 = inventoryMap[p2.getX()][p2.getY()];

		// �ϴ� null�̰� ���� ��ġ���� �ٲ��ش�.
		inventoryMap[p1.getX()][p1.getY()] = i2;
		inventoryMap[p2.getX()][p2.getY()] = i1;

		int result = 0;

		// ��ó�� - ��� ��ġ�� �������� ������ ���
		if (i2 == null) {
			// DB���� ���� �ִ� ������ ��ġ�� ������Ʈ�� �ش�.
			try {
				result = dbManager.updateItemPosition(i1, p1, p2, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// DB���� ���� �ִ� ������ ��ġ��, �ٲ� ������ ��ġ ��� ������Ʈ�� �ش�.
			try {
				result = dbManager.changeItemPosition(p1, p2, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public boolean isEmpty() {
		if (this.size == 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isFull() {
		if (this.size == maxRow * maxCol) {
			return true;
		} else {
			return false;
		}
	}

	public void extendInventory() {
		// ������ �ǾƷ� ��ĭ�� �ø���.
		this.maxRow++;

		// ū�� �ϳ� �����.
		Item[][] newInventoryMap = new Item[maxRow][maxCol];
		for (int i = 0; i < maxRow; i++)
			newInventoryMap[i] = new Item[maxCol];
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				newInventoryMap[i][j] = null;
			}
		}

		// �̰��Ѵ�.
		for (int i = 0; i < this.inventoryMap.length; i++) {
			for (int j = 0; j < this.inventoryMap[i].length; j++) {
				newInventoryMap[i][j] = this.inventoryMap[i][j];
			}
		}

		// ������.
		this.inventoryMap = newInventoryMap;
	}

	public void reduceInventory() {
		// ������ �ǾƷ� ��ĭ�� ���ش�.
		this.maxRow -= 1;

		// ������ �ϳ� ����� �̰��Ѵ�.
		Item[][] newInventoryMap = new Item[maxRow][maxCol];
		for (int i = 0; i < maxRow; i++)
			newInventoryMap[i] = new Item[maxCol];
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				newInventoryMap[i][j] = this.inventoryMap[i][j];
			}
		}

		// ������.
		this.inventoryMap = newInventoryMap;
	}

	// ã�� �������� �κ��丮�� �� ��° ��ġ�� �ִ��� �˷��ִ� �޼���
	public Point findItemPosition(Item item) {
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				Item nowItem = inventoryMap[i][j];

				if (nowItem == null)
					continue;

				if (item.getName().equals(nowItem.getName())) {
					return new Point(i, j);
				}
			}
		}

		return null;
	}

	// �������� ���� ��, ù ��° �� �ڸ��� ��� ��ġ���� �˷��ִ� �޼���
	private Point findEmptyPosition() {
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				Item nowItem = inventoryMap[i][j];
				if (nowItem == null) {
					return new Point(i, j);
				}
			}
		}

		return null;
	}

	// ���� �κ��丮 ���� �ִ� ��� �������� ����Ͽ� �����ش�.
	public void printAll() {
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				Item item = inventoryMap[i][j];
				if (item != null) {
					System.out.println("(" + i + ", " + j + ")" + item.getName() + " " + item.getCnt() + "��");
				}
			}
		}
	}

	public Item[][] getInventoryMap() {
		return inventoryMap;
	}

	public void setInventoryMap(Item[][] inventoryMap) {
		this.inventoryMap = inventoryMap;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getMaxRow() {
		return maxRow;
	}

	public void setMaxRow(int maxRow) {
		this.maxRow = maxRow;
	}

	public int getMaxCol() {
		return maxCol;
	}

	public void setMaxCol(int maxCol) {
		this.maxCol = maxCol;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

}
