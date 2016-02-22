package com.busybusy.intellij.taiga;

import com.busybusy.intellij.taiga.constants.ApiConstants;
import com.busybusy.intellij.taiga.models.TaigaProject;
import com.busybusy.intellij.taiga.models.TaigaRemoteTask;
import com.busybusy.intellij.taiga.models.TaigaTaskStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.CustomTaskState;
import com.intellij.tasks.Task;
import com.intellij.tasks.impl.BaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.busybusy.intellij.taiga.utilities.GsonUtilities.getAsStringOr;

/**
 * Created by Tjones on 5/21/15.
 */
@Tag("Taiga.io")
class TaigaRepository extends BaseRepositoryImpl
{
	private String             mAuthKey         = null;
	private String             mUserId          = null;
	private List<TaigaProject> mProjects        = new ArrayList<TaigaProject>();
	private TaigaProject       mSelectedProject = null;
	private boolean mFilterByUser;

	@SuppressWarnings("UnusedDeclaration")
	public TaigaRepository()
	{
		super();
	}

	@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
	public TaigaRepository(TaigaRepositoryType type)
	{
		super(type);
		setUseHttpAuthentication(false);
		setUrl("https://api.taiga.io/api/v1");
	}

	@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
	public TaigaRepository(TaigaRepository other)
	{
		super(other);
		mAuthKey = other.mAuthKey;
		mUserId = other.mUserId;
		mProjects = other.mProjects;
		mSelectedProject = other.mSelectedProject;
		mFilterByUser = other.mFilterByUser;
	}

	@Nullable
	@Override
	public Task findTask(@NotNull String s) throws Exception
	{
		return null;
	}

	@Override
	public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed, @NotNull ProgressIndicator cancelled) throws Exception
	{
		return getIssues();
	}

	@NotNull
	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public TaigaRepository clone()
	{
		return new TaigaRepository(this);
	}

	@Nullable
	@Override
	public CancellableConnection createCancellableConnection()
	{
		return new CancellableConnection()
		{
			@Override
			protected void doTest() throws Exception
			{
				TaigaRepository.this.doTest();
			}

			@Override
			public void cancel()
			{
				//Jetbrains left this method blank in their generic task repo as well. Just let it time out?
			}
		};
	}

	@Nullable
	@Override
	public String extractId(@NotNull String taskName)
	{
		Matcher matcher = Pattern.compile("(d+)").matcher(taskName);
		return matcher.find() ? matcher.group(1) : null;
	}

	@NotNull
	@Override
	public Set<CustomTaskState> getAvailableTaskStates(@NotNull Task task) throws Exception
	{
		Set<CustomTaskState> result = new HashSet<CustomTaskState>();
		if (mSelectedProject != null)
		{
			for (TaigaTaskStatus status : mSelectedProject.getStatusList())
			{
				result.add(new CustomTaskState(status.getTaigaId(), status.getName()));
			}
		}
		return result;
	}

	@Override
	public void setTaskState(@NotNull Task task, @NotNull CustomTaskState state) throws Exception
	{
		TaigaTask taigaTask = null;
		if (task instanceof TaigaTask)
		{
			taigaTask = (TaigaTask) task;
		}
		else
		{
			Task[] tasks = getIssues();
			if (tasks != null)
			{
				for (Task task_iter : tasks)
				{
					if (task.getId().equals(task_iter.getId()))
					{
						taigaTask = (TaigaTask) task_iter;
					}
				}
			}
			else
			{
				throw new Exception("Unable to get refresh tasks from server");
			}
		}
		if (taigaTask == null)
		{
			throw new Exception("Task not found");
		}

		JsonArray result = executeMethod(new GetMethod(getUrl() + ApiConstants.EndPoint.TASKS + taigaTask.mTask.getTaskId()));
		taigaTask.mTask.setStatus(state.getId());
		JsonObject jsonData = new JsonObject();
		jsonData.addProperty("status", state.getId());
		jsonData.addProperty("version", result.get(0).getAsJsonObject().get("version").getAsJsonPrimitive()
		                                      .getAsString());
		StringRequestEntity data = new StringRequestEntity(
				jsonData.toString(),
				"application/json",
				"UTF-8"
		);
		HttpMethod patchTask = getPatchMethod(getUrl() + ApiConstants.EndPoint.TASKS + taigaTask.mTask.getTaskId(), data);
		executeMethod(patchTask);
	}

	@Override
	protected int getFeatures()
	{
		return NATIVE_SEARCH | STATE_UPDATING;
	}

	private void doTest() throws Exception
	{
		mAuthKey = null;
		checkSetup();
		JsonArray response = executeMethod(getAuthMethod());
		if (response != null && response.get(0) != null)
		{
			JsonObject body = (JsonObject) response.get(0);
			if (body.has("_error_message"))
			{
				throw new Exception("Authentication failed with server error: " + body.get("_error_message")
				                                                                      .getAsJsonPrimitive()
				                                                                      .getAsString());
			}
			else
			{
				setAuthKey(body);
			}
		}
		else
		{
			throw new Exception("Unknown error: Auth method body came back null.");
		}
	}

	@Override
	public boolean isConfigured()
	{
		boolean result = true;
		if (!super.isConfigured())
		{
			result = false;
		}
		if (result && StringUtil.isEmpty(this.getUrl()))
		{
			result = false;
		}
		if (result && StringUtil.isEmpty(this.getUsername()))
		{
			result = false;
		}
		if (result && StringUtil.isEmpty(this.getPassword()))
		{
			result = false;
		}
		return result;
	}

	private void checkSetup() throws Exception
	{
		String result = "";
		int    errors = 0;
		if (StringUtil.isEmpty(getUrl()))
		{
			result += "Server";
			errors++;
		}
		if (StringUtil.isEmpty(getUsername()))
		{
			result += !StringUtils.isEmpty(result) ? " & " : "";
			result += "Username";
			errors++;
		}
		if (StringUtil.isEmpty(getPassword()))
		{
			result += !StringUtils.isEmpty(result) ? " & " : "";
			result += "Password";
			errors++;
		}
		if (!result.isEmpty())
		{
			throw new Exception(result + ((errors > 1) ? " are required" : " is required"));
		}
	}

	private Task[] getIssues() throws Exception
	{
		if (mSelectedProject.getProjectId().equals("-1"))
		{
			return null;
		}
		List<TaigaTask> result = new ArrayList<TaigaTask>();

		JsonArray tasks;
		if (mFilterByUser)
		{
			tasks = executeMethod(new GetMethod(getUrl() + ApiConstants.EndPoint.TASK_LIST + mSelectedProject.getProjectId() + ApiConstants.Arguments.ASSIGNED_TO + mUserId));
		}
		else
		{
			tasks = executeMethod(new GetMethod(getUrl() + ApiConstants.EndPoint.TASK_LIST + mSelectedProject.getProjectId()));
		}
		for (int i = 0; i < tasks.size(); i++)
		{
			JsonObject      current = tasks.get(i).getAsJsonObject();
			TaigaRemoteTask raw     = new TaigaRemoteTask(current);

			if (!raw.isValid()) //we need to throwout tasks with missing fields
			{
				continue;
			}

			TaigaTask mapped = new TaigaTask(this, raw);
			result.add(mapped);
		}
		Collections.sort(result);
		Task[] primArray = new Task[result.size()];
		return result.toArray(primArray);
	}

	private JsonArray executeMethod(@NotNull HttpMethod method) throws Exception
	{

		if (mAuthKey != null)
		{
			method.addRequestHeader("Content-type", "application/json");
			method.addRequestHeader("Authorization", "Bearer " + mAuthKey);
			method.addRequestHeader("x-disable-pagination", "True");
		}
		else
		{
			method.addRequestHeader("Content-type", "application/x-www-form-urlencoded");
		}
		getHttpClient().executeMethod(method);

		if (method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
		{
			JsonObject json = new JsonParser().parse(new InputStreamReader(method.getResponseBodyAsStream()))
			                                  .getAsJsonObject();
			if (json.has("_error_message") && json.get("_error_message").getAsJsonPrimitive().getAsString()
			                                      .equals("Invalid token"))
			{
				doTest();
				getHttpClient().executeMethod(method);
			}

		}

		JsonElement json = new JsonParser().parse(new InputStreamReader(method.getResponseBodyAsStream()));

		JsonArray responseBody;

		if (json.isJsonArray())
		{
			responseBody = json.getAsJsonArray();
		}
		else
		{
			responseBody = new JsonArray();
			responseBody.add(json);
		}

		if (method.getStatusCode() != HttpStatus.SC_OK)
		{
			throw new Exception("Request failed with HTTP error: " + method.getStatusText());
		}
		return responseBody;
	}

	private HttpMethod getAuthMethod()
	{
		PostMethod postMethod = new PostMethod(getUrl() + ApiConstants.EndPoint.AUTH);

		NameValuePair[] data = {
				new NameValuePair("type", "normal"),
				new NameValuePair("username", myUsername),
				new NameValuePair("password", myPassword)
		};
		postMethod.setRequestBody(data);
		return postMethod;
	}

	private HttpMethod getPatchMethod(String url, StringRequestEntity data)
	{
		PostMethod patchMethod = new PostMethod(url)
		{
			@Override
			public String getName()
			{
				return "PATCH";
			}
		};
		patchMethod.setRequestEntity(data);
		return patchMethod;
	}

	private void setAuthKey(JsonObject body) throws Exception
	{
		if (body.has("auth_token"))
		{
			mAuthKey = body.get("auth_token").getAsJsonPrimitive().getAsString();
		}
		else
		{
			throw new Exception("auth_token missing from server response");
		}
	}

	private void ensureUserId() throws Exception
	{
		if (mUserId == null || mUserId.isEmpty())
		{
			JsonArray result = executeMethod(new GetMethod(getUrl() + ApiConstants.EndPoint.USER_ME));
			mUserId = result.get(0).getAsJsonObject().get("id").getAsJsonPrimitive().getAsString();
		}
	}

	@Transient
	List<TaigaProject> getProjectList() throws Exception
	{
		ensureUserId();
		if (mProjects == null || mProjects.isEmpty())
		{
			JsonArray          query  = executeMethod(new GetMethod(getUrl() + ApiConstants.EndPoint.PROJECT_LIST + mUserId));
			List<TaigaProject> result = new ArrayList<TaigaProject>();
			for (int i = 0; i < query.size(); i++)
			{
				JsonObject current = query.get(i).getAsJsonObject();

				TaigaProject project = new TaigaProject().setProjectId(getAsStringOr(current, "id", ""))
				                                         .setProjectTitle(getAsStringOr(current, "name", ""))
				                                         .setSlug(getAsStringOr(current, "slug", ""));

				if (!project.isValid())
				{
					continue;
				}
				project.setStatusList(getStatusList(project.getProjectId()));

				result.add(project);
			}

			mProjects = result;
		}
		return mProjects;
	}

	@Transient
	private List<TaigaTaskStatus> getStatusList(String projectId) throws Exception
	{
		JsonArray             query  = executeMethod(new GetMethod(getUrl() + ApiConstants.EndPoint.TASK_STATUS + projectId));
		List<TaigaTaskStatus> result = new ArrayList<TaigaTaskStatus>();
		for (int i = 0; i < query.size(); i++)
		{
			TaigaTaskStatus status  = new TaigaTaskStatus();
			JsonObject      current = query.get(i).getAsJsonObject();
			status.setTaigaId(getAsStringOr(current, "id", ""))
			      .setName(getAsStringOr(current, "name", ""))
			      .setSlug(getAsStringOr(current, "slug", ""))
			      .setClosed(current.get("is_closed").getAsJsonPrimitive().getAsBoolean());
			result.add(status);
		}

		return result;
	}

	@SuppressWarnings("WeakerAccess")
	public TaigaProject getSelectedProject()
	{
		return mSelectedProject;
	}

	@SuppressWarnings("WeakerAccess")
	public void setSelectedProject(TaigaProject mSelectedProject)
	{
		this.mSelectedProject = mSelectedProject;
	}

	@SuppressWarnings("UnusedDeclaration")
	public String getmAuthKey()
	{
		return mAuthKey;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setmAuthKey(String mAuthKey)
	{
		this.mAuthKey = mAuthKey;
	}

	@SuppressWarnings("UnusedDeclaration")
	public String getmUserId()
	{
		return mUserId;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setmUserId(String mUserId)
	{
		this.mUserId = mUserId;
	}

	@SuppressWarnings("UnusedDeclaration")
	@AbstractCollection(surroundWithTag = false, elementTag = "TaigaProject", elementTypes = TaigaProject.class)
	public List<TaigaProject> getProjects()
	{
		return mProjects;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setProjects(List<TaigaProject> projects)
	{
		this.mProjects = projects;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void addTaigaProject(TaigaProject project)
	{
		if (!mProjects.contains(project))
		{
			mProjects.add(project);
		}
	}

	@SuppressWarnings("UnusedDeclaration")
	public void removeTaigaProject(TaigaProject project)
	{
		mProjects.remove(project);
	}

	@SuppressWarnings("WeakerAccess")
	public boolean isFilterByUser()
	{
		return mFilterByUser;
	}

	@SuppressWarnings("WeakerAccess")
	public void setFilterByUser(final boolean mFilterByUser)
	{
		this.mFilterByUser = mFilterByUser;
	}

}
