/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class Breakout extends GraphicsProgram implements ActionListener, KeyListener {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;
	private static final int BALL_DIAMETER = BALL_RADIUS * 2;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;
	private int triesLeft = NTURNS;

///** Amount Y velocity is increased each cycle as a
//* result of gravity */
//	private static final double GRAVITY = 3;

/** Animation delay or pause time between ball moves */
	private static final int DELAY = 25;

///** Amount Y Velocity is reduced when it bounces */
//	private static final double BOUNCE_REDUCE = 0.9;

	private RandomGenerator rgen = RandomGenerator.getInstance();

/** Starting X and Y Velocties */
	private static final double X_VEL = 1; 
	private double vX = X_VEL;
	private double vY = 3.0;

	private GPoint lastPosition;
	private GRect coloredBrick;
	private GRect paddle;
	private GOval ball;
	private GObject collider;

	private static final int TOTAL_BRICKS = NBRICKS_PER_ROW * NBRICK_ROWS;
	private int numberOfBricksLeft = TOTAL_BRICKS;

//	I decided not to use this, because it sounded annoying.
//	private static final AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");

	public static void main(String[] args) {
		new Breakout().start(args);
		}

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		/* You fill this in, along with any subsidiary methods */
		setupGame();

		while (!gameOver()) {
			gameplay();
		}
	}

	private void setupGame() {
		setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
		instructions1();
		instructions2();
		instructions3();
		waitForClick();
		removeAll();
		setupBricks();
		putPaddle();
		putBall();
	}

	private void resetPaddleAndBall() {
		putPaddle();
		putBall();
	}

	private GLabel instructions1() {
		String instructionsText = "Click on the paddle and " +
				"drag it to hit the ball.";
		GLabel instructions1 = new GLabel(instructionsText);
		double x = (APPLICATION_WIDTH/2) - (instructions1.getWidth()/2);
		double y = (APPLICATION_HEIGHT/3) - ((instructions1.getAscent() + instructions1.getDescent())/2);
		instructions1.setLocation(x, y);
		add(instructions1);

		return instructions1;
	}

	private GLabel instructions2() {
		String instructionsText = "You have 3 tries.";
		GLabel instructions2 = new GLabel(instructionsText);
		double x = (APPLICATION_WIDTH/2) - (instructions2.getWidth()/2);
		double y = instructions1().getY() + instructions2.getAscent();
		instructions2.setLocation(x, y);
		add(instructions2);

		return instructions2;
	}
	
	private GLabel instructions3() {
		String instructionsText = "Click to begin.";
		GLabel instructions3 = new GLabel(instructionsText);
		double x = (APPLICATION_WIDTH/2) - (instructions3.getWidth()/2);
		double y = instructions2().getY() + instructions3.getAscent();
		instructions3.setLocation(x, y);
		add(instructions3);

		return instructions3;
	}

	private void setupBricks() {
		// Sets up the positions & colors of bricks on the top.

		int xPosition = BRICK_SEP/2;
		int yPosition = BRICK_Y_OFFSET;
		int putBrickToRight = BRICK_WIDTH + BRICK_SEP;
		int putBrickBelow = BRICK_HEIGHT + BRICK_SEP;

		for (int i = 0; i < NBRICK_ROWS; i++){
			for(int j = 0; j < NBRICKS_PER_ROW; j++) {

				coloredBrick = putBrick();
				coloredBrick.setLocation(xPosition, yPosition);
				if (i == 0 || i == 1) {
					coloredBrick.setColor(Color.RED);
				} else if (i == 2 || i == 3) {
					coloredBrick.setColor(Color.ORANGE);
				} else if (i == 4 || i == 5) {
					coloredBrick.setColor(Color.YELLOW);
				} else if (i == 6 || i == 7) {
					coloredBrick.setColor(Color.GREEN);
				} else {
					coloredBrick.setColor(Color.CYAN);
				}
				add(coloredBrick);

				xPosition = xPosition + putBrickToRight;
			}

			xPosition = BRICK_SEP/2;
			yPosition = yPosition + putBrickBelow;
		}
	}

	private GRect filledRectangle(double width, double height) {
		/*	Sets up filled rectangles since all 
			the rectangles in this program are filled.  */ 

		GRect filledRect = new GRect(width, height);
		filledRect.setFilled(true);

		return filledRect;
	}

	private GRect putBrick() {
		coloredBrick = filledRectangle(BRICK_WIDTH, BRICK_HEIGHT);

		return coloredBrick;
	}

	private GRect putPaddle() {
		/*	Sets up & positions the paddle.  
			Y-axis is fixed; inital x-axis is center.  */

		int paddleYPosition = getHeight() - (PADDLE_Y_OFFSET + PADDLE_HEIGHT);
		int paddleInitialXPosition = (getWidth()/2)- (PADDLE_WIDTH/2);

		if (paddle != null) {
			remove(paddle);
		}

		paddle = filledRectangle(PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setLocation(paddleInitialXPosition, paddleYPosition);
		add(paddle);
		addMouseListeners();

		return paddle;
	}

	public void mousePressed(MouseEvent e) {
		// GPoint has X and Y coordinate
		lastPosition = new GPoint(e.getPoint());
	}

	public void mouseDragged(MouseEvent e) {
		// Controls movement & collision detection of paddle.

		int rightWall = APPLICATION_WIDTH;
		int leftWall = 0;
		double changeInX = e.getX() - lastPosition.getX();

		if (changeInX + paddle.getX() + PADDLE_WIDTH > rightWall) {
			changeInX = rightWall - paddle.getX() - PADDLE_WIDTH;
		} else if (changeInX + paddle.getX() < leftWall) {
			changeInX = leftWall - paddle.getX();
		}
		paddle.move(changeInX, 0);
		lastPosition = new GPoint(e.getPoint());
	}

	private GOval putBall() {
		System.out.println("Called putBall().");
		/*	Sets up the location & filledness of the ball.  
			Original x-axis & y-axis are the center.  */

		int ballInitialXLocation = (getWidth()/2)- BALL_RADIUS; 
		int ballInitialYLocation = (getHeight()/2) - BALL_RADIUS;

		ball = new GOval(BALL_DIAMETER, BALL_DIAMETER);
		ball.setLocation(ballInitialXLocation, ballInitialYLocation);
		ball.setFilled(true);
		add(ball);

		vX = rgen.nextDouble(1.0, 3.0);
			if (rgen.nextBoolean(0.5)) {
				vX = -vX;
			}
		vY = 3.0;

		return ball;
	}

	private void gameplay() {
		System.out.println("numberOfBricksLeft = " + numberOfBricksLeft);
		moveBall();
		collisionDetection();
		pause(DELAY);
		System.out.println("numberOfBricksLeft = " + numberOfBricksLeft);
	}

	private void moveBall() {
		System.out.println("Called moveBall().");
		// Makes the ball move.
		ball.move(vX, vY);
		System.out.println("ball.move(vX, vY) = " + "(" + vX + ", " + vY + ")");
		System.out.println("ball.getLocation() = " + ball.getLocation());
	}

	private void collisionDetection() {
		ballCollidesWithWalls();

		collider = getCollidingObject();
		if (collider == paddle) {
			ballCollidesWithPaddle();
		} else if (collider != null){
			ballCollidesWithColoredBrick();
		}
	}

	private void ballCollidesWithWalls() {
		System.out.println("Called ballCollidesWithWalls().");
		int hitCeiling = 0;  // Zero on the y-axis.
		int hitRightWall = APPLICATION_WIDTH - BALL_DIAMETER;
		int hitLeftWall = 0;  // Zero on the x-axis.
		int hitFloor = APPLICATION_HEIGHT;

		if (ball.getY() < hitCeiling) {
//			bounceClip.play();
			vY = -vY;
		}
		if (ball.getX() > hitRightWall) {
//			bounceClip.play();
			vX = -vX;
		}
		if (ball.getX() < hitLeftWall) {
//			bounceClip.play();
			vX = -vX;
		}
		if (ball.getY() > hitFloor) {
			moveTheBallOffScreen();
		}
	}

	private GObject getCollidingObject() {
		System.out.println("Called getCollidingObject().");
		GObject gobj = null;

		if (getElementAt(ball.getX(), ball.getY()) != null) {
			gobj = getElementAt(ball.getX(), ball.getY());
		} else if (getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER) != null) {
			gobj = getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER);
		} else if (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()) != null) {
			gobj = getElementAt(ball.getX() + BALL_DIAMETER, ball.getY());
		} else if (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY() + BALL_DIAMETER) != null) {
			gobj = getElementAt(ball.getX() + BALL_DIAMETER, ball.getY() + BALL_DIAMETER);
		}
		System.out.println("gobj = " + gobj);

		return gobj;
	}

	private void ballCollidesWithPaddle() {
		System.out.println("Called ballCollidesWithPaddle().");
//			bounceClip.play();
			vY = -vY;
	}

//	public void mouseClicked(MouseEvent e) {
//		System.out.println("Debug Code Check For ColoredBrick");
//		getElementAt(e.getX(), e.getY());
//		System.out.println("getElementAt(e.getX(), e.getY()) = " + getElementAt(e.getX(), e.getY()));
//	}

	private void ballCollidesWithColoredBrick() {
		System.out.println("Called ballCollidesWithColoredBrick().");
//		bounceClip.play();
		remove(collider);
		numberOfBricksLeft--;
		vY = -vY;
	}

/** determines if ball has moved off screen,
* if it has, removes bullet, sets variable to null.  */
	private void moveTheBallOffScreen() {
		if (ball != null) {
			if (ball.getY() >= APPLICATION_HEIGHT) {
				remove(ball);
				ball = null;
			}
		}
		GLabel ohNoes = new GLabel("Oh noes!!!");
		ohNoes.setFont("Verdana-bold-45");
		double x = (WIDTH/2) - (ohNoes.getWidth()/2);
		double y = (HEIGHT/2) - ((ohNoes.getAscent() + ohNoes.getDescent())/2);
		ohNoes.setLocation(x, y);
		add(ohNoes);
		waitForClick();
		remove(ohNoes);

		System.out.println("triesLeft = " + triesLeft);
		triesLeft--;
		String msg =	"You have " + triesLeft + " tries left; \n" +
		"and " + numberOfBricksLeft + " bricks left.  \n" +
		"Click to try again?";
		if (triesLeft == 1) {
			msg =	"You have " + triesLeft + " try left; \n" +
			"and " + numberOfBricksLeft + " bricks left.  \n" +
			"Click to try again?";
		}
		
		GLabel tryAgain = new GLabel(msg);
		x = (APPLICATION_WIDTH/2) - (tryAgain.getWidth()/2);
		y = (APPLICATION_HEIGHT/2) - ((tryAgain.getAscent() + tryAgain.getDescent())/2);
		tryAgain.setLocation(x, y);
		add(tryAgain);
		waitForClick();
		remove(tryAgain);
		resetPaddleAndBall();
	}

/** determines if game is over -- true if either
* the all the bricks are destroyed or if there 
* are no more tries left.  */
	private boolean gameOver() {
		boolean result = false;

		if (triesLeft == 0) {
			result = true;

			GLabel gameOver = new GLabel("GAME OVER");
			gameOver.setFont("Verdana-bold-45");
			double x = (APPLICATION_WIDTH/2) - (gameOver.getWidth()/2);
			double y = (APPLICATION_HEIGHT/2) - ((gameOver.getAscent() + gameOver.getDescent())/2);
			gameOver.setLocation(x, y);
			add(gameOver);
		}
		if (numberOfBricksLeft == 0) {
			result = true;

			GLabel winningMessage = new GLabel("You won!!!");
			winningMessage.setFont("Verdana-bold-45");
			double x = (WIDTH/2) - (winningMessage.getWidth()/2);
			double y = (HEIGHT/2) - ((winningMessage.getAscent() + winningMessage.getDescent())/2);
			winningMessage.setLocation(x, y);
			add(winningMessage);

			String statistics = "You have used " + triesLeft + 
			" tries to win this game.";
			if (triesLeft == NTURNS) {
				statistics = "You have won this game with ALL 3 tries left!!! ^_^";
			}
			GLabel stats = new GLabel(statistics);
			stats.setFont("Verdana");
			x = (WIDTH/2) - (stats.getWidth()/2);
			y = winningMessage.getY() + stats.getAscent();
			stats.setLocation(x, y);
			add(stats);
		}

		return result;
	}


}