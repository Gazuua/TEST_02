public class Inventory {

	private DBManager dbManager = DBManager.getInstance();

	private Item[][] inventoryMap;
	private int maxRow;
	private int maxCol;
	private String code;
	private String nickName;
	private int size;

	// 캐릭터가 최초 접속할 때 인벤토리 정보를 DB에서 싹 받아와서 셋팅한다
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

	// 아이템을 먹을 때 사용되는 메서드로, 먹은 아이템의 갯수를 리턴한다.
	public int insertItem(Item item) {
		int ret = 0;
		// 아이템창이 꽉 찬 경우 못 먹은 것으로 처리한다.
		if (isFull()) {
			return ret;
		}

		// 이미 아이템창에 그 아이템이 있는지 확인한다.
		Point position = findItemPosition(item);

		// position값이 null이면 같은 아이템이 없다는 뜻
		if (position == null) {
			// 빈 칸을 찾는다.
			Point insertPosition = findEmptyPosition();
			// 아이템창이 가득 찬 것도 아닌데 빈 칸이 없으면 오류이므로 리턴한다.
			if (insertPosition == null) {
				return ret;
			}

			// 같은 종류로 여러 칸 먹을 수 있는 아이템(장비, 아바타 등)일 경우
			if (item.getDupFlag()) {
				// 빈 자리를 하나 찾아서 삽입
				ret = 1;
				inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;

				// DB에서도 획득 처리하여 준다.
				try {
					dbManager.insertItem(item, insertPosition, this);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 사이즈를 늘린다.
				this.size++;
			} else { // 같은 종류당 한 칸에 여러 개가 들어가는 아이템(소모품, 재료 등)일 경우
				// 먹으려는 개수가 제한 개수보다 많은지 체크
				if (item.getCnt() >= item.getLimitCnt()) {
					// 최대 개수까지만 먹은 것으로 처리한다
					ret = item.getCnt() - item.getLimitCnt();
					item.setCnt(item.getLimitCnt());
					inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;
				} else { // 최대 개수보다는 적을 경우
					ret = item.getCnt();
					inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;
				}

				// DB에서 획득 처리하여 준다.
				try {
					dbManager.insertItem(item, insertPosition, this);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 사이즈를 늘린다.
				this.size++;
			}

		} else { // 이미 인벤토리에 해당 아이템이 존재하는 경우
			// 빈 칸을 찾는다.
			Point insertPosition = findEmptyPosition();
			// 아이템창이 가득 찬 것도 아닌데 빈 칸이 없으면 오류이므로 리턴한다.
			if (insertPosition == null) {
				return ret;
			}

			// 같은 종류로 여러 칸 먹을 수 있는 아이템(장비, 아바타 등)일 경우
			if (item.getDupFlag()) {
				ret = 1;
				inventoryMap[insertPosition.getX()][insertPosition.getY()] = item;

				// DB에서도 획득 처리하여 준다.
				try {
					dbManager.insertItem(item, insertPosition, this);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 사이즈를 늘린다.
				this.size++;
			} else { // 같은 종류당 한 칸에 여러 개가 들어가는 아이템(소모품, 재료 등)일 경우
				Item dupItem = inventoryMap[position.getX()][position.getY()];
				int sum = dupItem.getCnt() + item.getCnt();

				// 먹으려는 개수와 이미 있던것의 합이 인벤토리 안에 넣을 수 있는 최대 개수보다 많을 경우
				if (sum >= item.getLimitCnt()) {
					// 최대 개수까지만 먹은 것으로 처리한다
					ret = dupItem.getLimitCnt() - dupItem.getCnt();
					dupItem.setCnt(item.getLimitCnt());
					inventoryMap[position.getX()][position.getY()] = dupItem;

					// DB에서 갯수만 업데이트 처리하여 준다.
					try {
						dbManager.updateItem(item, dupItem.getLimitCnt(), this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else { // 합쳐도 최대 개수보다는 적을 경우
					ret = sum - dupItem.getCnt();
					dupItem.setCnt(sum);
					inventoryMap[position.getX()][position.getY()] = dupItem;

					// DB에서 갯수만 업데이트 처리하여 준다.
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

	// 아이템을 버릴 때 사용되는 메서드로, 버린 아이템의 개수를 리턴한다.
	public int deleteItem(Point position) {
		int ret = 0;
		// 인벤토리가 비어있는데 버릴 수 없으므로 0을 리턴한다.
		if (isEmpty()) {
			return ret;
		}

		// 없는 아이템을 버릴 수는 없으므로 0을 리턴한다.
		if (position == null) {
			return ret;
		} else { // 인벤토리에 해당 아이템이 존재하는 경우
			// 곧바로 버림 처리하여 준다.
			inventoryMap[position.getX()][position.getY()] = null;
			ret = 1;

			// DB에서 delete 처리하여 준다.
			try {
				dbManager.deleteItem(position, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 사이즈를 줄인다.
		this.size--;
		return ret;
	}

	// 아이템을 버릴 때 사용되는 메서드인데, 소모품처럼 한 칸에 여러 개인 아이템을 버릴 때 쓰는 메서드이다. 버린 아이템의 개수를 리턴한다.
	public int deleteItem(Point position, int cnt) {
		int ret = 0;
		// 인벤토리가 비어있는데 버릴 수 없으므로 0을 리턴한다.
		if (isEmpty()) {
			return ret;
		}

		// 없는 아이템을 버릴 수는 없으므로 0을 리턴한다.
		if (position == null) {
			return ret;
		} else { // 인벤토리에 해당 아이템이 존재하는 경우
			Item inItem = inventoryMap[position.getX()][position.getY()];
			int dif = inItem.getCnt() - cnt;

			// 버릴 개수가 이미 있는 개수보다 적은 경우
			if (dif <= 0) {
				// 다 버린다.
				ret = inItem.getCnt();
				inventoryMap[position.getX()][position.getY()] = null;

				// 사이즈를 줄인다.
				this.size--;
				// DB에서 delete 처리하여 준다.
				try {
					dbManager.deleteItem(position, this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else { // 일부분만 버릴 경우
				// 일부분만 버림 처리하여 준다.
				inItem.setCnt(dif);
				inventoryMap[position.getX()][position.getY()] = inItem;
				ret = cnt;

				// DB에서 갯수만 업데이트 처리하여 준다.
				try {
					dbManager.updateItem(position, dif, this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return ret;
	}

	// 드래그해서 아이템 위치를 바꾸는 메서드이다.
	public int changeItemPosition(Point p1, Point p2) {
		// 원하는 포지션이 max값보다 크면 안되게 처리한다.
		if (p1.getX() >= this.maxRow || p1.getY() >= this.maxCol || p2.getX() >= this.maxRow
				|| p2.getY() >= this.maxCol) {
			return 0;
		}

		Item i1 = inventoryMap[p1.getX()][p1.getY()];
		Item i2 = inventoryMap[p2.getX()][p2.getY()];

		// 일단 null이고 뭐고 위치부터 바꿔준다.
		inventoryMap[p1.getX()][p1.getY()] = i2;
		inventoryMap[p2.getX()][p2.getY()] = i1;

		int result = 0;

		// 후처리 - 대상 위치에 아이템이 없었을 경우
		if (i2 == null) {
			// DB에서 원래 있던 아이템 위치만 업데이트해 준다.
			try {
				result = dbManager.updateItemPosition(i1, p1, p2, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// DB에서 원래 있던 아이템 위치와, 바꿀 아이템 위치 모두 업데이트해 준다.
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
		// 가로줄 맨아래 한칸을 늘린다.
		this.maxRow++;

		// 큰거 하나 만든다.
		Item[][] newInventoryMap = new Item[maxRow][maxCol];
		for (int i = 0; i < maxRow; i++)
			newInventoryMap[i] = new Item[maxCol];
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				newInventoryMap[i][j] = null;
			}
		}

		// 이관한다.
		for (int i = 0; i < this.inventoryMap.length; i++) {
			for (int j = 0; j < this.inventoryMap[i].length; j++) {
				newInventoryMap[i][j] = this.inventoryMap[i][j];
			}
		}

		// 덮어씌운다.
		this.inventoryMap = newInventoryMap;
	}

	public void reduceInventory() {
		// 가로줄 맨아래 한칸을 없앤다.
		this.maxRow -= 1;

		// 작은거 하나 만들고 이관한다.
		Item[][] newInventoryMap = new Item[maxRow][maxCol];
		for (int i = 0; i < maxRow; i++)
			newInventoryMap[i] = new Item[maxCol];
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				newInventoryMap[i][j] = this.inventoryMap[i][j];
			}
		}

		// 덮어씌운다.
		this.inventoryMap = newInventoryMap;
	}

	// 찾는 아이템이 인벤토리의 몇 번째 위치에 있는지 알려주는 메서드
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

	// 아이템을 먹을 때, 첫 번째 빈 자리가 어느 위치인지 알려주는 메서드
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

	// 현재 인벤토리 내에 있는 모든 아이템을 출력하여 보여준다.
	public void printAll() {
		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				Item item = inventoryMap[i][j];
				if (item != null) {
					System.out.println("(" + i + ", " + j + ")" + item.getName() + " " + item.getCnt() + "개");
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
