package tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FetchLogV2 {

	public static void main(String[] args) {
		String startTime = printTime();
		System.out.println("開始查詢時間" + startTime);
		
		HashMap<String, List<String>> conditionMap = readConditions("conditions.txt");
		execute(conditionMap);
		
		System.out.println("開始查詢時間" + startTime);
		System.out.println("結束查詢時間" + printTime());
	}

	/**
	 * 時間戳記
	 * @param text
	 */
	private static String printTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		return dtf.format(LocalDateTime.now());
	}

	/**
	 * 讀取每一行
	 * @param conditionMap
	 */
	private static void execute(HashMap<String, List<String>> conditionMap) {
		//要讀取的檔案
		List<String> pathList = conditionMap.get("logPaths");
		for (String filePath : pathList) {
			try (FileReader fr = new FileReader(filePath); 
					BufferedReader br = new BufferedReader(fr);) {
				String line;
				int num = 1;
				HashMap<String, HashMap<String, Integer>> infoMap = initInfoMap(conditionMap);
				while ((line = br.readLine()) != null) {
					//跳過空行
					if (line.isEmpty()) {
						continue;
					}
					
					//是否滿足要找 log 的條件
					if (hasMeetCondition(line, conditionMap)) {
						//列印符合條件的完整log
						System.out.println("第" + num++ + "筆 " + line);
						addReportList(line, conditionMap, infoMap);
					}
				}
				
				printReport(infoMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 列出額外資訊報告
	 * @param infoMap
	 */
	private static void printReport(HashMap<String, HashMap<String, Integer>> infoMap) {
		for (Map.Entry<String, HashMap<String, Integer>> infoEntry : infoMap.entrySet()) {
			//列出種類
			System.out.println(infoEntry.getKey());
			
			//依照次數排序
			List<Map.Entry<String, Integer>> list = new ArrayList<>(infoEntry.getValue().entrySet());
	        list.sort(Map.Entry.comparingByValue());
	        for (Map.Entry<String, Integer> orderEntry : list) {
				System.out.println(orderEntry.getValue() + "次 " + orderEntry.getKey());
	        }
		}
	}

	/**
	 * 初始化infoMap
	 * @param conditionMap
	 * @return
	 */
	private static HashMap<String, HashMap<String, Integer>> initInfoMap(HashMap<String, List<String>> conditionMap) {
		List<String> infoList = conditionMap.get("infos");
		HashMap<String, HashMap<String, Integer>> resultMap = new HashMap<String, HashMap<String, Integer>>();
		for (String info : infoList) {
			resultMap.put(info, new HashMap<String, Integer>());
		}
		return resultMap;
	}

	/**
	 * 針對itr.log，整理出 想獲取的額外資訊
	 * @param line
	 * @param conditionMap
	 */
	private static void addReportList(String line, HashMap<String, List<String>> conditionMap, HashMap<String, HashMap<String, Integer>> infoMap) {
		List<String> infos = conditionMap.get("infos");
		
		//抓取log純訊息
		if (infos.contains("log")) {
			HashMap<String, Integer> logMap = infoMap.get("log");
			logMap.put(line.split("\\]")[3], logMap.get(line.split("\\]")[3]) == null ? 1 : logMap.get(line.split("\\]")[3]) + 1);
		}
		
		//TODO 其他的info抓取
	}

	/**
	 * webitr.log是否滿足條件
	 * @param line
	 * @param conditionMap
	 * @return
	 */
	private static boolean hasMeetCondition(String line, HashMap<String, List<String>> conditionMap) {
		//是否在查詢期間內
		List<String> periodList = conditionMap.get("period");
		String startTime = periodList.stream().min(String::compareTo).get();
		String endTime = periodList.stream().max(String::compareTo).get();
		//適用webitr.log
		if (line.length() < 25) {
			return false;
		}
		String lineTime = line.substring(6, 25);
		if (lineTime.compareTo(startTime) < 0 || endTime.compareTo(lineTime) < 0) {
			return false;
		}
		
		//是否符合關鍵字
		List<String> keywordList = conditionMap.get("keywords");
		for (String keyword : keywordList) {
			String operator = getConditionOperator(keyword);
			if ("and".equals(operator) && !line.contains(keyword.substring(4))) {
				return false;
			} else if ("not".equals(operator) && line.contains(keyword.substring(4))) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 取得條件運算子
	 */
	private static String getConditionOperator(String keyword) {
		if ("not ".equals(keyword.substring(0, 4))) {
			return "not";
		} else {
			return "and";
		}
	}

	/**
	 * 讀取條件(要查詢的log、關鍵字、時間 及 要獲取的資訊)
	 * 
	 * @param string
	 */
	private static HashMap<String, List<String>> readConditions(String filePath) {
		//回傳集合
		HashMap<String, List<String>> conditionMap = new HashMap<>();
		ArrayList<String> conditionList = new ArrayList<>();
		
		try (FileReader fr = new FileReader(filePath); 
				BufferedReader br = new BufferedReader(fr);) {
			String line, key = "";
			while ((line = br.readLine()) != null) {
				//不讀註解 及 空行
				if ((line.length() > 1 && "--".equals(line.substring(0, 2))) || line.isEmpty()) {
					continue;
				}
				
				//讀條件種類
				if ("*".equals(line.substring(0, 1))) {
					key = line.substring(1);
					conditionList = new ArrayList<>();
					continue;
				}
				
				//儲存條件
				conditionList.add(line);
				conditionMap.put(key, conditionList);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return conditionMap;
	}

}
