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
    
    //서버로 명령어를 보내는 메소드
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
    //클라이언트의 GUI 정보를 받아오는 메소드
    public void setPanel(YachtGUI.LoginPanel loginPanel, YachtGUI.LoadPanel loadPanel, YachtGUI.GamePanel gamePanel, YachtGUI.GameOverPanel gameOverPanel) {
        this.loginPanel = loginPanel;
        this.loadPanel = loadPanel;
        this.gamePanel = gamePanel;
        this.gameOverPanel = gameOverPanel;
    }
    //서버로부터 명령어를 계속해서 읽어 들일 수 있게 스레드로 생성함
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
                        case StoC_WAIT://다른 플레이어가 들어오는 것을 기다릴 때의 로딩화면
                            loginPanel.setVisible(false);
                            loadPanel.setVisible(true);
                            
                            break;
                        case StoC_GAME:
                            // StoC_GAME Player1이름 Player2이름
                            String player1 = st.nextToken();
                            String player2 = st.nextToken();
                            
                            // gamePanel 값 설정하는 과정
                           
                            gamePanel.scoreBoard.setPlayersName(player1, player2);
                            // 누구의 GUI 프레임인지 구분하기위해서 자신의 플레이어에 색을 RED로 바꿈
                            // 내 패널의 이름 정보를 player1을 기준으로 비교함
                            if (name.equals(player1)) 
                                gamePanel.scoreBoard.p1Score.playerNameLabel.setForeground(Color.RED); // 글씨 색을 빨간색으로 설정
                            else
                                gamePanel.scoreBoard.p2Score.playerNameLabel.setForeground(Color.RED); // 글씨 색을 빨간색으로 설정
                            
                            
                            loadPanel.setVisible(false);
                            gamePanel.setVisible(true);
                            break;
                            
                        case StoC_TURN:
                            // StoC_TURN name1 -> name1의 턴.
                            // button on / off 하는 과정 필요
                            String turnName = st.nextToken();    // turn 해야할 client 이름. 
                            //System.out.println(turnName + " Player 패널 변경");
                            //턴 횟수 설정.
                            gamePanel.scoreBoard.setTurns(PLAYER_TURN);

                            if (name.equals(turnName)) {
                                gamePanel.onButton();
                                // rollCount값 초기화
                                gamePanel.rollCount = Constant.ROLLCOUNT;
                                gamePanel.rollCountLabel.setText("ROLL LEFT : " + gamePanel.rollCount + " / " + Constant.ROLLCOUNT);
                            } else {
                                gamePanel.offButton();
                            }
                            break;
                        case StoC_DICES:
                            // StoC_DICES 1 2 3 4 5 -> 주사위 값 5개를 보내주는 명령. 만약에 값이 0이라면 해당 주사위는 변경하지 않는다.
                            int[] diceValues = new int[Constant.DICENUM];
                            for (int i = 0; i < diceValues.length; i++) 
                                diceValues[i] = Integer.parseInt(st.nextToken());
                            //서버로부터 받아온 주사위 값을 GUI에 나타냄
                            YachtGUI.setDicesValue(gamePanel.diceArray, diceValues);
                            break;
                        case StoC_GETSCORE:
                            // 서버에서 받은 점수값을 GUI에 나타내는 과정
                            // StoC_SCORE name 3 12 -> name의 3번째 카테고리에 12를 더하는 명령
                            
                            String plusname = st.nextToken();
                            int index = Integer.parseInt(st.nextToken());
                            int score = Integer.parseInt(st.nextToken());
                            // Player1의 이름과 같으면 Player1의 점수판 갱신
                            if (gamePanel.scoreBoard.p1Score.playerNameLabel.getText().equals(plusname)) {
                              
                                gamePanel.scoreBoard.p1Score.setScores(index, score);
                            } // Player1의 이름과 다르면 Player2의 점수판 갱신
                            else {
                               
                                gamePanel.scoreBoard.p2Score.setScores(index, score);
                            }
                            
                            if (name.equals(plusname)) {
                                gamePanel.scoreBoard.SELBUTTON[index].setVisible(false);
                            }
                            break;
                        case StoC_GAMEOVER: //게임이 끝나면 게임종료패널 ON
                            gamePanel.setVisible(false);
                            gameOverPanel.setVisible(true);
                            
                            System.out.println(gamePanel.scoreBoard.p1Score.playerNameLabel.getText()+" : "+gamePanel.scoreBoard.p1Score.INT_SCORES[Constant.TOTAL]+gamePanel.scoreBoard.p2Score.playerNameLabel.getText()+" : "+gamePanel.scoreBoard.p2Score.INT_SCORES[Constant.TOTAL]);
                            //누가 게임을 이겼는 지 화면에 출력할 수 있도록 GUI의 메소드를 불러옴
                            if(gamePanel.scoreBoard.p1Score.INT_SCORES[Constant.TOTAL] > gamePanel.scoreBoard.p2Score.INT_SCORES[Constant.TOTAL]){
                                gameOverPanel.whowins(gamePanel.scoreBoard.p1Score.playerNameLabel.getText());
                            }
                            else{
                                gameOverPanel.whowins(gamePanel.scoreBoard.p2Score.playerNameLabel.getText());
                            }
                            break;
                        case StoC_EXIT:
                            /* 종료 */
                            YachtGUI.closeFrame();
                            in.close();
                            out.close();
                            socket.close();
                            break;
                        default:
                            System.out.println(name + " 명령 종류 에러@@");
                    }
                } catch (IOException e) {
                    //예외가 발생하면 시스템 종료
                	System.out.println("시스템 ERROR");
                    System.exit(-1);
                }
            }
        }
    }
}
