package com.busybusy.intellij.taiga.models;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * Created by Tjones on 5/27/15.
 */
@Tag("TaigaTaskStatus")
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

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		TaigaTaskStatus that = (TaigaTaskStatus) o;

		if (mClosed != that.mClosed)
		{
			return false;
		}
		if (mTaigaId != null ? !mTaigaId.equals(that.mTaigaId) : that.mTaigaId != null)
		{
			return false;
		}
		if (mName != null ? !mName.equals(that.mName) : that.mName != null)
		{
			return false;
		}
		return !(mSlug != null ? !mSlug.equals(that.mSlug) : that.mSlug != null);

	}
}
