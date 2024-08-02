public interface CommandConstants {
    int ON = 1;
    int OFF = 0;

    // Client -> Server
    int CtoS_LOGIN = 0;
    int CtoS_ROLL = 1;
    int CtoS_GETSCORE = 2;
    int CtoS_GAMEOVER =3;
    int CtoS_EXIT =4;

    // Server -> Client
    int StoC_WAIT = 0;
    int StoC_GAME = 1;
    int StoC_TURN = 2;
    int StoC_DICES = 3;
    int StoC_COUNT = 4;
    int StoC_GETSCORE = 5;
    int StoC_GAMEOVER =6;
    int StoC_EXIT =7;
    
    
}