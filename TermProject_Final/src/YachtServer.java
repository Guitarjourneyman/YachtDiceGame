

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class YachtServer implements CommandConstants {
    private static final int PORT = 9000;
    static final int MAX_PLAYER = 2;
    static final ArrayList<ServerHandler> players = new ArrayList<>();
    static volatile boolean started = false;    // volatile �� ����� ���� �˾Ƶα�
    static volatile boolean finished = false;
    private static Random random = new Random();
    /*Server�� ������ �ִ� ����*/
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
        int[] frequencies;    // �� �ֻ����� ���� �� ���� ���Դ��� Ȯ���ϱ� ���� �迭
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
                // ��ī�尡 �´��� Ȯ�� �ϴ� ����
                boolean isFourKind = false;
                frequencies = getDiceValueFrequency(diceValues);

                for (int frequency : frequencies) {
                    if(frequency >= 4)   // �ֻ����� � ���� 4���̻� ���� ��� -> Four of Kind
                        isFourKind = true;

                    if(isFourKind)
                        return sumDice(diceValues, -1);
                }
                return 0;
            case Constant.FULL_HOUSE:
                // Ǯ �Ͽ콺�� �´��� Ȯ���ϴ� ����
                boolean is3Kind = false;    // �ֻ��� � ���� 3�� ���� ��츦 Ȯ���ϱ� ���� ����
                boolean is2Kind = false;    // �ֻ��� � ���� 2�� ���� ��츦 Ȯ���ϱ� ���� ����

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
                // Small Straight Ȯ���ϴ� ����
                // ������ ���� : [1,2,3,4], [2,3,4,5], [3,4,5,6] 3������ ��
                frequencies = getDiceValueFrequency(diceValues);

                for (int i = 0; i < 3; i++) {
                    int sequence = 0;   // 4�� ���ӵ� �ֻ��� ���� üũ�ϱ� ���� ����
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
                // Small Straight Ȯ���ϴ� ����
                // ������ ���� : [1,2,3,4,5], [2,3,4,5,6] 2������ ��
                frequencies = getDiceValueFrequency(diceValues);

                for (int i = 0; i < 2; i++) {
                    int sequence = 0;   // 4�� ���ӵ� �ֻ��� ���� üũ�ϱ� ���� ����
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
                //System.out.println("getScore ���� ��� ���� ����: [ī�װ� ���� ����]");
                return -1;
        }
    }

    /**
     * getDiceValueFrequency : �� �ֻ����� ������ �� ���� ���Դ��� �迭�� ���� ��ȯ���ִ� �Լ�
     */
    private int[] getDiceValueFrequency(int[] diceValues) {
        int[] frequency = new int[6];

        for (int i = 0; i < Constant.DICENUM; i++)
            frequency[diceValues[i] - 1]++;

        return frequency;
    }

    /**
     * sumDice - �ֻ��� �迭�� category ������ ���� ����ؼ� ��ȯ���ִ� �Լ�
     * @param diceValues : �ֻ��� ������ ��� �迭
     * @param requiredValue : 0�̸� ��� �ֻ����� ������ ���� ��ȯ, ONES ~ SIXES ���� ���̸� �ش� ī�װ� �� ����ؼ� ��ȯ
     * @return
     */
    private int sumDice(int[] diceValues, int requiredValue) {
        // requiredValue ��
        // �̸� �� ���� �ش��ϴ� �ֻ��� ���� ���ؼ� ��ȯ
        // Constant ���� ACES, DEUCES... �� 0���� �����Ѵ�. �׷��� diceValue�� ���� �� +1 �ؼ� ���Ѵ�.
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

                    //�� �� ��� �Դ� �� Ȯ�� �ڵ�
                    //System.out.println("���� ������ "+ players.size() + " ��");
                    th.start();

                    if (players.size() == MAX_PLAYER) {
                        toggleStarted();
                        /*������ �����ڵ鷯�� ������ ���� ����*/
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
     * Yacht ��� ���� �߰��� ����
     */
    public int[] getDiceValues(boolean[] noroll) {
        diceValues = new int[Constant.DICENUM];
        for(int i=0; i<diceValues.length; i++)
            diceValues[i] = random.nextInt(6) + 1;
        return diceValues;
    }
    
    
}

