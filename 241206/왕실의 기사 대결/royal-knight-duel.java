/*
L×L 크기의 체스판
    왼쪽 상단은 (1,1)
    각 칸은 빈칸, 함정, 또는 벽
    체스판 밖은 벽

기사
    각 기사의 초기위치는 (r,c)를 좌측상단, 높이h * 너비w 직사각형
    체력 k

기사의 이동
    상하좌우 1칸 이동
    이동하려는 위치에 다른 기사가 있다면 함께 연쇄적으로 밀림. 
    기사의 이동방향 끝에 벽이 있다면 모든 기사는 이동 불가.
    체스판에서 사라진 기사에겐 명령 불가
대결 대미지
    해당 기사가 이동한 곳에서 w×h 직사각형 내에 놓여 있는 함정의 수만큼만 피해
    체력 k - 기사 범위 내 함정 수
    현재 체력 이상의 데미지를 받으면 체스판에서 사라짐
    명령을 받은 기사는 데미지 없음
    데미지는 모두 밀린 후에 받음.
    밀렸더라도 함정이 없으면 데미지 없음.

Q 번의 대결이 모두 끝난 후 생존한 기사들이 총 받은 대미지의 합을 출력

// 입력
// 1. 첫 번째 줄에 L, N, Q
// 2. L개의 줄에 체스판 정보. 0 빈칸, 1 함정, 2 벽
// 3. N 개의 줄에 기사들의 초기 정보 (r,c,h,w,k) 1~N. 좌측상단, 세로, 가로, 초기 체력
//    처음 위치는 기사끼리 겹치거나, 기사와 벽이 겹치지 않음.
// 4. Q 개의 줄에 명령 (i,d). i번 기사에게 방향 d로 한 칸 이동
//    이미 사라진 기사 번호 주어질 수 있음.
//    d 0123  상우하좌

// 출력
// Q 개의 명령이 진행된 이후, 생존한 기사들이 총 받은 대미지의 합

*/
import java.util.*;
public class Main {
    	// 기사 인덱스가 1부터 시작. 따라서 최대값+1
	public static final int MAX_N = 31;
	public static final int MAX_L = 41;

	public static int l, n, q;
	public static int[][] board = new int[MAX_L][MAX_L]; // 체스판
	public static int[] initial_k = new int[MAX_N];
	public static int[] r = new int[MAX_N], c = new int[MAX_N], h = new int[MAX_N], w = new int[MAX_N],
			k = new int[MAX_N];
	public static int[] nr = new int[MAX_N], nc = new int[MAX_N];
	public static int[] dmg = new int[MAX_N];
	public static boolean[] is_moved = new boolean[MAX_N];

	public static int[] dx = { -1, 0, 1, 0 }, dy = { 0, 1, 0, -1 }; // 상우하좌

	public static void main(String[] args) {
		// 입력
		Scanner sc = new Scanner(System.in);
		// 1. 첫 번째 줄에 L, N, Q
		l = sc.nextInt(); // 체스판 크기
		n = sc.nextInt(); // 기사 수
		q = sc.nextInt(); // 명령 수
		// 2. L개의 줄에 체스판 정보. 0 빈칸, 1 함정, 2 벽
		for (int i = 1; i <= l; i++) {
			for (int j = 1; j <= l; j++) {
				board[i][j] = sc.nextInt();
			}
		}
		// 3. N 개의 줄에 기사들의 초기 정보 (r,c,h,w,k) 1~N. 좌측상단, 세로, 가로, 초기 체력
//		    처음 위치는 기사끼리 겹치거나, 기사와 벽이 겹치지 않음.
		for (int i = 1; i <= n; i++) {
			r[i] = sc.nextInt();
			c[i] = sc.nextInt();
			h[i] = sc.nextInt();
			w[i] = sc.nextInt();
			k[i] = sc.nextInt(); // 초기 체력. 게임 진행 때 사용.
			initial_k[i] = k[i]; // 초기 체력. 데미지 계산용.
		}

		// 4. Q 개의 줄에 명령 (i,d). i번 기사에게 방향 d로 한 칸 이동
//		    이미 사라진 기사 번호 주어질 수 있음.
//		    d 0123  상우하좌
		for (int i = 1; i <= q; i++) {
			int idx = sc.nextInt(); // 기사 번호
			int dir = sc.nextInt(); // 이동 방향
			movePiece(idx, dir); // 이동 명령
		}

		// 데미지 합 계산
		long ans = 0;
		for (int i = 1; i <= n; i++) {
			if (k[i] > 0) {
				ans += initial_k[i] - k[i];
			}
		}

		// 출력
		// Q 개의 명령이 진행된 이후, 생존한 기사들이 총 받은 대미지의 합
		System.out.println(ans);

		sc.close();
	}

	// idx번 기사를 dir 방향으로 1칸 이동
	public static void movePiece(int idx, int dir) {
		if (k[idx] <= 0) {
			return; // 체력이 없으면 패스
		}

		// 이동이 가능하면
		if (tryMovement(idx, dir)) {
			for (int i = 1; i <= n; i++) {
				r[i] = nr[i]; // 위치 변경
				c[i] = nc[i];
				k[i] -= dmg[i]; // 데미지만큼 체력 감소
			}
		}
	}

	// 움직임을 시도
	public static boolean tryMovement(int idx, int dir) {
		Queue<Integer> q = new LinkedList<>();

		// 초기화
		for (int i = 1; i <= n; i++) {
			dmg[i] = 0; // 데미지
			is_moved[i] = false; // 이동 여부
			nr[i] = r[i]; // 좌측상단 좌표
			nc[i] = c[i];
		}

		q.offer(idx);
		is_moved[idx] = true;

		// bfs
		while (!q.isEmpty()) {
			int x = q.poll(); // 조사할 기사 꺼냄

			nr[x] += dx[dir]; // 인접 칸: 상우하좌
			nc[x] += dy[dir];

			// 경계를 벗어나는지 체크 (체스판 인덱스는 1~51)
			if (nr[x] < 1 || nc[x] < 1 || nr[x] + h[x] - 1 > l || nc[x] + w[x] - 1 > l) {
				return false;
			}

			// 기사의 영역 내의 함정과 벽 검사
			// (r,c) ... 
			// ...			(r+h-1, c+w-1)
			for (int i = nr[x]; i <= nr[x] + h[x] - 1; i++) {
				for (int j = nc[x]; j <= nc[x] + w[x] - 1; j++) {
					if (board[i][j] == 1) { // 함정이면 데미지 증가
						dmg[x]++;
					}
					if (board[i][j] == 2) { // 벽이면 이동 불가하므로 메소드 종료
						return false;
					}
				}
			}

			// 다른 조각과 충돌하는 경우, 해당 조각도 같이 이동합니다.
			for (int i = 1; i <= n; i++) {
				if (is_moved[i] || k[i] <= 0) {
					continue; // 이미 이동했거나 사라진 기사
				}
				// 이동 전 행이 이동 후 행의 최대보다 크거나, 이동후 행이 이동 전 행의 최대보다 큼
				if (r[i] > nr[x] + h[x] - 1 || nr[x] > r[i] + h[i] - 1) {
					continue; // 행 범위 충돌 안 함
				}
				// 이동 전 열이 이동 후 열의 최대보다 크거나, 이동 후 열이 이동전 열의 최대보다 큼
				if (c[i] > nc[x] + w[x] - 1 || nc[x] > c[i] + w[i] - 1) {
					continue; // 열 범위 충돌 안 함
				}

				is_moved[i] = true; // 충돌한 기사를 이동했다고 표시
				q.add(i); // 충돌한 기사를 큐에 추가
			}
		}

		dmg[idx] = 0; // 명령을 받은 기사는 데미지 없으므로 초기화
		return true;
	}
}