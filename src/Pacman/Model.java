package Pacman;
import java.awt.*;
import javax.swing.*; 
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;

public class Model extends JPanel implements ActionListener {
	
	private Dimension dimension;

    private boolean gameGoing = false;
    private boolean dying = false;
    private boolean end = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 10;
    private final int PACMAN_SPEED = 6;

    private int GHOSTS_N, lives, score, level;
    private int[] dx, dy;
    private int[] ghostX, ghostY, ghostDeltaX, ghostDeltaY, ghostSpeed;

    private Image heart, ghost, up, down, left, right, opening, deathAlert;

    private int pacmanX, pacmanY, pacmanDeltaX, pacmanDeltaY;
    private int controlDeltaX, controlDeltaY;

    private final short levelData[] = {
    	19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
        17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
        21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
        17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
        // 0은 파란 블록
        // 16은 white dot
        // 1,2,4,8 전부 border
        
         // 0 = blue block , 1 = left border
      	 // 2 = top border , 4 = right border,
      	 // 8 = bottom border, 16 = white dots
      	 // 0,0 인덱스의 값은 19 = 16+2+1 = left , top, white
      	 // 모양 지정
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;
    
    private Font customFont;
    
    private File ringtone, getCoin;
    private Clip clip, clip2;
    private AudioInputStream audioStream;
    private AudioFormat format;
    private DataLine.Info info;
    

    public Model() {
    	initFont();
    	initSound();
        loadImages();
        initVariables();
        addKeyListener(new GameKeyControlAdapter(this));
        setFocusable(true);
        requestFocus();
    }
    
    private void initFont() {
    	try {
    		InputStream is = getClass().getResourceAsStream("/DungGeunMo.ttf");
    		customFont = Font.createFont(Font.TRUETYPE_FONT, is);
    	    //create the font to use. Specify the size!
    	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    	    //register the font
    	    ge.registerFont(customFont);
    	} catch (IOException e) {
    	    e.printStackTrace();
    	} catch(FontFormatException e) {
    	    e.printStackTrace();
    	}
    }
    
    private void initSound() {
    	try {
    		ringtone = new File("sound/pacman_ringtone.wav");
    		getCoin = new File("sound/pacman_chomp.wav");
    		try {
				audioStream = AudioSystem.getAudioInputStream(ringtone);
				
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
    		
        	format = audioStream.getFormat();
        	info = new DataLine.Info(Clip.class, format);
        	clip = (Clip)AudioSystem.getLine(info);
			clip.open(audioStream);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			
			
			try {
				audioStream = AudioSystem.getAudioInputStream(getCoin);
				
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
			
			format = audioStream.getFormat();
        	info = new DataLine.Info(Clip.class, format);
        	clip2 = (Clip)AudioSystem.getLine(info);
			clip2.open(audioStream);
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void loadImages() {
    	down = new ImageIcon("images/down.gif").getImage();
    	up = new ImageIcon("images/up.gif").getImage();
    	left = new ImageIcon("images/left.gif").getImage();
    	right = new ImageIcon("images/right.gif").getImage();
        ghost = new ImageIcon("images/clyde.png").getImage();
        heart = new ImageIcon("images/heart.png").getImage();
        opening = new ImageIcon("images/opening.png").getImage();
        deathAlert = new ImageIcon("images/deathalert.png").getImage();
    }
    
    private void initVariables() {
        screenData = new short[N_BLOCKS * N_BLOCKS];
        // 24개 x 24개 블록의 데이터를 담은 screenData 생성
        dimension = new Dimension(360, 380);
		setPreferredSize(dimension);
		// Dimension 객체를 생성해서 380x380 화면 사이즈 생성
		
        ghostX = new int[MAX_GHOSTS];
        ghostDeltaX = new int[MAX_GHOSTS];
        ghostY = new int[MAX_GHOSTS];
        ghostDeltaY = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        // 최대 적군 수만큼 배열 설정
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);
        timer.start();
        // 40ms 초 마다 초기화 한다
    }

    private void playGame(Graphics2D g2d) {
        if (dying) {
            die(g2d);

        } else {
            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
    	clip.start();
        g2d.setColor(Color.WHITE);
        g2d.drawImage(opening, 0, 0, this.getWidth(), this.getHeight(), null);
     }
    
    private void showDieScreen(Graphics2D g2d) {
    	g2d.drawImage(deathAlert, 0, 0, this.getWidth(), this.getHeight(), null);
    }

    private void drawScore(Graphics2D g2d) {
    	g2d.setColor(Color.BLACK);
    	g2d.fillRect(0, SCREEN_SIZE, SCREEN_SIZE, (int)dimension.getSize().getHeight() - (SCREEN_SIZE));
        
    	g2d.setFont(customFont);
    	g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 16F));
    	
        g2d.setColor(Color.WHITE);
        g2d.drawString("Level : " + level + " | Score: " + score, SCREEN_SIZE / 2, SCREEN_SIZE + 14);
        // 점수 기록은 checkMaze 에서 score 값 올리며 가능

        for (int i = 0; i < lives; i++) {
            g2d.drawImage(heart, i * 20 + 5, SCREEN_SIZE, 16, 16, this);
        }
    }

    private void checkMaze() {
        boolean finished = true;
        for(int i = 0; i < N_BLOCKS * N_BLOCKS && finished; i++) {
        	if((screenData[i] & 16) != 0) finished = false;
        	// 하나라도 data에 0 이 아닌 게 있을 경우 false
        	// => (white dot 아직 더 먹어야 함)
        }

        if (finished) {
        	// finished == true
        	// => white dot 다 먹었을 경우
            score += 50;
            // score 50 더 주고 다음 n단계 시작
            
            if (GHOSTS_N < MAX_GHOSTS) {
                GHOSTS_N++;
                level++;
                // 적수 최대 12마리
                // 1단계 6, 2단계 7, .. 7단계 12마리..
            }
            
            else if(GHOSTS_N > MAX_GHOSTS) {
            	// 7단계가 끝이고, 이후 8단계 없다.
            	// 끝입니다 표시
            	gameGoing = false;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
                // 적군의 스피드 랜덤으로 증가한다
            }

            initLevel();
        }
    }

    private void die(Graphics2D g2d) {
    	lives--;
    	// 생명 하나 줄어듬

        if (lives == 0) {
            end = true;
            gameGoing = false;
            // 죽음 표시도 하기
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D g2d) {
        for (int i = 0; i < GHOSTS_N; i++) {
        	// n 마리의 적군에 대해 설정
            if (ghostX[i] % BLOCK_SIZE == 0 && ghostY[i] % BLOCK_SIZE == 0) {
            	// 각각 블록 단위로 정의됨
                int position = (ghostX[i] / BLOCK_SIZE) + (N_BLOCKS * (int) (ghostY[i] / BLOCK_SIZE));
                int count = 0;

                if (((screenData[position] & 1) == 0) && ghostDeltaX[i] != 1) {
                	// left border이 아닐 경우, 적군이 오른쪽으로 움직이지 않을 경우
                    dx[count] = -1;
                    dy[count] = 0;
                    // 왼쪽으로
                    count++;
                }

                if (((screenData[position] & 2) == 0) && ghostDeltaY[i] != 1) {
                	// top border이 아닐 경우, 적군이 아래쪽으로 움직이지 않을 경우
                    dx[count] = 0;
                    dy[count] = -1;
                    // 위쪽으로
                    count++;
                }

                if (((screenData[position] & 4) == 0) && ghostDeltaX[i] != -1) {
                	// right border이 아닐 경우, 적군이 왼쪽으로 움직이지 않을 경우
                    dx[count] = 1;
                    // 오른쪽으로
                    dy[count] = 0;
                    count++;
                }

                if (((screenData[position] & 8) == 0) && ghostDeltaY[i] != -1) {
                	// bottom border가 아닐 경우, 적군이 위쪽으로 움직이지 않을 경우 
                    dx[count] = 0;
                    dy[count] = 1;
                    // 아래쪽으로
                    count++;
                }

                if (count == 0) {
                	// dx, dy 배열 속 값이 없음
                	// 위의 조건식에 하나도 해당하지 않음
                    if ((screenData[position] & 15) == 15) {
                    	// 적군의 현재 위치의 screenData 값이 15일 경우
                        ghostDeltaX[i] = 0;
                        ghostDeltaY[i] = 0;
                    } else {
                        ghostDeltaX[i] = -ghostDeltaX[i];
                        ghostDeltaY[i] = -ghostDeltaY[i];
                    }

                }
                else {
                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghostDeltaX[i] = dx[count];
                    ghostDeltaY[i] = dy[count];
                    // 랜덤으로 움직이는 위치 지정해 준다
                }
            }

            ghostX[i] = ghostX[i] + (ghostDeltaX[i] * ghostSpeed[i]);
            ghostY[i] = ghostY[i] + (ghostDeltaY[i] * ghostSpeed[i]);
            
            drawGhost(g2d, ghostX[i] + 1, ghostY[i] + 1);

            if (pacmanX > (ghostX[i] - 12) && pacmanX < (ghostX[i] + 12)
                    && pacmanY > (ghostY[i] - 12) && pacmanY < (ghostY[i] + 12)
                    && gameGoing) {
                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
    	g2d.drawImage(ghost, x, y, 18, 18, this);
    }

    private void movePacman() {
        short ch;
        if (pacmanX % BLOCK_SIZE == 0 && pacmanY % BLOCK_SIZE == 0) {
        	// 팩맨은 블록 단위로 정의됨
            int position = (pacmanX / BLOCK_SIZE) + (N_BLOCKS * (int) (pacmanY / BLOCK_SIZE));
            // 현재 pacman의 위치 인덱스 정보 받아옴
            ch = screenData[position];
            // 현재 pacman의 위치에 대한 정보 받아옴

            if ((ch & 16) != 0) {
            	// white dot의 위치에 있다 => white dot을 먹음
            	clip2.setMicrosecondPosition(0);
            	clip2.start();
                
                if(!clip2.isRunning()) {
                	clip2.start();
                }
                
            	screenData[position] = (short)(ch & 15);
                // 기존 white dot의 값을 제외한 나머지 border 등의 값을 대입함.
                // white dot만 있을 경우 0으로 처리.
                score++;
                // 스코어 +1
            }

            if (controlDeltaX != 0 || controlDeltaY != 0) {
            	// 화살표 제어키 받아온 경우
                if (!((controlDeltaX == -1 && controlDeltaY == 0 && (ch & 1) != 0)
                        || (controlDeltaX == 1 && controlDeltaY == 0 && (ch & 4) != 0)
                        || (controlDeltaX == 0 && controlDeltaY == -1 && (ch & 2) != 0)
                        || (controlDeltaX == 0 && controlDeltaY == 1 && (ch & 8) != 0))) {
                	// 왼쪽 키, 현재 위치가 left border
                	// 오른쪽 키, 현재 위치가 right border
                	// 위쪽 키, 현재 위치가 top border
                	// 아래쪽 키, 현재 위치가 bottom border
                	// 이 아닌 경우
                    pacmanDeltaX = controlDeltaX;
                    pacmanDeltaY = controlDeltaY;
                    // 델타 x, y 를 해당 제어키의 방향으로 바꿈
                }
            }

            // 가만히 서 있는지 체크
            if ((pacmanDeltaX == -1 && pacmanDeltaY == 0 && (ch & 1) != 0)
                    || (pacmanDeltaX == 1 && pacmanDeltaY == 0 && (ch & 4) != 0)
                    || (pacmanDeltaX == 0 && pacmanDeltaY == -1 && (ch & 2) != 0)
                    || (pacmanDeltaX == 0 && pacmanDeltaY == 1 && (ch & 8) != 0)) {
            	// 벽에 부딪혀서 갈 길이 없음
                pacmanDeltaX = 0;
                pacmanDeltaY = 0;
                // 현재 위치에 고정
            }
        }
         
        pacmanX = pacmanX + PACMAN_SPEED * pacmanDeltaX;
        pacmanY = pacmanY + PACMAN_SPEED * pacmanDeltaY;
        // 위치 재설정
    }

    private void drawPacman(Graphics2D g2d) {

        if (controlDeltaX == -1) {
        	// 왼쪽 화살표 눌림
        	g2d.drawImage(left, pacmanX + 1, pacmanY + 1, this);
        	// 현재 위치에 생성
        } else if (controlDeltaX == 1) {
        	// 오른쪽 화살표 눌림
        	g2d.drawImage(right, pacmanX + 1, pacmanY + 1, this);
        } else if (controlDeltaY == -1) {
        	// 위쪽 화살표 눌림
        	g2d.drawImage(up, pacmanX + 1, pacmanY + 1, this);
        } else {
        	// 아래쪽 화살표 눌림
        	g2d.drawImage(down, pacmanX + 1, pacmanY + 1, this);
        }
    }

    private void drawMaze(Graphics2D g2d) {
    	// 맵 그리기
        int i = 0;
        for (int y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (int x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {
                g2d.setColor(new Color(0,72,251));
                
                if ((levelData[i] == 0)) { 
                    g2d.setStroke(new BasicStroke(0));
                	g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                	// screenData는 0으로 변할 수 있으니 변하지 않는 levelData로 설정
                 }
                
		       	 // 0 = blue block , 1 = left border
		       	 // 2 = top border , 4 = right border,
		       	 // 8 = bottom border, 16 = white dots
		       	 // 0,0 인덱스의 값은 19 = 16+2+1 = left , top, white
		       	 // 모양 지정
                
                g2d.setStroke(new BasicStroke(7));
	       	 
                if ((screenData[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) { 
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) { 
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) { 
                	g2d.setStroke(new BasicStroke(0));
                    g2d.setColor(Color.WHITE)  ;
                    g2d.fillOval(x + 10, y + 10, 5, 5);
               }
                
                i++;
            }
        }
    }

    public void initGame() {
    	lives = 3;
    	// 생명 3개
        score = 0;
        // 점수 0점     
        GHOSTS_N = 3;
        // 적군 수 6마리
        currentSpeed = 3;
        // 레벨이 올라가면 currentSpeed도 함께 증가
        level = 1;
        
        initLevel();
    }

    private void initLevel() {
        for (int i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
            // LevelData의 모든 데이터 정보를 screenData로 복사
        }

        continueLevel();
    }

    private void continueLevel() {
    	int dx = 1;
        int random;

        // 적군 설정
        for (int i = 0; i < GHOSTS_N; i++) {
            ghostY[i] = 4 * BLOCK_SIZE; 
            ghostX[i] = 4 * BLOCK_SIZE;
            // 시작 위치 지정
            ghostDeltaY[i] = 0;
            ghostDeltaX[i] = dx;
            dx = -dx;
            // 가장 처음 시작할 때, 첫 적군은 오른쪽으로, 두번째 적군은 왼쪽으로만 ..
            // 홀수번째 적군이 오른쪽, 짝수번째 적군이 왼쪽으로 움직인다. 
            
            random = (int) (Math.random() * (currentSpeed + 1));
            // 가장 처음 시작할 때는, 0~3 사이
            
            if (random > currentSpeed) {
                random = currentSpeed;                
            }

            ghostSpeed[i] = validSpeeds[random];
            // 처음 시작할 때, 모든 적군은 1,2,3,4 중 랜덤으로 하나의 스피드를 가진다.
        }

        // 팩맨 설정
        pacmanX = (N_BLOCKS - 1) * BLOCK_SIZE;
        pacmanY = (N_BLOCKS - 1) * BLOCK_SIZE;
        // Pacmac은 가장 오른쪽 아래에서 시작
        pacmanDeltaX = 0;	//reset direction move
        pacmanDeltaY = 0;
        // 움직이는 방향 없음
        // pacmanDeltaX는 델타 x로, 팩맨이 움직이는 방향 가리킴
        controlDeltaX = 0;
        controlDeltaY = 0;
        // 화살표 컨트롤 X
        dying = false;
        // 죽지 않음
    }

 
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        // 전체 화면 검은칠

        drawMaze(g2d);
        drawScore(g2d);

        if (gameGoing) {
        	clip.stop();
            playGame(g2d);
        } else {
        	if(end) showDieScreen(g2d);
        	else showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    public boolean getGameStatus() {
    	if(gameGoing) return true;
    	else return false;
    }
    
    public void setGameStatus(boolean status) {
    	gameGoing = status;
    }
    
    public void setEndStatus(boolean status) {
    	end = status;
    }
    
    public void setMoveControl(int controlDeltaX, int controlDeltaY) {
    	this.controlDeltaX = controlDeltaX;
    	this.controlDeltaY = controlDeltaY;
    }
    
    public Timer getTimer() {
    	return timer;
    }

	
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
