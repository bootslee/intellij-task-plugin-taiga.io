package com.busybusy.intellij.taiga.models;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tjones on 5/26/15.
 */
@Tag("TaigaProject")
public class TaigaProject
{
	private String mProjectId;
	private String mProjectTitle;
	private String mSlug;
	private List<TaigaTaskStatus> mStatusList = new ArrayList<TaigaTaskStatus>();

	public TaigaProject()
	{
	}

	public String getProjectId()
	{
		return mProjectId;
	}

	public void setProjectId(String mProjectId)
	{
		this.mProjectId = mProjectId;
	}

	public String getProjectTitle()
	{
		return mProjectTitle;
	}

	public void setProjectTitle(String mProjectTitle)
	{
		this.mProjectTitle = mProjectTitle;
	}

	public String getSlug()
	{
		return mSlug;
	}

	public void setSlug(String slug)
	{
		this.mSlug = slug;
	}

	@AbstractCollection(surroundWithTag = false, elementTag = "TaigaTaskStatus")
	public List<TaigaTaskStatus> getStatusList()
	{
		return mStatusList;
	}

	public TaigaProject setStatusList(List<TaigaTaskStatus> statusList)
	{
		mStatusList = statusList;
		return this;
	}

	public void addTaigaTaskStatus(TaigaTaskStatus status)
	{
		if (!mStatusList.contains(status))
		{
			mStatusList.add(status);
		}
	}

	public void removeTaigaTaskStatus(TaigaTaskStatus status)
	{
		mStatusList.remove(status);
	}

	@Override
	public String toString()
	{
		return mProjectTitle != null ? mProjectTitle : super.toString();
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

		TaigaProject that = (TaigaProject) o;

		if (mProjectId != null ? !mProjectId.equals(that.mProjectId) : that.mProjectId != null)
		{
			return false;
		}
		if (mProjectTitle != null ? !mProjectTitle.equals(that.mProjectTitle) : that.mProjectTitle != null)
		{
			return false;
		}
		return !(mSlug != null ? !mSlug.equals(that.mSlug) : that.mSlug != null);

	}
}
