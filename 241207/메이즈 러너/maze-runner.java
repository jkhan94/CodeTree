/*
미로
    N*N 격자 (r,c)
    좌상단은 (1,1)
    아래로 갈수록 r이 증가, 오른쪽으로 갈수록 c가 증가
    빈칸 : 이동 가능
    벽 : 이동 불가/ 1이상 9이하 내구도/ 회전 시 내구도-1/ 내구도 0되면 빈칸으로 변경
    출구: 참가자 도착 시 즉시 탈출

// 1. M명의 참가자 이동
//     1초마다 전원 1칸씩 이동
//     이동 조건
//         두 위치 (x1,y1), (x2,y2)의 최단거리는 ∣x1−x2∣+∣y1−y2∣
//         움직인 칸은 현위치보다 출구까지의 최단 거리 작아야 함
//         방향: 상하좌우 빈칸. 상하 우선. 행, 열 순.
//         전원 동시에 이동.
//         1칸에 2명 이상 가능
//         이동 불가 시 이동 안 함 = 가까워지지 않으면 이동 안 함
        
// 2. 미로 회전
//     한 명 이상의 참가자와 출구를 포함한 가장 작은 정사각형 잡음
//     정사각형이 2개 이상이면 최상단 r 작은 것 우선. r 같으면 c 작은 거 우선
//     선택된 정사각형은 시계방향으로 90도 회전.
//     회전된 벽은 내구도 -1

K초(턴) 동안 위의 과정을 계속 반복
K초 전에 모든 참가자 탈출 시 게임 끝
모든 참가자들의 이동 거리 합과 출구 좌표를 출력

// 입력
// 1. 첫 번째 줄에 N, M, K
// 2. N개의 줄에 걸쳐서 N×N 크기의 미로에 대한 정보. 0빈칸, 1-9는 벽 내구도
// 3. M개의 줄에 참가자 좌표. 초기에는 전원 빈칸에 존재.
// 4. 출구 좌표. 출구는 빈 칸에만 지정, 참가자 좌표와 겹치지 않음

// N: 미로의 크기 (4 ≤ N ≤ 10)
// M: 참가자 수 (1 ≤ M ≤ 10)
// K: 게임 시간 (1 ≤ K ≤100)

// 출력
// 게임 시작 후 K초가 지났거나 || 모든 참가자가 미로를 탈출했을 때, 
// 모든 참가자들의 이동 거리 합과 출구 좌표를 다른 줄에 출력
*/
import java.util.*;
public class Main {
	public static final int MAX_N = 10; // N: 미로의 크기 (4 ≤ N ≤ 10). 좌측상단(1,1)
	public static final int MAX_M = 10; // M: 참가자 수 (1 ≤ M ≤ 10)
	// K: 게임 시간 (1 ≤ K ≤100)

	public static int n, m, k;
	public static int[][] maze = new int[MAX_N + 1][MAX_N + 1]; // 미로
	public static Pair[] runners = new Pair[MAX_M + 1];// 참가자 좌표
	public static Pair exit; // 출구 좌효

	public static int[][] rotate = new int[MAX_N + 1][MAX_N + 1]; // 회전할 정사각형
	public static int mr, mc, length; // rotate의 왼쪽상단 좌표 (mr, mc), 한 변 길이 length

	public static int distSum = 0; // 모든 참가자들의 이동 거리 합

	public static void main(String[] args) {
		// 입력
		Scanner sc = new Scanner(System.in);
		// 1. 첫 번째 줄에 N, M, K
		n = sc.nextInt();
		m = sc.nextInt();
		k = sc.nextInt();
		// 2. N개의 줄에 걸쳐서 N×N 크기의 미로에 대한 정보. 0빈칸, 1-9는 벽 내구도
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= n; j++) {
				maze[i][j] = sc.nextInt();
			}
		}
		// 3. M개의 줄에 참가자 좌표. 초기에는 전원 빈칸에 존재.
		int r = 0, c = 0;
		for (int i = 1; i <= m; i++) {
			r = sc.nextInt();
			c = sc.nextInt();
			runners[i] = new Pair(r, c);
		}
		// 4. 출구 좌표. 출구는 빈 칸에만 지정, 참가자 좌표와 겹치지 않음
		r = sc.nextInt();
		c = sc.nextInt();
		exit = new Pair(r, c);

		for (int a = 0; a < k; a++) {
//		1. M명의 참가자 이동
//	     1초마다 전원 1칸씩 이동
//	     이동 조건
//	         두 위치 (x1,y1), (x2,y2)의 최단거리는 ∣x1−x2∣+∣y1−y2∣
//	         움직인 칸은 현위치보다 출구까지의 최단 거리 작아야 함
//        	 이동 불가 시 이동 안 함 = 최단 거리로 이동 불가 시 이동 안 함
//	         방향: 상하좌우 빈칸. 상하(행) 우선 dr 0 0 -1 1 / dc-1 1 0 0
//	         전원 동시에 이동. 1칸에 2명 이상 가능.
			for (int i = 1; i <= m; i++) {
				// 이미 탈출했으면 고려 안 함
				if (runners[i].r == exit.r && runners[i].c == exit.c) {
					continue;
				}

				// 러너와 출구의 행이 다르면 행 방향 1칸 이동
				if (runners[i].r != exit.r) {
					int nr = runners[i].r;
					int nc = runners[i].c;

					if (exit.r > nr) { // 만약 출구가 더 아래에 있으면
						nr++; // 행 증가
					} else {
						nr--; // 아니면 행 감소
					}

					// 빈 칸이면 좌표 변경 후 다음 러너로 이동
					if (maze[nr][nc] == 0) { // 이동할 칸이 빈칸이면
						runners[i].r = nr; // 이동함
						runners[i].c = nc;
						distSum++; // 이동 거리 누적합
						continue; // 이동했으므로 다음 러너로 이동
					}
				}

				// 러너와 출구의 행이 같으면 열 방향 1칸 이동
				if (runners[i].c != exit.c) {
					int nr = runners[i].r;
					int nc = runners[i].c;

					if (exit.c > nc) { // 만약 출구가 더 오른래에 있으면
						nc++; // 열 증가
					} else {
						nc--; // 아니면 열 감소
					}

					// 빈 칸이면 좌표 변경 후 다음 러너로 이동
					if (maze[nr][nc] == 0) { // 이동할 칸이 빈칸이면
						runners[i].r = nr; // 이동함
						runners[i].c = nc;
						distSum++; // 이동 거리 누적합
					}
				}
			}

			// 1-1. 이동 후 전원 탈출했는지 확인
			// 다 탈출했으면 미로 회전 불필요. 연산 줄일 수 있음
			// 러너의 좌표와 출구의 좌표가 한 명이라도 다르면 false로 설정 후 탈출
			boolean isAllEscaped = true; // flag
			for (int i = 1; i <= m; i++) {
				if (runners[i].r != exit.r || runners[i].c != exit.c) {
					isAllEscaped = false;
					break;
				}
			}
			if (isAllEscaped) {
				break; // 전원 탈출했으므로 종료
			}

//		 2. 미로 회전
//	     	2-1. 한 명 이상의 참가자와 출구를 포함한 가장 작은 정사각형 잡음
//	     		 정사각형이 2개 이상이면 최상단 작은 것 우선. r 같으면 c 작은 거 우선
			// 참가자와 출구를 기준으로 범위를 정하기엔 참가자 수가 유동적이라 애매
			// 만들 수 있는 작은 정사각형부터 만들어보면서 참가자 1명 이상, 출구 포함했는지 검사하는 게 나을 듯
			// (mr,mc) ...
			// .. (mr+length-1, mc+length-1)
			outer: for (int length1 = 2; length1 <= n; length1++) {
				for (int mr1 = 1; mr1 <= n - length1 +1; mr1++) {
					for (int mc1 = 1; mc1 <= n - length1+1; mc1++) {
						// 좌상단, 우하단 내에 출구 없으면 패스
						if (!(mr1 <= exit.r && exit.r <= mr1 + length1-1 && mc1 <= exit.c && exit.c <= mc1 + length1-1)) {
							continue; // break하면 남은 열도 검사 안 해서 안 됨
						}
						// 참가자가 있는데 출구에 있는 것이 아니면 참가자가 영역 내에 있다고 봄
						boolean isRunnerIn = false;
						for (int i = 1; i <= m; i++) {
							if (mr1 <= runners[i].r && runners[i].r <= mr1 + length1-1 && mc1 <= runners[i].c
									&& runners[i].c <= mc1 + length1-1) {
								if (!(runners[i].r == exit.r && runners[i].c == exit.c)) {
									isRunnerIn = true;
								}
							}
						}
						// 참가자가 있다면 정사각형 최상단 좌표, 길이 저장
						if (isRunnerIn) {
							mr = mr1;
							mc = mc1;
							length = length1;
							break outer;
						}
					}
				}
			}

//	     	2-2. 선택된 정사각형은 시계방향으로 90도 회전.
//	     		 회전된 벽은 내구도 -1
			for (int i = mr; i < mr + length; i++) {
				for (int j = mc; j < mc + length; j++) {
					if (maze[i][j] > 0) {
						maze[i][j]--;
					}
				}
			}
			// 선택한 정사각형 회전.
			// 원점 기준 시계방향 90도 회전 공식: (x,y)→(y,squreLength−1−x)
			// 회전 시 기존x는 경계에서부터 x만큼 떨어져있으므로 최대인덱스-x가 인덱스값, 기존 y는 원점으로부터 y만큼 떨어진거라 인덱스 값
			for (r = mr; r < mr + length; r++) { // 좌측 상단부터
				for (c = mc; c < mc + length; c++) {
					// 기준점 (mr. mc)를 원점으로 변경
					int or = r - mr;
					int oc = c - mc;
					// 원점 기준 좌표 회전
					int nr = oc;
					int nc = length - 1 - or;
					// 기준점을 원점에서 좌측상단으로 이동
					rotate[nr + mr][nc + mc] = maze[r][c];
				}
			}
			// 회전된 격자를 미로에 반영
			for (r = mr; r < mr + length; r++) { // 좌측 상단부터
				for (c = mc; c < mc + length; c++) {
					maze[r][c] = rotate[r][c];
				}
			}
			// 출구 회전
			r = exit.r;
			c = exit.c;
			if (mr <= r && r < mr + length && mc <= c && c < mc + length) { // 출구가 격자 내에 있으면
				// 원점으로 변경
				int or = r - mr;
				int oc = c - mc;
				// 회전
				int nr = oc;
				int nc = length - 1 - or;
				// 반영
				exit.r = nr + mr;
				exit.c = nc + mc;
			}
			// 참가자 회전
			for (int i = 1; i <= m; i++) {
				r = runners[i].r;
				c = runners[i].c;
				// 격자 내 러너면 회전
				if (mr <= r && r < mr + length && mc <= c && c < mc + length) {
					// 원점으로 변경
					int or = r - mr;
					int oc = c - mc;
					// 회전
					int nr = oc;
					int nc = length - 1 - or;
					// 반영
					runners[i].r = nr + mr;
					runners[i].c = nc + mc;
				}
			}

		}
		// 출력
		// 게임 시작 후 K초가 지났거나 || 모든 참가자가 미로를 탈출했을 때,
		// 모든 참가자들의 이동 거리 합과 출구 좌표를 다른 줄 출력
		System.out.println(distSum);
		System.out.println(exit.r + " " + exit.c);

		sc.close();
	}

// 좌표 클래스
	static class Pair {
		int r, c;

		public Pair(int r, int c) {
			this.r = r;
			this.c = c;
		}
    }
}