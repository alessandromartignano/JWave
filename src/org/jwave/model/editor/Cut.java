package org.jwave.model.editor;

import java.util.List;

/**
 * This interface models the concept of cuts, segments of specific length
 * that subdivide any given song into multiple pieces. Cuts have a beginning
 * and an end that are relative to the length of the final modified song.
 * 
 * Cuts also contain a list of the segments that make up the cut, segments
 * are similiar to cuts but represent segments of the song relative to
 * the original song, and are used for obtaining actual audio.
 * 
 * All "ms" acronyms stand for milliseconds.
 *
 */
public interface Cut {
    /**
     * Gets the position where this cut begins.
     *          
     * @return
     * 			the position (in ms) of the beginning of the cut.
     */	
	int getFrom();
	
    /**
     * Gets the position where this cut ends.
     *          
     * @return
     * 			the position (in ms) of the end of the cut.
     */		
	int getTo();
	
    /**
     * Sets the position where this cut begins.
     *          
     * @param from
     * 			the position (in ms) of the beginning of the cut.
     */		
	void setFrom(int from);
	
    /**
     * Sets the position where this cut end.
     *          
     * @param to
     * 			the position (in ms) of the end of the cut.
     */			
	void setTo(int to);
	
    /**
     * Gets the length of the cut.
     *          
     * @return
     * 			the length (in ms) of the cut.
     */
	int getLength();
	
    /**
     * Gets a segment by index.
     * 
     * @param i
     * 			index of segment to retrieve.
     *          
     * @return
     * 			the segment.
     */	
	Segment getSegment(int i);
	
    /**
     * Gets a list of all of this cut's segments.
     *          
     * @return
     * 			a list of segments that make up this cut.
     */	
	List<Segment> getSegments();
}
