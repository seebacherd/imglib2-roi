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
package net.imglib2.roi.util.iterationcode;

import gnu.trove.list.array.TIntArrayList;
import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.Iterator;
import net.imglib2.Localizable;
import net.imglib2.Positionable;

/**
 * TODO
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class IterationCodeIterator< P extends Positionable & Localizable > extends AbstractEuclideanSpace implements Iterator
{
	private final TIntArrayList itcode;

	private final long[] offset;

	private final P position;

	private int itcodeIndex;

	private int itcodeOffsetX;

	private long maxX;

	private boolean hasNextRaster;

	public IterationCodeIterator( final IterationCode iterationCode, final long[] offset, final P position )
	{
		this( iterationCode.getItcode(), offset, position );
	}

	public IterationCodeIterator( final TIntArrayList itcode, final long[] offset, final P position )
	{
		super( position.numDimensions() );
		this.position = position;
		this.itcode = itcode;
		this.offset = offset;
		reset();
	}

	public IterationCodeIterator( final IterationCodeIterator< ? > copyFrom, final P position )
	{
		super( position.numDimensions() );
		this.itcode = copyFrom.itcode;
		this.offset = copyFrom.offset;
		this.position = position;
		this.position.setPosition( copyFrom.position );
		this.itcodeIndex = copyFrom.itcodeIndex;
		this.itcodeOffsetX = copyFrom.itcodeOffsetX;
		this.maxX = copyFrom.maxX;
		this.hasNextRaster = copyFrom.hasNextRaster;
	}

	private void nextRasterStretch()
	{
		int minItcodeX = itcode.get( itcodeIndex++ );
		if ( minItcodeX < 0 )
		{
			for ( int d = 1; d <= -minItcodeX; ++d )
				position.setPosition( itcode.get( itcodeIndex++ ) + offset[ d ], d );
			minItcodeX = itcode.get( itcodeIndex++ );
		}
		position.setPosition( minItcodeX + itcodeOffsetX + offset[ 0 ], 0 );
		maxX = itcode.get( itcodeIndex++ ) + itcodeOffsetX + offset[ 0 ];
		hasNextRaster = itcodeIndex < itcode.size();
	}

	@Override
	public void jumpFwd( final long steps )
	{
		for ( long j = 0; j < steps; ++j )
			fwd();
	}

	@Override
	public void fwd()
	{
		position.fwd( 0 );
		if ( position.getLongPosition( 0 ) > maxX )
			nextRasterStretch();
	}

	@Override
	public void reset()
	{
		itcodeIndex = 0;
		if ( !itcode.isEmpty() )
		{
			itcodeOffsetX = itcode.get( itcodeIndex++ );
			for ( int d = 1; d < n; ++d )
				position.setPosition( itcode.get( itcodeIndex++ ) + offset[ d ], d );
			nextRasterStretch();
			position.bck( 0 );
		}
		else
		{
			hasNextRaster = false;
			position.setPosition( 0, 0 );
			maxX = 0;
		}
	}

	@Override
	public boolean hasNext()
	{
		return hasNextRaster || ( position.getLongPosition( 0 ) < maxX );
	}
}
