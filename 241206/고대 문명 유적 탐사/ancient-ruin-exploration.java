/*
5*5 격자, 1~7까지 유물 조각

회전
    3*3격자를 선택해 90, 180, 270도 회전 가능. 선택하면 항상 회전.
    1. 유물 1차 획득 가치 최대화
    2. 회전 각도 최소화
    3. 회전 중심 좌표의 열 최소
    4. 열이 같다면 행 최소

획득
    1차 유물 획득
    상하좌우 인접 시 연결되어 있음.
    3개 이상 연결 시 유물이 되어 사라짐 (숫자가 사라짐)
    유물 가치 = 모인 조각의 개수

새 조각 채우기
    열 작은 순(왼>오) > 행 큰 순(아래>위)
    벽면에 적힌 수는 매우 많음. 한 번 사용한 숫자는 사라짐.
    삽입된 순서대로 나와야 함(선입선출)

총 K 번의 턴. (1턴 = 회전 > 1차 획득 > 연쇄 획득(2차 이후)
각 턴마다 획득한 유물의 가치의 총합을 출력
3개 이상 연결 안 되면 유물 생성 종료 -> 프로그램 종료(return). 종료 턴에는 출력X
*회전 전에는 유물이 없으나, 회전하면 항상 유물 생김.

// 입력
1. 탐사의 반복 횟수 K와 벽면에 적힌 유물 조각의 개수 M
2. 5개의 줄에 걸쳐 유물의 각 행에 있는 유물 조각에 적혀 있는 숫자들
3. 벽면에 적힌 M개의 유물 조각 번호

// 출력
한 줄에 각 턴 마다 획득한 유물의 가치의 총합 공백을 두고.
*/
import java.util.*;
public class Main {
    	static final int N_large = 5; // 고대 문명 전체 격자 크기
	static final int N_small = 3; // 회전 격자의 크기

	public static void main(String[] args) {
		// 입력
		Scanner sc = new Scanner(System.in);
		// 1. 탐사의 반복 횟수 K와 벽면에 적힌 유물 조각의 개수 M
		int K = sc.nextInt();
		int M = sc.nextInt();
		// 2. 5개의 줄에 걸쳐 유물의 각 행에 있는 유물 조각에 적혀 있는 숫자들
		Board board = new Board();
		for (int i = 0; i < N_large; i++) {
			for (int j = 0; j < N_large; j++) {
				board.a[i][j] = sc.nextInt();
			}
		}
		// 3. 벽면에 적힌 M개의 유물 조각 번호
		Queue<Integer> q = new LinkedList<>(); // 큐는 인터페이스. 주로 연결리스트로 구현.
		for (int i = 0; i < M; i++) {
			q.offer(sc.nextInt());
		}

		// 최대 K번의 탐사과정. 회전 > 1차 획득 > 칸 채우고 > 2차 획득 > 칸 채우고 > 3차 획득...
		while (K-- > 0) {
			int maxScore = 0;
			Board maxScoreBoard = null;
			// 회전 목표에 맞는 결과를 maxScoreBoard에 저장
			// (1) 유물 1차 획득 가치(점수)를 최대화: if문으로 구현되어 최우선 됨
			// (2) 회전 각도 최소화: 작은 각도부터 회전함
			// (3) 회전 중심 좌표의 열 최소화 > 열이 같다면 행 최소화 : 열, 행 순으로 순회하므로 우선순위 유지
			for (int cnt = 1; cnt <= 3; cnt++) {
				for (int sx = 0; sx <= N_large - N_small; sx++) {
					for (int sy = 0; sy <= N_large - N_small; sy++) {
						Board rotated = board.rotate(sy, sx, cnt); // 회전 
						int score = rotated.calScore(); // 1차 획득
						if (maxScore < score) {
							maxScore = score;
							maxScoreBoard = rotated;
						}
					}
				}
			}

			// 회전을 통해 유물을 획득할 수 없으면 탐사 종료
			if (maxScoreBoard == null) {
				break;
			}
			board = maxScoreBoard;

			// 유물의 연쇄 획득 (2차 이후)
			while (true) {
				board.fill(q); // 유물 조각 채우고
				int newScore = board.calScore(); // n차 획득
				if (newScore == 0) {
					break; // 못 찾으면 종료
				}
				maxScore += newScore;
			}

			// 출력: 한 줄에 각 턴 마다 획득한 유물의 가치의 총합 공백을 두고.
			System.out.print(maxScore + " ");
		}

		sc.close();
	}

	// 격자
	static class Board {
		int[][] a = new int[N_large][N_large];

		// 생성자: 격자의 유물 조각들 0으로 초기화
		public Board() {
			for (int i = 0; i < N_large; i++) {
				for (int j = 0; j < N_large; j++) {
					a[i][j] = 0;
				}
			}
		}

		// 주어진 y, x가 고대 문명 격자의 범위 안에 있는지 확인
		private boolean inRange(int y, int x) {
			return 0 <= y && y < N_large && 0 <= x && x < N_large;
		}

		// 현재 격자에서 sy, sx를 좌측상단으로 하여 시계방향 90도 회전을 cnt번 시행했을 때 결과
		public Board rotate(int sy, int sx, int cnt) {
			Board result = new Board();
			for (int i = 0; i < N_large; i++) {
				for (int j = 0; j < N_large; j++) {
					result.a[i][j] = this.a[i][j]; // this.a는 main 메소드의 board
				}
			}

			// sy, sx를 좌측상단으로 하여 시계방향 90도 회전
			// 우변 -> 좌변 좌표로 변경
			// sy, sx sy, sx + 1 sy, sx + 2
			// sy + 1, sx sy + 1, sx + 1 sy + 1, sx + 2
			// sy + 2, sx sy + 2, sx + 1 sy + 2, sx + 2
			for (int k = 0; k < cnt; k++) {
				// 모서리 4개
				int tmp = result.a[sy + 0][sx + 2];
				result.a[sy + 0][sx + 2] = result.a[sy + 0][sx + 0];
				result.a[sy + 0][sx + 0] = result.a[sy + 2][sx + 0];
				result.a[sy + 2][sx + 0] = result.a[sy + 2][sx + 2];
				result.a[sy + 2][sx + 2] = tmp;
				// 중앙 4개
				tmp = result.a[sy + 1][sx + 2];
				result.a[sy + 1][sx + 2] = result.a[sy + 0][sx + 1];
				result.a[sy + 0][sx + 1] = result.a[sy + 1][sx + 0];
				result.a[sy + 1][sx + 0] = result.a[sy + 2][sx + 1];
				result.a[sy + 2][sx + 1] = tmp;
			}
			return result;
		}

		// 현재 격자에서 유물을 획득 (새로운 유물 조각을 채우는 것은 고려X)
		// Flood fill 알고리즘: 시작점에서 조건을 만족하는 셀들로 확장하며 처리
		public int calScore() {
			int score = 0;
			boolean[][] visit = new boolean[N_large][N_large];
			int[] dy = { 0, 1, 0, -1 }, dx = { 1, 0, -1, 0 }; // 상하좌우

			for (int i = 0; i < N_large; i++) {
				for (int j = 0; j < N_large; j++) {
					if (!visit[i][j]) { // 방문되지 않은 칸(i,j)에서 BFS를 시작
						Queue<int[]> q = new LinkedList<>();
						Queue<int[]> trace = new LinkedList<>();
						q.offer(new int[] { i, j }); // BFS 탐색을 위한 큐.
						trace.offer(new int[] { i, j }); // 유물 조각의 위치를 저장해 그룹화하는 큐. Flood Fill 결과.
						visit[i][j] = true;

						// (i,j)부터 BFS 탐색 (Flood Fill 알고리즘을 사용)
						while (!q.isEmpty()) {
							int[] cur = q.poll(); // 현위치.
							for (int k = 0; k < 4; k++) {
								int ny = cur[0] + dy[k], nx = cur[1] + dx[k]; // 상하좌우 인접 셀
								// 격자 범위 내 && 유물 조각 값 동일 && 인접 셀 미방문
								if (inRange(ny, nx) && a[ny][nx] == a[cur[0]][cur[1]] && !visit[ny][nx]) {
									q.offer(new int[] { ny, nx }); // 조건 만족하는 셀들만 bfs 수행
									trace.offer(new int[] { ny, nx }); // 유물 조각 위치 저장
									visit[ny][nx] = true;
								}
							}
						}

						// 위에서 진행된 Flood Fill을 통해 조각들이 모여 유물이 되고 사라지는지 확인
						if (trace.size() >= 3) {
							score += trace.size(); // 유물이 되어 사라지는 경우 가치를 score에 더함
							while (!trace.isEmpty()) {
								int[] t = trace.poll();
								a[t[0]][t[1]] = 0; // 유물이 되어 사라지므로 0으로 변경
							}
						}
					}
				}
			}
			return score;
		}

		// 유물 획득과정에서 조각이 비어있는 곳에 새로운 조각을 채워줍니다.
		public void fill(Queue<Integer> que) {
			// j열이 작고 i행이 큰 우선순위로 채움.
			for (int j = 0; j < N_large; j++) {
				for (int i = N_large - 1; i >= 0; i--) {
					if (a[i][j] == 0 && !que.isEmpty()) {
						a[i][j] = que.poll();
					}
				}
			}
		}
	}
}