/* To use this Demo, place it in a java project with the 
	FlexiblePictureExplorer class and the other classes
	from the AP picture labs. Create a folder under
	that project called Pictures and copy the images
	from the AP picture lab there.
*/
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Concentration extends FlexiblePictureExplorer implements
		ImageObserver {
	public static final int COVERED = 1;
	public static final int REVEALED = 2;
	public static final int PERMANENTLY_REVEALED = 3;

	private final int imgHeight, imgWidth;
	private final int xTiles, yTiles;
	private final int tileWidth, tileHeight;
	private final List<Pixel> revealedLocations = new ArrayList<>();
	private final Picture[][] pictureGrid;
	private final int[][] statusGrid;
	private final Picture coverPicture;
	private int permanentlyRevealedCount = 0;
	private int turnsCounter = 0;

	public Concentration(Picture[] pictures, Picture coverPict) {
		this(pictures, coverPict, 400, 400, 4, 4);
	}
	
	public Concentration(Picture[] pictures, Picture coverPicture, int width,
			int height, int numColumns, int numRows) {
		super(new Picture(height, width));
		setTitle("Concentration");

		imgHeight = height;
		imgWidth = width;
		xTiles = numColumns;
		yTiles = numRows;
		tileWidth = imgWidth / xTiles;
		tileHeight = imgHeight / yTiles;
		pictureGrid = new Picture[xTiles][yTiles];
		statusGrid = new int[xTiles][yTiles];
		this.coverPicture = scaleSquare(coverPicture);

		initGrid(pictures);
		updateImage();
	}

	private void initGrid(Picture[] pictures) {
		ArrayList<Picture> pictureChoices = new ArrayList<>();
		for (Picture pict : pictures) {
			Picture scaled = scaleSquare(pict); // Scale the picture to the appropriate size
			pictureChoices.add(scaled);
			pictureChoices.add(scaled);
		}
		Collections.shuffle(pictureChoices); // Randomizes the order of the list
		for (int x = 0; x < xTiles; x++) {
			for (int y = 0; y < yTiles; y++) {
				pictureGrid[x][y] = pictureChoices.remove(0);
				statusGrid[x][y] = COVERED;
			}
		}
	}

	private Picture scaleSquare(Picture pict) {
		double xScale = tileWidth / ((double) pict.getWidth());
		double yScale = tileHeight / ((double) pict.getHeight());
		Picture scaled;
		Picture centered = new Picture(tileHeight, tileWidth);
		if (xScale < yScale) {
			scaled = pictureScale(pict, xScale, xScale);
			centered.copy(scaled, (tileHeight - scaled.getHeight()) / 2, 0);
		} else {
			scaled = pictureScale(pict, yScale, yScale);
			centered.copy(scaled, 0, (tileWidth - scaled.getWidth()) / 2);
		}
		return centered;
	}

	private Pixel clickedSquare(Pixel clickedPixel) {
		int x = clickedPixel.getX() / tileWidth;
		int y = clickedPixel.getY() / tileHeight;
		return new Pixel(null, x, y);
	}

	private Picture pictureForSquare(Pixel location) {
		return pictureGrid[location.getX()][location.getY()];
	}

	private void checkMatch() {
		Picture pic1 = pictureForSquare(revealedLocations.get(0));
		Picture pic2 = pictureForSquare(revealedLocations.get(1));
		if (pic1.equals(pic2)) {
			for (Pixel loc : revealedLocations) {
				statusGrid[loc.getX()][loc.getY()] = PERMANENTLY_REVEALED;
				permanentlyRevealedCount++;
			}
			revealedLocations.clear();
		}
	}

	@Override
	public void mouseClickedAction(DigitalPicture pict, Pixel pix) {
		Pixel selSquare = clickedSquare(pix);
		if (statusGrid[selSquare.getX()][selSquare.getY()] == COVERED) {
			revealedLocations.add(selSquare);
			statusGrid[selSquare.getX()][selSquare.getY()] = REVEALED;
			turnsCounter++;
			if (revealedLocations.size() == 2) {
				checkMatch();
			} else if (revealedLocations.size() > 2) {
				for (int i = 0; i < 2; i++) {
					Pixel loc = revealedLocations.remove(0);
					statusGrid[loc.getX()][loc.getY()] = COVERED;
				}
			}

			if (permanentlyRevealedCount >= xTiles * yTiles) {
				endGame();
			}
		}
		updateImage();
	}

	private void updateImage() {
		Picture disp = new Picture(imgHeight, imgWidth);
		Graphics2D graphics = disp.createGraphics();
		for (int x = 0; x < xTiles; x++) {
			for (int y = 0; y < yTiles; y++) {
				Picture pict;
				if (statusGrid[x][y] == COVERED) {
					pict = coverPicture;
				} else {
					pict = pictureGrid[x][y];
				}
				// This is used instead of the copy() method for performance
				// reasons
				graphics.drawImage(pict.getBufferedImage(), tileWidth * x,
						tileHeight * y, this);
			}		
		}
		setImage(disp);
		// setImage() changes the title each time it's called
		setTitle("Concentration");
	}

	private void endGame() {
		makePopUp("Congratulations! You won after " + turnsCounter + " turns");
	}

	@Override
	public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5) {
		return true;
	}

	// NOTE: This is mostly copied from the SimplePicture class's scale()
	// method, with one bugfix. If the bug is ever fixed in the SimplePicture
	// class, the scale() method can be safely used
	public static Picture pictureScale(Picture input, double xFactor,
			double yFactor) {
		// set up the scale transform
		AffineTransform scaleTransform = new AffineTransform();
		scaleTransform.scale(xFactor, yFactor);

		// create a new picture object that is the right size
		// BUGFIX (Tim Woodford 2/35/14): correct order of parameters
		Picture result = new Picture((int) (input.getHeight() * yFactor),
				(int) (input.getWidth() * xFactor));

		// get the graphics 2d object to draw on the result
		Graphics graphics = result.getGraphics();
		Graphics2D g2 = (Graphics2D) graphics;

		// draw the current image onto the result image scaled
		g2.drawImage(input.getImage(), scaleTransform, null);

		return result;
	}
	// You can modify this to include your own pictures
	public static void main(String[] args) {
		String basePath = "Pictures\\"; 
		Picture[] pictures = new Picture[] {
				new Picture(basePath + "arch.jpg"),
				new Picture(basePath + "moon-surface.jpg"),
				new Picture(basePath + "femaleLionAndHall.jpg"),
				new Picture(basePath + "redMotorcycle.jpg"),
				new Picture(basePath + "butterfly1.jpg"),
				new Picture(basePath + "robot.jpg"),
				new Picture(basePath + "snowman.jpg"),
				new Picture(basePath + "gorge.jpg") };
		Picture coverPic = new Picture(basePath+"water.jpg");
		new Concentration(pictures, coverPic, 700, 700, 4, 4);
	}
}
