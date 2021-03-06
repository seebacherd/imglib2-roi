/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.roi.util;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;

public class RandomAccessibleRegionCursor< T extends BooleanType< T > > extends AbstractWrappedInterval< RandomAccessibleInterval< T > > implements Cursor< T >
{
	private final RandomAccess< T > randomAccess;

	private final int n;

	private long index;

	private final long maxIndex;

	private long lineIndex;

	private final long maxLineIndex;

	public RandomAccessibleRegionCursor( final RandomAccessibleInterval< T > interval, final long size )
	{
		super( interval );
		randomAccess = interval.randomAccess();
		n = numDimensions();
		maxLineIndex = dimension( 0 ) - 1;
		maxIndex = size;
		reset();
	}

	protected RandomAccessibleRegionCursor( final RandomAccessibleRegionCursor< T > cursor )
	{
		super( cursor.sourceInterval );
		this.randomAccess = cursor.randomAccess.copyRandomAccess();
		n = cursor.n;
		lineIndex = cursor.lineIndex;
		maxIndex = cursor.maxIndex;
		maxLineIndex = cursor.maxLineIndex;
	}

	@Override
	public T get()
	{
		return randomAccess.get();
	}

	@Override
	public void jumpFwd( final long steps )
	{
		for ( long i = 0; i < steps; ++i )
			fwd();
	}

	@Override
	public void fwd()
	{
		do
		{
			randomAccess.fwd( 0 );
			if ( ++lineIndex > maxLineIndex )
				nextLine();
		}
		while ( !randomAccess.get().get() );
		++index;
	}

	private void nextLine()
	{
		lineIndex = 0;
		randomAccess.setPosition( min( 0 ), 0 );
		for ( int d = 1; d < n; ++d )
		{
			randomAccess.fwd( d );
			if ( randomAccess.getLongPosition( d ) > max( d ) )
				randomAccess.setPosition( min( d ), d );
			else
				break;
		}
	}

	@Override
	public void reset()
	{
		index = 0;
		lineIndex = 0;
		min( randomAccess );
		randomAccess.bck( 0 );
	}

	@Override
	public boolean hasNext()
	{
		return index < maxIndex;
	}

	@Override
	public T next()
	{
		fwd();
		return get();
	}

	@Override
	public void remove()
	{}

	@Override
	public RandomAccessibleRegionCursor< T > copy()
	{
		return new RandomAccessibleRegionCursor< T >( this );
	}

	@Override
	public RandomAccessibleRegionCursor< T > copyCursor()
	{
		return copy();
	}

	@Override
	public void localize( final float[] position )
	{
		randomAccess.localize( position );
	}

	@Override
	public void localize( final double[] position )
	{
		randomAccess.localize( position );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return randomAccess.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return randomAccess.getDoublePosition( d );
	}

	@Override
	public void localize( final int[] position )
	{
		randomAccess.localize( position );
	}

	@Override
	public void localize( final long[] position )
	{
		randomAccess.localize( position );
	}

	@Override
	public int getIntPosition( final int d )
	{
		return randomAccess.getIntPosition( d );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return randomAccess.getLongPosition( d );
	}
}
