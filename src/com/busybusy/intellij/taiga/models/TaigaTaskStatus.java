package com.busybusy.intellij.taiga.models;

/**
 * Created by Tjones on 5/27/15.
 */
public class TaigaTaskStatus
{
	private String mTaigaId;
	private String mName;
	private String mSlug;
	private boolean mClosed;


	public String getTaigaId()
	{
		return mTaigaId;
	}

	public TaigaTaskStatus setTaigaId(String taigaId)
	{
		mTaigaId = taigaId;
		return this;
	}

	public String getName()
	{
		return mName;
	}

	public TaigaTaskStatus setName(String name)
	{
		mName = name;
		return this;
	}

	public String getSlug()
	{
		return mSlug;
	}

	public TaigaTaskStatus setSlug(String slug)
	{
		mSlug = slug;
		return this;
	}

	public boolean isClosed()
	{
		return mClosed;
	}

	public TaigaTaskStatus setClosed(boolean closed)
	{
		mClosed = closed;
		return this;
	}


}
