/*
한 변의 길이가 N인 2차원 평면. 딘가에는 한 변의 길이가 M인 정육면체 형태의 시간의 벽.

스캔
1.미지의 공간의 평면도: 위에서 내려다본 전체 맵입니다.
2.시간의 벽의 단면도: 시간의 벽의 윗면과 동서남북 네 면의 단면도입니다.
빈 공간0, 장애물1

시작점: 시간의 벽의 윗면 어딘가
타임머신의 위치2
시간의 벽 위치3
탈출구4: 미지의 공간 바닥. 1칸 제외 전부 장애물.
총 F개의 시간 이상 현상. 
    바닥의 빈 공간에서 시작하여 매 v의 배수턴마다 d로 1칸씩,빈 공간으로만 확산.
    방향 d는 동(0), 서(1), 남(2), 북(3)
    확산 불가 시 이동 멈춤

매 턴마다 상하좌우로 한 칸씩 이동
시간 이상 현상이 확산된 직후 타임머신이 이동

입력
1. 미지의 공간의 한 변의 길이 N, 시간의 벽 한 변의 길이 M, 시간 이상 현상의 개수 F
2. N개의 줄에 걸쳐 미지의 공간의 평면도
3. M줄에 걸쳐 시간의 벽의 동, 서, 남, 북, 윗면의 단면도
4. F 줄에 걸쳐 각 시간 이상 현상의 초기 위치xy, 확산방향d, 확산상수v

출력
시작점에서 탈출구까지 이동하는 데 필요한 최소 시간(턴 수)을 출력
탈출할 수 없다면 -1
*/
import java.util.*;
public class Main {
    static final int MAXN = 20;
	static final int MAXM = 10; // 최대 10이지만 코드 일반화를 위해 20
	static final int MAXF = 10;

	static final int INF = Integer.MAX_VALUE;

	// 전역변수를 쓰는 이유
	// 1. 각 함수에서 반복적으로 접근해야 하므로 재사용 가능해야 함.
	// 2. 지역 변수 = 스택 메모리에 저장. 큰 변수들은 스택 오버플로 가능. 따라서 힙 메모리에 저장되는 전역변수 사용.
	static int[][] SpaceMap = new int[MAXN][MAXN]; // 미지의 공간의 평면도
	static int[][] SpaceMapCellId = new int[MAXN][MAXN]; // 평면도의 각 셀에 대응하는 그래프 정점의 번호를 저장하는 배열
	static int[][][] TimeWall = new int[6][MAXM][MAXM]; // 시간의 벽의 단면도
	static int[][][] TimeWallCellId = new int[6][MAXM][MAXM]; // 시간의 벽의 단면도의 각 셀에 대응하는 그래프 정점의 번호를 저장하는 배열

	// 시간 이상 현상에 대한 정보를 저장
	// 더 이상 확산 못 하면 확산 중지되므로 이를 관리하는 변수도 필요함.
	// 입력 시작점 행번호, 열번호, 확산 방향d, 확산 상수v, + 확산중인지 여부
	static class AbnormalTimeEvent {
		int xpos, ypos, direction, cycle, alive;
	}

	static AbnormalTimeEvent[] events = new AbnormalTimeEvent[MAXF];

	// 그래프를 저장할 인접리스트
	static int[][] Graph;

	// 타임머신에서 해당 번호의 셀까지 도달하는데 필요한 최소 턴 횟수
	static int[] dist;

	public static void main(String[] args) {
		// 입력
		Scanner sc = new Scanner(System.in);
		// 1. 미지의 공간의 한 변의 길이 N, 시간의 벽 한 변의 길이 M, 시간 이상 현상의 개수 F
		int N = sc.nextInt();
		int M = sc.nextInt();
		int F = sc.nextInt();
		// 2. N개의 줄에 걸쳐 미지의 공간의 평면도
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				SpaceMap[i][j] = sc.nextInt();
			}
		}
		// 3. M줄에 걸쳐 시간의 벽의 동, 서, 남, 북, 윗면의 단면도
		// 돟서남북 0213. 동서, 남북을 각각 대칭 숫자로 지정하면 방향 전환 시 유리. oppositeDirection = (direction
		// + 2) % 4;
		// 동
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				TimeWall[0][i][j] = sc.nextInt();
			}
		}
		// 서
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				TimeWall[2][i][j] = sc.nextInt();
			}
		}
		// 남
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				TimeWall[1][i][j] = sc.nextInt();
			}
		}
		// 북
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				TimeWall[3][i][j] = sc.nextInt();
			}
		}
		// 윗면
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				TimeWall[4][i][j] = sc.nextInt();
			}
		}
		// 4. F 줄에 걸쳐 각 시간 이상 현상의 초기 위치xy, 확산방향d, 확산상수v
		// 문제에서 동서남북은 0123 으로 주어짐. 따라서 1 -> 2, 2->1로 변경해야 함
		for (int i = 1; i <= F; i++) {
			events[i] = new AbnormalTimeEvent();
			events[i].xpos = sc.nextInt();
			events[i].ypos = sc.nextInt();
			events[i].direction = sc.nextInt();
			events[i].cycle = sc.nextInt();
			if (events[i].direction == 1)
				events[i].direction = 2;
			else if (events[i].direction == 2)
				events[i].direction = 1;

			events[i].alive = 1;
		}

		// 공간의 상황에 대응되는 그래프 생성
		build_graph(N, M);

		// 생성된 그래프의 정점의 개수: (N*N - M*M) + 5*M*M = N*N + 4*M*M 개
		// 거리 계산 배열. -1로 초기화.
		int maxNodes = N * N + 4 * M * M + 1;
		dist = new int[maxNodes];
		Arrays.fill(dist, -1);

		// 장애물 처리
		// 미지의 공간에 있는 장애물 설정
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				// 시간의 벽은 패스
				if (SpaceMap[i][j] == 3)
					continue;
				// 장애물이 있는 곳이면
				if (SpaceMap[i][j] == 1) {
					int idx = SpaceMapCellId[i][j];
					dist[idx] = INF;
				}
			}
		}
		// 미지의 공간에서 시간 이상 현상의 시작점도 장애물로 처리
		for (int i = 1; i <= F; i++) {
			int x = events[i].xpos;
			int y = events[i].ypos;
			int idx = SpaceMapCellId[x][y];
			dist[idx] = INF;
		}
		// 시간의 벽 위에 있는 장애물의 경우
		for (int t = 0; t < 5; t++) {
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < M; j++) {
					if (TimeWall[t][i][j] == 1) {
						int idx = TimeWallCellId[t][i][j];
						dist[idx] = INF;
					}
				}
			}
		}

		// BFS를 진행할 큐
		Queue<Integer> que = new LinkedList<Integer>();

		// 시작점, 탈출구 설정
		int cell_start = -1;
		int cell_end = -1;
		// 타임머신의 시작점 탐색
		outer1: for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				if (TimeWall[4][i][j] == 2) {
					cell_start = TimeWallCellId[4][i][j];
					break outer1;
				}
			}
		}
		// 탈출구 위치 탐색
		outer2: for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (SpaceMap[i][j] == 4) {
					cell_end = SpaceMapCellId[i][j];
					break outer2;
				}
			}
		}

		que.add(cell_start);
		dist[cell_start] = 0; // 시작점-시작점 거리는 0

		// 게임 시작
		for (int runs = 1;; runs++) {
			// 현재 턴에 확장하는 이상현상이 있으면 영향을 받는 셀을 업데이트
			for (int i = 1; i <= F; i++) {
				// 확신 현상 없음
				if (events[i] == null)
					continue;
				// 더 이상 확산하지 않는 이상 현상
				if (events[i].alive == 0)
					continue;
				// 지금 턴에 확산하지 않음
				if (runs % events[i].cycle != 0)
					continue;

				// 이상 현상의 시작점
				int nx = events[i].xpos;
				int ny = events[i].ypos;

				// 이상현상의 방향에 따라 영향을 받는 셀의 좌표
				// 동서남북 순으로 구함
				// runs / events[i].cycle 는 주기마다 1칸씩 이동한단 거고
				// 동남은 인덱스 증가하니까 +, 서북은 인덱스 감소하니까 -
				// 동서는 열 번호가 바뀌니까 ny, 남북은 행 번호가 바뀌니까 nx
				if (events[i].direction == 0) {
					ny += (runs / events[i].cycle);
				} else if (events[i].direction == 1) {
					nx += (runs / events[i].cycle);
				} else if (events[i].direction == 2) {
					ny -= (runs / events[i].cycle);
				} else {
					nx -= (runs / events[i].cycle);
				}

				// 이상 현상이 범위를 벗어나면 확산 불가로 변경
				if (nx < 0 || ny < 0 || nx >= N || ny >= N) {
					events[i].alive = 0;
					continue;
				}
				// 이상현상이 장애물이나 탈출구, 시간의 벽을 만난 경우, 확산하지 않습니다
				// 이상현상은 미지의 공간에서 0인 곳으로만 확산 가능
				if (SpaceMap[nx][ny] != 0) {
					events[i].alive = 0;
					continue;
				}

				// 이상현상이 확산된 셀(nx, ny)은 지나갈 수 없음
				int idx = SpaceMapCellId[nx][ny];
				dist[idx] = INF;
			}

			// 이번턴에 지나갈 수 있는 셀들의 번호를 저장. 매 턴마다 새로 생성
			ArrayList<Integer> next_cells = new ArrayList<Integer>();

			// 이번턴에 도달 가능한 셀들을 탐색
			while (!que.isEmpty()) {
				int idx = que.poll(); // 큐에 저장된 셀 좌표 꺼냄

				for (int i = 0; i < 4; i++) {
					int idy = Graph[idx][i]; // 현재 셀의 i방향으로 인접한 셀 좌표
					if (idy == -1)
						continue; // 해당 방향으로 이동가능한 셀 없음. (-1: 인접 셀 없음.)
					if (dist[idy] != -1)
						continue; // 이미 최소 턴의 수를 계산한 셀의 경우 통과

					dist[idy] = runs; // 인접 셀(다음 위치)에 도달한 턴 수 저장
					next_cells.add(idy); // 이번에 새로 도달 가능한 셀의 번호를 추가
				}
			}

			// 새로 도달 가능한 셀들이 없으면 종료
			if (next_cells.size() == 0) {
				break;
			}
			// 새로 도달 가능한 셀들에 대응하는 번호를 큐에 추가
			for (int i = 0; i < next_cells.size(); i++) {
				que.add(next_cells.get(i));
			}

			// 탈출구에 가기 위해 필요한 최소 턴수를 구했다면, 종료.
			if (dist[cell_end] != -1) {
				break;
			}
		}

		// 정답을 출력합니다.
		// 불가능하면 -1이 출력됩니다
		if (dist[cell_end] == -1 || dist[cell_end] >= INF) {
			System.out.println(-1);
		} else {
			System.out.println(dist[cell_end]);
		}

		sc.close();
	}

	// 그래프 생성 함수
	static void build_graph(int N, int M) {
		// 1. 각 셀에 대해 대응될 번호를 차례로 부여
		// 미지의 공간에서 시간의 벽이 아닌 부분에 셀 번호 부여
		int cnt = 0;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (SpaceMap[i][j] != 3) { // 시간의 벽이 있는 셀이 아닌 경우에만 번호를 부여합니다
					cnt++;
					SpaceMapCellId[i][j] = cnt;
				}
			}
		}
		// 단면도의 동쪽, 남쪽, 서쪽, 북쪽, 위쪽 순으로 순회하며 셀에 번호를 부여
		for (int t = 0; t < 5; t++) {
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < M; j++) {
					cnt++;
					TimeWallCellId[t][i][j] = cnt;
				}
			}
		}

		// 2. 셀 번호의 정점들로 구성된 그래프: 정점의 번호에 대응되는 셀과 인접한 셀의 번호를 가지는 정점을 간선으로 연결 (최대 4개의 정점과
		// 연결 가능)
		// 평면도(단면도)에서, 오른쪽으로 인접한 경우 0(동), 아래쪽으로 인접한 경우 1(남), 왼쪽으로 인접한 경우 2(서), 위쪽으로 인접한
		// 경우 3(북)을 인덱스에 저장.
		// 인접 리스트(목록) 방식: 인접 정점들의 목록을, 배열||리스트 자료구조를 이용해 저장한 것.
		Graph = new int[cnt + 1][4];
		for (int i = 0; i <= cnt; i++) {
			Arrays.fill(Graph[i], -1);
		}

		// 간선을 추가하는 작업을 위해 사용할 dx 남북, dy 동서 배열
		// 행렬 기준 가로 변화는 열 이동, 세로 변화는 행 이동
		// 동서=가로=열=y, 남북=세로=행=x
		int[] dx = { 0, 1, 0, -1 }; // 남북
		int[] dy = { 1, 0, -1, 0 }; // 동서

		// 평면도에 셀에 대응되는 번호의 정점 쌍에 대해 간선을 추가
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (SpaceMap[i][j] != 3) { // 현재 셀에 장애물이 없는 경우
					int idx = SpaceMapCellId[i][j];

					// 동남서북 순으로 인접한 셀들을 탐색
					for (int dd = 0; dd < 4; dd++) {
						int nx = i + dx[dd];
						int ny = j + dy[dd];

						// 범위를 벗어나면 넘김
						if (nx < 0 || ny < 0 || nx >= N || ny >= N)
							continue;
						// 시간의벽일 경우 넘김
						if (SpaceMap[nx][ny] == 3)
							continue;

						// 인접한 셀 번호를 그래프[현재 셀 번호][인접셀이 있는 방향] 칸에 저장함으로서 간선 표현 (인접 리스트 방식)
						Graph[idx][dd] = SpaceMapCellId[nx][ny];
					}
				}
			}
		}

		// 2. 시간의 벽의 동남서북에 있는 셀들이 인접할 경우 대응되는 번호의 정점들을 이어줍니다
		for (int t = 0; t < 4; t++) {
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < M; j++) {
					int idx = TimeWallCellId[t][i][j];

					// 위와 비슷하게 4방향 탐색
					for (int dd = 0; dd < 4; dd++) {
						int nx = i + dx[dd];
						int ny = j + dy[dd];

						// 행 범위가 넘어갔을 경우 통과
						if (nx < 0 || nx >= M)
							continue;

						// 동남서북 동남서북 동남서북 순으로 방향이 순환한다면 동의 이전 면은 북, 다음 면은 남. (북=3=t+3 동=0=t, 남=1=t+1)
						// <0: 현 단면에서 가장 왼쪽에 있는 정점들은 다음 면의 가장 오른쪽 정점과 닿아있음.
						// 시계방향순으로 하나 앞에 있는 단면도의 가장 오른쪽 열의 셀과 인접
						if (ny < 0) {
							Graph[idx][dd] = TimeWallCellId[(t + 1) % 4][nx][M - 1];
						}
						// >=M: 현 단면의 가장 오른쪽 점은 이전 면의 가장 왼쪽에 닿아 있음.
						// 열 번호가 M-1보다 커질 경우, 반시계방향순으로 하나 앞=시계방향으로 하나 뒤에 있는 단면도의 가장 왼쪽 열의 셀과 인접
						else if (ny >= M) {
							Graph[idx][dd] = TimeWallCellId[(t + 3) % 4][nx][0];
						}
						// 그 외의 경우 평면도의 경우처럼 이어줍니다
						else {
							Graph[idx][dd] = TimeWallCellId[t][nx][ny];
						}

					}
				}

			}
		}

		// 3. 시간의 벽 위쪽 단면도에 속하는 셀들에 대응되는 번호의 정점 쌍에 대해 간선을 추가
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				int idx = TimeWallCellId[4][i][j];

				for (int dd = 0; dd < 4; dd++) {
					int nx = i + dx[dd];
					int ny = j + dy[dd];
					// 범위를 벗어날 경우 넘어갑니다
					if (nx < 0 || ny < 0 || nx >= M || ny >= M)
						continue;
					// 그렇지 않을 경우 이어줍니다
					Graph[idx][dd] = TimeWallCellId[4][nx][ny];
				}
			}
		}

		// 4. 위쪽 시간의 벽와 인접한 동남서북 단면도의 셀들에 대해 대응되는 번호의 정점을 잇는 간선을 추가
		// 동서남북 면에선 윗면이 항상 북쪽이므로 방향 인덱스=3, 모든 인접 셀은 0행에 위치.
		// 동쪽 단면도와 인접한 셀들의 경우.
		// 동쪽면과 윗면의 행 번호 공유.
		// 동쪽 면의 가장 오른쪽 열에서 윗면의 i행을 빼면 동쪽면의 열 번호.
		for (int i = 0; i < M; i++) {
			int idx = TimeWallCellId[4][i][M - 1]; // 인접한 위쪽 단면도의 셀의 번호
			int idy = TimeWallCellId[0][0][M - 1 - i]; // 인접한 동쪽 단면도의 셀의 번호
			Graph[idx][0] = idy;
			Graph[idy][3] = idx;
		}
		// 남쪽 단면도와 인접한 셀들의 경우.
		// 윗면과 남쪽면을 열을 공유. 따라서 열 번호가 동일
		for (int i = 0; i < M; i++) {
			int idx = TimeWallCellId[4][M - 1][i]; // 인접한 위쪽 단면도의 셀의 번호
			int idy = TimeWallCellId[1][0][i]; // 인접한 남쪽 단면도의 셀의 번호
			Graph[idx][1] = idy;
			Graph[idy][3] = idx;
		}
		// 서쪽 단면도와 인접한 셀들의 경우
		// 행 번호 공유. 윗면 행과 서쪽 열 세는 순서가 동일
		for (int i = 0; i < M; i++) {
			int idx = TimeWallCellId[4][i][0]; // 인접한 위쪽 단면도의 셀의 번호
			int idy = TimeWallCellId[2][0][i]; // 인접한 서쪽 단면도의 셀의 번호
			Graph[idx][2] = idy;
			Graph[idy][3] = idx;
		}
		// 북쪽 단면도의 경우
		// 북쪽 가장 왼쪽 열에서 윗면 열 번호 빼면 인접 셀의 열 번호
		for (int i = 0; i < M; i++) {
			int idx = TimeWallCellId[4][0][i]; // 인접한 위쪽 단면도의 셀의 번호
			int idy = TimeWallCellId[3][0][M - 1 - i]; // 인접한 북쪽 단면도의 셀의 번호
			Graph[idx][3] = idy;
			Graph[idy][3] = idx;
		}

		// 미지의 공간에서 시간의 벽이 시작하는 셀의 행 번호, 열 번호
		int timewallStartx = -1;
		int timewallStarty = -1;
		// 미지의 공간에서 시간의 벽이 시작하는 셀 위치를 탐색. 시작점을 찾으면 루프 종료
		// outer: 레이블. 내부 루프에서 바깥 루프 종료시키기 위해 사용. 이거 대신 boolean flag를 사용할 수도 있음.
		outer: for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (SpaceMap[i][j] == 3) {
					timewallStartx = i;
					timewallStarty = j;
					break outer;
				}
			}
		}

		// 미지의 공간과 인접한 시간의 벽 셀들에 대응되는 번호의 정점을 잇는 간선 추가
		// 동남서북 단면의 셀은 모두 M-1행. 이 경우는 열도 전부 i열
		// 미지의공간의 경우 시간의벽 시작점인 tx를 기준으로 판단. 열은 단면의 열 번호 기준 계산.
		// 동쪽 단면도의 경우
		// 동쪽 i열이 평면 M-1행에서부터 i열 올라온 것.
		if (timewallStarty + M < N) {
			for (int i = 0; i < M; i++) {
				int idx = TimeWallCellId[0][M - 1][i];
				int idy = SpaceMapCellId[timewallStartx + (M - 1) - i][timewallStarty + M];
				Graph[idx][1] = idy;
				Graph[idy][2] = idx;
			}
		}
		// 남쪽 단면도의 경우
		if (timewallStartx + M < N) {
			for (int i = 0; i < M; i++) {
				int idx = TimeWallCellId[1][M - 1][i];
				int idy = SpaceMapCellId[timewallStartx + M][timewallStarty + i];
				Graph[idx][1] = idy;
				Graph[idy][3] = idx;
			}
		}
		// 서쪽 단면도의 경우
		if (timewallStarty > 0) {
			for (int i = 0; i < M; i++) {
				int idx = TimeWallCellId[2][M - 1][i];
				int idy = SpaceMapCellId[timewallStartx + i][timewallStarty - 1];
				Graph[idx][1] = idy;
				Graph[idy][0] = idx;
			}
		}
		// 북쪽 단면도의 경우
		if (timewallStartx > 0) {
			for (int i = 0; i < M; i++) {
				int idx = TimeWallCellId[3][M - 1][i];
				int idy = SpaceMapCellId[timewallStartx - 1][timewallStarty + (M - 1) - i];
				Graph[idx][1] = idy;
				Graph[idy][1] = idx;
			}
		}

		return;
	}
}