package test01;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

class Bird extends JFrame implements KeyListener, Runnable {
    int width=1300;    //프레임 가로
    int height=850;    //프레임 높이
    int x=100, y=350;  //플레이어 좌표 변수
    int score=0;    //게임 점수
    int life=5;     //게임 생명

    Bird() {
        getimg();  //사진 불러오기
        gogo();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //프레임 창 닫을 시 프로그램 종료
        setTitle("FlyingFries");  //프레임 이름
        setSize(width, height);  //프레임 크기
        setResizable(false);
        setVisible(true);
        
        Dimension frameSize = getSize();
        Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((windowSize.width - frameSize.width) / 2,
                    (windowSize.height - frameSize.height) / 2); 

    }

    Image player_img;       //플레이어 이미지
    Image background_img;   //배경화면 이미지
    Image explosion_img;    //폭발 이미지
    Image arrow_img;        //깃털 이미지
    Image alien_img;        //후추 이미지
    Image wordBG;           //글자 배경 색 이미지
    Image story_img;      //스토리 이미지
    
    public void getimg() {   // 이미지 가져오기
        try {
            wordBG = new ImageIcon("image/글자배경.png").getImage();
            arrow_img = new ImageIcon("image/feather.png").getImage();        //깃털 이미지
            alien_img = new ImageIcon("image/pepper.gif").getImage();      //후추 이미지
            player_img = new ImageIcon("image/bird.gif").getImage();       //플레이어 이미지
            background_img = new ImageIcon("image/background.png").getImage();   //배경 이미지
            explosion_img = new ImageIcon("image/exploding.png").getImage();    //폭발 이미지
            story_img = new ImageIcon("image/story.gif").getImage();     //스토리 이미지
        } catch (Exception e){
            System.out.println("파일을 열 수 없습니다. ");
        }
    }

    Thread th;  //스레드 생성
    public void gogo() {
       
        addKeyListener(this);       //키보드 이벤트 실행
        th = new Thread(this);  //스레드 생성
        th.start();  //스레드 실행
    }

    Arrow arrow;           //깃털 클래스
    Alien alien;           //후추 클래스
    Explosion explosion;   //폭발 클래스
    class Arrow {
        int x;  //깃털 좌표
        int y;

        Arrow(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void move() {
            x += 11;   //깃털 11만큼 오른쪽으로 쏘기
        }
    }

    class Alien {
        int x;  //후추 좌표
        int y;

        Alien(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void move() {
            x -= alien_speed;     //장애물 7만큼 왼쪽으로 움직이기
        }
    }

    class Explosion {
        int x;  //폭발 효과 좌표
        int y;
        int maintain_int; //폭발 효과가 유지되는 시간을 위한 변수
        int situation;    //폭발이 일어나는 상황
        //0:장애물이 깃털에 맞았을 때, 1:플레이어와 장애물이 충돌했을 때

        Explosion(int x, int y, int situation) {
            this.x = x;
            this.y = y;
            this.situation = situation;
            maintain_int = 0;
        }

        public void maintain() {
            maintain_int++;     //유지될 타임 변수 카운트
        }
    }

    int alien_speed=7;
    int round=1;
    int shoot=0;    //연속으로 발사 조절하기 위한 카운트 변수
    int appear=0;   //장애물 등장 간격 카운트 변수
    int ch_break=0;
    public void run() {  //스레드 무한루프되는 부분
        try {   //예외옵션 설정으로 에러 방지
            while (true) {  //무한루프
            	 if(ch_break==1){
                     break;
                 }
                 if(score==100){     //일정 점수가 될때마다 난이도 높이기 (후추 속도 빠르게 하기)
                     round=2;
                     alien_speed=12;
                 }
                 else if(score==250){
                     round=3;
                     alien_speed=15;
                 }
                 else if(score==350){
                     round=4;
                     alien_speed=17;
                 }
                 else if(score==450){   //난이도는 5단계까지만
                     round=5;
                     alien_speed=19;
                 }
                 else if(score>=500) {
                 	new End_s();
                 	setVisible(false);
                 	break;
                 }
                KeyWok();           //키보드 입력으로 x, y갱신
                WorkGame();         //게임 동작 메소드
                repaint();          //갱신된 값으로 이미지 새로 그리기
                Thread.sleep(20);  //20mill sec의 속도로 스레드 돌리기
                shoot++;    //연속 발사 위해 횟수 카운트
                appear++;   //장애물 등장 간격 위해 횟수 카운트
            }
        } catch (Exception e) {
            System.out.println("오류가 발생하였습니다. ");
        }
    }

    ArrayList arr_arrow = new ArrayList();      //깃털 배열
    ArrayList arr_alien = new ArrayList();      //장애물 배열
    ArrayList arr_explosion = new ArrayList();  //폭발 배열
    public void WorkGame() {
        //후추 동작
        for (int i = 0; i < arr_alien.size(); ++i) {
            alien = (Alien) (arr_alien.get(i));   //배열에 장애물이 만들어져있을 때 해당되는 후추
            alien.move();  //해당 후추 움직이기

            if (Crash_check(x, y, alien.x, alien.y, player_img, alien_img)==1) {    //플레이어가 장애물과 충돌했을 때
                life--;    //생명 하나 줄기
                arr_alien.remove(i);   //해당 장애물 삭제

                explosion = new Explosion(alien.x + alien_img.getWidth(null) / 2, alien.y + alien_img.getHeight(null) / 2, 0);
                //적의 현재 중심 좌표와 폭발 상황 "0" 받기
                arr_explosion.add(explosion);   //충돌된 적의 위치에 폭발 효과 넣기
                explosion = new Explosion(x, y, 1); //플레이어의 현재 좌표와 폭발 상황 "1" 받기
                arr_explosion.add(explosion);   //충돌된 플레이어의 위치에 폭발 효과 넣기
            }
        }
        if (appear == 150) {   //무한 루프 150마다 장애물 등장
        	 alien = new Alien(width + 100, 100);
             arr_alien.add(alien);   //각 좌표에 만든 후 배열에 추가
             alien = new Alien(width + 100, 300);
             arr_alien.add(alien);
             alien = new Alien(width + 100, 500);
             arr_alien.add(alien);
             alien = new Alien(width + 100, 700);
             arr_alien.add(alien);
            appear=0;   //appear 초기화
        }

        //깃털쏘기
        if (Space) {   //스페이스바가 눌러지면
            if (shoot > 15) {   //깃털 연속 발사 간격 조절  //간격을 15번 쯤으로 맞추고 발사
                arrow = new Arrow(x + 150, y + 30);
                arr_arrow.add(arrow);   //현재 깃털 추가
                shoot = 0;  //shoot 초기화
            }
        }

        for (int i = 0; i < arr_arrow.size(); ++i) {
            arrow = (Arrow) arr_arrow.get(i);
            arrow.move();
            for (int j = 0; j < arr_alien.size(); ++j) {
                alien = (Alien) arr_alien.get(j);
                if (Crash_check(arrow.x, arrow.y, alien.x, alien.y, arrow_img, alien_img)==1) {
                    //장애물이 깃털에 맞을 경우 장애물과 깃털 둘 다 화면에서 지우기
                    arr_arrow.remove(i);
                    arr_alien.remove(j);
                    score += 10;  //점수 +10
                    explosion = new Explosion(alien.x + alien_img.getWidth(null) / 2, alien.y + alien_img.getHeight(null) / 2, 0);
                    //적의 현재 중심 좌표와 폭발 상황 "0"받기 (장애물이 활에 맞을 경우)
                    arr_explosion.add(explosion);   //장애물이 사라진 위치에 폭발 효과 추가
                }
            }
        }

        //폭발 효과
        for (int i = 0; i < arr_explosion.size(); ++i) {
            explosion = (Explosion) arr_explosion.get(i);
            explosion.maintain();
        }
    }

    public int Crash_check(int x1, int y1, int x2, int y2, Image img1, Image img2) {  //충돌 메소드
        //두 사각형 이미지의 충돌 여부 확인
        //x1+img1.getWidth(null)/2 는 img1의 중심 좌표
        //x2+img2.getWidth(null)/2 는 img2의 중심 좌표
        //두 값을 빼서 각 중심좌표 간의 거리 얻기
        //img1.getWidth(null)/2, img2.getWidth(null)/2
        //두 이미지의 폭을 반으로 나눈 값들을 더해서 두 이미지가 접촉했을때의 이미지 중심좌표 간의 거리 얻기
        //만약 두 이미지가 떨어져있는 거리 < 두 이미지가 접촉했을 때 중심좌표 간의 거리 => 접촉 => true 리턴
        //아니면 false 리턴
        int check;
        if (Math.abs((x1 + img1.getWidth(null) / 2) - (x2 + img2.getWidth(null) / 2)) < (img2.getWidth(null) / 2 + img1.getWidth(null) / 2)
                && Math.abs((y1 + img1.getHeight(null) / 2) - (y2 + img2.getHeight(null) / 2)) < (img2.getHeight(null) / 2 + img1.getHeight(null) / 2)) {
            check = 1;
        } else {
            check = 0;
        }
        return check;
    }

    Image bufferimg;    //더블 버퍼링
    Graphics bufferg;   //더블 버퍼링
    public void paint(Graphics g) {
        bufferimg = createImage(width, height); //더블버퍼링 버퍼 크기를 화면 크기와 같게 설정
        bufferg = bufferimg.getGraphics();    //버퍼의 그래픽 객체 얻기
        update(g);
    }

    public void update(Graphics g) {
        Print_Background(); //배경 이미지 그리기
        bufferg.drawImage(player_img, x, y, this);   //플레이어 그리기
        Print_Alien();      //후추 출력 메소드 실행
        Print_Arrow();      //깃털 출력 메소드 실행
        Print_Explosion();  //폭발 효과 출력 메소드 실행
        Print_Text();       //게임 진행상황 표시 텍스트 출력 메소드 실행
        if(life==0){        //생명을 다 썼을 경우 게임 오버 창 출력
            Print_GAMEOVER();
        }
        g.drawImage(bufferimg, 0, 0, this);   //화면에 버퍼에 그린 그림을 가져와 그리기
    }

    int move_background = 0;   //배경 움직이는 효과 위한 변수
    public void Print_Background() {    //배경 이미지 출력
       
        bufferg.clearRect(0, 0, width, height);  //화면 지우기
        if (move_background > -700) {   //원래 0인 move_background 변수가 -700보다 크면 실행
            bufferg.drawImage(background_img, move_background, 0, this);
            move_background -= 1;   //move_background 변수를 0에서 -1만큼 계속 줄여서 배경 왼쪽으로 -1씩 이동
        } else {
            move_background = 0;    //0으로 초기화
        }
    }

    public void Print_Arrow() {    //깃털 이미지 출력
        for (int i = 0; i < arr_arrow.size(); ++i) {
            arrow = (Arrow) (arr_arrow.get(i));
            bufferg.drawImage(arrow_img, arrow.x, arrow.y, this);
        }
    }

    public void Print_Alien() {  //후추 이미지 출력
        for (int i = 0; i < arr_alien.size(); ++i) {
            alien = (Alien) (arr_alien.get(i));
            bufferg.drawImage(alien_img, alien.x, alien.y-35, this);
        }
    }

    public void Print_Explosion() { //폭발 효과 출력
        for (int i = 0; i < arr_explosion.size(); ++i) {
            explosion = (Explosion) arr_explosion.get(i);
            if (explosion.situation == 0) {    //폭발 상황 "0"이면
                if (explosion.maintain_int < 10) {  //10번 반복될 동안 폭발 효과 유지시키기
                    bufferg.drawImage(explosion_img, explosion.x - explosion_img.getWidth(null) / 2, explosion.y - explosion_img.getHeight(null) / 2, this);
                }
            } else {    //폭발 상황 "1"이면
                if (explosion.maintain_int < 8) {   //8번 반복될 동안 폭발 효과 유지시키기
                    bufferg.drawImage(explosion_img, explosion.x + 120, explosion.y + 15, this);
                }
            }
        }
    }

    public void Print_GAMEOVER(){   //게임오버
    	new End_F1();
        setVisible(false);
    }

    public void Print_Text() {  //게임 진행 상황 보여주기
    	bufferg.setFont(new Font("Rockwell", Font.BOLD, 30));  //폰트 설정
        bufferg.drawString("<"+round+" ROUND>", 1050,120);    //라운드 표시
        bufferg.setFont(new Font("Rockwell", Font.BOLD, 23));  //폰트 설정
        bufferg.drawString("SCORE : " + score, 1050, 145);    //점수 표시
        bufferg.drawString("LIFE : " + life, 1050, 170);      //생명 갯수 표시
    }

    ////키보드 입력
    boolean Up = false;  //키보드 입력 처리를 위한 변수
    boolean Down = false;
    boolean Left = false;
    boolean Right = false;
    boolean Space = false;
    public void KeyWok() {
        //키보드 입력 방향으로 플레이어 5씩 이동
    	if(x<=-10) {
    		x=0;
    	}
    	if(x>=1200) {
    		x=1200;
    	}
    	if(y<=50) {
    		y=50;
    	}
    	if(y>=720) {
    		y=720;
    	}
        if (Up == true) {
            y -= 5;   //화면 밖으로 못나가게 하기
        }
        if (Down == true) {
            y += 5;
        }
        if (Left == true) {
            x -= 5;
        }
        if (Right == true) {
            x += 5;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { } //키보드가 타이핑될 때 이벤트 처리하는 곳

    public void keyPressed(KeyEvent e) {
        //키보드가 눌러졌을때 이벤트 처리하는 곳
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                Up = true;
                break;
            case KeyEvent.VK_DOWN:
                Down = true;
                break;
            case KeyEvent.VK_LEFT:
                Left = true;
                break;
            case KeyEvent.VK_RIGHT:
                Right = true;
                break;
            case KeyEvent.VK_SPACE:
                Space = true;
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        //키보드가 눌러졌다가 떼어졌을때 이벤트 처리하는 곳
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                Up = false;
                break;
            case KeyEvent.VK_DOWN:
                Down = false;
                break;
            case KeyEvent.VK_LEFT:
                Left = false;
                break;
            case KeyEvent.VK_RIGHT:
                Right = false;
                break;
            case KeyEvent.VK_SPACE:
                Space = false;
                break;
        }
    }
}

public class BirdGame {
    public static void main(String[] args) {
        Bird game = new Bird();
    }
}