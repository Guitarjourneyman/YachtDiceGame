import javax.swing.*;

import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {
    public static void main(String[] args) {
        YachtGUI yachtGUI = new YachtGUI();
    }
}

class YachtGUI extends JFrame implements CommandConstants {
    /* GUI ���� */
    LoginPanel loginPanel;
    LoadPanel loadPanel;
    GamePanel gamePanel;
    GameOverPanel gameOverPanel;
    private static JFrame YachtGUI; // JFrame�� static ������ ����

    /* Client ���� */
    private YachtClient client;

    public YachtGUI(){
        //static �޼ҵ带 ���� ���� ����
        YachtGUI = this;

        setTitle(Constant.NAME);
        setSize(Constant.WIDTH, Constant.HEIGHT);
        setBackground(Color.white);
        setResizable(false);            // ���α׷� â ������ ����
        setLocationRelativeTo(null);            // ���α׷� ���� �� ���߾� ��ġ
        setDefaultCloseOperation(EXIT_ON_CLOSE);   // ���α׷� ���� ����
        setLayout(null);

        loginPanel = new LoginPanel();
        add(loginPanel);

        loadPanel = new LoadPanel();
        loadPanel.setVisible(false);
        add(loadPanel);

        gamePanel = new GamePanel();
        gamePanel.setVisible(false);
        add(gamePanel);

        gameOverPanel = new GameOverPanel();
        gameOverPanel.setVisible(false);
        add(gameOverPanel);
        
        setVisible(true);
    }

    /*Frame�� �����ϴ� �޼ҵ�*/
    public static void closeFrame() {
        YachtGUI.dispose();
    }

    /*Panel ����*/
    class LoginPanel extends JPanel implements ActionListener {
        private JPanel namePanel;
        private JLabel nameLabel;
        private JButton enterBtn;
        private JTextField nameField;

        public String getName() {
            String name = nameField.getText();
            if(name == null || name.isEmpty())
                return "null";
            else
                return name;
        }

        public LoginPanel(){
            setSize(Constant.WIDTH, 600);
            setBackground(Color.WHITE);
            setLayout(null);
            setLocation(0, 0);

            namePanel = new JPanel();
            namePanel.setSize(50,40);
            namePanel.setLayout(null);
            namePanel.setLocation(220, 280);
            namePanel.setVisible(true);
            namePanel.setBackground(Constant.GRAY1);

            nameLabel = new JLabel();
            nameLabel.setSize(50,40);
            nameLabel.setLocation(0, 0);
            nameLabel.setText("�̸�:");
            nameLabel.setFont(new Font("BOLD", Font.BOLD, 12));
            nameLabel.setHorizontalAlignment(JLabel.CENTER);
            nameLabel.setVisible(true);
            namePanel.add(nameLabel);
            add(namePanel);

            nameField = new JTextField();
            nameField.setSize(170, 40);
            nameField.setLocation(270, 280);
            nameField.setVisible(true);
            add(nameField);

            enterBtn = new JButton();
            enterBtn.setSize(80,60);
            enterBtn.setFont(new Font("BOLD", Font.BOLD, 12));
            enterBtn.setHorizontalAlignment(JButton.CENTER);
            enterBtn.setText("ENTER");
            enterBtn.setLocation(500, 270);
            enterBtn.setVisible(true);
            enterBtn.addActionListener(this);
            add(enterBtn);

            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = getName();
            client = new YachtClient(name);
            client.setPanel(loginPanel, loadPanel, gamePanel, gameOverPanel);

            String command = CtoS_LOGIN + " " + name;
            client.sendToServer(command);
        }
    }

    class LoadPanel extends JPanel{
        JLabel loading = new JLabel();

        public LoadPanel(){
            setSize(Constant.WIDTH, 600);
            setBackground(Color.WHITE);
            setLayout(null);
            setLocation(0, 0);

            loading.setSize(400,300);
            loading.setLocation(200, 150);
            loading.setText("Loading...");
            loading.setFont(new Font("BOLD", Font.BOLD, 60));
            loading.setHorizontalAlignment(JLabel.CENTER);
            loading.setVisible(true);
            add(loading);
        }
    }

    class GamePanel extends JPanel {
        int rollCount = Constant.ROLLCOUNT;

        ScoreBoard scoreBoard; //������
        DiceButton diceArray[] = new DiceButton[Constant.DICENUM]; //�ֻ��� 5��
        JLabel rollCountLabel = new JLabel(); //�ֻ��� ���� Ƚ��

        JButton rollButton = new JButton("ROLL"); //�ֻ��� ���� ��ư
        JButton scoreButton = new JButton("GET SCORE"); //���� �ݿ� ��ư

        public void onButton() {
            rollButton.setVisible(true);
            scoreButton.setVisible(true);
        }
        
        public void offButton() {
            rollButton.setVisible(false);
            scoreButton.setVisible(false);
        }
        
        public GamePanel(){
            setSize(Constant.WIDTH, 600);
            setBackground(Color.WHITE);
            setLayout(null);
            setLocation(0, 0);

            //SCOREBOARD PANEL SET. �ӽ÷� �־����.
            scoreBoard = new ScoreBoard(null, null);

            scoreBoard.setLocation(0, 0); //��ġ ����
            add(scoreBoard); //frame�� �߰�.

            //DICE SET. �ӽ÷� �־����.
            for(int i=0;i<Constant.DICENUM;i++) {
                diceArray[i] = new DiceButton((int)(Math.random()*6+1));
                diceArray[i].setLocation(Constant.DICEPOSX[i],Constant.DICEPOSY[i]);;
                add(diceArray[i]);
            }

            // roll button ����
            rollButton.setFont(new Font(Constant.DEFAULT_FONT, Font.BOLD, Constant.FONT_SIZE1));
            rollButton.setLocation(532, 330);
            rollButton.setSize(135, 51);

            // roll button ������ ���� ����
            rollButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    /**
                    * �ֻ��� ���� �� �ִ� Ƚ�� üũ, �������� ���� �ֻ��� üũ
                     *
                    * Ƚ���� ���������� ������ CtoS_ROLL ��� ����
                    */
                    if(rollCount > 0) { // �ֻ��� Ƚ���� 0���� Ŭ ��(�ֻ����� ���� �� ���� ��)
                        // �������� ���� �ֻ��� ó���ϴ� ���� �߰��ؾ���.
                        String dicesPressed = "";
                        for(int i=0; i<Constant.DICENUM; i++) {
                            dicesPressed += " " + (diceArray[i].isPressed() ? 1 : 0);
                        }
                        
                        String command = CtoS_ROLL + dicesPressed;
                        //System.out.println(client.name + ": roll ��ư ����. ���� Ƚ��: " + rollCount);

                        client.sendToServer(command);

                        rollCount--;
                        rollCountLabel.setText("ROLL LEFT : "+ rollCount +" / "+Constant.ROLLCOUNT);
                    } else {            // �ֻ��� Ƚ���� 0 ������ ��(�ֻ����� ���� �� ���� ��)
                        //JOptionPane.showMessageDialog(null, "�ֻ��� Ƚ���� ��� �����߽��ϴ�.");
                    }
                }
            });
            add(rollButton);

            //SCORE BUTTON SET.
            scoreButton.setFont(new Font(Constant.DEFAULT_FONT, Font.BOLD, Constant.FONT_SIZE1));
            scoreButton.setLocation(532, 403);
            scoreButton.setSize(135, 51);
            scoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = CtoS_GETSCORE + " " + client.name + " ";
                // � ĭ�� ������ ������ Ȯ��. ���õ� ��ư�� ĭ���� ������ �Էµ�
                int sel_index = -1;
                for (int i = 0; i < Constant.CATENUMS; i++) {
                    if (scoreBoard.SELBUTTON[i].isSelected() && scoreBoard.SELBUTTON[i].isVisible()) {
                        sel_index = i;
                        break;
                    }
                }
               //�÷��̾��� ���� Ȯ���ϱ����ؼ� ���ھ� ���忡 ������ �Է��ϰԵǸ� ��ü ���� ����
                if (sel_index != -1) {
                    //System.out.println("score ��ư �������!!");
                    command += sel_index;
                    client.sendToServer(command);
                    client.PLAYER_TURN++;
                    if(client.PLAYER_TURN > 12) {
                        //12���� ���� ����Ǹ� ������ ��ȣ�� ������.
                        command ="";
                        command += CtoS_GAMEOVER + " " + client.name +" ";
                        client.sendToServer(command);
                    }
                }
            }

        });
        add(scoreButton);

            //ROLL COUNT SET.
            rollCountLabel.setSize(260,50);
            rollCountLabel.setHorizontalAlignment(JLabel.CENTER);
            rollCountLabel.setLocation(470, 20);
            rollCountLabel.setFont(new Font(Constant.DEFAULT_FONT, Font.BOLD, Constant.FONT_SIZE2));
            rollCountLabel.setText("ROLL LEFT : "+ rollCount +" / "+Constant.ROLLCOUNT);
            add(rollCountLabel);
        }
    }

    public static void setDicesValue(DiceButton[] diceButtons, int[] diceValues) {
        int num = diceButtons.length;

        for(int i=0; i<num; i++) {
            if(diceValues[i] != 0)
                diceButtons[i].Updatedice(diceValues[i]);
        }
    }

    class DiceButton extends JButton implements ActionListener {
        private int diceValue;
        private boolean pressed = false;
        private ImageIcon diceImage;

        public DiceButton(int diceValue) {
            this.diceValue = diceValue;
            this.pressed = false;

            // �ֻ��� ���� ���� ���� �̹��� �ε�
            diceImage = new ImageIcon("Dicepng/" + diceValue + ".png");

            setIcon(diceImage);
            //setContentAreaFilled(false);

            addActionListener(this);

            setSize(75,75);
            setBackground(Constant.GRAY1);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            pressed = !pressed;
            if (pressed) {
                setBorder(BorderFactory.createLineBorder(Color.RED, 5));  // �׵θ� ���ϰ�
            } else {
                setBorder(BorderFactory.createEmptyBorder());  // ���� ���·�
            }
        }

        public boolean isPressed(){
            return pressed;
        }

        public int getDice(){
            return diceValue;
        }

        //�ֻ��� ���� �缳��.
        public void Updatedice(int diceValue) {
            this.diceValue = diceValue;
            diceImage = new ImageIcon("Dicepng/" + diceValue + ".png");
            setIcon(diceImage);
        }
    }
    
    class ScoreBoard extends JPanel {
        JPanel TURNLEFT = new JPanel();
        JLabel TURNLEFT_1 = new JLabel();
        JLabel TURNINT = new JLabel();

        JPanel CATEGORIES = new JPanel();
        JLabel CATEGORIES_1 = new JLabel();

        CATEPanel CATEPanel[] = new CATEPanel[Constant.CATENUMS];

        SelButton SELBUTTON[] = new SelButton[Constant.CATENUMS];
        ButtonGroup SELBUTTONGroup = new ButtonGroup();
        JPanel SELBUTTONPANEL;

        PlayerScoredIsPlay p1Score;
        PlayerScoredIsPlay p2Score;
        
        public void setPlayersName(String player1, String player2) {
            p2Score.setPlayerName(player2);
            p1Score.setPlayerName(player1);
        }

        public String getp1name(){
            return p1Score.getName();
        }

        public String getp2name(){
            return p2Score.getName();
        }

        public ScoreBoard(String p1, String p2) {
            setSize(400, Constant.HEIGHT);
            setLayout(null);
            setBackground(Color.GRAY);

            //set TURNLEFT
            //TURN LEFT �г�
            TURNLEFT.setSize(140, 80);
            TURNLEFT.setLocation(0, 0);
            TURNLEFT.setLayout(null);
            TURNLEFT.setBackground(Constant.GRAY1);
            TURNLEFT.setBorder(new LineBorder(Color.BLACK));

            //TURN LEFT �� ����
            TURNLEFT_1.setSize(88, 15);
            TURNLEFT_1.setLocation(31, 25);
            TURNLEFT_1.setFont(new Font("BOLD", Font.BOLD, 14));
            TURNLEFT_1.setText("TURN LEFT");
            TURNLEFT.add(TURNLEFT_1);

            // turn / 12 ����
            TURNINT.setSize(33,14);
            TURNINT.setLocation(53, 45);
            TURNINT.setLayout(null);
            TURNINT.setFont(new Font("BOLD", Font.BOLD, 12));
            TURNINT.setText(" 1/12");
            TURNLEFT.add(TURNINT);
            add(TURNLEFT);

            //set CATEGORIES
            //ī�װ� �г�
            CATEGORIES.setSize(140,40);
            CATEGORIES.setLocation(0, 80);
            CATEGORIES.setLayout(null);
            CATEGORIES.setBackground(Constant.GRAY1);
            CATEGORIES.setBorder(new LineBorder(Color.BLACK));

            //ī�װ� ��
            CATEGORIES_1.setSize(75,15);
            CATEGORIES_1.setLocation(32, 12);
            CATEGORIES_1.setFont(new Font("BOLD", Font.BOLD, 12));
            CATEGORIES_1.setText("CATEGORIES");
            CATEGORIES.add(CATEGORIES_1);
            add(CATEGORIES);

            //ī�װ� �̸��� ����.
            for(int i=0;i<Constant.CATENUMS;i++){
                if(i != 7){
                    CATEPanel[i] = new CATEPanel(Constant.CATE[i]);
                    CATEPanel[i].setLocation(40, 120+32*i);
                    add(CATEPanel[i]);
                }
                else{
                    JPanel CATENULL = new JPanel();
                    CATENULL.setBackground(Constant.GRAY1);
                    CATENULL.setLayout(null);
                    CATENULL.setSize(100,32);
                    CATENULL.setVisible(rootPaneCheckingEnabled);
                    CATENULL.setLocation(40, 120+32*i);
                    add(CATENULL);
                }
            }

            //set CATESEL Radiobuttons
            for(int i=0;i<Constant.CATENUMS;i++){
                //ī�װ� buttongroup display.
                //6,7,14���� ������ �߰��Ҽ� �ִ� �׸��� �ƴϹǷ� �����ϱ� ���� �г� �߰�.
                if(i == 6 || i == 7 || i == 14){
                    SELBUTTON[i] = new SelButton(); //null ����.

                    SELBUTTONPANEL = new JPanel();
                    SELBUTTONPANEL.setSize(40,32);
                    SELBUTTONPANEL.setLayout(null);
                    SELBUTTONPANEL.setBackground(Constant.GRAY1);
                    SELBUTTONPANEL.setLocation(0, 120+32*i);
                    SELBUTTONPANEL.setVisible(true);
                    add(SELBUTTONPANEL);
                }
                else{
                    SELBUTTON[i] = new SelButton();
                    SELBUTTON[i].setLocation(0, 120+32*i);
                    SELBUTTONGroup.add(SELBUTTON[i]);
                    add(SELBUTTON[i]);
                }
            }

            //set PLAYER INDICATES.
            p1Score = new PlayerScoredIsPlay(p1);
            p2Score = new PlayerScoredIsPlay(p2);
            p1Score.setLocation(140,0);
            p2Score.setLocation(270,0);
            add(p1Score);
            add(p2Score);
            setVisible(true);
        }

        public void setTurns(int turn){
            if(turn < 10) TURNINT.setText(" "+turn+"/12");
            else if(turn >=10 && turn <13)TURNINT.setText(turn+"/12");
            //���� ������ �� ���� ������ 13�̵Ǵ� ���� �����ϱ� ���ؼ� �߰���
            else TURNINT.setText("12"+"/12");
        }

        class CATEPanel extends JPanel{
            JLabel CATELables_1 = new JLabel();

            public CATEPanel(String line){
                setSize(100,32);
                setLayout(null);
                setBackground(Constant.GRAY1);
                setBorder(new LineBorder(Color.BLACK));

                CATELables_1.setSize(100,32);
                CATELables_1.setText(line);
                CATELables_1.setHorizontalAlignment(JLabel.CENTER);
                CATELables_1.setFont(new Font("BOLD", Font.BOLD, 12));
                add(CATELables_1);
                setVisible(true);
            }
        }

        class SelButton extends JRadioButton{
            public SelButton(){
                setSize(40, 32);
                setLayout(null);
                setBackground(Constant.GRAY1);
                setBorder(new LineBorder(Color.BLACK));
                setHorizontalAlignment(JRadioButton.CENTER); // ������ư�� ��� ����
                setVerticalAlignment(JRadioButton.CENTER); // ������ư�� ���� �߾� ����
                setVisible(true);
            }
        }

        class PlayerScoredIsPlay extends JPanel{
            JLabel playerNameLabel;
            JPanel playerGrid;

            JPanel scoreGrid[];
            JLabel scores[];
            int[] INT_SCORES = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            boolean[] SCORE_LOCK = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};

            public void setPlayerName(String name) {
                playerNameLabel.setText(name);
            }

            public String getPlayerName(){
                return playerNameLabel.getText();
            }

            public PlayerScoredIsPlay(String name){
                playerNameLabel = new JLabel();
                playerGrid = new JPanel();
                scoreGrid = new JPanel[Constant.CATENUMS];
                scores = new JLabel[Constant.CATENUMS];

                playerGrid.setSize(130,120);
                playerGrid.setLayout(null);
                playerGrid.setVisible(true);
                playerGrid.setBorder(new LineBorder(Color.BLACK));
                playerGrid.setLocation(0, 0);

                playerNameLabel.setSize(130,120);
                playerNameLabel.setHorizontalAlignment(JLabel.CENTER);
                playerNameLabel.setFont(new Font("BOLD", Font.BOLD, 14));
                playerNameLabel.setText(name);
                playerNameLabel.setVisible(true);
                playerGrid.add(playerNameLabel);
                add(playerGrid);

                //SCOREGRID SET.
                for(int i=0;i<Constant.CATENUMS;i++){   
                    scoreGrid[i] = new JPanel();
                    scores[i] = new JLabel();

                    scoreGrid[i].setSize(130,32);
                    scoreGrid[i].setLayout(null);
                    scoreGrid[i].setVisible(true);
                    scoreGrid[i].setBackground(Color.WHITE);
                    scoreGrid[i].setBorder(new LineBorder(Color.BLACK));
                    scoreGrid[i].setLocation(0, 120+32*i);

                    scores[i].setSize(130,32);
                    scores[i].setLayout(null);
                    scores[i].setVisible(true);
                    scores[i].setHorizontalAlignment(JLabel.CENTER);
                    scores[i].setFont(new Font("BOLD", Font.BOLD, 14));
                    scores[i].setText(INT_SCORES[i]+"");
                    scoreGrid[i].add(scores[i]);
                    
                    add(scoreGrid[i]);
                }
                //Bonus ���� ����
                scores[7].setText("");

                setSize(130,600);
                setLayout(null);
                setVisible(true);
            }

        public void setScores(int index, int score) {
            if (!SCORE_LOCK[index]) {
                INT_SCORES[index] += score;
                scores[index].setText(INT_SCORES[index] + "");
                SCORE_LOCK[index] = true;
                if (index >= 0 && index < 6) {
                    INT_SCORES[Constant.SUBTOTAL] += score;
                    scores[Constant.SUBTOTAL].setText(INT_SCORES[Constant.SUBTOTAL] + "");
                }
                INT_SCORES[Constant.TOTAL] += score;
                scores[Constant.TOTAL].setText(INT_SCORES[Constant.TOTAL] + "");
            }
        }

        public void setNames(String name) {
            playerNameLabel.setText(name);
        }
        }
    }
    
    class GameOverPanel extends JPanel{
        JLabel over = new JLabel();
        JLabel winnerdisplay = new JLabel();
        JButton overButton = new JButton();
    
        public GameOverPanel(){
            setSize(Constant.WIDTH, 800);
            setBackground(Color.WHITE);
            setLayout(null);
            setLocation(0, 0);
    
            over.setSize(400,300);
            over.setLocation(200, 40);
            over.setText("Game Over");
            over.setFont(new Font("BOLD", Font.BOLD, 60));
            over.setHorizontalAlignment(JLabel.CENTER);
            over.setVisible(true);
            add(over);
    
            winnerdisplay.setSize(800,300);
            winnerdisplay.setLocation(0, 140);
            winnerdisplay.setText("(name) Wins!");
            winnerdisplay.setFont(new Font("BOLD", Font.BOLD, 48));
            winnerdisplay.setHorizontalAlignment(JLabel.CENTER);
            winnerdisplay.setVisible(true);
            add(winnerdisplay);
    
            overButton.setSize(80, 60);
            overButton.setLocation(360, 380);
            overButton.setLayout(null);
            overButton.setBackground(Constant.GRAY1);
            overButton.setBorder(new LineBorder(Color.BLACK));
            overButton.setText("EXIT");
            overButton.setVisible(true);
            add(overButton);

            overButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String command = "";
                    command += CtoS_EXIT + "";
                    client.sendToServer(command);
                }
            });
        }
    
        public void whowins(String name){
            winnerdisplay.setText(name+" Wins!");
        }
    }
}