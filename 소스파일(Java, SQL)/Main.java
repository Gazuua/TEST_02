import java.util.*;

public class Main {

	// �� ���α׷������� main�޼��忡 �����ϴ� ���� �������� �����մϴ�.
	// ���� �� Scanner�� ���� �α����ϰ��� �ϴ� �г����� �Է¹޽��ϴ�.
	public static void main(String[] args) {

		// DB ���� �õ�
		DBManager dbManager = DBManager.getInstance();
		// DB ���� ���н� exit
		if (!dbManager.isConnected()) {
			System.exit(-1);
		}

		Scanner sc = new Scanner(System.in);
		String nickName = "";

		while (true) {
			// �α��� �ȳ�
			System.out.println("�α����� �г����� �Է��� �ֽʽÿ�.");
			System.out.print("�г��� �Է� : ");

			boolean bLogin = false;
			nickName = sc.next();
			sc.reset();

			// DB���� �г����� ��ȸ�Ͽ� �α����� �õ��Ѵ�
			try {
				bLogin = dbManager.selectNickName(nickName);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("SQL�� ���� �� ������ �߻��Ͽ����ϴ�. ���α׷��� �����մϴ�.");
				System.exit(-1);
			}

			if (!bLogin) {
				System.out.println("�г����� ��ȸ���� �ʾ� �α��ο� �����Ͽ����ϴ�. �ٽ� �Է��� �ּ���.");
				continue;
			} else {
				break;
			}
		}

		// �α��� ���� ��
		System.out.println("\n\n�α��ο� �����ϼ̽��ϴ�. �α����� �г����� [" + nickName + "] �Դϴ�.");
		System.out.println("�����͸� �ε��մϴ�...");

		// TB_INVENTORY_CODE�� ��ȸ�Ͽ� ������ �� �ִ� �κ��丮 ������ �ѷ��ش�.
		List<InventoryCode> invCode = null;
		try {
			invCode = dbManager.selectInventoryCode();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SQL�� ���� �� ������ �߻��Ͽ����ϴ�. ���α׷��� �����մϴ�.");
			System.exit(-1);
		}
		if (invCode == null || invCode.isEmpty()) {
			System.out.println("�κ��丮 �ڵ尪�� �ҷ����� �� ������ �߻��Ͽ����ϴ�. ���α׷��� �����մϴ�.");
			System.exit(-1);
		}

		// TB_INVENTORY_INFO�� ��� ���ؿ´�.
		List<InventoryInfo> invInfo = null;
		try {
			invInfo = dbManager.selectInventoryInfo(nickName);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SQL�� ���� �� ������ �߻��Ͽ����ϴ�. ���α׷��� �����մϴ�.");
			System.exit(-1);
		}
		if (invInfo == null || invInfo.isEmpty()) {
			System.out.println("�κ��丮 �������� �ҷ����� �� ������ �߻��Ͽ����ϴ�. ���α׷��� �����մϴ�.");
			System.exit(-1);
		}

		// invCode�� invInfo ����Ʈ�� ����Ͽ� �κ��丮 ��(�κ��丮 �Ӽ��� ������ ���� ���� Ŭ����)�� �����.
		Map<String, Inventory> inventoryMap = new HashMap<String, Inventory>();
		for (InventoryCode code : invCode) {

			// ��з��� ��� ��ŵ�Ѵ�.
			if (code.getInv_code().length() == 1) {
				continue;
			}

			// �Һз��� ��� ��з����� �ٰ��Ͽ� ������ �����´�
			char c = code.getInv_code().charAt(0);
			int row = 0;
			int col = 0;

			// ���� ��з��� ã���� ������ ����ְ� ������ �������� �ش�.
			for (InventoryInfo info : invInfo) {
				if (info.getInv_code().charAt(0) == c) {
					row = info.getMax_row();
					col = info.getMax_col();
					break;
				}
			}

			// �� �������� ������ ������ �������� �κ��丮 Ŭ������ �ʱⰪ�� �����Ͽ� �ν��Ͻ��� ����־� �ش�.
			Inventory inventory = new Inventory(row, col, code.getInv_code(), nickName);
			inventoryMap.put(code.getInv_code(), inventory);
		}
		if (inventoryMap == null || inventoryMap.isEmpty()) {
			System.out.println("�κ��丮�� �ҷ����� �� ������ �߻��Ͽ����ϴ�. ���α׷��� �����մϴ�.");
			System.exit(-1);
		}

		// TB_INVENTORY(�κ��丮�� ����ִ� ������ ����)�� ���� �о� Map�� �ϳ��ϳ� �÷��д�.
		try {
			dbManager.selectInventory(nickName, inventoryMap);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SQL�� ���� �� ������ �߻��Ͽ����ϴ�. ���α׷��� �����մϴ�.");
			System.exit(-1);
		}

		// ��� �����͸� �ε��Ͽ��ٸ� ���ѷ����� �������� �ȳ��Ѵ�.
		String firstCode = null;
		// 1�� �޴�
		while (true) {
			firstCode = "";
			System.out.println("\n\n��� �����Ͱ� �ε�Ǿ����ϴ�. ��ȸ�� ���Ͻô� �κ��丮�� �������ּ���.");
			System.out.println("������ �ڵ带 �Է��Ͻø� ������ �����մϴ�. ex) A01");
			// �κ��丮 �ڵ� ���̺��� �ֿܼ� ����Ͽ� �����ش�.
			for (InventoryCode i : invCode) {
				if (i.getInv_code().length() == 1) {
					System.out.println("\n[" + i.getInv_code_nm() + " ��]");
				} else {
					System.out.print("[" + i.getInv_code() + "] " + i.getInv_code_nm() + "    ");
				}
			}

			System.out.println("\n\n'Z'�� �Է��Ͻø� ���α׷��� ����˴ϴ�.");
			System.out.println("�ڵ� �Է�([3���� �ڵ�]�� �Է� �����մϴ�) : ");

			firstCode = sc.next();
			sc.reset();

			System.out.println("�Է��Ͻ� �� : " + firstCode);

			if (firstCode.equals("Z") || firstCode.equals("z") || firstCode.equals("��")) {
				System.out.println("���α׷��� �����մϴ�.");
				System.exit(0);
			}

			// �Է��� �ڵ忡 ���� �޸𸮻� �ִ� �κ��丮�� ��ȸ �� ���� �޴��� �Ѿ��.
			// �Է°��� �Һз�(3����)�̸� ��ȸ�ϵ��� �Ѵ�.
			if (firstCode.length() == 3) {
				Inventory selectedInventory = inventoryMap.get(firstCode);
				// �߸��� �Է°��� �Է����� ��� �ʱ�ȭ������ ����������.
				if (selectedInventory == null) {
					System.out.println("�߸��� �κ��丮�� �����ϼ̽��ϴ�. �ʱ�ȭ������ ���ư��ϴ�.");
					continue;
				} else { // �ùٸ� ��(�ڵ� ���̺� ��)�� �Է����� ��� �κ��丮�� ������ ��ȸ�ϰ� 2�� �޴��� �����ϰ� �ȴ�.
					String secondCode = "";
					boolean backflag = false;

					while (true) {
						for (InventoryCode code : invCode) {
							if (firstCode.equals(code.getInv_code())) {
								System.out.println("[" + firstCode + "] :: " + code.getInv_code_nm() + "�� ������ ����Ʈ");
								break;
							}
						}
						selectedInventory.printAll();

						System.out.println("\n\n���Ͻô� ������ ������ �ּ���.");
						System.out.println(
								"[insert] ������ ȹ��    [delete] ������ ������    [update] ������ ��ġ �ٲٱ�    [extend] ������â Ȯ��    [reduce] ������â ���̱�    [X] �ʱ�޴��� ���ư���");

						secondCode = sc.next();
						sc.reset();

						System.out.println("�Է��Ͻ� �� : " + secondCode);

						String thirdCode = "";

						switch (secondCode) {
						case "insert": {
							System.out.println("�κ��丮�� ���� �������� �̸��� �Է��� �ּ���.");

							thirdCode = sc.next();
							sc.reset();

							System.out.println("�Է��Ͻ� �� : " + thirdCode);

							if (thirdCode.length() < 1) {
								System.out.println("������ �̸��� �ʹ� ª���ϴ�.");
								continue;
							}

							Item item = null;
							String fourthCode = "";
							// �������� ������ ���, �ƹ�Ÿ, ũ����(ũ����, ��Ƽ��Ʈ), Ż������, ������ ���
							// �� ĭ�� �ϳ��� ���� �� �ְ�, ���� �̸��� �������� ���� ĭ�� ���� ������ �� �ִ�.
							if (firstCode.equals("A01") || firstCode.equals("B01") || firstCode.equals("C01")
									|| firstCode.equals("C02") || firstCode.equals("D01") || firstCode.equals("E01")) {
								item = new Item(firstCode, thirdCode, 1, 1, true);
							} else { // �׿� �Ҹ�ǰ�̳� �ٸ� �������� ���, �� ĭ�� �ϳ��� ������ �� ������ �� �� ĭ�� �������� ������ �� �ִ�.
								// ���⼭�� ���ǻ� �ƽø� ������ 1000���� ��´�.
								System.out.println("���� �������� ������ �Է��� �ּ���.");

								fourthCode = sc.next();
								sc.reset();

								System.out.println("�Է��Ͻ� �� : " + fourthCode);

								int cnt = 0;
								try {
									cnt = Integer.parseInt(fourthCode);
								} catch (Exception e) {
									System.out.println("������ ��Ȯ���� �ʽ��ϴ�.");
									continue;
								}
								item = new Item(firstCode, thirdCode, 1000, cnt, false);
							}

							int result = selectedInventory.insertItem(item);
							if (result == 0) {
								System.out.println("�������� ȹ������ ���߽��ϴ�. ������â�� ���� á�ų�, ���� ������ �ʰ��Ͽ����ϴ�.");
							} else {
								System.out.println("[" + thirdCode + "] �������� " + result + "�� ȹ���Ͽ����ϴ�.");
							}
						}
							break;
						case "delete": {
							System.out.println("�κ��丮���� ���� �������� ��ǥ�� ������ ���� �������� �Է��� �ּ���.");
							System.out.println("ex) 7,5");

							thirdCode = sc.next();
							sc.reset();

							System.out.println("�Է��Ͻ� �� : " + thirdCode);
							String[] splited = thirdCode.split(",");
							int x = 0, y = 0;
							try {
								x = Integer.parseInt(splited[0]);
								y = Integer.parseInt(splited[1]);
							} catch (Exception e) {
								System.out.println("�߸��� �������� �Է��ϼ̽��ϴ�. ���� ȭ������ ���ư��ϴ�.");
								continue;
							}

							String fourthCode = "";
							int result = 0;
							// �������� ������ ���, �ƹ�Ÿ, ũ����(ũ����, ��Ƽ��Ʈ), Ż������, ������ ���
							// �� ĭ�� �ϳ��̹Ƿ�, �ش� ��ġ���� �ٷ� �����ָ� �ȴ�.
							if (firstCode.equals("A01") || firstCode.equals("B01") || firstCode.equals("C01")
									|| firstCode.equals("C02") || firstCode.equals("D01") || firstCode.equals("E01")) {
								result = selectedInventory.deleteItem(new Point(x, y));
							} else { // �׿� �Ҹ�ǰ�̳� �ٸ� �������� ���, �� ĭ�� �ϳ��� ������ �� ������ �� �� ĭ�� �������� ������ �� �ִ�.
								// ���⼭�� ���ǻ� �ƽø� ������ 1000���� ��´�.
								System.out.println("���� �������� ������ �Է��� �ּ���.");

								fourthCode = sc.next();
								sc.reset();

								System.out.println("�Է��Ͻ� �� : " + fourthCode);

								int cnt = 0;
								try {
									cnt = Integer.parseInt(fourthCode);
								} catch (Exception e) {
									System.out.println("������ ��Ȯ���� �ʽ��ϴ�.");
									continue;
								}

								result = selectedInventory.deleteItem(new Point(x, y), cnt);
							}

							if (result == 0) {
								System.out.println("�������� ������ �� �����Ͽ����ϴ�.");
							} else {
								System.out.println("[" + thirdCode + "] �������� " + result + "�� ���Ƚ��ϴ�.");
							}
						}
							break;
						case "update": {
							System.out.println("�κ��丮���� ��ġ�� �ٲ� �������� ���� ��ġ�� ������ ���� �������� �Է��� �ּ���.");
							System.out.println("ex) 7,5");

							thirdCode = sc.next();
							sc.reset();

							System.out.println("�Է��Ͻ� �� : " + thirdCode);
							String[] splited1 = thirdCode.split(",");
							int x1 = 0, y1 = 0;
							try {
								x1 = Integer.parseInt(splited1[0]);
								y1 = Integer.parseInt(splited1[1]);
							} catch (Exception e) {
								System.out.println("�߸��� �������� �Է��ϼ̽��ϴ�. ���� ȭ������ ���ư��ϴ�.");
								continue;
							}

							System.out.println("�ű�� ���� �������� ���� ��ġ�� ������ ���� �������� �Է��� �ּ���.");
							System.out.println("ex) 7,5");

							String fourthCode = sc.next();
							sc.reset();

							System.out.println("�Է��Ͻ� �� : " + thirdCode);
							String[] splited2 = fourthCode.split(",");
							int x2 = 0, y2 = 0;
							try {
								x2 = Integer.parseInt(splited2[0]);
								y2 = Integer.parseInt(splited2[1]);
							} catch (Exception e) {
								System.out.println("�߸��� �������� �Է��ϼ̽��ϴ�. ���� ȭ������ ���ư��ϴ�.");
								continue;
							}

							Item i1 = selectedInventory.getInventoryMap()[x1][y1];
							Item i2 = selectedInventory.getInventoryMap()[x2][y2];

							int result = selectedInventory.changeItemPosition(new Point(x1, y1), new Point(x2, y2));
							if (result == 0) {
								System.out.println("�� �� ���� ������ ���� ������ ��ġ�� �ٲ��� �ʾҽ��ϴ�.");
							} else {
								System.out.println("�������� ��ġ�� �ٲ�����ϴ�.");
							}
						}
							break;
						case "extend": {
							// ���� �κ��丮(�Һз�)�� ��з� �ڵ尪�̴�.
							char bigSort = selectedInventory.getCode().charAt(0);
							int result = 0;

							// �κ��丮���� Ű���� �����Ͽ�, ��з��� ������ ���� �κ��丮 Ȯ���� ȿ���� �ش�.
							Iterator<String> iter = inventoryMap.keySet().iterator();
							while (iter.hasNext()) {
								String code = iter.next();
								if (code.charAt(0) == bigSort) {
									inventoryMap.get(code).extendInventory();
								}
							}

							String str = "" + bigSort;

							try {
								result = dbManager.extendInventory(nickName, str);
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (result == 0) {
								System.out.println("�� �� ���� ������ ���� �κ��丮�� Ȯ����� �ʾҽ��ϴ�.");
							} else {
								System.out.println("�κ��丮�� [" + selectedInventory.getMaxRow() + "/"
										+ selectedInventory.getMaxCol() + "] ���� 1�� Ȯ��Ǿ����ϴ�.");
							}
						}
							break;
						case "reduce": {
							// ���� �κ��丮(�Һз�)�� ��з� �ڵ尪�̴�.
							char bigSort = selectedInventory.getCode().charAt(0);
							int result = 0;

							// �κ��丮���� Ű���� �����Ͽ�, ��з��� ������ ���� �κ��丮 Ȯ���� ȿ���� �ش�.
							Iterator<String> iter = inventoryMap.keySet().iterator();
							while (iter.hasNext()) {
								String code = iter.next();
								if (code.charAt(0) == bigSort) {
									inventoryMap.get(code).reduceInventory();
								}
							}

							String str = "" + bigSort;

							try {
								result = dbManager.reduceInventory(nickName, str);
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (result == 0) {
								System.out.println("�� �� ���� ������ ���� �κ��丮�� ��ҵ��� �ʾҽ��ϴ�.");
							} else {
								System.out.println("�κ��丮�� [" + selectedInventory.getMaxRow() + "/"
										+ selectedInventory.getMaxCol() + "] ���� 1�� ��ҵǾ����ϴ�.");
							}
						}
							break;
						case "X":
						case "x":
						case "��":
							backflag = true;
							break;
						}

						if (backflag)
							break;
					}
				}

			} else {
				System.out.println("�ڵ尪�� ���� �ڵ�ǥ�� �ִ� [3���� �ڵ�]�� �Է� �����մϴ�. �Է�ȭ������ ���ư��ϴ�.");
				continue;
			}
		}
	}
}
