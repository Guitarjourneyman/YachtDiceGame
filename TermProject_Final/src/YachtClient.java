import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class YachtClient implements CommandConstants {
    String name = null;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private ClientReceiver receiver;
    private YachtGUI.LoginPanel loginPanel;
    private YachtGUI.LoadPanel loadPanel;
    private YachtGUI.GamePanel gamePanel;
    private YachtGUI.GameOverPanel gameOverPanel;
    public int PLAYER_TURN = 1;
    
    //������ ��ɾ ������ �޼ҵ�
    public void sendToServer(String command) {
        out.println(command);
    }

    public YachtClient(String name) {
        try {
            this.name = name;
            socket = new Socket("localhost", 9000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            receiver = new ClientReceiver(in);
            receiver.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Ŭ���̾�Ʈ�� GUI ������ �޾ƿ��� �޼ҵ�
    public void setPanel(YachtGUI.LoginPanel loginPanel, YachtGUI.LoadPanel loadPanel, YachtGUI.GamePanel gamePanel, YachtGUI.GameOverPanel gameOverPanel) {
        this.loginPanel = loginPanel;
        this.loadPanel = loadPanel;
        this.gamePanel = gamePanel;
        this.gameOverPanel = gameOverPanel;
    }
    //�����κ��� ��ɾ ����ؼ� �о� ���� �� �ְ� ������� ������
    class ClientReceiver extends Thread {
        private BufferedReader in;

        public ClientReceiver(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            StringTokenizer st = null;
            
            while (true) {
                try {
                    String line = in.readLine();

                    System.out.println("client read : "+line);

                    if (line == null) throw new IOException();
                    
                    
                    st = new StringTokenizer(line);
                    int cmd = Integer.parseInt(st.nextToken());

                    switch (cmd) {
                        case StoC_WAIT://�ٸ� �÷��̾ ������ ���� ��ٸ� ���� �ε�ȭ��
                            loginPanel.setVisible(false);
                            loadPanel.setVisible(true);
                            
                            break;
                        case StoC_GAME:
                            // StoC_GAME Player1�̸� Player2�̸�
                            String player1 = st.nextToken();
                            String player2 = st.nextToken();
                            
                            // gamePanel �� �����ϴ� ����
                           
                            gamePanel.scoreBoard.setPlayersName(player1, player2);
                            // ������ GUI ���������� �����ϱ����ؼ� �ڽ��� �÷��̾ ���� RED�� �ٲ�
                            // �� �г��� �̸� ������ player1�� �������� ����
                            if (name.equals(player1)) 
                                gamePanel.scoreBoard.p1Score.playerNameLabel.setForeground(Color.RED); // �۾� ���� ���������� ����
                            else
                                gamePanel.scoreBoard.p2Score.playerNameLabel.setForeground(Color.RED); // �۾� ���� ���������� ����
                            
                            
                            loadPanel.setVisible(false);
                            gamePanel.setVisible(true);
                            break;
                            
                        case StoC_TURN:
                            // StoC_TURN name1 -> name1�� ��.
                            // button on / off �ϴ� ���� �ʿ�
                            String turnName = st.nextToken();    // turn �ؾ��� client �̸�. 
                            //System.out.println(turnName + " Player �г� ����");
                            //�� Ƚ�� ����.
                            gamePanel.scoreBoard.setTurns(PLAYER_TURN);

                            if (name.equals(turnName)) {
                                gamePanel.onButton();
                                // rollCount�� �ʱ�ȭ
                                gamePanel.rollCount = Constant.ROLLCOUNT;
                                gamePanel.rollCountLabel.setText("ROLL LEFT : " + gamePanel.rollCount + " / " + Constant.ROLLCOUNT);
                            } else {
                                gamePanel.offButton();
                            }
                            break;
                        case StoC_DICES:
                            // StoC_DICES 1 2 3 4 5 -> �ֻ��� �� 5���� �����ִ� ���. ���࿡ ���� 0�̶�� �ش� �ֻ����� �������� �ʴ´�.
                            int[] diceValues = new int[Constant.DICENUM];
                            for (int i = 0; i < diceValues.length; i++) 
                                diceValues[i] = Integer.parseInt(st.nextToken());
                            //�����κ��� �޾ƿ� �ֻ��� ���� GUI�� ��Ÿ��
                            YachtGUI.setDicesValue(gamePanel.diceArray, diceValues);
                            break;
                        case StoC_GETSCORE:
                            // �������� ���� �������� GUI�� ��Ÿ���� ����
                            // StoC_SCORE name 3 12 -> name�� 3��° ī�װ��� 12�� ���ϴ� ���
                            
                            String plusname = st.nextToken();
                            int index = Integer.parseInt(st.nextToken());
                            int score = Integer.parseInt(st.nextToken());
                            // Player1�� �̸��� ������ Player1�� ������ ����
                            if (gamePanel.scoreBoard.p1Score.playerNameLabel.getText().equals(plusname)) {
                              
                                gamePanel.scoreBoard.p1Score.setScores(index, score);
                            } // Player1�� �̸��� �ٸ��� Player2�� ������ ����
                            else {
                               
                                gamePanel.scoreBoard.p2Score.setScores(index, score);
                            }
                            
                            if (name.equals(plusname)) {
                                gamePanel.scoreBoard.SELBUTTON[index].setVisible(false);
                            }
                            break;
                        case StoC_GAMEOVER: //������ ������ ���������г� ON
                            gamePanel.setVisible(false);
                            gameOverPanel.setVisible(true);
                            
                            System.out.println(gamePanel.scoreBoard.p1Score.playerNameLabel.getText()+" : "+gamePanel.scoreBoard.p1Score.INT_SCORES[Constant.TOTAL]+gamePanel.scoreBoard.p2Score.playerNameLabel.getText()+" : "+gamePanel.scoreBoard.p2Score.INT_SCORES[Constant.TOTAL]);
                            //���� ������ �̰�� �� ȭ�鿡 ����� �� �ֵ��� GUI�� �޼ҵ带 �ҷ���
                            if(gamePanel.scoreBoard.p1Score.INT_SCORES[Constant.TOTAL] > gamePanel.scoreBoard.p2Score.INT_SCORES[Constant.TOTAL]){
                                gameOverPanel.whowins(gamePanel.scoreBoard.p1Score.playerNameLabel.getText());
                            }
                            else{
                                gameOverPanel.whowins(gamePanel.scoreBoard.p2Score.playerNameLabel.getText());
                            }
                            break;
                        case StoC_EXIT:
                            /* ���� */
                            YachtGUI.closeFrame();
                            in.close();
                            out.close();
                            socket.close();
                            break;
                        default:
                            System.out.println(name + " ��� ���� ����@@");
                    }
                } catch (IOException e) {
                    //���ܰ� �߻��ϸ� �ý��� ����
                	System.out.println("�ý��� ERROR");
                    System.exit(-1);
                }
            }
        }
    }
}
