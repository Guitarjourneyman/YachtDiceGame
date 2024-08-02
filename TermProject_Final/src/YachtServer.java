

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class YachtServer implements CommandConstants {
    private static final int PORT = 9000;
    static final int MAX_PLAYER = 2;
    static final ArrayList<ServerHandler> players = new ArrayList<>();
    static volatile boolean started = false;    // volatile 을 사용한 이유 알아두기
    static volatile boolean finished = false;
    private static Random random = new Random();
    /*Server가 가지고 있는 정보*/
    static volatile int[] diceValues = {random.nextInt(6) + 1,random.nextInt(6) + 1,random.nextInt(6) + 1,random.nextInt(6) + 1,random.nextInt(6) + 1};
    static volatile int winnerscore = -1;
    static volatile String winnername = null;

    public void broadCasting(String line) {
        System.out.println("Server broadcast : "+line);
        players.get(0).sendToClient(line);
        players.get(1).sendToClient(line);
    }

    public boolean isStarted() {
        return started;
    }

    public void toggleStarted() {
        started = !started;
    }
    
    public void toggleFinished() {
        finished = !finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public static synchronized void addPlayer(ServerHandler player) {
        players.add(player);
    }

    public int calcScore(int[] diceValues, int category) {
        int[] frequencies;    // 각 주사위의 값이 몇 번씩 나왔는지 확인하기 위한 배열
        switch (category) {
            case Constant.ACES:
            case Constant.DEUCES:
            case Constant.THREES:
            case Constant.FOURS:
            case Constant.FIVES:
            case Constant.SIXES:
                return sumDice(diceValues, category);
            case Constant.CHOICES:
                return sumDice(diceValues, -1);
            case Constant.FOUR_OF_A_KIND:
                // 포카드가 맞는지 확인 하는 과정
                boolean isFourKind = false;
                frequencies = getDiceValueFrequency(diceValues);

                for (int frequency : frequencies) {
                    if(frequency >= 4)   // 주사위이 어떤 값이 4번이상 나온 경우 -> Four of Kind
                        isFourKind = true;

                    if(isFourKind)
                        return sumDice(diceValues, -1);
                }
                return 0;
            case Constant.FULL_HOUSE:
                // 풀 하우스가 맞는지 확인하는 과정
                boolean is3Kind = false;    // 주사위 어떤 값이 3번 나온 경우를 확인하기 위한 변수
                boolean is2Kind = false;    // 주사위 어떤 값이 2번 나온 경우를 확인하기 위한 변수

                frequencies = getDiceValueFrequency(diceValues);

                for (int frequency : frequencies) {
                    if(frequency == 3)
                        is3Kind = true;
                    if (frequency == 2)
                        is2Kind = true;
                    if(is3Kind && is2Kind)
                        return sumDice(diceValues, -1);
                }

                return 0;
            case Constant.SMALL_STRAIGHT:
                // Small Straight 확인하는 과정
                // 가능한 조합 : [1,2,3,4], [2,3,4,5], [3,4,5,6] 3가지가 끝
                frequencies = getDiceValueFrequency(diceValues);

                for (int i = 0; i < 3; i++) {
                    int sequence = 0;   // 4번 연속된 주사위 값을 체크하기 위한 변수
                    for (int j = i; j < i+4; j++) {
                        if (frequencies[j] > 0) {
                            sequence++;
                        }
                    }
                    if(sequence == 4)
                        return 15;
                }
                return 0;

            case Constant.LARGE_STRAIGHT:
                // Small Straight 확인하는 과정
                // 가능한 조합 : [1,2,3,4,5], [2,3,4,5,6] 2가지가 끝
                frequencies = getDiceValueFrequency(diceValues);

                for (int i = 0; i < 2; i++) {
                    int sequence = 0;   // 4번 연속된 주사위 값을 체크하기 위한 변수
                    for (int j = i; j < i+5; j++) {
                        if (frequencies[j] > 0) {
                            sequence++;
                        }
                    }
                    if(sequence == 5)
                        return 30;
                }
                return 0;
            case Constant.YAHTZEE:
                frequencies = getDiceValueFrequency(diceValues);

                for (int frequency : frequencies) {
                    if(frequency == 5)
                        return 50;
                }
                return 0;
            default:
                //System.out.println("getScore 점수 계산 과정 에러: [카테고리 종류 에러]");
                return -1;
        }
    }

    /**
     * getDiceValueFrequency : 각 주사위의 값들이 몇 번씩 나왔는지 배열을 만들어서 반환해주는 함수
     */
    private int[] getDiceValueFrequency(int[] diceValues) {
        int[] frequency = new int[6];

        for (int i = 0; i < Constant.DICENUM; i++)
            frequency[diceValues[i] - 1]++;

        return frequency;
    }

    /**
     * sumDice - 주사위 배열과 category 정보로 값을 계산해서 반환해주는 함수
     * @param diceValues : 주사위 값들이 담긴 배열
     * @param requiredValue : 0이면 모든 주사위의 값들의 합을 반환, ONES ~ SIXES 사이 값이면 해당 카테고리 값 계산해서 반환
     * @return
     */
    private int sumDice(int[] diceValues, int requiredValue) {
        // requiredValue 가
        // 이면 그 값에 해당하는 주사위 값만 더해서 반환
        // Constant 에서 ACES, DEUCES... 가 0부터 시작한다. 그래서 diceValue랑 비교할 때 +1 해서 비교한다.
        int result = 0;
        if (requiredValue == -1) {
            for(int i=0; i<Constant.DICENUM; i++)
                result += diceValues[i];
        } else {
            for (int i = 0; i < Constant.DICENUM; i++) {
                if(diceValues[i] == (requiredValue+1))
                    result += diceValues[i];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        YachtServer server = new YachtServer();
        server.start();
    }

    public void start() {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);

            while(!isStarted()) {
                try {
                    clientSocket = serverSocket.accept();

                    ServerHandler serverHandler = new ServerHandler(clientSocket, this);
                    Thread th = new Thread(serverHandler);
                    addPlayer(serverHandler);

                    //몇 명 들어 왔는 지 확인 코드
                    //System.out.println("현재 서버에 "+ players.size() + " 명");
                    th.start();

                    if (players.size() == MAX_PLAYER) {
                        toggleStarted();
                        /*서로의 서버핸들러에 상대방의 정보 전달*/
                        players.get(0).setOpponent(players.get(1));
                        players.get(1).setOpponent(players.get(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Yacht 계산 관련 추가할 예정
     */
    public int[] getDiceValues(boolean[] noroll) {
        diceValues = new int[Constant.DICENUM];
        for(int i=0; i<diceValues.length; i++)
            diceValues[i] = random.nextInt(6) + 1;
        return diceValues;
    }
    
    
}

