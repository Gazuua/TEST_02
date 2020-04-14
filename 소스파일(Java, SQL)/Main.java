import java.util.*;

public class Main {

	// 이 프로그램에서는 main메서드에 진입하는 것을 접속으로 간주합니다.
	// 접속 시 Scanner를 통해 로그인하고자 하는 닉네임을 입력받습니다.
	public static void main(String[] args) {

		// DB 연결 시도
		DBManager dbManager = DBManager.getInstance();
		// DB 연결 실패시 exit
		if (!dbManager.isConnected()) {
			System.exit(-1);
		}

		Scanner sc = new Scanner(System.in);
		String nickName = "";

		while (true) {
			// 로그인 안내
			System.out.println("로그인할 닉네임을 입력해 주십시오.");
			System.out.print("닉네임 입력 : ");

			boolean bLogin = false;
			nickName = sc.next();
			sc.reset();

			// DB에서 닉네임을 조회하여 로그인을 시도한다
			try {
				bLogin = dbManager.selectNickName(nickName);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("SQL문 실행 중 오류가 발생하였습니다. 프로그램을 종료합니다.");
				System.exit(-1);
			}

			if (!bLogin) {
				System.out.println("닉네임이 조회되지 않아 로그인에 실패하였습니다. 다시 입력해 주세요.");
				continue;
			} else {
				break;
			}
		}

		// 로그인 성공 시
		System.out.println("\n\n로그인에 성공하셨습니다. 로그인한 닉네임은 [" + nickName + "] 입니다.");
		System.out.println("데이터를 로드합니다...");

		// TB_INVENTORY_CODE를 조회하여 선택할 수 있는 인벤토리 종류를 뿌려준다.
		List<InventoryCode> invCode = null;
		try {
			invCode = dbManager.selectInventoryCode();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SQL문 실행 중 오류가 발생하였습니다. 프로그램을 종료합니다.");
			System.exit(-1);
		}
		if (invCode == null || invCode.isEmpty()) {
			System.out.println("인벤토리 코드값을 불러오는 중 오류가 발생하였습니다. 프로그램을 종료합니다.");
			System.exit(-1);
		}

		// TB_INVENTORY_INFO를 모두 구해온다.
		List<InventoryInfo> invInfo = null;
		try {
			invInfo = dbManager.selectInventoryInfo(nickName);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SQL문 실행 중 오류가 발생하였습니다. 프로그램을 종료합니다.");
			System.exit(-1);
		}
		if (invInfo == null || invInfo.isEmpty()) {
			System.out.println("인벤토리 정보값을 불러오는 중 오류가 발생하였습니다. 프로그램을 종료합니다.");
			System.exit(-1);
		}

		// invCode와 invInfo 리스트에 기반하여 인벤토리 맵(인벤토리 속성과 아이템 맵을 담은 클래스)을 만든다.
		Map<String, Inventory> inventoryMap = new HashMap<String, Inventory>();
		for (InventoryCode code : invCode) {

			// 대분류일 경우 스킵한다.
			if (code.getInv_code().length() == 1) {
				continue;
			}

			// 소분류일 경우 대분류값에 근거하여 정보를 가져온다
			char c = code.getInv_code().charAt(0);
			int row = 0;
			int col = 0;

			// 같은 대분류를 찾으면 정보를 집어넣고 루프를 빠져나와 준다.
			for (InventoryInfo info : invInfo) {
				if (info.getInv_code().charAt(0) == c) {
					row = info.getMax_row();
					col = info.getMax_col();
					break;
				}
			}

			// 위 루프에서 가져온 정보를 바탕으로 인벤토리 클래스의 초기값을 세팅하여 인스턴스를 집어넣어 준다.
			Inventory inventory = new Inventory(row, col, code.getInv_code(), nickName);
			inventoryMap.put(code.getInv_code(), inventory);
		}
		if (inventoryMap == null || inventoryMap.isEmpty()) {
			System.out.println("인벤토리를 불러오는 중 오류가 발생하였습니다. 프로그램을 종료합니다.");
			System.exit(-1);
		}

		// TB_INVENTORY(인벤토리에 들어있는 아이템 정보)를 전부 읽어 Map에 하나하나 올려둔다.
		try {
			dbManager.selectInventory(nickName, inventoryMap);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SQL문 실행 중 오류가 발생하였습니다. 프로그램을 종료합니다.");
			System.exit(-1);
		}

		// 모든 데이터를 로딩하였다면 무한루프로 선택지를 안내한다.
		String firstCode = null;
		// 1차 메뉴
		while (true) {
			firstCode = "";
			System.out.println("\n\n모든 데이터가 로드되었습니다. 조회를 원하시는 인벤토리를 선택해주세요.");
			System.out.println("좌측의 코드를 입력하시면 선택이 가능합니다. ex) A01");
			// 인벤토리 코드 테이블을 콘솔에 출력하여 보여준다.
			for (InventoryCode i : invCode) {
				if (i.getInv_code().length() == 1) {
					System.out.println("\n[" + i.getInv_code_nm() + " 탭]");
				} else {
					System.out.print("[" + i.getInv_code() + "] " + i.getInv_code_nm() + "    ");
				}
			}

			System.out.println("\n\n'Z'를 입력하시면 프로그램이 종료됩니다.");
			System.out.println("코드 입력([3글자 코드]만 입력 가능합니다) : ");

			firstCode = sc.next();
			sc.reset();

			System.out.println("입력하신 값 : " + firstCode);

			if (firstCode.equals("Z") || firstCode.equals("z") || firstCode.equals("ㅋ")) {
				System.out.println("프로그램을 종료합니다.");
				System.exit(0);
			}

			// 입력한 코드에 따라 메모리상에 있는 인벤토리를 조회 후 하위 메뉴로 넘어간다.
			// 입력값이 소분류(3글자)이면 조회하도록 한다.
			if (firstCode.length() == 3) {
				Inventory selectedInventory = inventoryMap.get(firstCode);
				// 잘못된 입력값을 입력했을 경우 초기화면으로 돌려버린다.
				if (selectedInventory == null) {
					System.out.println("잘못된 인벤토리를 선택하셨습니다. 초기화면으로 돌아갑니다.");
					continue;
				} else { // 올바른 값(코드 테이블 값)을 입력했을 경우 인벤토리의 내용을 조회하고 2차 메뉴로 진입하게 된다.
					String secondCode = "";
					boolean backflag = false;

					while (true) {
						for (InventoryCode code : invCode) {
							if (firstCode.equals(code.getInv_code())) {
								System.out.println("[" + firstCode + "] :: " + code.getInv_code_nm() + "탭 아이템 리스트");
								break;
							}
						}
						selectedInventory.printAll();

						System.out.println("\n\n원하시는 동작을 선택해 주세요.");
						System.out.println(
								"[insert] 아이템 획득    [delete] 아이템 버리기    [update] 아이템 위치 바꾸기    [extend] 아이템창 확장    [reduce] 아이템창 줄이기    [X] 초기메뉴로 돌아가기");

						secondCode = sc.next();
						sc.reset();

						System.out.println("입력하신 값 : " + secondCode);

						String thirdCode = "";

						switch (secondCode) {
						case "insert": {
							System.out.println("인벤토리에 넣을 아이템의 이름을 입력해 주세요.");

							thirdCode = sc.next();
							sc.reset();

							System.out.println("입력하신 값 : " + thirdCode);

							if (thirdCode.length() < 1) {
								System.out.println("아이템 이름이 너무 짧습니다.");
								continue;
							}

							Item item = null;
							String fourthCode = "";
							// 아이템의 종류가 장비, 아바타, 크리쳐(크리쳐, 아티팩트), 탈리스만, 휘장인 경우
							// 한 칸당 하나만 먹을 수 있고, 같은 이름의 아이템이 여러 칸에 걸쳐 존재할 수 있다.
							if (firstCode.equals("A01") || firstCode.equals("B01") || firstCode.equals("C01")
									|| firstCode.equals("C02") || firstCode.equals("D01") || firstCode.equals("E01")) {
								item = new Item(firstCode, thirdCode, 1, 1, true);
							} else { // 그외 소모품이나 다른 아이템의 경우, 한 칸에 하나만 존재할 수 있으나 그 한 칸에 여러개를 보유할 수 있다.
								// 여기서는 편의상 맥시멈 개수를 1000개로 잡는다.
								System.out.println("넣을 아이템의 개수를 입력해 주세요.");

								fourthCode = sc.next();
								sc.reset();

								System.out.println("입력하신 값 : " + fourthCode);

								int cnt = 0;
								try {
									cnt = Integer.parseInt(fourthCode);
								} catch (Exception e) {
									System.out.println("개수가 정확하지 않습니다.");
									continue;
								}
								item = new Item(firstCode, thirdCode, 1000, cnt, false);
							}

							int result = selectedInventory.insertItem(item);
							if (result == 0) {
								System.out.println("아이템을 획득하지 못했습니다. 아이템창이 가득 찼거나, 제한 개수를 초과하였습니다.");
							} else {
								System.out.println("[" + thirdCode + "] 아이템을 " + result + "개 획득하였습니다.");
							}
						}
							break;
						case "delete": {
							System.out.println("인벤토리에서 버릴 아이템의 좌표를 다음과 같은 형식으로 입력해 주세요.");
							System.out.println("ex) 7,5");

							thirdCode = sc.next();
							sc.reset();

							System.out.println("입력하신 값 : " + thirdCode);
							String[] splited = thirdCode.split(",");
							int x = 0, y = 0;
							try {
								x = Integer.parseInt(splited[0]);
								y = Integer.parseInt(splited[1]);
							} catch (Exception e) {
								System.out.println("잘못된 형식으로 입력하셨습니다. 이전 화면으로 돌아갑니다.");
								continue;
							}

							String fourthCode = "";
							int result = 0;
							// 아이템의 종류가 장비, 아바타, 크리쳐(크리쳐, 아티팩트), 탈리스만, 휘장인 경우
							// 한 칸당 하나이므로, 해당 위치에서 바로 없애주면 된다.
							if (firstCode.equals("A01") || firstCode.equals("B01") || firstCode.equals("C01")
									|| firstCode.equals("C02") || firstCode.equals("D01") || firstCode.equals("E01")) {
								result = selectedInventory.deleteItem(new Point(x, y));
							} else { // 그외 소모품이나 다른 아이템의 경우, 한 칸에 하나만 존재할 수 있으나 그 한 칸에 여러개를 보유할 수 있다.
								// 여기서는 편의상 맥시멈 개수를 1000개로 잡는다.
								System.out.println("버릴 아이템의 개수를 입력해 주세요.");

								fourthCode = sc.next();
								sc.reset();

								System.out.println("입력하신 값 : " + fourthCode);

								int cnt = 0;
								try {
									cnt = Integer.parseInt(fourthCode);
								} catch (Exception e) {
									System.out.println("개수가 정확하지 않습니다.");
									continue;
								}

								result = selectedInventory.deleteItem(new Point(x, y), cnt);
							}

							if (result == 0) {
								System.out.println("아이템을 버리는 데 실패하였습니다.");
							} else {
								System.out.println("[" + thirdCode + "] 아이템을 " + result + "개 버렸습니다.");
							}
						}
							break;
						case "update": {
							System.out.println("인벤토리에서 위치를 바꿀 아이템의 기존 위치를 다음과 같은 형식으로 입력해 주세요.");
							System.out.println("ex) 7,5");

							thirdCode = sc.next();
							sc.reset();

							System.out.println("입력하신 값 : " + thirdCode);
							String[] splited1 = thirdCode.split(",");
							int x1 = 0, y1 = 0;
							try {
								x1 = Integer.parseInt(splited1[0]);
								y1 = Integer.parseInt(splited1[1]);
							} catch (Exception e) {
								System.out.println("잘못된 형식으로 입력하셨습니다. 이전 화면으로 돌아갑니다.");
								continue;
							}

							System.out.println("옮기고 싶은 아이템의 최종 위치를 다음과 같은 형식으로 입력해 주세요.");
							System.out.println("ex) 7,5");

							String fourthCode = sc.next();
							sc.reset();

							System.out.println("입력하신 값 : " + thirdCode);
							String[] splited2 = fourthCode.split(",");
							int x2 = 0, y2 = 0;
							try {
								x2 = Integer.parseInt(splited2[0]);
								y2 = Integer.parseInt(splited2[1]);
							} catch (Exception e) {
								System.out.println("잘못된 형식으로 입력하셨습니다. 이전 화면으로 돌아갑니다.");
								continue;
							}

							Item i1 = selectedInventory.getInventoryMap()[x1][y1];
							Item i2 = selectedInventory.getInventoryMap()[x2][y2];

							int result = selectedInventory.changeItemPosition(new Point(x1, y1), new Point(x2, y2));
							if (result == 0) {
								System.out.println("알 수 없는 오류로 인해 아이템 위치가 바뀌지 않았습니다.");
							} else {
								System.out.println("아이템의 위치가 바뀌었습니다.");
							}
						}
							break;
						case "extend": {
							// 현재 인벤토리(소분류)의 대분류 코드값이다.
							char bigSort = selectedInventory.getCode().charAt(0);
							int result = 0;

							// 인벤토리별로 키값을 조사하여, 대분류가 같으면 전부 인벤토리 확장의 효과를 준다.
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
								System.out.println("알 수 없는 오류로 인해 인벤토리가 확장되지 않았습니다.");
							} else {
								System.out.println("인벤토리가 [" + selectedInventory.getMaxRow() + "/"
										+ selectedInventory.getMaxCol() + "] 으로 1줄 확장되었습니다.");
							}
						}
							break;
						case "reduce": {
							// 현재 인벤토리(소분류)의 대분류 코드값이다.
							char bigSort = selectedInventory.getCode().charAt(0);
							int result = 0;

							// 인벤토리별로 키값을 조사하여, 대분류가 같으면 전부 인벤토리 확장의 효과를 준다.
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
								System.out.println("알 수 없는 오류로 인해 인벤토리가 축소되지 않았습니다.");
							} else {
								System.out.println("인벤토리가 [" + selectedInventory.getMaxRow() + "/"
										+ selectedInventory.getMaxCol() + "] 으로 1줄 축소되었습니다.");
							}
						}
							break;
						case "X":
						case "x":
						case "ㅌ":
							backflag = true;
							break;
						}

						if (backflag)
							break;
					}
				}

			} else {
				System.out.println("코드값은 좌측 코드표에 있는 [3글자 코드]만 입력 가능합니다. 입력화면으로 돌아갑니다.");
				continue;
			}
		}
	}
}
