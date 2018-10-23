package ch.hesge.sedypro.utils;

public class MappingRegion {

	private int storyIndex;
	private int startIndex;
	private int endIndex;
	
	public MappingRegion() {
	}

	public MappingRegion(int storyIndex, int startIndex, int endIndex) {
		this.storyIndex = storyIndex;
		this.startIndex = startIndex;
		this.endIndex   = endIndex;
	}

	public int getStoryIndex() {
		return storyIndex;
	}

	public void setStoryIndex(int storyIndex) {
		this.storyIndex = storyIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getLength() {
		return endIndex - startIndex;
	}

	/**
	 * Retrieve the intersection between this and the region passed in argument.
	 * 
	 * this:	  			   |-------------|
	 * region:		|------------|
	 * return:		       |-----|
	 * 
	 * @param region the region to compare with
	 * @return
	 */
	public MappingRegion getIntersection(MappingRegion region) {
		int start = Math.max(this.startIndex, region.startIndex); 
        int end   = Math.min(this.endIndex, region.endIndex);
        return new MappingRegion(region.storyIndex, start, end);
	}

	/**
	 * Retrieve distance between this region and region passed in argument.
	 * The distance is calculated by comparing center segments distance.
	 * @param region
	 * @return
	 */
	public double getDistance(MappingRegion region) {
		double thisCenter = (this.endIndex + this.startIndex) / 2;
		double regionCenter = (region.endIndex + region.startIndex) / 2;
		return Math.abs(thisCenter - regionCenter);
	}

	/**
	 * Return true if this region intersects with region passed in argument.
	 * 
	 * @param region
	 * @return
	 */
	public boolean isIntersecting(MappingRegion region) {
		
		if (region != null) {
			return this.getIntersection(region).getLength() > 0;
		}

		return false;
	}
	
	/**
	 * Return true if region passed in argument is included in current one.
	 * 
	 * this:	  		|-------------|
 	 * region:			|------|
 	 * 
	 * @param region
	 * @return
	 */
	public boolean isInside(MappingRegion region) {
		return this.startIndex >= region.startIndex && this.endIndex <= region.endIndex;
	}

	/**
	 * Detect if region passed in argument is intersecting to the left with current one.
	 * 
	 * this:	  			   |-------------|
	 * region:		|------------|
	 *   
	 * @param region
	 * @return
	 */
	public boolean hasLeftIntersection(MappingRegion region) {
		return region.startIndex < this.startIndex && region.endIndex > this.startIndex && region.endIndex < this.endIndex;
	}
	
	/**
	 * Detect if region passed in argument is intersecting to the right with current one.
	 * 
	 * this:	  		|-------------|
 	 * region:				|------------|
 	 * 
	 * @param region
	 * @return
	 */
	public boolean hasRightIntersection(MappingRegion region) {
		return region.startIndex < this.endIndex && region.endIndex > this.endIndex && region.startIndex > this.startIndex;
	}
	
	/**
	 * Return true if current region immediatly follows the region passed in argument.
	 * 
	 * current region:	|-----|
	 * region:			            |----|
	 * 
	 * @param region
	 * @return
	 */
	public boolean isFollowing(MappingRegion region) {
		return this.startIndex > region.endIndex;
	}

}
