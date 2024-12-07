/*
N*M격자, 모든 칸에 포탑 있음

포탑
    공격력 초기값이 0일 수도 있음.
    최초에 부서지지 않은 포탑은 최소 2개 이상 존재
K턴
1. 공격자 선정
    부서지지 않은 포탑 중 가장 약한 포탑. 공격력(+ (M+N))
    약한 포탑 우선순위
        공격력 최소 포탑> 가장 최근에 공격한 포탑> 행+열 최대 > 열 최대
2. 공격자의 공격
    자신 제외 가장 강한 포탑 공격
    강한 포탑 우선순위
        공격력 최대> 공격한지 가장 오래된> 행+열 합 최소 > 열 최소
    2-1. 레이저 공격        
        공격자-공격대상 최단 경로. 우하좌상 순.
        부서진 포탑 위치는 못 지나감(주변이 전부 0이면 이동 불가)
        가장자리 지나면 반대편으로 나옴. (2,3) -> (2,4) -> (2,1)         
        공격대상의 공격력 - 공격자의 공격력
        경로에 있는 포탑의 공격력 - 공격자의 공격력/2
    2-2. 포탄 공격
        레이저 공격 최단거리가 없으면 시도
        공격대상 공격력 - 공격자의 공격력
        주위 8개 공격력 - 공격자의 공격력/2
        가장자리 공격시 반대편에도 영향 있음.
        공격자는 영향 없음
3. 포탑 부서짐
    공격력=0이면 부서짐 (공격불가)
4. 포탑 정비
    안 부서지거나, 공격자 아니거나, 공격 안 받은 포탑의 공격력+1

안 부서진 포탑 1개면 즉시 종료

입력
1. 첫 번째 줄에 N, M, K
2. N개의 줄에 걸쳐서 N×M 격자

4≤N,M≤10
1≤K≤1,000
0≤공격력≤5,000

출력
첫 번째 줄에 K번의 턴이 종료된 후 남아있는 포탑 중 가장 강한 포탑의 공격력
*/
import java.util.*;
public class Main {
    	// 4 ≤ N,M ≤10
	// 1 ≤ K ≤1,000
	// 0 ≤ 공격력 ≤5,000
	public static final int MAX_N = 10;

	public static int[] dx = new int[] { 0, 1, 0, -1 }; // 하상
	public static int[] dy = new int[] { 1, 0, -1, 0 }; // 우좌
	public static int[] dx2 = new int[] { 0, 0, 0, -1, -1, -1, 1, 1, 1 };
	public static int[] dy2 = new int[] { 0, -1, 1, 0, -1, 1, 0, -1, 1 };

	public static int n, m, k;
	public static int turn;

	public static int[][] board = new int[MAX_N][MAX_N]; // 포탑 공격력
	public static int[][] rec = new int[MAX_N][MAX_N]; // 포탑의 마지막 공격 시점

	// 레이저 공격
	public static boolean[][] vis = new boolean[MAX_N][MAX_N]; // 방문여부
	public static int[][] backX = new int[MAX_N][MAX_N]; // 경로 상의 포탑 저장
	public static int[][] backY = new int[MAX_N][MAX_N];

	public static boolean[][] isActive = new boolean[MAX_N][MAX_N]; // 공격 무관 여부

	public static ArrayList<Turret> liveTurret = new ArrayList<>(); // 살아있는 포탑

	public static void main(String[] args) {
//		입력
		Scanner sc = new Scanner(System.in);
//		1. 첫 번째 줄에 N, M, K
		n = sc.nextInt();
		m = sc.nextInt();
		k = sc.nextInt();
//		2. N개의 줄에 걸쳐서 N×M 격자
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				board[i][j] = sc.nextInt();
			}
		}

//		K턴
		while (k-- > 0) {
			// 격자 정보를 바탕으로 공격 가능한 포탑만 배열리스트에 저장
			liveTurret = new ArrayList<>();
			for (int i = 0; i < n; i++)
				for (int j = 0; j < m; j++)
					if (board[i][j] > 0) {
						Turret newTurret = new Turret(i, j, rec[i][j], board[i][j]);
						liveTurret.add(newTurret);
					}

//			안 부서진 포탑 1개면 즉시 종료
			if (liveTurret.size() <= 1) {
				break;
			}

			// 턴을 진행하기 전 필요한 전처리를 정리해줍니다.
			turn++; // 공격 시전 저장용 턴 수 = 시점
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					vis[i][j] = false; // 레이저 공격 bfs 방문 여부 초기화
					isActive[i][j] = false; // 공격자, 공격대상, 공격받음 여부 초기화
				}
			}

//			1. 공격자 선정			
			Collections.sort(liveTurret); // 살아있는 포탑을 오름차순 정렬
//		    부서지지 않은 포탑 중 가장 약한 포탑이 배열 맨 앞. 
			Turret weakTurret = liveTurret.get(0);

			int x = weakTurret.x;
			int y = weakTurret.y;
			board[x][y] += n + m; // 공격력(+ (M+N))
			rec[x][y] = turn; // 공격 시점 갱신
			weakTurret.p = board[x][y]; // 공격자의 공격력 저장
			weakTurret.r = rec[x][y]; // 공격자의 공격시점 저장
			isActive[x][y] = true; // 공격자이므로 연관 있는 포탑으로 설정

			liveTurret.set(0, weakTurret); // 가장 약한 포탑 갱신

//			2. 공격자의 공격
//	       weakTurret = liveTurret.get(0); 
			// 공격자 포탑 위치, 공격력
			int sx = weakTurret.x;
			int sy = weakTurret.y;
			int pow = weakTurret.p;

			// 정렬된 살아있는 포탑중 맨 뒤가 공격력 최대 = 공격 대상
			// 자신 제외 가장 강한 포탑 공격
			Turret strongTurret = liveTurret.get(liveTurret.size() - 1);
			int ex = strongTurret.x;
			int ey = strongTurret.y;

//		    2-1. 레이저 공격        
			boolean canAttack = false; // 가장 강한 포탑에게 도달 가능 여부
			Queue<Pair> q = new LinkedList<>();
			vis[sx][sy] = true;
			q.add(new Pair(sx, sy)); // 약한 포탑부터 거리 계산

			// bfs를 통해 최단경로를 관리
			while (!q.isEmpty()) {
				x = q.peek().x;
				y = q.peek().y;
				q.poll();

				// 약한 포탑이 강한 포탑과 최단 거리가 있다면 탐색 종료
				if (x == ex && y == ey) {
					canAttack = true;
					break;
				}

//		        공격자-공격대상 최단 경로. 우하좌상 순.
//		        가장자리 지나면 반대편으로 나옴. (2,3) -> (2,4) -> (2,1) : 토르타이핑(toroidal wrapping)
				for (int dir = 0; dir < 4; dir++) {
					int nx = (x + dx[dir] + n) % n;
					int ny = (y + dy[dir] + m) % m;

					if (vis[nx][ny]) {
						continue; // 이미 방문한 포탑은 패스
					}

					if (board[nx][ny] == 0) {
						continue; // 부서진 포탑 위치는 못 지나감(주변이 전부 0이면 이동 불가)
					}

					vis[nx][ny] = true;
					// 공격 경로에 있는 포탑 좌표 저장
					backX[nx][ny] = x; // 다음 위치 인덱스에 이전 포탑 저장
					backY[nx][ny] = y;
					q.add(new Pair(nx, ny)); // 인접 포탑 추가
				}
			} // 최단 경로 탐색 끝

			// 최단 경로 있으면 공격
			if (canAttack) {
				// 1. 공격대상의 공격력 - 공격자의 공격력
				board[ex][ey] -= pow; // 공격대상
				if (board[ex][ey] < 0) {
					board[ex][ey] = 0; // 공격 받고 공격력이면 부서진 포탑으로 변경
				}
				isActive[ex][ey] = true; // 공격대상이므로 유관한 포탑

				// 2. 경로에 있는 포탑의 공격력 - 공격자의 공격력/2
				int cx = backX[ex][ey];
				int cy = backY[ex][ey];
				// 공격대상(끝점)부터 공격자(약한포탑)으로 경로 역추적
				while (!(cx == sx && cy == sy)) {
					board[cx][cy] -= pow / 2;
					if (board[cx][cy] < 0) {
						board[cx][cy] = 0;
					}
					isActive[cx][cy] = true;
					// 현위치 기준 이전 포탑
					int nextCx = backX[cx][cy];
					int nextCy = backY[cx][cy];
					// 검사할 포탑을 이전 포탑으로 설정
					cx = nextCx;
					cy = nextCy;
				}
			}

//		    2-2. 포탄 공격: 레이저 공격 최단거리가 없으면 시도
			if (!canAttack) {
				// 공격자
				sx = weakTurret.x;
				sy = weakTurret.y;
				pow = weakTurret.p;

				// 공격대상
				ex = strongTurret.x;
				ey = strongTurret.y;

				// 공격대상을 중심으로 3*3 범위 탐색하여 공격 진행
				for (int dir = 0; dir < 9; dir++) {
					// 가장자리 공격시 반대편에도 영향 있음.
					int nx = (ex + dx2[dir] + n) % n;
					int ny = (ey + dy2[dir] + m) % m;

					// 공격자는 영향 없음
					if (nx == sx && ny == sy) {
						continue;
					}
					// 공격대상 공격력 - 공격자의 공격력
					if (nx == ex && ny == ey) {
						board[nx][ny] -= pow;
						if (board[nx][ny] < 0) {
							board[nx][ny] = 0;
						}
						isActive[nx][ny] = true;
					}
					// 주위 8개 공격력 - 공격자의 공격력/2
					else {
						board[nx][ny] -= pow / 2;
						if (board[nx][ny] < 0) {
							board[nx][ny] = 0;
						}
						isActive[nx][ny] = true;
					}
				}
			}

//			3. 포탑 부서짐: 공격력=0이면 부서짐 (공격불가)
//			4. 포탑 정비: 안 부서지거나, 공격자 아니거나, 공격 안 받은 포탑의 공격력+1
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					if (isActive[i][j]) {
						continue; // 공격자, 공격대상, 공격받음
					}
					if (board[i][j] == 0) {
						continue; // 부서진 포탑
					}
					board[i][j]++; // 무관하면 공격력++
				}
			}
		}

//		출력
//		첫 번째 줄에 K번의 턴이 종료된 후 남아있는 포탑 중 가장 강한 포탑의 공격력
		int ans = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				ans = Math.max(ans, board[i][j]);
			}
		}
		System.out.print(ans);

	}
}
	class Pair {
		int x, y;

		public Pair(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

// class turret을 정의해 관리합니다.
	class Turret implements Comparable<Turret> {
		int x, y, r, p;

		public Turret(int x, int y, int r, int p) {
			this.x = x;
			this.y = y;
			this.r = r; // 공격시점 rec[i]
			this.p = p; // 공격력
		}

//   공격자: 약한 포탑 우선순위: 공격력 최소 포탑> 가장 최근에 공격한 포탑> 행+열 최대 > 열 최대
//   공격대상: 강한 포탑 우선순위: 공격력 p 최대> 공격한지 가장 오래된 r > 행+열 x+y 합 최소 > 열 y 최소
		public int compareTo(Turret t) {
			if (this.p != t.p) { // 공격력
				return this.p - t.p;
			}
			if (this.r != t.r) { // 공격시기
				return t.r - this.r;
			}
			if (this.x + this.y != t.x + t.y) { // 행+열
				return (t.x + t.y) - (this.x + this.y);
			}
			return t.y - this.y; //
		}
	}    