
/**
 * Fixed version of Conway's Game of Life simulation
 * @author G. Cope (original)
 * Fixed by: Bug analysis
 */
public class Conway2DFixed {

	private final int width;
	private final int height;
	private final int size;
	private int seedCount = 9500;
	private boolean wrapperMode = false;  // false = bounded, true = wrapper (toroidal)

	byte[] data;
	
	public Conway2DFixed(){
		this(100, 100);
	}
	
	public Conway2DFixed(int width, int height){
		this.width = width;
		this.height = height;
		this.size = width * height;
		data = new byte[size];
	}

	/**
	 * Iterates the game one step forward
	 */
	public void iterate(){
		byte[] prev = new byte[size];
		System.arraycopy(data, 0, prev, 0, size);

		byte[] next = new byte[size];
		for ( int i = 0; i < width; i++ ){
			for ( int j = 0; j < height; j++ ){
				int type = isAlive(i, j, prev);
				
				if ( type > 0 ){
					next[j * width + i] = 1;
				}else{
					next[j * width + i] = 0;
				}
			}
		}
		
		System.arraycopy(next, 0, data, 0, size);
	}
	
	/**
	 * Checks if the cell should be alive in the next generation
	 * Supports both bounded and wrapper (toroidal) modes
	 * @param x The x position
	 * @param y The y position
	 * @param d The grid data
	 * @return 1 if alive, 0 if dead
	 */
	protected int isAlive(int x, int y, byte[] d){
		int count = 0;
		int pos1 = y * width + x;

		// Check all 8 neighbors
		for ( int i = x-1; i <= x + 1; i++ ){
			for ( int j = y - 1; j <= y + 1; j++ ){
				int actualI, actualJ;

				if (wrapperMode) {
					// Wrapper mode: wrap around edges using modulo
					actualI = (i + width) % width;
					actualJ = (j + height) % height;
				} else {
					// Bounded mode: check boundaries
					if ( i < 0 || i >= width || j < 0 || j >= height ){
						continue;  // Skip out-of-bounds neighbors
					}
					actualI = i;
					actualJ = j;
				}

				int pos = actualJ * width + actualI;
				// Skip the cell itself
				if ( pos != pos1 ){
					if ( d[pos] == 1 ){
						count++;
					}
				}
			}
		}

		// Apply Conway's rules
		if ( d[pos1] == 0 ){
			// Dead cell
			if ( count == 3 ){
				return 1;  // Becomes alive (reproduction)
			}
			return 0;  // Stays dead
		}else{
			// Live cell
			if ( count < 2 || count > 3 ){
				return 0;  // Dies (underpopulation or overpopulation)
			}
			return 1;  // Survives
		}
	}
	
	/**
	 * Randomly seeds the grid
	 */
	public void randomSeed(){
		for ( int i = 0; i < seedCount; i++ ){
			int x = (int)(Math.random() * width);
			int y = (int)(Math.random() * height);
			data[y * width + x] = 1;
		}
	}
	
	/**
	 * Seeds a specific pattern at given position
	 */
	public void seedPattern(int startX, int startY, int[][] pattern){
		for ( int j = 0; j < pattern.length; j++ ){
			for ( int i = 0; i < pattern[j].length; i++ ){
				int x = startX + i;
				int y = startY + j;
				if ( x >= 0 && x < width && y >= 0 && y < height ){
					data[y * width + x] = (byte)pattern[j][i];
				}
			}
		}
	}
	
	/**
	 * Retrieves the grid data
	 */
	public byte[] getData(){
		return data;
	}
	
	/**
	 * Counts total alive cells
	 */
	public int countAlive(){
		int count = 0;
		for ( byte cell : data ){
			if ( cell == 1 ) count++;
		}
		return count;
	}
	
	/**
	 * Sets the seed count for random seeding
	 */
	public void setSeedCount(int count){
		this.seedCount = count;
	}
	
	/**
	 * Clears the grid
	 */
	public void clear(){
		for ( int i = 0; i < size; i++ ){
			data[i] = 0;
		}
	}
	
	/**
	 * Toggles between bounded and wrapper (toroidal) mode
	 */
	public void toggleWrapperMode(){
		wrapperMode = !wrapperMode;
	}

	/**
	 * Sets the wrapper mode
	 * @param enabled true for wrapper mode, false for bounded mode
	 */
	public void setWrapperMode(boolean enabled){
		wrapperMode = enabled;
	}

	/**
	 * Gets the current mode
	 * @return true if wrapper mode, false if bounded mode
	 */
	public boolean isWrapperMode(){
		return wrapperMode;
	}

	// Getters for testing
	public int getWidth(){ return width; }
	public int getHeight(){ return height; }
}