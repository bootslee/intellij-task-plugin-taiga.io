package com.busybusy.intellij.taiga;

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

/**
 * Created by Tjones on 5/21/15.
 */
@Tag("Taiga.io")
public class TaigaRepository extends BaseRepositoryImpl
{
	private Pattern mPattern = Pattern.compile("(d+)");

	private String mAuthKey = null;
	private String mUserId = null;
	private List<TaigaProject> mProjects = new ArrayList<TaigaProject>();
	private TaigaProject mSelectedProject = null;
	private boolean mFilterByUser;

	private static final String kAuthEndpoint = "/auth";
	private static final String kUserMeEndpoint = "/users/me";
	private static final String kProjectListEndpoint = "/projects?member=";
	private static final String kTaskListEndpoint = "/tasks?project=";
	private static final String kTaskEndpoint = "/tasks/";
	private static final String kTaskStatusEndpoint = "/task-statuses?project=";
	private static final String kAssigendToArg = "&assigned_to=";


	@SuppressWarnings("UnusedDeclaration")
	public TaigaRepository()
	{
		super();
	}

	@SuppressWarnings("UnusedDeclaration")
	public TaigaRepository(TaigaRepositoryType type)
	{
		super(type);
		setUseHttpAuthentication(false);
		setUrl("https://api.taiga.io/api/v1");
	}

	@SuppressWarnings("UnusedDeclaration")
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
		Matcher matcher = mPattern.matcher(taskName);
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

		JsonArray result = executeMethod(new GetMethod(getUrl() + kTaskEndpoint + taigaTask.mTask.getTaskId()));
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
		HttpMethod patchTask = getPatchMethod(getUrl() + kTaskEndpoint + taigaTask.mTask.getTaskId(), data);
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

	public void checkSetup() throws Exception
	{
		String result = "";
		int errors = 0;
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
			tasks = executeMethod(new GetMethod(getUrl() + kTaskListEndpoint + mSelectedProject.getProjectId() + kAssigendToArg + mUserId));
		}
		else
		{
			tasks = executeMethod(new GetMethod(getUrl() + kTaskListEndpoint + mSelectedProject.getProjectId()));
		}
		for (int i = 0; i < tasks.size(); i++)
		{
			JsonObject current = tasks.get(i).getAsJsonObject();
			TaigaRemoteTask raw = new TaigaRemoteTask();

			raw.setStatus((current.get("status").isJsonPrimitive()) ? current.get("status").getAsJsonPrimitive().getAsString() : "")
			   .setRef((current.get("ref").isJsonPrimitive()) ? current.get("ref").getAsJsonPrimitive().getAsString() : "")
			   .setCreatedAt((current.get("created_date").isJsonPrimitive()) ? current.get("created_date").getAsJsonPrimitive().getAsString() : "")
			   .setUpdatedAt((current.get("modified_date").isJsonPrimitive()) ? current.get("modified_date").getAsJsonPrimitive().getAsString() : "")
			   .setDescription((current.get("description").isJsonPrimitive()) ? current.get("description").getAsJsonPrimitive().getAsString() : "")
			   .setSubject((current.get("subject").isJsonPrimitive()) ? current.get("subject").getAsJsonPrimitive().getAsString() : "")
			   .setProjectId((current.get("project").isJsonPrimitive()) ? current.get("project").getAsJsonPrimitive().getAsString() : "")
			   .setTaskId((current.get("id").isJsonPrimitive()) ? current.get("id").getAsJsonPrimitive().getAsString() : "")
			   .setAssignedTo((current.get("assigned_to").isJsonPrimitive()) ? current.get("assigned_to").getAsJsonPrimitive().getAsString() : "");

			if (!raw.isValid()) //had an issue with a json exception. the above ()?x:y check stops the exception. now we need to throwout tasks with missing fields
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
		PostMethod postMethod = new PostMethod(getUrl() + kAuthEndpoint);

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
			JsonArray result = executeMethod(new GetMethod(getUrl() + kUserMeEndpoint));
			mUserId = result.get(0).getAsJsonObject().get("id").getAsJsonPrimitive().getAsString();
		}
	}

	@Transient
	public List<TaigaProject> getProjectList() throws Exception
	{
		ensureUserId();
		if (mProjects == null || mProjects.isEmpty())
		{
			JsonArray query = executeMethod(new GetMethod(getUrl() + kProjectListEndpoint + mUserId));
			List<TaigaProject> result = new ArrayList<TaigaProject>();
			for (int i = 0; i < query.size(); i++)
			{
				TaigaProject project = new TaigaProject();
				project.setProjectId(query.get(i).getAsJsonObject().get("id").getAsJsonPrimitive().getAsString());
				project.setProjectTitle(query.get(i).getAsJsonObject().get("name").getAsJsonPrimitive().getAsString());
				project.setSlug(query.get(i).getAsJsonObject().get("slug").getAsJsonPrimitive().getAsString());
				project.setStatusList(getStatusList(project.getProjectId()));
				result.add(project);
			}

			mProjects = result;
		}
		return mProjects;
	}

	@Transient
	public List<TaigaTaskStatus> getStatusList(String projectId) throws Exception
	{
		JsonArray query = executeMethod(new GetMethod(getUrl() + kTaskStatusEndpoint + projectId));
		List<TaigaTaskStatus> result = new ArrayList<TaigaTaskStatus>();
		for (int i = 0; i < query.size(); i++)
		{
			TaigaTaskStatus status = new TaigaTaskStatus();
			status.setTaigaId(query.get(i).getAsJsonObject().get("id").getAsJsonPrimitive().getAsString())
			      .setName(query.get(i).getAsJsonObject().get("name").getAsJsonPrimitive().getAsString())
			      .setSlug(query.get(i).getAsJsonObject().get("slug").getAsJsonPrimitive().getAsString())
			      .setClosed(query.get(i).getAsJsonObject().get("is_closed").getAsJsonPrimitive().getAsBoolean());
			result.add(status);
		}

		return result;
	}

	public TaigaProject getSelectedProject()
	{
		return mSelectedProject;
	}

	public void setSelectedProject(TaigaProject mSelectedProject)
	{
		this.mSelectedProject = mSelectedProject;
	}

	public String getmAuthKey()
	{
		return mAuthKey;
	}

	public void setmAuthKey(String mAuthKey)
	{
		this.mAuthKey = mAuthKey;
	}

	public String getmUserId()
	{
		return mUserId;
	}

	public void setmUserId(String mUserId)
	{
		this.mUserId = mUserId;
	}

	@AbstractCollection(surroundWithTag = false, elementTag = "TaigaProject", elementTypes = TaigaProject.class)
	public List<TaigaProject> getProjects()
	{
		return mProjects;
	}

	public void setProjects(List<TaigaProject> projects)
	{
		this.mProjects = projects;
	}

	public void addTaigaProject(TaigaProject project)
	{
		if (!mProjects.contains(project))
		{
			mProjects.add(project);
		}
	}

	public void removeTaigaProject(TaigaProject project)
	{
		mProjects.remove(project);
	}

	public boolean isFilterByUser()
	{
		return mFilterByUser;
	}

	public void setFilterByUser(final boolean mFilterByUser)
	{
		this.mFilterByUser = mFilterByUser;
	}

	public static final TaigaProject UNSPECIFIED_PROJECT = new TaigaProject()
	{
		@Override
		public String getProjectTitle()
		{
			return "-- Select A Project --";
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
