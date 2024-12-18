package tool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class FetchLog {

	public static void main(String[] args) {
		// log檔案路徑
		String fileName = "D:\\logPaths.txt";
		
		// 想查的關鍵字
		String keywordsFileName = "D:\\keywords.txt";
		
		// 額外抓取的資訊
		String fetchInfoFileName = "D:\\fetchInfo.txt";
		read(fileName, keywordsFileName, fetchInfoFileName);
	}

	public static void read(String fileName, String keywordsFileName, String fetchInfoFileName) {
		// 時間格式
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		System.out.println("開始查詢時間" + dtf.format(LocalDateTime.now()));

		// ======================================================================讀file開始
		ArrayList<String> keywordsList = readTxt2List(keywordsFileName);
		String conditionConcat = getConditionConcat(keywordsList);

		ArrayList<String> fetchInfoList = readTxt2List(fetchInfoFileName);
		// ======================================================================讀file結束

		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String filePath = null;
		String logFileName = null;
		String line = null;// log訊息
		String result = null;
		Integer breakNum = 0;

		try {
			// 讀檔案路徑
			while (((filePath = br.readLine()) != null)) {
				if (filePath.isEmpty() || "@".equals(filePath.substring(0, 1))) {
					continue;
				} else if (filePath.contains("\"")) {
					logFileName = filePath.substring(1, filePath.length() - 1);
				} else {
					logFileName = filePath;
				}
				// 列出檔案路徑
				System.out.println(logFileName);

				// ======================================================================開始讀log
				FileReader logfr = null;
				try {
					logfr = new FileReader(logFileName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				BufferedReader logbr = new BufferedReader(logfr);
				Integer num = 0;// log行數
				Integer printNum = 1;// 列印筆數
				ArrayList<String> lineList = new ArrayList<>();
				String oldLine = "";
				try {
					while (((line = logbr.readLine()) != null)) {// 讀每行log

						num++;
						breakNum = num;
						Boolean fulfillFlag = conditionConcat.equals("OR") ? false : true;// 滿足keyword條件
						String logLevel = line.length() > 5 ? line.substring(0, 5) : "";
						if (!logLevel.equals("FETAL") && !logLevel.equals("ERROR") && !logLevel.equals("WARN ")
								&& !logLevel.equals("INFO ") && !logLevel.equals("DEBUG") && !logLevel.equals("TRACE")
								&& lineList
										.contains("第 " + (printNum - 1) + " 筆 " + "第 " + (num - 1) + " 行 " + oldLine)) {
							fulfillFlag = true;
						} else {
							for (int i = 0; i < keywordsList.size(); i++) {
								if (conditionConcat.equals("OR") ? line.contains(keywordsList.get(i))
										: !line.contains(keywordsList.get(i))) {
									fulfillFlag = conditionConcat.equals("OR") ? true : false;
									break;
								}
							}
						}

						if (fulfillFlag) {
							result = "第 " + printNum++ + " 筆 " + "第 " + num + " 行 " + line;
							oldLine = line;
							System.out.println(result);// 註解此行「列出符合條件的log」可加速執行
							lineList.add(result);
						}
					}

					// 印出資訊
					String key = null;
					for (String info : fetchInfoList) {
						Integer infoNum = 1;
						HashMap<String, Integer> map = new HashMap<>();
						for (String fetchLine : lineList) {
							if (fetchLine.contains(info)) {
								key = fetchLine.split(info)[1].split(",")[0];
								if (map.containsKey(key)) {
									map.put(key, map.get(key) + 1);
								} else {
									map.put(key, 1);
								}
							}
						}

						System.out.println(info);
						Set<String> set = map.keySet();
						Iterator<String> iterator = set.iterator();
						while (iterator.hasNext()) {
							key = iterator.next();
							// System.out.println("第 " + infoNum++ + " 筆 " + "有 " + map.get(key) + " 筆 " +
							// key);//可列出筆數 與資訊
							System.out.println(key);// 僅列出不重複的資訊
						}
					}
					// 印出資訊結束

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						logbr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// ======================================================================結束讀log
			}

			System.out.println("結束查詢時間" + dtf.format(LocalDateTime.now()));
			// TODO 計算執行時間
		} catch (IOException e) {
			System.out.println("中斷於第 " + (breakNum + 1) + " 行");
			e.printStackTrace();
		} finally {
			try {
				br.close();
				System.out.println("結束時間" + dtf.format(LocalDateTime.now()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 取得關鍵字條件運算子
	 * @param list
	 * @return
	 */
	private static String getConditionConcat(ArrayList<String> list) {
		if (list.contains("OR")) {
			list.remove("OR");
			return "OR";
		}
		return "AND";
	}

	/**
	 * 將文字檔轉成 list
	 * @param fileName
	 * @return
	 */
	public static ArrayList<String> readTxt2List(String fileName) {
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		ArrayList<String> list = new ArrayList<>();
		try {
			while (((line = br.readLine()) != null)) {// 讀檔案路徑
				if (line.isEmpty() || "@".equals(line.substring(0, 1))) {// 不會讀取空白行 與字首符號為「@」
					continue;
				}
				list.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

}
