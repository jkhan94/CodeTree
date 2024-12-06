/*
P 명의 산타들
N×N 크기의 게임판 격자. 
    좌표는 (r,c), 최상단(1,1)
    아래, 오른쪽으로 갈수록 증가
    두 칸 사이의 거리 = (r1 - r2)^2 + (c1 - c2)^2
M 개의 턴.
    매 턴마다 루돌프가 한 번 움직인 뒤, 1번 산타부터 P번 산타까지 순서대로 이동

루돌프의 움직임
    1. 가장 가까운 거리의 미탈락 산타에게 인접한 8방향 중 하나로 1칸.
    2. 거리가 같다면 r 큰 산타
    3. r 동일하면 c 큰 산타

산타의 움직임
    1-P 순서대로 이동
    루돌프와 가까워지는 방향으로 1칸. 상우하좌 순.
    기절 || 격자 밖으로 나가 탈락한 산타들은 이동 불가
    다른 산타가 있는 칸이나 게임판 밖으로는 이동 불가.
    갈 수 있는  칸이 없거나, 루돌프와 가까워지지 않는다면 이동 안 함

충돌
    산타와 루돌프 같은 칸
    *루돌프가 움직여 충돌: 산타 점수+C, 산타는 루돌프가 이동해온 방향으로 C 이동
    *산타가 움직여 충돌: 충돌한 산타점수+D, 산타가 이동해온 반대방향으로 D 이동
        밀리는 건 정확히 원하는 위치로 도달.
        밀려난 위치가 게임판 밖이면 탈락
        밀려난 칸에 다른 산타 있으면 상호작용

상호작용
    산타에게 밀린 산타가 해당 방향으로, 겹치지 않을 때까지 1칸씩 이동.
    만약 밖으로 밀리면 탈락

기절
    산타는 루돌프와의 충돌 후 기절
    현재가 k번째 턴이면, (k+1)번째 턴까지 기절. (k+2)번째 턴부터 정상
    기절 중 이동 불가. 그러나 충돌, 상호작용으로 밀릴 수 있음.
    루돌프 돌진 불가 상대

게임 종료
    P명의 산타 모두 탈락 시 즉시 게임 종료.
    매 턴 이후 미탈락 산타들_점수 +1

입력
    1. 첫 번째 줄에 N, M, P, C, D
    2. 루돌프의 초기 위치 (r,c)
    3. P 개의 줄에 걸쳐서 산타의 번호 Pn 과 초기 위치 (Sr, Sc)
​    처음 산타와 루돌프의 위치는 겹쳐져 주어지지 않음
    첫 산타 점수 = 0
 
출력
    각 산타가 얻은 최종 점수 1~P번까지 순서대로 공백 두고 출력.
*/
import java.util.*;
public class Main {
    // 산타 번호는 1번부터 시작. 0번 인덱스 버리므로 문제의 최대값보다 하나 크게 설정
   	static final int MAX_N = 51;
	static final int MAX_P = 31;

	static int n, m, p, c, d;
	static int[] points = new int[MAX_P]; // 산타 점수
	static Map<Integer, Pair> pos = new HashMap<>(); // 산타 좌표<번호, 좌표>
	static Pair rudolf = new Pair(0, 0); // 루돌프 좌표

	static int[][] board = new int[MAX_N][MAX_N]; // 게임판
	static boolean[] is_live = new boolean[MAX_P]; // 산타 탈락 여부
	static int[] stun = new int[MAX_P]; // 산타 기절

	// 우하가 +방향
	static final int[] dx = { -1, 0, 1, 0 }; // 상우하좌
	static final int[] dy = { 0, 1, 0, -1 };

	// (x, y)가 보드 내의 좌표인지 확인
	static boolean is_inrange(int x, int y) {
		return 1 <= x && x <= n && 1 <= y && y <= n;
	}

	public static void main(String[] args) {
		// 입력
		Scanner sc = new Scanner(System.in);
		// 1. 첫 번째 줄에 N, M, P, C, D
		n = sc.nextInt(); // 모드 크기 n*n
		m = sc.nextInt(); // 게임 턴 수
		p = sc.nextInt(); // 산타 수
		c = sc.nextInt(); // 루돌프 힘
		d = sc.nextInt(); // 산타 힘
		// 2. 루돌프의 초기 위치 (r,c)
		rudolf.x = sc.nextInt();
		rudolf.y = sc.nextInt();
		board[rudolf.x][rudolf.y] = -1; // 보드에 루돌프 위치 표시
		// 3. P 개의 줄에 걸쳐서 산타의 번호 Pn 과 초기 위치 (Sr, Sc)
		for (int i = 1; i <= p; i++) {
			int id = sc.nextInt();
			int x = sc.nextInt();
			int y = sc.nextInt();
			pos.put(id, new Pair(x, y)); // pos에 <번호, 좌표> 쌍 저장
			board[x][y] = id; // 각 산타의 위치를 보드에 표시
			is_live[id] = true; // 산타 탈락 여부 표시
		}

		// 턴 수만큼 게임 실행
		for (int t = 1; t <= m; t++) {
			int closestX = 10000; // 최소값을 구하므로 초기값은 크게 설정
			int closestY = 10000;
			int closestIdx = 0;

			// 1. 루돌프의 움직임
			// 살아있는 산타 중 루돌프에 가장 가까운 산타 탐색
			for (int i = 1; i <= p; i++) {
				if (!is_live[i]) {
					continue; // 기절 || 탈락 산타면 패스
				}

				// 루돌프의 움직임 우선순위
				// 1. 가장 가까운 거리의 미탈락 산타에게 인접한 8방향 중 하나로 1칸.
				// 2. 거리가 같다면 x=r 큰 산타
				// 3. r 동일하면 y=c 큰 산타
				// Tuple (두 점 사이 거리, x, y)
				Tuple currentBest = new Tuple(
						(closestX - rudolf.x) * (closestX - rudolf.x) + (closestY - rudolf.y) * (closestY - rudolf.y),
						-closestX, -closestY);
				Tuple currentValue = new Tuple((pos.get(i).x - rudolf.x) * (pos.get(i).x - rudolf.x)
						+ (pos.get(i).y - rudolf.y) * (pos.get(i).y - rudolf.y), -pos.get(i).x, -pos.get(i).y);

				// 현재 값이 더 작으면 변경
				if (currentValue.compareTo(currentBest) < 0) {
					closestX = pos.get(i).x;
					closestY = pos.get(i).y;
					closestIdx = i;
				}
			}

			// 가장 가까운 산타의 방향으로 루돌프가 이동
			if (closestIdx != 0) {
				Pair prevRudolf = new Pair(rudolf.x, rudolf.y); // 이동 전 위치 저장

				// x 이동방향
				int moveX = 0;
				if (closestX > rudolf.x) {
					moveX = 1; // 이동할 점의 x가 더 크면 오른쪽으로 +1
				} else if (closestX < rudolf.x) {
					moveX = -1; // 왼쪽
				}
				// y 이동방향
				int moveY = 0;
				if (closestY > rudolf.y) {
					moveY = 1; // 아래로 이동
				} else if (closestY < rudolf.y) {
					moveY = -1; // 위로 이동
				}

				// 루돌프 좌표 변경
				rudolf.x += moveX;
				rudolf.y += moveY;
				// 이동전 위치 비우기
				board[prevRudolf.x][prevRudolf.y] = 0;

				// 2. 루돌프 좌표 = 산타 좌표(충돌)
				// 루돌프가 움직여 충돌: 산타 점수+C, 산타는 루돌프가 이동해온 방향으로 C칸 이동
				if (rudolf.x == closestX && rudolf.y == closestY) {
					// 산타의 밀린 위치
					int firstX = closestX + moveX * c;
					int firstY = closestY + moveY * c;
					int lastX = firstX;
					int lastY = firstY;

					stun[closestIdx] = t + 1; // 산타 기절

					// 만약 이동한 위치에 산타가 있을 경우, 연쇄 이동
					// 게임판 내이고, 산타 번호가 저장되어 있을 때, 밀린 산타의 위치 갱신
					// 방향은 루돌프가 이동해 온 방향으로 동일
					while (is_inrange(lastX, lastY) && board[lastX][lastY] > 0) {
						lastX += moveX;
						lastY += moveY;
					}

					// 연쇄적으로 충돌이 일어난 가장 마지막 위치부터 순차적으로 게임판의 산타 1칸 이동
					while (!(lastX == firstX && lastY == firstY)) {
						// 밀리기 전 좌효
						int beforeX = lastX - moveX;
						int beforeY = lastY - moveY;

						if (!is_inrange(beforeX, beforeY)) {
							break; // 게임판 밖이면 산타 상호작용 끝
						}

						int idx = board[beforeX][beforeY]; // 밀린 산타 번호
						if (!is_inrange(lastX, lastY)) {
							is_live[idx] = false; // 밀려난 위치가 게임판 밖이면 탈락
						} else {
							board[lastX][lastY] = board[beforeX][beforeY]; // 산타 이동
							pos.put(idx, new Pair(lastX, lastY)); // 산타 번호, 좌표 저장
						}
						// 마지막으로 밀린 산타의 좌표 lastx, lasty
						lastX = beforeX;
						lastY = beforeY;
					}

					points[closestIdx] += c; // 루돌프와 충돌한 산타=루돌프와 가장 가까운 산타의 점수 +C
					pos.put(closestIdx, new Pair(firstX, firstY));
					if (is_inrange(firstX, firstY)) {
						board[firstX][firstY] = closestIdx; // 게임판 내면 좌표에 번호 저장
					} else {
						is_live[closestIdx] = false; // 나갔으면 탈락
					}
				}
			}
			// 충돌과 무관. 루돌프 위치 변경
			board[rudolf.x][rudolf.y] = -1;

			// 3. 산타 이동: 각 산타들은 루돌프와 가장 가까운 방향으로 한칸 이동
			for (int i = 1; i <= p; i++) {
				if (!is_live[i] || stun[i] >= t) {
					continue; // 탈락 || 기절이면 패스
				}

				int minDist = (pos.get(i).x - rudolf.x) * (pos.get(i).x - rudolf.x)
						+ (pos.get(i).y - rudolf.y) * (pos.get(i).y - rudolf.y); // 산타-루돌프 최소 거리
				int moveDir = -1; // 산타 이동방향

				for (int dir = 0; dir < 4; dir++) {
					int nx = pos.get(i).x + dx[dir]; // 상우하좌
					int ny = pos.get(i).y + dy[dir];

					if (!is_inrange(nx, ny) || board[nx][ny] > 0) {
						continue; // 게임판 밖이거나 산타 있는 칸이면 패스
					}

					int dist = (nx - rudolf.x) * (nx - rudolf.x) + (ny - rudolf.y) * (ny - rudolf.y);
					if (dist < minDist) { // 이동했을 때의 거리가 더 작으면 변경
						minDist = dist;
						moveDir = dir;
					}
				}

				// 산타가 이동했다면 충졸 고려
				if (moveDir != -1) {
					int nx = pos.get(i).x + dx[moveDir];
					int ny = pos.get(i).y + dy[moveDir];

					// 4. 산타가 이동하여 충돌
					// 충졸한 산타점수+D, 산타가 이동해온 반대 방향으로 D 이동
					if (nx == rudolf.x && ny == rudolf.y) {
						stun[i] = t + 1;

						// 이동한 방햔과 반대 방향
						int moveX = -dx[moveDir];
						int moveY = -dy[moveDir];
						// d칸 만큼 이동
						int firstX = nx + moveX * d;
						int firstY = ny + moveY * d;
						int lastX = firstX;
						int lastY = firstY;

						if (d == 1) {
							points[i] += d; // 충돌한 산타 점수 +d
						} else {
							// 만약 이동한 위치에 산타가 있으면 같은 방향으로 연쇄 이동
							while (is_inrange(lastX, lastY) && board[lastX][lastY] > 0) {
								lastX += moveX;
								lastY += moveY;
							}

							// 연쇄적으로 충돌이 일어난 가장 마지막 위치에서 순차적으로 보드판에 있는 산타를 한칸씩 이동.
							while (!(lastX == firstX && lastY == firstY)) {
								int beforeX = lastX - moveX;
								int beforeY = lastY - moveY;

								if (!is_inrange(beforeX, beforeY)) {
									break;
								}

								int idx = board[beforeX][beforeY];

								if (!is_inrange(lastX, lastY)) {
									is_live[idx] = false;
								} else {
									board[lastX][lastY] = board[beforeX][beforeY]; // 산타 번호를 다음 위치로 이동
									pos.put(idx, new Pair(lastX, lastY));
								}
								// 마지막 위치를 한 칸 당김
								lastX = beforeX;
								lastY = beforeY;
							}

							points[i] += d;
							board[pos.get(i).x][pos.get(i).y] = 0;
							pos.put(i, new Pair(firstX, firstY));
							if (is_inrange(firstX, firstY)) {
								board[firstX][firstY] = i; // 보드 안이면 산타 번호 저장
							} else {
								is_live[i] = false; // 나갔으면 탈락
							}
						}
					} else { // 산타끼리 충돌 안 했으면
						board[pos.get(i).x][pos.get(i).y] = 0;
						pos.put(i, new Pair(nx, ny));
						board[nx][ny] = i;
					}
				}
			}

			// 라운드가 끝나고 탈락하지 않은 산타들의 점수를 1 증가
			for (int i = 1; i <= p; i++) {
				if (is_live[i]) {
					points[i]++;
				}
			}
		} // 턴 끝

		// 출력
		// 각 산타가 얻은 최종 점수 1~P번까지 순서대로 공백 두고 출력.
		for (int i = 1; i <= p; i++) {
			System.out.print(points[i] + " ");
		}
	}

	static class Tuple implements Comparable<Tuple> {
		int distance, x, y;

		Tuple(int distance, int x, int y) {
			this.distance = distance;
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Tuple other) {
			if (this.distance != other.distance) {
				return Integer.compare(this.distance, other.distance); // first 값을 기준으로 비교
			}
			if (this.x != other.x) {
				return Integer.compare(this.x, other.x); // second 값을 기준으로 비교
			}
			return Integer.compare(this.y, other.y); // third 값을 기준으로 비교
		}
	}

	static class Pair {
		int x, y;

		Pair(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}