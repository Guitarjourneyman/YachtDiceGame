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
                //System.out.println(name + " readLine ��� ��");
                String line = in.readLine();
                //System.out.println(name + " ServerHandler : " + line);
                //System.out.println(name + " readLine ���� ��");
                if (line == null) throw new IOException();

                st = new StringTokenizer(line);
                
                int cmd = Integer.parseInt(st.nextToken());

                String command;
                switch (cmd) {
                    case CtoS_LOGIN:
                        // CtoS_LOGIN : Ŭ���̾�Ʈ�� �α����� ��û�� ���
                        name = st.nextToken();

                        command = StoC_WAIT + "";
                        sendToClient(command);
                        while (!yachtServer.isStarted()) {;}

                        // �� Ŭ���̾�Ʈ���� ������� ��.
                        String player1 = YachtServer.players.get(0).getName();
                        String player2 = YachtServer.players.get(1).getName();
                        
                        if(player1 != null && player2 != null){
                            // StoC_GAME : gamePanel�� �̵���Ű�� ���. player1�� player2�� �̸� ����
                            command = StoC_GAME + " " + player1 + " " + player2;
                            yachtServer.broadCasting(command);
                        }
                        
                        // StoC_TURN : player1�� turn�� �ǹ� 
                        command = StoC_TURN + " " + player1;
                        yachtServer.broadCasting(command);
                        
                        // StoC_DICES : dice ������ ������. - 4 5 2 1 6
                        // ���⼭�� �ֻ��� �ʱⰪ ������.
                        command = "";
                        command += StoC_DICES;
                        
                        int[] diceValues1 = yachtServer.getDiceValues(reset);
                        for (int value : diceValues1)
                            command += " " + value;
                        yachtServer.broadCasting(command);
                        break;
                    case CtoS_ROLL:
                        // CtoS_ROLL 0 0 1 0 1 : Ŭ���̾�Ʈ�� �ֻ��� roll ��ư�� ���� ��� ���
                        // �ڿ� ������ �ǹ̴� 1�� ��ġ�� �ֻ����� �������� �ʰڴٴ� �ǹ�.
                        // ó�� ����� 1�� ��ġ�� �ֻ��� ���� 0���� �����ְ� Ŭ���̾�Ʈ������ �ֻ��� ���� 0�̸� ���� �������� �ʰ� �����Ѵ�.
                        boolean[] dicesNoChange = new boolean[Constant.DICENUM];  // �ش� �ֻ��� ���� �ٲܰŸ� 1, �ٲ��� ���� �Ÿ� 0
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
                        // Ŭ���̾�Ʈ�� GET_SCORE��ư�� ���� ���
                        name = st.nextToken();
                        int category = Integer.parseInt(st.nextToken());

                        // �ֻ��� �� ��� ������ �����Ұ�.
                        int[] diceValues3 = YachtServer.diceValues;
                        int score_sum = yachtServer.calcScore(diceValues3, category);

                        command = "";
                        command += StoC_GETSCORE + " " + name + " ";
                        command += category + " " + score_sum;
                        //System.out.println(command + "����!!");
                        yachtServer.broadCasting(command);
                        /* �г� ����� */

                        command = "";
                        command += StoC_TURN + " " + opponent.name + " ";
                        // Getscore��ư�� ���� �÷��̾��� ������� �̸��� ����
                        yachtServer.broadCasting(command);
                        //System.out.println("�÷��̾� �̸�: " + command);

                        // ���� �ٲ������ ���ο� �ֻ��� ������.
                        command = "";
                        command += StoC_DICES;
                        int[] diceValues4 = yachtServer.getDiceValues(reset);
                        for (int value : diceValues4)
                            command += " " + value;
                        yachtServer.broadCasting(command);
                        break;
                    case CtoS_GAMEOVER:
                        yachtServer.toggleFinished();
                        //System.out.println("�� �� �� �� ���  !!");
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
                        
                        /*������ ���� ��뵵 �� ����*/
                        opponent.socket.close();
                        opponent.in.close();
                        opponent.out.close();
                        /* ������ ���� ���� �ʰ� �ٽ� ������ �� �ֵ��� ��*/
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
