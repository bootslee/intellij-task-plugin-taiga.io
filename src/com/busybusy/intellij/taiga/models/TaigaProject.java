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

	public TaigaProject setProjectId(String mProjectId)
	{
		this.mProjectId = mProjectId;
		return this;
	}

	public String getProjectTitle()
	{
		return mProjectTitle;
	}

	public TaigaProject setProjectTitle(String mProjectTitle)
	{
		this.mProjectTitle = mProjectTitle;
		return this;
	}

	public String getSlug()
	{
		return mSlug;
	}

	public TaigaProject setSlug(String slug)
	{
		this.mSlug = slug;
		return this;
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

	public TaigaProject addTaigaTaskStatus(TaigaTaskStatus status)
	{
		if (!mStatusList.contains(status))
		{
			mStatusList.add(status);
		}
		return this;
	}

	public TaigaProject removeTaigaTaskStatus(TaigaTaskStatus status)
	{
		mStatusList.remove(status);
		return this;
	}

	public boolean isValid()
	{
		return !(mProjectId.equals("") || mProjectTitle.equals("") || mSlug.equals(""));
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

	public static final TaigaProject UNSPECIFIED_PROJECT = new TaigaProject()
	{
		@Override
		public String getProjectTitle()
		{
			return "-- Select A Project (Required) --";
		}

		@Override
		public String getProjectId()
		{
			return "-1";
		}

		@Override
		public String toString()
		{
			return getProjectTitle();
		}
	};
}
