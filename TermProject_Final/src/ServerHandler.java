import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerHandler implements Runnable, CommandConstants {
    private String name;
    private Socket socket;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private YachtServer yachtServer;
    private ServerHandler opponent = null;
    boolean[] reset = { false, false, false, false, false };
    
    public ServerHandler(Socket s, YachtServer yachtServer) {
        this.yachtServer = yachtServer;
        try {
            socket = s;
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToClient(String command) {
        out.println(command);
    }
    
    public void setOpponent(ServerHandler opponent) {
        this.opponent = opponent;
    }
    
    @Override
    public void run() {
        StringTokenizer st = null;
        while (true) {
            try {
                //System.out.println(name + " readLine 대기 중");
                String line = in.readLine();
                //System.out.println(name + " ServerHandler : " + line);
                //System.out.println(name + " readLine 실행 후");
                if (line == null) throw new IOException();

                st = new StringTokenizer(line);
                
                int cmd = Integer.parseInt(st.nextToken());

                String command;
                switch (cmd) {
                    case CtoS_LOGIN:
                        // CtoS_LOGIN : 클라이언트가 로그인을 요청한 경우
                        name = st.nextToken();

                        command = StoC_WAIT + "";
                        sendToClient(command);
                        while (!yachtServer.isStarted()) {;}

                        // 두 클라이언트에게 보내줘야 함.
                        String player1 = YachtServer.players.get(0).getName();
                        String player2 = YachtServer.players.get(1).getName();
                        
                        if(player1 != null && player2 != null){
                            // StoC_GAME : gamePanel로 이동시키는 명령. player1과 player2의 이름 전송
                            command = StoC_GAME + " " + player1 + " " + player2;
                            yachtServer.broadCasting(command);
                        }
                        
                        // StoC_TURN : player1의 turn을 의미 
                        command = StoC_TURN + " " + player1;
                        yachtServer.broadCasting(command);
                        
                        // StoC_DICES : dice 값들을 보내줌. - 4 5 2 1 6
                        // 여기서는 주사위 초기값 보내줌.
                        command = "";
                        command += StoC_DICES;
                        
                        int[] diceValues1 = yachtServer.getDiceValues(reset);
                        for (int value : diceValues1)
                            command += " " + value;
                        yachtServer.broadCasting(command);
                        break;
                    case CtoS_ROLL:
                        // CtoS_ROLL 0 0 1 0 1 : 클라이언트가 주사위 roll 버튼을 누른 경우 명령
                        // 뒤에 숫자의 의미는 1인 위치의 주사위는 변경하지 않겠다는 의미.
                        // 처리 방법은 1인 위치의 주사위 값을 0으로 보내주고 클라이언트에서는 주사위 값이 0이면 값을 변경하지 않게 설정한다.
                        boolean[] dicesNoChange = new boolean[Constant.DICENUM];  // 해당 주사위 값을 바꿀거면 1, 바꾸지 않을 거면 0
                        for (int i = 0; i < Constant.DICENUM; i++)
                            dicesNoChange[i] = "1".equals(st.nextToken()) ? true : false;

                        command = "";
                        command += StoC_DICES;

                        int[] diceValues2 = yachtServer.getDiceValues(reset);
                        for (int i = 0; i < Constant.DICENUM; i++) {
                            if (!dicesNoChange[i])
                                command += " " + diceValues2[i];
                            else
                                command += " " + 0;
                        }
                        yachtServer.broadCasting(command);
                        break;
                    case CtoS_GETSCORE:
                        // 클라이언트가 GET_SCORE버튼을 누른 경우
                        name = st.nextToken();
                        int category = Integer.parseInt(st.nextToken());

                        // 주사위 값 대신 점수로 변경할것.
                        int[] diceValues3 = YachtServer.diceValues;
                        int score_sum = yachtServer.calcScore(diceValues3, category);

                        command = "";
                        command += StoC_GETSCORE + " " + name + " ";
                        command += category + " " + score_sum;
                        //System.out.println(command + "보냄!!");
                        yachtServer.broadCasting(command);
                        /* 패널 숨기기 */

                        command = "";
                        command += StoC_TURN + " " + opponent.name + " ";
                        // Getscore버튼을 누른 플레이어의 상대편의 이름을 보냄
                        yachtServer.broadCasting(command);
                        //System.out.println("플레이어 이름: " + command);

                        // 턴이 바뀌었으니 새로운 주사위 보내기.
                        command = "";
                        command += StoC_DICES;
                        int[] diceValues4 = yachtServer.getDiceValues(reset);
                        for (int value : diceValues4)
                            command += " " + value;
                        yachtServer.broadCasting(command);
                        break;
                    case CtoS_GAMEOVER:
                        yachtServer.toggleFinished();
                        //System.out.println("한 명 끝 났 어요  !!");
                        while (yachtServer.isFinished()) {;}
                        command = "";
                        command += StoC_GAMEOVER + " ";
                        yachtServer.broadCasting(command);
                        break;
                    case CtoS_EXIT:
                        command = "";
                        command += StoC_EXIT + "";
                        yachtServer.broadCasting(command);
                        
                        in.close();
                        out.close();
                        socket.close();
                        
                        /*누르지 않은 상대도 다 종료*/
                        opponent.socket.close();
                        opponent.in.close();
                        opponent.out.close();
                        /* 서버는 종료 되지 않고 다시 시작할 수 있도록 함*/
                        YachtServer.started = false;
                        break;
                    default:
                        //System.out.println("Command Type Error!");
                }
            } catch (IOException e) {
                //System.out.println("ServerHandler Error!");
                System.exit(-1);
            }
        }
    }

    public String getName() {
        return name;
    }
}
