package tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
				while ((line = br.readLine()) != null) {
					//跳過空行
					if (line.isEmpty()) {
						continue;
					}
					
					//是否滿足要找 log 的條件
					if (hasMeetCondition(line, conditionMap)) {
						System.out.println(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		
		//是否包含關鍵字
		List<String> keywordList = conditionMap.get("keywords");
		for (String keyword : keywordList) {
			keyword.split(" ");
			if (!line.contains(keyword)) {
				return false;
			}
		}
		
		return true;
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
