/*
R행 C열의 격자 마법의숲. 가장 위를 1행, 가장 아래를 R행
숲의 북쪽을 통해서만 숲에 들어올 수 있음. 동서남은 막힘.

K명의 정령이 각각 골렘 타고 숲 탐색.
십자모양 골렘(총 5칸 차지) 입구는 무관, 출구는 중앙 제외 1칸.
i번째로 숲을 탐색하는 골렘은 숲의 가장 북쪽에서 시작해 
    골렘의 중앙이 c[i]열이 되도록 하는 위치에서 내려옴

골렘 이동 우선순위
남 1칸
서 1칸 > 남 1칸. 출구가 반시계방향으로 회전 +3 %4
동 1칸 > 남 1칸. 출구가 시계방향으로 회전 +1 %4

골렘 최남쪽이면 정령은 골렘 내에서 상하좌우로 이동 가능.
현 골렘 출구가 다른 골렘과 인접할 경우 이동 가능
최대한 남쪽으로 이동 후 종료.

좌표는 시계방향
 0      y-1,x-1     y-1,x    y-1,x+1
3 1     y,x-1       y,x      y,x+1
 2                  y+1,x    y+1,x+1

입력
1. 숲의 크기를 의미하는 R, C, 정령의 수 K
2. K개의 줄에 거쳐 각 골렘이 출발하는 열 c(중앙 칸 위치), 
    골렘의 출구 방향 정보 d(0,1,2,3 북동남서)
 
출력
1. 첫번째 줄에 각 정령들이 최종적으로 위치한 행의 총합
골렘이 숲 밖에 있을 경우 보드 초기화. 헤당 턴에는 행 계산 안 함.
숲이 다시 텅 비게 돼도 행의 총합은 누적됨.
*/
import java.util.*;
public class Main {
    	private static final int MAX_L = 70; // R,C 최대값

	private static int R, C, K; // 행, 열, 골렘의 개수를 의미합니다
	private static int[][] A = new int[MAX_L + 3][MAX_L]; // 행+3개 하여 실제 숲을 [3~R+2][0~C-1]로 사용. 골렘id 저장.
	private static int[] dy = { -1, 0, 1, 0 }, dx = { 0, 1, 0, -1 }; // 북동남서
	private static boolean[][] isExit = new boolean[MAX_L + 3][MAX_L]; // 해당 칸이 골렘의 출구인지 저장
	private static int answer = 0; // 각 정령들이 도달할 수 있는 최하단 행의 총합

	public static void main(String[] args) {
	//		입력
		Scanner sc = new Scanner(System.in);
//		1. 숲의 크기를 의미하는 R, C, 정령의 수 K
		R = sc.nextInt();
		C = sc.nextInt();
		K = sc.nextInt();
//		2. K개의 줄에 거쳐 각 골렘이 출발하는 열 x(중앙 칸 위치), 골렘의 출구 방향 정보 d(0,1,2,3 북동남서)
        for (int id = 1; id <= K; id++) { // 골렘 번호 id
            int x = sc.nextInt() - 1;
            int d = sc.nextInt();
            down(0, x, d, id);
        }
//		출력
//		1. 첫번째 줄에 각 정령들이 최종적으로 위치한 행의 총합
//		골렘이 숲 밖에 있을 경우 보드 초기화. 헤당 턴에는 행 계산 안 함.
//		숲이 다시 텅 비게 돼도 행의 총합은 누적됨.
		System.out.println(answer);

        sc.close();
	}

	// 골렘id가 중심 (y, x), 출구의 방향이 d일때 규칙에 따라 움직임을 취하는 함수입니다
	// 1. 아래: 남쪽으로 1칸
	// 2. 왼쪽 아래: 서 회전 > 남 1칸. 출구가 반시계방향으로 이동 +3 %4
	// 3. 오른쪽 아래: 동 회전 > 남 1칸. 출구가 시계방향으로 이동 +1 %4
	private static void down(int y, int x, int d, int id) {
		if (canGo(y + 1, x)) {
			down(y + 1, x, d, id); // 아래로
		} else if (canGo(y + 1, x - 1)) {
			down(y + 1, x - 1, (d + 3) % 4, id); // 왼쪽 아래. 서쪽 회전
		} else if (canGo(y + 1, x + 1)) {
			down(y + 1, x + 1, (d + 1) % 4, id); // 오른쪽 아래. 동쪽 회전
		} else { 
			// 1, 2, 3의 움직임을 모두 취할 수 없을 떄
			// 중심 기준 좌상단 또는 우하단이 범위 밖이면
			if (!inRange(y - 1, x - 1) || !inRange(y + 1, x + 1)) {
				resetMap(); // 모든 골렘이 숲을 빠져나갑니다
			} else {
				// 골렘이 숲 안에 있지만 더 이상 움직일 수 없으면 골렘 위치 기록 = 못 가는 곳 
				A[y][x] = id; // 골렘 중심
				for (int k = 0; k < 4; k++) {
					A[y + dy[k]][x + dx[k]] = id; // 골렘 중심 기준 북동남서
				}	
				isExit[y + dy[d]][x + dx[d]] = true; // 골렘의 출구를 기록
				answer += bfs(y, x) - 3 + 1; // bfs를 통해 정령이 최대로 내려갈 수 있는 행를 계산하여 누적합
			}
		}
	}

	// 골렘의 중심이 y, x에 위치할 수 있는지 확인
	// 북쪽에서 남쪽으로 내려와야하므로, 이동할 중심(y, x)과 이동 전 중심(y-1, x)에 위치할 때 골렘이 범위 내인지 확인
	// 1<= x <= C-2
	private static boolean canGo(int y, int x) {
		boolean flag = 0 <= x - 1 && x + 1 < C && y + 1 < R + 3;
		flag = flag && (A[y - 1][x - 1] == 0); // 좌상단
		flag = flag && (A[y - 1][x] == 0);     // 상단
		flag = flag && (A[y - 1][x + 1] == 0); // 우상단
		flag = flag && (A[y][x - 1] == 0);     // 좌측
		flag = flag && (A[y][x] == 0);         // 중심
		flag = flag && (A[y][x + 1] == 0);     // 우측
		flag = flag && (A[y + 1][x] == 0);     // 하단
		return flag;
	}

	// (y, x)가 숲의 범위 안에 있는지 확인
	private static boolean inRange(int y, int x) {
		return 3 <= y && y < R + 3 && 0 <= x && x < C;
	}
	
	// 숲에 있는 골렘들이 모두 빠져나갑니다
		private static void resetMap() {
			for (int i = 0; i < R + 3; i++) {
				for (int j = 0; j < C; j++) {
					A[i][j] = 0;
					isExit[i][j] = false;
				}
			}
		}

	// 정령이 움직일 수 있는 모든 범위를 확인하고 도달할 수 있는 최하단 행을 반환
	private static int bfs(int y, int x) {
		int result = y; // 현위치를 최하단 행으로 설정
		Queue<int[]> q = new LinkedList<>();
		boolean[][] visit = new boolean[MAX_L + 3][MAX_L];
		
		q.offer(new int[] { y, x }); // 현재 중심을 큐에 삽입
		visit[y][x] = true;
		
		while (!q.isEmpty()) {
			int[] cur = q.poll();// 현위치
			for (int k = 0; k < 4; k++) { // 북동남서 순으로 탐색
				int ny = cur[0] + dy[k]; // ny,nx는 정령이 가려는 위치
				int nx = cur[1] + dx[k]; 
				
				if (inRange(ny, nx) // 정령이 가려는 위치는 숲 내부이고,
						&& !visit[ny][nx] // 방문하지 않았고, (이미 방문했으면 안 가도 됨)
						&& (A[ny][nx] == A[cur[0]][cur[1]] // 이동할 위치와 현위치에 저장된 골렘 id 동일(=골렘 내부)거나
								|| (A[ny][nx] != 0 && isExit[cur[0]][cur[1]]))) { // 다음 위치에 골렘id가 있고, 현위치가 탈출구면
					q.offer(new int[] { ny, nx }); // 큐에 다음 위치 저장
					visit[ny][nx] = true;
					result = Math.max(result, ny); // 저장된 최하단 행과 비교해서 더 큰 값으로 변경
				}
			}
		}
		return result;
	}
}