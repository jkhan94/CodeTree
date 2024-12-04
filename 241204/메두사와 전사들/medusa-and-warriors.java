/*
0에서 N−1의 범위로 이루어진 N×N. 도로는 0, 아니면 1.
집은 좌표 (Sr ,Sc )에 있고, 공원은 좌표 (Er ,Ec). 집과 공원 좌표는 다를 수 있음.
집 -> 공원으로 도로 따라 최단경로로 이동. 집과 공원은 항상 도로 위.
전사가 처음부터 집에 있진 않음.

M명의 전사. (ri,ci)에 위치. 메두사를 향해 최단 경로로 이동. 도로, 비도로 상관없이 이동.

1 메두사 이동.
도로 따라 1칸. 이동한 칸에 전사가 있으면 전사 소멸.
최단경로가 여러 개면 상하좌우 순으로 우선.
최단경로가 없을 수도 있음.

2 메두사의 시선.
상하좌우 중 택1. 전사를 가장 많이 볼 수 있는 방향을 봄. 우선순위는 상하좌우.
바라보는 방향으로 90도 시야각. 메두사 위치 기준 9*9: 3 5 7 9가 5번. 

한칸 8방향으로 나눴을 때, 메두사와 전사를 이은 선 뒷부분은 메두사에게 안 보임.
같은 위치에 있으면 현 턴 패스, 턴 종료 시 이동가능.
같은 간에 여러 전사가 있으면 전부 패스.

3 전사들의 이동
우선순위 상하좌우, 메두사와 거리 줄일 수 있는 방향으로 2칸 이동.
메두사의 시야로 이동 불가.
같은 칸 공유 가능.

4 전사의 공격
메두사와 같은 칸이면 전사 소멸.


입력
마을 크기 N, 전사의 수 M
집 위치 sr sc, 공원 위치 er ec
전사들의 좌표 a1r, a1e, a2r, ... 2*m개 1줄
N줄에 걸쳐 도로 정보

출력
메두사가 공원에 도달할 때까지 매 턴마다 해당 턴에서 
1. 모든 전사가 이동한 거리의 합, 
2. 메두사로 인해 돌이 된 전사의 수, 
3. 메두사를 공격한 전사의 수
공백을 사이에 두고 차례대로 출력

*최단경로는 맨해튼 거리 기준.
*메두사가 공원에 도착하면 0을 출력 프로그램 종료.

시간복잡도
마을의 크기 N과 전사의 수 M에 따라 결정. 
BFS를 사용하여 최단 경로를 계산하는 데 O(N^2) 소요
전사들의 이동을 계산 O(M) 소요
각 턴마다 메두사의 시야 계산 O(N^2)
=> 전체 시간복잡도는 O(N^4 +(N^2)*M)
*/
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
public class Main {
	// 상수 정의
	static final int INF = Integer.MAX_VALUE; // 무한대를 나타내는 상수.

	// 방향 배열:  X상하 Y좌우
	// 행렬에서는 맨 위가 (1,1). 아래로 내려가면 값이 커지브로 +1. 위로 올라가면 값이 감소하므로 -1
	// 행렬 좌표계로 생각해서 상하 값을 정해야 함.
	static final int[] DX = { -1, 1, 0, 0 };
	static final int[] DY = { 0, 0, -1, 1 };

	// 전사 위치를 나타내는 클래스
	static class Point {
		int x;
		int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	/**
	 * 메인 메소드: 프로그램의 시작점
	 * 
	 * @param args 명령줄 인자
	 * @throws IOException 입출력 예외
	 */
	public static void main(String[] args) throws IOException {
		// 입력
		Scanner sc = new Scanner(System.in);
		// 마을 크기 N, 전사의 수 M
		String input = sc.nextLine();
		String[] tokens = input.split(" ");
		int N = Integer.parseInt(tokens[0]);
		int M = Integer.parseInt(tokens[1]);

		// 집 위치 sr sc, 공원 위치 er ec
		input = sc.nextLine();
		tokens = input.split(" ");
		int startX = Integer.parseInt(tokens[0]);
		int startY = Integer.parseInt(tokens[1]);
		int endX = Integer.parseInt(tokens[2]);
		int endY = Integer.parseInt(tokens[3]);

		// 전사들의 좌표 a1r, a1e, a2r, ... m개
		Point[] warriorPositions = new Point[M];
		input = sc.nextLine();
	    tokens = input.split(" ");	    
		for (int i = 0; i < M; i++) {		    
		    warriorPositions[i] = new Point(Integer.parseInt(tokens[2*i]), Integer.parseInt(tokens[2*i+1]));
		}

		// N줄에 걸쳐 도로 정보
		int[][] roadGrid = new int[N][N];
		for (int i = 0; i < N; i++) {
		    input = sc.nextLine();
		    tokens = input.split(" ");
		    for (int j = 0; j < N; j++) {
		    	roadGrid[i][j] = Integer.parseInt(tokens[j]);
		    }
		}

		// 시작 지점과 종료 지점은 항상 도로
		// 종료 지점으로부터 모든 셀까지의 거리를 계산
		int[][] distanceGrid = computeDistances(endX, endY, N, roadGrid);

		// 메두사 집에서 공원으로 가는 경로 없음.
		if (distanceGrid[startX][startY] == -1) {
			System.out.println("-1");
			return;
		}

		// 메두사의 시작위치.
		int currentX = startX;
		int currentY = startY;

		// 메두사의 시야 맵 초기화
		int[][] sightMap = new int[N][N];

		// 전사 수 그리드 초기화
		int[][] warriorCountGrid = updateWarriorCountGrid(N, M, warriorPositions);

		// 메인 루프: 메두사가 집에 도착할 때까지 반복
		while (true) {
			boolean moved = false; // 메두사 이동 여부

			// 1.메두사 이동. 현위치에서 종료 지점 방향으로, 상하좌우 순서로 1칸 이동
			for (int dir = 0; dir < 4; dir++) {
				int nextX = currentX + DX[dir];
				int nextY = currentY + DY[dir];
				// 그리드 경계를 벗어나면 무시
				if (nextX < 0 || nextY < 0 || nextX >= N || nextY >= N)
					continue; 
				// 종료점까지의 거리가 다음점이 현위치보다 작을 경우 현위치=다음지점으로 변경
				if (distanceGrid[nextX][nextY] < distanceGrid[currentX][currentY]) {
					currentX = nextX;
					currentY = nextY;
					moved = true;					
					// 이동 후 루프 탈출
					break; 
				}
			}

			// 메두사가 공원에 도착
			if (currentX == endX && currentY == endY) {
				System.out.println("0");
				break;
			}

			// 2. 메두사의 시선. 현위치 기준 가장 전사를 많이 잡을 수 있는 방향을 봄(시야).
			// 메두사와 같은 위치에 있는 전사 수: 메두사의 현위치에 전사가 있을 경우 전사 위치를(-1,-1)로 변경
			for (int i = 0; i < M; i++) {
				if (warriorPositions[i].x == currentX && warriorPositions[i].y == currentY) {
					warriorPositions[i] = new Point(-1, -1); // 전사를 잡힌 상태로 표시
				}
			}
			// 메두사와 같은 위치에 있는 전사들을 제외하기 위해 전사 그리드 업데이트
			warriorCountGrid = updateWarriorCountGrid(N, M, warriorPositions);
			// 가려진 전사 수: 최적의 시야 방향을 선택하고 시야로 커버된 전사의 수 계산
			int sightCoverage = chooseBestSight(currentX, currentY, N, warriorCountGrid, sightMap);

			// 3-4. 전사 이동: 전사들을 2칸 이동. 메두사 공격했는지 체크
			// 이동한 전사 수와 플레이어에게 도달한 전사 수를 구함.
			Pair<Integer, Integer> warriorResult = moveWarriors(currentX, currentY, N, M, warriorPositions, sightMap);

			// 다음 턴을 위해 전사 이동 후 전사 그리드 업데이트
			warriorCountGrid = updateWarriorCountGrid(N, M, warriorPositions);

			// 결과 출력: 모든 전사가 이동한 거리의 합, 돌이 된 전사의 수, 메두사 공격한 전사 수
			System.out.println(warriorResult.first + " " + sightCoverage + " " + warriorResult.second);
		}
	}

	/**
	 * BFS를 이용하여 종료 지점(startX, startY)에서 모든 도달 가능한 셀까지의 최단 거리를 계산하는 메소드
	 * 
	 * @param startX       시작 지점의 x 좌표 (종료 지점)
	 * @param startY       시작 지점의 y 좌표 (종료 지점)
	 * @param N            그리드의 크기 (N x N)
	 * @param roadGrid 장애물 그리드 (1: 도로아님, 0: 도로)
	 * @return 각 셀에서의 거리 그리드
	 */
	static int[][] computeDistances(int startX, int startY, int N, int[][] roadGrid) {
		// 거리 그리드를 초기화: 장애물이 있는 셀은 INF, 그렇지 않으면 -1로 설정
		// BFS에서 -1은 미방문, INF는 방문불가 노드를 의미.
		// -1인 곳을 방문하여 거리 계산. 결과값을 저장하면 방문한 것으로 간주.
		int[][] distanceGrid = new int[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				distanceGrid[i][j] = roadGrid[i][j] == 1 ? INF : -1;
			}
		}
				
//		offer, poll은 추가 실패인 false 리턴. add, remove는 예외 발생.
//		queue.offer = queue.add
//		queue.poll = queue.remove
		// 큐에 (startX, startY)점 추가
		Queue<Point> queue = new LinkedList<>();
		queue.offer(new Point(startX, startY));
		// 시작점과 시작점 간의 거리는 0
		distanceGrid[startX][startY] = 0;

		// BFS 알고리즘 실행
		// 큐가 비면 인접노드 없음
		while (!queue.isEmpty()) {
			// 시작점을 꺼내서 현위치 지정
			Point current = queue.poll();
			int currentX = current.x;
			int currentY = current.y;

			// 상하좌우 순으로 인접노드로 이동.
			// 거리 계산 후 큐에 추가
			for (int dir = 0; dir < 4; dir++) {
				int nextX = currentX + DX[dir];
				int nextY = currentY + DY[dir];

				// 마을을 벗어나는지 확인
				if (nextX < 0 || nextY < 0 || nextX >= N || nextY >= N)
					continue;
				// 이미 방문했거나 도로가 아니면 패스
				if (distanceGrid[nextX][nextY] != -1)
					continue;

				// 다음 셀의 거리 업데이트 및 큐에 추가
				distanceGrid[nextX][nextY] = distanceGrid[currentX][currentY] + 1;
				queue.offer(new Point(nextX, nextY));
			}
		}

		return distanceGrid;
	}
	
	/**
	 * 전사들의 현위치 기준 각 셀에 있는 전사의 수를 업데이트하는 메소드
	 * 
	 * @param N                그리드의 크기
	 * @param M                전사의 수
	 * @param warriorPositions 전사들의 현재 위치
	 * @return 각 셀에 있는 전사의 수 그리드
	 */
	static int[][] updateWarriorCountGrid(int N, int M, Point[] warriorPositions) {
		// 전사 그리드를 초기화
		int[][] warriorCountGrid = new int[N][N];
		for (int i = 0; i < N; i++) {
			Arrays.fill(warriorCountGrid[i], 0);
		}

		// 각 전사의 위치를 확인하여 전사 그리드에 반영
		// 메두사에게 잡힌 전사의 포지션은 (-1, -1)
		for (int i = 0; i < M; i++) {
			if (warriorPositions[i].x == -1)
				continue; // 이미 잡힌 전사는 건너뜀
			int x = warriorPositions[i].x;
			int y = warriorPositions[i].y;
			warriorCountGrid[x][y]++;
		}

		return warriorCountGrid;
	}
	
	/**
	 * 최적의 시야 방향을 선택하여 시야를 설정하는 메소드
	 * 
	 * @param x                현재 플레이어의 x 좌표
	 * @param y                현재 플레이어의 y 좌표
	 * @param N                그리드의 크기
	 * @param warriorCountGrid 각 셀에 있는 전사의 수
	 * @param sightMap         현재 시야 상태 (1: 시야 내, 0: 시야 외)
	 * @return 최대 커버리지 (시야로 커버된 전사의 수)
	 */
	static int chooseBestSight(int x, int y, int N, int[][] warriorCountGrid, int[][] sightMap) {
		// 시야 맵을 초기화 (모든 셀을 시야 밖(0)으로 설정)
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				sightMap[i][j] = 0;
			}
		}

		int maxCoverage = -1; // 최대 커버리지를 저장할 변수
		int bestDirection = -1; // 최적의 시야 방향 (0: 위, 1: 하, 2: 좌, 3: 우)

		// 모든 시야 방향을 테스트하여 최대 커버리지를 찾음
		for (int dir = 0; dir < 4; dir++) {
			// 테스트를 위해 시야 맵을 초기화
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					sightMap[i][j] = 0;
				}
			}

			// 현재 방향으로 시야 설정하고 커버리지 계산
			int coverage = 0;
			// 최적의 경우를 찾을 때는 실행 후 시야각이 초기화 됨
			// 상하좌우 순
			if (dir == 0) {
				coverage = sightUp(x, y, N, true, warriorCountGrid, sightMap);
			} else if (dir == 1) {
				coverage = sightDown(x, y, N, true, warriorCountGrid, sightMap);
			} else if (dir == 2) {
				coverage = sightLeft(x, y, N, true, warriorCountGrid, sightMap);
			} else if (dir == 3) {
				coverage = sightRight(x, y, N, true, warriorCountGrid, sightMap);
			}

			if (maxCoverage < coverage) {
				maxCoverage = coverage;
				bestDirection = dir;
			}
		}

		// 유효한 방향이 선택되었는지 확인
		assert bestDirection != -1 : "최적의 시야 방향을 찾을 수 없습니다.";

		// 최적의 방향으로 실제 시야 설정. 상하좌우 순.
		// 최적의 방향에 맞게 시야각 정보 유지
		if (bestDirection == 0) {
			sightUp(x, y, N, false, warriorCountGrid, sightMap);
		} else if (bestDirection == 1) {
			sightDown(x, y, N, false, warriorCountGrid, sightMap);
		} else if (bestDirection == 2) {
			sightLeft(x, y, N, false, warriorCountGrid, sightMap);
		} else if (bestDirection == 3) {
			sightRight(x, y, N, false, warriorCountGrid, sightMap);
		}

		return maxCoverage;
	}

	/**
 		상하는 행 1칸씩 이동, 좌우는 열 1칸씩 이동하며 커버리지 찾음.
		i는 기준(상하면 행 기준 이동, 좌우면 열 기준 이동)
		범위 설정값은 양수가 되게 표현.
		
		*상
		int left = Math.max(0, y - (x - i));
		int right = Math.min(N - 1, y + (x - i));
		한칸 위부터 시작 i=x-1 (x<i)
		*하
		int left = Math.max(0, y - (i - x));
		int right = Math.min(N - 1, y + (i - x))
		한칸 아래부터 시작 i=x+1 (i>x)
		
		*좌
		int top = Math.max(0, x - (y - i));
		int bottom = Math.min(N - 1, x + (y - i));
		한칸 왼쪽부터 시작 i=y-1		
		*우
		int top = Math.max(0, x - (i - y));
		int bottom = Math.min(N - 1, x + (i - y));
		한칸 오른쪽부터 시작 i=y+1
	 */	
	/**
	 * 위쪽 방향으로 시야를 설정하는 메소드
	 * 
	 * @param x                현재 플레이어의 x 좌표
	 * @param y                현재 플레이어의 y 좌표
	 * @param N                그리드의 크기
	 * @param isTest           테스트 모드 여부 (true: 테스트, false: 실제 적용)
	 * @param warriorCountGrid 각 셀에 있는 전사의 수
	 * @param sightMap         현재 시야 상태 (1: 시야 내, 0: 시야 외)
	 * @return 시야로 커버된 전사의 수
	 */
	static int sightUp(int x, int y, int N, boolean isTest, int[][] warriorCountGrid, int[][] sightMap) {
		// 다이아몬드 형태로 위쪽 셀을 시야에 포함시킴
		// 한 칸 위부터 시작해서 1행씩 올라감
		for (int i = x - 1; i >= 0; i--) {
			//left, rigths는 열 범위
			// 왼쪽 시야각은 아래로 이동하면서 행 인덱스가 1씩 감소, 오른쪽 시야각은 행 인덱스가 1씩 증가.
			// 즉 메두사의 현위치 y왼쪽은 -1, 오른쪽은 +1씩 변함. 만약 두 행 아래에 있는 것을 찾으면 열의 범위는 2칸씩 변함
			// 열의 범위가 변하는 정도를 행 인덱스로 표현하면 x-i
			// 두사의 현위치(x행)에서 행의 인덱스를 가감하면 해당 행의 시야각(열) 범위가 left, right 
			int left = Math.max(0, y - (x - i));
			int right = Math.min(N - 1, y + (x - i));
			for (int j = left; j <= right; j++) {
				sightMap[i][j] = 1; // 시야 범위를 1로 설정
			}
		}

		// 장애물 처리: 설정된 시야각 범위에서 못 보는 곳을 0으로 변경
		boolean obstructionFound = false;
		// 메두사와 전사의 y가 같을 때, 전사 뒷부분 일직선 방향 셀 제거
		for (int i = x - 1; i >= 0; i--) {			
			if (warriorCountGrid[i][y] > 0) {
				obstructionFound = true; // 전사가 있는 경우 장애물로 간주
			}
			
			if (obstructionFound) {
				sightMap[i][y] = 0; // 장애물이 있으면 못 보는 곳으로 변경
			} else {
				sightMap[i][y] = 1; // 장애물이 없으면 보이는 곳 유지
			}			
		}
		// 메두사와 y가 다른 전사들의 대각선 방향 뒷부분 셀 제거
		for (int i = x - 1; i >= 1; i--) {
			// 행의 열 범위 설정
			int left = Math.max(0, y - (x - i));
			int right = Math.min(N - 1, y + (x - i));
			// 왼편
			// i행 j열(왼쪽 ~ 메두사 y까지) 시야각 범위가 아니거나 전사가 있으면			
			for (int j = left; j < y; j++) {
				if (sightMap[i][j] == 0 || warriorCountGrid[i][j] > 0) {
					// 전사의 대각선 뒷부분 제거
					// 위로 1칸 이동, 왼쪽으로 1칸 이동한 뒤 못 보는 곳으로 변경
					if (j > 0) {
						sightMap[i - 1][j - 1] = 0; // 왼쪽 위 셀의 시야 제거
					}
					// 전사의 일직선 뒷부분 제거
					sightMap[i - 1][j] = 0; // 바로 위 셀의 시야 제거
				}
			}
			// 오른편
			// i행 j열(메두사 y+1 ~ right)까지 전사 검사 
			for (int j = y + 1; j <= right; j++) {
				if (sightMap[i][j] == 0 || warriorCountGrid[i][j] > 0) {
					if (j + 1 < N) {
						// j가 범위 내이면, 위로 1칸, 오른쪽으로 1칸 이동						
						sightMap[i - 1][j + 1] = 0; // 오른쪽 위 셀의 시야 제거
					}
					sightMap[i - 1][j] = 0; // 바로 위 셀의 시야 제거
				}
			}
		}

		// 돌이 된 전사 수. 시야각=1이면서 전사=1인 곳
		int coverage = 0;
		for (int i = x - 1; i >= 0; i--) {
			int left = Math.max(0, y - (x - i));
			int right = Math.min(N - 1, y + (x - i));
			for (int j = left; j <= right; j++) {
				if (sightMap[i][j] == 1) {
					coverage += warriorCountGrid[i][j];
				}
			}
		}

		// 테스트 모드인 경우 시야 맵 초기화
		if (isTest) {
			for (int i = x - 1; i >= 0; i--) {
				int left = Math.max(0, y - (x - i));
				int right = Math.min(N - 1, y + (x - i));
				for (int j = left; j <= right; j++) {
					sightMap[i][j] = 0; // 시야 제거
				}
			}
		}

		return coverage; // 커버리지 반환
	}

	/**
	 * 아래쪽 방향으로 시야를 설정하는 메소드
	 * 
	 * @param x                현재 플레이어의 x 좌표
	 * @param y                현재 플레이어의 y 좌표
	 * @param N                그리드의 크기
	 * @param isTest           테스트 모드 여부 (true: 테스트, false: 실제 적용)
	 * @param warriorCountGrid 각 셀에 있는 전사의 수
	 * @param sightMap         현재 시야 상태 (1: 시야 내, 0: 시야 외)
	 * @return 시야로 커버된 전사의 수
	 */
	static int sightDown(int x, int y, int N, boolean isTest, int[][] warriorCountGrid, int[][] sightMap) {
		// 다이아몬드 형태로 아래쪽 셀을 시야에 포함시킴
		for (int i = x + 1; i < N; i++) {
			int left = Math.max(0, y - (i - x));
			int right = Math.min(N - 1, y + (i - x));
			for (int j = left; j <= right; j++) {
				sightMap[i][j] = 1; // 시야 설정
			}
		}

		// 장애물 처리: 시야 막힘 여부 확인
		boolean obstructionFound = false;
		for (int i = x + 1; i < N; i++) {
			if (obstructionFound) {
				sightMap[i][y] = 0; // 장애물이 발견된 후에는 시야 제거
			} else {
				sightMap[i][y] = 1; // 장애물이 발견되지 않으면 시야 유지
			}

			if (warriorCountGrid[i][y] > 0) {
				obstructionFound = true; // 전사가 있는 경우 장애물로 간주
			}
		}

		// 장애물에 따라 시야 조정
		for (int i = x + 1; i < N - 1; i++) {
			int left = Math.max(0, y - (i - x));
			int right = Math.min(N - 1, y + (i - x));

			// 왼쪽 측면 조정
			for (int j = left; j < y; j++) {
				if (sightMap[i][j] == 0 || warriorCountGrid[i][j] > 0) {
					if (j > 0) {
						sightMap[i + 1][j - 1] = 0; // 왼쪽 아래 셀의 시야 제거
					}
					sightMap[i + 1][j] = 0; // 바로 아래 셀의 시야 제거
				}
			}

			// 오른쪽 측면 조정
			for (int j = y + 1; j <= right; j++) {
				if (sightMap[i][j] == 0 || warriorCountGrid[i][j] > 0) {
					if (j + 1 < N) {
						sightMap[i + 1][j + 1] = 0; // 오른쪽 아래 셀의 시야 제거
					}
					sightMap[i + 1][j] = 0; // 바로 아래 셀의 시야 제거
				}
			}
		}

		// 시야로 커버된 전사 수 계산
		int coverage = 0;
		for (int i = x + 1; i < N; i++) {
			int left = Math.max(0, y - (i - x));
			int right = Math.min(N - 1, y + (i - x));
			for (int j = left; j <= right; j++) {
				if (sightMap[i][j] == 1) {
					coverage += warriorCountGrid[i][j];
				}
			}
		}

		// 테스트 모드인 경우 시야 맵을 원래대로 되돌림
		if (isTest) {
			for (int i = x + 1; i < N; i++) {
				int left = Math.max(0, y - (i - x));
				int right = Math.min(N - 1, y + (i - x));
				for (int j = left; j <= right; j++) {
					sightMap[i][j] = 0; // 시야 제거
				}
			}
		}

		return coverage; // 커버리지 반환
	}

	/**
	 * 왼쪽 방향으로 시야를 설정하는 메소드
	 * 
	 * @param x                현재 플레이어의 x 좌표
	 * @param y                현재 플레이어의 y 좌표
	 * @param N                그리드의 크기
	 * @param isTest           테스트 모드 여부 (true: 테스트, false: 실제 적용)
	 * @param warriorCountGrid 각 셀에 있는 전사의 수
	 * @param sightMap         현재 시야 상태 (1: 시야 내, 0: 시야 외)
	 * @return 시야로 커버된 전사의 수
	 */
	static int sightLeft(int x, int y, int N, boolean isTest, int[][] warriorCountGrid, int[][] sightMap) {
		// 다이아몬드 형태로 왼쪽 셀을 시야에 포함시킴
		for (int i = y - 1; i >= 0; i--) {
			int top = Math.max(0, x - (y - i));
			int bottom = Math.min(N - 1, x + (y - i));
			for (int j = top; j <= bottom; j++) {
				sightMap[j][i] = 1; // 시야 설정
			}
		}

		// 장애물 처리: 시야 막힘 여부 확인
		boolean obstructionFound = false;
		for (int i = y - 1; i >= 0; i--) {
			if (obstructionFound) {
				sightMap[x][i] = 0; // 장애물이 발견된 후에는 시야 제거
			} else {
				sightMap[x][i] = 1; // 장애물이 발견되지 않으면 시야 유지
			}

			if (warriorCountGrid[x][i] > 0) {
				obstructionFound = true; // 전사가 있는 경우 장애물로 간주
			}
		}

		// 장애물에 따라 시야 조정
		for (int i = y - 1; i > 0; i--) {
			int top = Math.max(0, x - (y - i));
			int bottom = Math.min(N - 1, x + (y - i));

			// 상단 측면 조정
			for (int j = top; j < x; j++) {
				if (sightMap[j][i] == 0 || warriorCountGrid[j][i] > 0) {
					if (j > 0) {
						sightMap[j - 1][i - 1] = 0; // 왼쪽 위 셀의 시야 제거
					}
					sightMap[j][i - 1] = 0; // 바로 왼쪽 셀의 시야 제거
				}
			}

			// 하단 측면 조정
			for (int j = x + 1; j <= bottom; j++) {
				if (sightMap[j][i] == 0 || warriorCountGrid[j][i] > 0) {
					if (j + 1 < N) {
						sightMap[j + 1][i - 1] = 0; // 왼쪽 아래 셀의 시야 제거
					}
					sightMap[j][i - 1] = 0; // 바로 왼쪽 셀의 시야 제거
				}
			}
		}

		// 시야로 커버된 전사 수 계산
		int coverage = 0;
		for (int i = y - 1; i >= 0; i--) {
			int top = Math.max(0, x - (y - i));
			int bottom = Math.min(N - 1, x + (y - i));
			for (int j = top; j <= bottom; j++) {
				if (sightMap[j][i] == 1) {
					coverage += warriorCountGrid[j][i];
				}
			}
		}

		// 테스트 모드인 경우 시야 맵을 원래대로 되돌림
		if (isTest) {
			for (int i = y - 1; i >= 0; i--) {
				int top = Math.max(0, x - (y - i));
				int bottom = Math.min(N - 1, x + (y - i));
				for (int j = top; j <= bottom; j++) {
					sightMap[j][i] = 0; // 시야 제거
				}
			}
		}

		return coverage; // 커버리지 반환
	}

	/**
	 * 오른쪽 방향으로 시야를 설정하는 메소드
	 * 
	 * @param x                현재 플레이어의 x 좌표
	 * @param y                현재 플레이어의 y 좌표
	 * @param N                그리드의 크기
	 * @param isTest           테스트 모드 여부 (true: 테스트, false: 실제 적용)
	 * @param warriorCountGrid 각 셀에 있는 전사의 수
	 * @param sightMap         현재 시야 상태 (1: 시야 내, 0: 시야 외)
	 * @return 시야로 커버된 전사의 수
	 */
	static int sightRight(int x, int y, int N, boolean isTest, int[][] warriorCountGrid, int[][] sightMap) {
		// 다이아몬드 형태로 오른쪽 셀을 시야에 포함시킴
		for (int i = y + 1; i < N; i++) {
			int top = Math.max(0, x - (i - y));
			int bottom = Math.min(N - 1, x + (i - y));
			for (int j = top; j <= bottom; j++) {
				sightMap[j][i] = 1; // 시야 설정
			}
		}

		// 장애물 처리: 시야 막힘 여부 확인
		boolean obstructionFound = false;
		for (int i = y + 1; i < N; i++) {
			if (obstructionFound) {
				sightMap[x][i] = 0; // 장애물이 발견된 후에는 시야 제거
			} else {
				sightMap[x][i] = 1; // 장애물이 발견되지 않으면 시야 유지
			}

			if (warriorCountGrid[x][i] > 0) {
				obstructionFound = true; // 전사가 있는 경우 장애물로 간주
			}
		}

		// 장애물에 따라 시야 조정
		for (int i = y + 1; i < N - 1; i++) {
			int top = Math.max(0, x - (i - y));
			int bottom = Math.min(N - 1, x + (i - y));

			// 상단 측면 조정
			for (int j = top; j < x; j++) {
				if (sightMap[j][i] == 0 || warriorCountGrid[j][i] > 0) {
					if (j > 0) {
						sightMap[j - 1][i + 1] = 0; // 오른쪽 위 셀의 시야 제거
					}
					sightMap[j][i + 1] = 0; // 바로 오른쪽 셀의 시야 제거
				}
			}

			// 하단 측면 조정
			for (int j = x + 1; j <= bottom; j++) {
				if (sightMap[j][i] == 0 || warriorCountGrid[j][i] > 0) {
					if (j + 1 < N) {
						sightMap[j + 1][i + 1] = 0; // 오른쪽 아래 셀의 시야 제거
					}
					sightMap[j][i + 1] = 0; // 바로 오른쪽 셀의 시야 제거
				}
			}
		}

		// 시야로 커버된 전사 수 계산
		int coverage = 0;
		for (int i = y + 1; i < N; i++) {
			int top = Math.max(0, x - (i - y));
			int bottom = Math.min(N - 1, x + (i - y));
			for (int j = top; j <= bottom; j++) {
				if (sightMap[j][i] == 1) {
					coverage += warriorCountGrid[j][i];
				}
			}
		}

		// 테스트 모드인 경우 시야 맵을 원래대로 되돌림
		if (isTest) {
			for (int i = y + 1; i < N; i++) {
				int top = Math.max(0, x - (i - y));
				int bottom = Math.min(N - 1, x + (i - y));
				for (int j = top; j <= bottom; j++) {
					sightMap[j][i] = 0; // 시야 제거
				}
			}
		}

		return coverage; // 커버리지 반환
	}

	/**
	 * 플레이어를 향해 전사들을 이동시키는 메소드
	 * 
	 * @param playerX          플레이어의 현재 x 좌표
	 * @param playerY          플레이어의 현재 y 좌표
	 * @param N                그리드의 크기
	 * @param M                전사의 수
	 * @param warriorPositions 전사들의 현재 위치
	 * @param sightMap         현재 시야 상태 (1: 시야 내, 0: 시야 외)
	 * @return (총 이동한 전사 수, 플레이어에게 도달한 전사 수)
	 */
	static Pair<Integer, Integer> moveWarriors(int playerX, int playerY, int N, int M, Point[] warriorPositions,
			int[][] sightMap) {
		int totalMoved = 0; // 총 이동한 전사 수
		int totalHits = 0; // 메두사를 공격한 전사 수

		// DX, DY를 쓰지 않은 건 메소드의 독립성을 유지하기 위해서.
		// 전사의 이동 방향: 상하좌우
		int[] moveDX = { -1, 1, 0, 0 };
		int[] moveDY = { 0, 0, -1, 1 };

		// 3. 전사 이동: 모든 전사에 대해 이동 처리
		for (int i = 0; i < M; i++) {
			// 메두사와 같은 위치에 있는 전사
			if (warriorPositions[i].x == -1)
				continue; // 이미 잡힌 전사는 건너뜀

			int warriorX = warriorPositions[i].x;
			int warriorY = warriorPositions[i].y;

			// 시야 내에 있는 전사는 이동하지 않음
			if (sightMap[warriorX][warriorY] == 1)
				continue;

			// 메두사와의 맨해튼 거리 계산
			int currentDistance = calculateManhattanDistance(new Point(playerX, playerY), new Point(warriorX, warriorY));
			boolean hasMoved = false; // 이동 여부 플래그

			// 1. 첫 번째 이동: 거리를 줄이기 위해 이동
			// 첫 이동은 주어진 우선순위에 따라 상하좌우 순.
			for (int dir = 0; dir < 4; dir++) {
				int nextX = warriorX + moveDX[dir];
				int nextY = warriorY + moveDY[dir];

				// 이동할 위치가 그리드 내에 있고 시야 밖인지 확인
				if (nextX < 0 || nextY < 0 || nextX >= N || nextY >= N)
					continue;
				if (sightMap[nextX][nextY] == 1)
					continue;

				// 새로운 위치에서의 거리 계산
				int newDistance = calculateManhattanDistance(new Point(playerX, playerY), new Point(nextX, nextY));
				if (newDistance < currentDistance) {
					warriorX = nextX;
					warriorY = nextY;
					hasMoved = true;
					totalMoved++;
					break; // 첫 번째 이동 후 루프 탈출
				}
			}

			// 2. 두 번째 이동: 추가로 거리를 줄일 수 있는지 확인
			// 행렬이라서 유리한 방향은 상=좌, 하=우임. 상하좌우순으로 고려했으므로 상하중 1개는 걸림.
			// 이 상태에서 다시 상하좌우로 해봤자 한 번은 이미 선택한 케이스이므로 불필요한 연산 발생.
			// 따라서 oppositeDir로 좌우먼저 계산. 그럼 초대 2번만에 유리한 방향을 구할 수 있음.
			if (hasMoved) {
				int newDistance = calculateManhattanDistance(new Point(playerX, playerY), new Point(warriorX, warriorY));
				for (int dir = 0; dir < 4; dir++) {
					// 반대 방향으로 이동 시도
					int oppositeDir = (dir + 2) % 4;
					int nextX = warriorX + moveDX[oppositeDir];
					int nextY = warriorY + moveDY[oppositeDir];

					// 이동할 위치가 그리드 내에 있고 시야 내에 있지 않은지 확인
					if (nextX < 0 || nextY < 0 || nextX >= N || nextY >= N)
						continue;
					if (sightMap[nextX][nextY] == 1)
						continue;

					// 새로운 위치에서의 거리 계산
					int furtherDistance = calculateManhattanDistance(new Point(playerX, playerY), new Point(nextX, nextY));
					if (furtherDistance < newDistance) {
						warriorX = nextX;
						warriorY = nextY;
						totalMoved++;
						break; // 두 번째 이동 후 루프 탈출
					}
				}
			}

			// 이동 완료 후 전사의 위치로 업데이트
			warriorPositions[i] = new Point(warriorX, warriorY);
		}

		// 4. 메두사 공격 체크
		// 전사 이동 후 플레이어에게 도달한 전사 수 계산
		for (int i = 0; i < M; i++) {
			if (warriorPositions[i].x == -1)
				continue; // 이미 잡힌 전사는 건너뜀

			// 이동 후 메두사와 위치가 동일하면 (-1, -1)로 변경
			if (warriorPositions[i].x == playerX && warriorPositions[i].y == playerY) {
				totalHits++;
				warriorPositions[i] = new Point(-1, -1); // 전사를 잡힌 상태로 표시
			}
		}

		return new Pair<>(totalMoved, totalHits);
	}	

	/**
	 * 두 점 사이의 맨해튼 거리를 계산하는 메소드
	 * 
	 * @param a 첫 번째 점의 좌표 (x, y)
	 * @param b 두 번째 점의 좌표 (x, y)
	 * @return 맨해튼 거리 (xa-xb) + (ya-yb) = (xa+ya) - (xb+yb)
	 */
	static int calculateManhattanDistance(Point a, Point b) {
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}
	
	/**
	 * 간단한 쌍(Pair)을 나타내는 클래스
	 * 
	 * @param <F> 첫 번째 요소의 타입
	 * @param <S> 두 번째 요소의 타입
	 */
	static class Pair<F, S> {
		F first;
		S second;

		Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}
	}
}