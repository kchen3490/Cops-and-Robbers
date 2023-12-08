import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

//Picture Project by Kai C.

public class CopsnRobbers extends FlexiblePictureExplorer {
	public static final String folderPath = "CopsnRobbersPics\\";
	
	public static final int row = 4;
	public static final int column = 8;
	public static final int empty = 0;
	public static final int playerCover = 1;
	public static final int computerCover = 2;
	public static final int selectedPlayerCover = 4;
	public static final int possibleCover = 5;
	public static final int obstacleCover = 6;
	
	public static final int columnBasePix = 163;
	public static final int rowBasePix = 123;
	public static final int pixDifference = 120;
	public static int maxRowPix = 720;
	public static int maxColumnPix = 1280;
	
	public static final int maxMove = 25;
	public static int moveCounter = 0;
	public static boolean gameOver = false;
	public static boolean mainScreen = true;
	public static Picture[][] pictureGrid;
	public static int[][] statusGrid;
	
	public static final Picture thumbnail = new Picture(folderPath + "Thumbnail.png");
	public static final Picture playerPiece = new Picture(folderPath + "PlayerPiece.jpg");
	public static final Picture computerPiece = new Picture(folderPath + "ComputerPiece.jpg");
	public static final Picture openSpace = new Picture(folderPath + "OpenSpace.jpg");
	public static final Picture selectedPiece = new Picture(folderPath + "SelectedPiece.jpg");
	public static final Picture possibleArea = new Picture(folderPath + "PossibleArea.jpg");
	public static final Picture obstacleArea = new Picture(folderPath + "ObstacleArea.jpg");
	
	public CopsnRobbers(Picture load) {
		super(load);
		setTitle("Cops & Robbers");
	}
	private void initializeGrid() {
		int count=0;
		int randomRow = (int)(Math.random()*4);
		int obstacleColumn = (int)((Math.random()*6)+1);
		pictureGrid = new Picture[row][column];
		statusGrid = new int[row][column];
		for (int rowIndex=0; rowIndex<row; rowIndex++)
			for (int columnIndex=0; columnIndex<column; columnIndex++) {
				if ((rowIndex==0 && columnIndex==0) || 
						(rowIndex==row-1 && columnIndex==0)) {
					pictureGrid[rowIndex][columnIndex] = playerPiece;
					statusGrid[rowIndex][columnIndex] = playerCover;
				}
				else if (rowIndex==randomRow && columnIndex==column-1) {
					pictureGrid[rowIndex][columnIndex] = computerPiece;
					statusGrid[rowIndex][columnIndex] = computerCover;
				}
				else {
					pictureGrid[rowIndex][columnIndex] = openSpace;
					statusGrid[rowIndex][columnIndex] = empty;
				}
				/*
				//Debugging
				System.out.println("pictureGrid["+rowIndex+"]["+columnIndex+"] = "
						+pictureGrid[rowIndex][columnIndex]);
				System.out.println("statusGrid["+rowIndex+"]["+columnIndex+"] = "
						+statusGrid[rowIndex][columnIndex]);
				*/
			}
		while (count<3) {
			pictureGrid[randomRow][obstacleColumn] = obstacleArea;
			statusGrid[randomRow][obstacleColumn] = obstacleCover;
			randomRow = (int)(Math.random()*4);
			obstacleColumn = (int)((Math.random()*6)+1);
			count++;
		}
		updateImage();
	}
	public void mouseClickedAction(DigitalPicture pict, Pixel pix) {
		int pixRow = pix.getY();
		int pixColumn = pix.getX();
		if (mainScreen) {
			mainScreen=false;
			Picture background = new Picture(folderPath + "background.png");
			thumbnail.copy(background, 0, 0);;
			//Initializing the 4x8 grid (row x column)
			initializeGrid();
			moveUpdate();
		}
		else if (gameOver==false) {
			if ((pixRow>=rowBasePix && pixRow<=maxRowPix-rowBasePix) && 
					(pixColumn>=columnBasePix && 
					pixColumn<=maxColumnPix-columnBasePix)) {
				int currentRow = (pixRow-rowBasePix)/pixDifference;
				int currentColumn = (pixColumn-columnBasePix)/pixDifference;
				if (statusGrid[currentRow][currentColumn]==playerCover) {
					cancelMove();
					statusGrid[currentRow][currentColumn] = selectedPlayerCover;
					pictureGrid[currentRow][currentColumn] = selectedPiece;
					possibleMove(currentRow, currentColumn);
				}				
				else if (statusGrid[currentRow][currentColumn]==possibleCover) {
					statusGrid[currentRow][currentColumn]=playerCover;
					pictureGrid[currentRow][currentColumn]=playerPiece;
					moveUpdate();
					playerMove();
					computerMove();
					if (gameOver==false && moveCounter>=maxMove)
						try {
							gameOver(false);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				else
					cancelMove();
			}
			else
				cancelMove();
		}
	}
	public void possibleMove(int currentRow, int currentColumn) {
		if (currentRow>0) {
			if (statusGrid[currentRow-1][currentColumn]==empty) {
				statusGrid[currentRow-1][currentColumn] = possibleCover;
				pictureGrid[currentRow-1][currentColumn] = possibleArea;
			}
		}
		if (currentRow<row-1) {
			if (statusGrid[currentRow+1][currentColumn]==empty) {
				statusGrid[currentRow+1][currentColumn] = possibleCover;
				pictureGrid[currentRow+1][currentColumn] = possibleArea;
			}
		}
		if (currentColumn>0) {
			if (statusGrid[currentRow][currentColumn-1]==empty) {
				statusGrid[currentRow][currentColumn-1] = possibleCover;
				pictureGrid[currentRow][currentColumn-1] = possibleArea;
			}
		}
		if (currentColumn<column-1) {
			if (statusGrid[currentRow][currentColumn+1]==empty) {
				statusGrid[currentRow][currentColumn+1] = possibleCover;
				pictureGrid[currentRow][currentColumn+1] = possibleArea;
			}
		}
		updateImage();
	}
	public void possibleMove(int randomMove, int currentRow, int currentColumn) {
		int count=0;
		boolean unmoveable = true;
		while (unmoveable) {
			if (count>4) {
				statusGrid[currentRow][currentColumn]=computerCover;
				pictureGrid[currentRow][currentColumn]=computerPiece;
				try {
					gameOver(true);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				unmoveable=false;
			}
			else if (randomMove==0) { //Moves left
				if (currentColumn>0)
					if (statusGrid[currentRow][currentColumn-1]==empty) {
						statusGrid[currentRow][currentColumn-1]
								=computerCover;
						pictureGrid[currentRow][currentColumn-1]
								=computerPiece;
						unmoveable=false;
					}
					else {
						randomMove++;
						count++;
					}
				else {
					randomMove++;
					count++;
				}
			}
			else if (randomMove==1) { //Moves up
				if (currentRow>0)
					if (statusGrid[currentRow-1][currentColumn]==empty) {
						statusGrid[currentRow-1][currentColumn]
								=computerCover;
						pictureGrid[currentRow-1][currentColumn]
								=computerPiece;
						unmoveable=false;
					}
					else {
						randomMove++;
						count++;
					}
				else {
					randomMove++;
					count++;
				}
			}
			else if (randomMove==2) { //Moves right
				if (currentColumn<column-1)
					if (statusGrid[currentRow][currentColumn+1]==empty) {
						statusGrid[currentRow][currentColumn+1]
								=computerCover;
						pictureGrid[currentRow][currentColumn+1]
								=computerPiece;
						unmoveable=false;
					}
					else {
						randomMove++;
						count++;
					}
				else {
					randomMove++;
					count++;
				}
			}
			else if (randomMove==3) { //Moves down
				if (currentRow<row-1)
					if (statusGrid[currentRow+1][currentColumn]==empty) {
						statusGrid[currentRow+1][currentColumn]
								=computerCover;
						pictureGrid[currentRow+1][currentColumn]
								=computerPiece;
						unmoveable=false;
					}
					else {
						randomMove=0;
						count++;
					}
				else {
					randomMove=0;
					count++;
				}
			}
		}
		updateImage();
	}
	public void cancelMove() {
		for (int rowIndex=0; rowIndex<row; rowIndex++)
			for (int columnIndex=0; columnIndex<column; columnIndex++) {
				if (statusGrid[rowIndex][columnIndex] == selectedPlayerCover) {
					statusGrid[rowIndex][columnIndex] = playerCover;
					pictureGrid[rowIndex][columnIndex] = playerPiece;
				}
				if (statusGrid[rowIndex][columnIndex] == possibleCover) {
					statusGrid[rowIndex][columnIndex] = empty;
					pictureGrid[rowIndex][columnIndex] = openSpace;
				}
			}
		updateImage();
	}
	public void playerMove() {
		for (int rowIndex=0; rowIndex<row; rowIndex++)
			for (int columnIndex=0; columnIndex<column; columnIndex++) {
				if (statusGrid[rowIndex][columnIndex] == selectedPlayerCover) {
					statusGrid[rowIndex][columnIndex] = empty;
					pictureGrid[rowIndex][columnIndex] = openSpace;
				}
				if (statusGrid[rowIndex][columnIndex] == possibleCover) {
					statusGrid[rowIndex][columnIndex] = empty;
					pictureGrid[rowIndex][columnIndex] = openSpace;
				}
			}
		moveCounter++;
		moveUpdate();
		updateImage();
	}
	public void computerMove() {
		int randomMove = (int)(Math.random()*4);
		int currentRow=0, currentColumn=0;
		for (int rowIndex=0; rowIndex<row; rowIndex++)
			for (int columnIndex=0; columnIndex<column; columnIndex++) {
				if (statusGrid[rowIndex][columnIndex] == computerCover) {
					currentRow=rowIndex;
					currentColumn=columnIndex;
					statusGrid[rowIndex][columnIndex] = empty;
					pictureGrid[rowIndex][columnIndex] = openSpace;
				}
			}
		possibleMove(randomMove, currentRow, currentColumn);
	}
	public void moveUpdate() {
		Picture moveBackground = new Picture(folderPath + "MoveUpdate.png");
		thumbnail.copy(moveBackground, 630, 800);
		Graphics moveUpdate = thumbnail.getGraphics();
		moveUpdate.setFont(new Font("Calibri", Font.BOLD, 28));
		moveUpdate.setColor(Color.black);
		moveUpdate.drawString("Moves Left: " + (maxMove-moveCounter), 800, 650);
	}
	public void updateImage() {
		for (int rowIndex=0; rowIndex<row; rowIndex++) {
			for (int columnIndex=0; columnIndex<column; columnIndex++) {
				int currentRowPix = rowBasePix + rowIndex*pixDifference;
				int currentColumnPix = columnBasePix + columnIndex*pixDifference;
				thumbnail.copy(pictureGrid[rowIndex][columnIndex], 
						currentRowPix, currentColumnPix);
			}
		}
	}
	private void gameOver(boolean playerWon) throws InterruptedException {
		gameOver = true;
		Thread.sleep(300);
		if (playerWon==false) {
			Picture winPic = new Picture(folderPath + "RobbersWin.png");
			new CopsnRobbers(winPic);
		}
		else {
			Picture losePic = new Picture(folderPath + "CopsWin.png");
			new CopsnRobbers(losePic);
		}
	}
	public static void main(String arg[]) {
		new CopsnRobbers(thumbnail);
	}
}
