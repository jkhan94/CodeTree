/*
산 사이 이동은 현위치보다 오른쪽, 더 높은 산으로만 가능.
케이블카 특정 산에만 있음. 높이 무관, 현위치 포함 임의의 산으로 이동. 이후 이동 규칙은 동일.

1. 시작 산 자유.
2. 오른쪽 높은 산으로 이동 시 +1,000,000
3. 케이블카 가능하면 케이블카 타고 이동. 성공 시 +1,000,000
4. 케이블카 이후 이동 성공 시 +1,000,000
5. 이동 끝이면 +최종적으로 위치한 산 높이
총점 = 2-5 

명령의 개수 Q
100 n개의_산 산높이...: 항상 첫번째 명령으로 주어짐.
200 h : 오른쪽 끝에 높이 h 산 추가
300 : 가장 오른쪽 산 제거
400 m : 케이블카 이용 가능한 산이 왼쪽에서 m번째일 때 최대 점수 출력.
200 300 연달아 나오면 오른쪽 산 추가 후 제거

출력 개수 = 400 입력 수
이동수가 최대여야 최대값.
케이블카 타고 가장 작은 높이, 왼쪽에 있는 산으로 이동.
가장 마지막 산은 오른쪽에서 가장 높은 산.

케이블카 왼쪽 가장 작은 수에서 시작.
오른쪽으로 이동하면서 케이블카 높이보다 작은 산들 방문
케이블카를 타고 왼쪽 가장 작은 산으로 이동(시작점으로 이동)
오른쪽에 있는 값 중 가장 큰 값이 마지막 산.
이동하면서 작은 순서대로 방문.

예제 정리
41324 : 3. 13 134 4번+4
41324 : 1. 1 124 3번+4
532543 : 4. 24 25 3번+5 / 뒤5. 25 25 3번+5 / 뒤3. 23 25 3번+5
532578 : 400 4 > 7. 257 2578  6번+8
532578531 10 8 : 400 10 > 마지막 8. 2578 2578 10 8번+10

추가
4
100 4 1 3 2 3 4
400 2
1 1234 4번+4
*/
import java.util.*;

public class Main {
	public static void main(String[] args) {
		int score = 0;
		int success = 1000000;
		Scanner sc = new Scanner(System.in);

		LinkedList<Integer> bigBang = new LinkedList<>();

		int Q = sc.nextInt();
		int round = 0;
		int cableCar = 0;
		int cableCarLoc = 0;
		int start = 0;
		int startLoc = 0;
		int temp = 0;
		int max = 0;
		int maxLoc = 0;

		sc.nextLine();

		while (round < Q) {
			String[] input = sc.nextLine().split(" ");
			int[] command = new int[input.length];
			for (int i = 0; i < input.length; i++) {
				command[i] = Integer.parseInt(input[i]);
			}

			boolean bigBangFlag = false;

			switch (command[0]) {
			case 100:
				if (bigBangFlag == true) {
					break;
				}
				for (int i = 1; i < command.length; i++) {
					bigBang.add(command[i]);
				}
				bigBangFlag = true;
				break;
			case 200:
				bigBang.add(command[1]);
				break;
			case 300:
				bigBang.remove(bigBang.size() - 1);
				break;
			case 400:
				score = 0;
				cableCarLoc = command[1];
				cableCar = bigBang.get(cableCarLoc);

				// 시작점
				start = bigBang.get(0);
				for (int i = 0; i <= cableCarLoc; i++) {
					temp = bigBang.get(i);
					if (temp <= cableCar && start > temp) {
						start = temp;
						startLoc = i;
						break;
					}
				}

				// 케이블카 높이보다 작은 산들
				if (startLoc == cableCarLoc) {
					score += success;
				} else {
					for (int i = startLoc; i < cableCarLoc; i++) {
						temp = bigBang.get(i);
						if (start < temp && temp < cableCar) {
							score += success;
						}
					}
					// 케이블카로 이동 후 케이블카에서 시작점으로 이동
					score += 2 * success;
				}

				// 시작점으로부터 최대 방문수 계산
				max = Collections.max(bigBang);
				maxLoc = bigBang.lastIndexOf(max);
				for (int i = startLoc; i < maxLoc; i++) {
					temp = bigBang.get(i);
					if (start < temp && temp <= max && temp < bigBang.get(i + 1)) {
						score += success;
						start = temp;
					}
				}
				// 마지막 산
				score += success;
				score += max;

				System.out.println(score);
				break;
			}

			round++;
		}

		sc.close();

	}
}
