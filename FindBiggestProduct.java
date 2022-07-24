import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class FindBiggestProduct {

	private static int maxRow = 20;
	private static int maxColumn = 20;
	private static int nNumbers = 5;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLACK = ""; // "\u001B[30m";
	public static final String ANSI_WHITE_BACKGROUND = ""; // "\u001B[47m";

	private static boolean printData = false;

	public static void main(String[] args) {

		if (nNumbers > maxRow && nNumbers > maxColumn) {
			System.err.println("--> data not correct");
			return;
		}

		printData = false;
		maxRow = 1500;
		maxColumn = 1530;
		nNumbers = 50;

		try {
			new FindBiggestProduct().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run() throws Exception {

		NumArray numArray = new NumArray();
		ResultFinder resultArray = null;

		ResultFinder resultHorz = new ResultFinder(numArray, ORDERTYPE.HORIZONTAL);
		ResultFinder resultVert = new ResultFinder(numArray, ORDERTYPE.VERTICAL);
		ResultFinder resultDiagDec = new ResultFinder(numArray, ORDERTYPE.DIAGONALDEC);
		ResultFinder resultDiagInc = new ResultFinder(numArray, ORDERTYPE.DIAGONALINC);

		boolean useThread = true;
		long startTime = System.nanoTime();

		if (useThread) {
			ExecutorService executor = Executors.newFixedThreadPool(100);

			executor.execute(resultHorz);
			executor.execute(resultVert);
			executor.execute(resultDiagDec);
			executor.execute(resultDiagInc);

			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);

		} else {
			resultHorz.findBiggestProduct();
			resultVert.findBiggestProduct();
			resultDiagDec.findBiggestProduct();
			resultDiagInc.findBiggestProduct();
		}

		if (resultHorz.total > resultVert.total) {
			if (resultHorz.total > resultDiagDec.total) {
				if (resultHorz.total > resultDiagInc.total) {
					resultArray = resultHorz;
				} else {
					resultArray = resultDiagInc;
				}
			} else {
				resultArray = resultDiagDec;
			}
		} else {
			if (resultVert.total > resultDiagDec.total) {
				if (resultVert.total > resultDiagInc.total) {
					resultArray = resultVert;
				} else {
					resultArray = resultDiagInc;
				}
			} else {
				resultArray = resultDiagDec;
			}
		}
		long endTime = System.nanoTime();

		if (printData) {
			System.out.println("--------------------");
			numArray.printInputData(resultHorz, resultVert, resultDiagDec, resultDiagInc);
		}

		System.out.println("--------------------");
		resultArray.printResult();

		System.out.println("--------------------");
		resultHorz.printResult();
		resultVert.printResult();
		resultDiagDec.printResult();
		resultDiagInc.printResult();

		System.out.println("--------------------");
		long duration = (endTime - startTime) / 1000000;
		System.out.println("Operation took " + duration + " milliseconds");
	}

	class NumArray {
		private int[][] inputData = new int[maxRow][maxColumn];

		public NumArray() {
			for (int row = 0; row < maxRow; row++) {
				for (int col = 0; col < maxColumn; col++) {
					inputData[row][col] = ThreadLocalRandom.current().nextInt(1, 100);
				}
			}
		}

		public void printInputData(ResultFinder resultHor, ResultFinder resultVer, ResultFinder resultDiagDec,
				ResultFinder resultDiagInc) {
			for (int row = 0; row < maxRow; row++) {
				StringBuilder printRow = new StringBuilder();
				for (int col = 0; col < maxColumn; col++) {
					if (resultHor.coordinateMatch(row, col)) {
						printRow.append(resultHor.printComponent(inputData[row][col]));
					} else if (resultVer.coordinateMatch(row, col)) {
						printRow.append(resultVer.printComponent(inputData[row][col]));
					} else if (resultDiagDec.coordinateMatch(row, col)) {
						printRow.append(resultDiagDec.printComponent(inputData[row][col]));
					} else if (resultDiagInc.coordinateMatch(row, col)) {
						printRow.append(resultDiagInc.printComponent(inputData[row][col]));
					} else {
						printRow.append(ANSI_WHITE_BACKGROUND + ANSI_BLACK
								+ String.format("%02d  ", inputData[row][col]) + ANSI_RESET);
					}
				}
				System.out.println(printRow.toString());
			}
		}
	}

	enum ORDERTYPE {
		HORIZONTAL(ANSI_RED),
		VERTICAL(ANSI_CYAN),
		DIAGONALINC(ANSI_GREEN),
		DIAGONALDEC(ANSI_YELLOW),
		ERROR("");

		public String consoleColor = "";

		private ORDERTYPE(String consoleColor) {
			this.consoleColor = consoleColor;
		}
	};

	static class ResultFinder implements Runnable {

		int total = 1;
		int[] numbers = new int[nNumbers];
		int totalOperation = 0;

		Point[] points = new Point[nNumbers];
		NumArray numArray;
		ORDERTYPE orderType = ORDERTYPE.ERROR;

		public ResultFinder(NumArray numArray, ORDERTYPE orderType) {
			this.orderType = orderType;
			this.numArray = numArray;
		}

		public void printResult() {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < numbers.length; i++) {
				result.append(String.format("%02d  ", numbers[i]));
			}
			System.out.println(ANSI_WHITE_BACKGROUND + orderType.consoleColor
					+ String.format("Total: %d : %s, operation: %d, %s", total, result.toString().trim(),
							totalOperation,
							orderType)
					+ ANSI_RESET);
		}

		public String printComponent(int data) {
			return (ANSI_WHITE_BACKGROUND + orderType.consoleColor + String.format("%02d  ", data) + ANSI_RESET);
		}

		@Override
		public void run() {
			findBiggestProduct();
		}

		public void findBiggestProduct() {
			int colStart = 0;
			int colEnd = maxColumn;
			int rowStart = 0;
			int rowEnd = maxRow;

			switch (orderType) {
				case HORIZONTAL:
					colEnd = maxColumn - nNumbers;
					break;
				case VERTICAL:
					rowEnd = maxRow - nNumbers;
					break;
				case DIAGONALDEC:
					colEnd = maxColumn - nNumbers;
					rowEnd = maxRow - nNumbers;
					break;
				case DIAGONALINC:
					rowStart = nNumbers;
					colEnd = maxColumn - nNumbers;
					break;
				default:
			}

			for (int row = rowStart; row < rowEnd; row++) {
				for (int col = colStart; col < colEnd; col++) {
					checkElements(row, col);
				}
			}
		}

		void checkElements(int startRow, int startCol) {
			totalOperation++;
			int currentResult = 1;
			int[] currentNumbers = new int[nNumbers];
			Point[] currentPoints = new Point[nNumbers];

			boolean indexValid = true;

			switch (orderType) {
				case HORIZONTAL:
					for (int y = 0; y < nNumbers && indexValid; y++) {
						currentPoints[y] = new Point((startRow), (startCol + y));
						currentNumbers[y] = numArray.inputData[currentPoints[y].x][currentPoints[y].y];
						currentResult *= currentNumbers[y];
						indexValid = (startCol + y) + 1 <= maxColumn;
					}
					break;
				case VERTICAL:
					for (int y = 0; y < nNumbers && indexValid; y++) {
						currentPoints[y] = new Point((startRow + y), (startCol));
						currentNumbers[y] = numArray.inputData[currentPoints[y].x][currentPoints[y].y];
						currentResult *= currentNumbers[y];
						indexValid = (startRow + y) + 1 <= maxRow;
					}
					break;
				case DIAGONALDEC:
					for (int y = 0; y < nNumbers && indexValid; y++) {
						currentPoints[y] = new Point((startRow + y), (startCol + y));
						currentNumbers[y] = numArray.inputData[currentPoints[y].x][currentPoints[y].y];
						currentResult *= currentNumbers[y];
						indexValid = ((startRow + y) + 1 <= maxRow) && ((startCol + y) + 1 <= maxColumn);
					}
					break;
				case DIAGONALINC:
					for (int y = 0; y < nNumbers && indexValid; y++) {
						currentPoints[y] = new Point((startRow - y), (startCol + y));
						currentNumbers[y] = numArray.inputData[currentPoints[y].x][currentPoints[y].y];
						currentResult *= currentNumbers[y];
						indexValid = ((startRow - y) - 1 >= 0) && ((startCol + y) + 1 <= maxColumn);
					}
					break;
				default:
			}

			if (currentResult > total) {
				total = currentResult;
				points = currentPoints;
				numbers = currentNumbers;
			}
		}

		boolean coordinateMatch(int row, int column) {
			boolean result = false;
			for (int i = 0; i < nNumbers; i++) {
				if (points[i] == null) {
					// System.err.println("NULL?? " + i + " -> in: " + row + ", " + column + " - " +
					// orderType);
				}
				if (points[i] != null && points[i].y == column && points[i].x == row) {
					return true;
				}
			}
			return result;
		}

		class Point {
			int x;
			int y;

			public Point(int x, int y) {
				this.x = x;
				this.y = y;
			}
		}

	}

}