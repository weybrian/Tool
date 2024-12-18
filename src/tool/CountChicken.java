package tool;

public class CountChicken {

	public static void main(String[] args) {
		int month = 16;
		System.out.println(countAllChicken(month));
	}

	private static int newChickenInTheMonth(int month) {
		if (month == 0) {
			return 1;
		} else if (month == 1 || month == 2) {
			return 0;
		} else {
			return countAllChicken(month - 3);
		}
	}

	private static int countAllChicken(int month) {
		if (month == 0) {
			return 1;
		} else {
			return countAllChicken(month - 1) + newChickenInTheMonth(month);
		}
	}

}
