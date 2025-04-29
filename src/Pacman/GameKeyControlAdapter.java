package Pacman;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameKeyControlAdapter extends KeyAdapter {
	private Model model;
	
	public GameKeyControlAdapter(Model model) {
		this.model = model;
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if(model.getGameStatus()) {
			if(key == KeyEvent.VK_LEFT) {
				model.setMoveControl(-1, 0);
			}
			else if(key == KeyEvent.VK_RIGHT) {
				model.setMoveControl(1, 0);
			}
			else if(key == KeyEvent.VK_UP) {
				model.setMoveControl(0, -1);
			}
			else if(key == KeyEvent.VK_DOWN) {
				model.setMoveControl(0, 1);
			}
			else if(key == KeyEvent.VK_ESCAPE && model.getTimer().isRunning()) {
				model.setGameStatus(false);
			}
		}
		else {
			// 현재 게임이 실행되고 있지 않다
			if(key == KeyEvent.VK_SPACE) {
				model.initGame();
				model.setGameStatus(true);
				model.setEndStatus(false);
			}
		}
    }
	
}
