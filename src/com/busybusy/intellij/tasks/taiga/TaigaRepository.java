package com.busybusy.intellij.tasks.taiga;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.BaseRepositoryImpl;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Tjones on 5/21/15.
 */
public class TaigaRepository extends BaseRepositoryImpl {

    private static final Logger LOG = Logger.getInstance(TaigaRepository.class);

    private String mAuthKey = null;

    @NonNls public static final String SERVER_URL = "serverUrl";
    @NonNls public static final String USERNAME = "username";
    @NonNls public static final String PASSWORD = "password";

    private final TemplateVariable.FactoryVariable myServerTemplateVariable = new TemplateVariable.FactoryVariable(SERVER_URL) {
        @NotNull
        @Override
        public String getValue() {
            return TaigaRepository.this.getUrl();
        }
    };
    private final TemplateVariable.FactoryVariable myUserNameTemplateVariable = new TemplateVariable.FactoryVariable(USERNAME) {
        @NotNull
        @Override
        public String getValue() {
            return TaigaRepository.this.getUsername();
        }
    };
    private final TemplateVariable.FactoryVariable myPasswordTemplateVariable = new TemplateVariable.FactoryVariable(PASSWORD, true) {
        @NotNull
        @Override
        public String getValue() {
            return TaigaRepository.this.getPassword();
        }
    };

    private static final String kAuthEndpoint = "/auth";

    @SuppressWarnings("UnusedDeclaration")
    public TaigaRepository() {

    }

    @SuppressWarnings("UnusedDeclaration")
    public TaigaRepository(TaskRepositoryType type) {
        super(type);
        setUseHttpAuthentication(false);
        setUrl("https://api.taiga.io/api/v1");
    }

    @SuppressWarnings("UnusedDeclaration")
    public TaigaRepository(TaigaRepository other) {
        super(other);
    }

    @Nullable
    @Override
    public Task findTask(@NotNull String s) throws Exception {
        return null;
    }

    @NotNull
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public BaseRepository clone() {
        return new TaigaRepository(this);
    }

    @Nullable
    @Override
    public CancellableConnection createCancellableConnection() {
        return new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                JsonArray response = executeMethod(getAuthMethod());
                if(response != null && response.get(0) == null)
                {
                    JsonObject body = (JsonObject) response.get(0);
                    if(body.has("_error_message")) {
                        throw new Exception("Authentication failed with server error: " + body.get("_error_message").getAsJsonPrimitive().getAsString());
                    }
                    else {
                        setAuthKey(body);
                    }
                }
                else
                {
                    throw new Exception("Unknown error: Auth method body came back null.");
                }
            }

            @Override
            public void cancel() {
                //Jetbrains left this method blank in their generic task repo as well. Just let it time out?
            }
        };
    }

    @Override
    public boolean isConfigured() {
        boolean result = true;
        if (!super.isConfigured()) {
            result = false;
        }
        if (result && StringUtil.isEmpty(myServerTemplateVariable.getValue()))
        {
            result = false;
        }
        if (result && StringUtil.isEmpty(myUserNameTemplateVariable.getValue()))
        {
            result = false;
        }
        if (result && StringUtil.isEmpty(myPasswordTemplateVariable.getValue()))
        {
            result = false;
        }
        return result;
    }

    private JsonArray executeMethod(HttpMethod method) throws Exception {
        method.addRequestHeader("Content-type","application/json");
        if(mAuthKey != null)
        {
            method.addRequestHeader("Authorization", "Bearer " + mAuthKey);
        }
        getHttpClient().executeMethod(method);

        JsonElement json = new JsonParser().parse(method.getResponseBodyAsString());
        JsonArray responseBody = json.getAsJsonArray();

        if (method.getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception("Request failed with HTTP error: " + method.getStatusText());
        }
        return responseBody;
    }

    private HttpMethod getAuthMethod()
    {
        return new PostMethod(getUrl() + kAuthEndpoint);
    }

    private void setAuthKey(JsonObject body) throws Exception
    {
        if(body.has("auth_token"))
        {
            mAuthKey = body.get("auth_token").getAsJsonPrimitive().getAsString();
        }
        else
        {
            throw new Exception("auth_token missing from server response");
        }
    }
}
