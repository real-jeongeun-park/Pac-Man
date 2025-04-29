package Pacman;
import javax.swing.JFrame;

public class Pacman extends JFrame{
	public Pacman() {
		setTitle("Pacman");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(new Model());
		setResizable(false);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new Pacman();
	}
}
