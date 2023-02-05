package frick;
import java.lang.Math;
import java.awt.*;
import javax.swing.*;
import java.util.Vector;


class Vector2f {
	public float x;
	public float y;
	
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float getLength() {
		return (float)Math.sqrt(x * x + y * y);
	}
	
	public void add_(Vector2f other) {
		this.x += other.x;
		this.y += other.y;
	}
	
	public Vector2f add(Vector2f other) {
		float newX = this.x + other.x;
		float newY = this.y + other.y;
		return new Vector2f(newX, newY);
	}
	
	public Vector2f substract(Vector2f other) {
		return add(other.multiply(-1));
	}
	
	public void substract_(Vector2f other) {
		add_(other.multiply(-1));
	}
	
	public void multiply_(float coef) {
		this.x *= coef;
		this.y *= coef;
	}
	
	public Vector2f multiply(float coef) {
		return new Vector2f(
				this.x * coef,
				this.y * coef);
	}
	
	public float distanceFrom(Vector2f other) {
		Vector2f v = this.add(other.multiply(-1));
		return v.getLength();
	}
	
	public float product(Vector2f other) {
		return other.x * x + other.y * y;
	}
	
	
}


class Ball {
	public static float massCoef = 3f;
	private float radius;
	private float mass;
	public Vector2f pos;
	public Vector2f vel;
	
	public Ball(Vector2f pos, float radius) {
		this.pos = pos;
		this.radius = radius;
		this.mass = Ball.massCoef * radius;
		this.vel = new Vector2f(0, 0);
	}
	
	public Vector2f getImpulse() {
		return vel.multiply(mass);
	}
	
	public float getRadius() {
		return radius;
	}
	
	public float getMass() {
		return mass;
	}
	
	public void updatePos(float deltaT) {
		pos.add_(vel.multiply(deltaT));
	}
	
	public void updateWallCollision(int winW, int winH) {
		if (pos.x - radius <= 0 || pos.x + radius >= winW)
			vel.x = -vel.x;
		if (pos.y - radius <= 0 || pos.y + radius >= winH)
			vel.y = -vel.y;
	}
	
	public boolean doCollideWith(Ball other) {
		float rsum = this.radius + other.radius;
		return other.pos.distanceFrom(this.pos) <= rsum;
	}
}


class Game extends JPanel implements Runnable {
	private Thread gameThread;
	private int FPS = 120;
	private Vector<Ball> balls = new Vector<Ball>();
	private int winHeight, winWidth;
	
	
	public Ball addBall(Vector2f pos, float radius) {
		Ball ball = new Ball(pos, radius);
		balls.add(ball);
		return ball;
	}
	
	Game(int winWidth, int winHeight) {
		this.winWidth = winWidth;
		this.winHeight = winHeight;
		
		Ball b1 = addBall(new Vector2f(100, 500), 15);
		b1.vel = new Vector2f(0, 100);
		//Ball b2 = addBall(new Vector2f(400, 400), 15);
		//b2.vel = new Vector2f(-50, -45);
	}
	
	public void startGameThread() {
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	@Override
	public void run() {
		long updateInterval = 1_000_000_000 / FPS; // interval in ns
		long startTime, execTime, toSleep = 0;
		float deltaTime = 0;
		
		while (gameThread != null) {
			startTime = System.nanoTime();
			
			update(deltaTime);
			repaint();
			
			execTime = System.nanoTime() - startTime;
			toSleep = Math.max(updateInterval - execTime, 0);
			
			deltaTime = (float)(execTime + toSleep) / 1_000_000_000;
			
			try {
				Thread.sleep(toSleep / 1_000_000);
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void update(float deltaTime) {
		//System.out.println(deltaTime);
		for (int i = 0; i < balls.size(); ++i) {
			Ball b1 = balls.get(i);
			for (int j = 0; j < i; ++j) {
				Ball b2 = balls.get(j);
				
				if (!b1.doCollideWith(b2))
					continue;
				
				Vector2f a = b1.vel.substract(b2.vel);
				Vector2f b = b1.pos.substract(b2.pos);
				float c = b1.getMass() + b2.getMass();
				float p = a.product(b) / (float)Math.pow(b.getLength(), 2);
				float k1 = (2 * b2.getMass() / c) * p;
				float k2 = (2 * b1.getMass() / c) * p;
				
				b1.vel.substract_(b.multiply(k1));
				b2.vel.substract_(b.multiply(-k2));
			}
		}
		balls.forEach(b -> {
			b.updatePos(deltaTime);
			b.updateWallCollision(winWidth, winHeight);
		});
		
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		setBackground(Color.WHITE);

		balls.forEach(b -> {
			int radius = (int)b.getRadius();
			int x = (int)(b.pos.x - radius);
			int y = (int)(b.pos.y - radius);
			g2.fillOval(x,  y, 2 * radius, 2 * radius);
		});
		
		g2.dispose();
	}
}



public class Main {
    public static void main(String[] args) {
    	int width = 640;
    	int height = 640;
    	
    	JFrame window = new JFrame();
    	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	window.setResizable(false);
    	window.setSize(new Dimension(width, height));
    	window.setTitle("ma ballz");
    	
    	Game game = new Game(width, height);
    	window.add(game);
    	
    	window.setLocationRelativeTo(null);
    	window.setVisible(true);
    	
    	game.startGameThread();
    }
}
